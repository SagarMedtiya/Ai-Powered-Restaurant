import base64
import streamlit as st
from pathlib import Path

st.set_page_config(page_title="RestaurantAI", page_icon="🍽️", layout="wide")

# Hide Streamlit chrome
st.markdown("""
<style>
    header, .stDeployButton, #MainMenu, footer { display: none !important; }
    .stApp { background: #ffffff; }
    .main > .blockContainer { max-width: 100% !important; padding: 0 !important; }
</style>
""", unsafe_allow_html=True)

BASE = Path(__file__).resolve().parent / "src" / "main" / "resources" / "static"

css_files = list((BASE / "assets").glob("*.css"))
js_files = list((BASE / "assets").glob("*.js"))

if not css_files or not js_files:
    st.error("React build not found. Run `cd frontend && npm install && npm run build` first.")
    st.stop()

html = (BASE / "index.html").read_text(encoding="utf-8")
css = css_files[0].read_text(encoding="utf-8")
js = js_files[0].read_text(encoding="utf-8")

# Inline CSS
html = html.replace(
    f'<link rel="stylesheet" crossorigin href="/assets/{css_files[0].name}">',
    f"<style>{css}</style>"
)

# Inline JS via Blob URL to avoid module/data-URI restrictions
js_b64 = base64.b64encode(js.encode()).decode()
inject_script = f"""
<script>
(function() {{
    var blob = new Blob([atob('{js_b64}')], {{ type: 'text/javascript' }});
    var script = document.createElement('script');
    script.type = 'module';
    script.src = URL.createObjectURL(blob);
    document.body.appendChild(script);
}})();
</script>
"""
html = html.replace(
    f'<script type="module" crossorigin src="/assets/{js_files[0].name}"></script>',
    inject_script
)

# Remove favicon link
html = html.replace('<link rel="icon" type="image/svg+xml" href="/vite.svg" />', "")

st.html(html, unsafe_allow_javascript=True)
