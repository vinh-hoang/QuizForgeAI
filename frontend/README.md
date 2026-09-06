# QuizForgeAI Frontend

Vue 3, Vite, and TypeScript frontend for the QuizForgeAI Spring Boot API.

## Run locally

Start the backend on `http://localhost:8080`, then run:

```powershell
npm install
npm run dev
```

Open `http://localhost:5173/` in Chrome or Firefox. Vite proxies `/quiz` requests to the Spring Boot server, so no frontend CORS configuration is needed for local development. The dev server also listens on the LAN for device testing; use it only on a trusted network.

For a deployed frontend, copy `.env.example` to `.env` and set `VITE_API_BASE_URL` to the API origin. If it is omitted, the client uses relative `/quiz` requests.

## Build

```powershell
npm run build
```

Run the deterministic frontend regression suite with:

```powershell
npm run test
```

Question, answer, hint, and explanation text supports LaTeX math using inline
delimiters such as `$x^2$` or display delimiters such as `$$\frac{a}{b}$$`.

The active quiz and its full answer review are kept in memory for the current session. Refresh recovery, authentication, and quiz history are intentionally out of scope for this version.
