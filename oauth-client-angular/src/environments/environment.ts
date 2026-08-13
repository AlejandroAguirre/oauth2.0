export const environment = {
  production: false,

  auth: {
    issuer: 'http://127.0.0.1:9000',
    clientId: 'angular-client',
    scope: 'openid profile read write'
  },

  api: {
    baseUrl: 'http://127.0.0.1:8081'
  }
  
};