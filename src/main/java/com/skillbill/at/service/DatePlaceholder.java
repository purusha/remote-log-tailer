package com.skillbill.at.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import com.skillbill.at.akka.RemoteLoggerTailer.RemoteLoggerFile;

public class DatePlaceholder {

    public static String resolve(RemoteLoggerFile rlf) {
        final String remoteFile = rlf.getRemoteFile();
        final String dateFormat =
                new SimpleDateFormat(StringUtils.substringBetween(remoteFile, "${", "}"))
                        .format(new Date());

        final int start = StringUtils.indexOf(remoteFile, "${");
        final int end = StringUtils.indexOf(remoteFile, "}") + 1;

        return StringUtils.substring(remoteFile, 0, start) + dateFormat
                + StringUtils.substring(remoteFile, end, remoteFile.length());

    }

}
