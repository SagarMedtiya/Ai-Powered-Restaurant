# Phase 4 Evaluation Criteria: REST API & Orchestration

This document details the evaluation criteria, verification steps, and quality gates for **Phase 4: REST API & Orchestration**.

---

## 1. Objectives

Verify that the end-to-end backend orchestration pipeline works correctly via HTTP. Verify that request DTOs are validated using Jakarta validation, that errors are mapped to RFC 7807 problem details, and that metadata is correctly served.

---

## 2. Key Metrics & Baselines

| Metric | Target Baseline | Verification Method |
| :--- | :--- | :--- |
| **Orchestration Overhead** | `< 10 milliseconds` (excluding LLM time) | Controller-level performance tracking |
| **HTTP Status Code Mapping**| `100%` compliance with REST standards | Integration test assertions |
| **Validation Error Details**| RFC 7807 structured JSON details | Inspecting 400 response body fields |
| **CORS Access Rules** | Allowed origins match configuration in dev/prod | CORS preflight test headers |

---

## 3. Quality Gates (Must Pass to Advance)

1. **Validation Checks**: Requests containing empty cities, ratings out of `0.0 - 5.0` boundaries, or a `topK` count `< 1` or `> 10` must be rejected with HTTP `400 Bad Request`.
2. **Catalog Readiness**: Requests sent while the catalog is loading must be rejected with HTTP `503 Service Unavailable`, accompanied by an informative body.
3. **Robust Fallback Routing**: If the LLM throws an exception, the REST response must still be HTTP `200 OK` containing fallback recommendations and a flag indicating a fallback was used, rather than returning a `500 Internal Server Error`.
4. **No-Match Short-Circuit**: If the hard filters result in 0 matches, the controller must immediately return HTTP `200 OK` with an empty recommendations array and a helpful guide message in the response payload.

---

## 4. Automated Verification Steps

```bash
# 1. Run controller slice tests and integration tests
./mvnw test -Dtest=RecommendationControllerTest,RecommendationIntegrationTest

# 2. Start app locally in dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev &
# Wait for catalog load...

# 3. Test validation constraint (should return 400)
curl -i -X POST http://localhost:8080/api/v1/recommend \
  -H "Content-Type: application/json" \
  -d '{"location":"","budget":"MEDIUM","cuisine":"Italian","minRating":6.0}'

# 4. Test normal recommendation flow (should return 200)
curl -i -X POST http://localhost:8080/api/v1/recommend \
  -H "Content-Type: application/json" \
  -d '{"location":"Bangalore","budget":"MEDIUM","cuisine":"Italian","minRating":4.0,"topK":3}'

# 5. Fetch metadata lists
curl -i http://localhost:8080/api/v1/metadata
```

---

## 5. Manual Evaluation Checklist

- [ ] **ProblemDetail Format**: Check that validation failures return JSON containing `type`, `title`, `status`, `detail`, and `instance` per RFC 7807.
- [ ] **Metadata Consistency**: Verify that `GET /api/v1/metadata` returns arrays of cities and cuisines derived dynamically from the loaded dataset.
- [ ] **CORS Settings**: Verify that headers like `Access-Control-Allow-Origin` are set correctly when accessing the API from the configured local UI port (e.g. `http://localhost:5173`).
- [ ] **Concurrency Virtual Threads**: If virtual threads are enabled, confirm they handle incoming requests without thread exhaustion issues.

---

## Related Documents
- [Phase 4 Task List](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/implementation-plan.md#phase-4--rest-api--orchestration)
- [System Architecture](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/architecture.md#103-rest-api-boundary)
