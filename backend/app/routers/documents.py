import asyncio
import shutil
from pathlib import Path

from fastapi import APIRouter, Depends, HTTPException, UploadFile

from app.audit import log_event
from app.rag.chunking import chunk_text, extract_text
from app.rag.embeddings import embed_texts
from app.rag.vector_store import add_chunks, chunk_count, delete_document, document_count, list_documents
from app.routers.auth import get_current_user

router = APIRouter(prefix="/documents", tags=["documents"])

DOCS_DIR = Path("data") / "documents"
ALLOWED_EXTENSIONS = {".pdf", ".txt", ".md", ".csv", ".docx", ".xlsx"}


def _process_document(saved_path: Path, filename: str) -> tuple[str, list, list, int]:
    """Runs all blocking CPU/IO work in a thread so the event loop stays free."""
    text = extract_text(saved_path)
    if not text.strip():
        raise ValueError("No readable text found in this file.")
    chunks = chunk_text(text)
    vectors = embed_texts(chunks)
    added = add_chunks(filename, chunks, vectors)
    return text, chunks, vectors, added


@router.post("/upload")
async def upload_document(
    file: UploadFile,
    current_user: dict = Depends(get_current_user),
):
    if current_user["role"] not in ("admin", "project_manager"):
        raise HTTPException(
            status_code=403,
            detail="Only admins and project managers can upload documents.",
        )

    suffix = Path(file.filename or "").suffix.lower()
    if suffix not in ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=400,
            detail=f"Unsupported file type. Allowed: {', '.join(sorted(ALLOWED_EXTENSIONS))}",
        )

    DOCS_DIR.mkdir(parents=True, exist_ok=True)
    saved_path = DOCS_DIR / file.filename

    raw = await file.read()
    await asyncio.to_thread(saved_path.write_bytes, raw)

    try:
        _, _, _, added = await asyncio.to_thread(
            _process_document, saved_path, file.filename
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))

    log_event(current_user["email"], "document_upload", file.filename)

    return {
        "filename": file.filename,
        "chunks_added": added,
        "total_documents": document_count(),
        "total_chunks": chunk_count(),
    }


@router.get("/stats")
def stats(current_user: dict = Depends(get_current_user)):
    return {
        "total_documents": document_count(),
        "total_chunks": chunk_count(),
    }


@router.get("/list")
def list_all(current_user: dict = Depends(get_current_user)):
    return list_documents()


@router.delete("/{filename}")
def delete(filename: str, current_user: dict = Depends(get_current_user)):
    if current_user["role"] not in ("admin", "project_manager"):
        raise HTTPException(
            status_code=403,
            detail="Only admins and project managers can delete documents.",
        )

    removed = delete_document(filename)
    if removed == 0:
        raise HTTPException(status_code=404, detail="Document not found")

    saved_path = DOCS_DIR / filename
    if saved_path.exists():
        saved_path.unlink()

    log_event(current_user["email"], "document_delete", filename)

    return {
        "filename": filename,
        "chunks_removed": removed,
        "total_documents": document_count(),
        "total_chunks": chunk_count(),
    }