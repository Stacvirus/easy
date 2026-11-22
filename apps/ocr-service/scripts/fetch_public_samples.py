"""Fetch a tiny labeled IAM/RIMES test subset for an initial OCR baseline."""

import json
from pathlib import Path
from urllib.parse import urlencode
from urllib.request import urlopen


DATASETS = (
    ("en", "Ay534v/IAM-line"),
    ("fr", "Teklia/RIMES-2011-line"),
)
OUTPUT = Path(__file__).resolve().parents[1] / "benchmark-data"


def main() -> None:
    """Download three test lines per language and write their exact labels."""
    OUTPUT.mkdir(exist_ok=True)
    samples = []
    for language, dataset in DATASETS:
        query = urlencode({"dataset": dataset, "config": "default", "split": "test"})
        with urlopen(f"https://datasets-server.huggingface.co/first-rows?{query}", timeout=30) as response:
            rows = json.load(response)["rows"][:3]
        if len(rows) != 3:
            raise RuntimeError(f"Expected three test rows from {dataset}, got {len(rows)}")
        for item in rows:
            name = f"{language}-{item['row_idx']}.jpg"
            with urlopen(item["row"]["image"]["src"], timeout=30) as response:
                (OUTPUT / name).write_bytes(response.read())
            samples.append({
                "id": name.removesuffix(".jpg"),
                "language": language,
                "image": name,
                "reference": item["row"]["text"],
                "dataset": dataset,
                "split": "test",
                "row": item["row_idx"],
            })
    (OUTPUT / "manifest.json").write_text(json.dumps(samples, ensure_ascii=False, indent=2) + "\n")
    print(f"Saved {len(samples)} labeled lines to {OUTPUT}")


if __name__ == "__main__":
    main()
