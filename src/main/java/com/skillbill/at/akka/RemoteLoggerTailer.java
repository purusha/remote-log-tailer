package com.skillbill.at.akka;

import java.io.IOException;
import java.io.OutputStream;

import com.google.inject.Inject;
import com.skillbill.at.guice.GuiceAbstractActor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteLoggerTailer extends GuiceAbstractActor {
	
	@Inject
	public RemoteLoggerTailer() {		
		final ProcessBuilder builder = new ProcessBuilder("ssh -f user@host tail -f /var/log/boo.log");
		
		try {
			final Process p = builder.start();
			
			//write password over this
			OutputStream outputStream = p.getOutputStream();
		} catch (IOException e) {
			LOGGER.error("", e);			
			getContext().stop(getSelf());
		}		
	}

	@Override
	public void postStop() throws Exception {
		super.postStop();
		LOGGER.info("end {} ", getSelf().path());
	}
	
	@Override
	public void preStart() throws Exception {
		super.preStart();		
		LOGGER.info("start {} with parent {}", getSelf().path(), getContext().parent().path());
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.matchAny(o -> {
				LOGGER.warn("not handled message", o);
				unhandled(o);
			})			
			.build();
	}

}
