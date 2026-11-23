"""Check metric behavior independently of model inference."""

from app.services.ocr_engine import RecognizedLine
from scripts.benchmark import edit_distance, line_text, normalize


def test_normalization_keeps_french_accents_and_punctuation() -> None:
    assert normalize("  ÉCOLE  ! \n") == "école !"


def test_edit_distance_counts_substitution_insertion_and_deletion() -> None:
    assert edit_distance(list("cat"), list("cut")) == 1
    assert edit_distance(list("cat"), list("cats")) == 1
    assert edit_distance(list("cats"), list("cat")) == 1
    assert edit_distance("one two".split(), "one too".split()) == 1


def test_single_line_text_follows_horizontal_position() -> None:
    lines = [
        RecognizedLine("second", 0.8, (100, 0, 160, 20)),
        RecognizedLine("first", 0.9, (0, 5, 50, 25)),
    ]
    assert line_text(lines) == "first second"
