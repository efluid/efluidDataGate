#!/bin/bash

## BASIC BUILD OF DOCKER INSTANCE IN STANDALONE MODE

## MVN build
docker run -it --rm \
    -v ~/.m2/repository:/root/.m2 \
    -v $(pwd):/usr/src/mymaven \
    -w /usr/src/mymaven maven:3.5-jdk-10 /bin/bash \
    -c "mvn --batch-mode install -DskipTests; cp /usr/src/mymaven/param-app/src/docker/standalone/build-desktop/standalone-with-h2/* /usr/src/mymaven/param-app/target"

## Docker build
docker build -t paramethor:latest-h2 ./param-app/target

## Completed
echo "param-gest standalone build completed"
