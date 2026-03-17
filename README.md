0. собираем проект в jar:
Справа в idea нажать maven - package  
После файл jar должен появиться в папке проекта, в директории `app/target`.

Можно и через терминал:

```bash
cd app
mvn clean package
```

1. создаем Dockerfile для spring boot приложения:
Dockerfile находится в папке `app`

```dockerfile
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml ./
COPY src ./src

RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /build/target/docker-crud-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

`RUN` - собираю проект внутри докера

`FROM maven:3.9.9-eclipse-temurin-21 AS build` - образ для сборки spring boot приложения через maven

`WORKDIR` - назначаем рабочую директорию, в которой будут выполняться дальнейшие команды

`COPY pom.xml ./` - копируем файл зависимостей Maven

`COPY src ./src` - копируем исходный код приложения

`RUN mvn clean package -Dmaven.test.skip=true` - собираем jar внутри контейнера без тестов

`FROM eclipse-temurin:21-jre` - отдельный образ только для запуска приложения

`COPY --from=build /build/target/docker-crud-0.0.1-SNAPSHOT.jar app.jar` - копируем готовый jar из первого stage

`EXPOSE 8080` - описание порта, который будет слушать приложение внутри контейнера

`ENTRYPOINT ["java", "-jar", "app.jar"]` - jar-файл запускается внутри контейнера

2. создаем Dockerfile для базы данных:
Dockerfile находится в папке `db`

```dockerfile
FROM postgres:16-alpine

COPY init.sql /docker-entrypoint-initdb.d/01-init.sql
```

`FROM postgres:16-alpine` - используем готовый образ PostgreSQL

`COPY init.sql /docker-entrypoint-initdb.d/01-init.sql` - при первом запуске контейнера автоматически выполняется sql-скрипт, который создает таблицу `tasks` и добавляет стартовые записи

3. создаем в корне docker-compose.yml
ВАЖНО!!

В `db.env` должно быть:

```env
POSTGRES_DB=task_tracker
POSTGRES_USER=task_manager
POSTGRES_PASSWORD=task_manager_password
```

В `app.env` должно быть:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/task_tracker
SPRING_DATASOURCE_USERNAME=task_manager
SPRING_DATASOURCE_PASSWORD=task_manager_password
SERVER_PORT=8080
```

должна быть - `POSTGRES_DB=task_tracker` в db, иначе spring app не найдет базу

`SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/task_tracker` - вместо localhost указываем ИМЯ КОНТЕЙНЕРА БАЗЫ = `db`

либо можно строку подключения сразу прописать в `application.yml`, но здесь она берется из env-файла

`docker-compose.yml`:

```yaml
services:
  db:
    build:
      context: ./db
    container_name: docker-crud-db
    env_file:
      - ./db.env
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U task_manager -d task_tracker"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s
    networks:
      - backend-network

  app:
    build:
      context: ./app
    container_name: docker-crud-app
    env_file:
      - ./app.env
    depends_on:
      db:
        condition: service_healthy
    ports:
      - "8080:8080"
    networks:
      - backend-network

volumes:
  postgres-data:

networks:
  backend-network:
    driver: bridge
```

4. Запускаем

```bash
docker compose up --build
```

создаются все контейнеры из файла

после запуска API будет доступно по адресу:

```text
http://localhost:8080/api/tasks
```

чтобы все удалить и запустить заново:

```bash
docker compose down
```

удаляет контейнеры

```bash
docker compose down -v
```

удаляет контейнеры и volume базы данных

```bash
docker compose up --build
```

заново создает контейнеры

если нужно удалить образ вручную:

```bash
docker images
docker rmi titleImage
```

если ошибка spring boot тестов, можно собрать без тестов:

```bash
cd app
mvn clean install -Dmaven.test.skip=true
```

5. ДОП инфа:
создать сеть:

```bash
docker network create -d bridge my-bridge-network
```

посмотреть все сети:

```bash
docker network ls
```

удалив `driver: bridge` из файла `docker-compose.yml`: Docker Compose все равно сам создаст сеть по умолчанию и подключит к ней контейнеры

это означает, что внутренние ip контейнеров обычно знать не нужно  
внутри Docker можно обращаться к контейнеру базы по имени сервиса `db`

например:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/task_tracker
```

https://docs.docker.com/engine/reference/commandline/network_create/  
https://docs.docker.com/compose/networking/

6. создаю env-файлы
файлы лежат там же, где `docker-compose.yml` - один уровень

создаю для постгреса и приложения: `db.env` и `app.env`  
после их добавляю в docker-compose:

```yaml
env_file:
  - ./db.env
```

и

```yaml
env_file:
  - ./app.env
```

https://docs.docker.com/compose/environment-variables/set-environment-variables/

7. про init.sql
в проекте есть файл `db/init.sql`

он нужен для начальной инициализации базы данных  
при первом запуске контейнера PostgreSQL автоматически выполняет этот SQL-файл

в `init.sql`:
- создается таблица `tasks`
- добавляются стартовые записи

8. про EXPOSE и ports:
`EXPOSE` - описание сетевого порта, который будет прослушиваться в запущенном контейнере

в Dockerfile приложения указано:

```dockerfile
EXPOSE 8080
```

наружу API открывается через `docker-compose.yml`:

```yaml
ports:
  - "8080:8080"
```

тут:
1. первый `8080` - порт хост машины
2. второй `8080` - порт внутри контейнера

у базы данных `ports` нет  
это значит, что база данных доступна только внутри docker-сети

9. в docker-compose:
`volumes` - описываем именованные тома

`networks` - описываем сеть bridge

`services`:
`build` - путь к Dockerfile  
`container_name` - имя контейнера  
`env_file` - переменные окружения для контейнера  
`volumes` - именованные тома  
`networks` - принадлежность к сети  
`depends_on` - приложение зависит от базы данных  
`healthcheck` - проверка, что postgres готов к подключениям  
`ports` - указываем порт, который хотим открыть наружу

10. запросы для проверки:

получить все задачи:

```bash
curl http://localhost:8080/api/tasks
```

получить задачу по id:

```bash
curl http://localhost:8080/api/tasks/1
```

создать задачу:

```bash
curl -X POST http://localhost:8080/api/tasks ^
-H "Content-Type: application/json" ^
-d "{\"title\":\"Finish docker lab\",\"description\":\"Prepare project for defense\",\"priority\":\"HIGH\",\"status\":\"PLANNED\",\"dueDate\":\"2026-03-20\"}"
```

изменить задачу:

```bash
curl -X PUT http://localhost:8080/api/tasks/1 ^
-H "Content-Type: application/json" ^
-d "{\"title\":\"Finish docker lab\",\"description\":\"Project is ready\",\"priority\":\"HIGH\",\"status\":\"DONE\",\"dueDate\":\"2026-03-20\"}"
```

удалить задачу:

```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```
