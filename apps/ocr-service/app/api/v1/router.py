"""Aggregate the existing OCR HTTP routes."""

from fastapi import APIRouter

from app.api.v1.endpoints import health, ocr

router = APIRouter()
router.include_router(health.router)
router.include_router(ocr.router)
