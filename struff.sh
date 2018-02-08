#!/bin/bash

## MVN build
docker run -it --rm \
    -v /opt/server/build:/root/.m2 \
    -v ~/GestionParamEfluid:/usr/src/mymaven \
    -w /usr/src/mymaven maven:3.3-jdk-8 /bin/bash \
    -c "mvn --batch-mode install -D skipTests; cp /usr/src/mymaven/src/docker/* /usr/src/mymaven/target"

## Docker build
docker build -t param-gest:latest ~/GestionParamEfluid/target

## Prepare instance
cp ~/GestionParamEfluid/src/docker/docker-compose.yml \
   ~/.instances/GestionParamEfluid/docker-compose.yml

## Completed
echo "param-gest build completed"
