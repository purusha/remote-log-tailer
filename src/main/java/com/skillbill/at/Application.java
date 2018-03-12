package com.skillbill.at;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.skillbill.at.akka.RemoteLoggerTailer;
import com.skillbill.at.guice.GuiceExtension;
import com.skillbill.at.guice.GuiceExtensionImpl;
import com.typesafe.config.Config;
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

        config.getConfigList("logs").forEach(c -> {
            system.actorOf(Props.create(
                RemoteLoggerTailer.class,
                injector.getInstance(ActorMaterializer.class),
                new RemoteLoggerTailer.RemoteLoggerFile(c)
            ));
        });

        LOGGER.info("-------------------------------------------------");
        LOGGER.info(" STARTED");
        LOGGER.info("-------------------------------------------------");

    }
}
