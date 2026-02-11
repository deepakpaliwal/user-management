FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml pom.xml
COPY uums-batch/pom.xml uums-batch/pom.xml
COPY uums-batch/src uums-batch/src
RUN mvn -pl uums-batch -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /workspace/uums-batch/target/uums-batch-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
