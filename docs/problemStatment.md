# AI-Powered Restaurant Recommendation System

## Project Context

This project builds an **AI-powered restaurant recommendation service** inspired by platforms like **Zomato**. Users often face choice overload when searching for places to eat: hundreds of listings, inconsistent filters, and little guidance on *why* one option fits their situation better than another.

Traditional search and filter UIs return long lists ranked mainly by rating or distance. They struggle with **nuanced preferences**—for example, “family-friendly Italian under ₹800 in Bangalore with at least 4 stars” or “quick lunch near Connaught Place for two people.” Users want fewer, better-matched options with **clear, human-readable explanations**, not just raw rows from a database.

This application combines **structured restaurant data** with a **Large Language Model (LLM)** to act as an intelligent recommendation layer: filter candidates from real data, then reason over them to rank choices and explain each suggestion in natural language.

---

## Problem Statement

**How might we help users discover restaurants that match their preferences quickly and confidently, using real restaurant data augmented by AI-generated reasoning and explanations?**

### Pain Points

| Pain point | Current experience | What we want |
|------------|-------------------|--------------|
| Information overload | Too many results with weak personalization | A short, curated list of top matches |
| Rigid filters | Hard to express soft preferences (e.g. “cozy”, “good for groups”) | Free-text and structured inputs together |
| No reasoning | Star ratings alone don’t explain fit | AI explains *why* each place is recommended |
| Generic rankings | Same top-rated places for everyone | Recommendations tailored to this user’s session |

### Target Users

- **Everyday diners** choosing where to eat based on location, budget, cuisine, and rating.
- **Learners / builders** exploring how to combine datasets, filtering, and LLMs in a practical product-shaped app.

---

## Objectives

Design and implement an application that:

1. Accepts user preferences (location, budget, cuisine, minimum rating, and optional free-text needs).
2. Uses a **real-world Zomato-style dataset** as the source of truth for restaurant facts.
3. Applies **deterministic filtering** first, then **LLM-based ranking and explanation** on the candidate set.
4. Presents results in a clear, user-friendly format with name, cuisine, rating, cost, and an AI-generated rationale.

### Success Criteria

- Recommendations respect hard constraints (location, budget band, cuisine, minimum rating).
- The LLM output is grounded in filtered dataset rows—not invented restaurants.
- Each top recommendation includes a concise, relevant explanation.
- The end-to-end flow (input → filter → LLM → display) is demonstrable and repeatable.

### Out of Scope (for initial version)

- User accounts, saved history, or persistent profiles.
- Live Zomato API integration or real-time availability / booking.
- Geolocation or map-based discovery (location is text/city-based unless extended later).
- Multi-language support beyond English.

---

## Data Source

**Dataset:** [ManikaSaini/zomato-restaurant-recommendation](https://huggingface.co/datasets/ManikaSaini/zomato-restaurant-recommendation) on Hugging Face  

- ~51,700 restaurant records (~574 MB)
- Typical fields to extract and use: restaurant name, location/city, cuisine type(s), approximate cost for two, aggregate rating, and other metadata available in the schema

The dataset is the **factual backbone**; the LLM must not hallucinate venues outside this filtered set.

---

## System Workflow

### 1. Data Ingestion

- Load and preprocess the Zomato dataset from Hugging Face.
- Normalize fields (names, locations, cuisines, cost bands, ratings).
- Keep a queryable in-memory or lightweight store suitable for filtering.

### 2. User Input

Collect preferences such as:

- **Location** (e.g. Delhi, Bangalore)
- **Budget** (low / medium / high, mapped to cost ranges in the data)
- **Cuisine** (e.g. Italian, Chinese, North Indian)
- **Minimum rating**
- **Additional preferences** (optional free text: family-friendly, quick service, date night, etc.)

### 3. Integration Layer

- Filter restaurants that match structured criteria.
- Cap the candidate set to a manageable size for the LLM context window.
- Build a prompt that includes user preferences plus structured candidate data.
- Instruct the model to rank, explain, and stay faithful to the provided records.

### 4. Recommendation Engine (LLM)

The LLM should:

- Rank filtered candidates by fit to stated preferences.
- Provide a short explanation per recommendation (why it matches).
- Optionally summarize trade-offs (e.g. best value vs. highest rated).

### 5. Output Display

Present top recommendations in a readable format:

| Field | Description |
|-------|-------------|
| Restaurant name | From dataset |
| Cuisine | From dataset |
| Rating | From dataset |
| Estimated cost | From dataset |
| AI explanation | Generated; must reference user preferences and dataset facts |

---

## Technical Direction (High Level)

```
User preferences → Structured filter → Candidate restaurants → LLM prompt → Ranked recommendations + explanations → UI
```

**Design principles:**

- **Grounding:** Filter first; LLM reasons over real rows only.
- **Separation of concerns:** Deterministic filters for hard rules; LLM for ranking and language.
- **Transparency:** Show enough detail that users can trust why a place was suggested.

Planned stack: **Java 21**, **Spring Boot 3.x**, **Spring AI** for LLM integration, plus data loader, preference schema, filter service, prompt templates, and a Thymeleaf or React front end. See [Architecture](./architecture.md) for full design.

---

## Inspiration & References

- **Zomato** — discovery, filters, and restaurant metadata patterns.
- **Hugging Face dataset** — [zomato-restaurant-recommendation](https://huggingface.co/datasets/ManikaSaini/zomato-restaurant-recommendation).
- **Hybrid retrieval + LLM** — common pattern for grounded recommendations in production AI apps.

---

## Document Purpose

This file is the **single source of context** for what we are building and why. Use it when:

- Onboarding to the codebase
- Scoping new features (check objectives and out-of-scope)
- Designing prompts and filters (align with workflow and success criteria)
- Reviewing whether a change still solves the original problem

**Related documentation:**

- [Architecture](./architecture.md) — detailed C4 views, component design, data flow, deployment
- [Implementation Plan](./implementation-plan.md) — phase-wise tasks, deliverables, and acceptance criteria

*Last updated: June 2025*
