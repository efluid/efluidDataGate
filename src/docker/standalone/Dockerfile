FROM postgres:10.2

RUN apt-get update
RUN apt-get -y install dos2unix aptitude

RUN echo "deb http://archive.ubuntu.com/ubuntu trusty main universe" > /etc/apt/sources.list
RUN apt-get -y update

RUN DEBIAN_FRONTEND=noninteractive aptitude install -y -f python-software-properties software-properties-common

ENV JAVA_VER 8
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

RUN echo 'deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main' >> /etc/apt/sources.list && \
    echo 'deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main' >> /etc/apt/sources.list && \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys C2518248EEA14886 && \
    apt-get update && \
    echo oracle-java${JAVA_VER}-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-get install -y --force-yes --no-install-recommends oracle-java${JAVA_VER}-installer oracle-java${JAVA_VER}-set-default && \
    apt-get clean && \
    rm -rf /var/cache/oracle-jdk${JAVA_VER}-installer
    
RUN update-java-alternatives -s java-8-oracle
RUN echo "export JAVA_HOME=/usr/lib/jvm/java-8-oracle" >> ~/.bashrc
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

EXPOSE 8080

ENV xms=256m
ENV xmx=256m

RUN mkdir /app

COPY param-gest.jar /app/param-gest.jar
COPY init-database.sql /docker-entrypoint-initdb.d
COPY run.sh /app/run.sh
COPY application.yml /cfg/application.yml

RUN dos2unix /app/run.sh && chmod +x /app/run.sh && chown -R postgres:postgres /app

VOLUME /tmp
VOLUME /logs	
VOLUME /cfg

CMD ["/app/run.sh"]