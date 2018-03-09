package com.skillbill.at.akka;

import java.io.File;
import java.util.concurrent.CompletionStage;

import com.google.inject.Inject;
import com.skillbill.at.guice.GuiceAbstractActor;

import akka.actor.ActorRef;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.StreamConverters;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteLoggerTailer extends GuiceAbstractActor {
	
	private Process proc;
	private final Materializer materializer;
	
	@Inject
	public RemoteLoggerTailer(Materializer materializer) {		
		this.materializer = materializer;
		
		try {
			
			this.proc = new ProcessBuilder(
				"bash", "-c", "ssh alan.toro@volta.tomatowin.local \"tail -f /usr/home/alan.toro/smartsend01.contactlab.prod/03/09/local1.log\""
			)	
				.redirectErrorStream(true)
				.start();

			self().tell(new Waiting(), ActorRef.noSender());
			
		} catch (Exception e) {
			LOGGER.error("", e);			
			getContext().stop(getSelf());
		}		
	}

	@Override
	public void postStop() throws Exception {
		super.postStop();
		LOGGER.info("end {} ", getSelf().path());
		
		try {
        	if (proc != null) {  
        		proc.destroy();    
    		}            
        } catch (Exception ignore) {}		
	}
	
	@Override
	public void preStart() throws Exception {
		super.preStart();		
		LOGGER.info("start {} with parent {}", getSelf().path(), getContext().parent().path());
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(Waiting.class, w -> {
				if (proc.isAlive()) {
					handler();
				} else {
					LOGGER.error("process is not alive");
					getContext().stop(getSelf());
				}
			})
			.matchAny(o -> {
				LOGGER.warn("not handled message", o);
				unhandled(o);
			})			
			.build();
	}

	private void handler() {
		final String target = getPath();
		LOGGER.info("target file is {}", target);
		
		CompletionStage<IOResult> stage = StreamConverters.fromInputStream(() -> {
			return proc.getInputStream();
		})
			.map(byteString -> {
				return byteString;
			})									
			.runWith(FileIO.toFile(new File(target)), materializer);						
		
		stage.thenAccept( action -> {
			LOGGER.info("terminated batch on {}", getSelf().path());	                	
		    getContext().stop(getSelf());
		});
		
		stage.exceptionally(e -> {
			LOGGER.error("", e);                	
			getContext().stop(getSelf());
			
			return IOResult.createFailed(1, e);
		});
	}
	
	private String getPath() {
		return "/tmp/" + System.currentTimeMillis();
	}

	private static class Waiting { }

}
