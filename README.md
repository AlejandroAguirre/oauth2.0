# OAuth2 / OIDC — Arquitectura de Autenticación y Resource Server

Proyecto de ejemplo que implementa una arquitectura basada en **OAuth 2.0 + OpenID Connect (OIDC)** utilizando Spring Security.

La solución está compuesta por:

* **OAuth Server** — Authorization Server responsable de autenticar usuarios y emitir tokens.
* **OAuth Client** — Aplicación web Spring Boot + Thymeleaf que utiliza OAuth2 para autenticarse y consumir el Resource Server.
* **OAuth Client Angular** — Aplicación frontend Angular que utiliza OAuth2 Authorization Code + PKCE.
* **Resource Server** — API protegida que valida los access tokens y permite acceder a los recursos según los scopes/autorizaciones.

Cada aplicación tiene responsabilidades independientes y el **Authorization Server no comparte la misma base de datos que el Resource Server**.

---

## 1. Arquitectura

```text
                         ┌─────────────────────────┐
                         │                         │
                         │     OAuth Server        │
                         │  Authorization Server    │
                         │                         │
                         │  Spring Authorization    │
                         │       Server             │
                         │                         │
                         │  H2 - Usuarios/Auth      │
                         │                         │
                         └────────────┬────────────┘
                                      │
                         OAuth2 / OIDC│
                                      │
                    ┌─────────────────┴─────────────────┐
                    │                                   │
                    │                                   │
          ┌─────────▼─────────┐               ┌────────▼──────────┐
          │                   │               │                   │
          │    OAuth Client   │               │  Angular Client   │
          │                   │               │                   │
          │ Spring Boot       │               │ Angular           │
          │ Thymeleaf         │               │                   │
          │                   │               │ Authorization Code │
          │ Client Secret     │               │ + PKCE             │
          │                   │               │                   │
          └─────────┬─────────┘               └────────┬──────────┘
                    │                                  │
                    │          Bearer Token             │
                    └────────────────┬─────────────────┘
                                     │
                                     ▼
                         ┌─────────────────────────┐
                         │                         │
                         │    Resource Server      │
                         │                         │
                         │       REST API          │
                         │                         │
                         │  Validación de Token     │
                         │                         │
                         │  H2 - Datos de negocio   │
                         │                         │
                         └─────────────────────────┘
```

---

# 2. Componentes

## `/oauth-server`

Es el **Authorization Server**.

Su responsabilidad principal es:

* Autenticar usuarios.
* Gestionar usuarios y contraseñas.
* Autorizar clientes OAuth2.
* Emitir `access_token`.
* Emitir `refresh_token`.
* Emitir `id_token` mediante OpenID Connect.
* Gestionar scopes.
* Validar las solicitudes de autorización.
* Gestionar el login y logout.

Este servidor utiliza una base de datos H2 propia para la autenticación.

> La base de datos del Authorization Server es independiente de la base de datos del Resource Server.

---

## `/oauth-client`

Es una aplicación **Spring Boot + Thymeleaf**.

Su responsabilidad es:

1. Redirigir al usuario al Authorization Server.
2. Autenticar al usuario mediante OAuth2/OIDC.
3. Obtener los tokens necesarios.
4. Interceptar las peticiones hacia el Resource Server.
5. Agregar el `Bearer Token` a las peticiones.
6. Consumir la API protegida.
7. Gestionar el logout.
8. Mostrar la información obtenida desde el Resource Server.

Por ejemplo:

```java
@Controller
public class UserController {

    private final UserClient userClient;

    public UserController(UserClient userClient) {
        this.userClient = userClient;
    }

    @GetMapping("/user")
    public String user(Model model) {

        UserResponse response = userClient.getUser();

        model.addAttribute("user", response);

        return "user";
    }
}
```

La aplicación tiene, entre otras, las siguientes vistas:

```text
index.html
user.html
```

`index.html` funciona como página inicial y `user.html` muestra la información obtenida desde el Resource Server.

---

# 3. Seguridad del OAuth Client

La configuración de Spring Security permite que las rutas públicas puedan ser consultadas sin autenticación y que el resto requiera autenticación.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http.authorizeHttpRequests(
                auth -> auth
                    .requestMatchers(
                        "/",
                        "/css/**",
                        "/js/**"
                    )
                    .permitAll()
                    .anyRequest()
                    .authenticated()
        )
        .oauth2Login(
            oauth2 -> oauth2
                .defaultSuccessUrl("/user", true)
        )
        .oauth2Client(Customizer.withDefaults())
        .logout(
            logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
        );

        return http.build();
    }
}
```

La configuración permite utilizar:

```text
oauth2Login()
```

para autenticar al usuario y:

```text
oauth2Client()
```

para que la aplicación pueda actuar como cliente OAuth2 y consumir APIs protegidas.

---

# 4. Registro OAuth2 del Client

El cliente Spring utiliza:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          oauth-client:
            provider: oauth-server
            client-id: oauth-client
            client-secret: 12345678910
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            client-name: oauth-client
            scope: openid,profile,read,write

        provider:
          oauth-server:
            issuer-uri: http://127.0.0.1:9000
```

El flujo utilizado es:

```text
Authorization Code
```

El cliente obtiene los metadatos del Authorization Server mediante:

```text
issuer-uri
```

En este caso:

```text
http://127.0.0.1:9000
```

---

# 5. OAuth Server

El Authorization Server utiliza Spring Authorization Server.

Se definen dos `SecurityFilterChain`.

La primera protege los endpoints propios del Authorization Server:

```java
@Bean
@Order(1)
public SecurityFilterChain authorizationServerSecurityFilterChain(
        HttpSecurity http) throws Exception {

    OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
            OAuth2AuthorizationServerConfigurer.authorizationServer();

    http.securityMatcher(
            authorizationServerConfigurer.getEndpointsMatcher()
        )
        .with(
            authorizationServerConfigurer,
            authorizationServer ->
                authorizationServer.oidc(
                    Customizer.withDefaults()
                )
        )
        .authorizeHttpRequests(
            authorize ->
                authorize.anyRequest().authenticated()
        )
        .exceptionHandling(
            exceptions ->
                exceptions.defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
        )
        .cors(Customizer.withDefaults());

    return http.build();
}
```

Esta configuración habilita los endpoints relacionados con OAuth2 y OpenID Connect.

---

# 6. Seguridad general del Authorization Server

La segunda cadena protege la aplicación web del Authorization Server:

```java
@Bean
@Order(2)
public SecurityFilterChain defaultSecurityFilterChain(
        HttpSecurity http) throws Exception {

    http.authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/login",
                        "/css/**",
                        "/js/**",
                        "/h2-console/**",
                        "/error",
                        "/.well-known/appspecific/**"
                    )
                    .permitAll()
                    .anyRequest()
                    .authenticated()
        )
        .csrf(
            csrf ->
                csrf.ignoringRequestMatchers(
                    "/h2-console/**",
                    "/oauth2/token"
                )
        )
        .headers(
            headers ->
                headers.frameOptions(
                    frame -> frame.disable()
                )
        )
        .formLogin(
            form -> form
                .loginPage("/login")
                .permitAll()
        )
        .cors(Customizer.withDefaults());

    return http.build();
}
```

El login del usuario se realiza mediante:

```text
/login
```

y utiliza autenticación basada en formulario.

---

# 7. Password Encoder

Las contraseñas de los usuarios se almacenan utilizando BCrypt:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Esto evita almacenar contraseñas en texto plano.

---

# 8. OAuth Clients registrados

El Authorization Server registra dos clientes:

```text
oauth-client
angular-client
```

Cada cliente tiene una configuración diferente.

---

## 8.1 OAuth Client — Spring Boot + Thymeleaf

```java
RegisteredClient oauthClient =
    RegisteredClient
        .withId(UUID.randomUUID().toString())
        .clientId("oauth-client")
        .clientSecret(
            new BCryptPasswordEncoder()
                .encode("12345678910")
        )
        .clientAuthenticationMethod(
            ClientAuthenticationMethod.CLIENT_SECRET_BASIC
        )
        .authorizationGrantType(
            AuthorizationGrantType.AUTHORIZATION_CODE
        )
        .authorizationGrantType(
            AuthorizationGrantType.REFRESH_TOKEN
        )
        .redirectUri(
            "http://127.0.0.1:8080/login/oauth2/code/oauth-client"
        )
        .postLogoutRedirectUri(
            "http://127.0.0.1:8080/logout"
        )
        .scope(OidcScopes.OPENID)
        .scope(OidcScopes.PROFILE)
        .scope("read")
        .scope("write")
        .clientSettings(
            ClientSettings.builder()
                .requireAuthorizationConsent(false)
                .build()
        )
        .build();
```

Este cliente utiliza:

```text
Client ID:
oauth-client
```

y:

```text
Client Authentication:
CLIENT_SECRET_BASIC
```

Por lo tanto, es un cliente capaz de mantener un secreto.

También tiene habilitado:

```text
authorization_code
refresh_token
```

---

# 9. Angular Client

Angular utiliza una configuración diferente debido a que se trata de una aplicación frontend.

```java
RegisteredClient angularClient =
    RegisteredClient
        .withId(UUID.randomUUID().toString())
        .clientId("angular-client")
        .clientAuthenticationMethod(
            ClientAuthenticationMethod.NONE
        )
        .authorizationGrantType(
            AuthorizationGrantType.AUTHORIZATION_CODE
        )
        .redirectUri(
            "http://127.0.0.1:4200/login-callback"
        )
        .postLogoutRedirectUri(
            "http://127.0.0.1:4200/logout"
        )
        .scope(OidcScopes.OPENID)
        .scope(OidcScopes.PROFILE)
        .scope("read")
        .scope("write")
        .clientSettings(
            ClientSettings.builder()
                .requireProofKey(true)
                .requireAuthorizationConsent(false)
                .build()
        )
        .build();
```

Angular utiliza:

```text
clientAuthenticationMethod:
NONE
```

porque es un **public client** y no debe almacenar un `client_secret` de forma segura en el navegador.

Además utiliza:

```text
PKCE
```

mediante:

```java
.requireProofKey(true)
```

El flujo utilizado es:

```text
Authorization Code + PKCE
```

---

# 10. Scopes

Los clientes tienen disponibles los siguientes scopes:

```text
openid
profile
read
write
```

Los scopes representan permisos que el cliente solicita al Authorization Server.

Por ejemplo:

```text
openid
```

habilita funcionalidades relacionadas con OpenID Connect.

Mientras que:

```text
read
write
```

representan permisos utilizados por la API de recursos.

---

# 11. Flujo de autenticación

Cuando un usuario intenta acceder a una ruta protegida del cliente:

```text
GET /user
```

Spring Security detecta que el usuario no está autenticado.

El navegador es redirigido al Authorization Server:

```text
/oauth2/authorize
```

El usuario realiza login en:

```text
/login
```

El Authorization Server autentica al usuario.

Después solicita autorización para el cliente y genera un código de autorización.

El navegador regresa al cliente:

```text
/login/oauth2/code/oauth-client
```

El cliente utiliza el código para obtener los tokens.

Conceptualmente:

```text
Usuario
   │
   │ GET /user
   ▼
OAuth Client
   │
   │ Usuario no autenticado
   ▼
OAuth Server
   │
   │ Login
   ▼
Usuario autenticado
   │
   │ Authorization Code
   ▼
OAuth Client
   │
   │ Code → Token
   ▼
Access Token
```

---

# 12. Consumo del Resource Server

Una vez autenticado el usuario, el `oauth-client` dispone de un `access_token`.

Cuando `UserClient` necesita consultar información:

```java
UserResponse response = userClient.getUser();
```

la petición hacia el Resource Server debe incluir:

```http
Authorization: Bearer <access_token>
```

El token identifica al cliente/usuario y contiene la información necesaria para que el Resource Server pueda determinar si la petición está autorizada.

---

# 13. Resource Server

El proyecto:

```text
/resource-server
```

es una API REST protegida.

Tiene su propia base de datos H2.

Esta base de datos **no es la misma que utiliza el Authorization Server**.

La separación es intencional:

```text
OAuth Server H2
    │
    └── Usuarios / autenticación / autorización


Resource Server H2
    │
    └── Datos de negocio / empleados
```

El Authorization Server se ocupa de:

```text
¿Quién eres?
¿Puedes acceder?
¿Qué permisos tienes?
```

Mientras que el Resource Server se ocupa de:

```text
¿Qué datos puedo entregarte?
```

---

# 14. Endpoint protegido

El Resource Server expone:

```http
GET /resources/user
```

El controlador es:

```java
@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final EmpleadoService empleadoService;

    public ResourceController(
            EmpleadoService empleadoService) {

        this.empleadoService = empleadoService;
    }

    @GetMapping("/user")
    @RateLimiter(
        name = "userEndpoint",
        fallbackMethod = "rateLimitFallback"
    )
    public ResponseEntity<UserResponseDTO> readUser(
            Authentication authentication) {

        List<EmpleadoDTO> empleados =
                empleadoService.obtenerEmpleados();

        UserResponseDTO response =
                new UserResponseDTO(
                    "Usuario tiene permisos ",
                    authentication.getName(),
                    authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList(),
                    empleados
                );

        return ResponseEntity.ok(response);
    }
}
```

El parámetro:

```java
Authentication authentication
```

permite acceder a la información del usuario autenticado y sus autoridades.

Por ejemplo:

```java
authentication.getName()
```

permite obtener el nombre/identidad asociada al usuario.

Mientras que:

```java
authentication.getAuthorities()
```

permite obtener sus permisos.

---

# 15. Validación del Access Token

El Resource Server no confía simplemente en que el cliente diga:

```text
"Estoy autenticado"
```

La petición debe contener un token válido:

```http
Authorization: Bearer eyJ...
```

El Resource Server valida el token utilizando la configuración del Authorization Server.

Conceptualmente:

```text
OAuth Client
     │
     │ Authorization: Bearer <token>
     ▼
Resource Server
     │
     │ ¿Token válido?
     │
     ├── NO ──► 401 Unauthorized
     │
     └── SÍ
          │
          ▼
     Procesar petición
          │
          ▼
     Consultar H2
          │
          ▼
     Devolver datos
```

---

# 16. Flujo completo

El flujo completo de la aplicación es:

```text
                    1. Solicita /user
Usuario ───────────────────────────────► OAuth Client
                                            │
                                            │
                                  2. Redirección OAuth2
                                            │
                                            ▼
                                      OAuth Server
                                            │
                                      3. Login
                                            │
                                            ▼
                                          Usuario
                                            │
                                      4. Login OK
                                            │
                                            ▼
                                      OAuth Server
                                            │
                                   5. Authorization Code
                                            │
                                            ▼
                                      OAuth Client
                                            │
                                   6. Token Request
                                            │
                                            ▼
                                      OAuth Server
                                            │
                                   7. Access Token
                                            │
                                            ▼
                                      OAuth Client
                                            │
                              8. Bearer Access Token
                                            │
                                            ▼
                                   Resource Server
                                            │
                                  9. Validar Token
                                            │
                                            ▼
                                      H2 Resource
                                            │
                                  10. Datos empleados
                                            │
                                            ▼
                                      OAuth Client
                                            │
                                  11. Render Thymeleaf
                                            │
                                            ▼
                                         Usuario
```

---

# 17. Logout

El OAuth Client tiene configurado:

```java
.logout(
    logout -> logout
        .logoutSuccessUrl("/")
        .invalidateHttpSession(true)
        .clearAuthentication(true)
        .deleteCookies("JSESSIONID")
)
```

Al cerrar sesión se realizan principalmente estas acciones:

* Se invalida la sesión HTTP.
* Se elimina la autenticación de Spring Security.
* Se elimina la cookie `JSESSIONID`.
* El usuario es redirigido a `/`.

El Authorization Server también tiene configurado un:

```text
postLogoutRedirectUri
```

para cada cliente.

Para Spring Boot:

```text
http://127.0.0.1:8080/logout
```

Para Angular:

```text
http://127.0.0.1:4200/logout
```

---

# 18. Diferencia entre los dos clientes

## Spring Boot + Thymeleaf

```text
OAuth Client
│
├── Backend
├── Spring Security
├── Thymeleaf
├── Client Secret
├── Authorization Code
└── Refresh Token
```

Es un cliente que tiene un backend propio y puede proteger su `client_secret`.

---

## Angular

```text
Angular
│
├── Frontend
├── Public Client
├── Authorization Code
├── PKCE
└── No Client Secret
```

Angular no debe almacenar un secreto confidencial porque todo el código se ejecuta en el navegador.

Por esta razón utiliza:

```text
Authorization Code + PKCE
```

---

# 19. Separación de responsabilidades

La arquitectura puede resumirse de la siguiente manera:

| Componente      | Responsabilidad      |
| --------------- | -------------------- |
| OAuth Server    | Autenticación        |
| OAuth Server    | Autorización         |
| OAuth Server    | Emisión de tokens    |
| OAuth Server    | OpenID Connect       |
| OAuth Client    | Aplicación web       |
| OAuth Client    | Login OAuth2         |
| OAuth Client    | Gestión de sesión    |
| OAuth Client    | Consumo de API       |
| Angular Client  | Aplicación frontend  |
| Angular Client  | OAuth2 + PKCE        |
| Resource Server | API REST             |
| Resource Server | Validación del token |
| Resource Server | Datos de negocio     |

---

# 20. Bases de datos

La aplicación utiliza dos bases H2 independientes.

### OAuth Server

Contiene información relacionada con:

```text
Usuarios
Contraseñas
Clientes OAuth
Autorizaciones
Tokens
```

### Resource Server

Contiene información relacionada con:

```text
Empleados
Datos de negocio
```

No es necesario que el Resource Server conozca la contraseña del usuario.

El Resource Server únicamente necesita confiar en el Authorization Server para validar los tokens.

---

# 21. Resumen

La arquitectura implementa una separación clara entre autenticación, clientes y recursos:

```text
                    AUTHENTICATION
                          │
                          ▼
                  ┌───────────────┐
                  │ OAuth Server  │
                  │               │
                  │ Login         │
                  │ OAuth2        │
                  │ OIDC          │
                  │ Tokens        │
                  └───────┬───────┘
                          │
                    Access Token
                          │
              ┌───────────┴───────────┐
              │                       │
              ▼                       ▼
       ┌──────────────┐       ┌──────────────┐
       │ OAuth Client │       │    Angular   │
       │              │       │    Client    │
       │ Thymeleaf    │       │    + PKCE    │
       └──────┬───────┘       └──────┬───────┘
              │                       │
              └───────────┬───────────┘
                          │
                    Bearer Token
                          │
                          ▼
                  ┌───────────────┐
                  │Resource Server│
                  │               │
                  │ REST API      │
                  │ Token Valid.  │
                  │ Business Data │
                  └───────────────┘
```

En resumen:

> **OAuth Server autentica y emite tokens. Los clientes obtienen y utilizan esos tokens. El Resource Server valida los tokens y entrega los recursos protegidos.**

Esta separación permite que múltiples clientes —como una aplicación web tradicional y una aplicación Angular— consuman la misma API protegida sin que la API tenga que gestionar directamente las credenciales de los usuarios.
