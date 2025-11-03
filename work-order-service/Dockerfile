# Etapa 1: Build com Maven e Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia o pom.xml raiz e os módulos necessários
COPY pom.xml ./
COPY shared-library ./shared-library
COPY work-order-service ./work-order-service

# Build apenas este módulo
RUN mvn clean package -DskipTests -pl work-order-service -am

# Etapa 2: Runtime com Java 21
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copia o jar do módulo
COPY --from=builder /app/work-order-service/target/work-order-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
