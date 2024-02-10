# OpenBlocks API Gateway

В данном проекте представлен набор сервисов для реализации API Gateway для вашей организации.

**API Gateway** &mdash; это архитектурный паттерн, который подразумевает, что взаимодействие 
с какой-то информационной системой происходит через посредника, собственно гейтвей.

Этот посредник часто реализует следующие функции:
- **Аутентификация**
- Авторизация
- Service Discovery
- Маршрутизация
- Очистка данных в запросе
- Обогащение данных в запросе

## auth-manager-service
Сервис **auth-manager-service** представляет собой Менеджер аутентификации, который заведует
следующими вопросами:
- Проверка логина и пароля пользователя по имеющейся БД
- Выдача JWT-токена для доступа к другим системам
- Публикация JWKS для валидации токена другими системами

### Эндпойнты

POST /api/v1/oauth2/token

Верифицирует логин и пароль пользователя, возвращает JWT-токен в случае успеха.
Принимает набор параметров, соответствующий спецификации **OAuth2**.

Возвращает токен в формате, соответствующем спецификации Oauth2.

Пример запроса:
```bash
curl --location 'http://localhost:8092/api/v1/oauth2/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'client_id=test@example.com' \
--data-urlencode 'client_secret=test_pwd'
```

---

GET /api/v1/oauth2/.well-known/jwks.json

Возвращает JWKS для верификации токена другими сервисами в системе.
Пример ответа:

```json
{
    "kty": "EC",
    "d": "AZI52NY9J4QLph4vNMtii2bjCfomaOnwXKTCVaSls7IBrM5iKA5JJWFzK7hGSuPyTN1FzcX0mxxZDigV9RhSrSuV",
    "crv": "P-521",
    "x": "APkp-bMYv8PzdOR99HT1sL79WvjzoD3R76RXL_R4LyyOFDGUYAH4H7dGF1aZOI1TCbEcgn36yaEVk6iVFSbQs9V0",
    "y": "ATbD54DOPCVGvoI5febzYOEvchz2p4bjADYZmiw4mFsFhUNrk1M93Ien2YnIMnRKxqzwcABCdzorbR1LgepQltOC"
}
```

### Предполагаемая структура БД и предварительные требования
Требуется БД PostgreSQL версии 12+.

В БД должна существовать таблица **user_data** со следующим атрибутивным составом:

| Имя поля |  Тип   | Описание                                                                              |
|----------|:------:|---------------------------------------------------------------------------------------|
| login    | string | Логин пользователя в любом предпочитаемом формате, по умолчанию предполагается имейл. |
| password | string | Хешированный пароль пользователя, предполагается использование **BCrypt**             |

### Техническое описание

Сервис написан на языке программирования Java, для запуска требуется LTS версия 21.

Используется фреймворк Spring Boot 3.

Сервис работает как REST API для потребителей, не имеет механизмов рендеринга данных и может быть
использован независимо от других компонентов приложения "Управление".

### Системные требования
| Требование       |        Версия         |
|------------------|:---------------------:|
| JDK              |          21           |
| PostgreSQL       |          12+          |
| Operating system | Windows, Linux, MacOS |

### Об инициативе OpenBlocks

Инициатива **OpenBlocks** &mdash; это проекты с открытым исходным кодом. Основная цель состоит в том,
чтобы предоставить открытые и расширяемые решения для пользователей любого масштаба.


