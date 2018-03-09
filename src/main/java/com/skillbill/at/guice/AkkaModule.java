package com.skillbill.at.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;

public class AkkaModule implements Module {
	@Override
	public void configure(Binder binder) {
		
        final ActorSystem actorSystem = ActorSystem.create("remote-logger", ConfigFactory.load());

    	binder
			.bind(ActorSystem.class)
			.toInstance(actorSystem);

        binder
	        .bind(ActorMaterializer.class)
	        .toInstance(ActorMaterializer.create(actorSystem));
    	
		
//		final String property = System.getProperty("config.file", "kilebeat.conf");				
//		final ValidationResponse validResp = new ConfigurationValidator().isValid(new File(property));		
//		
//		if (!validResp.isValid()) {
//			System.err.println("config.file is INVALID ... exit!!?");
//			System.exit(-1);
//		}
//		
//		binder
//			.bind(FileSystemWatcherService.class)
//			.in(Singleton.class);
//		
//		binder
//			.bind(ExportsConfiguration.class)
//			.toInstance(validResp.getConfig());
		
	}
}
