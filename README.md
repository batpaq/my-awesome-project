# Task Management API

## Описание

REST API для управления задачами, пользователями и аутентификацией. Поддерживает регистрацию, вход, выход, управление задачами текущего пользователя и обновление access-токена через refresh-токен.

---

## Базовый URL

http://localhost:8080
---

## Эндпоинты

### Task Controller

- Получить список задач текущего пользователя  
  `GET /tasks`

- Создать новую задачу  
  `POST /tasks`

- Обновить задачу по ID  
  `POST /tasks/{id}`

- Удалить задачу по ID  
  `DELETE /tasks/{id}`

### Auth Controller

- Регистрация пользователя  
  `POST /auth/register`

- Авторизация пользователя  
  `POST /auth/login`

- Выход из системы  
  `POST /auth/logout`

### User Controller

- Получить пользователя по ID  
  `GET /api/users/{id}`

- Обновить access-токен с помощью refresh-токена  
  `POST /api/users/token/refresh`

---

## Модели (Schemas)

- `Role` — роль пользователя  
- `Task` — задача  
- `UserApp` — пользователь  
- `RegisterRequest` — тело запроса при регистрации  
- `AuthRequest` — тело запроса при авторизации  
- `AuthResponse` — ответ с токенами  
- `ErrorResponse` — структура ошибки  

---

## Авторизация

Все защищённые эндпоинты требуют JWT в заголовке:


---

## Пример `.env` файла

```env
JWT_SECRET=your_jwt_secret
JWT_EXPIRATION=3600000
./gradlew bootRun
