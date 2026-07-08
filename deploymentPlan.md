# Deployment Plan — AI-Powered Restaurant Recommendation System (Streamlit)

## Architecture Overview

```
┌──────────────────────┐       ┌──────────────────────────────┐
│   Streamlit App      │       │   Spring Boot Backend         │
│   (Python frontend)  │──────►│   (REST API)                  │
│                      │ HTTP  │                               │
│  streamlit.app       │◄──────│  railway.app / render.com     │
│                      │  JSON │                               │
└──────────────────────┘       └──────────────────────────────┘
```

- **Streamlit** serves as the UI — forms, result cards, explanations
- **Spring Boot** backend runs separately, exposing the REST API

---

## Prerequisites

- Python 3.11+
- Java 21 + Maven (for backend)
- [Streamlit Community Cloud](https://streamlit.io/cloud) account (free tier: 1 app)
- A hosting service for the backend: [Railway](https://railway.app/) (free tier until Nov 2025), [Render](https://render.com/), or [Fly.io](https://fly.io/)
- `LLM_API_KEY` from Groq (or OpenAI-compatible provider)
- [Optional] A Hugging Face dataset access token if needed

---

## Step 1 — Spring Boot Backend Deployment

### Build the JAR

```bash
# From project root
./mvnw clean package -DskipTests -Pprod
```

This produces `target/recommender-0.0.1-SNAPSHOT.jar`.

### Deploy on Railway (recommended)

1. Create a `Dockerfile` at project root (already exists in architecture plan):

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/recommender-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

2. Push to GitHub and connect repo to Railway.
3. Set environment variables in Railway dashboard:

| Variable | Value |
|----------|-------|
| `LLM_API_KEY` | `gsk_...` (your Groq API key) |
| `LLM_BASE_URL` | `https://api.groq.com/openai` |
| `SERVER_PORT` | `8080` |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `JAVA_OPTS` | `-Xmx512m -Xms256m` |

4. Railway auto-detects the Dockerfile and deploys.
5. Note the backend URL: `https://your-app.up.railway.app`

### Alternative: Deploy on Render

1. Connect GitHub repo → **Web Service**
2. Build command: `./mvnw clean package -DskipTests -Pprod`
3. Start command: `java -jar target/recommender-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`
4. Add same environment variables as above.
5. Set health check path: `/actuator/health`

### Verify Backend

```bash
curl https://your-app.up.railway.app/actuator/health
# → {"status":"UP"}
curl https://your-app.up.railway.app/api/v1/metadata
# → {"cities":["Agra","Ahmedabad",...],"cuisines":["North Indian","Chinese",...],"ready":true}
```

---

## Step 2 — Streamlit Frontend

Create a `streamlit_app.py` file at the project root:

```python
"""streamlit_app.py — Restaurant Recommender UI"""

import os
import requests
import streamlit as st

# ── Config ──────────────────────────────────────────────────────────
API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080")
st.set_page_config(page_title="AI Restaurant Recommender", page_icon="🍽️", layout="centered")


# ── Cached metadata ─────────────────────────────────────────────────
@st.cache_data(ttl=300)
def load_metadata():
    try:
        resp = requests.get(f"{API_BASE_URL}/api/v1/metadata", timeout=10)
        resp.raise_for_status()
        data = resp.json()
        return data.get("cities", []), data.get("cuisines", [])
    except requests.RequestException:
        return [], []


# ── Recommendation call ─────────────────────────────────────────────
def get_recommendations(location, cuisine, budget, min_rating, extra_prefs, top_k):
    payload = {}
    if location and location != "Any":
        payload["location"] = location
    if cuisine and cuisine != "Any":
        payload["cuisine"] = cuisine
    if budget and budget != "Any":
        payload["budget"] = budget.upper()
    if min_rating and min_rating != "Any":
        payload["minRating"] = float(min_rating)
    if extra_prefs:
        payload["additionalPreferences"] = extra_prefs
    payload["topK"] = top_k

    try:
        resp = requests.post(f"{API_BASE_URL}/api/v1/recommend", json=payload, timeout=60)
        resp.raise_for_status()
        return resp.json()
    except requests.RequestException as e:
        st.error(f"Backend unavailable: {e}")
        return None


# ── UI ──────────────────────────────────────────────────────────────
st.title("🍽️ AI-Powered Restaurant Recommender")
st.markdown("Tell us what you're looking for and get personalized recommendations powered by AI.")

cities, cuisines = load_metadata()
if not cities:
    st.warning("⚠️ Could not load metadata. Is the backend running?")

with st.form("search_form"):
    col1, col2 = st.columns(2)
    with col1:
        location = st.selectbox("📍 Location", ["Any", *sorted(cities)])
        cuisine = st.selectbox("🥘 Cuisine", ["Any", *sorted(cuisines)])
        budget = st.selectbox("💰 Budget", ["Any", "Low", "Medium", "High"])
    with col2:
        min_rating = st.selectbox("⭐ Minimum Rating", ["Any", "3.0", "3.5", "4.0", "4.5"])
        top_k = st.selectbox("🔢 Results", [3, 5, 10], index=1)
        extra_prefs = st.text_input("📝 Extra Preferences (optional)", placeholder="e.g. family-friendly, vegetarian, cozy")

    submitted = st.form_submit_button("🔍 Find Restaurants", type="primary", use_container_width=True)

if submitted:
    if not location or location == "Any":
        st.info("Showing recommendations from all locations.")
    with st.spinner("🤖 Consulting the AI chef..."):
        result = get_recommendations(location, cuisine, budget, min_rating, extra_prefs, top_k)

    if result is None:
        st.stop()

    if result.get("usedFallback"):
        st.warning("⚠️ AI ranking unavailable — showing top-rated results as fallback.")

    recs = result.get("recommendations", [])
    summary = result.get("summary", "")
    candidates = result.get("candidatesConsidered", 0)

    if summary:
        st.markdown(f"**{summary}**")

    if not recs:
        st.info(f"No recommendations found. (Considered {candidates} restaurants — try broadening your filters.)")
    else:
        st.success(f"Found {len(recs)} recommendations (from {candidates} candidates)")
        for r in recs:
            with st.container(border=True):
                cols = st.columns([1, 5])
                with cols[0]:
                    st.markdown(f"<h1 style='text-align:center;color:#ff4b4b;'>{r['rank']}</h1>", unsafe_allow_html=True)
                with cols[1]:
                    st.markdown(f"### {r['restaurantName']}")
                    meta = []
                    if r.get("rating"):
                        meta.append(f"⭐ {r['rating']}/5")
                    if r.get("costForTwo") is not None:
                        meta.append(f"💰 ₹{r['costForTwo']} for two")
                    if r.get("city"):
                        meta.append(f"📍 {r['city']}")
                    st.markdown(" · ".join(meta))
                    if r.get("cuisines"):
                        st.markdown(f"*{', '.join(r['cuisines'])}*")
                    if r.get("explanation"):
                        st.markdown(r["explanation"])
                    if r.get("tags"):
                        st.markdown(" ".join(f"`{t}`" for t in r["tags"]))
```

### Run Streamlit locally (for testing)

```bash
pip install streamlit requests
streamlit run streamlit_app.py
# Opens at http://localhost:8501
```

Create a `requirements.txt`:

```
streamlit>=1.35.0
requests>=2.31.0
```

---

## Step 3 — Deploy Streamlit App

### Option A: Streamlit Community Cloud (recommended)

1. Push `streamlit_app.py` and `requirements.txt` to a GitHub repo.
2. Go to [share.streamlit.io](https://share.streamlit.io/) → **Deploy an app**.
3. Select repo, branch, and main file: `streamlit_app.py`.
4. Add **Secrets** in Streamlit Cloud dashboard → Advanced settings:

```toml
API_BASE_URL = "https://your-backend.up.railway.app"
```

5. Click **Deploy**.
6. Your app will be live at `https://your-username-your-repo-app-name.streamlit.app`

### Option B: Deploy on Hugging Face Spaces

1. Create a Space at [huggingface.co/spaces](https://huggingface.co/spaces) → **Streamlit** SDK.
2. Push `streamlit_app.py`, `requirements.txt`, and a `README.md` with:

```yaml
---
title: AI Restaurant Recommender
emoji: 🍽️
colorFrom: red
colorTo: blue
sdk: streamlit
sdk_version: "1.35.0"
app_file: streamlit_app.py
pinned: false
---
```

3. Add `API_BASE_URL` as a Space secret.
4. App at `https://your-username-your-space.hf.space`

---

## Environment Variables Summary

| Variable | Where | Purpose |
|----------|-------|---------|
| `LLM_API_KEY` | Backend host | Groq/OpenAI API key |
| `LLM_BASE_URL` | Backend host | LLM provider endpoint |
| `SERVER_PORT` | Backend host | Spring Boot port (default 8080) |
| `DATASET_URL` | Backend host | Hugging Face CSV URL |
| `API_BASE_URL` | Streamlit secrets | URL of Spring Boot backend |

---

## Cost Breakdown (Free Tier)

| Service | Component | Cost |
|---------|-----------|------|
| Streamlit Community Cloud | Frontend hosting | Free |
| Railway / Render | Backend hosting | Free tier (limited hours/mo) |
| Groq API | LLM inference | Free tier (limited req/min) |
| GitHub | Source code | Free |

---

## Troubleshooting

### CORS Errors

The Spring Boot `WebConfig` must allow the Streamlit origin. Add to `application.yml` or a `@Configuration`:

```yaml
app:
  cors:
    allowed-origins: https://your-app.streamlit.app,http://localhost:8501
```

### Backend Cold Start

Railway/Render free tier spins down after inactivity. First request may take 15–30s. Consider using a **cron-job.org** ping every 10 min to `/actuator/health`.

### Dataset Loading (Memory)

The 51k-row Zomato CSV loads in ~2 seconds with < 256 MB heap. Monitor with `/actuator/health`.

---

## Verification Checklist

- [ ] Backend health check responds `200`
- [ ] `GET /api/v1/metadata` returns cities and cuisines
- [ ] `POST /api/v1/recommend` returns recommendations with valid JSON
- [ ] Streamlit app loads and populates dropdowns
- [ ] Search form submits and displays result cards
- [ ] Fallback ranking works when LLM is unavailable
