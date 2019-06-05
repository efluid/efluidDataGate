FROM openjdk:10-jre

EXPOSE 8080
EXPOSE 8000

ENV remoteXdebug=false
ENV xms=256m
ENV xmx=256m

RUN mkdir /app

COPY param-app-exec.jar /app/param-app-exec.jar
COPY run.sh /app/run.sh

RUN chmod +x /app/run.sh

VOLUME  ["/tmp", "/logs", "/cfg", "/data"]

CMD ["/app/run.sh"]