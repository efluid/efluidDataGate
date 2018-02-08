FROM java:8-jre

RUN apt-get update
RUN apt-get install dos2unix

EXPOSE 8080
EXPOSE 8000

ENV instance=CHANGE-ME
ENV remoteXdebug=false
ENV xms=256m
ENV xmx=256m

COPY param-gest-*.jar /param-gest.jar
COPY run.sh /run.sh

RUN dos2unix /run.sh
RUN chmod +x /run.sh

VOLUME /tmp
VOLUME /logs	
VOLUME /cfg

ENTRYPOINT /run.sh