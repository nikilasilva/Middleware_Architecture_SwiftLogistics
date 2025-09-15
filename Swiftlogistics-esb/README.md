# SwiftLogistics ESB Starter

This project is a Spring Boot + Apache Camel ESB prototype that integrates with the Python mock services:

- **CMS (SOAP):** http://localhost:5001  
- **ROS (REST):** http://localhost:5002  
- **WMS (TCP):** Port 5003  

---

## Table of Contents
1. [Quick Start Guide](#quick-start-guide)
2. [Build & Run](#build--run)
3. [Applications & Ports](#applications--ports)
4. [Troubleshooting](#troubleshooting)
5. [Curl Test Commands](#curl-test-commands)
   - [Order Creation](#test-order-creation)
   - [Package Tracking](#test-package-tracking)
   - [Route Optimization](#test-route-optimization)
   - [Order Status Management](#test-order-status-management)
   - [Health Checks](#test-health-checks)
   - [Legacy ESB Endpoints](#test-legacy-esb-endpoints)
   - [Error Scenarios](#test-error-scenarios)
6. [Expected Responses](#expected-response-examples)

---

## Quick Start Guide

### 1. Start Backend Mock Services (Required)

**⚠️ CRITICAL: Start all Python services FIRST before Spring Boot applications!**

```bash
# Terminal 1 - CMS Server (SOAP/XML)
cd d:\Middleware_Architecture_SwiftLogistics\prototypes
python cms_server.py

# Terminal 2 - ROS Server (REST/JSON)
cd d:\Middleware_Architecture_SwiftLogistics\prototypes
python ros_server.py

# Terminal 3 - WMS Server (TCP Binary)
cd d:\Middleware_Architecture_SwiftLogistics\prototypes
python wms_server.py

# Terminal 4 - RabbitMQ (if using Docker)
docker run -d --hostname my-rabbit --name some-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 2. Start Spring Boot Applications

```bash
# Terminal 5 - Eureka Server
cd d:\Middleware_Architecture_SwiftLogistics\eureka-server
mvn spring-boot:run

# Terminal 6 - ESB Service
cd d:\Middleware_Architecture_SwiftLogistics\Swiftlogistics-esb
mvn spring-boot:run

# Terminal 7 - Order Service
cd d:\Middleware_Architecture_SwiftLogistics\order-service
mvn spring-boot:run

# Terminal 8 - Notification Service
cd d:\Middleware_Architecture_SwiftLogistics\notification-service
mvn spring-boot:run

# Terminal 9 - API Gateway
cd d:\Middleware_Architecture_SwiftLogistics\api-gateway
mvn spring-boot:run
```

### 3. Verify All Services

```bash
# Backend Services
curl http://localhost:5001/cms/health    # CMS (should return HTTP 200)
curl http://localhost:5002/api/v1/health # ROS (should return health info)
# WMS - check terminal for "WMS Server listening on localhost:5003"

# Spring Boot Services
curl http://localhost:8761/             # Eureka (should show dashboard)
curl http://localhost:8084/health       # ESB (should show all systems UP)
curl http://localhost:8081/api/orders/health # Order Service
curl http://localhost:8088/health       # Notification Service
curl http://localhost:8089/actuator/health  # API Gateway
```

---

## Build & Run

**Build:**
```bash
mvn clean package
```

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

| Application          | Port | Type | Notes |
| -------------------- | ---- | ---- | ----- |
| **Backend Services** |      |      |       |
| CMS (SOAP)           | 5001 | Python | Must be running first |
| ROS (REST)           | 5002 | Python | Must be running first |
| WMS (TCP)            | 5003 | Python | Must be running first |
| RabbitMQ             | 5672 | Docker | Message broker |
| RabbitMQ Management  | 15672| Docker | Web UI (guest/guest) |
| **Spring Boot Apps** |      |        |       |
| Eureka Server        | 8761 | Spring | Service discovery |
| ESB Application      | 8084 | Spring | Main integration hub |
| Order Service        | 8081 | Spring | Customer-facing API |
| Notification Service | 8088 | Spring | Event processing |
| API Gateway          | 8089 | Spring | External entry point |

---

## Troubleshooting

### Common Issues & Solutions

#### 1. Connection Refused Errors
```
Error: I/O error on POST request for "http://localhost:5001/cms/soap": Connection refused
```
**Solution:** Start Python backend services first:
```bash
cd d:\Middleware_Architecture_SwiftLogistics\prototypes
python cms_server.py  # Terminal 1
python ros_server.py  # Terminal 2  
python wms_server.py  # Terminal 3
```

#### 2. Client Not Found SOAP Fault
```
Error: 404 NOT FOUND: Client not found
```
**Solution:** Use valid client IDs from CMS mock data:
- `CLIENT001` (TechMart Electronics)
- `CLIENT002` (Fashion Hub Lanka)


#### 4. Package Tracking Method Missing
```
Error: Method getPackageInfo not found
```
**Solution:** Ensure all tracking methods are implemented in service classes.

### Service Dependencies

```
Client Request → API Gateway (8089) → Order Service (8081) → ESB (8084) → {CMS (5001), ROS (5002), WMS (5003)}
                                                                  ↓
                                                            RabbitMQ (5672) → Notification Service (8088)
```

---

## Curl Test Commands

### Test Order Creation

```bash
# ✅ API Gateway (Recommended)
curl -X POST "http://localhost:8089/api/orders" \
     -H "Content-Type: application/json" \
     -d '{
       "clientId": "CLIENT001",
       "deliveryAddress": "123 Main St, City",
       "pickupAddress": "456 Warehouse Ave",
       "packageDetails": "Electronics package"
     }'

# ✅ Direct Order Service
curl -X POST "http://localhost:8081/api/orders" \
     -H "Content-Type: application/json" \
     -d '{
       "clientId": "CLIENT002",
       "deliveryAddress": "789 Oak Street, Downtown",
       "pickupAddress": "101 Industrial Blvd",
       "packageDetails": "Medical supplies"
     }'

# ✅ Use Valid Client IDs Only
# CLIENT001 = TechMart Electronics
# CLIENT002 = Fashion Hub Lanka
```

**Expected Success Output:**
```json
{
    "success": true,
    "orderId": "ORD1694604707179",
    "clientValidation": "Client CLIENT001 validated successfully",
    "routeId": "Route optimized: RT20250915122854214",
    "wmsStatus": "Warehouse status: 3 packages",
    "processedBy": "order-service",
    "timestamp": 1694604707179
}
```

---

### Test Package Tracking

```bash
# ✅ Track by Package ID (API Gateway - Recommended)
curl -X GET "http://localhost:8089/api/orders/packages/PKG123456/track" \
     -H "Content-Type: application/json"

# ✅ Track by Order ID (API Gateway - Convenience)
curl -X GET "http://localhost:8089/api/orders/ORD1234567890/track" \
     -H "Content-Type: application/json"

# ✅ Direct ESB Package Tracking
curl -X GET "http://localhost:8084/packages/PKG123456/track" \
     -H "Content-Type: application/json"

# Test with different package IDs
curl -X GET "http://localhost:8089/api/orders/packages/PKG789/track" \
     -H "Content-Type: application/json"

curl -X GET "http://localhost:8089/api/orders/packages/PKG001/track" \
     -H "Content-Type: application/json"
```

**Expected Package Tracking Output:**
```json
{
    "packageId": "PKG123456",
    "warehouseInfo": "Status: Processing, Weight: 1.8kg, Zone: B, Dimensions: 25x15x10cm",
    "orderInfo": "Client: Fashion Hub Lanka, Recipient: Jane Smith, Order Status: processing, Delivery: 456 Oak Ave",
    "routeInfo": "Route: RT002, Vehicle: VEH002, Driver: Nimal Silva, Status: planned, ETA: 2024-01-16 16:00",
    "timestamp": 1757914259042,
    "servicedBy": "order-service",
    "trackingRequestTime": 1757914259308,
    "customerFriendlyStatus": "⚙️ Processing - Your package is being prepared",
    "estimatedDelivery": "2024-01-16 16:00"
}
```

---

### Test Route Optimization

```bash
# ✅ Route Optimization (Direct ESB)
curl -X POST "http://localhost:8084/routes/optimize" \
     -H "Content-Type: application/json" \
     -d '{
       "vehicleId": "VEH001",
       "address": "123 Main Street, Colombo"
     }'

# ✅ Route Optimization with Multiple Addresses
curl -X POST "http://localhost:8084/routes/optimize" \
     -H "Content-Type: application/json" \
     -d '{
       "vehicleId": "VEH002",
       "address": "456 Galle Road, Mount Lavinia"
     }'

# ✅ Route Optimization via API Gateway (if proxied)
curl -X POST "http://localhost:8089/api/orders/routes/optimize" \
     -H "Content-Type: application/json" \
     -d '{
       "vehicleId": "VEH004",
       "address": "101 Negombo Road, Ja-Ela"
     }'
```

**Expected Route Optimization Output:**
```json
{
    "success": true,
    "routeOptimization": "Route optimized for 123 Main Street, Colombo",
    "vehicleId": "VEH001",
    "estimatedTime": "45 minutes",
    "distance": "12.5 km"
}
```

---

### Test Order Status Management

```bash
# ✅ Get Order Status (API Gateway)
curl -X GET "http://localhost:8089/api/orders/ORD001/status" \
     -H "Content-Type: application/json"

# ✅ Update Order Status - All Systems
curl -X PUT "http://localhost:8089/api/orders/ORD001/status?status=shipped" \
     -H "Content-Type: application/json"

# ✅ Update CMS Only
curl -X PUT "http://localhost:8089/api/orders/ORD001/status?status=delivered&system=CMS" \
     -H "Content-Type: application/json"

# ✅ Update ROS Only
curl -X PUT "http://localhost:8089/api/orders/ORD001/status?status=in_transit&system=ROS" \
     -H "Content-Type: application/json"

# ✅ Update WMS Only
curl -X PUT "http://localhost:8089/api/orders/ORD001/status?status=ready_for_pickup&system=WMS" \
     -H "Content-Type: application/json"

# ✅ Direct ESB Status Update
curl -X PUT "http://localhost:8084/orders/ORD001/status?status=processing" \
     -H "Content-Type: application/json"
```

**Expected Status Update Output:**
```json
{
    "rosUpdate": "ROS route for order ORD001 updated to shipped successfully",
    "newStatus": "shipped",
    "orderId": "ORD001",
    "success": true,
    "wmsUpdate": "Package status updated successfully",
    "cmsUpdate": "CMS order ORD001 status updated to shipped successfully",
    "timestamp": 1757735485904
}
```

---

### Test Health Checks

```bash
# ✅ Comprehensive Health Check (API Gateway → Order Service → ESB)
curl -X GET "http://localhost:8089/api/orders/health" \
     -H "Content-Type: application/json"

# ✅ Order Service Health
curl -X GET "http://localhost:8081/api/orders/health" \
     -H "Content-Type: application/json"

# ✅ ESB Health Check
curl -X GET "http://localhost:8084/health" \
     -H "Content-Type: application/json"

# ✅ Individual Service Health Checks
curl -X GET "http://localhost:8761/"              # Eureka Dashboard
curl -X GET "http://localhost:8088/health"        # Notification Service
curl -X GET "http://localhost:8089/actuator/health" # API Gateway

# ✅ Backend Service Health
curl -X GET "http://localhost:5001/cms/health"    # CMS
curl -X GET "http://localhost:5002/api/v1/health" # ROS
# WMS: Check terminal output for "listening" message
```

**Expected Health Check Output:**
```json
{
    "orderService": "UP",
    "esbConnectivity": "UP",
    "esbHealth": {
        "cms": {"status": "UP", "details": "SOAP service responding"},
        "ros": {"status": "UP", "details": "REST API responding"},
        "wms": {"status": "UP", "details": "TCP connection established"},
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
# ✅ Simple Process Order
curl -X GET "http://localhost:8084/esb/processOrder?clientId=CLIENT001&address=Test Address" \
     -H "Content-Type: application/json"

# ✅ Create Order with Map
curl -X POST "http://localhost:8084/orders/map" \
     -H "Content-Type: application/json" \
     -d '{
       "orderId": "TEST001",
       "clientId": "CLIENT001",
       "deliveryAddress": "Test Address",
       "pickupAddress": "Test Pickup"
     }'

# ✅ ESB Connection Test
curl -X GET "http://localhost:8081/api/orders/test/CLIENT001" \
     -H "Content-Type: application/json"
```

---

### Test Error Scenarios

```bash
# ❌ Invalid Client ID (should fail gracefully)
curl -X POST "http://localhost:8089/api/orders" \
     -H "Content-Type: application/json" \
     -d '{
       "clientId": "INVALID_CLIENT",
       "deliveryAddress": "123 Main St",
       "pickupAddress": "456 Warehouse Ave",
       "packageDetails": "Test package"
     }'

# ❌ Missing Required Fields
curl -X POST "http://localhost:8089/api/orders" \
     -H "Content-Type: application/json" \
     -d '{"clientId": "CLIENT001"}'

# ❌ Invalid Order ID
curl -X GET "http://localhost:8089/api/orders/INVALID_ID/status" \
     -H "Content-Type: application/json"

# ❌ Invalid Package ID
curl -X GET "http://localhost:8089/api/orders/packages/INVALID_PKG/track" \
     -H "Content-Type: application/json"

# ❌ Invalid Status Update
curl -X PUT "http://localhost:8089/api/orders/ORD001/status?status=INVALID_STATUS" \
     -H "Content-Type: application/json"

# ✅ Health Check When Services Down
curl -X GET "http://localhost:8089/api/orders/health" \
     -H "Content-Type: application/json" --max-time 5
```

---

### Advanced Testing

```bash
# 🔄 Integration Test Workflow
ORDER_RESPONSE=$(curl -s -X POST "http://localhost:8089/api/orders" \
     -H "Content-Type: application/json" \
     -d '{
       "clientId": "CLIENT001",
       "deliveryAddress": "999 Integration Test St",
       "pickupAddress": "999 Source Warehouse",
       "packageDetails": "Integration test package"
     }')

echo "Order created: $ORDER_RESPONSE"

# Extract order ID and test tracking
ORDER_ID="ORD$(date +%s)"
curl -X GET "http://localhost:8089/api/orders/$ORDER_ID/status" -H "Content-Type: application/json"
curl -X PUT "http://localhost:8089/api/orders/$ORDER_ID/status?status=confirmed" -H "Content-Type: application/json"

# 📊 Performance Testing
curl -w "@curl-format.txt" -X GET "http://localhost:8089/api/orders/health" \
     -H "Content-Type: application/json"

# 🔍 Verbose Testing
curl -v -X POST "http://localhost:8089/api/orders" \
     -H "Content-Type: application/json" \
     -d '{
       "clientId": "CLIENT002",
       "deliveryAddress": "Verbose Test Address",
       "pickupAddress": "Verbose Test Pickup",
       "packageDetails": "Verbose test package"
     }'
```

**curl-format.txt for performance timing:**
```text
time_namelookup:  %{time_namelookup}s\n
time_connect:     %{time_connect}s\n
time_appconnect:  %{time_appconnect}s\n
time_pretransfer: %{time_pretransfer}s\n
time_redirect:    %{time_redirect}s\n
time_starttransfer: %{time_starttransfer}s\n
----------\n
time_total:       %{time_total}s\n
```

---

## Expected Response Examples

### Successful Order Creation
```json
{
    "success": true,
    "orderId": "ORD1757919534142",
    "clientValidation": "Client CLIENT001 validated successfully",
    "routeId": "Route optimized: RT20250915122854214",
    "wmsStatus": "Warehouse status: 3 packages",
    "processedBy": "order-service",
    "timestamp": 1757919534312
}
```

### Package Tracking Response
```json
{
    "packageId": "PKG123456",
    "warehouseInfo": "Status: In Transit, Weight: 2.5kg, Zone: A, Dimensions: 30x20x15cm",
    "orderInfo": "Client: TechMart Electronics, Recipient: John Doe, Order Status: confirmed, Delivery: 123 Main St",
    "routeInfo": "Route: RT001, Vehicle: VEH001, Driver: Saman Perera, Status: in_progress, ETA: 2024-01-16 14:30",
    "timestamp": 1705317600000,
    "servicedBy": "order-service",
    "trackingRequestTime": 1705317605000,
    "customerFriendlyStatus": "🚚 Out for Delivery - Your package is on its way!",
    "estimatedDelivery": "2024-01-16 14:30"
}
```

### Error Response Examples
```json
{
    "success": false,
    "error": "Client validation failed: Client not found",
    "orderId": null,
    "processedBy": "order-service",
    "timestamp": 1757919534312
}
```

---

## Quick Reference Commands

| Test Type | Command | Expected Result |
|-----------|---------|----------------|
| **Order Creation** | `POST /api/orders` | Success with orderId |
| **Package Tracking** | `GET /api/orders/packages/{id}/track` | Comprehensive tracking info |
| **Order Tracking** | `GET /api/orders/{id}/track` | Package tracking by order ID |
| **Route Optimization** | `POST /routes/optimize` | Optimized route details |
| **Health Check** | `GET /api/orders/health` | All systems status |
| **Status Update** | `PUT /api/orders/{id}/status` | Update confirmation |

---

## Notes

* ✅ Use `CLIENT001` or `CLIENT002` for valid client IDs
* ✅ Always start Python backend services before Spring Boot apps
* ✅ Check service health before running integration tests
* ✅ Use API Gateway (port 8089) for all client-facing requests
* ✅ Package tracking supports both package ID and order ID
* ✅ Route optimization integrates with ROS service automatically
* ⚠️ Services must be started in dependency order
* 🔧 Configure RabbitMQ message converters for notification service

---

## Startup Checklist

- [ ] CMS Server running on port 5001
- [ ] ROS Server running on port 5002  
- [ ] WMS Server running on port 5003
- [ ] RabbitMQ running on port 5672
- [ ] Eureka Server running on port 8761
- [ ] ESB Service running on port 8084
- [ ] Order Service running on port 8081
- [ ] Notification Service running on port 8088
- [ ] API Gateway running on port 8089
- [ ] All health checks passing
- [ ] Test order creation successful
- [ ] Package tracking functional

---

**🚀 Quick Test Command:**
```bash
curl -X POST "http://localhost:8089/api/orders" -H "Content-Type: application/json" -d '{"clientId":"CLIENT001","deliveryAddress":"Test Address","pickupAddress":"Test Pickup","packageDetails":"Test Package"}' | jq