# FilmDB - demo Spring Boot REST service

FilmDB is a REST service application created with Spring Boot.

### Running with the Docker
You can run the app with the provided `docker-compose.yml` file by executing `docker compose up` from the project
directory. The compose stack contains application service `app`, database service `postgres` and a volume
`filmdb_postgres_data`.

### Documentation

To see `springdoc-openapi` generated OpenAPI 3 documentation visit http://localhost:8080/v3/api-docs.
Or use Swagger-ui: http://localhost:8080/swagger-ui.html

### Access

To browse hypermedia-driven data directly: http://localhost:8080/api

### Database configuration

The app uses in-memory H2 database by default. To use persistent PostgreSQL database the app must be run with `postgres`
profile by setting environment variable `spring.profiles.active=postgres`.

To run PostgreSQL in a docker container:
```
docker run -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=secret -e POSTGRES_DB=filmdb postgres:15-alpine
```

## Libraries used

[Flyway](https://flywaydb.org/), [Mapstruct](https://github.com/mapstruct/mapstruct).

Testing: [JUnit5](https://junit.org/junit5/), [AssertJ](https://github.com/assertj/assertj), [Mockito](https://site.mockito.org/).

## License

MIT License.