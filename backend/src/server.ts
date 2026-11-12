import { createApp } from './app.js';
import { loadEnv } from './config/env.js';

const env = loadEnv();
const app = createApp({ corsOrigin: env.CORS_ORIGIN });

app.listen(env.PORT, () => {
  console.log(`Mecanico API démarrée sur http://localhost:${env.PORT}/api (${env.NODE_ENV})`);
});
