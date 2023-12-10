# syntax=docker/dockerfile:1

FROM openjdk:8
COPY ./Registry.jar /registry/registry.jar
WORKDIR /registry

EXPOSE 1200
ENTRYPOINT ["java", "-jar", "registry.jar"]