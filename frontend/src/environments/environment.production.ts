/**
 * Production build. The Angular app is bundled inside the Spring Boot jar and
 * served from the SAME origin as the API and WebSocket, so we use relative URLs.
 * That means no CORS config and no hard-coded host to keep in sync after deploy.
 */
export const environment = {
  apiBase: '/api',
  wsUrl: '/ws',
};
