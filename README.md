# GIT Usage Basics

## Start a feature (you currently only have main)

`git checkout main`

`git pull origin main`

### Create and switch to a feature branch

`git checkout -b feature/<short-name>`   # e.g., feature/login

`git push -u origin feature/<short-name>`  # publish branch (once)

## Work on feature

### Add files and commit often (small, focused commits)

`git add -A`

`git commit -m "feat(login): show login form"`

### Push your progress

`git push`

## Keep  your branch fresh if main moved ahead
(Do this before you finish/merge)

`git checkout main`

`git pull origin main`

`git checkout feature/<short-name>`

`git rebase main`       (!! preferred: linear history)

if conflicts: edit files -> `git add` the fixed files -> `git rebase --continue`
#### to abort rebase: git rebase --abort

### After a rebase, force-push the branch (safe here)
`git push --force-with-lease`

## Finish the feature (merge back to main)

### Option A – Fast-forward (clean & simple, after rebase)

`git checkout main`

`git pull origin main`

`git merge --ff-only feature/<short-name>`   # will fail if not a fast-forward

`git push origin main`

### Option B – Squash merge (one commit on main)

`git checkout main`

`git pull origin main`

`git merge --squash feature/<short-name>`

`git commit -m "feat(<scope>): <summary> (#<issue/PR>)"`

`git push origin main` 

## Clean up merged branches

`git branch -d feature/<short-name>`                  # delete local

`git push origin --delete feature/<short-name>`       # delete remote



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
6. If the application is up and running, go to your cmd and type `curl http://localhost:8080/documents`
8. Now you can add data within pgAdmin or in the cmd -> command: `curl.exe -i -X POST http://localhost:8080/documents -H "Content-Type: application/json" -d "{\"title\":\"Mein erstes Doc\",\"content\":\"Hallo DB\"}"`
7. Now you should see something like this: `[{"id":1,"title":"Mein erstes Doc","content":"Hallo DB"}]`


## RUNNING THE APP WITH DOCKER

- go into the terminal in IntelliJ
- Command: `docker compose up -d --build`
- Command for showing logs: `docker compose logs -f app`
- At this point, the db is empty, so you have to add data, go into cmd
- Command: `curl.exe -i -X POST http://localhost:8080/documents -H "Content-Type: application/json" -d "{\"title\":\"Mein erstes Doc\",\"content\":\"Hallo DB\"}"`
- To show all data -> command: `curl http://localhost:8080/documents`
- Output something like this: `[{"id":1,"title":"Mein erstes Doc","content":"Hallo DB"}]`


## STARTING THE TESTS

- `mvn test`