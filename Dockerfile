FROM openjdk:13-slim as builder
WORKDIR app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw package

ARG JAR_FILE=target/*.jar
RUN cp ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract


FROM openjdk:13-slim
WORKDIR app

COPY --from=builder app/dependencies/ ./
COPY --from=builder app/snapshot-dependencies/ ./
COPY --from=builder app/resources/ ./
COPY --from=builder app/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]