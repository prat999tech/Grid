/**
 * Where the backend lives. Change these if you run the backend elsewhere.
 * `wsUrl` uses http(s):// because SockJS negotiates the WebSocket itself.
 */
export const environment = {
  apiBase: 'http://localhost:8080/api',
  wsUrl: 'http://localhost:8080/ws',
};
