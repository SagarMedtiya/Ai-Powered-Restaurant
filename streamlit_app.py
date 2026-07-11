"""streamlit_app.py — AI-Powered Restaurant Recommender UI"""

import os
import requests
import streamlit as st

API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080")
REQUEST_TIMEOUT = 60

st.set_page_config(page_title="AI Restaurant Recommender", page_icon="🍽️", layout="centered", initial_sidebar_state="collapsed")

st.markdown(f"""
<style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

    * {{ font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }}
    .stApp {{ background: #fafafa; }}
    .main > .blockContainer {{ max-width: 720px !important; padding: 0 28px; }}

    /* Navbar */
    .navbar {{
        flex-shrink: 0;
        background: rgba(255, 255, 255, 0.9);
        backdrop-filter: blur(12px);
        border-bottom: 1px solid #eee;
        padding: 0 28px;
        display: flex;
        justify-content: center;
        height: 56px;
        align-items: center;
    }}
    .navbar-inner {{
        width: 100%; max-width: 720px;
        display: flex; align-items: center;
    }}
    .nav-brand {{ font-size: 24px; font-weight: 700; color: #ff4b4b; text-decoration: none; letter-spacing: -0.3px; }}

    /* Hero */
    .hero {{ text-align: center; margin-bottom: 28px; padding-top: 32px; }}
    .hero-icon {{ font-size: 40px; display: block; margin-bottom: 8px; }}
    .hero h1 {{ font-size: 28px; font-weight: 800; color: #111; letter-spacing: -0.4px; line-height: 1.2; }}
    .hero p {{ font-size: 16px; color: #666; max-width: 480px; margin: 8px auto 0; }}

    /* Search card */
    .search-card {{
        background: #fff; border-radius: 16px; padding: 24px;
        box-shadow: 0 2px 12px rgba(0,0,0,0.04);
        border: 1px solid #eee; margin-bottom: 24px;
    }}
    .search-card div[data-testid="stForm"] {{ border: none; padding: 0; }}
    .search-card div[data-testid="stForm"] > div {{ gap: 0; }}

    .stSelectbox label, .stTextInput label {{ font-size: 13px !important; font-weight: 600 !important; color: #555 !important; letter-spacing: 0.2px !important; }}
    div[data-testid="stSelectbox"] > div > div, div[data-testid="stTextInput"] > div > div > input {{
        border: 1.5px solid #ddd !important; border-radius: 10px !important;
        background: #fff !important; font-size: 15px !important;
        padding: 10px 14px !important;
        transition: border-color 0.2s, box-shadow 0.2s !important;
    }}
    div[data-testid="stSelectbox"] > div > div:focus-within, div[data-testid="stTextInput"] > div > div > input:focus {{
        border-color: #ff4b4b !important; box-shadow: 0 0 0 3px rgba(255, 75, 75, 0.12) !important;
    }}

    .btn-search button {{
        width: 100% !important; background: #ff4b4b !important; color: #fff !important;
        border: none !important; border-radius: 10px !important;
        padding: 14px 24px !important; font-size: 17px !important;
        font-weight: 600 !important; height: auto !important;
        font-family: inherit !important;
        transition: background 0.2s, transform 0.15s !important;
    }}
    .btn-search button:hover {{ background: #e03e3e !important; }}
    .btn-search button:active {{ transform: scale(0.98) !important; }}
    .btn-search button:disabled {{ opacity: 0.5; cursor: not-allowed; }}

    /* Loading */
    .loading-state {{
        display: flex; flex-direction: column; align-items: center;
        justify-content: center; padding: 40px 0; gap: 14px; color: #888;
        animation: fadeIn 0.3s ease;
    }}
    .spinner {{
        width: 36px; height: 36px; border: 3px solid #eee;
        border-top-color: #ff4b4b; border-radius: 50%;
        animation: spin 0.7s linear infinite;
    }}
    @@keyframes spin {{ to {{ transform: rotate(360deg); }} }}
    .loading-state p {{ font-size: 16px; font-weight: 500; }}

    /* Messages */
    .msg {{
        padding: 16px 20px; border-radius: 12px; margin-bottom: 20px;
        font-size: 15px; animation: fadeIn 0.3s ease;
    }}
    .msg-error {{ background: #fff5f5; color: #c0392b; border: 1px solid #ffd6d6; }}
    .msg-empty {{ background: #f8f9fa; color: #666; border: 1px solid #eee; }}

    /* Results header */
    .results-header {{
        margin-bottom: 18px;
        border-bottom: 1px solid #eee; padding-bottom: 8px;
    }}
    .results-header h2 {{ font-size: 16px; font-weight: 500; color: #555; }}
    .results-header h2 strong {{ font-weight: 700; color: #111; }}
    .results-header .count {{ color: #999; font-weight: 400; }}

    .results-summary {{ font-size: 15px; color: #777; font-style: italic; margin-top: 10px; }}

    .fallback-banner {{
        background: #fffbeb; border: 1px solid #fde68a;
        border-radius: 10px; padding: 10px 16px; margin-top: 12px;
        font-size: 14px; color: #92400e; width: 100%;
    }}

    /* Result card */
    .result-card {{
        display: flex; background: #fff; border-radius: 16px; overflow: hidden;
        box-shadow: none;
        transition: box-shadow 0.25s ease, transform 0.25s ease;
        border: 1px solid #eee; margin-bottom: 12px;
        animation: fadeInUp 0.35s ease forwards;
        opacity: 0;
    }}
    .result-card:hover {{ box-shadow: 0 10px 32px rgba(0,0,0,0.07); transform: translateY(-2px); }}

    .card-rank {{
        display: flex; align-items: center; justify-content: center;
        min-width: 60px; font-size: 24px; font-weight: 800;
        color: #ff4b4b;
    }}

    .card-body {{
        padding: 16px 18px 16px 0; flex: 1; min-width: 0;
    }}
    .card-title-row {{
        display: flex; justify-content: space-between;
        align-items: flex-start; gap: 10px; margin-bottom: 6px;
    }}
    .card-title {{ font-size: 18px; font-weight: 700; color: #111; line-height: 1.3; }}

    .rating-badge {{
        display: inline-flex; align-items: center; gap: 3px;
        padding: 3px 10px; border-radius: 6px;
        font-size: 13px; font-weight: 600; white-space: nowrap; flex-shrink: 0;
    }}
    .rating-badge.green {{ background: #e8f5e9; color: #2e7d32; }}
    .rating-badge.gray {{ background: #f0f0f0; color: #666; }}

    .card-meta {{
        display: flex; align-items: center; gap: 12px;
        font-size: 14px; color: #777;
        flex-wrap: wrap; margin-bottom: 8px;
    }}
    .card-cuisines {{
        font-size: 14px; font-style: italic; color: #888; margin-bottom: 8px;
    }}
    .card-explanation {{
        font-size: 14px; color: #444; line-height: 1.6; margin-bottom: 0;
    }}
    .card-tags {{ display: flex; flex-wrap: wrap; gap: 6px; margin-top: 10px; }}
    .tag {{
        display: inline-block; padding: 3px 10px; border-radius: 6px;
        font-size: 12px; font-weight: 500;
        background: #f0f0f0; color: #666;
    }}

    /* Footer */
    .footer {{
        width: 100%; padding: 14px 28px; margin-top: 32px;
        border-top: 1px solid #eee; background: #fff;
        text-align: center;
    }}
    .footer-inner {{ max-width: 720px; margin: 0 auto; }}
    .footer .brand {{ font-size: 16px; font-weight: 700; color: #ff4b4b; margin-bottom: 3px; }}
    .footer-copy {{ font-size: 12px; color: #999; }}

    /* Animations */
    @@keyframes fadeIn {{
        from {{ opacity: 0; }}
        to {{ opacity: 1; }}
    }}
    @@keyframes fadeInUp {{
        from {{ opacity: 0; transform: translateY(12px); }}
        to {{ opacity: 1; transform: translateY(0); }}
    }}

    @@media (max-width: 640px) {{
        .hero h1 {{ font-size: 22px; }}
        .hero {{ margin-bottom: 20px; }}
        .hero-icon {{ font-size: 32px; }}
        .result-card {{ flex-direction: column; }}
        .card-rank {{ min-width: unset; padding: 8px 0 0 16px; font-size: 20px; }}
        .card-body {{ padding: 8px 16px 16px; }}
        .card-title {{ font-size: 16px; }}
    }}
</style>
""", unsafe_allow_html=True)

# ── Navbar ──
st.markdown("""
<div class="navbar">
    <div class="navbar-inner">
        <span class="nav-brand">RestaurantAI</span>
    </div>
</div>
""", unsafe_allow_html=True)

# ── Hero ──
st.markdown("""
<div class="hero">
    <span class="hero-icon">🍽️</span>
    <h1>AI-Powered Restaurant Recommender</h1>
    <p>Tell us what you're looking for and get personalized recommendations powered by AI.</p>
</div>
""", unsafe_allow_html=True)


# ── Helpers ──
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
        resp = requests.post(f"{API_BASE_URL}/api/v1/recommend", json=payload, timeout=REQUEST_TIMEOUT)
        resp.raise_for_status()
        return resp.json(), None
    except requests.exceptions.Timeout:
        return None, "The recommendation engine is taking longer than expected. Please try again."
    except requests.exceptions.ConnectionError:
        return None, "Cannot connect to the backend. Make sure the server is running."
    except requests.RequestException as e:
        return None, f"Request failed: {e}"


# ── Load metadata ──
cities, cuisines, _ = load_metadata()

# ── Search Form ──
st.markdown('<div class="search-card">', unsafe_allow_html=True)
with st.form("search_form"):
    col1, col2 = st.columns(2)
    with col1:
        location = st.selectbox("Location", ["Any", *sorted(cities)] if cities else ["Any"])
        cuisine = st.selectbox("Cuisine", ["Any", *sorted(cuisines)] if cuisines else ["Any"])
        budget = st.selectbox("Budget", ["Any", "Low", "Medium", "High"])
    with col2:
        min_rating = st.selectbox("Min Rating", ["Any", "3.0", "3.5", "4.0", "4.5"])
        top_k = st.selectbox("Results", [3, 5, 10], index=1, format_func=lambda x: f"{x} Recommendations")
        extra_prefs = st.text_input("Extra Preferences", placeholder="e.g. family-friendly, vegetarian")

    st.markdown('<div class="btn-search">', unsafe_allow_html=True)
    submitted = st.form_submit_button("🔍 Find Restaurants", type="primary", use_container_width=True)
    st.markdown("</div>", unsafe_allow_html=True)
st.markdown("</div>", unsafe_allow_html=True)


# ── Results ──
if submitted:
    with st.spinner(""):
        st.markdown("""
        <div class="loading-state">
            <div class="spinner"></div>
            <p>🤖 Consulting the AI chef...</p>
        </div>
        """, unsafe_allow_html=True)
        result, error = get_recommendations(location, cuisine, budget, min_rating, extra_prefs, top_k)

    if error:
        st.markdown(f"""
        <div class="msg msg-error">{error}</div>
        """, unsafe_allow_html=True)
        st.stop()

    if result is None:
        st.stop()

    recs = result.get("recommendations", [])
    summary = result.get("summary", "")
    candidates = result.get("candidatesConsidered", 0)
    used_fallback = result.get("usedFallback", False)

    if used_fallback:
        st.markdown('<div class="fallback-banner">⚠️ AI ranking unavailable — showing top-rated results as a fallback.</div>', unsafe_allow_html=True)

    if summary:
        st.markdown(f'<p class="results-summary">{summary}</p>', unsafe_allow_html=True)

    if not recs:
        st.markdown("""
        <div class="msg msg-empty">😕 No recommendations found. Try broadening your filters.</div>
        """, unsafe_allow_html=True)
        st.stop()

    # Results header
    plural_r = "s" if len(recs) > 1 else ""
    plural_c = "s" if candidates != 1 else ""
    st.markdown(f"""
    <div class="results-header">
        <h2>Found <strong>{len(recs)}</strong> rec{plural_r} <span class="count">· {candidates} candidate{plural_c}</span></h2>
    </div>
    """, unsafe_allow_html=True)

    # Result cards
    for idx, r in enumerate(recs):
        rating = r.get("rating", 0)
        cost = r.get("costForTwo")
        city = r.get("city", "")
        cuisines_list = r.get("cuisines", [])
        explanation = r.get("explanation", "")
        tags = r.get("tags", [])

        badge_class = "green" if rating >= 4 else "gray"

        meta_parts = []
        if cost is not None:
            meta_parts.append(f'<span>₹{cost} for two</span>')
        if city:
            meta_parts.append(f'<span>📍 {city}</span>')

        tags_html = ""
        if tags:
            tags_html = '<div class="card-tags">' + "".join(f'<span class="tag">{t}</span>' for t in tags) + "</div>"

        st.markdown(f"""
        <div class="result-card" style="animation-delay:{idx * 0.06}s">
            <div class="card-rank">#{r['rank']}</div>
            <div class="card-body">
                <div class="card-title-row">
                    <h3 class="card-title">{r['restaurantName']}</h3>
                    <span class="rating-badge {badge_class}">⭐ {rating}</span>
                </div>
                <div class="card-meta">{" ".join(meta_parts)}</div>
                <p class="card-cuisines">{", ".join(cuisines_list)}</p>
                <p class="card-explanation">{explanation}</p>
                {tags_html}
            </div>
        </div>
        """, unsafe_allow_html=True)


# ── Footer ──
st.markdown("""
<div class="footer">
    <div class="footer-inner">
        <p class="brand">RestaurantAI</p>
        <p class="footer-copy">© 2026 RestaurantAI. All rights reserved.</p>
    </div>
</div>
""", unsafe_allow_html=True)
