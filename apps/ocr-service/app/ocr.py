"""PaddleOCR baseline for handwritten text; models are loaded on first use."""

from dataclasses import dataclass
from pathlib import Path
from typing import Protocol


@dataclass(frozen=True)
class RecognizedLine:
    """One recognized text region in the order returned by the OCR pipeline."""

    text: str
    confidence: float | None
    box: tuple[int, int, int, int] | None


class OcrEngine(Protocol):
    """Recognize text in a local image; inference errors propagate to the caller."""

    def recognize(self, image: Path) -> list[RecognizedLine]: ...


class PaddleOcrEngine:
    """Run PP-OCRv5 detection and language-specific recognition on the CPU."""

    def __init__(self, language: str):
        if language not in {"en", "fr"}:
            raise ValueError(f"Unsupported language: {language}")
        from paddleocr import PaddleOCR

        self._pipeline = PaddleOCR(
            device="cpu",
            text_detection_model_name="PP-OCRv5_mobile_det",
            text_recognition_model_name=(
                "en_PP-OCRv5_mobile_rec" if language == "en" else "latin_PP-OCRv5_mobile_rec"
            ),
            use_doc_orientation_classify=False,
            use_doc_unwarping=False,
            use_textline_orientation=False,
        )

    def recognize(self, image: Path) -> list[RecognizedLine]:
        """Return text, model confidence and boxes without modifying the source image."""
        lines: list[RecognizedLine] = []
        for page in self._pipeline.predict(str(image)):
            result = page.json["res"]
            for text, score, box in zip(
                result["rec_texts"], result["rec_scores"], result["rec_boxes"], strict=True
            ):
                if text.strip():
                    lines.append(
                        RecognizedLine(
                            text=text,
                            confidence=float(score),
                            box=tuple(int(value) for value in box),
                        )
                    )
        lines.sort(key=lambda line: (line.box[1], line.box[0]) if line.box else (0, 0))
        return lines
