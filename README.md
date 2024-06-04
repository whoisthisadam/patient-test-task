Проект состоит из четырех компонетнов:
 - API(Resource Server),
 - Клиент - консольное Spring Boot приложение
 - Keycloak(Authorization Server)
 - БД (PostgreSQL)

В Keycloak имеются роли Patient и Practitioner. Так же client-based роли для доступа к ресурсу Patients - read, write, deletw. При создании Patient(права на это есть только у Practitioner), он сохранятеся и в БД, и в Keycloak.

Кроме realm-based роли, каждый метод Resource Server'a защищен еще и по правам на доступ к ресурсу Patient. Все права и роли беруться из JWT Токена, который генерируется Authorization server'ом  

Также, в методе getPatient, в случае если у клиента роль Patient, проверяется имя запршиваемого patient на совпадение с именем аутентифицированного юзера, для того чтобы каждый Patient мог получить только свои данные.

Права на удаление и создание Patient у разных Practitioner. 

Приложено 3 Postman environment для разных ролей и прав, а также коллекция с OAuth2

Деплоймент на VM через docker-compose.
