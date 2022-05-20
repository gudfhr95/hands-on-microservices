FROM openjdk:11

ENV HOST '0.0.0.0'

EXPOSE 9999

ADD ./build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]