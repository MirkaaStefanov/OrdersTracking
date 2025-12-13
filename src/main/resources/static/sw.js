// Basic Service Worker to satisfy PWA installation criteria
self.addEventListener('install', (event) => {
    self.skipWaiting();
});

self.addEventListener('activate', (event) => {
    return self.clients.claim();
});

self.addEventListener('fetch', (event) => {
    // Simple pass-through network strategy to ensure
    // real-time data for the Order System is always fresh.
    event.respondWith(fetch(event.request));
});