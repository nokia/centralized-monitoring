FROM centos:centos7 AS build

ARG RPMS='wget java-11-openjdk-devel java-11-openjdk'

RUN mkdir -p /opt/cmonitoring/daemon\
             /root/.m2

COPY src /opt/cmonitoring/daemon/src
COPY pom.xml /opt/cmonitoring/daemon

RUN rpm --rebuilddb &&\
	yum install -y $RPMS &&\
    yum -y clean all

WORKDIR /usr/local

RUN wget https://apache.mediamirrors.org/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz &&\
    tar xvf apache-maven-3.6.3-bin.tar.gz

ENV M2_HOME /usr/local/apache-maven-3.6.3
ENV M2 $M2_HOME/bin
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk
ENV PATH $M2:$PATH

WORKDIR /opt/cmonitoring/daemon

RUN mvn -Dmaven.repo.local=/root/.m2/repository package -DskipTests

##

FROM centos:centos7

ARG RPMS='java-11-openjdk-devel'

RUN mkdir -p /opt/cmonitoring/daemon

WORKDIR /opt/cmonitoring/daemon

COPY --from=build /opt/cmonitoring/daemon/target/centralized-monitoring.jar .
COPY resources /opt/cmonitoring/daemon/resources

RUN rpm --rebuilddb &&\
    yum install -y $RPMS &&\
    yum -y clean all &&\
    groupadd -g 5555 cmonitoring &&\
    adduser -u 5555 -g 5555 -d /home/cmonitoring cmonitoring &&\
    chown -R 5555 /opt/cmonitoring

USER 5555

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "centralized-monitoring.jar"]
