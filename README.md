# HOW TO

## RUNNING THE APP WITH DOCKER

- go into the terminal in IntelliJ
- Command: `docker-compose up -d --build`
- Command for showing logs: `docker-compose logs -f app`

## WHEN CHANGING DB SCHEMA DO THE FOLLOWING:
docker-compose down -v (used for a clean reset, all data gets deleted too)

docker-compose up -d --build

## Connecting to localhost

- `http://localhost:4200/`

## Connecting to rabbitMQ

- `http://localhost:15672/ (admin/admin)`

## Connecting to minIO

- `http://localhost:9000/ (minioadmin/minioadmin)`

## Using the XML Import:

- Create a folder 'input' in C:\
- Paste xml file into it
- Watch it go

## Proof

- ```docker-compose exec db psql -U dms_admin -d dms -c "SELECT id, title, access_count FROM documents;"```

## STARTING THE TESTS

- `mvn test`

## Starting specifically the Document Upload Integration Test:
- `mvn test -Dtest=DocumentUploadIntegrationTest`

# REST SERVER & DATABASE USE

### How to set up Database (steps for local setup):

1. open pgAdmin
2. create database, Name: dms
3. right-click on the database -> Query Tool
4. execute Command (create db user):
`CREATE USER dms_admin WITH PASSWORD 'dmsIsVeryCool123';
GRANT ALL PRIVILEGES ON DATABASE dms TO dms_admin;
GRANT USAGE, CREATE ON SCHEMA public TO dms_admin;`

if this is not working, look into which port you are using and if the credentials in the application.properties file match the credentials of the dms_admin user in pgAdmin

5. After setting up the DB, start the application, if the db is not connected, the program should terminate
6. If the application is up and running, go to your cmd and type `curl http://localhost:8080//api/documents`
8. Now you can add data within pgAdmin or in the cmd -> command: `curl.exe -i -X POST http://localhost:8080/api/documents -H "Content-Type: application/json" -d "{\"title\":\"Mein erstes Doc\",\"content\":\"Hallo DB\"}"`
7. Now you should see something like this: `[{"id":1,"title":"Mein erstes Doc","content":"Hallo DB"}]`


# Angular Frontend (WebUI)

## What you need:
- [Node.js](https://nodejs.org/) (Version 21 or newer)
- Angular CLI install:
  ```bash
  npm install -g @angular/cli
  
- after installing angular, go to frontend/WebUI (in terminal -> cd frontend/WebUI)
- ```bash
  npm install
  
- now all dependencies should be installed
- run the project with:
- ```bash
  ng serve
