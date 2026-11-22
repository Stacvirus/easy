"""Private recognition and DOCX rendering endpoints used by the API worker."""

import logging
import os
import secrets
import tempfile
from functools import lru_cache
from pathlib import Path
from threading import Lock
from typing import Literal
from uuid import UUID

from fastapi import Depends, FastAPI, Header, HTTPException, Request, Response
from pydantic import BaseModel

from app.docx_export import render_docx
from app.ocr import OcrEngine, PaddleOcrEngine

app = FastAPI(title="Document OCR service")
logger = logging.getLogger(__name__)
ocr_lock = Lock()
DOCX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"


class LineDto(BaseModel):
    """A recognized line and any score or box supplied by the engine."""

    text: str
    confidence: float | None = None
    box: tuple[int, int, int, int] | None = None


class RecognizeResponseDto(BaseModel):
    """Structured text extracted from one source image."""

    lines: list[LineDto]


class RecognizedPageDto(BaseModel):
    """One page in the order captured when the conversion job was submitted."""

    pageId: UUID
    position: int
    lines: list[LineDto]


class DocxRequestDto(BaseModel):
    """Ordered pages to render as an editable document."""

    pages: list[RecognizedPageDto]


def require_internal_token(x_internal_token: str | None = Header(default=None)) -> None:
    """Reject calls without the shared internal service token."""
    expected = os.environ.get("OCR_INTERNAL_TOKEN")
    if not expected:
        raise HTTPException(status_code=503, detail="Internal token is not configured")
    if not x_internal_token or not secrets.compare_digest(x_internal_token, expected):
        raise HTTPException(status_code=401, detail="Unauthorized")


@lru_cache(maxsize=2)
def get_engine(language: str) -> OcrEngine:
    """Load each language's model once per OCR process."""
    return PaddleOcrEngine(language)


@app.get("/health")
def health() -> dict[str, str]:
    """Report whether the OCR process can serve requests."""
    return {"status": "ok"}


@app.post("/internal/recognize", response_model=RecognizeResponseDto)
async def recognize(
    request: Request,
    language: Literal["en", "fr"] = "en",
    _: None = Depends(require_internal_token),
) -> RecognizeResponseDto:
    """Recognize one original image and return text blocks in reading order."""
    content_type = request.headers.get("content-type")
    if content_type not in {"image/jpeg", "image/png"}:
        raise HTTPException(status_code=415, detail="Only JPEG and PNG are supported")
    image = await request.body()
    if not image or len(image) > 10 * 1024 * 1024:
        raise HTTPException(status_code=400, detail="Invalid image size")
    suffix = ".png" if content_type == "image/png" else ".jpg"
    try:
        with tempfile.TemporaryDirectory(prefix="document-ocr-") as directory:
            image_path = Path(directory) / ("source" + suffix)
            image_path.write_bytes(image)
            with ocr_lock:
                lines = get_engine(language).recognize(image_path)
    except Exception as error:
        logger.exception("Recognition failed")
        raise HTTPException(status_code=503, detail="Recognition failed") from error
    return RecognizeResponseDto(lines=[LineDto(**vars(line)) for line in lines])


@app.post("/internal/docx")
def docx(request: DocxRequestDto, _: None = Depends(require_internal_token)) -> Response:
    """Generate DOCX from the supplied ordered OCR lines."""
    content = render_docx([[line.text for line in page.lines] for page in request.pages])
    return Response(content=content, media_type=DOCX_CONTENT_TYPE)
