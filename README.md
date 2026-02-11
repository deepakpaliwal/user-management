# Unified User Management System (UUMS)

Initial project scaffold for a multi-module user-management platform:

- `uums-api`: Spring Boot API (Security, OAuth2 resource server, Liquibase, JPA).
- `uums-batch`: Spring Batch job runner for analytics/reporting pipelines.
- `uums-ui`: React + Tailwind UI starter.
- `memory`: Daily development worklogs with chronological timestamped updates.
- `scripts`: Local lifecycle helpers.

## Quick start

```bash
./scripts/start.sh
```

Stop:

```bash
./scripts/stop.sh
```

Restart:

```bash
./scripts/restart.sh
```

## Implemented in current iteration

- `POST /api/v1/auth/register` with Bean Validation for username/email/password and default `ROLE_USER` assignment.
- `POST /api/v1/auth/login` with account lockout after configurable failed-attempt threshold.
- `POST /api/v1/auth/refresh` to rotate access/refresh JWT tokens.
- JWT token issuance (HS256) and Spring Security JWT resource-server verification.
- In-memory login rate limiting filter per client IP.
- `POST /api/v1/services` and `GET /api/v1/services` for initial tenant onboarding and API key generation.
- `GET/PUT/DELETE /api/v1/admin/users` plus `GET /api/v1/admin/users/{userId}` for admin user CRUD, account state updates, password overrides, and role assignment.
- `POST /api/v1/auth/mfa/challenge` and `POST /api/v1/auth/mfa/verify` for two-step authentication via OTP challenge.
- `POST /api/v1/auth/recovery/challenge` and `POST /api/v1/auth/recovery/reset` for security-question + OTP based password recovery.
- OpenAPI/Swagger docs enabled at `/swagger-ui.html` and `/v3/api-docs`.

## API documentation

Once the API is running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Integration snippets

### Java (Spring WebClient)

```java
WebClient client = WebClient.builder().baseUrl("http://localhost:8080").build();

Map<String, Object> loginRequest = Map.of("username", "test_user_01", "password", "Password@123");
Map<String, Object> auth = client.post()
        .uri("/api/v1/auth/login")
        .bodyValue(loginRequest)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
        .block();
```

### Python (requests)

```python
import requests

resp = requests.post(
    "http://localhost:8080/api/v1/auth/login",
    json={"username": "test_user_01", "password": "Password@123"},
    timeout=10,
)
resp.raise_for_status()
print(resp.json())
```

### React (fetch helper)

```javascript
export async function login(username, password) {
  const response = await fetch('http://localhost:8080/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  if (!response.ok) throw new Error('Login failed');
  return response.json();
}
```

### Node.js (native fetch)

```javascript
const response = await fetch('http://localhost:8080/api/v1/services', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${accessToken}`,
  },
  body: JSON.stringify({
    serviceName: 'billing-service',
    ownerEmail: 'owner@uums.local',
    pricingTier: 'BASIC',
  }),
});

if (!response.ok) throw new Error('Service onboarding failed');
console.log(await response.json());
```

## UI experience

The React homepage now includes:

- Service showcase cards with visuals for auth/admin/analytics.
- Login/register panel preview.
- Admin panel and user stats widgets.
- Role-based quick action buttons (`guest`, `user`, `admin`, `developer`).
- Developer docs panel linking to Swagger/OpenAPI.

## Build and test

```bash
mvn test
```

## Worklog policy

A new file is created per day under `memory/` with ISO date naming, and entries are appended with timestamps in chronological order.
