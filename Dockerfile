# Etapa 1: Construcción
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copiar archivos de configuración de Gradle
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Dar permisos de ejecución al wrapper y descargar dependencias
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Copiar el código fuente y construir el proyecto
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# En Gradle, el archivo generado suele estar en build/libs/
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-Dserver.port=10000", "-jar", "app.jar"]