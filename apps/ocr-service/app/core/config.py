"""Configuration read from the OCR process environment."""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Internal credential and request limit shared by OCR endpoints."""

    model_config = SettingsConfigDict(env_prefix="OCR_")

    internal_token: str | None = None
    max_image_bytes: int = 10 * 1024 * 1024


def get_settings() -> Settings:
    """Resolve environment settings for the current request."""
    return Settings()
