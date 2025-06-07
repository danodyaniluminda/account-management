FROM harbor.biapay.net/maven/3-adoptopenjdk-11:01 as builder
WORKDIR /application
COPY . /application
RUN mvn dependency:go-offline
RUN mvn package -B -DskipTests
ARG JAR_FILE=/application/target/*.jar

FROM amazoncorretto:11
WORKDIR application
COPY --from=builder application/target/*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 9004
