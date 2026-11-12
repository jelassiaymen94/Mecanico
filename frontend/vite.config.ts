import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // En développement, les appels /api sont redirigés vers le backend (pas de CORS à gérer).
    proxy: {
      '/api': 'http://localhost:3000',
    },
  },
});
