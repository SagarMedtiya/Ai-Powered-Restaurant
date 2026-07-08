# Phase 6 Evaluation Criteria: Hardening, Testing & Deployment

This document details the evaluation criteria, verification steps, and quality gates for **Phase 6: Hardening, Testing & Deployment**.

---

## 1. Objectives

Verify that the application is hardened for production deployment: achieving high test coverage, logging events systematically, collecting core system metrics, packaging clean Docker images, and maintaining robust security practices.

---

## 2. Key Metrics & Baselines

| Metric | Target Baseline | Verification Method |
| :--- | :--- | :--- |
| **Service Code Coverage** | `> 80%` instruction & branch coverage | Jacoco test report |
| **P95 Latency (Mock LLM)** | `< 50 milliseconds` | Load tests / JMeter |
| **P95 Latency (Real LLM)** | `< 12 seconds` (model dependent) | Real E2E requests profiling |
| **Docker Image Size** | `< 250 MB` (with Alpine JRE base) | `docker images` size print |
| **Secrets Exposure** | `0` API keys committed in source control | Static code scanner (e.g. GitLeaks) |

---

## 3. Quality Gates (Must Pass to Advance)

1. **Test Verification**: `./mvnw clean verify` must compile, run, and pass all tests including slice tests and integration tests.
2. **Docker Portability**: The Docker image must build successfully using `Dockerfile` and start the server correctly using `docker compose up`.
3. **Secrets Invariant**: The LLM API key must *never* be hardcoded or written to configuration files. It must be injected at runtime via system environment variables (`LLM_API_KEY`).
4. **Structured Ingestion Logging**: In the `prod` profile, logs must print in structured JSON format with correlation IDs to track requests across thread boundaries.
5. **Observability Indicators**: Standard Micrometer metrics must track:
   * `recommendation.requests.total`: total endpoint invocations
   * `recommendation.filter.candidates`: candidate distribution count
   * `recommendation.llm.latency`: duration of LLM calls
   * `recommendation.llm.fallback.total`: number of times fallback ranking ran
   * `recommendation.validator.dropped.total`: count of hallucinated items dropped

---

## 4. Automated Verification Steps

```bash
# 1. Run full verification suite (unit + integration tests)
./mvnw clean verify

# 2. Build Docker container image
docker build -t restaurant-recommender:v1 .

# 3. Test running the Docker container locally
docker run -p 8080:8080 -e LLM_API_KEY="your-api-key" restaurant-recommender:v1 &
# Wait for server startup log...

# 4. Check docker container health status
curl -i http://localhost:8080/actuator/health
```

---

## 5. Manual Evaluation Checklist

- [ ] **Docker Cache Check**: Confirm that a volume path exists in `docker-compose.yml` mapping `./data/cache/` so downloaded CSV files are cached outside the container.
- [ ] **Heap Size Configuration**: Check that the JVM startup flags inside the Dockerfile specify heap configurations (`-XX:+UseG1GC`, `-Xms512m`, `-Xmx1g`) to manage memory.
- [ ] **Production Config Audit**: Confirm that `application-prod.yml` disables debug endpoints and limits actuator exposures to health and info only.
- [ ] **Documentation Completeness**: Verify the root `README.md` includes clear instructions for local maven starts, environment variable injection, and Docker execution commands.

---

## Related Documents
- [Phase 6 Task List](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/implementation-plan.md#phase-6--hardening-testing--deployment)
- [System Architecture](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/architecture.md#15-deployment-architecture)
