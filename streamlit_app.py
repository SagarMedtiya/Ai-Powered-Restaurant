import os, requests, streamlit as st

API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080")

FOOD_IMAGES = [
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCX7dbEjC7CrB4wX-SvOe23SKn9SzxYa4RrYGHxhSaFEfogKqsCxp636UPuJxNUwiaGnNrKcJZnYP1Znl-7hH924xFjXwLlEH_B8frh3xTQ_xUmjkLU7PA_qHbLXbGw9Jh57y1I0_ab_rGECwHMPOa4ayk8p-dzYmoxNviXmRonr1lThfjB6qOSridQGK3clJG01JtoVAsERKYuiRHDZ3o3W_q0wLox_5D-t14YrxJTtzFF1lolIoYfnOFafTngJc-SNDp1onQQ-A",
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCxSYY0iKHtqoglVudGEV1LUmVPZD4ENwGElgdTE7WtBeVeJA_I0PHH38QrU1Q8M_Ri_FAIImvpDPqKaNZ_ftU4srT_JknWkKxDVq3Ff3Pt7DUBK8G5DMgcsOaKBHZcU4T1uNYOvPjdWbLJQx7StDtd2z-9uq3lyO9gWvuLQ4I4A4cbbvn51POsV3QrQsd2Ky5zoxTKWHwIL_C6w0h0oDVJ3SYRFhAtuByhhVQE1kDIhSLzmf-z3g6RIqIAo3Mnu4qqPRrpGmkIzw",
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCCGTHUJWTQtJFBWD7gX_w0dNm5vG0PWF_vBJ_VKDlgQpKru_2j7-rK9OR0FC3BS1qLtqNs8fQbQ5HwLE5scPQcdVmgXmIdFFdCRT6eCnT5v9q27TQ7n2Ak4aqQL0qrrI2o46n0vF_nG7PY-1K3BQp5Fl12gZ5_p4NMGDLYnKMZRCgh-_kVU6YQK0OlgGMUFvA3puLqZ0vUf0bG-vX5D_QCOnqjQMGJ7FBgrRtjgVVdMmYNhSi3nMnSrxu7jz3CWQ5cOqQrLQ",
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCMrESx0St0WBEs1Ghs-3tPZgHkloHq1Z8tcMqZ17F1eWUZl1k7aO1tBF6l6ln0AYMYCH2vDSd-HLLeCxYTF7WqBcNufr-CLISJRPuHeKzWzJ3tPwHakxJHgLkxBFsfWSAM8FOYw_PNKtMP9QvEr8pLXsFszz9EBDxbYqVqW-mQAU4B8OsERGNzGpxKDOsJEqf8YY3Qp2r2R_2ys0BKBJqKEP-LGGDlKD5zCwAblhBH0NlGR81NQnBn7pU5k5TmYQ8fS5OZJQ",
]

st.set_page_config(page_title="RestaurantAI", page_icon="🍽️", layout="centered", initial_sidebar_state="collapsed")

st.markdown(f"""
<style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');
    @import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

    * {{ font-family: 'Inter', sans-serif; box-sizing: border-box; }}
    html, body {{ margin: 0 !important; padding: 0 !important; height: 100vh !important; overflow: hidden !important; }}
    .stApp {{ background: #ffffff; height: 100vh; overflow-y: auto; }}
    header, .stDeployButton, #MainMenu {{ display: none !important; visibility: hidden !important; }}
    footer {{ display: none !important; }}
    .main > .blockContainer {{ max-width: 800px !important; padding: 0 24px !important; }}

    .navbar {{
        position: fixed; top: 0; left: 0; right: 0; z-index: 50;
        background: rgba(255,255,255,0.9); backdrop-filter: blur(12px);
        border-bottom: 1px solid #e9ecef;
        padding: 0 24px; display: flex; justify-content: center; height: 48px;
    }}
    .navbar-inner {{
        display: flex; align-items: center;
        width: 100%; max-width: 800px;
    }}
    .nav-brand, .brand {{ font-size: 20px; font-weight: 700; color: #e17055; text-decoration: none; }}

    .main-content {{ margin-top: 56px; padding: 10px 0 12px; display: flex; flex-direction: column; gap: 8px; }}

    .hero {{ text-align: center; }}
    .hero h1 {{ font-size: 22px; font-weight: 800; color: #2d3436; line-height: 28px; margin: 0; letter-spacing: -0.3px; }}
    .hero p {{ font-size: 13px; color: #868e96; max-width: 420px; margin: 4px auto 0; line-height: 18px; }}

    .search-card {{
        background: #fff; border-radius: 10px; padding: 12px;
        border: 1px solid #e9ecef;
    }}
    .search-card div[data-testid="stForm"] {{ border: none; padding: 0; }}
    .search-card div[data-testid="stForm"] > div {{ gap: 0; }}

    .form-grid {{ display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }}
    .form-col {{ display: flex; flex-direction: column; gap: 8px; }}

    div[data-testid="stForm"] > div {{ padding: 0 !important; gap: 0 !important; }}
    .stSelectbox label, .stTextInput label {{ font-size: 12px !important; font-weight: 600 !important; color: #495057 !important; padding: 0 !important; line-height: 1.2 !important; min-height: 0 !important; }}
    div[data-testid="stSelectbox"] > div, div[data-testid="stTextInput"] > div {{ margin: 0 !important; padding: 0 !important; }}
    div[data-testid="stSelectbox"] > div > div, div[data-testid="stTextInput"] > div > div > input {{
        border: 1px solid #dee2e6 !important; border-radius: 6px !important;
        background: #fff !important; font-size: 13px !important; font-family: inherit !important;
        padding: 5px 10px !important; min-height: 30px !important; height: auto !important;
        line-height: 1.3 !important;
    }}
    div[data-testid="stSelectbox"] > div > div {{ padding-right: 34px !important; }}
    div[data-testid="stSelectbox"] > div > div:focus-within, div[data-testid="stTextInput"] > div > div > input:focus {{
        border-color: #e17055 !important; box-shadow: 0 0 0 3px rgba(225,112,85,0.1) !important;
    }}

    .stSelectbox, .stTextInput {{ padding: 0 !important; margin: 0 !important; }}
    div[data-testid="stVerticalBlock"] > div {{ gap: 0 !important; }}
    .row-widget.stSelectbox, .row-widget.stTextInput {{ margin: 0 !important; padding: 0 !important; }}
    div[data-testid="element-container"] {{ padding: 0 !important; margin: 0 !important; }}
    div.stForm {{ border: none !important; padding: 0 !important; background: none !important; }}

    .form-field {{ display: flex; flex-direction: column; gap: 4px; }}

    .btn-search button {{
        width: 100% !important; background: #e17055 !important; color: #fff !important;
        border: none !important; border-radius: 6px !important;
        padding: 7px 16px !important; font-size: 14px !important;
        font-weight: 600 !important; font-family: inherit !important;
        transition: background 0.2s, transform 0.15s !important;
        display: flex !important; align-items: center !important; justify-content: center !important; gap: 5px !important;
    }}
    .btn-search button:hover {{ background: #d35400 !important; }}
    .btn-search button:active {{ transform: scale(0.97) !important; }}
    .btn-search button:disabled {{ opacity: 0.5; }}
    .btn-search button::before {{
        font-family: 'Material Symbols Outlined';
        content: "search";
        font-size: 18px;
        font-weight: 400;
    }}

    .loading-skeleton {{
        display: flex; flex-direction: column; gap: 16px;
        animation: fadeIn 0.2s ease;
    }}

    .skeleton-card {{
        display: flex; background: #fff; border-radius: 12px; overflow: hidden;
        border: 1px solid #e9ecef;
    }}

    .skeleton-image {{
        width: 180px; min-height: 140px; flex-shrink: 0;
        background: #eee;
        background-image: linear-gradient(90deg, #eee 0%, #f5f5f5 40%, #eee 80%);
        background-size: 200% 100%;
        animation: shimmer 1.4s ease infinite;
    }}

    .skeleton-body {{
        flex: 1; padding: 14px 16px;
        display: flex; flex-direction: column; gap: 10px;
    }}

    .skeleton-line {{
        height: 14px; border-radius: 6px;
        background: #eee;
        background-image: linear-gradient(90deg, #eee 0%, #f5f5f5 40%, #eee 80%);
        background-size: 200% 100%;
        animation: shimmer 1.4s ease infinite;
    }}

    .skeleton-line.w-60 {{ width: 60%; }}
    .skeleton-line.w-40 {{ width: 40%; }}
    .skeleton-line.w-80 {{ width: 80%; }}

    @@keyframes shimmer {{
        0% {{ background-position: 200% 0; }}
        100% {{ background-position: -200% 0; }}
    }}

    .state-card {{
        background: #f8f9fa; border-radius: 12px; padding: 20px;
        text-align: center; border: 1px solid #e9ecef;
    }}
    .state-card h3 {{ font-size: 17px; font-weight: 600; color: #2d3436; margin-bottom: 4px; }}
    .state-card p {{ font-size: 14px; color: #868e96; margin: 0; }}

    .results-section {{ display: flex; flex-direction: column; gap: 12px; }}
    .results-header {{
        display: flex; align-items: center; justify-content: space-between;
        border-bottom: 1px solid #e9ecef; padding-bottom: 6px; flex-wrap: wrap; gap: 6px;
    }}
    .results-header h2 {{ font-size: 15px; font-weight: 500; color: #868e96; margin: 0; }}
    .results-header h2 strong {{ font-weight: 700; color: #2d3436; }}
    .candidates-count {{ font-size: 13px; font-weight: 400; color: #868e96; }}

    .fallback-banner {{
        background: #fff3e0; border: 1px solid #ffcc80;
        border-radius: 8px; padding: 8px 14px;
        font-size: 13px; color: #e65100; font-weight: 500; width: 100%;
    }}

    .results-summary {{ font-size: 14px; color: #868e96; font-style: italic; margin: 0; }}

    .card-list {{ display: flex; flex-direction: column; gap: 16px; }}

    .result-card {{
        background: #fff; border-radius: 12px; overflow: hidden;
        border: 1px solid #e9ecef; display: flex;
        transition: box-shadow 0.25s ease, transform 0.25s ease;
        animation: slideUp 0.35s ease both;
    }}
    .result-card:hover {{ box-shadow: 0 4px 16px rgba(0,0,0,0.06); transform: translateY(-2px); }}

    @@keyframes slideUp {{
        from {{ opacity: 0; transform: translateY(10px); }}
        to {{ opacity: 1; transform: translateY(0); }}
    }}

    @@keyframes fadeIn {{
        from {{ opacity: 0; }}
        to {{ opacity: 1; }}
    }}

    .card-image {{
        width: 180px; min-height: 140px; flex-shrink: 0; overflow: hidden;
    }}
    .card-image img {{ width: 100%; height: 100%; object-fit: cover; display: block; transition: transform 0.4s ease; }}

    .result-card:hover .card-image img {{ transform: scale(1.06); }}

    .card-body {{ padding: 14px 16px; flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }}
    .card-top {{ display: flex; justify-content: space-between; align-items: flex-start; gap: 8px; }}
    .card-title {{ font-size: 17px; font-weight: 700; color: #2d3436; line-height: 22px; margin: 0; }}

    .rating-badge {{
        display: flex; align-items: center; gap: 2px;
        background: #00b894; color: #fff;
        padding: 2px 8px; border-radius: 5px;
        font-size: 11px; font-weight: 600; white-space: nowrap; flex-shrink: 0;
    }}

    .card-meta {{ font-size: 13px; color: #868e96; display: flex; gap: 6px; flex-wrap: wrap; }}
    .card-cuisines {{ font-size: 13px; font-style: italic; color: #636e72; margin: 0; }}
    .card-explanation {{ font-size: 13px; color: #636e72; line-height: 20px; margin: 0; }}
    .card-tags {{ display: flex; flex-wrap: wrap; gap: 6px; }}
    .tag {{
        display: inline-block; padding: 2px 10px; border-radius: 9999px;
        font-size: 11px; font-weight: 500; background: #f8f9fa; color: #636e72; border: 1px solid #dee2e6;
    }}

    .footer {{
        padding: 12px 24px; margin-top: 12px;
        border-top: 1px solid #e9ecef;
        background: #f8f9fa; text-align: center;
    }}
    .footer-inner {{ max-width: 800px; margin: 0 auto; }}
    .footer-brand {{ font-size: 14px; font-weight: 700; color: #e17055; margin-bottom: 2px; }}
    .footer-copy {{ font-size: 11px; color: #868e96; margin: 0; }}

    @@media (max-width: 768px) {{
        .navbar {{ height: 44px; padding: 0 12px; }}
        .main-content {{ margin-top: 52px; padding: 8px 0 12px; gap: 8px; }}
        .hero h1 {{ font-size: 18px; line-height: 24px; }}
        .hero p {{ font-size: 12px; }}
        .form-grid {{ grid-template-columns: 1fr; }}
        .result-card {{ flex-direction: column; }}
        .card-image {{ width: 100%; height: 100px; }}
        .search-card {{ padding: 10px; }}
        .form-col {{ gap: 6px; }}
    }}
</style>
""", unsafe_allow_html=True)

st.markdown("""
<div class="navbar">
    <div class="navbar-inner">
        <a href="#" class="brand">RestaurantAI</a>
    </div>
</div>
""", unsafe_allow_html=True)

st.markdown('<div class="main-content">', unsafe_allow_html=True)

st.markdown("""
<div class="hero">
    <h1>RestaurantAI</h1>
    <p>Tell us what you're looking for and get personalized recommendations powered by AI.</p>
</div>
""", unsafe_allow_html=True)


@st.cache_data(ttl=300)
def load_metadata():
    try:
        resp = requests.get(f"{API_BASE_URL}/api/v1/metadata", timeout=10)
        resp.raise_for_status()
        data = resp.json()
        return data.get("cities", []), data.get("cuisines", [])
    except requests.RequestException:
        return [], []


def get_recommendations(location, cuisine, budget, min_rating, extra_prefs, top_k):
    payload = {"minRating": 0.0, "topK": top_k}
    if location and location != "Any": payload["location"] = location
    if cuisine and cuisine != "Any": payload["cuisine"] = cuisine
    if budget and budget != "Any": payload["budget"] = budget.upper()
    if min_rating and min_rating != "Any": payload["minRating"] = float(min_rating)
    if extra_prefs: payload["additionalPreferences"] = extra_prefs

    try:
        resp = requests.post(f"{API_BASE_URL}/api/v1/recommend", json=payload, timeout=60)
        resp.raise_for_status()
        return resp.json(), None
    except requests.exceptions.Timeout:
        return None, "The recommendation engine is taking longer than expected. Please try again."
    except requests.exceptions.ConnectionError:
        return None, "Cannot connect to the backend. Make sure the server is running."
    except requests.RequestException as e:
        return None, f"Request failed: {e}"


cities, cuisines = load_metadata()

st.markdown('<div class="search-card">', unsafe_allow_html=True)
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
    submitted = st.form_submit_button("Find Restaurants", type="primary", use_container_width=True)
    st.markdown('</div>', unsafe_allow_html=True)
st.markdown('</div>', unsafe_allow_html=True)

if submitted:
    with st.spinner(""):
        st.markdown("""
        <div class="loading-skeleton">
            <div class="skeleton-card">
                <div class="skeleton-image"></div>
                <div class="skeleton-body">
                    <div class="skeleton-line w-60"></div>
                    <div class="skeleton-line w-40"></div>
                    <div class="skeleton-line w-80"></div>
                </div>
            </div>
            <div class="skeleton-card">
                <div class="skeleton-image"></div>
                <div class="skeleton-body">
                    <div class="skeleton-line w-60"></div>
                    <div class="skeleton-line w-40"></div>
                    <div class="skeleton-line w-80"></div>
                </div>
            </div>
            <div class="skeleton-card">
                <div class="skeleton-image"></div>
                <div class="skeleton-body">
                    <div class="skeleton-line w-60"></div>
                    <div class="skeleton-line w-40"></div>
                    <div class="skeleton-line w-80"></div>
                </div>
            </div>
        </div>
        """, unsafe_allow_html=True)
        result, error = get_recommendations(location, cuisine, budget, min_rating, extra_prefs, top_k)

    if error:
        st.markdown(f'<div class="state-card"><h3>Something went wrong</h3><p>{error}</p></div>', unsafe_allow_html=True)
        st.stop()
    if result is None:
        st.stop()

    recs = result.get("recommendations", [])
    summary = result.get("summary", "")
    candidates = result.get("candidatesConsidered", 0)
    used_fallback = result.get("usedFallback", False)

    st.markdown('<div class="results-section">', unsafe_allow_html=True)

    st.markdown(f"""
    <div class="results-header">
        <h2>Found <strong>{len(recs)}</strong> recommendation{'s' if len(recs) != 1 else ''} <span class="candidates-count">({candidates} candidate{'s' if candidates != 1 else ''})</span></h2>
    </div>
    """, unsafe_allow_html=True)

    if used_fallback:
        st.markdown('<div class="fallback-banner">⚠️ AI ranking unavailable — showing top-rated results as a fallback.</div>', unsafe_allow_html=True)
    if summary:
        st.markdown(f'<p class="results-summary">{summary}</p>', unsafe_allow_html=True)
    if not recs:
        st.markdown('<div class="state-card"><h3>No recommendations found</h3><p>Try adjusting your filters for more results.</p></div>', unsafe_allow_html=True)
        st.stop()

    st.markdown('<div class="card-list">', unsafe_allow_html=True)
    for idx, r in enumerate(recs):
        rating = r.get("rating", 0)
        cost = r.get("costForTwo")
        city = r.get("city", "")
        img_url = FOOD_IMAGES[idx % len(FOOD_IMAGES)]
        tags_html = ""
        if r.get("tags"):
            tags_html = '<div class="card-tags">' + "".join(f'<span class="tag">{t}</span>' for t in r["tags"]) + "</div>"

        meta = []
        if cost is not None:
            meta.append(f'₹{cost}')
        if city:
            meta.append(f'📍 {city}')

        st.markdown(f"""
        <div class="result-card" style="animation-delay: {round(idx * 0.06, 3)}s">
            <div class="card-image"><img src="{img_url}" alt="" loading="lazy" /></div>
            <div class="card-body">
                <div class="card-top">
                    <h3 class="card-title">{r['restaurantName']}</h3>
                    <div class="rating-badge">{rating} ★</div>
                </div>
                <div class="card-meta">{" ".join(meta)}</div>
                <p class="card-cuisines">{", ".join(r.get("cuisines", []))}</p>
                <p class="card-explanation">{r.get("explanation", "")}</p>
                {tags_html}
            </div>
        </div>
        """, unsafe_allow_html=True)
    st.markdown('</div>', unsafe_allow_html=True)
    st.markdown('</div>', unsafe_allow_html=True)

st.markdown('</div>', unsafe_allow_html=True)

st.markdown("""
<div class="footer">
    <div class="footer-inner">
        <p class="footer-brand">RestaurantAI</p>
        <p class="footer-copy">© 2026 RestaurantAI. All rights reserved.</p>
    </div>
</div>
""", unsafe_allow_html=True)
