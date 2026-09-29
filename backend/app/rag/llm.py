from anthropic import Anthropic

from app.config import settings

_client: Anthropic | None = None


def is_llm_configured() -> bool:
    return bool(settings.llm_api_key)


def _get_client() -> Anthropic:
    global _client
    if _client is None:
        _client = Anthropic(api_key=settings.llm_api_key)
    return _client


def generate_answer(question: str, context_chunks: list[str]) -> str:
    """Uses Claude to write a real answer grounded in the retrieved chunks.
    Falls back to raw chunks if no API key is configured.
    """
    if not is_llm_configured():
        return "\n\n".join(context_chunks)

    context = "\n\n---\n\n".join(context_chunks)
    prompt = (
        "Answer the question using only the context below. "
        "If the context doesn't contain the answer, say so.\n\n"
        f"Context:\n{context}\n\nQuestion: {question}"
    )

    client = _get_client()
    response = client.messages.create(
        model="claude-haiku-4-5-20251001",
        max_tokens=500,
        messages=[{"role": "user", "content": prompt}],
    )
    return response.content[0].text