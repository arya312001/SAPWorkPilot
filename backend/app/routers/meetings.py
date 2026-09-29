import re

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel

from app.rag.llm import generate_answer, is_llm_configured
from app.routers.auth import get_current_user

router = APIRouter(prefix="/meetings", tags=["meetings"])


class MeetingNotesRequest(BaseModel):
    title: str = "Meeting Summary"
    notes: str

    @property
    def is_valid(self) -> bool:
        return 0 < len(self.notes) <= 20000


def _split_sentences(text: str) -> list[str]:
    text = text.replace("\n", " ")
    sentences = re.split(r"(?<=[.!?])\s+", text.strip())
    return [s.strip() for s in sentences if len(s.strip()) > 15]


def _pick_summary_sentences(sentences: list[str], max_sentences: int = 3) -> list[str]:
    if len(sentences) <= max_sentences:
        return sentences
    word_lists = [set(re.findall(r"[a-zA-Z]{4,}", s.lower())) for s in sentences]
    scores = []
    for i, words in enumerate(word_lists):
        overlap = sum(len(words & other) for j, other in enumerate(word_lists) if i != j)
        scores.append((overlap, i))
    scores.sort(reverse=True)
    top_indices = sorted(i for _, i in scores[:max_sentences])
    return [sentences[i] for i in top_indices]


def _find_lines(sentences: list[str], keywords: list[str]) -> list[str]:
    found = [s for s in sentences if any(k in s.lower() for k in keywords)]
    return found[:5]


def _rule_based_summary(notes: str) -> dict:
    sentences = _split_sentences(notes)
    if not sentences:
        return {
            "summary": "No readable content found in the notes.",
            "decisions": [],
            "action_items": [],
        }
    summary_sentences = _pick_summary_sentences(sentences)
    decisions = _find_lines(sentences, ["decided", "agreed", "approved", "will proceed", "decision"])
    action_items = _find_lines(sentences, ["will", "action", "assign", "by friday", "by monday", "todo", "to do"])
    return {
        "summary": " ".join(summary_sentences),
        "decisions": decisions or ["No explicit decisions detected."],
        "action_items": action_items or ["No explicit action items detected."],
    }


def _llm_summary(notes: str) -> dict:
    prompt = (
        "Summarize these meeting notes. Reply in exactly this format, "
        "with no extra text:\n\n"
        "SUMMARY: <2-3 sentence summary>\n"
        "DECISIONS: <one decision per line, prefixed with '- '>\n"
        "ACTIONS: <one action item per line, prefixed with '- '>\n\n"
        f"Notes:\n{notes}"
    )
    raw = generate_answer(prompt, [])

    summary = ""
    decisions: list[str] = []
    action_items: list[str] = []
    section = None

    for line in raw.splitlines():
        stripped = line.strip()
        if stripped.startswith("SUMMARY:"):
            summary = stripped.replace("SUMMARY:", "").strip()
            section = "summary"
        elif stripped.startswith("DECISIONS:"):
            section = "decisions"
        elif stripped.startswith("ACTIONS:"):
            section = "actions"
        elif stripped.startswith("- "):
            if section == "decisions":
                decisions.append(stripped[2:].strip())
            elif section == "actions":
                action_items.append(stripped[2:].strip())
        elif section == "summary" and stripped:
            summary = (summary + " " + stripped).strip()

    return {
        "summary": summary or raw,
        "decisions": decisions or ["No explicit decisions detected."],
        "action_items": action_items or ["No explicit action items detected."],
    }


@router.post("/summarize")
def summarize(request: MeetingNotesRequest, current_user: dict = Depends(get_current_user)):
    if not request.notes.strip():
        raise HTTPException(status_code=400, detail="Notes cannot be empty.")
    if len(request.notes) > 20000:
        raise HTTPException(status_code=400, detail="Notes are too long (max 20,000 characters).")

    result = _llm_summary(request.notes) if is_llm_configured() else _rule_based_summary(request.notes)
    return {"title": request.title, **result}