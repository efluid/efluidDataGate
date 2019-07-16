FROM postgres:10.5

RUN apt-get update
RUN apt-get -y install dos2unix wget

RUN cd /tmp && \
    wget https://download.java.net/java/GA/jdk10/10/binaries/openjdk-10_linux-x64_bin.tar.gz && \
    tar xvf openjdk-10*_bin.tar.gz && \
    mkdir -p /usr/lib/jvm/jdk-10 && \
    mv jdk-10 /usr/lib/jvm && \
    rm openjdk-10_linux-x64_bin.tar.gz

ENV JAVA_HOME /usr/lib/jvm/jdk-10
ENV PATH $PATH:$JAVA_HOME/bin

EXPOSE 8080

ENV xms=256m
ENV xmx=256m

RUN mkdir /app

COPY datagate-app-exec.jar /app/datagate-app-exec.jar
COPY init-database.sql /docker-entrypoint-initdb.d
COPY run.sh /app/run.sh
COPY application.yml /cfg/application.yml
COPY logback.xml /cfg/logback.xml

RUN dos2unix /app/run.sh && chmod +x /app/run.sh && chown -R postgres:postgres /app

VOLUME /tmp
VOLUME /logs	
VOLUME /cfg

CMD ["/app/run.sh"]