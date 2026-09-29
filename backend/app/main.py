import logging

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.config import settings
from app.jira_client import is_configured as jira_is_configured
from app.rag.llm import is_llm_configured
from app.rag.embeddings import refit_from_existing
from app.rag.vector_store import get_all_texts
from app.routers import audit, auth, chat, documents, jira, meetings, projects

logger = logging.getLogger("sapworkpilot")

app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    debug=settings.debug,
)

app.include_router(auth.router)
app.include_router(projects.router)
app.include_router(jira.router)
app.include_router(documents.router)
app.include_router(chat.router)
app.include_router(meetings.router)
app.include_router(audit.router)


@app.on_event("startup")
def refit_vectorizer_on_startup():
    refit_from_existing(get_all_texts())


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception):
    logger.exception("Unhandled error on %s %s", request.method, request.url.path)
    return JSONResponse(
        status_code=500,
        content={"detail": "Something went wrong on the server. Please try again."},
    )


@app.get("/health")
def health():
    return {
        "status": "ok",
        "service": settings.app_name,
        "version": settings.app_version,
        "jira_configured": jira_is_configured(),
        "llm_configured": is_llm_configured(),
    }