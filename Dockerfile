FROM registry.access.redhat.com/fuse7/fuse-java-openshift:1.7-11 as build
MAINTAINER  Salvatore Mongiardo <smongiar@redhat.com>
ENV MAVEN_VERSION 3.3.9
USER 0

RUN mkdir /home/jboss/maven && mkdir /home/jboss/camel-proxy \
  && curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
    | tar -xzC /home/jboss/maven --strip-components=1 \
  && ln -s /home/jboss/maven/bin/mvn /usr/bin/mvn


ENV MAVEN_HOME /home/jboss/maven

COPY src src
COPY configuration configuration
COPY pom.xml .
COPY keystore.jks keystore.jks

RUN ["mvn", "clean", "package", "-s", "configuration/settings.xml"]

FROM registry.access.redhat.com/fuse7/fuse-java-openshift:1.7-11

EXPOSE 8088
EXPOSE 8443

WORKDIR /home/jboss/camel-proxy

ARG JAR_FILE=/home/jboss/target/*.jar

COPY --from=build /home/jboss/keystore.jks /tls/keystore.jks
COPY --from=build ${JAR_FILE} fuse-proxy.jar
USER root

RUN ["chmod", "+x", "fuse-proxy.jar"]
ENTRYPOINT ["java","-jar","fuse-proxy.jar"]
CMD ["-start"]