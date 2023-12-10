# syntax=docker/dockerfile:1

FROM openjdk:8
COPY ./Client.jar /client/client.jar
WORKDIR /client

EXPOSE 1230
ENTRYPOINT ["java", "-jar", "client.jar"]