#!/bin/bash

INSTANCE=$1
PORT=$2

CONTAINER_REV="datagate:latest-h2"
CONTAINER_NAME="datagate-$INSTANCE"

CFG_ROOT="/opt/server/datagate/$INSTANCE/cfg"
LOG_ROOT="/opt/server/datagate/$INSTANCE/logs"

docker rm -f $CONTAINER_NAME

echo -e "\n##### STARTING DATAGATE $INSTANCE #####\n"

mkdir -p $CFG_ROOT
mkdir -p $LOG_ROOT

docker run -d --name $CONTAINER_NAME \
     -v $LOG_ROOT:/logs \
     -v $CFG_ROOT:/cfg \
     -p $PORT:8080 \
     $CONTAINER_REV
     
APP_HOST="$(sudo docker inspect --format '{{ .NetworkSettings.IPAddress }}' $CONTAINER_NAME):8080"

UPD_RESULT=$(curl -sSf http://$APP_HOST/rest/v1/app/state)

printf "Wait for startup ."

## Call every X seconds for status. Wait for completion (status is no more "running")
while [ "$UPD_RESULT" != "RUNNING" ]
do
    sleep 1
   	printf "."
	UPD_RESULT=$(curl -sSf http://$APP_HOST/rest/v1/app/state)
done

echo -e "\n\n##### DATAGATE $INSTANCE IS STARTED AND AVAILABLE ON PORT $PORT #####\n"