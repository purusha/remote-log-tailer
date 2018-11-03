package com.skillbill.at;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.skillbill.at.akka.RemoteLoggerTailer;
import com.skillbill.at.guice.GuiceExtension;
import com.skillbill.at.guice.GuiceExtensionImpl;
import com.typesafe.config.Config;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.ActorMaterializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

    private final Injector injector;
    private final ActorSystem system;
    private final Config config;

    @Inject
    public Application(Injector injector, ActorSystem system, Config config) {
        this.injector = injector;
        this.system = system;
        this.config = config;
    }

    public void run() throws Exception {

        system.registerExtension(GuiceExtension.provider);

        // configure Guice
        final GuiceExtensionImpl guiceExtension = GuiceExtension.provider.get(system);
        guiceExtension.setInjector(injector);
        
        final ActorRef tailer = system.actorOf(            
            /*
                XXX use Guice instead of Akka Props please !!?
             */
                
            Props.create(
                RemoteLoggerTailer.class,
                injector.getInstance(ActorMaterializer.class)                    
            )            
        );
        
        config.getConfigList("logs").forEach(c -> {
            tailer.tell(
                new RemoteLoggerTailer.RemoteLoggerFile(c), ActorRef.noSender()
            );
        });

        LOGGER.info("-------------------------------------------------");
        LOGGER.info(" STARTED");
        LOGGER.info("-------------------------------------------------");

    }
}
