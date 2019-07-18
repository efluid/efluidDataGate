#!/bin/bash

INSTANCE=$1

ALL_ROOT=/opt/server/efluidDataGate
CFG_ROOT="$ALL_ROOT/$INSTANCE/cfg"
LOG_ROOT="$ALL_ROOT/$INSTANCE/logs"
DATA_ROOT="$ALL_ROOT/$INSTANCE/data"

echo -e "\n##### PREPARING STUFF FOR EFLUID-DATAGATE $INSTANCE #####\n"

mkdir -p $CFG_ROOT
mkdir -p $LOG_ROOT
mkdir -p $DATA_ROOT

cp ./efluid-datagate-app/src/docker/start-efluidDataGate.sh /opt/server/efluidDataGate/start-efluidDataGate.sh
cp ./efluid-datagate-app/src/docker/build-serv-efluid/logback.xml $CFG_ROOT/logback.xml
cp ./efluid-datagate-app/src/main/resources/config/application.yml $CFG_ROOT/application.yml

echo "Copied files : "

echo " - './efluid-datagate-app/src/docker/start-efluidDataGate.sh' ==> copied to '/opt/server/efluidDataGate/start-efluidDataGate.sh' "
echo " - './efluid-datagate-app/src/docker/build-serv-efluid/logback.xml' ==> copied to '$CFG_ROOT/logback.xml' "
echo " - './efluid-datagate-app/src/main/resources/config/application.yml' ==> copied to '$CFG_ROOT/application.yml' "

echo -e "\nPreparation done. Don't forget to check content of '$CFG_ROOT/application.yml' before running '$ALL_ROOT/start-efluidDataGate.sh $INSTANCE 8081'\n"