#!/bin/bash

## STARTUP SCRIPT FOR APP IN DOCKER IMAGE

## CONFIG FILES
app_cfg="/cfg/application.yml"

## OPTIONS FOR DEBUG
activate_debug_param="-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y"
java_option="-Xmx$xmx -Xms$xms -jar /datagate-gest.jar --spring.config.location=file:$app_cfg"

echo "STARTING DATAGATE"

## IF SPECIFIED, ENABLE DEBUG
if [ "$remoteXdebug" = "true" ]; then 
    java_option="$activate_debug_param $java_option"
    echo "DEBUG MODE ENABLED"
fi

## LAUNCH APP
java $java_option