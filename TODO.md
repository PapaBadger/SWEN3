# Features for Sprint 3
## Queues Integration:

- Extend docker-compose.yml to run RabbitMQ in a container
- Integrate Queues into REST Server
- on document upload the REST-Server should also
- -  send a message to the RabbitMQ
- -  will be processed by an "empty" OCR-worker
- Failure/exception-handling (with layer-specific exceptions) implemented
- Logging in remarkable/critical positions integrated
- Prepare for the mid-term Code-Review


## MUST-HAVE Check Criteria:

- No build error (docker compose build)
- docker compose up successfully starts containers `DONE`
-POST http://localhost/... some PDF-Document will lead to a log-entry at the worker-service (for to be processed with OCR)

# !! Deadline TBD !!