from app.security import hash_password, verify_password

# Temporary fake users. Replaced by the PostgreSQL database in a later step.
_FAKE_USERS = [
    {
        "email": "admin@sapworkpilot.com",
        "name": "Admin User",
        "role": "admin",
        "password_hash": hash_password("Admin@123"),
    },
    {
        "email": "pm@sapworkpilot.com",
        "name": "Project Manager",
        "role": "project_manager",
        "password_hash": hash_password("Manager@123"),
    },
    {
        "email": "member@sapworkpilot.com",
        "name": "Team Member",
        "role": "member",
        "password_hash": hash_password("Member@123"),
    },
]


def get_user_by_email(email: str) -> dict | None:
    for user in _FAKE_USERS:
        if user["email"].lower() == email.lower():
            return user
    return None


def authenticate_user(email: str, password: str) -> dict | None:
    user = get_user_by_email(email)
    if user is None:
        return None
    if not verify_password(password, user["password_hash"]):
        return None
    return user