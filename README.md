# A Small REST Service example for Interacting with MongoDB Information Using Play3

## Startup Requirements
A pre-installed version of Java 21 and access to MongoDB are required.

If necessary, you can change the database access settings in /conf/application.conf.

The service can be started using the sbt command run.

For authentication, run the service with your username and password already saved in the database.
Example:
curl --header "Content-Type:application/json" --request POST --data "{\"username\": \"*yourUsername*\", \"password\": \"*yourPassword*\", \"grant_type\": \"password\"}" http://127.0.0.1:9000/oauth2/auth

## Examples of Service Requests

1) To retrieve the list of students:
curl --header "Content-Type:application/json" --request GET http://127.0.0.1:9000/students/get

2) To add a student to the database:
curl --header "Content-Type:application/json" --header "Authorization: Bearer *pasteYourToken*" --request PUT --data "{\"surname\":\"Smirnov\",\"name\":\"Sergey\",\"patronym\":\"Petrovich\",\"group\":\"c61\",\"avgScore\":4.12}" http://127.0.0.1:9000/students/add

3) To update a student's record (Use the id obtained from the second request or an existing student):
curl --header "Content-Type:application/json" --header "Authorization: Bearer *pasteYourToken*" --request POST --data "{\"avgScore\":4.18,\"group\":\"c62\"}" http://127.0.0.1:9000/students/update?id=pasteYourId

4) To delete a student from the database (Use the id obtained from the second request or an existing student):
curl --header "Content-Type:application/json" --header "Authorization: Bearer *pasteYourToken*" --request DELETE http://127.0.0.1:9000/students/delete?id=pasteYourId
