package com.skillbill.at.connection;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConnectionBuilder {
	
	public static String connection(String user, String host, String remoteFile) {		
		final String path = new SimpleDateFormat("/${MM}/${dd}/").format(new Date());
		
		return "ssh " + user + "@" + host + " \"tail -f " + path + "\"";
	}

}
