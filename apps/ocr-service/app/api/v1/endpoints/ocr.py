"""Private recognition and DOCX rendering endpoints used by the API worker."""

import logging
import tempfile
from pathlib import Path
from typing import Literal

from fastapi import APIRouter, Depends, HTTPException, Request, Response

from app.core.config import Settings, get_settings
from app.core.exceptions import OcrProcessingError
from app.core.security import require_internal_token
from app.schemas.ocr import DocxRequestDto, LineDto, RecognizeResponseDto
from app.services.docx_export import render_docx
from app.services.ocr_engine import get_engine, ocr_lock

router = APIRouter()
logger = logging.getLogger(__name__)
DOCX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"


@router.post("/internal/recognize", response_model=RecognizeResponseDto)
async def recognize(
    request: Request,
    language: Literal["en", "fr"] = "en",
    _: None = Depends(require_internal_token),
    settings: Settings = Depends(get_settings),
) -> RecognizeResponseDto:
    """Recognize one original image and return text blocks in reading order."""
    content_type = request.headers.get("content-type")
    if content_type not in {"image/jpeg", "image/png"}:
        raise HTTPException(status_code=415, detail="Only JPEG and PNG are supported")
    image = await request.body()
    if not image or len(image) > settings.max_image_bytes:
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
        raise OcrProcessingError() from error
    return RecognizeResponseDto(lines=[LineDto(**vars(line)) for line in lines])


@router.post("/internal/docx")
def docx(request: DocxRequestDto, _: None = Depends(require_internal_token)) -> Response:
    """Generate DOCX from the supplied ordered OCR lines."""
    content = render_docx([[line.text for line in page.lines] for page in request.pages])
    return Response(content=content, media_type=DOCX_CONTENT_TYPE)
