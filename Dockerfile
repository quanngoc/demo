FROM openjdk:17
LABEL maintainer="quanngoc"
VOLUME /tmp
COPY ./build/libs/*.jar demo.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=dev","demo.jar"]