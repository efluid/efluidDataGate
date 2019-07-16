#!/bin/bash

INSTANCE=$1

ALL_ROOT=/opt/server/datagate
CFG_ROOT="$ALL_ROOT/$INSTANCE/cfg"
LOG_ROOT="$ALL_ROOT/$INSTANCE/logs"
DATA_ROOT="$ALL_ROOT/$INSTANCE/data"

echo -e "\n##### PREPARING STUFF FOR PARAMETOR $INSTANCE #####\n"

mkdir -p $CFG_ROOT
mkdir -p $LOG_ROOT
mkdir -p $DATA_ROOT

cp ./datagate-app/src/docker/start-datagate.sh /opt/server/datagate/start-datagate.sh
cp ./datagate-app/src/docker/build-serv-efluid/logback.xml $CFG_ROOT/logback.xml
cp ./datagate-app/src/main/resources/config/application.yml $CFG_ROOT/application.yml

echo "Copied files : "

echo " - './datagate-app/src/docker/start-datagate.sh' ==> copied to '/opt/server/datagate/start-datagate.sh' "
echo " - './datagate-app/src/docker/build-serv-efluid/logback.xml' ==> copied to '$CFG_ROOT/logback.xml' "
echo " - './datagate-app/src/main/resources/config/application.yml' ==> copied to '$CFG_ROOT/application.yml' "

echo -e "\nPreparation done. Don't forget to check content of '$CFG_ROOT/application.yml' before running '$ALL_ROOT/start-datagate.sh $INSTANCE 8081'\n"