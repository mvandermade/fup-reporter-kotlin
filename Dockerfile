FROM eclipse-temurin:21 as builder
COPY . /opt/repo
WORKDIR /opt/repo
RUN chmod +x ./gradlew && ./gradlew bootJar

FROM eclipse-temurin:21 as runner
RUN mkdir /opt/app
COPY --from=builder /opt/repo/build/libs/made-funicular-stamp-reporter-kotlin-0.0.1-SNAPSHOT.jar /opt/app/made-funicular-stamp-reporter-kotlin-0.0.1-SNAPSHOT.jar
CMD ["java", "-jar", "/opt/app/made-funicular-stamp-reporter-kotlin-0.0.1-SNAPSHOT.jar"]
