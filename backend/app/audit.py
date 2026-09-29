import json
import time
from pathlib import Path

AUDIT_LOG_PATH = Path("data") / "audit_log.jsonl"


def log_event(user_email: str, action: str, detail: str = "") -> None:
    AUDIT_LOG_PATH.parent.mkdir(parents=True, exist_ok=True)
    entry = {
        "timestamp": time.time(),
        "user": user_email,
        "action": action,
        "detail": detail,
    }
    with AUDIT_LOG_PATH.open("a", encoding="utf-8") as f:
        f.write(json.dumps(entry) + "\n")


def read_recent(limit: int = 50) -> list[dict]:
    if not AUDIT_LOG_PATH.exists():
        return []
    lines = AUDIT_LOG_PATH.read_text(encoding="utf-8").strip().splitlines()
    entries = [json.loads(line) for line in lines[-limit:]]
    return list(reversed(entries))