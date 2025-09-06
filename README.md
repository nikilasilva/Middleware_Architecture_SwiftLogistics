python test_servers.py
🚀 SWIFTLOGISTICS SYSTEM PROTOTYPE TESTING
Testing all three heterogeneous systems...

---

###TESTING CMS (SOAP/XML) - Port 5001

1. Testing CMS Health Check...
   Health Status: 200
   Response: {'clients_count': 2, 'orders_count': 0, 'protocol': 'SOAP/XML', 'service': 'CMS', 'status': 'healthy', 'timestamp': '2025-09-05T23:26:03.953024'}

2. Testing Get Client Info (SOAP)...
   SOAP Response Status: 200
   Client Name: TechMart Electronics

3. Testing Create Order (SOAP)...
   Create Order Status: 200
   ✅ Order Created: ORD202509050001

============================================================
TESTING ROS (REST/JSON) - Port 5002
============================================================

1. Testing ROS Health Check...
   Health Status: 200
   Response: {'active_routes': 0, 'available_vehicles': 2, 'protocol': 'REST/JSON', 'service': 'ROS', 'status': 'healthy', 'timestamp': '2025-09-05T23:26:10.048062', 'total_vehicles': 3}

2. Testing Get Vehicles...
   Available Vehicles: 3

- VEH001: Saman Perera (available)
- VEH002: Nimal Silva (available)
- VEH003: Kamala Wijesinghe (busy)

3. Testing Route Optimization...
   Route Optimization Status: 201
   ✅ Route Created: RT20250905232614242
   Total Distance: 8.32 km
   Estimated Time: 21 minutes
   Optimized Stops: 3

============================================================
TESTING WMS (TCP/IP) - Port 5003
============================================================

1. Connecting to WMS TCP Server...
   ✅ Connected to WMS successfully

2. Testing Heartbeat...
   ✅ Heartbeat Response: alive

3. Testing Package Status Request...
   ✅ Package PKG001 Status: READY_FOR_LOADING
   Zone: A, Weight: 2.5kg

4. Testing Warehouse Status...
   ✅ Warehouse Status:
   Total Packages: 3
   Zone Occupancy:
   Zone A: 15/100 packages
   Zone B: 23/150 packages
   Zone C: 45/200 packages

5. Testing New Package Receipt...
   ✅ New Package Added: PKG_TEST_232614
   Assigned Zone: A
   ✅ WMS TCP connection closed

============================================================
INTEGRATION FLOW TEST
============================================================

🔄 Testing complete order processing flow...

📝 Step 1: Creating order in CMS...

============================================================
TESTING CMS (SOAP/XML) - Port 5001
============================================================

1. Testing CMS Health Check...
   Health Status: 200
   Response: {'clients_count': 2, 'orders_count': 1, 'protocol': 'SOAP/XML', 'service': 'CMS', 'status': 'healthy', 'timestamp': '2025-09-05T23:26:16.194210'}

2. Testing Get Client Info (SOAP)...
   SOAP Response Status: 200
   Client Name: TechMart Electronics

3. Testing Create Order (SOAP)...
   Create Order Status: 200
   ✅ Order Created: ORD202509050002

🚚 Step 2: Optimizing delivery route in ROS...

============================================================
TESTING ROS (REST/JSON) - Port 5002
============================================================

1. Testing ROS Health Check...
   Health Status: 200
   Response: {'active_routes': 1, 'available_vehicles': 1, 'protocol': 'REST/JSON', 'service': 'ROS', 'status': 'healthy', 'timestamp': '2025-09-05T23:26:22.309994', 'total_vehicles': 3}

2. Testing Get Vehicles...
   Available Vehicles: 3

- VEH001: Saman Perera (assigned)
- VEH002: Nimal Silva (available)
- VEH003: Kamala Wijesinghe (busy)

3. Testing Route Optimization...
   Route Optimization Status: 201
   ✅ Route Created: RT20250905232626303
   Total Distance: 8.32 km
   Estimated Time: 21 minutes
   Optimized Stops: 3

📦 Step 3: Tracking package in WMS...

============================================================
TESTING WMS (TCP/IP) - Port 5003
============================================================

1. Connecting to WMS TCP Server...
   ✅ Connected to WMS successfully

2. Testing Heartbeat...
   ✅ Heartbeat Response: alive

3. Testing Package Status Request...
   ✅ Package PKG001 Status: READY_FOR_LOADING
   Zone: A, Weight: 2.5kg

4. Testing Warehouse Status...
   ✅ Warehouse Status:
   Total Packages: 4
   Zone Occupancy:
   Zone A: 16/100 packages
   Zone B: 23/150 packages
   Zone C: 45/200 packages

5. Testing New Package Receipt...
   ✅ New Package Added: PKG_TEST_232626
   Assigned Zone: A
   ✅ WMS TCP connection closed

✅ Integration flow test completed!
Order ID: ORD202509050002
Route ID: RT20250905232626303

============================================================
TESTING COMPLETED
============================================================
✅ All systems are operational and ready for middleware integration!

Next Steps:

1. Build ESB/middleware layer to connect these systems
2. Add message queues (RabbitMQ) for asynchronous processing
3. Implement API Gateway for unified access
4. Add real-time WebSocket connections
5. Build client applications (web portal & mobile app)
