FROM openjdk:10-jre

RUN apt-get update
RUN apt-get install dos2unix

EXPOSE 8080
EXPOSE 8000

ENV remoteXdebug=false
ENV xms=256m
ENV xmx=256m

COPY efluid-datagate-gest.jar /efluid-datagate-gest.jar
COPY run.sh /run.sh

RUN dos2unix /run.sh
RUN chmod +x /run.sh

VOLUME /tmp
VOLUME /logs	
VOLUME /cfg

ENTRYPOINT /run.sh