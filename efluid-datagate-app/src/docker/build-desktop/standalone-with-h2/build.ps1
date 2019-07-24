## BASIC BUILD OF DOCKER INSTANCE IN STANDALONE MODE

## MVN build
docker run -it --rm -v ${HOME}\.m2:/root/.m2 -v ${PWD}:/usr/src/mymaven -w /usr/src/mymaven maven:3.5-jdk-10 /bin/bash -c "mvn --batch-mode install -DskipTests; cp /usr/src/mymaven/efluid-datagate-app/src/docker/build-desktop/standalone-with-h2/* /usr/src/mymaven/efluid-datagate-app/target"

## Docker build
docker build -t efluid-datagate:latest-h2 .\efluid-datagate-app\target

## Completed
Write-Host "efluid-datagate-gest standalone build completed"
