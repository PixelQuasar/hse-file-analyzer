# Используем базовый образ с OpenJDK 17
FROM openjdk:17-jdk-slim

# Аргумент для указания имени JAR файла
ARG JAR_FILE=target/*.jar

# Рабочая директория
WORKDIR /app

# Копируем JAR
COPY ${JAR_FILE} app.jar

# Порт приложения внутри контейнера
EXPOSE 8081

# Запуск приложения
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
