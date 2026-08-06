# QuizForgeAI Frontend

Vue 3, Vite, and TypeScript frontend for the QuizForgeAI Spring Boot API.

## Run locally

Start the backend on `http://localhost:8080`, then run:

```powershell
npm install
npm run dev
```

Open `http://localhost:5173/` in Chrome or Firefox. Vite proxies `/quiz` requests to the Spring Boot server, so no frontend CORS configuration is needed for local development.

## Build

```powershell
npm run build
```

The active quiz and its full answer review are kept in memory for the current session. Refresh recovery, authentication, and quiz history are intentionally out of scope for this version.
