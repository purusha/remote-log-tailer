package com.skillbill.at.guice;

import java.io.File;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AkkaModule implements Module {
    @Override
    public void configure(Binder binder) {

        final ActorSystem actorSystem = ActorSystem.create("remote-logger", ConfigFactory.load());

        binder.bind(ActorSystem.class).toInstance(actorSystem);

        binder.bind(ActorMaterializer.class).toInstance(ActorMaterializer.create(actorSystem));

        final File appConf = new File(System.getProperty("config.file", "remote-log.conf"));
        LOGGER.info("parsing appConf {} ", appConf.getAbsolutePath());

        final Config load = ConfigFactory.parseFile(appConf);

        binder.bind(Config.class).toInstance(load);

    }
}
