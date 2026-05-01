# AGENTS.md

## Project Overview

AI 爆款文章创作器 - Full-stack application with Spring Boot backend (Java 21) and Vue 3 frontend.

## Developer Commands

### Backend
```bash
# Run (requires MySQL + Redis running locally)
mvn spring-boot:run

# Build
mvn clean package -DskipTests
```

### Frontend
```bash
cd frontend

# Dev server
npm run dev

# Build
npm run build

# Type-check
npm run type-check

# Lint
npm run lint

# Format
npm run format
```

### DB Setup
```bash
# Run SQL scripts in sql/ directory order:
# create_table.sql -> add_*.sql (migrations)
mysql -uroot -p123456 < sql/create_table.sql
```

## Architecture

- **Backend port**: 8567, context-path: `/api`
- **DB**: MyBatis-Flex + MySQL (db: `ai_passage_creator`)
- **Session**: Redis (30-day expiry)
- **AI**: Spring AI Alibaba Agent Framework (multi-agent orchestration enabled)
- **Image services**: Pexels, Nano Banana (Gemini), COS storage

Run `openapi2ts` in frontend to regenerate API client from `http://localhost:8567/api/v3/api-docs`.

## Env Setup

Copy `application-local.yml.example` to `application-local.yml` and configure API keys:
- `PEXELS_API_KEY`
- `nano-banana.api-key`
- Spring AI / DashScope keys