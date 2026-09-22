
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY src ./src

RUN javac src/main/java/com/roxana/*.java -d out

EXPOSE 6379

CMD ["java", "-cp", "out", "com.roxana.Main"]