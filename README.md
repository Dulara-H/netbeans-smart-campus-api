# Smart Campus — Sensor & Room Management API

A RESTful API built with **JAX-RS (Jersey)** and an embedded **Grizzly HTTP server** for the University of Westminster **5COSC022W Client-Server Architectures** coursework. The API manages **Rooms**, **Sensors**, and **Sensor Readings** for a Smart Campus scenario using **in-memory Java data structures only**, as required by the coursework specification.

---

## Table of Contents

1. [API Design Overview](#api-design-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Build & Run Instructions](#build--run-instructions)
5. [API Endpoints Reference](#api-endpoints-reference)
6. [Sample curl Commands](#sample-curl-commands)
7. [Postman Test Scenarios](#postman-test-scenarios)
8. [Report: Question Answers](#report-question-answers)
9. [Error Handling Summary](#error-handling-summary)
10. [Logging and Observability](#logging-and-observability)
11. [Conclusion](#conclusion)

---

## API Design Overview

This project implements a versioned REST API with the base path:

```text
/api/v1
```

The API models three core entities from the coursework brief:

- **Room**
- **Sensor**
- **SensorReading**

The resource hierarchy mirrors the Smart Campus structure:

```text
/api/v1
  /rooms
    /{roomId}
  /sensors
    /{sensorId}
      /readings
```

### Core design decisions

- **Versioned API design** using `/api/v1` to support future extension without breaking existing clients.
- **Resource-based modelling** where rooms and sensors are treated as first-class resources.
- **Nested reading history** under `/sensors/{sensorId}/readings` to reflect that readings belong to a specific sensor.
- **In-memory storage** using Java collections, fully complying with the coursework restriction against databases.
- **Business rule enforcement** such as preventing room deletion when sensors are still assigned, validating `roomId` on sensor creation, and blocking new readings when a sensor is in `MAINTENANCE`.
- **Custom exception mappers** to guarantee structured JSON error responses instead of raw Java stack traces.
- **Request/response logging filters** to improve API observability while keeping business logic clean.

This design follows the coursework requirement to produce a robust RESTful API in **pure JAX-RS**, without Spring Boot or any database technology.

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java |
| Build Tool | Maven |
| REST Framework | JAX-RS (Jersey) |
| Embedded Server | Grizzly HTTP Server |
| JSON Binding | Jackson |
| Testing Tool | Postman / curl |
| Data Persistence | In-memory Java collections |

### Important coursework compliance

This project uses:

- **JAX-RS only**
- **No Spring Boot**
- **No SQL / No database**
- **In-memory data structures only**

That is fully aligned with the coursework rules.

---

## Project Structure

```text
smart-campus-api/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── smartcampus/
                    └── api/
                        ├── Main.java
                        ├── config/
                        │   └── ApplicationConfig.java
                        ├── exception/
                        │   ├── LinkedResourceNotFoundException.java
                        │   ├── RoomNotEmptyException.java
                        │   └── SensorUnavailableException.java
                        ├── filter/
                        │   └── ApiLoggingFilter.java
                        ├── mapper/
                        │   ├── GlobalExceptionMapper.java
                        │   ├── LinkedResourceNotFoundExceptionMapper.java
                        │   ├── RoomNotEmptyExceptionMapper.java
                        │   └── SensorUnavailableExceptionMapper.java
                        ├── model/
                        │   ├── ApiError.java
                        │   ├── Room.java
                        │   ├── Sensor.java
                        │   └── SensorReading.java
                        ├── resource/
                        │   ├── DiscoveryResource.java
                        │   ├── RoomResource.java
                        │   ├── SensorReadingResource.java
                        │   └── SensorResource.java
                        └── store/
                            └── ...
```

### Purpose of the main packages

- **config** — registers resources, exception mappers, Jackson, and filters.
- **model** — POJO classes representing the Smart Campus entities.
- **resource** — REST endpoint classes.
- **exception** — custom exceptions for coursework-specific business rules.
- **mapper** — exception mappers that convert Java exceptions into proper HTTP responses.
- **filter** — request/response logging.
- **store** — in-memory data storage using Java collections.

---

## Build & Run Instructions

### Prerequisites

Make sure you have:

- Java installed
- Maven installed
- IntelliJ IDEA or NetBeans (recommended)

### Step 1 - Open the project

Open the Maven project in IntelliJ or NetBeans.

### Step 2 - Let Maven download dependencies

If prompted, allow Maven to import all project dependencies.

### Step 3 - Run the API

Run the `Main.java` class.

### Step 4 - Server base URI

When the server starts successfully, the console should show something similar to:

```text
Smart Campus API started.
Base URI: http://localhost:8080/api/v1/
Press Enter to stop the server...
```

### Step 5 - Verify the server

Open Postman or a browser and send:

```text
GET http://localhost:8080/api/v1/
```

If everything is working, the API should return a JSON discovery response.

---

## API Endpoints Reference

| Method | Endpoint | Description | Expected Status |
|---|---|---|---|
| GET | `/api/v1/` | Discovery endpoint with API metadata | 200 |
| GET | `/api/v1/rooms` | Get all rooms | 200 |
| POST | `/api/v1/rooms` | Create a new room | 200 / 201 |
| GET | `/api/v1/rooms/{roomId}` | Get one room by ID | 200 / 404 |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room if no sensors are assigned | 200 / 204 / 404 / 409 |
| GET | `/api/v1/sensors` | Get all sensors | 200 |
| GET | `/api/v1/sensors?type=CO2` | Filter sensors by type | 200 |
| POST | `/api/v1/sensors` | Create a sensor and validate `roomId` | 200 / 201 / 422 |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history for a sensor | 200 |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a reading to a sensor | 200 / 201 / 403 |

> Note: Depending on the implementation details, successful creation may return either **200 OK** with the created entity or **201 Created**. Both patterns are acceptable if the response is meaningful and consistent.

---

## Sample curl Commands

### 1. Discovery endpoint

```bash
curl -X GET http://localhost:8080/api/v1/
```

### 2. Create a room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"ENG-201\",\"name\":\"Engineering Lab\",\"capacity\":25}"
```

### 3. Get all rooms

```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### 4. Get a single room

```bash
curl -X GET http://localhost:8080/api/v1/rooms/ENG-201
```

### 5. Create a valid sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":410.0,\"roomId\":\"ENG-201\"}"
```

### 6. Get all sensors

```bash
curl -X GET http://localhost:8080/api/v1/sensors
```

### 7. Filter sensors by type

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 8. Add a reading to a sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"R-CO2-001\",\"timestamp\":1713730000000,\"value\":425.5}"
```

### 9. Get sensor reading history

```bash
curl -X GET http://localhost:8080/api/v1/sensors/CO2-001/readings
```

### 10. Attempt to delete a room with sensors (expect 409)

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/ENG-201
```

### 11. Attempt to create a sensor with an invalid roomId (expect 422)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"BAD-001\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":22.0,\"roomId\":\"NO-ROOM\"}"
```

### 12. Attempt to post a reading to a MAINTENANCE sensor (expect 403)

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-002/readings \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"R-002\",\"timestamp\":1713730100000,\"value\":24}"
```

---

## Postman Test Scenarios

The API was tested in Postman using the following sequence:

1. **Discovery test** - `GET /api/v1/`
2. **Create room** - `POST /rooms`
3. **Get all rooms** - `GET /rooms`
4. **Get one room** - `GET /rooms/{roomId}`
5. **Create valid sensor** - `POST /sensors`
6. **Filter sensors** - `GET /sensors?type=...`
7. **Add sensor reading** - `POST /sensors/{sensorId}/readings`
8. **Get reading history** - `GET /sensors/{sensorId}/readings`
9. **Delete room with linked sensors** - expect `409 Conflict`
10. **Create sensor with invalid roomId** - expect `422 Unprocessable Entity`
11. **Post reading to maintenance sensor** - expect `403 Forbidden`

These tests demonstrate both the success paths and the coursework-required error scenarios.

---

## Report: Question Answers

## Part 1 - Service Architecture & Setup

### 1. Default lifecycle of a JAX-RS resource class

By default, JAX-RS resource classes are generally treated as **request-scoped**, which means a new resource object is created for each incoming HTTP request. This behaviour is useful because it avoids storing shared mutable state directly inside a resource instance.

This design has an important effect on in-memory APIs. If data were stored only as instance variables inside the resource classes, the data would be lost after each request because the object would be discarded. For this reason, the application stores shared data in separate in-memory structures rather than depending on temporary resource instances.

This also has implications for concurrency. In a server environment, multiple requests may arrive at the same time, so shared collections must be managed carefully to avoid race conditions or inconsistent updates. Even though this coursework uses in-memory storage instead of a database, the API still needs a design where data survives across requests and can be accessed consistently by different resource classes.

### 2. Why hypermedia is important in RESTful design

Hypermedia, often discussed through the HATEOAS principle, improves RESTful desi
gn because the API can guide the client by exposing useful entry points and navigation information. Instead of forcing developers to rely entirely on external documentation, the API can provide links or paths to important resources such as rooms and sensors.

This benefits client developers because it improves discoverability and reduces hardcoded assumptions. A client can start from the root discovery endpoint and learn where the main collections are located. This makes the API easier to understand, easier to integrate, and more adaptable if the design evolves in the future.

---

## Part 2 - Room Management

### 1. Returning only IDs versus returning full room objects

Returning only room IDs can reduce payload size and save bandwidth, especially when the system contains many rooms. However, this approach shifts more work onto the client because the client must send additional requests to retrieve details such as room name, capacity, and linked sensors.

Returning full room objects provides a richer response that is more convenient for the client. It reduces the number of follow-up requests and makes the API easier to test and use, especially in administrative dashboards where detailed information is usually needed immediately.

In this project, returning full room objects is appropriate because it improves usability and gives meaningful metadata in a single response. The trade-off is a slightly larger payload, but for this coursework scenario the benefit to the client outweighs the network cost.

### 2. Idempotency of the DELETE operation

The DELETE method is considered **idempotent** because sending the same request multiple times should not continue changing the state of the system after the first meaningful result. If a room is deleted successfully once, sending the same DELETE request again should simply leave the system in the same final state.

In this implementation, if a room still contains sensors, repeated DELETE requests will consistently fail with the same `409 Conflict` response until the sensors are removed. If a room is deleted successfully, any later repeated request would not delete it again because it is already gone. Therefore, the operation is idempotent in terms of system state, even if the response message or status code may differ in later attempts.

---

## Part 3 — Sensor Operations & Linking

### 1. What happens if the client sends the wrong Content-Type

The POST methods use `@Consumes(MediaType.APPLICATION_JSON)` to explicitly state that the API expects JSON input. If a client sends the request body in another format, such as `text/plain` or `application/xml`, the JAX-RS runtime cannot match the payload to the expected method contract in the normal way.

As a result, the request will not be processed as intended, and the framework will usually return an unsupported media type error or another request-format-related failure. This is useful because it enforces a clear contract between client and server. The API only accepts data in the format it was designed to process, which improves consistency and reduces parsing ambiguity.

### 2. Why query parameters are better for filtering collections

Using a query parameter such as `/sensors?type=CO2` is the better RESTful approach for filtering because the client is still asking for the same base collection  the sensors collection but with additional selection criteria applied.

A path such as `/sensors/type/CO2` makes the filter look like a completely separate resource path rather than a filtered view of the same collection. Query parameters are more flexible and scalable because more filters can be added later without redesigning the path structure. For example, it would be easy to support combinations such as `?type=CO2&status=ACTIVE` in the future.

For these reasons, `@QueryParam` is generally the cleaner and more conventional design for filtering and searching in REST APIs.

---

## Part 4 — Deep Nesting with Sub-Resources

### 1. Benefits of the Sub-Resource Locator pattern

The Sub-Resource Locator pattern improves API design by separating nested resource logic into dedicated classes. In this coursework, the main sensor collection logic belongs in `SensorResource`, while the reading history for a specific sensor belongs in `SensorReadingResource`.

This division improves readability and maintainability because each class has a focused responsibility. If every nested path were implemented inside one very large resource class, the file would become harder to understand, harder to test, and harder to extend. By delegating reading-related operations to a separate sub-resource, the API becomes more modular and easier to manage as it grows.

### 2. Historical data management and parent sensor consistency

The readings sub-resource stores the history of measurements for a specific sensor. This allows clients to retrieve past reading data while still keeping the sensor's current state available at the main sensor level.

A successful POST to a reading must also update the `currentValue` field of the parent sensor. This side effect is important because it keeps the API internally consistent. The latest measurement should be reflected both in the reading history and in the current sensor state so that clients can quickly access the most recent value without having to inspect the full history every time.

---

## Part 5 — Advanced Error Handling, Exception Mapping & Logging

### 1. Resource conflict — HTTP 409 Conflict

The API returns `409 Conflict` when a client attempts to delete a room that still has sensors assigned to it. This is the correct status code because the request is understood, but it conflicts with a business rule in the system.

This design prevents orphaned sensor records and reflects the coursework requirement that rooms cannot be deleted while they still contain active hardware.

### 2. Why HTTP 422 is better than 404 in this scenario

HTTP `422 Unprocessable Entity` is more semantically accurate than `404 Not Found` when the problem is a missing reference inside a valid JSON payload. In the invalid sensor-creation scenario, the endpoint `/api/v1/sensors` does exist, and the JSON request body is syntactically correct. The issue is that one field inside the payload  `roomId`  contains an invalid reference.

A `404` would suggest that the requested URL itself does not exist, which is misleading. A `422` correctly communicates that the server understood the request but could not process it because of a semantic validation error inside the request body.

### 3. State constraint — HTTP 403 Forbidden

The API returns `403 Forbidden` when a client tries to add a new reading to a sensor whose status is `MAINTENANCE`. This is appropriate because the request is recognised, but the current state of the target sensor does not allow the operation to proceed.

This clearly communicates that the failure is caused by a business rule rather than by a missing endpoint or malformed request.

### 4. Cybersecurity risks of exposing Java stack traces

Exposing raw Java stack traces to external API users is a security risk because stack traces reveal internal implementation details. An attacker could learn class names, package structure, library information, method names, and even file paths or internal logic flow. This information can make it easier to identify weaknesses or plan targeted attacks.

To avoid this, the API uses a global exception mapper that converts unexpected failures into safe, structured JSON responses. This ensures that technical details remain internal while clients still receive a clear and consistent error message.

### 5. Why JAX-RS filters are better for logging

JAX-RS filters are the correct tool for cross-cutting concerns such as logging because they apply centrally to all requests and responses. This prevents the need to place repetitive `Logger.info()` statements inside every resource method.

Using filters improves maintainability, keeps the resource classes cleaner, and ensures that logging is consistent throughout the entire API. It also makes future changes easier, because the logging behaviour can be updated in one place rather than across many endpoint methods.

---

## Error Handling Summary

The API implements the following coursework-required error handling behaviour:

| Scenario | Status Code | Explanation |
|---|---|---|
| Delete room that still has sensors | 409 Conflict | Room cannot be removed while hardware is still assigned |
| Create sensor with non-existent `roomId` | 422 Unprocessable Entity | Request body is valid JSON but semantically invalid |
| Add reading to maintenance sensor | 403 Forbidden | Sensor state blocks the operation |
| Unexpected server error | 500 Internal Server Error | Global safety net prevents stack trace leakage |

All error responses are returned as structured JSON using custom exception mappers.

---

## Logging and Observability

The API includes request and response logging using JAX-RS filter interfaces.

The logging filter records:

- incoming HTTP method
- target URI
- outgoing response status code

This makes the API easier to test, debug, and demonstrate. It also supports the coursework requirement for API observability without mixing logging statements into every business method.

Example console behaviour:

```text
Incoming request: GET http://localhost:8080/api/v1/rooms
Outgoing response: HTTP 200
Incoming request: DELETE http://localhost:8080/api/v1/rooms/ENG-201
Outgoing response: HTTP 409
```

---

## Conclusion

This project delivers a complete Smart Campus REST API in line with the coursework specification. It demonstrates:

- correct use of JAX-RS resource design
- nested resource handling
- in-memory persistence without databases
- business-rule enforcement
- exception mapping with appropriate HTTP status codes
- API logging through filters
- successful testing through Postman and curl

Overall, the implementation satisfies the key technical and conceptual requirements of the coursework while following clear RESTful design principles and safe API engineering practices.
