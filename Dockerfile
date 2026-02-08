FROM maven:3.9-eclipse-temurin-17-alpine as maven-build
WORKDIR /app
COPY . .
RUN mvn verify

FROM eclipse-temurin:17-jre-alpine
RUN adduser \
 -h /var/github-summary \
 -D -u 1000 \
 ghsum ghsum

USER ghsum
WORKDIR /var/github-summary

ENV TOKENS=""
ENTRYPOINT ["java", "-Dapi-tokens=${TOKENS}", "-jar", "profile-summary-for-github.jar"]
EXPOSE 7070

COPY --from=maven-build \
 --chown=ghsum:ghsum \
 /app/target/profile-summary-for-github-jar-with-dependencies.jar profile-summary-for-github.jar
