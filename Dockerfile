# syntax=docker/dockerfile:1

FROM amazoncorretto:17-alpine as build
WORKDIR /app
COPY . ./
RUN ./gradlew bootJar --no-daemon

FROM amazoncorretto:17-alpine as layers
WORKDIR /app
COPY --from=build /app/build/libs/filmdb-*.jar filmdb.jar
RUN java -Djarmode=layertools -jar filmdb.jar extract

FROM amazoncorretto:17-alpine as production
WORKDIR /applayers
COPY --from=layers app/dependencies/ ./
COPY --from=layers app/spring-boot-loader/ ./
COPY --from=layers app/snapshot-dependencies/ ./
COPY --from=layers app/application/ ./
EXPOSE 8080
CMD ["java", "-Dspring.profiles.active=postgres", "org.springframework.boot.loader.JarLauncher"]
