# Etapa 1: Build com Maven e Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia todo o projeto (necessário porque o parent POM referencia todos os módulos)
COPY . .

# Build apenas este módulo
RUN mvn clean package -DskipTests -pl budget-service -am

# Etapa 2: Runtime com Java 21
FROM eclipse-temurin:21-jdk-alpine

# Instalar wget para healthcheck
RUN apk add --no-cache wget

WORKDIR /app

# Copia o jar do módulo
COPY --from=builder /app/budget-service/target/budget-service-*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
