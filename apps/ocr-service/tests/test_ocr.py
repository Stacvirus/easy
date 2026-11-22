"""Verify structured OCR extraction without loading real model weights."""

from pathlib import Path
from types import SimpleNamespace

from app.ocr import PaddleOcrEngine, RecognizedLine


def test_recognition_retains_text_score_and_box() -> None:
    engine = PaddleOcrEngine.__new__(PaddleOcrEngine)
    engine._pipeline = SimpleNamespace(predict=lambda _: [SimpleNamespace(json={"res": {
        "rec_texts": ["Bonjour", " ", "le monde"],
        "rec_scores": [0.8, 0.1, 0.9],
        "rec_boxes": [[1, 2, 3, 4], [5, 6, 7, 8], [9, 10, 11, 12]],
    }})])

    assert engine.recognize(Path("page.jpg")) == [
        RecognizedLine("Bonjour", 0.8, (1, 2, 3, 4)),
        RecognizedLine("le monde", 0.9, (9, 10, 11, 12)),
    ]
