import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // Em desenvolvimento, /api vai para o gateway (Docker Compose)
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
