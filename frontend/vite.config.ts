import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

const withoutApi = (path: string) => path.replace(/^\/api/, '')

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // Em desenvolvimento, sem o gateway: cada rota /api vai direto ao serviço dono dela.
    // A ordem importa: a primeira regra que combina vence.
    proxy: {
      '/api/recommend': { target: 'http://localhost:8082', rewrite: withoutApi },
      '/api': { target: 'http://localhost:8081', rewrite: withoutApi },
    },
  },
})
