package com.skillbill.at.akka;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.google.inject.Inject;
import com.skillbill.at.guice.GuiceAbstractActor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteLoggerTailer extends GuiceAbstractActor {
	
	private OutputStreamWriter writer;
	
	private Process proc;
	
	@Inject
	public RemoteLoggerTailer() {		
		try {
			
			this.proc = new ProcessBuilder("ssh -f user@host tail -f /var/log/boo.log")
				.redirectErrorStream(true)
				.start();

//			//write password over this
//			OutputStream outputStream = proc.getOutputStream();
//			
//			this.writer = new OutputStreamWriter(outputStream, "UTF-8");
//			
//			//read lines from this
//			InputStream inputStream = proc.getInputStream();
//			
//			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
//		    String line = null;
//		    while((line = in.readLine()) != null ){
//		        System.out.println(line);
//		    }
//		    in.close();			
			
			proc.waitFor();
			
			int exitValue = proc.exitValue();
			
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
			
            if (writer != null) {  
            	writer.close();                
            }
            
        	if (proc != null) {  
        		proc.destroy();    
    		}
            
        } catch (IOException ignore) {}		
	}
	
	private void cmd(String command)  {
        try {
            writer.write(command+'\n');
            writer.flush();
        } catch (IOException e) {}
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
