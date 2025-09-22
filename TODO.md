# Features for Sprint 2
## (Web-)UI

- Webserver service (e.g. nginx or else) integrated
- Dashboard and detail-pages are served by the webserver
- The Webpage communication with the REST server
- Extend docker-compose.yml to run the UI in an additional container

## MUST-HAVE Check Criteria:

- No build error (docker compose build)
- docker compose up successfully starts containers
- GET http://localhost/ returns the functioning paperless-frontend