from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel

from app.rag.answer import answer_question
from app.routers.auth import get_current_user

router = APIRouter(prefix="/chat", tags=["chat"])


class ChatRequest(BaseModel):
    question: str


@router.post("/ask")
def ask(request: ChatRequest, current_user: dict = Depends(get_current_user)):
    if not request.question.strip():
        raise HTTPException(status_code=400, detail="Question cannot be empty.")
    if len(request.question) > 2000:
        raise HTTPException(status_code=400, detail="Question is too long (max 2,000 characters).")

    return answer_question(request.question)