# Etapa 1: Construcción - Usamos JDK 21
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copiar archivos de configuración de Gradle
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Dar permisos de ejecución al wrapper
RUN chmod +x gradlew

# Descargar dependencias
# Nota: Eliminamos el comando 'dependencies' y usamos 'build -x test' más adelante 
# para evitar conflictos de Toolchains prematuros.
RUN ./gradlew --version

# Copiar el código fuente y construir el proyecto
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# Etapa 2: Ejecución - Usamos JRE 21 (más ligero)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiar el JAR generado
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 10000

# Forzamos el puerto 10000 que requiere Render
ENTRYPOINT ["java", "-Dserver.port=10000", "-jar", "app.jar"]