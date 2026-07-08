# Phase 5 Evaluation Criteria: Frontend

This document details the evaluation criteria, verification steps, and quality gates for **Phase 5: Frontend**.

---

## 1. Objectives

Verify that the user interface (Thymeleaf + HTMX or React SPA) collects user preferences, interacts with backend APIs, manages waiting states visually, handles empty or error states gracefully, and displays structured restaurant metadata alongside clear AI explanations.

---

## 2. Key Metrics & Baselines

| Metric | Target Baseline | Verification Method |
| :--- | :--- | :--- |
| **First Contentful Paint** | `< 1 second` (local) | Browser Performance tab |
| **Input Validation** | `100%` client-side validation rate | Test submitting empty mandatory forms |
| **Loading Indication** | Displayed immediately upon submit, stays visible | Check visibility of loading spinner during REST call |
| **Response Rendering** | `< 100 milliseconds` (after API returns) | DOM updates performance tracking |
| **Responsive Compatibility** | Correct layout on mobile, tablet, and desktop | Chrome DevTools device mode simulation |

---

## 3. Quality Gates (Must Pass to Advance)

1. **Aesthetic Premium Standards**: The UI must look modern, clean, and professional (avoid default styles, raw blue links, or basic HTML tables). It should use a harmonious color palette, clean typography, and card layouts.
2. **Visual Separation of Concerns**: Fact-based details (name, rating, cost, cuisine, locality) must be visually separated from AI-generated text/explanations to build user trust.
3. **Spinner Visibility**: An animated spinner or skeleton screen must be displayed during the LLM latency period (~3-10s) to reassure the user that the request is processing.
4. **Validation Guard**: The form must check and prevent empty submissions for required fields (City, Cuisine) before triggering requests to the backend.
5. **Short-Circuit Empty Page**: If the filter finds 0 candidate restaurants, the UI must display a helpful message suggesting that the user relax their criteria (e.g., lower the rating threshold or change the budget band), rather than showing a blank screen or a general system crash error.

---

## 4. Automated Verification Steps
Since this is a frontend component, automated tests might include basic selenium/playwright checks (if configured), but the primary automated gates are checking clean compilation:

```bash
# If using React SPA:
cd frontend
npm run build
# Ensure no TypeScript or bundler errors occur

# If using Thymeleaf:
./mvnw compile
# Ensure no syntax or template parsing compilation errors
```

---

## 5. Manual Evaluation Checklist

- [ ] **Metadata Fetching**: Open the app and verify that city and cuisine dropdown values populate dynamically from the `/api/v1/metadata` endpoint.
- [ ] **Form Submission E2E**: Select valid values (e.g. Bangalore, Medium, Italian, 4.0+) and click submit. Ensure cards appear with ratings, cuisines, costs, rank badges, and a custom section for the AI explanation.
- [ ] **Loading State UX**: Submit a request and verify that the search button disables and a clear loading animation is shown immediately.
- [ ] **Error Fallback Rendering**: Shut down the backend or input an invalid LLM key, submit a request, and verify that the UI displays a clean message describing the error (or showing fallback items gracefully) rather than breaking the layout.
- [ ] **XSS Script Injection Safe**: Input `<script>alert('xss')</script>` in the additional preferences textbox. Ensure the template engine escapes the output completely and does not execute the script.

---

## Related Documents
- [Phase 5 Task List](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/implementation-plan.md#phase-5--frontend)
- [System Architecture](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/architecture.md#72-startup--dataset-bootstrap)
