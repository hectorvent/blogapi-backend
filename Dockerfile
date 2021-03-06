FROM maven:3.6.3-openjdk-11-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package


FROM openjdk:11-jre-buster
LABEL maintainer="Hector Ventura <hectorvent@gmail.com>"

WORKDIR /app

COPY docker/entrypoint.sh .
COPY  --from=builder /app/target/blogapi*-fat.jar app.jar

ENV DATABASE_USER=root
ENV DATABASE_PASSWORD=blogapi
ENV DATABASE_URL=jdbc:mysql://dbserver:3306/blogapi?characterEncoding=UTF-8&useSSL=false

EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh"]
