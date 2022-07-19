FROM openjdk:11
LABEL maintainer="quanngoc"
VOLUME /tmp
EXPOSE 8080
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} demo-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-Dspring.profiles.active=dev","-jar","demo-0.0.1-SNAPSHOT.jar"]
