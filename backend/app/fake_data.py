PROJECTS = [
    {"id": "p1", "name": "S/4HANA Finance Migration",
     "description": "Migrating legacy ERP finance to S/4HANA", "status": "HIGH"},
    {"id": "p2", "name": "SAP Integration Modernization",
     "description": "Moving interfaces to SAP BTP Integration Suite", "status": "MEDIUM"},
    {"id": "p3", "name": "Warehouse Rollout",
     "description": "Extended Warehouse Management deployment", "status": "LOW"},
]

_FINANCE_TICKETS = [
    {"key": "FIN-101", "title": "GL account mapping incomplete", "assignee": "Rahul",
     "status": "BLOCKED", "priority": "Critical", "is_overdue": True},
    {"key": "FIN-108", "title": "Currency conversion rules review", "assignee": "Priya",
     "status": "IN_PROGRESS", "priority": "High", "is_overdue": False},
]

_DATA_TICKETS = [
    {"key": "DM-45", "title": "Legacy vendor data cleansing", "assignee": "Amit",
     "status": "BLOCKED", "priority": "Critical", "is_overdue": True},
    {"key": "DM-52", "title": "Migration cockpit dry run", "assignee": "Sneha",
     "status": "IN_PROGRESS", "priority": "High", "is_overdue": False},
]

_INTEGRATION_TICKETS = [
    {"key": "INT-23", "title": "Bank interface testing", "assignee": "Karan",
     "status": "IN_PROGRESS", "priority": "Medium", "is_overdue": False},
]

_TESTING_TICKETS = [
    {"key": "QA-77", "title": "UAT scripts for finance close", "assignee": "Neha",
     "status": "TO_DO", "priority": "Medium", "is_overdue": False},
]

ALL_TICKETS = _FINANCE_TICKETS + _DATA_TICKETS + _INTEGRATION_TICKETS + _TESTING_TICKETS

HEATMAP = [
    {"name": "Finance", "level": "HIGH",
     "explanation": "GL account mapping is blocked and overdue, which delays the finance close scope.",
     "tickets": _FINANCE_TICKETS},
    {"name": "Data Migration", "level": "HIGH",
     "explanation": "Vendor data cleansing is blocked and the dry run depends on it.",
     "tickets": _DATA_TICKETS},
    {"name": "Integration", "level": "MEDIUM",
     "explanation": "Bank interface testing is progressing but has a tight deadline.",
     "tickets": _INTEGRATION_TICKETS},
    {"name": "Testing", "level": "MEDIUM",
     "explanation": "UAT scripts have not started and depend on migration completion.",
     "tickets": _TESTING_TICKETS},
    {"name": "Deployment", "level": "LOW",
     "explanation": "Cutover plan is drafted and on schedule.", "tickets": []},
    {"name": "Security", "level": "LOW",
     "explanation": "Role design is complete and under review.", "tickets": []},
]

RISKS = [
    {"id": "r1", "title": "GL mapping delay", "area": "Finance", "level": "HIGH",
     "reason": "FIN-101 is blocked and overdue, and it affects the finance close.",
     "source": "Jira FIN-101"},
    {"id": "r2", "title": "Vendor data quality", "area": "Data Migration", "level": "HIGH",
     "reason": "DM-45 is blocked, so the migration dry run may slip.",
     "source": "Jira DM-45"},
    {"id": "r3", "title": "UAT start dependency", "area": "Testing", "level": "MEDIUM",
     "reason": "UAT cannot start until the migration dry run is done.",
     "source": "FURY Methodology.pdf"},
    {"id": "r4", "title": "Bank interface deadline", "area": "Integration", "level": "MEDIUM",
     "reason": "Testing window is tight before the cutover date.",
     "source": "Project Plan v3.docx"},
]

MILESTONES = [
    {"title": "Data migration dry run", "date": "15 Oct"},
    {"title": "UAT start", "date": "01 Nov"},
    {"title": "Go-live cutover", "date": "15 Dec"},
]

AI_SUMMARY = (
    "Finance and Data Migration are at high risk because critical Jira tickets "
    "(FIN-101, DM-45) are blocked and overdue. Resolve these first to protect the UAT start date."
)