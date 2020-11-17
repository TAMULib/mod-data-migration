FROM alpine:3.11

#Prerequisites
RUN apk add --no-cache curl

#Create folders in container
RUN mkdir -p /usr/local/bin/folio/install

#Set working directory
WORKDIR /usr/local/bin/folio

#Copy in scripts and files
COPY import-users.sh /usr/local/bin/folio

#Set environment variables for script
ENV DIVIT_DB_PASSWORD='password'
ENV TENANT_ID='mytenant'

#Execute the script on container runtime
CMD ["/usr/local/bin/folio/import-users.sh"]