# TinderBot

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue?logo=apachemaven)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-red?logo=flyway)
![Telegram](https://img.shields.io/badge/Telegram%20Bots-6.9.7-26A5E4?logo=telegram)
![OpenAI](https://img.shields.io/badge/OpenAI-Compatible-412991?logo=openai)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-green?logo=springsecurity)
![Resilience4j](https://img.shields.io/badge/Resilience4j-2.4.0-blueviolet)
![MapStruct](https://img.shields.io/badge/MapStruct-1.6.3-red)
![Testcontainers](https://img.shields.io/badge/Testcontainers-2.0.5-blue)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3-green?logo=swagger)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)

![CI](https://github.com/charset-8utf/tinder-bot/actions/workflows/ci.yml/badge.svg?branch=main)

Telegram-бот для знакомств с **REST API**, **OpenAPI** и **Docker Compose**.  
Монолит на **Spring Boot 4** (Java 21, Maven): профиль, opener, переписка, GPT, режим DATE.

---

## Возможности

| Канал        | Что умеет                                                                 |
|--------------|---------------------------------------------------------------------------|
| **Telegram** | Long polling, режимы через inline-кнопки, опросники PROFILE/OPENER        |
| **REST API** | Сессии, опросники, GPT, переписка, DATE — те же сервисы, что и бот        |
| **OpenAPI**  | Swagger UI (`/swagger-ui.html`) + JSON (`/v3/api-docs`), `tinderbot.http` |
| **Auth**     | JWT (доступ только к своей сессии: `chatId = telegram_user_id`)           |

---

## Архитектура

```text
Клиент (Telegram) ── long polling ──► TinderBotController
                                              │
REST-клиент ── /api/v1/* ──► REST controllers ┤
                                              ▼
                                    ModeHandler / CallbackHandler
                                              │
                         ┌────────────────────┼────────────────────┐
                         ▼                    ▼                    ▼
               QuestionnaireService    DialogService (GPT)   UserSessionService
                         │                    │              (SessionPersistence)
                         │                    │                    │
                         │                    │         api/session/* (slice stores)
                         └────────────────────┼────────────────────┘
                                              ▼
                                    PostgreSQL (Flyway)
                                              │
                                    OpenAI-compatible API
```

Сессии: один `UserSessionService` + in-memory кэш, но зависимости в коде разбиты на **slice-интерфейсы** (`SessionStore`, `TelegramUiSessionStore`, `GptHistoryStore` и др.) — каждый компонент видит только нужный срез API.

### Режимы диалога (`DialogMode`)

| Режим     | Назначение                                               |
|-----------|----------------------------------------------------------|
| `MAIN`    | Главное меню                                             |
| `PROFILE` | Опросник → генерация текста профиля                      |
| `OPENER`  | Опросник → первое сообщение для знакомства               |
| `MESSAGE` | Переписка от имени пользователя (история + next message) |
| `DATE`    | Диалог со «звездой» (лимит сообщений, `starKey`)         |
| `GPT`     | Свободные вопросы к ChatGPT                              |

Telegram и REST используют общий слой `service/` и хранят состояние сессии в PostgreSQL.

### Форматирование Telegram-сообщений

| Тип текста | Как отправляется | Примеры |
|------------|------------------|---------|
| Шаблоны из `resources/messages/` | HTML (`parse_mode: HTML`) | приветствие, intro PROFILE/OPENER/GPT |
| Статусы, вопросы опросника, ошибки | plain text | «ChatGPT думает...», «Сколько вам лет?» |
| Ответы GPT | plain text (`updateTextMessage`) | сгенерированный профиль, opener, переписка |

Шаблоны используют теги `<b>`, `</b>`. Ответы GPT и пользовательский ввод **не** прогоняются через разметку — так меньше риск, что Telegram отклонит сообщение из‑за спецсимволов.

---

## Security

| Механизм       | Где                                         | Поведение                                            |
|----------------|---------------------------------------------|------------------------------------------------------|
| **Bearer JWT** | REST `/api/v1/**`                           | Доступ только к сессии с `chatId = telegram_user_id` |
| **Login**      | `POST /api/v1/auth/login`                   | Публичный endpoint, выдаёт JWT                       |
| **Disabled**   | профиль `dev`, `API_SECURITY_ENABLED=false` | Все REST-запросы без auth                            |

Пользователи REST API — таблицы `users` и `user_credentials` (BCrypt). При **первом** сообщении боту автоматически создаётся пользователь с `telegram_user_id = chatId`; логин и пароль пишутся **один раз в лог приложения** (не в Telegram-чат).

| Пользователь | Как получить | Telegram `chatId` |
|--------------|--------------|-------------------|
| `demo`       | seed Flyway (`password`) | `1` (только для примеров) |
| ваш аккаунт  | напишите боту `/start`, затем `docker compose logs app \| grep "Создан REST API пользователь"` (поле `oneTimeValue` — пароль для login) | ваш реальный Telegram ID |

---

## Точки входа

| Режим                      | URL                     | Команда                                           |
|----------------------------|-------------------------|---------------------------------------------------|
| **Docker** (рекомендуется) | `http://localhost:8080` | `docker compose up --build`                       |
| Swagger UI                 | `/swagger-ui.html`      | после старта приложения                           |
| OpenAPI JSON               | `/v3/api-docs`          | стандартный путь SpringDoc                        |
| Health                     | `/actuator/health`      | readiness/liveness                                |
| Info                       | `/actuator/info`        | версия и описание                                 |
| Postgres снаружи           | `localhost:5433`        | порт по умолчанию (не конфликтует с локальным PG) |

Подключение к БД (IDEA, DBeaver): `localhost:5433`, БД `tinderbot`, user/password `tinderbot`.  
Если меняли `.env` при первом запуске — креды зафиксированы в Docker volume; смена требует `docker compose down -v`.

---

## Быстрый старт

### Требования

- **Docker** + Docker Compose
- **Java 21** и **Maven** — для локальной разработки и тестов

### 1. Конфигурация

```bash
cp .env.example .env   
```

По умолчанию `.env.example` настроен на **REST и Swagger без реального Telegram** (`TELEGRAM_BOT_REGISTER=false`).

Ключевые переменные:

| Переменная                                          | Назначение                                    |
|-----------------------------------------------------|-----------------------------------------------|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | учётные данные Postgres (дефолт: `tinderbot`) |
| `POSTGRES_PORT`, `APP_PORT`                         | порты снаружи (дефолт: `5433`, `8080`)        |
| `API_SECURITY_ENABLED`, `JWT_SECRET` | REST auth в Docker |
| `TELEGRAM_BOT_*`, `OPENAI_*`                        | Telegram и LLM                                |

### 2. Сборка и запуск

```bash
docker compose up --build
```

Стек: `postgres:16-alpine` + Spring Boot app (`SPRING_PROFILES_ACTIVE=docker`).

### 3. Проверка

```bash
curl -s http://localhost:8080/actuator/health
curl -s http://localhost:8080/v3/api-docs | head -c 200    # OpenAPI JSON
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","password":"password"}' | jq -r .accessToken)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/sessions/1
open http://localhost:8080/swagger-ui.html
```

Готовые HTTP-запросы: [`requests/tinderbot.http`](requests/tinderbot.http) — в HTTP Client выберите environment **local**, сначала выполните запрос **JWT login**.

### 4. Локальная разработка (без Docker app)

```bash
docker compose up postgres -d
mvn spring-boot:run              # профиль dev, Postgres на :5433
mvn test                         # unit + integration 
```

| Профиль  | Назначение                                             |
|----------|--------------------------------------------------------|
| `dev`    | Локальный Postgres `:5433`, auth выключен по умолчанию |
| `docker` | Контейнер, auth включён                                |
| `it`     | Integration-тесты (Testcontainers PostgreSQL)          |

---

## REST API (`/api/v1`)

### Sessions

| Метод  | Путь                      | Описание                       |
|--------|---------------------------|--------------------------------|
| GET    | `/sessions/{chatId}`      | Состояние сессии               |
| PATCH  | `/sessions/{chatId}/mode` | Смена режима                   |
| DELETE | `/sessions/{chatId}`      | Удаление сессии (404 если нет) |

### Questionnaires

| Метод | Путь                                                        | Описание            |
|-------|-------------------------------------------------------------|---------------------|
| POST  | `/sessions/{chatId}/questionnaires/{PROFILE\|OPENER}/start` | Старт опросника     |
| POST  | `/sessions/{chatId}/questionnaires/{type}/answers`          | Ответ на вопрос     |
| POST  | `/sessions/{chatId}/questionnaires/{type}/generate`         | Генерация через GPT |

### Dialogs (GPT, переписка, DATE)

| Метод | Путь                               | Описание                                      |
|-------|------------------------------------|-----------------------------------------------|
| POST  | `/sessions/{chatId}/gpt/messages`  | Вопрос ChatGPT                                |
| POST  | `/sessions/{chatId}/messages`      | Добавить реплику в историю                    |
| POST  | `/sessions/{chatId}/messages/next` | Сгенерировать следующее сообщение             |
| POST  | `/sessions/{chatId}/date/messages` | Сообщение звезде (DATE); первое — с `starKey` |

>`starKey`: `date_grande`, `date_robbie`, `date_zendaya`, `date_gosling`, `date_hardy`

### Auth

| Метод | Путь          | Описание                        |
|-------|---------------|---------------------------------|
| POST  | `/auth/login` | JWT login (`demo` / `password`) |

### Примеры auth

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","password":"password"}' | jq -r .accessToken)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/sessions/1
```

---

## Telegram E2E

1. В `.env` задайте реальные токены:

```env
TELEGRAM_BOT_REGISTER=true
TELEGRAM_BOT_NAME=your_bot_name
TELEGRAM_BOT_TOKEN=123456789:AAH...
OPENAI_TOKEN=sk-...
```

2. Проверьте сборку: `mvn clean test`

3. Запустите и смотрите логи:

```bash
docker compose up --build
docker compose logs -f app
```

4. В Telegram: `/start` → профиль → opener → GPT → DATE (выбор звезды)

---

## Тесты

```bash
mvn clean test
```

| Слой            | Что проверяется                                                                        |
|-----------------|----------------------------------------------------------------------------------------|
| **Unit**        | Handlers, callbacks, parsers, auth, session access (Mockito / plain)                   |
| **Slice**       | `@WebMvcTest`, `@DataJpaTest` для controllers и persistence                            |
| **Integration** | Postgres Testcontainers (singleton), REST end-to-end, JWT, actuator, OpenAPI |

Integration-тесты (заменяют shell smoke-сценарии):

| Класс                                        | Сценарий                                        |
|----------------------------------------------|-------------------------------------------------|
| `ApplicationEndpointsIntegrationTest`        | health, info, swagger-ui, `/v3/api-docs`        |
| `ApiSecurityIntegrationTest`                 | JWT login, доступ к своей/чужой сессии |
| `SessionRestControllerIntegrationTest`       | GET/DELETE сессии                               |
| `QuestionnaireRestControllerIntegrationTest` | PROFILE/OPENER цикл                             |
| `DialogRestControllerIntegrationTest`        | mode, GPT, messages, DATE                       |
| `UserSessionServiceIntegrationTest`          | persistence + cache eviction                    |

---

## Паттерны

- **Strategy** — опросники PROFILE / OPENER (`service/questionnaire/`)
- **Mode handler** — маршрутизация по `DialogMode` в Telegram
- **Session slices** — `api/session/*` + `UserSessionService` / `SessionPersistence` (кэш + Postgres)
- **Registry** — lookup без static-методов в enum (`MenuOptionRegistry`, `StarRegistry`)
- **Resilience4j** — rate limit / retry для LLM
- **API auth** — `users` + `user_credentials` в Flyway, без JPA cascade

---

## Структура репозитория

```text
tinder-bot/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── lombok.config
├── .env.example              — шаблон env (см. README «Быстрый старт»)
├── requests/
│   └── tinderbot.http        — готовые REST-запросы (JWT)
├── src/main/java/com/tinderbot/telegram/
│   ├── TinderBotApplication.java
│   ├── api/                    — контракты сервисов
│   │   ├── ISessionService.java    — композитный фасад сессии
│   │   ├── IChatGPTService.java
│   │   ├── ModeHandler.java, CallbackHandler.java
│   │   └── session/              — slice-интерфейсы (SessionStore, …)
│   ├── controller/             — TinderBotController + REST `/api/v1`
│   ├── dto/                    — REST request/response
│   ├── entity/                 — JPA: user_sessions, users, user_credentials
│   ├── model/                  — UserSession, DialogMode, MenuOption, Star
│   ├── repository/             — Spring Data JPA
│   ├── mapper/                 — MapStruct (domain ↔ DTO ↔ payload)
│   ├── service/
│   │   ├── session/            — UserSessionService, SessionPersistence
│   │   ├── questionnaire/      — Strategy PROFILE / OPENER
│   │   ├── dialog/             — GPT, MESSAGE, DATE
│   │   ├── telegram/           — MessageCleaner, MainMenuService
│   │   ├── auth/               — JWT
│   │   └── llm/                — ChatGPT client, parsers
│   ├── handler/
│   │   ├── mode/               — Main, Profile, Opener, Gpt, Date, …
│   │   └── callback/           — inline-кнопки, выбор звезды
│   ├── view/                   — MessageView, KeyboardFactory
│   ├── core/                   — BotRegistration, Telegram helpers
│   ├── common/
│   │   ├── config/             — Security, OpenAPI, OpenAI, registries
│   │   └── util/               — MessageSender, constants
│   └── exception/              — GlobalExceptionHandler
├── src/main/resources/
│   ├── application.yml         — профили dev / docker
│   ├── db/migration/           — Flyway V1 sessions, V2 users
│   ├── messages/               — тексты бота
│   └── prompts/                — GPT-промпты (profile, opener, date_*)
└── src/test/java/com/tinderbot/telegram/
    ├── integration/            — Testcontainers + REST smoke
    ├── controller/             — @WebMvcTest
    ├── handler/                — unit handlers
    ├── service/                — unit services
    ├── view/                   — unit view
    ├── testsupport/            — fixtures, TestUserSeedConfiguration
    └── resources/
        ├── application.properties   — unit/slice (H2)
        └── application-it.properties — integration (security on)
```

---

## Полезные команды

```bash
docker compose up --build -d    # пересобрать и запустить в фоне
docker compose ps               # статус контейнеров (ожидается healthy)
docker compose logs -f app
docker compose down
docker compose down -v          # пересоздать БД (сброс кредов из .env)
mvn clean test                  # unit + integration
```

Быстрая проверка после Docker-старта:

```bash
curl -s http://localhost:8080/actuator/health
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","password":"password"}'
```

---

## Автор

[charset-8utf](https://github.com/charset-8utf)
