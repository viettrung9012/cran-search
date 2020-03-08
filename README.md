# CRAN Search
## Description
This is an application to read the list of packages from CRAN server, store their details in DB and expose a REST API endpoint to query for the packages
## Architecture

## Requirements
- Docker Engine - 19.03.5
- docker-compose - 1.25.4

For Mac and Windows, these should be setup automatically with [Docker Desktop](https://www.docker.com/products/docker-desktop)

For Linux, please follow the installation instructions [here](https://docs.docker.com/install/)  
## Setup and Running
Build and run
```shell script
docker-compose build
docker-compose up

# navigate to http://localhost:8080/ to view API
```
Stop and tear down
```shell script
docker-compose down
```
