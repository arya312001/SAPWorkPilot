from fastapi import APIRouter, Depends

from app import jira_client
from app.routers.auth import get_current_user

router = APIRouter(prefix="/jira", tags=["jira"])


@router.get("/status")
def status(current_user: dict = Depends(get_current_user)):
    return {"configured": jira_client.is_configured()}


@router.get("/tickets")
def tickets(current_user: dict = Depends(get_current_user)):
    return jira_client.fetch_tickets()


@router.get("/blocked")
def blocked(current_user: dict = Depends(get_current_user)):
    return [t for t in jira_client.fetch_tickets() if t["status"] == "BLOCKED"]


@router.get("/overdue")
def overdue(current_user: dict = Depends(get_current_user)):
    return [t for t in jira_client.fetch_tickets() if t["is_overdue"]]