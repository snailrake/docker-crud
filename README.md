## Что делает проект

Проект представляет собой сервис для управления задачами.

Через API можно добавлять, получать, редактировать и удалять задачи. Spring Boot используется для логики приложения, PostgreSQL для хранения данных, запуск выполняется в отдельных Docker-контейнерах.

## Что где лежит

- `app` - Spring Boot приложение
- `app/src/main/java` - основной код приложения
- `app/src/main/resources/application.yml` - настройки Spring Boot
- `app/Dockerfile` - Dockerfile для приложения
- `db/init.sql` - создание таблицы и стартовые данные
- `db/Dockerfile` - Dockerfile для PostgreSQL
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

## Если нужно собрать jar вручную

```powershell
cd app
mvn clean package
```

Jar-файл появится в папке:

```text
app/target
```

## Проверка

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
-d "{\"title\":\"Finish docker lab\",\"description\":\"Project is ready\",\"priority\":\"HIGH\",\"status\":\"DONE\",\"dueDate\":\"2026-03-20\"}"
```

Удалить задачу:

```powershell
curl -X DELETE http://localhost:8080/api/tasks/1
```
