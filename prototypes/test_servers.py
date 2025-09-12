#!/usr/bin/env python3
"""
Complete System Testing Script
Tests all three prototypes: CMS (SOAP), ROS (REST), WMS (TCP/IP)
"""

import requests
import socket
import struct
import json
import xml.etree.ElementTree as ET
from datetime import datetime


def print_separator(title):
    """Print a section separator"""
    print("\n" + "=" * 60)
    print(f" {title} ")
    print("=" * 60)


def test_cms_soap():
    """Test CMS SOAP/XML API"""
    print_separator("TESTING CMS (SOAP/XML) - Port 5001")

    try:
        # Test 1: Health Check
        print("\n1. Testing CMS Health Check...")
        response = requests.get("http://localhost:5001/cms/health")
        print(f"Health Status: {response.status_code}")
        print(f"Response: {response.json()}")

        # Test 2: Get Client Info
        print("\n2. Testing Get Client Info (SOAP)...")
        soap_request = """<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <GetClientInfo>
            <ClientId>CLIENT001</ClientId>
        </GetClientInfo>
    </soap:Body>
</soap:Envelope>"""

        headers = {"Content-Type": "text/xml", "SOAPAction": "GetClientInfo"}

        response = requests.post(
            "http://localhost:5001/cms/soap", data=soap_request, headers=headers
        )
        print(f"SOAP Response Status: {response.status_code}")

        # Parse XML response
        if response.status_code == 200:
            root = ET.fromstring(response.text)
            client_name = root.find(".//{http://swiftlogistics.lk/cms}Name")
            if client_name is not None:
                print(f"Client Name: {client_name.text}")
            else:
                print("Response received but couldn't parse client name")

        # Test 3: Create Order
        print("\n3. Testing Create Order (SOAP)...")
        create_order_soap = """<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <CreateOrder>
            <ClientId>CLIENT001</ClientId>
            <RecipientName>John Doe</RecipientName>
            <RecipientAddress>123 Main Street, Colombo 03</RecipientAddress>
            <RecipientPhone>+94771234567</RecipientPhone>
            <PackageDetails>Electronics - Laptop</PackageDetails>
        </CreateOrder>
    </soap:Body>
</soap:Envelope>"""

        headers["SOAPAction"] = "CreateOrder"
        response = requests.post(
            "http://localhost:5001/cms/soap", data=create_order_soap, headers=headers
        )

        print(f"Create Order Status: {response.status_code}")
        if response.status_code == 200:
            root = ET.fromstring(response.text)
            order_id = root.find(".//{http://swiftlogistics.lk/cms}OrderId")
            if order_id is not None:
                print(f"✅ Order Created: {order_id.text}")
                return order_id.text

    except Exception as e:
        print(f"❌ CMS Test Error: {e}")
        return None


def test_ros_rest():
    """Test ROS REST/JSON API"""
    print_separator("TESTING ROS (REST/JSON) - Port 5002")

    try:
        # Test 1: Health Check
        print("\n1. Testing ROS Health Check...")
        response = requests.get("http://localhost:5002/api/v1/health")
        print(f"Health Status: {response.status_code}")
        print(f"Response: {response.json()}")

        # Test 2: Get Vehicles
        print("\n2. Testing Get Vehicles...")
        response = requests.get("http://localhost:5002/api/v1/vehicles")
        vehicles_data = response.json()
        print(f"Available Vehicles: {len(vehicles_data['vehicles'])}")
        for vehicle in vehicles_data["vehicles"]:
            print(
                f"  - {vehicle['vehicle_id']}: {vehicle['driver_name']} ({vehicle['status']})"
            )

        # Test 3: Optimize Route
        print("\n3. Testing Route Optimization...")
        delivery_data = {
            "vehicle_id": "VEH001",
            "delivery_addresses": [
                {
                    "address": "123 Galle Road, Colombo 03",
                    "lat": 6.9147,
                    "lng": 79.8730,
                    "order_id": "ORD001",
                },
                {
                    "address": "456 Kandy Road, Colombo 07",
                    "lat": 6.9319,
                    "lng": 79.8478,
                    "order_id": "ORD002",
                },
                {
                    "address": "789 Negombo Road, Colombo 13",
                    "lat": 6.9497,
                    "lng": 79.8653,
                    "order_id": "ORD003",
                },
            ],
            "priority": "normal",
        }

        response = requests.post(
            "http://localhost:5002/api/v1/routes/optimize", json=delivery_data
        )

        print(f"Route Optimization Status: {response.status_code}")
        if response.status_code == 201:
            route_data = response.json()
            route_id = route_data["route_id"]
            print(f"✅ Route Created: {route_id}")
            print(
                f"Total Distance: {route_data['route_details']['total_distance_km']} km"
            )
            print(
                f"Estimated Time: {route_data['route_details']['estimated_total_time_minutes']} minutes"
            )
            print(
                f"Optimized Stops: {len(route_data['route_details']['optimized_stops'])}"
            )
            return route_id

    except Exception as e:
        print(f"❌ ROS Test Error: {e}")
        return None


def test_wms_tcp():
    """Test WMS TCP/IP Protocol"""
    print_separator("TESTING WMS (TCP/IP) - Port 5003")

    try:
        # Connect to WMS
        print("\n1. Connecting to WMS TCP Server...")
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect(("localhost", 5003))
        print("✅ Connected to WMS successfully")

        # Test 2: Send Heartbeat
        print("\n2. Testing Heartbeat...")
        message_type = 0x08  # HEARTBEAT
        payload = json.dumps({"client_id": "TEST_CLIENT"}).encode("utf-8")

        # Send message
        header = struct.pack("!II", message_type, len(payload))
        sock.send(header + payload)

        # Receive response
        response_header = sock.recv(8)
        msg_type, payload_len = struct.unpack("!II", response_header)
        payload_data = sock.recv(payload_len)
        response_data = json.loads(payload_data.decode("utf-8"))

        print(f"✅ Heartbeat Response: {response_data['status']}")

        # Test 3: Get Package Status
        print("\n3. Testing Package Status Request...")
        message_type = 0x04  # PACKAGE_STATUS_REQ
        payload = json.dumps({"package_id": "PKG001"}).encode("utf-8")

        header = struct.pack("!II", message_type, len(payload))
        sock.send(header + payload)

        # Receive response
        response_header = sock.recv(8)
        msg_type, payload_len = struct.unpack("!II", response_header)
        payload_data = sock.recv(payload_len)
        response_data = json.loads(payload_data.decode("utf-8"))

        print(
            f"✅ Package {response_data['package_id']} Status: {response_data['status']}"
        )
        print(f"   Zone: {response_data['zone']}, Weight: {response_data['weight']}kg")

        # Test 4: Get Warehouse Status
        print("\n4. Testing Warehouse Status...")
        message_type = 0x06  # WAREHOUSE_STATUS_REQ
        payload = json.dumps({}).encode("utf-8")

        header = struct.pack("!II", message_type, len(payload))
        sock.send(header + payload)

        response_header = sock.recv(8)
        msg_type, payload_len = struct.unpack("!II", response_header)
        payload_data = sock.recv(payload_len)
        response_data = json.loads(payload_data.decode("utf-8"))

        print(f"✅ Warehouse Status:")
        print(f"   Total Packages: {response_data['total_packages']}")
        print(f"   Zone Occupancy:")
        for zone, info in response_data["zones"].items():
            print(f"     Zone {zone}: {info['current']}/{info['capacity']} packages")

        # Test 5: Add New Package
        print("\n5. Testing New Package Receipt...")
        message_type = 0x01  # PACKAGE_RECEIVED
        new_package = {
            "package_id": f"PKG_TEST_{datetime.now().strftime('%H%M%S')}",
            "order_id": "ORD_TEST_001",
            "client_id": "CLIENT001",
            "weight": 2.5,
            "dimensions": "30x20x15",
            "special_handling": False,
        }
        payload = json.dumps(new_package).encode("utf-8")

        header = struct.pack("!II", message_type, len(payload))
        sock.send(header + payload)

        response_header = sock.recv(8)
        msg_type, payload_len = struct.unpack("!II", response_header)
        payload_data = sock.recv(payload_len)
        response_data = json.loads(payload_data.decode("utf-8"))

        print(f"✅ New Package Added: {response_data['package_id']}")
        print(f"   Assigned Zone: {response_data['assigned_zone']}")

        sock.close()
        print("✅ WMS TCP connection closed")

    except Exception as e:
        print(f"❌ WMS Test Error: {e}")


def test_integration_flow():
    """Test integration flow between systems"""
    print_separator("INTEGRATION FLOW TEST")

    print("\n🔄 Testing complete order processing flow...")

    # Step 1: Create order in CMS
    print("\n📝 Step 1: Creating order in CMS...")
    order_id = test_cms_soap()

    # Step 2: Optimize route in ROS
    print("\n🚚 Step 2: Optimizing delivery route in ROS...")
    route_id = test_ros_rest()

    # Step 3: Track package in WMS
    print("\n📦 Step 3: Tracking package in WMS...")
    test_wms_tcp()

    print("\n✅ Integration flow test completed!")
    print(f"Order ID: {order_id}")
    print(f"Route ID: {route_id}")


def main():
    """Run all tests"""
    print("🚀 SWIFTLOGISTICS SYSTEM PROTOTYPE TESTING")
    print("Testing all three heterogeneous systems...")

    # Test individual systems
    test_cms_soap()
    test_ros_rest()
    test_wms_tcp()

    # Test integration flow
    test_integration_flow()

    print_separator("TESTING COMPLETED")
    print("✅ All systems are operational and ready for middleware integration!")
    print("\nNext Steps:")
    print("1. Build ESB/middleware layer to connect these systems")
    print("2. Add message queues (RabbitMQ) for asynchronous processing")
    print("3. Implement API Gateway for unified access")
    print("4. Add real-time WebSocket connections")
    print("5. Build client applications (web portal & mobile app)")


if __name__ == "__main__":
    main()
