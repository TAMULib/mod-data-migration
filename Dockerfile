# build base image
FROM maven:3-jdk-8-alpine as maven

# copy pom.xml
COPY ./pom.xml ./pom.xml

# copy src files
COPY ./src ./src

# build
RUN mvn package

# final base image
FROM openjdk:8u171-jre-alpine

# set deployment directory
WORKDIR /mod-external-reference-resolver

# copy over the built artifact from the maven image
COPY --from=maven /target/mod-external-reference-resolver*.jar ./mod-external-reference-resolver.jar

#Settings
ENV LOGGING_LEVEL_FOLIO='INFO'
ENV SERVER_PORT='9003'
ENV SPRING_DATASOURCE_PLATFORM='h2'
ENV SPRING_DATASOURCE_URL='jdbc:h2:./mod-external-reference-resolver;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE'
ENV SPRING_DATASOURCE_DRIVERCLASSNAME='org.h2.Driver'
ENV SPRING_DATASOURCE_USERNAME='folio'
ENV SPRING_DATASOURCE_PASSWORD='folio'
ENV SPRING_H2_CONSOLE_ENABLED='true'
ENV SPRING_JPA_DATABASE_PLATFORM='org.hibernate.dialect.H2Dialect'
ENV SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT='org.folio.rest.dialect.CustomH2Dialect'
ENV SPRING_JPA_PROPERTIES_HIBERNATE_JDBC_BATCH_SIZE='1000'
ENV TENANT_DEFAULT_TENANT='tern'
ENV TENANT_INITIALIZE_DEFAULT_TENANT='false'

#expose port
EXPOSE ${SERVER_PORT}

#run java command
CMD java -jar ./mod-external-reference-resolver.jar \
  --logging.level.org.folio=${LOGGING_LEVEL_FOLIO} --server.port=${SERVER_PORT} --spring.datasource.platform=${SPRING_DATASOURCE_PLATFORM} \
  --spring.datasource.url=${SPRING_DATASOURCE_URL} --spring.datasource.driverClassName=${SPRING_DATASOURCE_DRIVERCLASSNAME} \
  --spring.datasource.username=${SPRING_DATASOURCE_USERNAME} --spring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
  --spring.h2.console.enabled=${SPRING_H2_CONSOLE_ENABLED} --spring.jpa.database-platform=${SPRING_JPA_DATABASE_PLATFORM} \
  --spring.jpa.properties.hibernate.dialect=${SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT} --spring.jpa.properties.hibernate.jdbc.batch_size=${SPRING_JPA_PROPERTIES_HIBERNATE_JDBC_BATCH_SIZE} \
  --tenant.default-tenant=${TENANT_DEFAULT_TENANT} --tenant.initialize-default-tenant=${TENANT_INITIALIZE_DEFAULT_TENANT}