package com.skillbill.at.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.skillbill.at.akka.RemoteLoggerTailer.RemoteLoggerFile;

public class LocalFileBuilder {
	
	public static String getPath(RemoteLoggerFile rlf) {
		final String remoteFile = DatePlaceholder.resolve(rlf);
		final String[] split = StringUtils.split(remoteFile, "/");
		
		String dirName = "";
		for (int i = 0; i < split.length; i++) {
			if (i + 1 != split.length) {
				dirName += split[i];				
				
				if (i + 2 != split.length) {
					dirName += "-";
				}				
			}			
		}
		
		final String dir = "/tmp/_/" + rlf.getSshHost() + "/" + dirName;
		new File(dir).mkdirs();
		
		final String file = dir + "/" + split[split.length - 1];
		
		try {
			new File(file).createNewFile();
		} catch (IOException e) { }
		
		return file;
	}
	
}
