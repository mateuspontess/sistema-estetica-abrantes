# Etapa de construção
FROM eclipse-temurin:17-jdk-alpine AS builder

# Define o diretório de trabalho dentro do container
WORKDIR /application

# Copia o código fonte e arquivos de configuração Maven para o container
COPY src src
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# Fix line endings for Windows and make executable
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Executa o Maven para construir o projeto, ignorando os testes
RUN ./mvnw clean install -DskipTests


# Etapa de execução
FROM eclipse-temurin:17-jre-alpine

# Define o diretório de trabalho dentro do container
WORKDIR /application

# Expõe a porta
EXPOSE 8080

# Copia o arquivo JAR gerado na etapa de construção para o container final
COPY --from=builder /application/target/*.jar ./application.jar

# Define o comando de entrada para iniciar a aplicação
ENTRYPOINT [ "java", "-jar", "application.jar" ]
