from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "SAPWorkPilot API"
    app_version: str = "0.1.0"
    debug: bool = True

    # Security (change the secret key before real use)
    secret_key: str = "007b5ac4d49cd501337248f2c3323d15c7e28b2d3914d3281522291e80eca038"
    access_token_expire_minutes: int = 60

    # Database (used in a later step)
    database_url: str = "postgresql://postgres:postgres@localhost:5432/sapworkpilot"

    # Jira integration (used in a later step)
    jira_base_url: str = ""
    jira_email: str = ""
    jira_api_token: str = ""

    # LLM and RAG (used in a later step)
    llm_api_key: str = ""

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()