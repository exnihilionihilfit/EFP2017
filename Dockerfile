FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/upload_validate_java.jar /upload_validate_java/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/upload_validate_java/app.jar"]
