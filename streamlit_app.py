"""streamlit_app.py — AI-Powered Restaurant Recommender UI"""

import os
import requests
import streamlit as st

API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080")
REQUEST_TIMEOUT = 60

FOOD_IMAGES = [
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCX7dbEjC7CrB4wX-SvOe23SKn9SzxYa4RrYGHxhSaFEfogKqsCxp636UPuJxNUwiaGnNrKcJZnYP1Znl-7hH924xFjXwLlEH_B8frh3xTQ_xUmjkLU7PA_qHbLXbGw9Jh57y1I0_ab_rGECwHMPOa4ayk8p-dzYmoxNviXmRonr1lThfjB6qOSridQGK3clJG01JtoVAsERKYuiRHDZ3o3W_q0wLox_5D-t14YrxJTtzFF1lolIoYfnOFafTngJc-SNDp1onQQ-A",
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCxSYY0iKHtqoglVudGEV1LUmVPZD4ENwGElgdTE7WtBeVeJA_I0PHH38QrU1Q8M_Ri_FAIImvpDPqKaNZ_ftU4srT_JknWkKxDVq3Ff3Pt7DUBK8G5DMgcsOaKBHZcU4T1uNYOvPjdWbLJQx7StDtd2z-9uq3lyO9gWvuLQ4I4A4cbbvn51POsV3QrQsd2Ky5zoxTKWHwIL_C6w0h0oDVJ3SYRFhAtuByhhVQE1kDIhSLzmf-z3g6RIqIAo3Mnu4qqPRrpGmkIzw",
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCCGTHUJWTQtJFBWD7gX_w0dNm5vG0PWF_vBJ_VKDlgQpKru_2j7-rK9OR0FC3BS1qLtqNs8fQbQ5HwLE5scPQcdVmgXmIdFFdCRT6eCnT5v9q27TQ7n2Ak4aqQL0qrrI2o46n0vF_nG7PY-1K3BQp5Fl12gZ5_p4NMGDLYnKMZRCgh-_kVU6YQK0OlgGMUFvA3puLqZ0vUf0bG-vX5D_QCOnqjQMGJ7FBgrRtjgVVdMmYNhSi3nMnSrxu7jz3CWQ5cOqQrLQ",
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCMrESx0St0WBEs1Ghs-3tPZgHkloHq1Z8tcMqZ17F1eWUZl1k7aO1tBF6l6ln0AYMYCH2vDSd-HLLeCxYTF7WqBcNufr-CLISJRPuHeKzWzJ3tPwHakxJHgLkxBFsfWSAM8FOYw_PNKtMP9QvEr8pLXsFszz9EBDxbYqVqW-mQAU4B8OsERGNzGpxKDOsJEqf8YY3Qp2r2R_2ys0BKBJqKEP-LGGDlKD5zCwAblhBH0NlGR81NQnBn7pU5k5TmYQ8fS5OZJQ",
]

st.set_page_config(page_title="AI Restaurant Recommender", page_icon="🍽️", layout="centered", initial_sidebar_state="collapsed")

st.markdown(f"""
<style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');
    @import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

    * {{ font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }}
    .stApp {{ background: #faf8ff; }}
    .main > .blockContainer {{ max-width: 800px !important; padding: 0 24px; }}

    /* Navbar */
    .navbar {{
        position: fixed; top: 0; left: 0; right: 0; z-index: 50;
        background: #fff; border-bottom: 1px solid #e8e8e8;
        padding: 0 24px; display: flex; justify-content: center;
    }}
    .navbar-inner {{
        display: flex; align-items: center; justify-content: space-between;
        width: 100%; max-width: 800px; height: 64px;
    }}
    .nav-brand {{ font-size: 24px; font-weight: 700; color: #b81120; text-decoration: none; letter-spacing: -0.01em; }}
    .nav-links {{ display: flex; gap: 16px; align-items: center; }}
    .nav-link {{ font-size: 14px; font-weight: 500; color: #5f5e5e; text-decoration: none; padding: 4px 0; transition: color 0.2s; }}
    .nav-link:hover {{ color: #b81120; }}
    .nav-link.active {{ color: #b81120; border-bottom: 2px solid #b81120; }}
    .btn-signin {{
        background: #b81120; color: #fff; border: none;
        padding: 8px 20px; border-radius: 8px;
        font-size: 14px; font-weight: 500; font-family: inherit; cursor: pointer;
        transition: background 0.2s;
    }}
    .btn-signin:hover {{ background: #dc3135; }}

    /* Main Content */
    .main-content {{
        margin-top: 88px; padding: 32px 0 64px;
        display: flex; flex-direction: column; gap: 32px;
    }}

    /* Hero */
    .hero {{ text-align: center; padding-top: 8px; }}
    .hero-icon {{ display: flex; align-items: center; justify-content: center; margin-bottom: 8px; }}
    .hero-icon span {{ font-size: 40px; color: #b81120; }}
    .hero h1 {{ font-size: 36px; font-weight: 800; color: #181b26; letter-spacing: -0.02em; line-height: 44px; }}
    .hero p {{ font-size: 18px; color: #5f5e5e; max-width: 560px; margin: 8px auto 0; line-height: 28px; }}

    /* Search Section */
    .search-section {{
        background: #fff; border-radius: 16px; padding: 24px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        border: 1px solid #e0e2f1;
    }}
    .search-section div[data-testid="stForm"] {{ border: none; padding: 0; }}
    .search-section div[data-testid="stForm"] > div {{ gap: 0; }}

    .form-grid {{
        display: grid; grid-template-columns: 1fr 1fr; gap: 16px;
    }}
    .form-col {{ display: flex; flex-direction: column; gap: 16px; }}

    .stSelectbox label, .stTextInput label {{ font-size: 14px !important; font-weight: 500 !important; color: #5b403e !important; line-height: 20px !important; }}
    div[data-testid="stSelectbox"] > div > div, div[data-testid="stTextInput"] > div > div > input {{
        border: 1px solid #e4bdba !important; border-radius: 8px !important;
        background: #faf8ff !important; font-size: 16px !important;
        padding: 8px 12px !important; line-height: 24px !important;
        transition: border-color 0.2s, box-shadow 0.2s !important;
    }}
    div[data-testid="stSelectbox"] > div > div:focus-within, div[data-testid="stTextInput"] > div > div > input:focus {{
        border-color: #b81120 !important; box-shadow: 0 0 0 2px rgba(184,17,32,0.15) !important;
    }}

    .btn-search button {{
        display: flex !important; align-items: center !important; justify-content: center !important; gap: 8px !important;
        width: 100% !important; background: #ff4b4b !important; color: #fff !important;
        border: none !important; border-radius: 8px !important;
        padding: 12px 24px !important; font-size: 20px !important;
        font-weight: 600 !important; height: auto !important; line-height: 28px !important;
        font-family: inherit !important; cursor: pointer !important;
        transition: background 0.2s, transform 0.1s !important;
        box-shadow: 0 1px 2px rgba(0,0,0,0.05) !important;
    }}
    .btn-search button:hover {{ background: #dc3135 !important; }}
    .btn-search button:active {{ transform: scale(0.98) !important; }}
    .btn-search button:disabled {{ opacity: 0.6; cursor: not-allowed !important; }}

    /* Loading */
    .loading-state {{
        display: flex; flex-direction: column; align-items: center;
        justify-content: center; padding: 40px 0; gap: 16px; color: #5f5e5e;
    }}
    .spinner {{
        width: 32px; height: 32px; border: 3px solid #e0e2f1;
        border-top-color: #b81120; border-radius: 50%;
        animation: spin 0.7s linear infinite;
    }}
    @@keyframes spin {{ to {{ transform: rotate(360deg); }} }}
    .loading-state p {{ font-size: 14px; font-weight: 500; line-height: 20px; }}

    /* State Cards */
    .state-card {{
        background: #ebedfc; border-radius: 12px; padding: 24px;
        text-align: center; border: 1px solid #e0e2f1;
    }}
    .state-card .state-icon {{ font-size: 36px; color: #5f5e5e; margin-bottom: 8px; }}
    .state-card h3 {{ font-size: 20px; font-weight: 600; color: #181b26; line-height: 28px; margin-bottom: 4px; }}
    .state-card p {{ font-size: 16px; color: #5f5e5e; line-height: 24px; }}

    /* Results */
    .results-section {{ display: flex; flex-direction: column; gap: 16px; }}

    .results-header {{
        display: flex; align-items: center; justify-content: space-between;
        border-bottom: 1px solid #e0e2f1; padding-bottom: 8px;
        flex-wrap: wrap; gap: 8px;
    }}
    .results-header h2 {{ font-size: 20px; font-weight: 600; color: #181b26; line-height: 28px; }}
    .candidates-count {{ font-size: 16px; font-weight: 400; color: #5f5e5e; }}

    .fallback-banner {{
        display: flex; align-items: center; gap: 6px;
        background: #fffbe6; border: 1px solid #ffe58f;
        border-radius: 8px; padding: 8px 14px;
        font-size: 14px; color: #8d6e00; font-weight: 500; line-height: 20px; width: 100%;
    }}

    .results-summary {{ font-size: 16px; color: #5f5e5e; font-style: italic; line-height: 24px; }}

    /* Result Cards */
    .card-list {{ display: flex; flex-direction: column; gap: 32px; }}

    .result-card {{
        background: #fff; border-radius: 16px; overflow: hidden;
        box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        transition: box-shadow 0.3s;
        border: 1px solid #e0e2f1;
        display: flex; flex-direction: column;
    }}
    .result-card:hover {{ box-shadow: 0 8px 24px rgba(0,0,0,0.12); }}

    .card-image {{
        width: 100%; height: 200px; overflow: hidden; flex-shrink: 0;
    }}
    .card-image img {{
        width: 100%; height: 100%; object-fit: cover; display: block;
    }}

    .card-content {{
        padding: 16px; display: flex; gap: 16px; flex: 1;
    }}
    .card-rank {{ flex-shrink: 0; padding-top: 2px; }}
    .rank-number {{
        font-size: 36px; font-weight: 800;
        color: rgba(184,17,32,0.4); line-height: 44px;
        letter-spacing: -0.02em; transition: opacity 0.2s;
    }}
    .result-card:hover .rank-number {{ opacity: 1; }}
    .rank-number.top {{ opacity: 0.8; }}
    .result-card:hover .rank-number.top {{ opacity: 1; }}

    .card-details {{ flex: 1; display: flex; flex-direction: column; gap: 8px; min-width: 0; }}
    .card-title-row {{
        display: flex; justify-content: space-between;
        align-items: flex-start; gap: 12px;
    }}
    .card-title {{ font-size: 24px; font-weight: 700; color: #181b26; line-height: 32px; letter-spacing: -0.01em; }}

    .rating-badge {{
        display: flex; align-items: center; gap: 2px;
        background: #008472; color: #fff;
        padding: 4px 8px; border-radius: 4px;
        font-size: 12px; font-weight: 500; line-height: 16px; white-space: nowrap; flex-shrink: 0;
    }}

    .card-meta {{
        display: flex; align-items: center; gap: 8px;
        font-size: 14px; font-weight: 500; color: #5f5e5e; line-height: 20px;
        flex-wrap: wrap;
    }}
    .meta-sep {{ color: #906f6d; }}

    .card-cuisines {{
        font-size: 16px; font-style: italic; color: #5b403e; line-height: 24px;
    }}
    .card-explanation {{
        font-size: 16px; color: #181b26; line-height: 24px;
    }}
    .card-tags {{ display: flex; flex-wrap: wrap; gap: 8px; margin-top: 4px; }}
    .tag {{
        display: inline-block; padding: 4px 12px; border-radius: 9999px;
        font-size: 12px; font-weight: 500; line-height: 16px;
        background: #f8f8f8; color: #5b403e; border: 1px solid #e4bdba;
    }}

    /* Footer */
    .footer {{
        width: 100%; padding: 24px; margin-top: 32px;
        border-top: 1px solid #e4bdba; background: #f2f3ff;
        text-align: center;
    }}
    .footer-inner {{ max-width: 800px; margin: 0 auto; display: flex; flex-direction: column; align-items: center; gap: 16px; }}
    .footer .brand {{ font-size: 20px; font-weight: 700; color: #b81120; }}
    .footer-links {{ display: flex; gap: 16px; }}
    .footer-links a {{ font-size: 12px; font-weight: 500; color: #474747; text-decoration: none; line-height: 16px; transition: opacity 0.2s; }}
    .footer-links a:hover {{ text-decoration: underline; opacity: 0.8; }}
    .footer-copy {{ font-size: 12px; font-weight: 500; color: #5f5e5e; line-height: 16px; }}

    @@media (max-width: 768px) {{
        .hero h1 {{ font-size: 28px; line-height: 36px; }}
        .hero p {{ font-size: 16px; line-height: 24px; }}
        .nav-links {{ display: none; }}
        .form-grid {{ grid-template-columns: 1fr; }}
        .card-content {{ flex-direction: column; gap: 8px; }}
        .rank-number {{ font-size: 24px; line-height: 32px; }}
        .card-title {{ font-size: 20px; line-height: 28px; }}
        .card-image {{ height: 160px; }}
        .results-header {{ flex-direction: column; align-items: flex-start; }}
    }}
</style>
""", unsafe_allow_html=True)

# ── Navbar ──
st.markdown("""
<div class="navbar">
    <div class="navbar-inner">
        <a href="#" class="nav-brand">CraveAI</a>
        <div class="nav-links">
            <a href="#" class="nav-link active">Discover</a>
            <a href="#" class="nav-link">Favorites</a>
            <a href="#" class="nav-link">History</a>
        </div>
        <button class="btn-signin">Sign In</button>
    </div>
</div>
""", unsafe_allow_html=True)

# ── Main Content ──
st.markdown('<div class="main-content">', unsafe_allow_html=True)

# ── Hero ──
st.markdown("""
<header class="hero">
    <div class="hero-icon">
        <span class="material-symbols-outlined">restaurant</span>
    </div>
    <h1>AI-Powered Restaurant Recommender</h1>
    <p>Tell us what you're looking for and get personalized recommendations powered by AI.</p>
</header>
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

# ── Search Section ──
st.markdown('<section class="search-section">', unsafe_allow_html=True)
with st.form("search_form"):
    st.markdown('<div class="form-grid">', unsafe_allow_html=True)

    st.markdown('<div class="form-col">', unsafe_allow_html=True)
    location = st.selectbox("Location", ["Any", *sorted(cities)] if cities else ["Any"])
    cuisine = st.selectbox("Cuisine", ["Any", *sorted(cuisines)] if cuisines else ["Any"])
    budget = st.selectbox("Budget", ["Any", "Low", "Medium", "High"])
    st.markdown('</div>', unsafe_allow_html=True)

    st.markdown('<div class="form-col">', unsafe_allow_html=True)
    min_rating = st.selectbox("Min Rating", ["Any", "3.0", "3.5", "4.0", "4.5"])
    top_k = st.selectbox("Results count", [3, 5, 10], index=1, format_func=lambda x: f"{x} Recommendations")
    extra_prefs = st.text_input("Extra Preferences", placeholder="e.g. family-friendly, vegetarian")
    st.markdown('</div>', unsafe_allow_html=True)

    st.markdown('</div>', unsafe_allow_html=True)

    st.markdown('<div class="btn-search">', unsafe_allow_html=True)
    submitted = st.form_submit_button(" Find Restaurants", type="primary", use_container_width=True)
    st.markdown('</div>', unsafe_allow_html=True)
st.markdown('</section>', unsafe_allow_html=True)


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
        <div class="state-card">
            <span class="material-symbols-outlined state-icon">error</span>
            <h3>Something went wrong</h3>
            <p>{error}</p>
        </div>
        """, unsafe_allow_html=True)
        st.stop()

    if result is None:
        st.stop()

    recs = result.get("recommendations", [])
    summary = result.get("summary", "")
    candidates = result.get("candidatesConsidered", 0)
    used_fallback = result.get("usedFallback", False)

    st.markdown('<section class="results-section">', unsafe_allow_html=True)

    st.markdown(f"""
    <div class="results-header">
        <h2>
            Found <strong>{len(recs)}</strong> recommendation{'s' if len(recs) != 1 else ''}
            <span class="candidates-count"> (from {candidates} candidate{'s' if candidates != 1 else ''})</span>
        </h2>
    </div>
    """, unsafe_allow_html=True)

    if used_fallback:
        st.markdown("""
        <div class="fallback-banner">
            <span class="material-symbols-outlined" style="font-size:16px">info</span>
            AI ranking unavailable — showing top-rated results as a fallback.
        </div>
        """, unsafe_allow_html=True)

    if summary:
        st.markdown(f'<p class="results-summary">{summary}</p>', unsafe_allow_html=True)

    if not recs:
        st.markdown("""
        <div class="state-card">
            <span class="material-symbols-outlined state-icon">search_off</span>
            <h3>No recommendations found</h3>
            <p>Try adjusting your filters for more results.</p>
        </div>
        """, unsafe_allow_html=True)
        st.stop()

    st.markdown('<div class="card-list">', unsafe_allow_html=True)

    for idx, r in enumerate(recs):
        rating = r.get("rating", 0)
        cost = r.get("costForTwo")
        city = r.get("city", "")
        cuisines_list = r.get("cuisines", [])
        explanation = r.get("explanation", "")
        tags = r.get("tags", [])

        rank_top = r["rank"] <= 3

        cost_symbol = ""
        if cost is not None:
            if cost >= 1500:
                cost_symbol = "₹₹₹"
            elif cost >= 500:
                cost_symbol = "₹₹"
            else:
                cost_symbol = "₹"

        img_url = FOOD_IMAGES[idx % len(FOOD_IMAGES)]

        tags_html = ""
        if tags:
            tags_html = '<div class="card-tags">' + "".join(f'<span class="tag">{t}</span>' for t in tags) + "</div>"

        location_html = ""
        if city:
            location_html = f"""
                <span class="meta-sep">·</span>
                <span>📍 {city}</span>
            """

        st.markdown(f"""
        <article class="result-card">
            <div class="card-image">
                <img src="{img_url}" alt="{r['restaurantName']}" loading="lazy" />
            </div>
            <div class="card-content">
                <div class="card-rank">
                    <span class="rank-number{' top' if rank_top else ''}">#{r['rank']}</span>
                </div>
                <div class="card-details">
                    <div class="card-title-row">
                        <h3 class="card-title">{r['restaurantName']}</h3>
                        <div class="rating-badge">{rating} ★</div>
                    </div>
                    <div class="card-meta">
                        <span>{cost_symbol}</span>
                        {location_html}
                    </div>
                    <p class="card-cuisines">{", ".join(cuisines_list)}</p>
                    <p class="card-explanation">{explanation}</p>
                    {tags_html}
                </div>
            </div>
        </article>
        """, unsafe_allow_html=True)

    st.markdown('</div>', unsafe_allow_html=True)
    st.markdown('</section>', unsafe_allow_html=True)

st.markdown('</div>', unsafe_allow_html=True)

# ── Footer ──
st.markdown("""
<div class="footer">
    <div class="footer-inner">
        <div class="brand">CraveAI</div>
        <div class="footer-links">
            <a href="#">Privacy Policy</a>
            <a href="#">Terms of Service</a>
            <a href="#">Contact Support</a>
        </div>
        <p class="footer-copy">© 2024 CraveAI Recommender. All rights reserved.</p>
    </div>
</div>
""", unsafe_allow_html=True)
