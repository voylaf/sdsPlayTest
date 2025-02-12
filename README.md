# Небольшой REST-сервис для взаимодействия с информацией в MongoDB с помощью Play3.

## Условия запуска
Необходима установленная версия Java 21 и доступ к MongoDB.

Если нужно, поменять настройки доступа к базе данных можно в /conf/application.conf.

Запустить сервис можно из sbt командой run

Для авторизации необходимо запустить с вашими логином и паролем
curl --header "Content-Type:application/json" --request POST --data "{\"username\": \"*yourUsername*\", \"password\": \"*yourPassword*\", \"grant_type\": \"password\"}" http://127.0.0.1:9000/oauth2/auth

## Примеры запросов к сервису.

1) Получить список студентов:
curl --header "Content-Type:application/json" --request GET http://127.0.0.1:9000/students/get

2) Добавить студента в базу:
curl --header "Content-Type:application/json" --header "Authorization: Bearer *pasteYourToken*" --request PUT --data "{\"surname\":\"Smirnov\",\"name\":\"Sergey\",\"patronym\":\"Petrovich\",\"group\":\"c61\",\"avgScore\":4.12}" http://127.0.0.1:9000/students/add

3) Изменить сущность студента (*Используется ид, полученный из второго запроса или уже существующий студент*):
curl --header "Content-Type:application/json" --header "Authorization: Bearer *pasteYourToken*" --request POST --data "{\"avgScore\":4.18,\"group\":\"c62\"}" http://127.0.0.1:9000/students/update?id=pasteYourId

4) Удалить студента из базы (*Используется ид, полученный из второго запроса или уже существующий студент*):
curl --header "Content-Type:application/json" --header "Authorization: Bearer *pasteYourToken*" --request DELETE http://127.0.0.1:9000/students/delete?id=pasteYourId