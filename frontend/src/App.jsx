import { useState, useEffect } from 'react';
import { fetchMetadata, fetchRecommendations } from './api';
import './App.css';

const RATINGS = ['Any', '3.0', '3.5', '4.0', '4.5'];
const TOP_K = ['3', '5', '10'];
const BUDGETS = [
  { value: '', label: 'Any Budget' },
  { value: 'LOW', label: 'Low (up to ₹500)' },
  { value: 'MEDIUM', label: 'Medium (₹500 – ₹1500)' },
  { value: 'HIGH', label: 'High (₹1500+)' },
];

function App() {
  const [metadata, setMetadata] = useState({ cities: [], cuisines: [], ready: false });
  const [form, setForm] = useState({
    location: '', cuisine: '', budget: '', minRating: '4.0', topK: '5', additionalPreferences: '',
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
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResults(null);

    const body = {
      location: form.location || undefined,
      cuisine: form.cuisine || undefined,
      budget: form.budget || undefined,
      minRating: parseFloat(form.minRating) || 0,
      topK: parseInt(form.topK) || 5,
      additionalPreferences: form.additionalPreferences || undefined,
    };

    try {
      const data = await fetchRecommendations(body);
      setResults(data);
    } catch (err) {
      setError('Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container">
      <header>
        <h1>Restaurant Recommender</h1>
        <p className="subtitle">Find the perfect restaurant for your next meal</p>
      </header>

      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="location">Location</label>
              <select id="location" name="location" value={form.location} onChange={handleChange} className="form-control">
                <option value="">Any Location</option>
                {metadata.cities.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="cuisine">Cuisine</label>
              <select id="cuisine" name="cuisine" value={form.cuisine} onChange={handleChange} className="form-control">
                <option value="">Any Cuisine</option>
                {metadata.cuisines.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="budget">Budget</label>
              <select id="budget" name="budget" value={form.budget} onChange={handleChange} className="form-control">
                {BUDGETS.map(b => <option key={b.value} value={b.value}>{b.label}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="minRating">Minimum Rating</label>
              <select id="minRating" name="minRating" value={form.minRating} onChange={handleChange} className="form-control">
                {RATINGS.map(r => <option key={r} value={r}>{r === 'Any' ? 'Any Rating' : r + '+'}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="topK">Results</label>
              <select id="topK" name="topK" value={form.topK} onChange={handleChange} className="form-control">
                {TOP_K.map(k => <option key={k} value={k}>{k}</option>)}
              </select>
            </div>

            <div className="form-group full-width">
              <label htmlFor="additionalPreferences">Extra Preferences</label>
              <input type="text" id="additionalPreferences" name="additionalPreferences"
                value={form.additionalPreferences} onChange={handleChange}
                className="form-control" placeholder="e.g., family-friendly, vegetarian, good for dates" />
            </div>
          </div>

          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Finding...' : '🔍 Find Restaurants'}
          </button>
        </form>
      </div>

      {loading && (
        <div className="loading">
          <div className="spinner"></div>
          <p>Finding the best restaurants for you...</p>
        </div>
      )}

      {error && (
        <div className="card empty-state">
          <h2>Error</h2>
          <p>{error}</p>
        </div>
      )}

      {results && results.recommendations.length === 0 && (
        <div className="card empty-state">
          <h2>No Recommendations</h2>
          <p>{results.summary}</p>
        </div>
      )}

      {results && results.recommendations.length > 0 && (
        <>
          <div className="results-header">
            <p className="summary">{results.summary}</p>
            <p className="candidates-count">Considered {results.candidatesConsidered} restaurants</p>
            {results.usedFallback && <p className="fallback-note">(Ranked by rating — AI unavailable)</p>}
          </div>
          <div className="recommendations-grid">
            {results.recommendations.map(rec => (
              <div key={rec.rank} className="recommendation-card">
                <div className="card-rank">#{rec.rank}</div>
                <div className="card-body">
                  <h3 className="card-title">{rec.restaurantName}</h3>
                  <div className="card-meta">
                    <span className="badge badge-rating">{rec.rating} ★</span>
                    {rec.costForTwo != null && <span className="badge badge-cost">₹{rec.costForTwo}</span>}
                    <span className="badge badge-location">{rec.city}</span>
                  </div>
                  <p className="card-location">{rec.location}</p>
                  <p className="card-cuisines">{rec.cuisines.join(', ')}</p>
                  <p className="card-explanation">{rec.explanation}</p>
                  {rec.tags && rec.tags.length > 0 && (
                    <div className="card-tags">
                      {rec.tags.map(tag => <span key={tag} className="tag">{tag}</span>)}
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}

export default App;
