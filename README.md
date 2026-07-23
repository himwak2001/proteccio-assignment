# Data Governance Dashboard

Upload a CSV/Excel file, and this app figures out its structure, flags columns that
look sensitive, runs basic quality checks, and scores the dataset for trust and
usage value - all viewable on a dashboard.

Built for the Proteccio Data Full Stack Developer assignment.

## Live links

- Frontend: _TODO_
- Backend API: _TODO_
- Repo: _TODO_

> Free-tier hosting sleeps after inactivity - first request after idle can take
> 20-30 seconds to wake up. Expected, not a bug.

## Stack

- Backend: Java 17, Spring Boot 3, PostgreSQL, Apache Commons CSV, Apache POI
- Frontend: React 18, Vite, React Router, Axios
- Tests: JUnit 5 with H2 in-memory DB

**Why Spring Boot instead of Express?** It's my day-to-day stack, so I could spend
the limited time on the actual governance logic instead of learning a new framework
under deadline.

## Project structure

```
proteccio-assignment/
├── backend/                        Spring Boot API
│   └── src/main/java/com/proteccio/datagov/
│       ├── model/                  JPA entities (Dataset, ColumnMeta) + enums
│       ├── repository/             Spring Data repositories
│       ├── dto/                    Request/response DTOs (never expose entities directly)
│       ├── service/                One service per governance concern (see below)
│       ├── controller/             REST endpoints
│       ├── config/                 CORS config
│       └── exception/              Centralized error handling
├── frontend/                       React dashboard
│   └── src/
│       ├── api/                    Axios client, one function per endpoint
│       └── components/             Dashboard (catalog list) + DatasetDetail (drill-down)
├── sample-data/                    Test CSVs (one clean, one deliberately messy)
└── README.md
```

## How the 7 requirements map to code

| Requirement | Where |
|---|---|
| Ingestion | `IngestionService` - parses CSV/XLSX, handles ragged rows and blank headers |
| Discovery | `TypeInferenceService` - guesses column type from a sample of values |
| Classification | `ClassificationService` - header-name + regex heuristics, overridable from the UI |
| Quality | `QualityService` - missing %, invalid values, duplicate rows, rolled into one score |
| Trust | `TrustService` - quality score + classification completeness |
| Value | `ValueService` - view count, last-viewed date, flags datasets idle 30+ days |
| Dashboard | `DatasetController` + React `Dashboard` / `DatasetDetail` |

`DatasetService` runs the whole pipeline in order when a file gets uploaded - that's
the place to look if you want to see the end-to-end flow.

## Scoring formulas

**Quality score** (0-100):
```
quality = 100 - (avg missing % × 0.5) - (invalid value rate % × 0.3) - (duplicate row rate % × 0.2)
```
Missing data is weighted highest since it affects every downstream check. Some rate
of duplicates/invalid values is normal in real data, so those count for less.

**Trust score** (0-100):
```
trust = quality_score × 0.7 + classification_completeness % × 0.3
```
Completeness = % of columns either auto-tagged as sensitive or manually
reviewed/overridden. An all-"NONE" dataset that's never been reviewed scores lower
on trust, since auto-classification can miss things.

**Value / activity:** view count + last-viewed timestamp, flagged "low activity" if
untouched for 30+ days. A fuller usage-weighted model was out of scope for the time
I had — noting that here instead of pretending it's more sophisticated than it is.

## Running it locally

### Backend
```bash
cd backend
createdb datagov
export DATABASE_URL=jdbc:postgresql://localhost:5432/datagov
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
mvn spring-boot:run
```
Runs on `http://localhost:8080`. Swagger UI at `/swagger-ui.html`. Tables are
created automatically on startup.

### Frontend
```bash
cd frontend
npm install
npm run dev
```
Runs on `http://localhost:5173`, talks to `http://localhost:8080/api` by default.

### Tests
```bash
cd backend
mvn test
```
Uses H2 in-memory DB, no Postgres needed for tests.

### Sample data
- `sample-data/employees_clean.csv` - happy path
- `sample-data/customers_messy.csv` - missing values, a duplicate row, a bad phone
  number, a malformed email - for exercising the edge cases

## Deployment

- Backend (Render/Railway): set `DATABASE_URL`, `DATABASE_USERNAME`,
  `DATABASE_PASSWORD`, `FRONTEND_ORIGIN`
- Database: any free-tier Postgres (Render/Neon/Supabase)
- Frontend (Vercel/Netlify): set `VITE_API_BASE_URL` to your backend's `/api` URL

## Assumptions / scope

- No auth or multi-user support, per the brief
- Type inference and classification sample values rather than scanning huge files
  end to end, to keep upload time reasonable
- "Value" is view count + recency, not a full downstream-consumer model
- Excel support covers `.xlsx`, first sheet only
- Duplicate detection is exact match, not fuzzy

