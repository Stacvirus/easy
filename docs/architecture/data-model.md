# Data model

- **AppUser** has a UUID, normalized email, BCrypt password hash and creation time. The email is unique.
- **Subject** has a UUID, owner, name and creation time. Names are unique per owner without regard to case.
- **Document** has a UUID, owner, title, required logical date, optional subject and creation time. A composite foreign key ensures the subject has the same owner.
- **DocumentPage** has a document, stable ID, one-based position, content type, size, upload time and server-generated original image object key. Migration V2 enforces unique positions per document; the constraint is deferred so page swaps can be saved atomically.
- **ObjectDeletion** is a durable queue entry for deleting a stored image after its page or document metadata is removed.
- **ConversionJob** has owner, document, status, ordered page snapshot, timestamps and failure information.
- **DocumentExport** has document, source job, DOCX object key and creation time.

Migration V1 implements users, subjects and documents. Migration V2 adds pages and object cleanup. Conversion jobs and exports remain for later migrations. Their expected invariant is that an export refers to a completed job.
