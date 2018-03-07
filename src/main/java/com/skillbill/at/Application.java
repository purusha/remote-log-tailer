package com.skillbill.at;

import java.io.File;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.skillbill.at.guice.GuiceExtension;
import com.skillbill.at.guice.GuiceExtensionImpl;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {
	
	private Injector injector;

	@Inject
	public Application(Injector injector) {
		this.injector = injector;
	}
	
	public void run() throws Exception {
		
        //create system
        final ActorSystem system = ActorSystem.create("remote-logger", ConfigFactory.load());        
        
		//Add shutdownhook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	        LOGGER.info("-------------------------------------------------");
	        LOGGER.info(" STOPPED");
	        LOGGER.info("-------------------------------------------------");
			
            system.terminate();
        }));
		
		system.registerExtension(GuiceExtension.provider);

        //configure Guice
        final GuiceExtensionImpl guiceExtension = GuiceExtension.provider.get(system);
        guiceExtension.setInjector(injector);
                
        final File appConf = new File(System.getProperty("config.file", "remote-log.conf"));
        final Config load = ConfigFactory.parseFile(appConf);
        
        
        
//        //XXX start only in development environment
//        system
//        	.eventStream()
//        	.subscribe(
//    	        system.actorOf(
//	        		GuiceActorUtils.makeProps(system, RetrieveActors.class), "retrieve"
//	    		), 
//    			DeadLetter.class
//			);
//
//        //XXX create before watcher because ... manager use watcher internally
//        system.actorOf(
//    		GuiceActorUtils.makeProps(system, ExportsManagerActor.class), "manager"
//		);        
//        
//        system.actorOf(
//    		GuiceActorUtils.makeProps(system, FileSystemWatcherActor.class), "watcher"
//		);
//
//        system.actorOf(
//    		GuiceActorUtils.makeProps(system, BulkTimeoutActor.class), "bulk-timeout"
//		);

        LOGGER.info("-------------------------------------------------");
        LOGGER.info(" STARTED");
        LOGGER.info("-------------------------------------------------");

    }
}
