"""Process health endpoint used by Compose."""

from fastapi import APIRouter

router = APIRouter()


@router.get("/health")
def health() -> dict[str, str]:
    """Report whether the OCR process can serve requests."""
    return {"status": "ok"}
