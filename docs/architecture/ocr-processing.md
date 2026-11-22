# OCR processing design

The Python service is private to the API and separates recognition, structured text results, and DOCX export. The provisional MVP engine is PaddleOCR. Its English and French handwriting benchmark records character error rate, word error rate and processing time. A PaddleOCR detection plus TrOCR recognition pipeline is justified only if measured results warrant it.

The API sends each original image to `POST /internal/recognize?language=en|fr` with `X-Internal-Token`. The response contains ordered lines, confidence and boxes. It sends the ordered pages to `POST /internal/docx`, which inserts page breaks between source pages. Original images remain in MinIO for reference. Both internal endpoints require `OCR_INTERNAL_TOKEN`.

The service also exposes `/health` for process health. PaddleOCR models load on first recognition request, so the first conversion can take longer.

## First measured baseline (22 November 2026)

The baseline uses PaddleOCR 3.3.0, PaddlePaddle 3.2.0, `PP-OCRv5_mobile_det`, `en_PP-OCRv5_mobile_rec` for English and `latin_PP-OCRv5_mobile_rec` for French. Both recognition models are listed in [PaddleOCR's multilingual model documentation](https://www.paddleocr.ai/latest/en/version3.x/algorithm/PP-OCRv5/PP-OCRv5_multi_languages.html). Orientation and unwarping classifiers were disabled. No additional image preprocessing or text correction was applied. Text regions were sorted left to right because every benchmark image contains one cropped line.

The sample set contains the first three held-out test lines from [IAM-line](https://huggingface.co/datasets/Ay534v/IAM-line) and [RIMES-2011-line](https://huggingface.co/datasets/Teklia/RIMES-2011-line). The script downloads the images and their published labels to ignored local `benchmark-data/`; no dataset images are committed. A sample of each language was visually checked. These are real handwriting, but **cropped lines rather than photographed lecture pages**.

CER and WER are total edit errors divided by total reference characters and words, respectively, within each language. Case and repeated whitespace are normalized; accents and punctuation remain significant. The table reports micro-averaged errors and mean warm inference time per line on an Intel Core i5-11500H CPU, with `OMP_NUM_THREADS=1`. Model download and initialization are excluded from inference time.

| Language | Lines | CER | WER | Mean inference |
| --- | ---: | ---: | ---: | ---: |
| English | 3 | 11.9% | 62.7% | 0.182 s |
| French | 3 | 28.8% | 66.7% | 0.140 s |

The English baseline recognized most characters on these lines but made word-level errors, including punctuation spacing and spelling changes. One English line required explicit left-to-right sorting of two detected regions. French performance varied sharply: one line had 54.5% CER and 100% WER. The high WER is partly sensitive to spaces around punctuation in dataset labels, but the character errors show that recognition is also materially wrong. A photographed English note also produced recognizable headings and many misspelled words. On 22 November 2026, the project owner accepted this PaddleOCR pipeline as the **provisional MVP engine** despite these measured limitations. Keep original images available and describe conversion as best effort.

Reproduce from `apps/ocr-service` with:

```sh
uv pip install --python .venv/bin/python -r requirements-ocr.txt
python3 -m scripts.fetch_public_samples
OMP_NUM_THREADS=1 OPENBLAS_NUM_THREADS=1 .venv/bin/python -m scripts.benchmark benchmark-data/manifest.json --output benchmark-data/results.json
```

The first run downloads model weights and is much slower. The local report includes each prediction, its reference, CER, WER, and inference time. This tiny line-level set cannot estimate performance on camera blur, shadows, page layout, diagrams or a particular student's handwriting. Later quality work should benchmark transcribed note photos and compare an English handwriting model such as [Microsoft TrOCR](https://github.com/microsoft/unilm/blob/master/trocr/README.md). TrOCR's IAM-trained models should not be assumed to solve French handwriting; evaluate a French-trained HTR model separately.
