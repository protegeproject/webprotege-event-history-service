FROM openjdk:17
MAINTAINER protege.stanford.edu

EXPOSE 7761
ARG JAR_FILE
COPY target/${JAR_FILE} webprotege-events-history-service.jar
ENTRYPOINT ["java","--add-opens=java.management/sun.net=ALL-UNNAMED","-jar","/webprotege-events-history-service.jar"]