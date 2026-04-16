import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const projectRoot = resolve(fileURLToPath(new URL('.', import.meta.url)), '../../..');

export default defineConfig(({ command }) => ({
  base: command === 'build' ? '/react-app/' : '/',
  envDir: projectRoot,
  plugins: [react()],
  build: {
    outDir: '../resources/static/react-app',
    emptyOutDir: true,
  },
  server: {
    proxy: {
      '/api': 'http://127.0.0.1:8080',
      '/login': 'http://127.0.0.1:8080',
      '/logout': 'http://127.0.0.1:8080',
    },
  },
}));
