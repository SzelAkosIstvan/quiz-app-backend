# Stage 1: Build with Maven
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/QuizApp-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]


ENV DATABASE_URL = jdbc:postgresql://localhost:5432/quiz_app
ENV DATABASE_USERNAME = testuser
ENV DATABASE_PASSWORD = asd
ENV OPENAI_URL = https://api.openai.com/v1/chat/completions
ENV OPENAI_MODEL = gpt-4o-mini
ENV OPENAI_KEY = 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
ENV AWS_ACCESS_KEY = AKIA6GSNG2NJIPBSWNJ7
ENV AWS_SECRET_KEY = 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
ENV AWS_BUCKET_NAME = vduzzle-images
ENV FRONTEND_URL = http://localhost:3000

#docker run -p 8080:8080 -e SPRING_DATASOURCE_PASSWORD=secret quiz-app