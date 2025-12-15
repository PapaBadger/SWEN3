# Sprint 7 TODO

1. Show the functionality of use case „document upload“ with an integration-test `done`
2. Create an additional application for running the scheduled service that reads daily XML files from
   an input folder to process access
   logs from external systems via a batch process.
   Consider how such a process is best integrated into the existing architecture.
   Define an appropriate XML format for the access statistics.
   Extend the PostgreSQL database so that the daily access count is stored per document.
   Implement the batch service to be scheduled (e.g. daily at 01:00 AM) to retrieve and process
   the XML files. Ensure the input folder and filename patterns are configurable.
   Provide a sample XML file to demonstrate the functionality.
   Ensure that processed XML files are appropriately archived or removed to prevent redundant
   processing.
3. Project finalization
4. Prepare for the final code-review

## MUST-HAVE Check Criteria:

- No build error 
- docker compose up starts all required containers `done`
- Provided integration-test (write an HOWTO in README.md) will be executed and should run
  successfully to the end.
- The batch process must successfully read the sample XML file, process the data, and persist it
  in the database as demonstrated by relevant database queries.



# Unit tests schreiben!!! `done`

# TODO EXTRA
.env file einfügen `done`
@Profile vielleciht wieder ändern in ocrworker und genaiworker, ist nämlich unnötig
UNIT TESTS!!!! `done 92% coverage`
CATEGORIES IM FRONTEND AUSWÄHLEN (DROPDOWN MENU)