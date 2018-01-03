#!/bin/bash
# Symlink JAR
JAR="gairos-web-latest.jar"

# Pick the correct config
CONFIG_PATH="${HOME}/${UDEPLOY_SERVICE_NAME}/config"


# Java opts, please change as necessary.
JAVA_OPTS="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintAdaptiveSizePolicy -XX:+UseG1GC -Xms12g -Xmx12g -XX:MaxGCPauseMillis=120 -XX:+ParallelRefProcEnabled -XX:ParallelGCThreads=18 -XX:-ResizePLAB -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=1"

# Run it!
RUN_COMMAND="java $JAVA_OPTS -jar $JAR server $RT_GAIROS_CONFIG"
$RUN_COMMAND
