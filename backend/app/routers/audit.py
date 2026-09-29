from fastapi import APIRouter, Depends, HTTPException

from app.audit import read_recent
from app.routers.auth import get_current_user

router = APIRouter(prefix="/audit", tags=["audit"])


@router.get("/log")
def get_log(current_user: dict = Depends(get_current_user)):
    if current_user["role"] != "admin":
        raise HTTPException(status_code=403, detail="Only admins can view the audit log.")
    return read_recent()