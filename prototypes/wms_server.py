#!/usr/bin/env python3
"""
Warehouse Management System (WMS) Prototype
TCP/IP Proprietary Protocol Server - Real-time Package Tracking
"""

import socket
import threading
import json
import struct
import time
from datetime import datetime
from enum import Enum

# Protocol Constants
HEADER_SIZE = 8  # 4 bytes for message type + 4 bytes for payload length
MESSAGE_TYPES = {
    "PACKAGE_RECEIVED": 0x01,
    "PACKAGE_PROCESSED": 0x02,
    "PACKAGE_LOADED": 0x03,
    "PACKAGE_STATUS_REQ": 0x04,
    "PACKAGE_STATUS_RESP": 0x05,
    "WAREHOUSE_STATUS_REQ": 0x06,
    "WAREHOUSE_STATUS_RESP": 0x07,
    "WMS_CANCEL_PACKAGE_REQ": 0x10,
    "WMS_CANCEL_PACKAGE_RESP": 0x11,
    "HEARTBEAT": 0x08,
    "ERROR": 0xFF,
}

REVERSE_MESSAGE_TYPES = {v: k for k, v in MESSAGE_TYPES.items()}


class PackageStatus(Enum):
    RECEIVED = "RECEIVED"
    PROCESSING = "PROCESSING"
    READY_FOR_LOADING = "READY_FOR_LOADING"
    LOADED = "LOADED"
    DISPATCHED = "DISPATCHED"


class WMSServer:
    def __init__(self, host="localhost", port=5003):
        self.host = host
        self.port = port
        self.socket = None
        self.clients = {}
        self.packages = {}
        self.warehouse_zones = {
            "A": {"capacity": 100, "current": 15, "packages": []},
            "B": {"capacity": 150, "current": 23, "packages": []},
            "C": {"capacity": 200, "current": 45, "packages": []},
        }
        self.running = False

        # Initialize some sample packages
        self._initialize_sample_data()

    def _initialize_sample_data(self):
        """Initialize with sample package data"""
        sample_packages = [
            {
                "package_id": "PKG001",
                "order_id": "ORD20250101001",
                "external_order_id": "TEST-001",  # Add external order mapping
                "client_id": "CLIENT001",
                "status": PackageStatus.READY_FOR_LOADING.value,
                "zone": "A",
                "weight": 2.5,
                "dimensions": "30x20x15",
                "received_at": "2025-01-15T10:30:00Z",
                "special_handling": False,
            },
            {
                "package_id": "PKG002",
                "order_id": "ORD20250101002",
                "external_order_id": "TEST-002",  # Add external order mapping
                "client_id": "CLIENT002",
                "status": PackageStatus.PROCESSING.value,
                "zone": "B",
                "weight": 1.8,
                "dimensions": "25x15x10",
                "received_at": "2025-01-15T11:45:00Z",
                "special_handling": True,
            },
            {
                "package_id": "PKG003",
                "order_id": "ORD20250101003",
                "external_order_id": "TEST-003",  # Add external order mapping
                "client_id": "CLIENT001",
                "status": PackageStatus.LOADED.value,
                "zone": "C",
                "weight": 3.2,
                "dimensions": "40x30x20",
                "received_at": "2025-01-15T09:15:00Z",
                "special_handling": False,
            },
        ]

        for pkg in sample_packages:
            self.packages[pkg["package_id"]] = pkg
            zone = pkg["zone"]
            if zone in self.warehouse_zones:
                self.warehouse_zones[zone]["packages"].append(pkg["package_id"])

    def start_server(self):
        """Start the TCP server"""
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.socket.bind((self.host, self.port))
            self.socket.listen(5)
            self.running = True

            print(f"[WMS] TCP Server listening on {self.host}:{self.port}")
            print("[WMS] Ready to accept client connections...")

            while self.running:
                try:
                    client_socket, address = self.socket.accept()
                    print(f"[WMS] Client connected from {address}")

                    # Handle client in separate thread
                    client_thread = threading.Thread(
                        target=self.handle_client, args=(client_socket, address)
                    )
                    client_thread.daemon = True
                    client_thread.start()

                except socket.error as e:
                    if self.running:
                        print(f"[WMS] Socket error: {e}")

        except Exception as e:
            print(f"[WMS] Server startup error: {e}")
        finally:
            if self.socket:
                self.socket.close()

    def handle_client(self, client_socket, address):
        """Handle individual client connection"""
        client_id = f"{address[0]}:{address[1]}"
        self.clients[client_id] = {
            "socket": client_socket,
            "address": address,
            "connected_at": datetime.now().isoformat(),
            "last_heartbeat": time.time(),
        }

        try:
            while self.running:
                # Receive message header
                header_data = client_socket.recv(HEADER_SIZE)
                if not header_data or len(header_data) != HEADER_SIZE:
                    break

                # Parse header
                message_type, payload_length = struct.unpack("!II", header_data)

                # Receive payload
                payload_data = b""
                remaining = payload_length

                while remaining > 0:
                    chunk = client_socket.recv(min(remaining, 4096))
                    if not chunk:
                        break
                    payload_data += chunk
                    remaining -= len(chunk)

                if len(payload_data) != payload_length:
                    self.send_error(client_socket, "Incomplete payload received")
                    continue

                # Process message
                self.process_message(client_socket, message_type, payload_data)

        except socket.error as e:
            print(f"[WMS] Client {client_id} connection error: {e}")
        finally:
            client_socket.close()
            if client_id in self.clients:
                del self.clients[client_id]
            print(f"[WMS] Client {client_id} disconnected")

    def process_message(self, client_socket, message_type, payload_data):
        """Process incoming messages based on type"""
        try:
            message_name = REVERSE_MESSAGE_TYPES.get(message_type, "UNKNOWN")
            print(f"[WMS] Processing message: {message_name}")

            if message_type == MESSAGE_TYPES["PACKAGE_STATUS_REQ"]:
                self.handle_package_status_request(client_socket, payload_data)

            elif message_type == MESSAGE_TYPES["PACKAGE_RECEIVED"]:
                self.handle_package_received(client_socket, payload_data)

            elif message_type == MESSAGE_TYPES["PACKAGE_PROCESSED"]:
                self.handle_package_processed(client_socket, payload_data)

            elif message_type == MESSAGE_TYPES["PACKAGE_LOADED"]:
                self.handle_package_loaded(client_socket, payload_data)

            elif message_type == MESSAGE_TYPES["WAREHOUSE_STATUS_REQ"]:
                self.handle_warehouse_status_request(client_socket, payload_data)

            elif message_type == MESSAGE_TYPES["HEARTBEAT"]:
                self.handle_heartbeat(client_socket, payload_data)

            elif message_type == MESSAGE_TYPES["WMS_CANCEL_PACKAGE_REQ"]:
                self.handle_cancel_package_request(client_socket, payload_data)

            elif message_type == MESSAGE_TYPES["WMS_CANCEL_PACKAGE_RESP"]:
                self.handle_cancel_package_response(client_socket, payload_data)

            else:
                self.send_error(client_socket, f"Unknown message type: {message_type}")

        except Exception as e:
            self.send_error(client_socket, f"Message processing error: {str(e)}")

    def handle_package_status_request(self, client_socket, payload_data):
        """Handle package status request"""
        try:
            data = json.loads(payload_data.decode("utf-8"))
            package_id = data.get("package_id")

            if not package_id:
                self.send_error(client_socket, "Missing package_id")
                return

            if package_id not in self.packages:
                self.send_error(client_socket, f"Package {package_id} not found")
                return

            package_info = self.packages[package_id]

            response_data = {
                "package_id": package_id,
                "status": package_info["status"],
                "order_id": package_info["order_id"],
                "client_id": package_info["client_id"],
                "zone": package_info["zone"],
                "weight": package_info["weight"],
                "dimensions": package_info["dimensions"],
                "received_at": package_info["received_at"],
                "special_handling": package_info["special_handling"],
                "last_updated": package_info.get(
                    "last_updated", package_info["received_at"]
                ),
            }

            self.send_response(
                client_socket, MESSAGE_TYPES["PACKAGE_STATUS_RESP"], response_data
            )

        except Exception as e:
            self.send_error(client_socket, f"Package status request error: {str(e)}")

    def handle_package_received(self, client_socket, payload_data):
        """Handle new package received notification"""
        try:
            data = json.loads(payload_data.decode("utf-8"))

            required_fields = [
                "package_id",
                "order_id",
                "client_id",
                "weight",
                "dimensions",
            ]
            for field in required_fields:
                if field not in data:
                    self.send_error(client_socket, f"Missing required field: {field}")
                    return

            package_id = data["package_id"]

            # Assign to least occupied zone
            assigned_zone = min(
                self.warehouse_zones.keys(),
                key=lambda z: self.warehouse_zones[z]["current"],
            )

            # Create package record
            package_record = {
                "package_id": package_id,
                "order_id": data["order_id"],
                "client_id": data["client_id"],
                "status": PackageStatus.RECEIVED.value,
                "zone": assigned_zone,
                "weight": data["weight"],
                "dimensions": data["dimensions"],
                "received_at": datetime.now().isoformat(),
                "special_handling": data.get("special_handling", False),
                "last_updated": datetime.now().isoformat(),
            }

            # Store package and update zone
            self.packages[package_id] = package_record
            self.warehouse_zones[assigned_zone]["current"] += 1
            self.warehouse_zones[assigned_zone]["packages"].append(package_id)

            # Send confirmation
            response_data = {
                "package_id": package_id,
                "status": "RECEIVED",
                "assigned_zone": assigned_zone,
                "message": "Package received and stored successfully",
            }

            self.send_response(
                client_socket, MESSAGE_TYPES["PACKAGE_RECEIVED"], response_data
            )
            print(f"[WMS] Package received: {package_id} in zone {assigned_zone}")

            # Broadcast update to other clients
            self.broadcast_package_update(package_record)

        except Exception as e:
            self.send_error(client_socket, f"Package received error: {str(e)}")

    def handle_package_processed(self, client_socket, payload_data):
        """Handle package processed notification"""
        try:
            data = json.loads(payload_data.decode("utf-8"))
            package_id = data.get("package_id")

            if package_id not in self.packages:
                self.send_error(client_socket, f"Package {package_id} not found")
                return

            # Update package status
            self.packages[package_id]["status"] = PackageStatus.READY_FOR_LOADING.value
            self.packages[package_id]["processed_at"] = datetime.now().isoformat()
            self.packages[package_id]["last_updated"] = datetime.now().isoformat()

            response_data = {
                "package_id": package_id,
                "status": "READY_FOR_LOADING",
                "message": "Package processed and ready for loading",
            }

            self.send_response(
                client_socket, MESSAGE_TYPES["PACKAGE_PROCESSED"], response_data
            )
            print(f"[WMS] Package processed: {package_id}")

            # Broadcast update
            self.broadcast_package_update(self.packages[package_id])

        except Exception as e:
            self.send_error(client_socket, f"Package processed error: {str(e)}")

    def handle_package_loaded(self, client_socket, payload_data):
        """Handle package loaded notification"""
        try:
            data = json.loads(payload_data.decode("utf-8"))
            package_id = data.get("package_id")
            vehicle_id = data.get("vehicle_id")

            if package_id not in self.packages:
                self.send_error(client_socket, f"Package {package_id} not found")
                return

            # Update package status
            package = self.packages[package_id]
            package["status"] = PackageStatus.LOADED.value
            package["loaded_at"] = datetime.now().isoformat()
            package["last_updated"] = datetime.now().isoformat()
            package["loaded_vehicle"] = vehicle_id

            # Update zone occupancy
            zone = package["zone"]
            if zone in self.warehouse_zones:
                self.warehouse_zones[zone]["current"] -= 1
                if package_id in self.warehouse_zones[zone]["packages"]:
                    self.warehouse_zones[zone]["packages"].remove(package_id)

            response_data = {
                "package_id": package_id,
                "status": "LOADED",
                "vehicle_id": vehicle_id,
                "message": "Package loaded onto vehicle successfully",
            }

            self.send_response(
                client_socket, MESSAGE_TYPES["PACKAGE_LOADED"], response_data
            )
            print(f"[WMS] Package loaded: {package_id} onto vehicle {vehicle_id}")

            # Broadcast update
            self.broadcast_package_update(package)

        except Exception as e:
            self.send_error(client_socket, f"Package loaded error: {str(e)}")

    def handle_warehouse_status_request(self, client_socket, payload_data):
        """Handle warehouse status request"""
        try:
            # Get current warehouse status
            status_data = {
                "timestamp": datetime.now().isoformat(),
                "total_packages": len(self.packages),
                "zones": self.warehouse_zones,
                "packages_by_status": {},
            }

            # Count packages by status
            for status in PackageStatus:
                count = len(
                    [p for p in self.packages.values() if p["status"] == status.value]
                )
                status_data["packages_by_status"][status.value] = count

            self.send_response(
                client_socket, MESSAGE_TYPES["WAREHOUSE_STATUS_RESP"], status_data
            )

        except Exception as e:
            self.send_error(client_socket, f"Warehouse status error: {str(e)}")

    def handle_heartbeat(self, client_socket, payload_data):
        """Handle heartbeat message"""
        try:
            # Update client's last heartbeat
            for client_id, client_info in self.clients.items():
                if client_info["socket"] == client_socket:
                    client_info["last_heartbeat"] = time.time()
                    break

            # Send heartbeat response
            response_data = {"status": "alive", "timestamp": datetime.now().isoformat()}

            self.send_response(client_socket, MESSAGE_TYPES["HEARTBEAT"], response_data)

        except Exception as e:
            self.send_error(client_socket, f"Heartbeat error: {str(e)}")
    

    def handle_cancel_package_request(self, client_socket, payload_data):
        """Handle cancel package request"""
        try:
            data = json.loads(payload_data.decode("utf-8"))
            # Accept both package_id and order_id from ESB
            package_id = data.get("package_id")
            order_id = data.get("order_id")
            
            # Use order_id if package_id is not provided (for ESB compatibility)
            search_id = package_id if package_id else order_id

            if not search_id:
                self.send_error(client_socket, "Missing package_id or order_id for cancel request")
                return

            # Find package by package_id, order_id, or external_order_id
            found_package_id = None
            for pkg_id, package in self.packages.items():
                if (pkg_id == search_id or 
                    package.get("order_id") == search_id or 
                    package.get("external_order_id") == search_id):
                    found_package_id = pkg_id
                    break

            if not found_package_id:
                self.send_error(client_socket, f"Package not found for ID: {search_id}")
                return

            # Mark package as cancelled
            package = self.packages[found_package_id]
            previous_status = package["status"]
            package["status"] = "CANCELLED"
            package["cancelled_at"] = datetime.now().isoformat()
            package["last_updated"] = datetime.now().isoformat()

            # Remove from zone if present
            zone = package.get("zone")
            if zone and zone in self.warehouse_zones:
                if found_package_id in self.warehouse_zones[zone]["packages"]:
                    self.warehouse_zones[zone]["packages"].remove(found_package_id)
                    self.warehouse_zones[zone]["current"] = max(0, self.warehouse_zones[zone]["current"] - 1)

            response_data = {
                "package_id": found_package_id,
                "order_id": package.get("external_order_id", package.get("order_id")),  # Return external order ID for ESB
                "status": "CANCELLED",
                "previous_status": previous_status,
                "message": "Package cancelled successfully"
            }
            self.send_response(client_socket, MESSAGE_TYPES["WMS_CANCEL_PACKAGE_RESP"], response_data)
            print(f"[WMS] Package cancelled: {found_package_id} (search ID: {search_id})")

            # Broadcast update
            self.broadcast_package_update(package)

        except Exception as e:
            self.send_error(client_socket, f"Cancel package request error: {str(e)}")

    def handle_cancel_package_response(self, client_socket, payload_data):
        """Handle cancel package response (no-op or log)"""
        try:
            data = json.loads(payload_data.decode("utf-8"))
            package_id = data.get("package_id")
            print(f"[WMS] Cancel package response received for package: {package_id}")
        except Exception as e:
            print(f"[WMS] Error handling cancel package response: {e}")

    def send_response(self, client_socket, message_type, data):
        """Send response to client"""
        try:
            payload = json.dumps(data).encode("utf-8")
            header = struct.pack("!II", message_type, len(payload))
            client_socket.send(header + payload)

        except Exception as e:
            print(f"[WMS] Error sending response: {e}")

    def send_error(self, client_socket, error_message):
        """Send error response to client"""
        try:
            error_data = {
                "error": error_message,
                "timestamp": datetime.now().isoformat(),
            }
            payload = json.dumps(error_data).encode("utf-8")
            header = struct.pack("!II", MESSAGE_TYPES["ERROR"], len(payload))
            client_socket.send(header + payload)

        except Exception as e:
            print(f"[WMS] Error sending error response: {e}")

    def broadcast_package_update(self, package_data):
        """Broadcast package update to all connected clients"""
        update_data = {
            "type": "PACKAGE_UPDATE",
            "package": package_data,
            "timestamp": datetime.now().isoformat(),
        }

        disconnected_clients = []

        for client_id, client_info in self.clients.items():
            try:
                payload = json.dumps(update_data).encode("utf-8")
                header = struct.pack(
                    "!II", MESSAGE_TYPES["PACKAGE_PROCESSED"], len(payload)
                )
                client_info["socket"].send(header + payload)

            except Exception as e:
                print(f"[WMS] Error broadcasting to client {client_id}: {e}")
                disconnected_clients.append(client_id)

        # Clean up disconnected clients
        for client_id in disconnected_clients:
            if client_id in self.clients:
                del self.clients[client_id]

    def stop_server(self):
        """Stop the server"""
        print("[WMS] Stopping server...")
        self.running = False
        if self.socket:
            self.socket.close()


def main():
    """Main function"""
    print("Starting WMS (Warehouse Management System) - TCP/IP Server")

    server = WMSServer()

    try:
        server.start_server()
    except KeyboardInterrupt:
        print("\n[WMS] Received interrupt signal")
    finally:
        server.stop_server()
        print("[WMS] Server stopped")


if __name__ == "__main__":
    main()
