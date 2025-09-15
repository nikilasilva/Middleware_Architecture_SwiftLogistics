Here’s a well-organized **README.md** version of your SwiftLogistics ESB Starter project with sorted `curl` commands, outputs, and updated ports:

````markdown
# SwiftLogistics ESB Starter

This project is a Spring Boot + Apache Camel ESB prototype that integrates with the Python mock services:

- **CMS (SOAP):** http://localhost:5001  
- **ROS (REST):** http://localhost:5002  
- **WMS (TCP):** Port 5003  

---

## Table of Contents
1. [Build & Run](#build--run)
2. [Applications & Ports](#applications--ports)
3. [Curl Test Commands](#curl-test-commands)
   - [Order Creation](#test-order-creation)
   - [Order Status Retrieval](#test-order-status-retrieval)
   - [Order Status Update](#test-order-status-update)
   - [Health Checks](#test-health-checks)
   - [Legacy ESB Endpoints](#test-legacy-esb-endpoints)
   - [Additional Integration Tests](#additional-integration-tests)
   - [Error Scenarios](#test-error-scenarios)
   - [Verbose & Timing Tests](#test-with-verbose-output)
4. [Expected Responses](#expected-response-examples)

---

## Build & Run

**Build:**
```bash
mvn clean package
````

**Run:**

```bash
java -jar target/esb-starter-0.0.1-SNAPSHOT.jar
```

**Or with Docker Compose:**

```bash
docker-compose up --build
```

---

## Applications & Ports

| Application          | Port |
| -------------------- | ---- |
| Eureka Server        | 8761 |
| Notification Service | 8088 |
| Order Service        | 8081 |
| API Gateway          | 8089 |
| ESB Application      | 8084 |

---

## Curl Test Commands

### Test Order Creation

```bash
# API Gateway
curl -X POST "http://localhost:8089/api/orders" \
     -H "Content-Type: application/json" \
     -d '{
       "clientId": "CLIENT123",
       "deliveryAddress": "123 Main St, City",
       "pickupAddress": "456 Warehouse Ave",
       "packageDetails": "Electronics package"
     }'

# Direct Order Service
curl -X POST "http://localhost:8081/api/orders" \
     -H "Content-Type: application/json" \
     -d '{
       "clientId": "CLIENT456",
       "deliveryAddress": "789 Oak Street, Downtown",
       "pickupAddress": "101 Industrial Blvd",
       "packageDetails": "Medical supplies"
     }'
```

**Expected Output:**

```json
{
    "success": true,
    "orderId": "ORD1694604707179",
    "clientValidation": "Client CLIENT123 validated successfully",
    "routeId": "Route optimized for 123 Main St, City",
    "wmsStatus": "Warehouse status: Available",
    "processedBy": "order-service",
    "timestamp": 1694604707179
}
```

---

### Test Order Status Retrieval

```bash
# API Gateway
curl -X GET "http://localhost:8089/api/orders/ORD001/status" \
     -H "Content-Type: application/json"

# Direct Order Service
curl -X GET "http://localhost:8081/api/orders/ORD001/status" \
     -H "Content-Type: application/json"

# Direct ESB
curl -X GET "http://localhost:8084/orders/ORD001/status" \
     -H "Content-Type: application/json"

# Test with other order IDs
curl -X GET "http://localhost:8089/api/orders/ORD123456789/status" \
     -H "Content-Type: application/json"
```

**Sample Output:**

```json
{
    "orderId": "ORD001",
    "cmsStatus": "confirmed",
    "routeStatus": "assigned",
    "packageStatus": "ready_for_pickup",
    "timestamp": 1694604707179,
    "queriedBy": "order-service",
    "queryTimestamp": 1694604708000
}
```

---

### Test Order Status Update

```bash
# Update all systems through API Gateway
curl -X PUT "http://localhost:8089/api/orders/ORD001/status?status=shipped" \
     -H "Content-Type: application/json"

# Update CMS only
curl -X PUT "http://localhost:8089/api/orders/ORD001/status?status=delivered&system=CMS" \
     -H "Content-Type: application/json"

# Update ROS only
curl -X PUT "http://localhost:8089/api/orders/ORD001/status?status=in_transit&system=ROS" \
     -H "Content-Type: application/json"

# Update WMS only
curl -X PUT "http://localhost:8089/api/orders/ORD001/status?status=ready_for_pickup&system=WMS" \
     -H "Content-Type: application/json"

# Direct ESB call
curl -X PUT "http://localhost:8084/orders/ORD001/status?status=processing" \
     -H "Content-Type: application/json"
```

**Sample Output:**

```json
{
    "rosUpdate": "ROS route for order ORD001 updated to shipped successfully",
    "newStatus": "shipped",
    "orderId": "ORD001",
    "success": true,
    "wmsUpdate": "Unknown package update response type: 8",
    "cmsUpdate": "CMS order ORD001 status updated to shipped successfully (mock response)",
    "timestamp": 1757735485904
}
```

---

### Test Health Checks

```bash
# API Gateway -> Order Service -> ESB
curl -X GET "http://localhost:8089/api/orders/health" \
     -H "Content-Type: application/json"

# Direct Order Service
curl -X GET "http://localhost:8081/api/orders/health" \
     -H "Content-Type: application/json"

# Direct ESB
curl -X GET "http://localhost:8084/health" \
     -H "Content-Type: application/json"
```

**Sample Output:**

```json
{
    "orderService": "UP",
    "esbConnectivity": "UP",
    "esbHealth": {
        "cms": {"status": "UP"},
        "ros": {"status": "UP"},
        "wms": {"status": "UP"},
        "overall": "UP",
        "timestamp": 1694604707179
    },
    "overall": "UP",
    "timestamp": 1694604708000
}
```

---

### Test Legacy ESB Endpoints

```bash
# Process order
curl -X GET "http://localhost:8084/esb/processOrder?clientId=CLIENT123&address=Test Address" \
     -H "Content-Type: application/json"

# Create order with map
curl -X POST "http://localhost:8084/orders/map" \
     -H "Content-Type: application/json" \
     -d '{
       "orderId": "TEST001",
       "clientId": "CLIENT123",
       "deliveryAddress": "Test Address",
       "pickupAddress": "Test Pickup"
     }'
```

---

### Additional Integration Tests

```bash
ORDER_RESPONSE=$(curl -s -X POST "http://localhost:8089/api/orders" \
     -H "Content-Type: application/json" \
     -d '{
       "clientId": "CLIENT999",
       "deliveryAddress": "999 Integration Test St",
       "pickupAddress": "999 Source Warehouse",
       "packageDetails": "Integration test package"
     }')

ORDER_ID="ORD$(date +%s)"

curl -X GET "http://localhost:8089/api/orders/$ORDER_ID/status" -H "Content-Type: application/json"
curl -X PUT "http://localhost:8089/api/orders/$ORDER_ID/status?status=confirmed" -H "Content-Type: application/json"
curl -X GET "http://localhost:8089/api/orders/$ORDER_ID/status" -H "Content-Type: application/json"
```

---

### Test Error Scenarios

```bash
# Invalid order ID
curl -X GET "http://localhost:8089/api/orders/INVALID_ID/status" -H "Content-Type: application/json"

# Missing required fields
curl -X POST "http://localhost:8089/api/orders" -H "Content-Type: application/json" \
     -d '{"clientId": "CLIENT123"}'

# Invalid status
curl -X PUT "http://localhost:8089/api/orders/ORD001/status?status=INVALID_STATUS" -H "Content-Type: application/json"

# Services down
curl -X GET "http://localhost:8089/api/orders/health" -H "Content-Type: application/json" --max-time 5
```

---

### Test with Verbose Output

```bash
# Verbose POST
curl -v -X POST "http://localhost:8089/api/orders" \
     -H "Content-Type: application/json" \
     -d '{
       "clientId": "CLIENT123",
       "deliveryAddress": "123 Main St, City",
       "pickupAddress": "456 Warehouse Ave",
       "packageDetails": "Electronics package"
     }'

# Timing info
curl -w "@curl-format.txt" -X GET "http://localhost:8089/api/orders/ORD001/status" \
     -H "Content-Type: application/json"
```

**curl-format.txt for timing info:**

```text
time_namelookup:  %{time_namelookup}\n
time_connect:  %{time_connect}\n
time_appconnect:  %{time_appconnect}\n
time_pretransfer:  %{time_pretransfer}\n
time_redirect:  %{time_redirect}\n
time_starttransfer:  %{time_starttransfer}\n
----------\n
time_total:  %{time_total}\n
```

---

## Notes

* ESB endpoints communicate with CMS, ROS, and WMS mock services.
* API Gateway routes traffic to Order Service and ESB.
* Ports have been updated for your Spring Boot applications.
* Use `--max-time` for quick health checks if services are down.


## Quick Reference: Curl Commands & Expected Outputs

| Function | Endpoint / Command | Expected Output |
|----------|------------------|----------------|
| **Create Order (API Gateway)** | `POST http://localhost:8089/api/orders`<br>`-d '{"clientId":"CLIENT123","deliveryAddress":"123 Main St","pickupAddress":"456 Warehouse Ave","packageDetails":"Electronics"}'` | `{"success":true,"orderId":"ORD1694604707179","clientValidation":"Client CLIENT123 validated successfully","routeId":"Route optimized for 123 Main St, City","wmsStatus":"Warehouse status: Available","processedBy":"order-service","timestamp":1694604707179}` |
| **Create Order (Direct Order Service)** | `POST http://localhost:8081/api/orders`<br>`-d '{"clientId":"CLIENT456","deliveryAddress":"789 Oak Street","pickupAddress":"101 Industrial Blvd","packageDetails":"Medical supplies"}'` | Similar success JSON with orderId and timestamps |
| **Get Order Status (API Gateway)** | `GET http://localhost:8089/api/orders/ORD001/status` | `{"orderId":"ORD001","cmsStatus":"confirmed","routeStatus":"assigned","packageStatus":"ready_for_pickup","timestamp":1694604707179,"queriedBy":"order-service","queryTimestamp":1694604708000}` |
| **Get Order Status (Direct ESB)** | `GET http://localhost:8084/orders/ORD001/status` | Same as above |
| **Update Order Status (All Systems via API Gateway)** | `PUT http://localhost:8089/api/orders/ORD001/status?status=shipped` | `{"rosUpdate":"ROS route for order ORD001 updated to shipped successfully","newStatus":"shipped","orderId":"ORD001","success":true,"wmsUpdate":"Unknown package update response type: 8","cmsUpdate":"CMS order ORD001 status updated to shipped successfully (mock response)","timestamp":1757735485904}` |
| **Update CMS Only** | `PUT http://localhost:8089/api/orders/ORD001/status?status=delivered&system=CMS` | CMS-specific success JSON |
| **Update ROS Only** | `PUT http://localhost:8089/api/orders/ORD001/status?status=in_transit&system=ROS` | ROS-specific success JSON |
| **Update WMS Only** | `PUT http://localhost:8089/api/orders/ORD001/status?status=ready_for_pickup&system=WMS` | WMS-specific success JSON |
| **Health Check (API Gateway)** | `GET http://localhost:8089/api/orders/health` | `{"orderService":"UP","esbConnectivity":"UP","esbHealth":{"cms":{"status":"UP"},"ros":{"status":"UP"},"wms":{"status":"UP"},"overall":"UP","timestamp":1694604707179},"overall":"UP","timestamp":1694604708000}` |
| **Health Check (Direct ESB)** | `GET http://localhost:8084/health` | Similar JSON with CMS, ROS, WMS status |
| **Legacy ESB: Process Order** | `GET http://localhost:8084/esb/processOrder?clientId=CLIENT123&address=Test Address` | Simple processing JSON for order |
| **Legacy ESB: Create Order Map** | `POST http://localhost:8084/orders/map`<br>`-d '{"orderId":"TEST001","clientId":"CLIENT123","deliveryAddress":"Test Address","pickupAddress":"Test Pickup"}'` | JSON confirming order creation |
| **Error Scenario: Invalid Order ID** | `GET http://localhost:8089/api/orders/INVALID_ID/status` | Error JSON indicating order not found |
| **Error Scenario: Missing Fields** | `POST http://localhost:8089/api/orders`<br>`-d '{"clientId":"CLIENT123"}'` | Error JSON indicating missing required fields |
| **Error Scenario: Invalid Status** | `PUT http://localhost:8089/api/orders/ORD001/status?status=INVALID_STATUS` | Error JSON indicating invalid status |
| **Verbose POST Example** | `curl -v -X POST http://localhost:8089/api/orders ...` | Headers + request/response info |
| **Timing Info Example** | `curl -w "@curl-format.txt" -X GET http://localhost:8089/api/orders/ORD001/status` | Output showing time_namelookup, time_connect, time_total, etc. |
| **Package Tracking (Direct ESB)** | `GET http://localhost:8084/packages/PKG123456/track` | `{"packageId":"PKG123456","warehouseInfo":"Status: Processing, Weight: 1.8kg, Zone: B, Dimensions: 25x15x10cm","orderInfo":"Client: Fashion Hub Lanka, Recipient: Jane Smith, Order Status: processing, Delivery: 456 Oak Ave","routeInfo":"Route: RT002, Vehicle: VEH002, Driver: Nimal Silva, Status: planned, ETA: 2024-01-16 16:00","timestamp":1757911738925}` |
| **Package Tracking via API Gateway** | `GET http://localhost:8089/api/orders/packages/PKG123456/track` | Similar JSON with additional fields:<br>`"servicedBy":"order-service","customerFriendlyStatus":"⚙️ Processing - Your package is being prepared","estimatedDelivery":"2024-01-16 16:00"` |

