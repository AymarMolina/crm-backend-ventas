# Etapa 1: Construcción (Maven)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# Copiar solo el pom.xml primero para aprovechar la caché de capas de Docker
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el código fuente y construir el JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copiar el JAR generado desde la etapa de construcción
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto que Render usa por defecto
EXPOSE 10000

# Ejecutar la aplicación
# Usamos -Dserver.port=10000 para forzar el puerto que Render espera
ENTRYPOINT ["java", "-Dserver.port=10000", "-jar", "app.jar"]