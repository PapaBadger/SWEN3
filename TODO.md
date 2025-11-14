# Features for Sprint 5
## Queues Integration:

1. Extend docker-compose.yml to include configuration for GenAI service
2. Add a new GenAI-worker service
3. On document upload, after OCR is complete:
   Send the extracted text to a GenAI API (Google Gemini)
   Receive a summary as a response
4. Extend REST Server to store the summary in the database
5. Logging in critical positions is integrated
6. Exceptions and API failures are handled properly

### MAKE FRONTEND FANCIER PLSS

## MUST-HAVE Check Criteria:

- No build error
- docker compose up starts all required containers including PaperlessServices
- POST http://localhost:8080/ … some PDF-Document
- will lead to the summary generated with GenAI
- will lead to summary stored in the database

# Unit tests schreiben!!!


# Rabbit MQ soll mit OCR UND GENAI worker kommunizieren, also mit publisher, damit alles seperat und auf eingenen port läuft