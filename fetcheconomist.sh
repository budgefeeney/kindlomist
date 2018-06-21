#!/bin/bash

export JAVA_HOME=`/usr/libexec/java_home`
JAVA=$JAVA_HOME/bin/java

OPTIONS="-u bryan.feeney@gmail.com"
OPTIONS="$OPTIONS -f /Users/bryanfeeney/Desktop/eco.passwd"
OPTIONS="$OPTIONS -o /Users/bryanfeeney/Desktop"
OPTIONS="$OPTIONS -k /Users/bryanfeeney/Downloads/KindleGen_Mac_i386_v2_9/kindlegen"

JAVA -jar target/kindlomist-1.0-SNAPSHOT-jar-with-dependencies.jar $OPTIONS
