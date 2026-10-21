import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 개발 시 /api 는 백엔드(8080)로 프록시 → 백엔드 CORS 설정 불필요.
// 이용자용 확률 정보 화면(/probability)이 쓰는 /api/site/probability 는 BOC Replica Gateway(8090)로 보낸다.
// 더 구체적인 경로를 /api 보다 먼저 둬야 한다(먼저 맞는 규칙이 적용된다).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/site/probability': { target: 'http://localhost:8090', changeOrigin: true },
      '/api': 'http://localhost:8080',
    },
  },
});
