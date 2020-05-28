FROM openjdk:11-jre


ENV http_proxy http://usine-logicielle:ul-logicielle@lpsrvpxy:8080
ENV no_proxy localhost,127.0.0.1,wikefluid,eartifact,usinelogicielle,ldsanbld2


RUN apt-get update
RUN apt-get -y install dos2unix

EXPOSE 8080
EXPOSE 8000

ENV remoteXdebug=false
ENV xms=12g
ENV xmx=12g

RUN mkdir /app

COPY efluid-datagate-app-exec.jar /app/efluid-datagate-app-exec.jar
COPY run.sh /app/run.sh
COPY application.yml /cfg/application.yml
COPY logback.xml /cfg/logback.xml

RUN dos2unix /app/run.sh && chmod +x /app/run.sh

VOLUME  ["/tmp", "/logs", "/cfg", "/data"]

CMD ["/app/run.sh"]