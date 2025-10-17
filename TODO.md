# Features for Sprint 4
## Queues Integration:

1. Create an additional application for running the OCR service
2. Tesseract for Ghostscript (or the like) integraded and working, show function with unit-tests.
3. Extend REST Server to store PDF document in MinIO
4. Implement the OCR-worker service to
   retrieve messages from the queue (sent by REST-Server on document-upload),
   fetch the original PDF-document from MinIO
   perform the OCR-recognition
   show functionality with unit-tests
5. Extend docker-compose.yml to run the MinIO and OCR-service in a container
6. Make the frontend a little bit fancier :D


## MUST-HAVE Check Criteria:

- No build error (docker compose build)
- docker compose up sucessfully starts all containers
- POST http://localhost/... some PDF-Document
  - will lead to store the PDF on MinIO
  - will lead to a log-output on the PaperlessService.OcrWorker (stating OCR result)

# !! Deadline 22.10.2025 23:59 !!