from sklearn.feature_extraction.text import TfidfVectorizer

_vectorizer: TfidfVectorizer | None = None
_fitted_texts: list[str] = []


def _get_vectorizer() -> TfidfVectorizer:
    global _vectorizer
    if _vectorizer is None:
        _vectorizer = TfidfVectorizer(stop_words="english")
    return _vectorizer


def refit_from_existing(texts: list[str]) -> None:
    """Rebuilds the vectorizer from chunks already saved on disk.
    Called on server startup so restarts don't lose the fit.
    """
    global _fitted_texts
    if not texts:
        return
    _fitted_texts = list(set(texts))
    _get_vectorizer().fit(_fitted_texts)


def embed_texts(texts: list[str]) -> list[list[float]]:
    if not texts:
        return []

    global _fitted_texts
    _fitted_texts = list(set(_fitted_texts) | set(texts))

    vectorizer = _get_vectorizer()
    vectorizer.fit(_fitted_texts)
    matrix = vectorizer.transform(texts)
    return matrix.toarray().tolist()


def embed_query(text: str) -> list[float]:
    vectorizer = _get_vectorizer()
    matrix = vectorizer.transform([text])
    return matrix.toarray()[0].tolist()