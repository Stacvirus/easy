"""Initialize the private OCR HTTP service."""

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.api.v1.router import router
from app.core.exceptions import OcrProcessingError

app = FastAPI(title="Document OCR service")
app.include_router(router)


@app.exception_handler(OcrProcessingError)
async def handle_ocr_processing_error(
    _request: Request, _error: OcrProcessingError
) -> JSONResponse:
    """Return a stable error without leaking OCR engine details."""
    return JSONResponse(status_code=503, content={"detail": "Recognition failed"})
