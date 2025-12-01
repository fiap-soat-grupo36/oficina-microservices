# syntax=docker/dockerfile:1.6
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia o projeto (multi-módulo) para compilar o serviço
COPY . .

# Cache de dependências para builds mais rápidos
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests -pl shared-library -am install
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests -pl auth-service -am package

FROM eclipse-temurin:21-jre

WORKDIR /app

# Usuário não-root para runtime
RUN groupadd -r app && useradd -r -g app app

COPY --from=builder /app/auth-service/target/auth-service-*.jar app.jar

USER app
EXPOSE 8082

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
