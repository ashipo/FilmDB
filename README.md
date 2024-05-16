# FilmDB - RESTful web service created with Spring Boot

FilmDB is a demo application that allows to manage a database of films, people and their creative roles.

## Usage

Running service can be accessed with the following URLs:

API root - http://localhost:8080/api

Swagger UI documentation - http://localhost:8080/swagger-ui.html

OpenAPI 3 documentation - http://localhost:8080/v3/api-docs

### Authentication and authorization

Authentication is implemented with JSON Web Tokens. To perform requests that chage data you must provide a valid
token in `Authorization` header using the `Bearer` schema. To get a token, log into `/api/login` with the following
credentials:

```
{
  "username": "admin",
  "password": "password"
}
```

### Screenshots

<img src="https://github.com/meume/FilmDB/assets/24320267/bb074831-784b-4710-bf7f-ad873e903e1a" alt="API example" title="API example" width="400"/>
<img src="https://github.com/ashipo/FilmDB/assets/24320267/207815f2-8cb7-40fe-82b2-54d6a22b79e0" alt="Swagger example" title="Swagger example" width="400"/>

## How to run

Besides using IntelliJ IDEA you can run the application with Docker by executing `docker compose up` from the project
directory.

### Database configuration

The application uses in-memory H2 database by default. To use persistent PostgreSQL database the application must be run
with `postgres` profile by setting environment variable `spring.profiles.active=postgres`.

To run PostgreSQL database in a docker container named `filmdb-postgres-db`:

```
docker run --name filmdb-postgres-db -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=secret -e POSTGRES_DB=filmdb postgres:alpine
```

## License

This project is licensed under the MIT license.
