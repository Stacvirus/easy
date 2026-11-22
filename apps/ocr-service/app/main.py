"""HTTP entry point for document processing; OCR routes arrive in the next increment."""

from fastapi import FastAPI

app = FastAPI(title="Document OCR service")


@app.get("/health")
def health() -> dict[str, str]:
    """Report whether the OCR process can serve requests."""
    return {"status": "ok"}
