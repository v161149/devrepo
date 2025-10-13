# React + TypeScript + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:


## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...

      // Remove tseslint.configs.recommended and replace with this
      tseslint.configs.recommendedTypeChecked,
      // Alternatively, use this for stricter rules
      tseslint.configs.strictTypeChecked,
      // Optionally, add this for stylistic rules
      tseslint.configs.stylisticTypeChecked,

      // Other configs...
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```

You can also install [eslint-plugin-react-x](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs['recommended-typescript'],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```

## How to run this project (development)

Open a PowerShell terminal and run:

```powershell
cd c:\development\GCP\projects\bmd_health\frontend
npm.cmd install
npm.cmd run dev
```

Then open http://localhost:5173 in your browser.

## Backend API and CORS / Proxy
The frontend expects an API endpoint to serve images at `GET http://localhost:5000/api/image`.

If your backend is running on `localhost:5000` and you encounter CORS issues, add the proxy below to `vite.config.ts` (development only):

```ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:5000',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
```

With that proxy in place you can fetch `/api/image` from the frontend without CORS problems during development.

If you'd like, I can add the proxy configuration to `vite.config.ts` now.

## Quick test for two-image SSE/polling

1. Start the backend (see `backend/README.md`) and the frontend dev server.
2. Upload sample images to the backend streams:

```powershell
cd c:\development\GCP\projects\bmd_health\backend
python send_image.py images\sample1.svg 1
python send_image.py images\sample2.svg 2
```

3. The frontend panels should update immediately via SSE. If SSE fails, the frontend will fall back to polling `/api/image1` and `/api/image2` every 2 seconds.

You can also open `public/sample1.svg` and `public/sample2.svg` directly to confirm the UI layout.
