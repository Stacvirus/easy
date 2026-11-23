"""Verify internal auth, structured OCR responses and valid DOCX output."""

from io import BytesIO
from pathlib import Path

from docx import Document
from fastapi.testclient import TestClient

from app import main
from app.api.v1.endpoints import ocr as ocr_endpoint
from app.services.ocr_engine import RecognizedLine


class FakeEngine:
    """Return a predictable line without loading model weights."""

    def __init__(self) -> None:
        self.path: Path | None = None

    def recognize(self, image: Path) -> list[RecognizedLine]:
        self.path = image
        assert image.read_bytes() == b"test-image"
        return [RecognizedLine("Bonjour", 0.82, (1, 2, 3, 4))]


def test_health_route_remains_available() -> None:
    response = TestClient(main.app).get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_recognition_requires_token_and_returns_structured_result(monkeypatch) -> None:
    monkeypatch.setenv("OCR_INTERNAL_TOKEN", "internal-test-token")
    engine = FakeEngine()
    monkeypatch.setattr(ocr_endpoint, "get_engine", lambda language: engine)
    client = TestClient(main.app)
    assert client.post("/internal/recognize", content=b"test-image",
                       headers={"Content-Type": "image/png"}).status_code == 401

    response = client.post("/internal/recognize?language=fr", content=b"test-image",
                           headers={"Content-Type": "image/png", "X-Internal-Token": "internal-test-token"})
    assert response.status_code == 200
    assert response.json() == {"lines": [{"text": "Bonjour", "confidence": 0.82, "box": [1, 2, 3, 4]}]}
    assert engine.path is not None and not engine.path.exists()


def test_recognition_keeps_validation_and_failure_responses(monkeypatch) -> None:
    monkeypatch.setenv("OCR_INTERNAL_TOKEN", "internal-test-token")
    client = TestClient(main.app)
    headers = {"X-Internal-Token": "internal-test-token"}

    unsupported = client.post(
        "/internal/recognize", content=b"image", headers={**headers, "Content-Type": "text/plain"}
    )
    assert unsupported.status_code == 415
    assert unsupported.json() == {"detail": "Only JPEG and PNG are supported"}

    monkeypatch.setenv("OCR_MAX_IMAGE_BYTES", "4")
    oversized = client.post(
        "/internal/recognize", content=b"image", headers={**headers, "Content-Type": "image/png"}
    )
    assert oversized.status_code == 400
    assert oversized.json() == {"detail": "Invalid image size"}

    monkeypatch.delenv("OCR_MAX_IMAGE_BYTES")

    def fail(_language: str):
        raise RuntimeError("private engine detail")

    monkeypatch.setattr(ocr_endpoint, "get_engine", fail)
    failed = client.post(
        "/internal/recognize", content=b"image", headers={**headers, "Content-Type": "image/png"}
    )
    assert failed.status_code == 503
    assert failed.json() == {"detail": "Recognition failed"}
    assert "private engine detail" not in failed.text


def test_missing_internal_token_configuration_returns_503(monkeypatch) -> None:
    monkeypatch.delenv("OCR_INTERNAL_TOKEN", raising=False)
    client = TestClient(main.app)
    response = client.post("/internal/docx", json={"pages": []})
    assert response.status_code == 503
    assert response.json() == {"detail": "Internal token is not configured"}


def test_docx_keeps_page_order_and_page_breaks(monkeypatch) -> None:
    monkeypatch.setenv("OCR_INTERNAL_TOKEN", "internal-test-token")
    client = TestClient(main.app)
    response = client.post("/internal/docx", headers={"X-Internal-Token": "internal-test-token"}, json={
        "pages": [
            {"pageId": "00000000-0000-0000-0000-000000000002", "position": 1,
             "lines": [{"text": "Second page", "confidence": None, "box": None}]},
            {"pageId": "00000000-0000-0000-0000-000000000001", "position": 2,
             "lines": [{"text": "First page", "confidence": 0.8, "box": [1, 2, 3, 4]}]},
        ]
    })
    assert response.status_code == 200
    assert response.headers["content-type"].startswith(ocr_endpoint.DOCX_CONTENT_TYPE)
    document = Document(BytesIO(response.content))
    assert [paragraph.text for paragraph in document.paragraphs if paragraph.text] == [
        "Second page", "First page"
    ]
    assert b'w:type="page"' in response.content or any(
        '<w:br w:type="page"/>' in paragraph._p.xml for paragraph in document.paragraphs
    )
