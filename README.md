# remote-log-tailer

follow remote log file over ssh ... write it locally under path /tmp/_/{ssh-host}/{remote-file}

example of remote-log.conf

```json
logs = [
    {
        ssh-user = "user"
        ssh-host = "myhost"
        remote-file = "/usr/home/user/service/${MM}/${dd}/local1.log"
    }
]
```

example of placeholder resolved by [SimpleDateFormat](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html) are enclosed by '${' and '}'

naturally this evaluation are optionally ... 
