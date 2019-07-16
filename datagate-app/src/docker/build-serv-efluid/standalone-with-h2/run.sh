#!/bin/bash

## STARTUP SCRIPT FOR APP IN DOCKER IMAGE

## CONFIG FILES
app_cfg="/cfg/application.yml"
log_cfg="/cfg/logback.xml"

## OPTIONS FOR DEBUG
activate_debug_param="-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y"
java_option="-Xmx$xmx -Xms$xms -jar /app/datagate-app-exec.jar --spring.config.location=classpath:/application.yml,file:$app_cfg --logging.config=file:$log_cfg"

echo "STARTING DATAGATE"

## LAUNCH APP
$JAVA_HOME/bin/java $java_option