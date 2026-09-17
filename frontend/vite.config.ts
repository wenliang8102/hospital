import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const backendPort = process.env.SERVER_PORT ?? '8090'
const frontendPort = Number(process.env.FRONTEND_PORT ?? '5173')

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: frontendPort,
    proxy: {
      '/api': {
        target: `http://localhost:${backendPort}`,
        changeOrigin: true,
      },
      '/actuator': {
        target: `http://localhost:${backendPort}`,
        changeOrigin: true,
      },
    },
  },
})
