#!/bin/bash

## As a stuff pipeline, the context is :
## - Current user has limited rights
## - Current user can use docker
## - Current working directory is root of checkout sources
## - To use ref to local directory on volumn mount for docker, you can use $1
## - Docker compose is used in instance
## - The instance location is "./.instance"

## MVN build
docker run -it --rm \
    -v /opt/server/build:/root/.m2 \
    -v $1:/usr/src/mymaven \
    -w /usr/src/mymaven maven:3.3-jdk-8 /bin/bash \
    -c "mvn --batch-mode install -D skipTests; cp /usr/src/mymaven/src/docker/* /usr/src/mymaven/target"

## Docker build
docker build -t param-gest:latest ./target

## Prepare instance
cp ./src/docker/docker-compose.yml ./.instance/docker-compose.yml

## Completed
echo "param-gest build completed"
