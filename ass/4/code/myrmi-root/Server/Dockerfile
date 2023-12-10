# syntax=docker/dockerfile:1

FROM openjdk:8

RUN sed -i s@/deb.debian.org/@/mirrors.aliyun.com/@g /etc/apt/sources.list
RUN apt-get update && \
    apt-get install -y --fix-missing wget software-properties-common build-essential sgml-base rsync xml-core openssh-client && \
    apt-get clean

RUN apt-get install -y --fix-missing cmake git gfortran openmpi-common openmpi-bin libopenmpi-dev && \
    apt-get clean

COPY ./Server.jar /server/server.jar
COPY ./mat_mul.c /server/mat_mul.c

WORKDIR /server
RUN mpicc mat_mul.c -o mat_mul

EXPOSE 1220 43801
ENTRYPOINT ["java", "-jar", "server.jar"]