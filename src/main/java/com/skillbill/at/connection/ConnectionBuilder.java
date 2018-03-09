package com.skillbill.at.connection;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConnectionBuilder {
	
	public static String connection() {		
		final String path = new SimpleDateFormat("/MM/dd/").format(new Date());
		final String user = "alan.toro";		
		final String host = "volta.tomatowin.local";
		
		return "ssh " + user + "@" + host + " \"tail -f /usr/home/" + user + "/smartsend01.contactlab.prod" + path + "local1.log\"";
	}

}
