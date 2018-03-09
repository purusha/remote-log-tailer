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
    	
	}
}
