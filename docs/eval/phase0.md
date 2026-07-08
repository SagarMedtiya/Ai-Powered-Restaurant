# Phase 0 Evaluation Criteria: Project Setup & Dataset Discovery

This document details the evaluation criteria, verification steps, and quality gates for **Phase 0: Project Setup & Dataset Discovery**.

---

## 1. Objectives

Verify that the Spring Boot project is properly initialized, basic properties are configured, and the Hugging Face Zomato dataset has been analyzed and a development sample extracted.

---

## 2. Key Metrics & Baselines

| Metric | Target Baseline | Verification Method |
| :--- | :--- | :--- |
| **Startup Build Time** | `< 10 seconds` | Maven build execution output |
| **Verification Coverage** | `0%` (skeleton only) | `./mvnw clean verify` execution |
| **Dataset Sample Volume** | `~500 rows` | CSV row count in `restaurants-sample.csv` |
| **Metadata Completeness** | `100%` | Presence of all core mapped columns |

---

## 3. Quality Gates (Must Pass to Advance)

1. **Build Quality**: `./mvnw clean verify` must execute and pass without errors.
2. **Endpoint Availability**: Actuator `/actuator/health` endpoint must return `{"status":"UP"}` when the app is run locally.
3. **Artifact Existence**: The environment template `.env.example` and development sample CSV `restaurants-sample.csv` must be present.
4. **Data Calibrations**: The budget threshold ranges (low/medium/high band definitions) must be justified and written in the schema document.

---

## 4. Automated Verification Steps

```bash
# 1. Clean build the project
./mvnw clean package -DskipTests

# 2. Check main class compile and execution
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev &
# Wait 5 seconds for startup

# 3. Probe the actuator health status
curl -s -f http://localhost:8080/actuator/health | grep -q '"status":"UP"'
```

---

## 5. Manual Evaluation Checklist

- [ ] **Project Structure**: Verify directories `config`, `controller`, `dto`, `domain`, `service`, `repository`, `data`, `client`, and `exception` exist under `com.restaurant.recommender`.
- [ ] **Dataset Schema Documentation**: Review [dataset-schema.md](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/dataset-schema.md) to ensure columns (name, city, location, cuisines, rating, cost) are mapped to target records.
- [ ] **Config Files**: Confirm that `application.yml` uses placeholders referencing environment variables (`LLM_API_KEY`, `DATASET_URL`, `DATASET_CACHE_PATH`).
- [ ] **Git Configuration**: Check that target/ and raw dataset files are correctly ignored in `.gitignore`.

---

## Related Documents
- [Phase 0 Task List](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/implementation-plan.md#phase-0--project-setup--dataset-discovery)
- [System Architecture](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/architecture.md)
