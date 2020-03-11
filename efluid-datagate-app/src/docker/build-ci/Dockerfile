FROM openjdk:11-jre

RUN apt-get update
RUN apt-get -y install dos2unix

EXPOSE 8080
EXPOSE 8087
EXPOSE 8000

ENV remoteXdebug=false
ENV xms=256m
ENV xmx=512m

RUN mkdir /app

COPY efluid-datagate-app/target/efluid-datagate-app-exec.jar /app/efluid-datagate-app-exec.jar
COPY efluid-datagate-app/target/run.sh /app/run.sh
COPY efluid-datagate-app/target/application.yml /cfg/application.yml
COPY efluid-datagate-app/target/logback.xml /cfg/logback.xml

RUN dos2unix /app/run.sh && chmod +x /app/run.sh

VOLUME  ["/tmp", "/logs", "/cfg", "/data"]

CMD ["/app/run.sh"]