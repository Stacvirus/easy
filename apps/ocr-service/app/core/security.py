"""Authenticate calls from the Spring API worker."""

import secrets

from fastapi import Depends, Header, HTTPException

from app.core.config import Settings, get_settings


def require_internal_token(
    x_internal_token: str | None = Header(default=None),
    settings: Settings = Depends(get_settings),
) -> None:
    """Reject requests without the configured shared service token."""
    if not settings.internal_token:
        raise HTTPException(status_code=503, detail="Internal token is not configured")
    if not x_internal_token or not secrets.compare_digest(
        x_internal_token, settings.internal_token
    ):
        raise HTTPException(status_code=401, detail="Unauthorized")
