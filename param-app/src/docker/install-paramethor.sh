#!/bin/bash

INSTANCE=$1

ALL_ROOT=/opt/server/paramethor
CFG_ROOT="$ALL_ROOT/$INSTANCE/cfg"
LOG_ROOT="$ALL_ROOT/$INSTANCE/logs"
DATA_ROOT="$ALL_ROOT/$INSTANCE/data"

echo -e "\n##### PREPARING STUFF FOR PARAMETOR $INSTANCE #####\n"

mkdir -p $CFG_ROOT
mkdir -p $LOG_ROOT
mkdir -p $DATA_ROOT

cp ./param-app/src/docker/start-paramethor.sh /opt/server/paramethor/start-paramethor.sh
cp ./param-app/src/docker/build-serv-efluid/logback.xml $CFG_ROOT/logback.xml
cp ./param-app/src/main/resources/config/application.yml $CFG_ROOT/application.yml

echo "Copied files : "

echo " - './param-app/src/docker/start-paramethor.sh' ==> copied to '/opt/server/paramethor/start-paramethor.sh' "
echo " - './param-app/src/docker/build-serv-efluid/logback.xml' ==> copied to '$CFG_ROOT/logback.xml' "
echo " - './param-app/src/main/resources/config/application.yml' ==> copied to '$CFG_ROOT/application.yml' "

echo -e "\nPreparation done. Don't forget to check content of '$CFG_ROOT/application.yml' before running '$ALL_ROOT/start-paramethor.sh $INSTANCE 8081'\n"