#!/bin/bash

## STARTUP SCRIPT FOR APP IN DOCKER IMAGE

## CONFIG FILES
app_cfg="/cfg/application.yml"

## OPTIONS FOR DEBUG
activate_debug_param="-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y"
activate_log_param="--logging.file=$customLogging"
java_option="-Xmx$xmx -Xms$xms -jar /app/efluid-datagate-app-exec.jar --spring.config.location=classpath:/application.yml,file:$app_cfg"

echo "STARTING EFLUID-DATAGATE"

## IF SPECIFIED, ENABLE CUSTOM LOGGING
if [ "$customLogging" != "disabled" ]; then
    java_option="$java_option $activate_log_param"
    echo "CUSTOM LOGGING ENABLED"
fi

## IF SPECIFIED, ENABLE DEBUG
if [ "$remoteXdebug" = "true" ]; then
    java_option="$activate_debug_param $java_option"
    echo "DEBUG MODE ENABLED"
fi

## LAUNCH APP
$JAVA_HOME/bin/java $java_option