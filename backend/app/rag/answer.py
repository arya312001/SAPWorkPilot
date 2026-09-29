import logging

from app.rag.llm import generate_answer
from app.rag.vector_store import search

logger = logging.getLogger("sapworkpilot")
MIN_SCORE = 0.10


def answer_question(question: str, top_k: int = 3) -> dict:
    results = search(question, top_k=top_k)
    scores = [r["score"] for r in results]
    logger.info("CHAT question=%r  top_scores=%s", question, [round(s, 4) for s in scores])
    strong_results = [r for r in results if r["score"] >= MIN_SCORE]

    if not strong_results:
        return {
            "answer": "I could not find anything relevant in the uploaded documents "
                      "for that question. Try uploading a document that covers this topic.",
            "sources": [],
        }

    chunks = [r["text"] for r in strong_results]
    answer = generate_answer(question, chunks)
    sources = sorted({r["source"] for r in strong_results})

    return {"answer": answer, "sources": sources}