## Что делает проект

Проект представляет собой сервис для управления задачами.

Через API можно добавлять, получать, редактировать и удалять задачи. Spring Boot используется для логики приложения, PostgreSQL для хранения данных, запуск выполняется в отдельных Docker-контейнерах.

## Что где лежит

- `app` - Spring Boot приложение
- `app/src/main/java` - основной код приложения
- `app/src/test/java` - unit и web-тесты
- `app/src/main/resources/application.yml` - настройки Spring Boot
- `app/checkstyle.xml` - конфиг линтера
- `app/Dockerfile` - Dockerfile для приложения
- `db/init.sql` - создание таблицы и стартовые данные
- `db/Dockerfile` - Dockerfile для PostgreSQL
- `.github/workflows/ci-cd.yml` - pipeline для GitHub Actions
- `docker-compose.yml` - запуск контейнеров
- `app.env` - переменные окружения для приложения
- `db.env` - переменные окружения для базы данных

## Что реализовано

- создание задачи
- получение списка задач
- получение задачи по id
- изменение задачи
- удаление задачи
- фильтрация и сортировка задач
- инициализация базы через SQL-скрипт
- unit и web-тесты
- линтер
- coverage с порогом 50%
- CI/CD pipeline для GitHub Actions

## Запуск

```powershell
docker compose up -d --build
```

После запуска API будет доступно по адресу:

```text
http://localhost:8080/api/tasks
```

## Если нужно остановить

```powershell
docker compose down
```

## Если нужно полностью сбросить

```powershell
docker compose down -v
```

## Локальная проверка

Сборка:

```powershell
cd app
mvn clean package -DskipTests
```

Линтер:

```powershell
cd app
mvn checkstyle:check
```

Тесты и coverage:

```powershell
cd app
mvn clean verify
```

HTML-отчет coverage после проверки будет лежать в папке:

```text
app/target/site/jacoco
```

## Проверка API

Получить все задачи:

```powershell
curl http://localhost:8080/api/tasks
```

Получить задачу по id:

```powershell
curl http://localhost:8080/api/tasks/1
```

Создать задачу:

```powershell
curl -X POST http://localhost:8080/api/tasks ^
-H "Content-Type: application/json" ^
-d "{\"title\":\"Finish docker lab\",\"description\":\"Prepare project for defense\",\"priority\":\"HIGH\",\"status\":\"PLANNED\",\"dueDate\":\"2026-03-20\"}"
```

Изменить задачу:

```powershell
curl -X PUT http://localhost:8080/api/tasks/1 ^
-H "Content-Type: application/json" ^
-d "{\"title\":\"Project is ready\",\"description\":\"Update task status\",\"priority\":\"HIGH\",\"status\":\"DONE\",\"dueDate\":\"2026-03-20\"}"
```

Удалить задачу:

```powershell
curl -X DELETE http://localhost:8080/api/tasks/1
```

## CI/CD

Pipeline запускается при открытии и обновлении pull request, а также при push в `main`.

Для проверки можно открыть отдельный pull request и посмотреть выполнение job во вкладке GitHub Actions.

В pipeline есть отдельные job:

- `build` - проверяет, что приложение собирается
- `lint` - запускает checkstyle
- `test` - запускает тесты и проверку coverage
- `docker_build` - собирает Docker-образ приложения
- `docker_push` - отправляет образ в Docker Hub после успешных предыдущих шагов

В `docker_push` используются секреты GitHub Actions:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`

Образ публикуется с тегами:

- `sha-<short_commit_sha>`
- `latest`
