"""streamlit_app.py — AI-Powered Restaurant Recommender UI"""

import os
import requests
import streamlit as st

# ── Configuration ────────────────────────────────────────────────────
API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080")
REQUEST_TIMEOUT = 60

st.set_page_config(
    page_title="AI Restaurant Recommender",
    page_icon="🍽️",
    layout="centered",
    initial_sidebar_state="collapsed",
)

# ── Styling ──────────────────────────────────────────────────────────
st.markdown("""
<style>
    .main > .block-container { padding-top: 2rem; }
    .rank-badge {
        font-size: 2.2rem;
        font-weight: 800;
        color: #ff4b4b;
        text-align: center;
        line-height: 1;
    }
    .result-card {
        border: 1px solid #e0e0e0;
        border-radius: 12px;
        padding: 1.2rem 1.5rem;
        margin-bottom: 1rem;
        background: #ffffff;
    }
    .restaurant-name { font-size: 1.3rem; font-weight: 700; margin-bottom: 0.25rem; }
    .meta-row { font-size: 0.9rem; color: #555; margin-bottom: 0.5rem; }
    .cuisines { font-size: 0.9rem; font-style: italic; color: #777; margin-bottom: 0.5rem; }
    .explanation { font-size: 0.95rem; line-height: 1.5; color: #333; }
    .tag {
        display: inline-block;
        background: #f0f0f0;
        border-radius: 4px;
        padding: 0.1rem 0.5rem;
        font-size: 0.8rem;
        margin-right: 0.3rem;
        color: #555;
    }
    .fallback-banner {
        background: #fffbe6;
        border: 1px solid #ffe58f;
        border-radius: 8px;
        padding: 0.75rem 1rem;
        margin-bottom: 1rem;
        font-size: 0.9rem;
    }
    div[data-testid="stForm"] { border: none; padding: 0; }
    .stButton button { font-weight: 600; }
</style>
""", unsafe_allow_html=True)


# ── Helpers ──────────────────────────────────────────────────────────
@st.cache_data(ttl=300, show_spinner="Loading restaurant data...")
def load_metadata():
    try:
        resp = requests.get(f"{API_BASE_URL}/api/v1/metadata", timeout=10)
        resp.raise_for_status()
        data = resp.json()
        return data.get("cities", []), data.get("cuisines", []), data.get("ready", False)
    except requests.RequestException:
        return [], [], False


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
    else:
        payload["minRating"] = 0.0
    if extra_prefs:
        payload["additionalPreferences"] = extra_prefs
    payload["topK"] = top_k

    try:
        resp = requests.post(
            f"{API_BASE_URL}/api/v1/recommend",
            json=payload,
            timeout=REQUEST_TIMEOUT,
        )
        resp.raise_for_status()
        return resp.json(), None
    except requests.exceptions.Timeout:
        return None, "The recommendation engine is taking longer than expected. Please try again."
    except requests.exceptions.ConnectionError:
        return None, "Cannot connect to the backend. Make sure the server is running."
    except requests.RequestException as e:
        return None, f"Request failed: {e}"


# ── Header ──────────────────────────────────────────────────────────
st.title("🍽️ AI-Powered Restaurant Recommender")
st.markdown(
    "Tell us what you're looking for and get personalized recommendations "
    "powered by AI."
)

# ── Load metadata ────────────────────────────────────────────────────
cities, cuisines, ready = load_metadata()
if not ready:
    st.warning(
        "⚠️ Restaurant catalog is still loading. Some features may be "
        "unavailable. Please wait a moment and refresh."
    )

# ── Form ─────────────────────────────────────────────────────────────
with st.form("search_form"):
    col1, col2 = st.columns(2)

    with col1:
        loc_opts = ["Any", *sorted(cities)] if cities else ["Any"]
        location = st.selectbox("📍 Location", loc_opts)

        cui_opts = ["Any", *sorted(cuisines)] if cuisines else ["Any"]
        cuisine = st.selectbox("🥘 Cuisine", cui_opts)

        budget = st.selectbox(
            "💰 Budget",
            ["Any", "Low", "Medium", "High"],
            help="Low: up to ₹500 | Medium: ₹500–₹1500 | High: ₹1500+",
        )

    with col2:
        min_rating = st.selectbox(
            "⭐ Minimum Rating",
            ["Any", "3.0", "3.5", "4.0", "4.5"],
        )

        top_k = st.selectbox("🔢 Results", [3, 5, 10], index=1)

        extra_prefs = st.text_input(
            "📝 Extra Preferences",
            placeholder="e.g. family-friendly, vegetarian, cozy",
            help="Any special requests? The AI will consider them.",
        )

    submitted = st.form_submit_button(
        "🔍 Find Restaurants", type="primary", use_container_width=True
    )

# ── Results ──────────────────────────────────────────────────────────
if submitted:
    if not ready:
        st.error("The restaurant catalog is still loading. Please try again in a moment.")
        st.stop()

    with st.spinner("🤖 Consulting the AI chef..."):
        result, error = get_recommendations(
            location, cuisine, budget, min_rating, extra_prefs, top_k,
        )

    if error:
        st.error(error)
        st.stop()

    if result is None:
        st.stop()

    recs = result.get("recommendations", [])
    summary = result.get("summary", "")
    candidates = result.get("candidatesConsidered", 0)
    used_fallback = result.get("usedFallback", False)

    # ── Summary ──
    if summary:
        st.markdown(f"> {summary}")

    if used_fallback:
        st.markdown(
            '<div class="fallback-banner">'
            "⚠️ **AI ranking unavailable** — showing top-rated results as a fallback."
            "</div>",
            unsafe_allow_html=True,
        )

    # ── Empty state ──
    if not recs:
        st.info(
            f"😕 No recommendations found. "
            f"Considered **{candidates}** restaurants — "
            "try broadening your filters."
        )
        st.stop()

    # ── Results header ──
    st.success(
        f"Found **{len(recs)}** recommendation{'s' if len(recs) > 1 else ''} "
        f"(from {candidates} candidate{'s' if candidates != 1 else ''})"
    )

    # ── Result cards ──
    for r in recs:
        with st.container():
            cols = st.columns([1, 6])
            with cols[0]:
                st.markdown(
                    f'<div class="rank-badge">#{r["rank"]}</div>',
                    unsafe_allow_html=True,
                )
            with cols[1]:
                st.markdown(
                    f'<div class="result-card">'
                    f'  <div class="restaurant-name">{r["restaurantName"]}</div>',
                    unsafe_allow_html=True,
                )

                meta_parts = []
                if r.get("rating"):
                    meta_parts.append(f"⭐ {r['rating']}/5")
                if r.get("costForTwo") is not None:
                    meta_parts.append(f"💰 ₹{r['costForTwo']} for two")
                if r.get("city"):
                    meta_parts.append(f"📍 {r['city']}")
                if r.get("location"):
                    meta_parts.append(f"🗺️ {r['location']}")

                if meta_parts:
                    st.markdown(
                        f'<div class="meta-row">{" · ".join(meta_parts)}</div>',
                        unsafe_allow_html=True,
                    )

                if r.get("cuisines"):
                    st.markdown(
                        f'<div class="cuisines">{", ".join(r["cuisines"])}</div>',
                        unsafe_allow_html=True,
                    )

                if r.get("explanation"):
                    st.markdown(
                        f'<div class="explanation">{r["explanation"]}</div>',
                        unsafe_allow_html=True,
                    )

                if r.get("tags"):
                    tags_html = "".join(
                        f'<span class="tag">{t}</span>' for t in r["tags"]
                    )
                    st.markdown(f'<div style="margin-top:0.5rem">{tags_html}</div>', unsafe_allow_html=True)

                st.markdown("</div>", unsafe_allow_html=True)
