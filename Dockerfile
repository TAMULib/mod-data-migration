# build base image
FROM maven:3-jdk-8-alpine as maven

# copy pom.xml
COPY ./pom.xml ./pom.xml

# copy src files
COPY ./src ./src

# Copy the sub-modules to the container
COPY ./data-import-raml-storage ./data-import-raml-storage
COPY ./mod-inventory-storage ./mod-inventory-storage
COPY ./mod-organizations-storage ./mod-organizations-storage
COPY ./mod-users ./mod-users
COPY ./mod-circulation ./mod-circulation
COPY ./mod-feesfines ./mod-feesfines
COPY ./mod-notes ./mod-notes

# build
RUN mvn package

# final base image
FROM openjdk:8u191-jre-alpine

# set deployment directory
WORKDIR /mod-data-migration

# copy over the built artifact from the maven image
COPY --from=maven /target/mod-data-migration*.jar ./mod-data-migration.jar

#Settings
ENV LOGGING_LEVEL_FOLIO='INFO'
ENV SERVER_PORT='9003'
ENV SPRING_DATASOURCE_PLATFORM='h2'
ENV SPRING_DATASOURCE_URL='jdbc:h2:./mod_data_migration;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE'
ENV SPRING_DATASOURCE_DRIVERCLASSNAME='org.h2.Driver'
ENV SPRING_DATASOURCE_USERNAME='folio_admin'
ENV SPRING_DATASOURCE_PASSWORD='folio_admin'
ENV SPRING_H2_CONSOLE_ENABLED='true'
ENV SPRING_JPA_DATABASE_PLATFORM='org.hibernate.dialect.H2Dialect'
ENV SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT='org.hibernate.dialect.H2Dialect'
ENV SPRING_JPA_PROPERTIES_HIBERNATE_JDBC_BATCH_SIZE='1000'
ENV TENANT_DEFAULT_TENANT='tern'
ENV TENANT_INITIALIZE_DEFAULT_TENANT='false'
ENV ACTIVE_PROCESSOR_COUNT='12'

ENV TIME_ZONE='America/Chicago'

#expose port
EXPOSE ${SERVER_PORT}

#run java command
CMD java -XX:ActiveProcessorCount=${ACTIVE_PROCESSOR_COUNT} -Duser.timezone=${TIME_ZONE} -jar ./mod-data-migration.jar \
  --logging.level.org.folio=${LOGGING_LEVEL_FOLIO} --server.port=${SERVER_PORT} --spring.datasource.platform=${SPRING_DATASOURCE_PLATFORM} \
  --spring.datasource.url=${SPRING_DATASOURCE_URL} --spring.datasource.driverClassName=${SPRING_DATASOURCE_DRIVERCLASSNAME} \
  --spring.datasource.username=${SPRING_DATASOURCE_USERNAME} --spring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
  --spring.h2.console.enabled=${SPRING_H2_CONSOLE_ENABLED} --spring.jpa.database-platform=${SPRING_JPA_DATABASE_PLATFORM} \
  --spring.jpa.properties.hibernate.dialect=${SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT} --spring.jpa.properties.hibernate.jdbc.batch_size=${SPRING_JPA_PROPERTIES_HIBERNATE_JDBC_BATCH_SIZE} \
  --tenant.default-tenant=${TENANT_DEFAULT_TENANT} --tenant.initialize-default-tenant=${TENANT_INITIALIZE_DEFAULT_TENANT}
