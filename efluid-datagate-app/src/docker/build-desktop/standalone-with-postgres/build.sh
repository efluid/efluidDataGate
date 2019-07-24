#!/bin/bash

## BASIC BUILD OF DOCKER INSTANCE IN STANDALONE MODE

## MVN build
docker run -it --rm \
    -v /opt/apache-maven-3.3.9/conf/settings.xml:/root/.m2/settings.xml \
    -v /data/EDT/mavenRepository:/data/EDT/mavenRepository \
    -v $(pwd):/usr/src/mymaven \
    -w /usr/src/mymaven maven:3.5-jdk-10 /bin/bash \
    -c "mvn --global-settings /root/.m2/settings.xml --batch-mode install -DskipTests; cp /usr/src/mymaven/efluid-datagate-app/src/docker/build-desktop/standalone-with-postgres/* /usr/src/mymaven/efluid-datagate-app/target"

## Docker build
docker build -t efluid-datagate:latest-pgsql ./efluid-datagate-app/target

## Completed
echo "efluid-datagate-gest standalone build completed"
