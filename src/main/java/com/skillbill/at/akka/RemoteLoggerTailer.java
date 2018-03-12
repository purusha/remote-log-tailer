package com.skillbill.at.akka;

import java.io.File;
import java.util.concurrent.CompletionStage;

import com.google.inject.Inject;
import com.skillbill.at.guice.GuiceAbstractActor;
import com.skillbill.at.service.SshConnectionBuilder;
import com.skillbill.at.service.LocalFileBuilder;
import com.typesafe.config.Config;

import akka.actor.ActorRef;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.StreamConverters;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteLoggerTailer extends GuiceAbstractActor {

	private final ActorMaterializer materializer;
	private final RemoteLoggerFile rlf;
	private Process proc;	

	@Inject
	public RemoteLoggerTailer(ActorMaterializer materializer, RemoteLoggerFile rlf) {
		this.materializer = materializer;
		this.rlf = rlf;

		try {
			this.proc = new ProcessBuilder("bash", "-c", SshConnectionBuilder.connection(rlf))
				.redirectErrorStream(true)
				.start();

			self().tell(new StartTail(), ActorRef.noSender());

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
		} catch (Exception ignore) { }
	}

	@Override
	public void preStart() throws Exception {
		super.preStart();
		LOGGER.info("start {} with parent {}", getSelf().path(), getContext().parent().path());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(StartTail.class, w -> {
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
		final String target = LocalFileBuilder.getPath(rlf);
		LOGGER.info("target file is {}", target);

		final CompletionStage<IOResult> stage = StreamConverters.fromInputStream(() -> {
				return proc.getInputStream();
			}).map(byteString -> {
				return byteString;
			}).runWith(
				FileIO.toFile(new File(target)), materializer
			);

		stage.thenAccept(action -> {
			LOGGER.info("terminated batch with {} on {}", action.getError(), getSelf().path());
			getContext().stop(getSelf());
		});

		stage.exceptionally(e -> {
			LOGGER.error("", e);
			getContext().stop(getSelf());

			return IOResult.createFailed(1, e);
		});
	}

	private static class StartTail { }
	
	@Getter
	public static class RemoteLoggerFile {
		final String sshUser;
		final String sshHost;
		final String remoteFile;
		
		public RemoteLoggerFile(Config c) {
			this.sshUser = c.getString("ssh-user");
			this.sshHost = c.getString("ssh-host");
			this.remoteFile = c.getString("remote-file");					
		}
	}

}
