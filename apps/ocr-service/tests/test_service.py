"""Verify internal auth, structured OCR responses and valid DOCX output."""

from io import BytesIO
from pathlib import Path

from docx import Document
from fastapi.testclient import TestClient

from app import main
from app.ocr import RecognizedLine


class FakeEngine:
    """Return a predictable line without loading model weights."""

    def __init__(self) -> None:
        self.path: Path | None = None

    def recognize(self, image: Path) -> list[RecognizedLine]:
        self.path = image
        assert image.read_bytes() == b"test-image"
        return [RecognizedLine("Bonjour", 0.82, (1, 2, 3, 4))]


def test_recognition_requires_token_and_returns_structured_result(monkeypatch) -> None:
    monkeypatch.setenv("OCR_INTERNAL_TOKEN", "internal-test-token")
    engine = FakeEngine()
    monkeypatch.setattr(main, "get_engine", lambda language: engine)
    client = TestClient(main.app)
    assert client.post("/internal/recognize", content=b"test-image",
                       headers={"Content-Type": "image/png"}).status_code == 401

    response = client.post("/internal/recognize?language=fr", content=b"test-image",
                           headers={"Content-Type": "image/png", "X-Internal-Token": "internal-test-token"})
    assert response.status_code == 200
    assert response.json() == {"lines": [{"text": "Bonjour", "confidence": 0.82, "box": [1, 2, 3, 4]}]}
    assert engine.path is not None and not engine.path.exists()


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
    assert response.headers["content-type"].startswith(main.DOCX_CONTENT_TYPE)
    document = Document(BytesIO(response.content))
    assert [paragraph.text for paragraph in document.paragraphs if paragraph.text] == [
        "Second page", "First page"
    ]
    assert b'w:type="page"' in response.content or any(
        '<w:br w:type="page"/>' in paragraph._p.xml for paragraph in document.paragraphs
    )
