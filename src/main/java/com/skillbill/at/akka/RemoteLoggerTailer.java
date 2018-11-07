package com.skillbill.at.akka;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import com.google.inject.Inject;
import com.skillbill.at.guice.GuiceAbstractActor;
import com.skillbill.at.service.LocalFileBuilder;
import com.skillbill.at.service.SshConnectionBuilder;
import com.typesafe.config.Config;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.StreamConverters;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteLoggerTailer extends GuiceAbstractActor {

    private final ActorMaterializer materializer;
    private final Map<RemoteLoggerFile, Process> state;
    
    @Inject
    public RemoteLoggerTailer(ActorMaterializer materializer) {
        this.materializer = materializer;
        this.state = new HashMap<RemoteLoggerTailer.RemoteLoggerFile, Process>();
    }
    
    @Override
    public void postStop() throws Exception {
        super.postStop();
        LOGGER.info("end {} ", getSelf().path());
        
        state.values().forEach(proc -> {
          try {
              proc.destroy();
          } catch (Exception ignore) { }            
        });
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        LOGGER.info("start {} with parent {}", getSelf().path(), getContext().parent().path());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(RemoteLoggerFile.class, rlf -> {
                
                if (! state.containsKey(rlf)) {
                    handler(rlf);    
                }
                
            }).matchAny(o -> {
                LOGGER.warn("not handled message", o);
                unhandled(o);
            }).build();
    }

    private void handler(RemoteLoggerFile rlf) {
        try{

            final Process proc = new ProcessBuilder("bash", "-c", SshConnectionBuilder.connection(rlf))
                .redirectErrorStream(true)
                .start();                        
            
            if (proc.isAlive()) {
                runBlueprint(rlf, proc);
            } else {
                LOGGER.error("process for {} is not alive", rlf);
            }
            
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private void runBlueprint(RemoteLoggerFile rlf, Process proc) {
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
            LOGGER.info("terminated batch with {} on {}", action.getError().getMessage(), rlf);
        });

        stage.exceptionally(e -> {
            LOGGER.error("", e);

            return IOResult.createFailed(1, e);
        });
        
        state.put(rlf, proc);
    }

    @Getter
    @EqualsAndHashCode
    @ToString
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
