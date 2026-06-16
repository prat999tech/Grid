# Capture Grid — a real-time shared board

A 40 × 25 board of **1000 tiles**. Anyone who opens the site picks a name + colour
and starts capturing tiles. Every capture is broadcast to all connected players
**instantly** over WebSockets. Includes a live leaderboard, online-player count,
per-user cooldown, and zoom/pan.

> **Live demo:** _<paste your deployed URL here>_
> **Repository:** _<paste your public GitHub URL here>_

```
┌─────────────────────────────────────────────────────┐
│  ◧ Capture Grid     ● Live  · 3 online · Ada · 12 tiles│
├──────────────────────────────────────┬────────────────┤
│                                       │  LEADERBOARD    │
│      ▓▓░░▓▓░░  the grid (1000 tiles)  │  1 Ada     12   │
│      ░░▓▓░░▓▓   click to capture      │  2 Bob      8   │
│      ▓▓░░▓▓░░   drag to pan / scroll  │  3 Cy       5   │
│                                       │                 │
└──────────────────────────────────────┴────────────────┘
```

---

## Tech stack & why

| Layer        | Choice                              | Why |
|--------------|-------------------------------------|-----|
| Frontend     | **Angular 19** (standalone + signals) | Signals give fine-grained, fast reactivity — perfect for 1000 tiles where only a few change at a time. |
| Backend      | **Spring Boot 3.4** (Java)          | First-class WebSocket/STOMP support, clean dependency injection, and JPA for persistence. |
| Real-time    | **STOMP over WebSocket** (SockJS)   | A pub/sub messaging protocol on top of WebSocket: the server publishes one message and the broker fans it out to every subscriber. SockJS adds automatic fallback + reconnect. |
| Database     | **PostgreSQL**                      | Reliable, and its row-level guarantees plus our optimistic-locking column keep tile ownership consistent. |
| Async work   | **Java thread pool** (`ThreadPoolTaskExecutor`) | Captures are processed off the messaging thread on worker threads, so the server stays responsive under bursts of clicks. |

---

## How the real-time + concurrency design works

This is the part that matters most, so here's the full path of a single click:

```
 Browser click
     │  STOMP send  /app/claim  {tileId, userId}
     ▼
 TileWebSocketController        (messaging thread — returns immediately)
     │  hands off to…
     ▼
 ClaimProcessor.process()       @Async → runs on a "claim-worker-" thread
     │  1. lock the tile  (TileLockRegistry — striped ReentrantLocks)
     │  2. TileService.capture()  @Transactional
     │       • load user + tile
     │       • run every ClaimRule (e.g. cooldown)   ← Strategy pattern
     │       • update owner/colour, save (commit)
     │       • @Version column bumps                 ← optimistic-lock safety net
     │  3. unlock
     ▼
 BroadcastService
     ├─ /topic/tiles        → every client paints the tile
     ├─ /topic/leaderboard  → everyone's standings refresh
     └─ /user/queue/errors  → only the clicker hears "cooldown" if rejected
```

**Handling many users at once without breaking:**

1. **Off-thread processing (multithreading).** The WebSocket controller never
   touches the database. It hands the click to `ClaimProcessor`, annotated
   `@Async("claimExecutor")`, which runs on a configurable pool of worker
   threads (`AsyncConfig`). A burst of 100 clicks doesn't block the socket.

2. **Per-tile locking (conflict handling).** Two people clicking the *same* tile
   at the same millisecond must not corrupt it. `TileLockRegistry` hands out a
   lock per tile using **lock striping** (a fixed array of `ReentrantLock`s, so
   memory is bounded). Captures on the same tile are serialised; captures on
   *different* tiles run fully in parallel. The transaction commits **inside**
   the lock, so the next thread always sees committed state. Verified by a test
   that fires two simultaneous captures at one tile — exactly one owner wins and
   both clients converge.

3. **Optimistic locking as a safety net.** Each tile has a JPA `@Version` column.
   Even if locking were bypassed, a stale concurrent write would be rejected by
   the database.

4. **Ordering / idempotency on the client.** Every tile update carries its
   `version`. The browser ignores any update older than what it already shows,
   so out-of-order or duplicate messages can never roll a tile backwards.

5. **Auto-reconnect.** If a client's socket drops, SockJS reconnects after 3s and
   the client re-fetches the board, so it never drifts out of sync.

---

## Design patterns & SOLID

The code is intentionally small and commented so it's easy to follow. Patterns used:

- **Repository** — `TileRepository`, `UserRepository` (Spring Data generates them).
- **DTO** — `TileView`, `GridSnapshot`, … keep the API separate from JPA entities.
- **Strategy (+ Open/Closed)** — `ClaimRule` interface. `CooldownRule` is one
  implementation; add a new rule (e.g. "can't recapture your own tile",
  "area cooldown") by writing one `@Component` — **no existing code changes**.
- **Observer / Publish–Subscribe** — the whole WebSocket topic model; the server
  publishes once and all subscribers receive it.
- **Producer/Consumer (thread pool)** — clicks are produced on the messaging
  thread and consumed by the async worker pool.
- **Facade / Service layer** — `TileService`, `UserService` hold the logic;
  controllers stay thin.

SOLID, concretely:

- **S** — each class has one job: `BroadcastService` only sends messages,
  `CooldownTracker` only stores timestamps, `TileLockRegistry` only hands out locks.
- **O** — new rules plug in via `ClaimRule` without editing the service.
- **L** — every `ClaimRule` is interchangeable.
- **I** — small, focused interfaces.
- **D** — `TileService` depends on the `ClaimRule` interface and repository
  interfaces, not concrete classes (injected by Spring).

---

## Project layout

```
realtime-grid/
├── backend/   Spring Boot + PostgreSQL + WebSocket
│   └── src/main/java/com/example/grid/
│       ├── config/      WebSocket, async pool, CORS, board seeding
│       ├── controller/  REST + WebSocket endpoints, presence tracking
│       ├── service/     business logic, async processor, broadcaster, locks
│       │   └── rules/   pluggable game rules (Strategy)
│       ├── model/       JPA entities (Tile, AppUser)
│       ├── repository/  Spring Data repositories
│       └── dto/         request/response records
└── frontend/  Angular standalone app
    └── src/app/
        ├── core/        models + ApiService, RealtimeService, GameStore
        └── components/   login, toolbar, grid, leaderboard
```

---

## Running it

### Prerequisites
- Java 21+ (tested on JDK 26), Maven, Node 18+/npm, PostgreSQL.

### 1. Database (once)
```bash
createdb gridapp
# or, if you log in as a specific role:
#   psql -U postgres -c "CREATE DATABASE gridapp;"
```
Default connection is `localhost:5432`, user `postgres`, password `postgres`.
Override with env vars if needed:
`DB_HOST DB_PORT DB_NAME DB_USER DB_PASSWORD`.

### 2. Backend  → http://localhost:8080
```bash
cd backend
mvn spring-boot:run
```
On first start it seeds the empty 40 × 25 board. Captures survive restarts.

### 3. Frontend  → http://localhost:4200
```bash
cd frontend
npm install
npm start
```

Open **http://localhost:4200** in two browser windows, pick different names, and
watch captures appear in both instantly.

### Option B: one command with Docker (UI + API in a single container)
The `Dockerfile` builds the Angular app, bundles it into the Spring Boot jar, and
serves everything from one origin (no CORS, one URL). You still need a PostgreSQL.
```bash
docker build -t capture-grid .
docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal -e DB_NAME=gridapp \
  -e DB_USER=postgres -e DB_PASSWORD=postgres \
  capture-grid
# open http://localhost:8080
```

---

## Deployment

The app is packaged as a **single deployable image** (frontend bundled inside the
Spring Boot jar), so you get one URL that serves the UI, the REST API and the
WebSocket — same origin, no CORS. In production the Angular build uses relative
URLs (`/api`, `/ws`), so nothing needs editing after deploy.

### Deploy to Render (free tier) — recommended
A `render.yaml` blueprint is included; it provisions a PostgreSQL database and a
Docker web service and wires the DB credentials in automatically.

1. Push this repo to **GitHub** (public).
2. On [render.com](https://render.com): **New + → Blueprint**, select your repo.
3. Render reads `render.yaml`, creates `grid-db` (Postgres) and `capture-grid`
   (web service), and builds the Dockerfile. First build takes a few minutes.
4. Your live URL is the web service URL, e.g. `https://capture-grid.onrender.com`.

Health checks hit `/actuator/health`. The app reads the port from `PORT` and the
database from `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD` — all set by the blueprint.

> Notes: Render's free web service sleeps after inactivity (first hit is slow to
> wake), and the free database has a limited lifetime — fine for a demo. Render
> supports WebSockets, so the real-time layer works out of the box.

Any Docker-capable host works the same way (Railway, Fly.io, a VM): build the
Dockerfile and provide the five `DB_*` env vars plus a reachable PostgreSQL.

---

## API reference

REST (used on load):
- `POST /api/users` `{name, color?}` → `{id, name, color}` (reuses account by name)
- `GET  /api/grid` → `{cols, rows, tiles[]}`
- `GET  /api/leaderboard` → `[{userId, name, color, tiles}]`

WebSocket (`/ws`, STOMP):
- send → `/app/claim` `{tileId, userId}`
- subscribe → `/topic/tiles`, `/topic/leaderboard`, `/topic/presence`,
  `/user/queue/errors` (private)

---

## Configuration (`backend/.../application.properties`)

| Property | Default | Meaning |
|----------|---------|---------|
| `grid.cols` / `grid.rows` | 40 / 25 | board size |
| `grid.cooldown-ms` | 800 | min time between a user's captures |
| `grid.async.*` | 4 / 8 / 500 | worker pool core/max/queue |

---

## Notes & trade-offs

- **Capture model is "anyone can take any tile" (like r/place)**, gated by a
  cooldown. This makes the game lively and turns conflicts into a feature; the
  per-tile lock + version still guarantee a single consistent owner.
- The in-memory STOMP broker is fine for one server. To scale horizontally you'd
  swap in an external broker (e.g. RabbitMQ/Redis) — `BroadcastService` is the
  only place that publishes, so that change is localised.
- The leaderboard is recomputed per capture via a single grouped SQL query.
  At higher write rates you'd throttle/debounce these broadcasts.
