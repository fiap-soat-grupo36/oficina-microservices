# Etapa 1: Build com Maven e Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia o pom.xml raiz e o módulo específico
COPY ../pom.xml ./pom.xml
COPY ../shared-library ./shared-library
COPY . ./auth-service

# Build apenas este módulo
RUN mvn clean package -DskipTests -pl auth-service -am

# Etapa 2: Runtime com Java 21
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copia o jar do módulo
COPY --from=builder /app/auth-service/target/auth-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
