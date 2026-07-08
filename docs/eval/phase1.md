# Phase 1 Evaluation Criteria: Data Ingestion & In-Memory Catalog

This document details the evaluation criteria, verification steps, and quality gates for **Phase 1: Data Ingestion & In-Memory Catalog**.

---

## 1. Objectives

Verify that the application successfully downloads (or falls back to local cache), parses, normalizes, and loads the Zomato restaurant data into an in-memory repository at startup, exposing a custom health indicator status based on readiness.

---

## 2. Key Metrics & Baselines

| Metric | Target Baseline | Verification Method |
| :--- | :--- | :--- |
| **Parsing Latency (Sample)** | `< 500 milliseconds` | Application startup logs |
| **Parsing Latency (Full 51k)**| `< 15 seconds` (on standard machines) | Startup logs in `prod` profile |
| **Memory Consumption** | `< 1 GB Heap Allocation` | Actuator metrics / JVM profiling |
| **Data Integrity** | `0` null values in mandatory fields (`id`, `name`, `city`, `location`, `cuisines`) | Unit test assertion |
| **Test Coverage** | `> 80%` on normalizer and loaders | JUnit test execution metrics |

---

## 3. Quality Gates (Must Pass to Advance)

1. **Bootstrap Invariant**: The application must log the exact number of parsed rows at startup, e.g. `"Loaded N restaurants successfully"`.
2. **Graceful Health Transition**: 
   * During the loading sequence, `/actuator/health` must return `503 Service Unavailable` with `{"status":"DOWN"}` or equivalent.
   * Once loaded, it must immediately transition to `200 OK` and `{"status":"UP"}`.
3. **Robust Normalization**: The [RestaurantNormalizer](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/architecture.md#71-component-catalog) must not fail on dirty strings in Zomato data:
   * Ratings such as `"NEW"`, `"-"`, `""` must normalize to `0.0`.
   * Cost strings like `"Rs. 1,200"`, `"1200 for two"`, or nulls must not throw exceptions.
4. **Derived Metadata Availability**: `InMemoryRestaurantRepository` must compute sorted, unique lists of cities and cuisines from the dataset immediately after load.

---

## 4. Automated Verification Steps

```bash
# 1. Run all unit tests for Phase 1
./mvnw test -Dtest=RestaurantNormalizerTest,RestaurantDataLoaderTest

# 2. Verify dev profile starts up and logs loading behavior
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev &
# Wait for logs showing loaded count

# 3. Request health check during/after startup
curl -i http://localhost:8080/actuator/health
```

---

## 5. Manual Evaluation Checklist

- [ ] **Repository Read-Only Safety**: Confirm that `InMemoryRestaurantRepository.findAll()` returns an unmodifiable list to prevent external mutability.
- [ ] **CatalogNotReadyException Handling**: Confirm that calling repository methods before startup completion throws a `CatalogNotReadyException` instead of returning null or partial lists.
- [ ] **Cuisine Splits**: Check a sample record to ensure cuisines like `"North Indian, Chinese"` are parsed as `["North Indian", "Chinese"]` rather than a single string.
- [ ] **Cache Fallback Strategy**: Disconnect network access and verify that the startup runner successfully falls back to cached files or the classpath sample without failing.

---

## Related Documents
- [Phase 1 Task List](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/implementation-plan.md#phase-1--data-ingestion--in-memory-catalog)
- [System Architecture](file:///d:/Work%20Space/AI%20Projects/AI%20Powered%20Restaurant/docs/architecture.md)
