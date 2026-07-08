# AI-Powered Restaurant Recommendation System

An intelligent restaurant discovery app inspired by Zomato. It combines structured Zomato-style restaurant data with an LLM to deliver personalized, explained recommendations—not just long filter lists.

## What It Does

Users describe what they want (location, budget, cuisine, rating, and optional free-text preferences). The system:

1. Filters real restaurants from a Hugging Face dataset
2. Uses an LLM to rank matches and explain *why* each one fits
3. Presents a short, curated list with clear rationale

## Documentation

| Document | Description |
|----------|-------------|
| [Problem Statement](docs/problemStatment.md) | Why we're building this, goals, and scope |
| [Architecture](docs/architecture.md) | Detailed system architecture — C4 views, components, data flow, deployment |
| [Implementation Plan](docs/implementation-plan.md) | Phase-wise build guide — tasks, deliverables, acceptance criteria |

Start with the **problem statement** for context, **architecture** for system design, then **implementation plan** when building.

## Status

**Planning / early stage** — documentation and architecture defined; implementation in progress.

## Tech Stack

- **Java 21** · **Spring Boot 3.x** · **Spring AI** (OpenAI / Anthropic)
- In-memory restaurant catalog · REST API · Thymeleaf or React frontend

## Quick Links

- **Dataset:** [ManikaSaini/zomato-restaurant-recommendation](https://huggingface.co/datasets/ManikaSaini/zomato-restaurant-recommendation) (~51.7k records)
- **Pattern:** Structured filter → Spring AI ranking → user-facing results

## License

TBD
