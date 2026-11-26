export const environment = {
  // Everything goes through the API Gateway (8084), which resolves services with Eureka.
  apiBaseUrl: 'http://localhost:8084/api',
  usersApiBaseUrl: 'http://localhost:8084/api',
  appName: 'Mecanico Vehicle Passport',
  usersPortalUrl: 'http://localhost:4200',
  // MS3 Marketplace service (called directly; not yet registered with Eureka/the gateway).
  marketplaceApiBaseUrl: 'http://localhost:8081',
};
