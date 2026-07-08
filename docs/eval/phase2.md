# Phase 2 Evaluation Criteria: Filter & Candidate Truncation

This document details the evaluation criteria, verification steps, and quality gates for **Phase 2: Filter & Candidate Truncation**.

---

## 1. Objectives

Verify that the deterministic filtering layer correctly applies location, cuisine, rating, and budget band constraints using Java streams, and that candidate sets exceeding LLM token thresholds are correctly sorted and truncated.

---

## 2. Key Metrics & Baselines

| Metric | Target Baseline | Verification Method |
| :--- | :--- | :--- |
| **Filtering Latency** | `< 2 milliseconds` (per call on sample set) | JUnit execution duration measurements |
| **Deterministic Output** | `100%` consistency (same input always yields same candidates) | Test assertion loops |
| **Boundary Precision** | `100%` correct budget band mapping | Unit tests on budget boundaries |
| **Branch Coverage** | `> 90%` coverage on filter logic branches | Jacoco report or IDE test runner |

---

## 3. Quality Gates (Must Pass to Advance)

1. **Composition Rule**: Filters must combine with strict logical **AND** behavior.
2. **Correct Budget Inclusions**: Budget band filters must use inclusive boundaries mapping to INR values:
   * `LOW` -> `costForTwo <= 500`
   * `MEDIUM` -> `501 <= costForTwo <= 1500`
   * `HIGH` -> `costForTwo >= 1501`
3. **Deterministic Sorting on Truncation**: When the candidate set size exceeds `maxCandidatesForLlm` (default `25`), the [CandidateTruncationService](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/architecture.md#71-component-catalog) must sort candidates by:
   1. `rating` descending.
   2. `costForTwo` ascending (tie-breaker: lower cost is preferred).
   3. `name` ascending (tie-breaker: alphabetical sorting).
4. **Empty Set Resilience**: If no candidates match, the system must return an empty list immediately without throwing an exception or returning partial matches.

---

## 4. Automated Verification Steps

```bash
# 1. Run filter and truncation service unit tests
./mvnw test -Dtest=RestaurantFilterServiceTest,CandidateTruncationServiceTest

# 2. Compile and run build validation
./mvnw clean test
```

---

## 5. Manual Evaluation Checklist

- [ ] **String Normalization**: Verify that queries for location/city are case-insensitive and trim extra whitespaces (e.g. `" bangalore"` matches `"Bangalore"`).
- [ ] **Unknown Cost Ingestion Option**: Verify how the system handles `null` values for `costForTwo` in budget bands (confirm whether they are excluded or default-included and that the decision is documented).
- [ ] **Truncation Cap Enforcement**: Set `maxCandidatesForLlm` to `3` in `application.yml`, verify that filtering a set that would return `10` candidates correctly returns exactly the `3` top-rated, lowest-cost candidates.
- [ ] **Empty Result Short-Circuit**: Verify that if the filter output count is `0`, subsequent orchestrator calls are skipped to prevent redundant LLM invocations.

---

## Related Documents
- [Phase 2 Task List](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/implementation-plan.md#phase-2--filter--candidate-truncation)
- [System Architecture](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/architecture.md#73-restaurantfilterservice)
