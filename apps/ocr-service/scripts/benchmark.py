"""Measure OCR accuracy and CPU inference time on labeled image lines."""

import argparse
import json
import time
import unicodedata
from pathlib import Path

from app.services.ocr_engine import PaddleOcrEngine, RecognizedLine


def normalize(text: str) -> str:
    """Ignore case and repeated whitespace, retaining accents and punctuation."""
    return " ".join(unicodedata.normalize("NFKC", text).casefold().split())


def edit_distance(reference: list[str], prediction: list[str]) -> int:
    """Count insertions, deletions and substitutions with linear memory."""
    previous = list(range(len(prediction) + 1))
    for row, expected in enumerate(reference, 1):
        current = [row]
        for column, actual in enumerate(prediction, 1):
            current.append(min(
                current[-1] + 1,
                previous[column] + 1,
                previous[column - 1] + (expected != actual),
            ))
        previous = current
    return previous[-1]


def line_text(lines: list[RecognizedLine]) -> str:
    """Restore left-to-right order for these single-line benchmark images."""
    return " ".join(line.text for line in sorted(lines, key=lambda line: line.box[0] if line.box else 0))


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("manifest", type=Path, help="JSON manifest with image, language and reference")
    parser.add_argument("--output", type=Path, help="Write full results as JSON")
    args = parser.parse_args()
    samples = json.loads(args.manifest.read_text())
    engines = {}
    totals = {language: {"chars": 0, "char_errors": 0, "words": 0, "word_errors": 0, "seconds": 0.0, "count": 0}
              for language in {sample["language"] for sample in samples}}
    results = []
    for sample in samples:
        language = sample["language"]
        if language not in engines:
            started = time.perf_counter()
            engines[language] = PaddleOcrEngine(language)
            print(f"Loaded {language} model in {time.perf_counter() - started:.2f}s")
        image = args.manifest.parent / sample["image"]
        started = time.perf_counter()
        recognized = engines[language].recognize(image)
        seconds = time.perf_counter() - started
        reference = normalize(sample["reference"])
        prediction = normalize(line_text(recognized))
        char_errors = edit_distance(list(reference), list(prediction))
        word_errors = edit_distance(reference.split(), prediction.split())
        total = totals[language]
        total["chars"] += len(reference)
        total["char_errors"] += char_errors
        total["words"] += len(reference.split())
        total["word_errors"] += word_errors
        total["seconds"] += seconds
        total["count"] += 1
        results.append({**sample, "prediction": prediction, "seconds": round(seconds, 3),
                        "cer": char_errors / len(reference), "wer": word_errors / len(reference.split())})
        print(f"{sample['id']}: CER={results[-1]['cer']:.3f}, WER={results[-1]['wer']:.3f}, {seconds:.2f}s")
    summary = {language: {"samples": values["count"],
                          "cer": values["char_errors"] / values["chars"],
                          "wer": values["word_errors"] / values["words"],
                          "mean_seconds": values["seconds"] / values["count"]}
               for language, values in totals.items()}
    print(json.dumps(summary, indent=2))
    if args.output:
        args.output.write_text(json.dumps({"summary": summary, "samples": results}, ensure_ascii=False, indent=2) + "\n")


if __name__ == "__main__":
    main()
