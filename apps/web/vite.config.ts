import path from 'node:path'
import { fileURLToPath } from 'node:url'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

const root = path.dirname(fileURLToPath(import.meta.url))

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, root, '')
  return {
    plugins: [react(), tailwindcss()],
    resolve: { alias: { '@': path.resolve(root, 'src') } },
    server: {
      port: Number(env.WEB_PORT ?? 5173),
      proxy: { '/api': env.API_PROXY_TARGET ?? 'http://localhost:8080' },
    },
  }
})
