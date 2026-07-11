const API_BASE = import.meta.env.VITE_API_BASE_URL || '';

export async function fetchMetadata() {
  const res = await fetch(`${API_BASE}/api/v1/metadata`);
  if (!res.ok) throw new Error('Failed to fetch metadata');
  return res.json();
}

export async function fetchRecommendations(params) {
  const res = await fetch(`${API_BASE}/api/v1/recommend`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params),
  });
  if (!res.ok) throw new Error('Failed to fetch recommendations');
  return res.json();
}
