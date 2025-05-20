FROM ubuntu:22.04 AS build-common

RUN apt update --fix-missing \
    && apt install -y --no-install-recommends openjdk-21-jdk maven curl \
    && apt clean

COPY ./pom.xml /build/pom.xml
COPY ./api /build/api
COPY ./metabus /build/metabus
COPY ./coverage-report /build/coverage-report

WORKDIR /build
RUN --mount=type=cache,target=/root/.m2 mvn --projects metabus/cardano-metabus-txwatcher --also-make clean package

FROM eclipse-temurin:21-jre-jammy

COPY --from=build-common /build/metabus/cardano-metabus-txwatcher/target/*.jar /api/app.jar
ENTRYPOINT ["java","-jar","/api/app.jar"]