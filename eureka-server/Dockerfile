# Etapa 1: Build com Maven e Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia o pom.xml raiz e o módulo eureka-server
COPY pom.xml ./
COPY eureka-server ./eureka-server

# Build apenas este módulo
RUN mvn clean package -DskipTests -pl eureka-server -am

# Etapa 2: Runtime com Java 21
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copia o jar do módulo
COPY --from=builder /app/eureka-server/target/eureka-server-*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
