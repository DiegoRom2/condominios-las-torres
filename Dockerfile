# ---------- Etapa 1: compilar el proyecto ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Primero el wrapper y la configuración de Gradle, para aprovechar la caché de Docker
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
# Por si el archivo llega con saltos de línea de Windows o sin permiso de ejecución
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ---------- Etapa 2: imagen final, solo con lo necesario para correr ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Usa como máximo el 75% de la memoria del contenedor (el plan gratis de Render da 512 MB)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

# El puerto lo define la variable PORT (en Render se asigna sola; local usa 8081)
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
