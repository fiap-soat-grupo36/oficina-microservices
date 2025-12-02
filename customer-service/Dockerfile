# syntax=docker/dockerfile:1.6
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests -pl shared-library -am install
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests -pl customer-service -am package

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN groupadd -r app && useradd -r -g app app

COPY --from=builder /app/customer-service/target/customer-service-*.jar app.jar

USER app
EXPOSE 8081

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
