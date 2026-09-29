from datetime import date

import httpx

from app import fake_data
from app.config import settings

# Jira Cloud needs a bounded query. Adjust this JQL later for your project.
DEFAULT_JQL = "created >= -365d ORDER BY priority DESC"


def is_configured() -> bool:
    return bool(
        settings.jira_base_url and settings.jira_email and settings.jira_api_token
    )


def _map_status(fields: dict) -> str:
    status = fields.get("status") or {}
    name = (status.get("name") or "").lower()
    category = ((status.get("statusCategory") or {}).get("key") or "").lower()
    if "block" in name:
        return "BLOCKED"
    if category == "done":
        return "DONE"
    if category == "indeterminate":
        return "IN_PROGRESS"
    return "TO_DO"


def _is_overdue(fields: dict, status: str) -> bool:
    due = fields.get("duedate")
    if not due or status == "DONE":
        return False
    try:
        return date.fromisoformat(due) < date.today()
    except ValueError:
        return False


def _to_ticket(issue: dict) -> dict:
    fields = issue.get("fields", {})
    status = _map_status(fields)
    assignee = fields.get("assignee") or {}
    priority = fields.get("priority") or {}
    return {
        "key": issue.get("key", ""),
        "title": fields.get("summary", ""),
        "assignee": assignee.get("displayName", "Unassigned"),
        "status": status,
        "priority": priority.get("name", "Medium"),
        "is_overdue": _is_overdue(fields, status),
    }


def fetch_tickets(jql: str = DEFAULT_JQL) -> list[dict]:
    # Not configured: use the sample tickets
    if not is_configured():
        return fake_data.ALL_TICKETS

    url = settings.jira_base_url.rstrip("/") + "/rest/api/3/search/jql"
    params = {
        "jql": jql,
        "fields": "summary,status,priority,assignee,duedate",
        "maxResults": 100,
    }
    try:
        with httpx.Client(timeout=15.0) as client:
            response = client.get(
                url,
                params=params,
                auth=(settings.jira_email, settings.jira_api_token),
                headers={"Accept": "application/json"},
            )
            response.raise_for_status()
        return [_to_ticket(issue) for issue in response.json().get("issues", [])]
    except httpx.HTTPError:
        # Jira unreachable or rejected the request: fall back to sample tickets
        return fake_data.ALL_TICKETS