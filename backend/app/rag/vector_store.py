import json
import time
from pathlib import Path

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity as sk_cosine

STORE_PATH = Path("data") / "vector_store.json"


def _load() -> list[dict]:
    if not STORE_PATH.exists():
        return []
    return json.loads(STORE_PATH.read_text(encoding="utf-8"))


def _save(records: list[dict]) -> None:
    STORE_PATH.parent.mkdir(parents=True, exist_ok=True)
    STORE_PATH.write_text(json.dumps(records), encoding="utf-8")


def add_chunks(source: str, chunks: list[str], vectors: list[list[float]]) -> int:
    records = _load()
    uploaded_at = time.time()
    for chunk, vector in zip(chunks, vectors):
        records.append({
            "source": source,
            "text": chunk,
            "vector": vector,
            "uploaded_at": uploaded_at,
        })
    _save(records)
    return len(chunks)


def search(query: str, top_k: int = 4) -> list[dict]:
    """Finds the top_k chunks whose meaning is closest to the query.

    Fits TF-IDF fresh on all stored texts + the query together so every
    vector always shares the same vocabulary — avoids dimension mismatch
    when documents were uploaded across separate server sessions.
    """
    records = _load()
    if not records:
        return []

    texts = [r["text"] for r in records]
    vectorizer = TfidfVectorizer(stop_words="english")
    matrix = vectorizer.fit_transform(texts + [query])

    chunk_matrix = matrix[:-1]
    query_vec = matrix[-1]

    scores = sk_cosine(query_vec, chunk_matrix)[0]
    scored = [{**r, "score": float(s)} for r, s in zip(records, scores)]
    scored.sort(key=lambda r: r["score"], reverse=True)
    return scored[:top_k]


def document_count() -> int:
    sources = {r["source"] for r in _load()}
    return len(sources)


def chunk_count() -> int:
    return len(_load())


def list_documents() -> list[dict]:
    records = _load()
    info: dict[str, dict] = {}
    for r in records:
        source = r["source"]
        if source not in info:
            info[source] = {"chunks": 0, "uploaded_at": r.get("uploaded_at")}
        info[source]["chunks"] += 1
    return [
        {"filename": name, "chunks": data["chunks"], "uploaded_at": data["uploaded_at"]}
        for name, data in info.items()
    ]


def delete_document(filename: str) -> int:
    """Removes all chunks belonging to a given source file. Returns how many were removed."""
    records = _load()
    remaining = [r for r in records if r["source"] != filename]
    removed = len(records) - len(remaining)
    _save(remaining)
    return removed


def get_all_texts() -> list[str]:
    return [r["text"] for r in _load()]