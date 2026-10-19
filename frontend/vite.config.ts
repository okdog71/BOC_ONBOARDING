import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 개발 시 /api 는 백엔드(8080)로 프록시 → 백엔드 CORS 설정 불필요.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { '/api': 'http://localhost:8080' },
  },
});
