# FilmDB - RESTful/GraphQL web service created with Spring Boot

FilmDB is a demo application that allows to manage a database of films, people and their creative roles.

## How to run

Besides IDE the application can be started with Docker by executing `docker compose up -d` from the project directory.

### Database configuration

The application uses in-memory H2 database by default. To use persistent PostgreSQL database the application must be run
with `postgres` spring profile.

To run PostgreSQL database in a docker container named `filmdb-postgres-db`:

```
docker run -d --name filmdb-postgres-db -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=secret -e POSTGRES_DB=filmdb postgres:16-alpine
```

## Usage

### REST

Running service can be accessed with the following URLs:

API root - http://localhost:8080/api

Swagger UI documentation - http://localhost:8080/swagger-ui.html

OpenAPI 3 documentation - http://localhost:8080/v3/api-docs

### GraphQL

GraphiQL IDE is available at http://localhost:8080/graphiql

GraphQL requests are accepted at http://localhost:8080/graphql

## Authentication and authorization

Authentication is implemented with JSON Web Tokens. You must be authenticated as `admin` to be authorized to modify data.
Authenticated requests must provide a valid token in `Authorization` header using the `Bearer` schema:

```
"Authorization": "Bearer <token>"
```

### REST

To get a token, perform POST request at `/api/login` with the following credentials:

```
{
  "username": "admin",
  "password": "password"
}
```

### GraphQL

JWT can be obtained with the following query:

```
{
  login(username: "admin", password: "password")
}
```

## Screenshots

REST API

<img src="https://github.com/meume/FilmDB/assets/24320267/bb074831-784b-4710-bf7f-ad873e903e1a" alt="API example" title="API example" width="600"/>

Swagger

<img src="https://github.com/ashipo/FilmDB/assets/24320267/207815f2-8cb7-40fe-82b2-54d6a22b79e0" alt="Swagger example" title="Swagger example" width="600"/>

GraphiQL

<img src="https://github.com/meume/FilmDB/assets/24320267/bb074831-784b-4710-bf7f-ad873e903e1a" alt="API example" title="API example" width="600"/>

## License

This project is licensed under the MIT license.
