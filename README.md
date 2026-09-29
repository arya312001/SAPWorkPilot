# SAPWorkPilot

AI-powered SAP project intelligence — Android app + Python FastAPI backend.

## Structure

```
├── android/    Jetpack Compose Android app (Kotlin, MVVM, Hilt, Retrofit, Room)
└── backend/    FastAPI backend with RAG pipeline (TF-IDF, pypdf, sklearn)
```

## Android App

Built with:
- Kotlin + Jetpack Compose (Material 3)
- MVVM + Clean Architecture + Hilt DI
- Retrofit / OkHttp for networking
- Room for local chat history
- DataStore for settings

### Run

Open `android/` in Android Studio, connect a device, and click Run.

## Backend

Built with:
- Python 3.11+, FastAPI, uvicorn
- RAG: pypdf for PDF extraction, sklearn TF-IDF for semantic search
- Optional: Anthropic Claude for LLM-generated answers (set `LLM_API_KEY` in `.env`)

### Setup

```bash
cd backend
python -m venv venv
venv\Scripts\activate        # Windows
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### Default users

| Email | Password | Role |
|---|---|---|
| admin@sapworkpilot.com | Admin@123 | admin |
| pm@sapworkpilot.com | Manager@123 | project_manager |
| member@sapworkpilot.com | Member@123 | member |

### Environment variables (optional `.env`)

```
LLM_API_KEY=your_anthropic_api_key
```
