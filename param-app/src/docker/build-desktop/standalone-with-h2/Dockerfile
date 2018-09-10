FROM openjdk:10-jre

RUN apt-get update
RUN apt-get -y install dos2unix

EXPOSE 8080
EXPOSE 8000

ENV remoteXdebug=false
ENV xms=256m
ENV xmx=256m

RUN mkdir /app

COPY param-app.jar /app/param-app.jar
COPY run.sh /app/run.sh
COPY application.yml /cfg/application.yml

RUN dos2unix /app/run.sh && chmod +x /app/run.sh

VOLUME /tmp
VOLUME /logs	
VOLUME /cfg

CMD ["/app/run.sh"]