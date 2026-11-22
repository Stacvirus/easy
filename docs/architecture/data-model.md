# Initial data model

- **User** owns subjects and documents.
- **Subject** has an owner and name; a document may refer to one subject.
- **Document** has owner, title, logical date and creation time.
- **DocumentPage** has a document, stable ID, explicit position and original image object key.
- **ConversionJob** has owner, document, status, ordered page snapshot, timestamps and failure information.
- **DocumentExport** has document, source job, DOCX object key and creation time.

Schema and constraints will be finalized in the first persistence slice. Expected invariants: owned records cannot cross users; page positions are unique within a document; an export refers to a completed job; object keys are generated server side.
