# Smart Campus Sensor & Room Management API

A RESTful API built with JAX-RS (Jersey 2.41) and Apache Tomcat for managing campus rooms and IoT sensors. Built as part of the 5COSC022W Client-Server Architectures module at the University of Westminster.

---

## API Design Overview

The API is structured around three core resources:

- **Rooms** – physical campus spaces with capacity management
- **Sensors** – IoT devices (Temperature, CO2, Occupancy etc.) assigned to rooms  
- **SensorReadings** – historical measurement data per sensor (sub-resource of Sensors)

**Base URL:** `http://localhost:8080/SmartCampusAPI/api/v1`

**Authentication:** All endpoints except the discovery endpoint require the header:
```
X-API-Key: smartcampus-secret-2026
```

**Key Features:**
- Full CRUD for Rooms and Sensors
- Sub-resource locator pattern for sensor readings
- Business logic constraints (cannot delete a room with active sensors)
- Structured JSON error responses for all failure scenarios
- API key authentication middleware
- ConcurrentHashMap in-memory store (thread-safe, no database required)
- Java Logger used throughout (no System.out)

---

## Project Structure

```
src/main/java/com/mycompany/smartcampusapi/
├── SmartCampusApp.java                          ← @ApplicationPath entry point
├── com.mycompany.smartcampusapi.model/
│   ├── Room.java
│   ├── Sensor.java
│   └── SensorReading.java
├── com.mycompany.smartcampusapi.store/
│   └── DataStore.java                           ← Singleton ConcurrentHashMap store
├── com.mycompany.smartcampusapi.resource/
│   ├── DiscoveryResource.java                   ← GET /api/v1
│   ├── RoomResource.java                        ← /api/v1/rooms
│   ├── SensorResource.java                      ← /api/v1/sensors
│   └── SensorReadingResource.java               ← /api/v1/sensors/{id}/readings
├── com.mycompany.smartcampusapi.exception/
│   ├── RoomNotEmptyException.java
│   ├── RoomNotEmptyExceptionMapper.java         ← 409 Conflict
│   ├── LinkedResourceNotFoundException.java
│   ├── LinkedResourceNotFoundExceptionMapper.java ← 422 Unprocessable Entity
│   ├── SensorUnavailableException.java
│   ├── SensorUnavailableExceptionMapper.java    ← 403 Forbidden
│   └── GlobalExceptionMapper.java              ← 500 catch-all
└── com.mycompany.smartcampusapi.filter/
    └── AuthFilter.java                          ← API key authentication middleware
```

---

## Endpoints Summary

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | /api/v1/ | Discovery – API metadata and links | No |
| GET | /api/v1/rooms | List all rooms | Yes |
| POST | /api/v1/rooms | Create a new room | Yes |
| GET | /api/v1/rooms/{roomId} | Get room by ID | Yes |
| DELETE | /api/v1/rooms/{roomId} | Delete room (blocked if sensors assigned) | Yes |
| GET | /api/v1/sensors | List sensors (optional ?type= filter) | Yes |
| POST | /api/v1/sensors | Register a sensor (validates roomId exists) | Yes |
| GET | /api/v1/sensors/{sensorId}/readings | Get reading history for a sensor | Yes |
| POST | /api/v1/sensors/{sensorId}/readings | Add reading (updates sensor currentValue) | Yes |

---

## How to Build and Run

### Prerequisites
- Apache NetBeans IDE
- JDK 17
- Apache Tomcat 9.x (configured in NetBeans Services tab)
- Postman (for testing)

### Steps

**1.** Clone the repository:
```bash
git clone https://github.com/PasinduJayasekara/SmartCampusAPI.git
```

**2.** Open NetBeans → File → Open Project → select the cloned folder

**3.** Right-click project → **Clean and Build**

**4.** Right-click project → **Run** (deploys to Tomcat)

**5.** API is live at:
```
http://localhost:8080/SmartCampusAPI/api/v1/
```

---

## Sample curl Commands

### 1. Discovery endpoint (no auth required)
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/
```

Expected response:
```json
{
    "version": "1.0",
    "name": "Smart Campus Sensor & Room Management API",
    "contact": "admin@smartcampus.ac.uk",
    "resources": {
        "rooms": "/api/v1/rooms",
        "sensors": "/api/v1/sensors"
    }
}
```

### 2. Create a room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -H "X-API-Key: smartcampus-secret-2026" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

Expected: `201 Created`

### 3. Get all rooms
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "X-API-Key: smartcampus-secret-2026"
```

### 4. Get a specific room
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301 \
  -H "X-API-Key: smartcampus-secret-2026"
```

### 5. Register a sensor
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -H "X-API-Key: smartcampus-secret-2026" \
  -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"LIB-301"}'
```

Expected: `201 Created`

### 6. Filter sensors by type
```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=Temperature" \
  -H "X-API-Key: smartcampus-secret-2026"
```

### 7. Add a sensor reading
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -H "X-API-Key: smartcampus-secret-2026" \
  -d '{"value":22.5}'
```

Expected: `201 Created` with auto-generated `id` and `timestamp`

### 8. Get reading history
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "X-API-Key: smartcampus-secret-2026"
```

### 9. Try to delete a room with sensors assigned (triggers 409)
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301 \
  -H "X-API-Key: smartcampus-secret-2026"
```

Expected:
```json
{
    "error": "Conflict",
    "status": "409",
    "message": "Room 'LIB-301' cannot be deleted. It has 2 sensor(s) still assigned."
}
```

### 10. Register sensor with non-existent roomId (triggers 422)
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -H "X-API-Key: smartcampus-secret-2026" \
  -d '{"id":"FAKE-001","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"DOES-NOT-EXIST"}'
```

Expected:
```json
{
    "error": "Unprocessable Entity",
    "status": "422",
    "message": "Room 'DOES-NOT-EXIST' does not exist. Sensor cannot be registered."
}
```

### 11. Post reading to MAINTENANCE sensor (triggers 403)
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -H "X-API-Key: smartcampus-secret-2026" \
  -d '{"value":5.0}'
```

Expected:
```json
{
    "error": "Forbidden",
    "status": "403",
    "message": "Sensor 'OCC-001' is under maintenance and cannot accept new readings."
}
```

### 12. Request without API key (triggers 401)
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms
```

Expected:
```json
{
    "error": "Unauthorized",
    "message": "A valid X-API-Key header is required."
}
```

---

## Report – Answers to Questions

### Part 1.1 – JAX-RS Resource Lifecycle

By default, JAX-RS creates a new instance of a resource class for every incoming HTTP request
(per-request scope). This means each request gets a fresh object, which avoids concurrency
issues with instance variables. However, because our data lives in a Singleton `DataStore`
backed by `ConcurrentHashMap` — shared across all instances — we do not lose data between
requests. If we had used instance-level fields to store data, each request would see an empty
dataset. The `ConcurrentHashMap` is critical because multiple threads can hit the DataStore
simultaneously — unlike a regular `HashMap`, it handles concurrent reads and writes safely
without data corruption or race conditions.

---

### Part 1.2 – HATEOAS

HATEOAS (Hypermedia as the Engine of Application State) means the API response includes links
to related resources, so clients can navigate the API without needing separate documentation.
For example, the discovery endpoint at `GET /api/v1` returns links to `/api/v1/rooms` and
`/api/v1/sensors`, so clients do not need to hard-code endpoint paths. This benefits client
developers because the API self-documents its navigation paths, making the client more resilient
to URL changes and easier to explore without reading static external documentation.

---

### Part 2.1 – IDs vs Full Objects

Returning only IDs in a list is bandwidth-efficient but forces the client to make N additional
requests to fetch each resource's details — known as the N+1 problem. Returning full objects
sends more data in one response but reduces round trips. For large collections, a middle ground
— returning summary fields only — is best practice. This project returns full objects by
default, which suits facilities managers who need room details immediately without extra
requests.

---

### Part 2.2 – DELETE Idempotency

Yes, DELETE is idempotent in this implementation. The first DELETE on an existing room returns
`204 No Content` and removes it. All subsequent identical DELETE requests return `404 Not Found`
because the room no longer exists. Although the response code differs between calls, the server
state is identical after each call — the room is gone. RFC 7231 defines idempotency as the
server state being unchanged by repeated identical calls, not the response code being identical.
Therefore DELETE is correctly idempotent in this implementation.

---

### Part 3.1 – @Consumes(APPLICATION_JSON)

If a client sends data in a format other than `application/json` — for example `text/plain` or
`application/xml` — JAX-RS automatically returns `HTTP 415 Unsupported Media Type` before the
method is even invoked. The runtime checks the `Content-Type` header against the declared
`@Consumes` value and rejects mismatches without reaching the business logic. This ensures type
safety at the protocol level without requiring manual content-type checks inside each method.

---

### Part 3.2 – @QueryParam vs Path-based Filtering

Query parameters (`?type=CO2`) are semantically correct for filtering and searching because they
are optional modifiers on a collection resource — the base resource `/sensors` still makes sense
without them. Path-based filtering (`/sensors/type/CO2`) implies a separate resource hierarchy
and creates URL pollution. Query params also support multiple filters easily
(`?type=CO2&status=ACTIVE`), which path segments cannot handle cleanly without creating an
explosion of nested routes.

---

### Part 4.1 – Sub-Resource Locator Pattern

The Sub-Resource Locator pattern delegates request handling to a separate dedicated class based
on a path segment. In this project, `SensorResource` delegates `/sensors/{sensorId}/readings`
to `SensorReadingResource` via a locator method rather than defining all reading logic directly
in `SensorResource`. This improves separation of concerns, makes classes easier to test
independently, and keeps each class focused on one resource. As the API grows, adding new
sub-resource operations only requires changes to `SensorReadingResource` without modifying the
parent sensor logic at all.

---

### Part 5.2 – HTTP 422 vs 404

HTTP 404 means the requested URL or resource was not found on the server. HTTP 422 means the
server understood the request and the URL exists, but the payload is semantically invalid — in
this case, the request references a `roomId` that does not exist in the system. Since the
`/sensors` endpoint itself exists and the JSON is syntactically valid, returning 404 would
mislead the client into thinking the URL is wrong. HTTP 422 precisely communicates that the
data inside the request body contains an unresolvable reference, helping client developers
debug the correct issue immediately.

---

### Part 5.4 – Stack Trace Security Risk

Exposing Java stack traces to external API consumers reveals critical internal information:
(1) internal package and class names, allowing attackers to map the full application structure;
(2) framework and library versions such as Jersey 2.41 and JDK version, enabling version-specific
CVE exploit targeting; (3) server file paths, leaking the deployment directory structure;
(4) logic flow details, helping attackers craft inputs that trigger specific vulnerabilities.
The `GlobalExceptionMapper<Throwable>` in this project prevents all of this by catching every
unhandled error and returning only a generic `500 Internal Server Error` message, keeping all
internal details in server-side logs where only administrators can access them.

---

Authentication Middleware

This project implements API key authentication using a JAX-RS `ContainerRequestFilter`
(`AuthFilter`) with `@Priority(Priorities.AUTHENTICATION)`. The filter intercepts every request
before it reaches any resource method and validates the `X-API-Key` header. Using a filter for
authentication is superior to checking the key manually in each method because: (1) it follows
the DRY principle — the logic is written once and applies to all endpoints automatically;
(2) resource methods stay clean and focused on business logic only; (3) the filter can be
modified or replaced without touching any resource class; (4) it mirrors industry-standard
middleware patterns used in enterprise and cloud-native systems.
