# Data model

- **AppUser** has a UUID, normalized email, BCrypt password hash and creation time. The email is unique.
- **Subject** has a UUID, owner, name and creation time. Names are unique per owner without regard to case.
- **Document** has a UUID, owner, title, required logical date, optional subject and creation time. A composite foreign key ensures the subject has the same owner.
- **DocumentPage** has a document, stable ID, explicit position and original image object key.
- **ConversionJob** has owner, document, status, ordered page snapshot, timestamps and failure information.
- **DocumentExport** has document, source job, DOCX object key and creation time.

Migration V1 implements the first three records. Page positions, exports and object references will be added in later migrations. Their expected invariants are: page positions are unique within a document; an export refers to a completed job; object keys are generated server side.
