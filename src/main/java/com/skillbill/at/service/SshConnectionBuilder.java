package com.skillbill.at.service;

import com.skillbill.at.akka.RemoteLoggerTailer.RemoteLoggerFile;

public class SshConnectionBuilder {

    public static String connection(RemoteLoggerFile rlf) {
        final String path = DatePlaceholder.resolve(rlf);

        return "ssh " + rlf.getSshUser() + "@" + rlf.getSshHost() + " \"tail -f " + path + "\"";
    }

}
