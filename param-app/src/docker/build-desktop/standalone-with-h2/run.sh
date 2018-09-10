#!/bin/bash

## STARTUP SCRIPT FOR APP IN DOCKER IMAGE

## CONFIG FILES
app_cfg="/cfg/application.yml"

## OPTIONS FOR DEBUG
activate_debug_param="-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y"
java_option="-Xmx$xmx -Xms$xms -jar /app/param-app.jar --spring.config.location=classpath:/application.yml,file:$app_cfg"

echo "STARTING EFLUID PARAM MANAGER"

## LAUNCH APP
$JAVA_HOME/bin/java $java_option