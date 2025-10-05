# Features for Sprint 3
## Queues Integration:

- Extend docker-compose.yml to run RabbitMQ in a container `DONE`
- Integrate Queues into REST Server `DONE`
- on document upload the REST-Server should also `DONE`
- -  send a message to the RabbitMQ `DONE`
- -  will be processed by an "empty" OCR-worker `DONE`
- Failure/exception-handling (with layer-specific exceptions) implemented `DONE`
- Logging in remarkable/critical positions integrated `DONE`
- Prepare for the mid-term Code-Review


## MUST-HAVE Check Criteria:

- No build error (docker compose build) `DONE`
- docker compose up successfully starts containers `DONE`
- POST http://localhost/... some PDF-Document will lead to a log-entry at the worker-service (for to be processed with OCR)

# !! Deadline TBD !!