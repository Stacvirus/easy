"""Errors whose public response must hide engine implementation details."""


class OcrProcessingError(Exception):
    """OCR inference failed for an accepted image."""
