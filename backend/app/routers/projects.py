from fastapi import APIRouter, Depends

from app import fake_data, jira_client
from app.routers.auth import get_current_user

router = APIRouter(prefix="/projects", tags=["projects"])


def _grouped_by_area(tickets: list[dict]) -> dict[str, list[dict]]:
    """Groups live Jira tickets by the same areas used in the sample heatmap."""
    by_key = {}
    for area in fake_data.HEATMAP:
        keys_in_area = {t["key"] for t in area["tickets"]}
        by_key[area["name"]] = keys_in_area

    grouped: dict[str, list[dict]] = {area["name"]: [] for area in fake_data.HEATMAP}
    for ticket in tickets:
        placed = False
        for area_name, keys in by_key.items():
            if ticket["key"] in keys:
                grouped[area_name].append(ticket)
                placed = True
                break
        if not placed:
            grouped.setdefault("Other", []).append(ticket)
    return grouped


def _live_heatmap() -> list[dict]:
    tickets = jira_client.fetch_tickets()
    grouped = _grouped_by_area(tickets)

    result = []
    for area in fake_data.HEATMAP:
        area_tickets = grouped.get(area["name"], [])
        blocked_or_overdue = any(t["status"] == "BLOCKED" or t["is_overdue"] for t in area_tickets)
        in_progress_issue = any(t["status"] != "DONE" for t in area_tickets)
        level = "HIGH" if blocked_or_overdue else ("MEDIUM" if in_progress_issue else "LOW")
        result.append({
            "name": area["name"],
            "level": level if area_tickets else area["level"],
            "explanation": area["explanation"] if not area_tickets else
                f"{len(area_tickets)} live ticket(s) tracked for this area.",
            "tickets": area_tickets if area_tickets else area["tickets"],
        })
    return result


@router.get("/dashboard")
def dashboard(current_user: dict = Depends(get_current_user)):
    tickets = jira_client.fetch_tickets()
    high_risk = sum(1 for area in _live_heatmap() if area["level"] == "HIGH")
    blocked = sum(1 for t in tickets if t["status"] == "BLOCKED")
    overdue = sum(1 for t in tickets if t["is_overdue"])
    return {
        "projects": fake_data.PROJECTS,
        "milestones": fake_data.MILESTONES,
        "ai_summary": fake_data.AI_SUMMARY,
        "high_risk_count": high_risk,
        "blocked_count": blocked,
        "overdue_count": overdue,
    }


@router.get("/heatmap")
def heatmap(current_user: dict = Depends(get_current_user)):
    return _live_heatmap()


@router.get("/risks")
def risks(current_user: dict = Depends(get_current_user)):
    return fake_data.RISKS