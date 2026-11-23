"""Request and response models shared with the Spring API worker."""

from uuid import UUID

from pydantic import BaseModel


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
