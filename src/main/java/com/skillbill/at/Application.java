package com.skillbill.at;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.skillbill.at.akka.RemoteLoggerTailer;
import com.skillbill.at.guice.GuiceActorUtils;
import com.skillbill.at.guice.GuiceExtension;
import com.skillbill.at.guice.GuiceExtensionImpl;

import akka.actor.ActorSystem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

	private final Injector injector;
	private final ActorSystem system;

	@Inject
	public Application(Injector injector, ActorSystem system) {
		this.injector = injector;
		this.system = system;
	}

	public void run() throws Exception {

		// Add shutdownhook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			LOGGER.info("-------------------------------------------------");
			LOGGER.info(" STOPPED");
			LOGGER.info("-------------------------------------------------");

			system.terminate();
		}));

		system.registerExtension(GuiceExtension.provider);

		// configure Guice
		final GuiceExtensionImpl guiceExtension = GuiceExtension.provider.get(system);
		guiceExtension.setInjector(injector);

		// final File appConf = new File(System.getProperty("config.file", "remote-log.conf"));
		// final Config load = ConfigFactory.parseFile(appConf);

		system.actorOf(GuiceActorUtils.makeProps(system, RemoteLoggerTailer.class), "manager");

		LOGGER.info("-------------------------------------------------");
		LOGGER.info(" STARTED");
		LOGGER.info("-------------------------------------------------");

	}
}
