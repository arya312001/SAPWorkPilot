from app.jira_client import fetch_tickets, is_configured

print(is_configured(), len(fetch_tickets()))