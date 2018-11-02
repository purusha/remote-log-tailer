package com.skillbill.at.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import com.skillbill.at.akka.RemoteLoggerTailer.RemoteLoggerFile;

public class DatePlaceholder {
    public static String resolve(RemoteLoggerFile rlf) {
        
        String remoteFile = rlf.getRemoteFile();
        
        final String[] patterns = StringUtils.substringsBetween(remoteFile, "${", "}");
        final Date now = new Date();
        
        if (Objects.nonNull(patterns)) {
            for(String pattern : patterns) {
                
                final int start = StringUtils.indexOf(remoteFile, "${");
                final int end = StringUtils.indexOf(remoteFile, "}") + 1;
                final String dateFormat = new SimpleDateFormat(pattern).format(now); 
                
                remoteFile = StringUtils.substring(remoteFile, 0, start) + dateFormat + StringUtils.substring(remoteFile, end, remoteFile.length());
                
            }            
        }
        
        return remoteFile;
        
    }    
}
