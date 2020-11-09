## Build Docker image

`docker build -t import-users .`

Deploy in Rancher with following daily cron `0 1 * * *`

### Linux console run command

`docker run --rm -d --network folio-network --name import-users -h import-users -e TENANT_ID=tamu -e DIVIT_DB_PASSWORD=password import-users`

### Notes

This container expects a Secret containing the DivIT DB credentials as well as the Tenant ID.

## Environment variables

When you deploy the `import-users` image, you can adjust the configuration by passing one or more environment variables on the `docker run` command line. This assumes you have a Docker network `folio-network` created.

### TENANT_ID

The short name of the Tenant. Defaults to `mytenant`.

### DIVIT_DB_PASSWORD

Password to use for the DivIT DB. Defaults to `password`.
