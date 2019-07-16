## BASIC BUILD OF DOCKER INSTANCE IN STANDALONE MODE

## MVN build
docker run -it --rm -v ${HOME}\.m2:/root/.m2 -v ${PWD}:/usr/src/mymaven -w /usr/src/mymaven maven:3.5-jdk-10 /bin/bash -c "mvn --batch-mode install -DskipTests; cp /usr/src/mymaven/datagate-app/src/docker/build-desktop/standalone-with-h2/* /usr/src/mymaven/datagate-app/target"

## Docker build
docker build -t datagate:latest-h2 .\datagate-app\target

## Completed
Write-Host "datagate-gest standalone build completed"
