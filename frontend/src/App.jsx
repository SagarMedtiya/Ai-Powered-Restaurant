import { useState, useEffect } from 'react';
import { fetchMetadata, fetchRecommendations } from './api';
import './App.css';

const RATINGS = ['Any', '3.0', '3.5', '4.0', '4.5'];
const TOP_KS = [3, 5, 10];
const BUDGETS = ['Any', 'Low', 'Medium', 'High'];

const FOOD_IMAGES = [
  'https://lh3.googleusercontent.com/aida-public/AB6AXuCX7dbEjC7CrB4wX-SvOe23SKn9SzxYa4RrYGHxhSaFEfogKqsCxp636UPuJxNUwiaGnNrKcJZnYP1Znl-7hH924xFjXwLlEH_B8frh3xTQ_xUmjkLU7PA_qHbLXbGw9Jh57y1I0_ab_rGECwHMPOa4ayk8p-dzYmoxNviXmRonr1lThfjB6qOSridQGK3clJG01JtoVAsERKYuiRHDZ3o3W_q0wLox_5D-t14YrxJTtzFF1lolIoYfnOFafTngJc-SNDp1onQQ-A',
  'https://lh3.googleusercontent.com/aida-public/AB6AXuCxSYY0iKHtqoglVudGEV1LUmVPZD4ENwGElgdTE7WtBeVeJA_I0PHH38QrU1Q8M_Ri_FAIImvpDPqKaNZ_ftU4srT_JknWkKxDVq3Ff3Pt7DUBK8G5DMgcsOaKBHZcU4T1uNYOvPjdWbLJQx7StDtd2z-9uq3lyO9gWvuLQ4I4A4cbbvn51POsV3QrQsd2Ky5zoxTKWHwIL_C6w0h0oDVJ3SYRFhAtuByhhVQE1kDIhSLzmf-z3g6RIqIAo3Mnu4qqPRrpGmkIzw',
  'https://lh3.googleusercontent.com/aida-public/AB6AXuCCGTHUJWTQtJFBWD7gX_w0dNm5vG0PWF_vBJ_VKDlgQpKru_2j7-rK9OR0FC3BS1qLtqNs8fQbQ5HwLE5scPQcdVmgXmIdFFdCRT6eCnT5v9q27TQ7n2Ak4aqQL0qrrI2o46n0vF_nG7PY-1K3BQp5Fl12gZ5_p4NMGDLYnKMZRCgh-_kVU6YQK0OlgGMUFvA3puLqZ0vUf0bG-vX5D_QCOnqjQMGJ7FBgrRtjgVVdMmYNhSi3nMnSrxu7jz3CWQ5cOqQrLQ',
  'https://lh3.googleusercontent.com/aida-public/AB6AXuCMrESx0St0WBEs1Ghs-3tPZgHkloHq1Z8tcMqZ17F1eWUZl1k7aO1tBF6l6ln0AYMYCH2vDSd-HLLeCxYTF7WqBcNufr-CLISJRPuHeKzWzJ3tPwHakxJHgLkxBFsfWSAM8FOYw_PNKtMP9QvEr8pLXsFszz9EBDxbYqVqW-mQAU4B8OsERGNzGpxKDOsJEqf8YY3Qp2r2R_2ys0BKBJqKEP-LGGDlKD5zCwAblhBH0NlGR81NQnBn7pU5k5TmYQ8fS5OZJQ',
];

const RANK_MEDALS = { 1: '🥇', 2: '🥈', 3: '🥉' };
const RANK_ACCENT = { 1: '#ffd700', 2: '#c0c0c0', 3: '#cd7f32' };

function SkeletonCard() {
  return (
    <div className="skeleton-card">
      <div className="skeleton-image" />
      <div className="skeleton-body">
        <div className="skeleton-line w-60" />
        <div className="skeleton-line w-40" />
        <div className="skeleton-line w-80" />
      </div>
    </div>
  );
}

function App() {
  const [metadata, setMetadata] = useState({ cities: [], cuisines: [], ready: false });
  const [form, setForm] = useState({
    location: 'Any',
    cuisine: 'Any',
    budget: 'Any',
    minRating: 'Any',
    topK: 5,
    additionalPreferences: '',
  });
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchMetadata()
      .then(data => { if (data.ready) setMetadata(data); })
      .catch(() => {});
  }, []);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: name === 'topK' ? Number(value) : value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResults(null);

    const body = {};
    if (form.location && form.location !== 'Any') body.location = form.location;
    if (form.cuisine && form.cuisine !== 'Any') body.cuisine = form.cuisine;
    if (form.budget && form.budget !== 'Any') body.budget = form.budget.toUpperCase();
    if (form.minRating && form.minRating !== 'Any') body.minRating = parseFloat(form.minRating);
    else body.minRating = 0.0;
    body.topK = form.topK;
    if (form.additionalPreferences?.trim()) body.additionalPreferences = form.additionalPreferences.trim();

    try {
      const data = await fetchRecommendations(body);
      setResults(data);
    } catch {
      setError('Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="app">
      <nav className="navbar">
        <div className="navbar-inner">
          <a href="#" className="brand">CraveAI</a>
          <div className="nav-links">
            <a href="#" className="nav-link active">Discover</a>
            <a href="#" className="nav-link">Favorites</a>
            <a href="#" className="nav-link">History</a>
          </div>
          <button className="btn-signin">Sign In</button>
        </div>
      </nav>

      <main className="main-content">
        <header className="hero">
          <div className="hero-bg" />
          <div className="hero-icon">🍽️</div>
          <h1 className="hero-title">AI-Powered Restaurant Recommender</h1>
          <p className="hero-subtitle">
            Tell us what you're looking for and get personalized recommendations powered by AI.
          </p>
        </header>

        <section className="search-section">
          <form className="search-form" onSubmit={handleSubmit}>
            <div className="form-grid">
              <div className="form-col">
                <div className="form-field">
                  <label>Location</label>
                  <select name="location" value={form.location} onChange={handleChange}>
                    <option>Any</option>
                    {metadata.cities.map(c => <option key={c}>{c}</option>)}
                  </select>
                </div>
                <div className="form-field">
                  <label>Cuisine</label>
                  <select name="cuisine" value={form.cuisine} onChange={handleChange}>
                    <option>Any</option>
                    {metadata.cuisines.map(c => <option key={c}>{c}</option>)}
                  </select>
                </div>
                <div className="form-field">
                  <label>Budget</label>
                  <select name="budget" value={form.budget} onChange={handleChange}>
                    {BUDGETS.map(b => <option key={b}>{b}</option>)}
                  </select>
                </div>
              </div>
              <div className="form-col">
                <div className="form-field">
                  <label>Min Rating</label>
                  <select name="minRating" value={form.minRating} onChange={handleChange}>
                    {RATINGS.map(r => <option key={r}>{r}</option>)}
                  </select>
                </div>
                <div className="form-field">
                  <label>Results count</label>
                  <select name="topK" value={form.topK} onChange={handleChange}>
                    {TOP_KS.map(k => <option key={k} value={k}>{k} Recommendations</option>)}
                  </select>
                </div>
                <div className="form-field">
                  <label>Extra Preferences</label>
                  <input
                    type="text"
                    name="additionalPreferences"
                    value={form.additionalPreferences}
                    onChange={handleChange}
                    placeholder="e.g. family-friendly, vegetarian"
                  />
                </div>
              </div>
            </div>
            <button type="submit" className="btn-submit" disabled={loading}>
              <span className="material-symbols-outlined">search</span>
              Find Restaurants
            </button>
          </form>
        </section>

        {loading && (
          <div className="loading-skeleton">
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
          </div>
        )}

        {error && (
          <div className="state-card error-state">
            <span className="material-symbols-outlined state-icon">error</span>
            <h3>Something went wrong</h3>
            <p>{error}</p>
          </div>
        )}

        {results && results.recommendations.length === 0 && (
          <div className="state-card empty-state">
            <span className="material-symbols-outlined state-icon">search_off</span>
            <h3>No recommendations found</h3>
            <p>Try adjusting your filters for more results.</p>
          </div>
        )}

        {results && results.recommendations.length > 0 && (
          <section className="results-section">
            <div className="results-header">
              <h2>
                Found <strong>{results.recommendations.length}</strong> recommendation{results.recommendations.length > 1 ? 's' : ''}
                <span className="candidates-count"> (from {results.candidatesConsidered} candidate{results.candidatesConsidered !== 1 ? 's' : ''})</span>
              </h2>
              {results.usedFallback && (
                <div className="fallback-banner">
                  <span className="material-symbols-outlined" style={{ fontSize: 16 }}>info</span>
                  AI ranking unavailable — showing top-rated results as a fallback.
                </div>
              )}
            </div>

            {results.summary && <p className="results-summary">{results.summary}</p>}

            <div className="card-list">
              {results.recommendations.map((r, idx) => {
                const accent = RANK_ACCENT[r.rank] || 'transparent';
                const medal = RANK_MEDALS[r.rank];
                return (
                  <article key={r.rank} className="result-card" style={{ animationDelay: `${idx * 0.06}s`, '--accent': accent }}>
                    <div className="card-image">
                      <img
                        src={FOOD_IMAGES[idx % FOOD_IMAGES.length]}
                        alt={r.restaurantName}
                        loading="lazy"
                      />
                    </div>
                    <div className="card-body">
                      <div className="card-top">
                        <h3 className="card-title">
                          {medal && <span className="rank-medal">{medal}</span>}
                          {r.restaurantName}
                        </h3>
                        <div className="rating-badge">{r.rating} ★</div>
                      </div>
                      <div className="card-meta">
                        {r.costForTwo != null && <span>₹{r.costForTwo} for two</span>}
                        {r.city && <span>📍 {r.city}</span>}
                      </div>
                      {r.cuisines?.length > 0 && (
                        <p className="card-cuisines">{r.cuisines.join(', ')}</p>
                      )}
                      {r.explanation && <p className="card-explanation">{r.explanation}</p>}
                      {r.tags?.length > 0 && (
                        <div className="card-tags">
                          {r.tags.map(t => <span key={t} className="tag">{t}</span>)}
                        </div>
                      )}
                    </div>
                  </article>
                );
              })}
            </div>
          </section>
        )}
      </main>

      <footer className="footer">
        <div className="footer-inner">
          <div className="brand">CraveAI</div>
          <div className="footer-links">
            <a href="#">Privacy Policy</a>
            <a href="#">Terms of Service</a>
            <a href="#">Contact Support</a>
          </div>
          <p className="footer-copy">© 2026 CraveAI Recommender. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}

export default App;
