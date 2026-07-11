import base64
import streamlit as st
from pathlib import Path

st.set_page_config(page_title="RestaurantAI", page_icon="🍽️", layout="wide")

BASE = Path(__file__).resolve().parent / "src" / "main" / "resources" / "static"

css_files = list((BASE / "assets").glob("*.css"))
js_files = list((BASE / "assets").glob("*.js"))

if not css_files or not js_files:
    st.error("React build not found. Run `cd frontend && npm install && npm run build` first.")
    st.stop()

html = (BASE / "index.html").read_text(encoding="utf-8")
css = css_files[0].read_text(encoding="utf-8")
js = js_files[0].read_text(encoding="utf-8")

html = html.replace(f'<link rel="stylesheet" crossorigin href="/assets/{css_files[0].name}">', f"<style>{css}</style>")

js_b64 = base64.b64encode(js.encode()).decode()
html = html.replace(f'<script type="module" crossorigin src="/assets/{js_files[0].name}"></script>', f'<script type="module" src="data:text/javascript;base64,{js_b64}"></script>')

html = html.replace('<link rel="icon" type="image/svg+xml" href="/vite.svg" />', "")

st.html(html)
