# Используем базовый образ с OpenJDK 17 (или ту версию, с которой вы компилируете)
FROM openjdk:17-jdk-slim

# Аргумент для указания имени JAR файла, передается из docker-compose.yml
ARG JAR_FILE=target/*.jar

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем собранный JAR файл из директории target (или указанный через JAR_FILE)
# в контейнер и переименовываем его в app.jar для единообразия
COPY ${JAR_FILE} app.jar

# Указываем порт, на котором приложение будет слушать внутри контейнера
# Этот порт будет мапиться на порт хоста в docker-compose.yml
EXPOSE 8080

# Команда для запуска приложения при старте контейнера
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Опционально: можно передавать Java опции, например, для указания профиля Spring
# ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "/app/app.jar"]
# Но мы передаем SPRING_PROFILES_ACTIVE через environment в docker-compose.yml, что предпочтительнее.
