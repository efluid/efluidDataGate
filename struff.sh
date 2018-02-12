#!/bin/bash

## As a struff pipeline, the context is :
## - Current user has limited rights
## - Current user can use docker
## - Current working directory is root of checkout sources
## - To use ref to local directory on volumn mount for docker, you can use /project (substitued)
## - For instance root folder you can use /instance
## - For folder to use as (shared) cache you can use /cache. /cache/maven is for maven, /cache/npm for npm node_modules ...
## - Docker compose is used in instance
## - All resources copied in instance from struff script can use also "/project", "/instance", "/cache" ...

## MVN build
docker run -it --rm \
    -v /cache/maven:/root/.m2 \
    -v /project:/usr/src/mymaven \
    -w /usr/src/mymaven maven:3.3-jdk-8 /bin/bash \
    -c "mvn --batch-mode install; cp /usr/src/mymaven/src/docker/* /usr/src/mymaven/target"

## Docker build
docker build -t param-gest:latest ./target

## Prepare instance
cp /project/src/docker/docker-compose.yml /instance/docker-compose.yml

## Completed
echo "param-gest build completed"
