import { useState, useEffect } from 'react';
import { fetchMetadata, fetchRecommendations } from './api';
import './App.css';

const RATINGS = ['Any', '3.0', '3.5', '4.0', '4.5'];
const TOP_KS = [3, 5, 10];
const BUDGETS = ['Any', 'Low', 'Medium', 'High'];

const FOOD_IMAGES = [
  'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1551782450-a2132b4ba21d?w=400&h=300&fit=crop',
];

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
          <a href="#" className="brand">RestaurantAI</a>
        </div>
      </nav>

      <main className="main-content">
        <header className="hero">
          <h1 className="hero-title">RestaurantAI</h1>
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
            <h3>Something went wrong</h3>
            <p>{error}</p>
          </div>
        )}

        {results && results.recommendations.length === 0 && (
          <div className="state-card empty-state">
            <h3>No recommendations found</h3>
            <p>Try adjusting your filters for more results.</p>
          </div>
        )}

        {results && results.recommendations.length > 0 && (
          <section className="results-section">
            <div className="results-header">
              <h2>
                Found <strong>{results.recommendations.length}</strong> recommendation{results.recommendations.length > 1 ? 's' : ''}
                <span className="candidates-count"> ({results.candidatesConsidered} candidate{results.candidatesConsidered !== 1 ? 's' : ''})</span>
              </h2>
              {results.usedFallback && (
                <div className="fallback-banner">
                  AI ranking unavailable — showing top-rated results as a fallback.
                </div>
              )}
            </div>

            {results.summary && <p className="results-summary">{results.summary}</p>}

            <div className="card-list">
              {results.recommendations.map((r, idx) => (
                <article key={r.rank} className="result-card" style={{ animationDelay: `${idx * 0.06}s` }}>
                  <div className="card-image">
                    <img
                      src={FOOD_IMAGES[idx % FOOD_IMAGES.length]}
                      alt={r.restaurantName}
                      loading="lazy"
                    />
                  </div>
                  <div className="card-body">
                    <div className="card-top">
                      <h3 className="card-title">{r.restaurantName}</h3>
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
              ))}
            </div>
          </section>
        )}
      </main>

      <footer className="footer">
        <div className="footer-inner">
          <p className="footer-brand">RestaurantAI</p>
          <p className="footer-copy">© 2026 RestaurantAI. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}

export default App;
