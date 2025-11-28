# Dockerfile multi-stage para aplicação Spring Boot com Maven
# Estágio builder: compila a aplicação usando Maven e JDK 21
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copiar arquivos do projeto (copiar mvnw e .mvn, se presentes, acelera builds com cache)
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src ./src

# Compilar a aplicação (pula testes no build Docker para maior velocidade)
RUN mvn -B -DskipTests package

# Estágio runtime base: imagem JRE menor que recebe o jar gerado
FROM eclipse-temurin:21-jre AS base
WORKDIR /app

# Copia o jar final gerado pelo estágio builder
COPY --from=builder /workspace/target/*.jar app.jar

# Usar usuário não-root por segurança
RUN useradd -u 1000 -m appuser && chown -R appuser:appuser /app
USER appuser

ENV JAVA_OPTS=""
EXPOSE 8080

# Estágio homolog (nomeado) - pode ser direcionado via docker-compose build.target
FROM base AS homolog
ENV SPRING_PROFILES_ACTIVE=homolog
ENV JAVA_OPTS="-Dspring.profiles.active=homolog $JAVA_OPTS"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

# Estágio production (nomeado) - pode ser direcionado via docker-compose build.target
FROM base AS production
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Dspring.profiles.active=prod $JAVA_OPTS"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
