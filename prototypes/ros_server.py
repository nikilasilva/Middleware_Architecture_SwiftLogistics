#!/usr/bin/env python3
"""
Route Optimization System (ROS) Prototype
REST/JSON API Server - Cloud-based Service Simulation
"""

from flask import Flask, request, jsonify
from datetime import datetime, timedelta
import uuid
import random
import math

app = Flask(__name__)

# Mock data
vehicles = {
    "VEH001": {
        "vehicle_id": "VEH001",
        "driver_name": "Saman Perera",
        "vehicle_type": "van",
        "capacity": 50,
        "current_location": {"lat": 6.9271, "lng": 79.8612},  # Colombo
        "status": "available",
    },
    "VEH002": {
        "vehicle_id": "VEH002",
        "driver_name": "Nimal Silva",
        "vehicle_type": "truck",
        "capacity": 100,
        "current_location": {"lat": 6.9319, "lng": 79.8478},  # Colombo Fort
        "status": "available",
    },
    "VEH003": {
        "vehicle_id": "VEH003",
        "driver_name": "Kamala Wijesinghe",
        "vehicle_type": "bike",
        "capacity": 10,
        "current_location": {"lat": 6.9147, "lng": 79.8730},  # Bambalapitiya
        "status": "busy",
    },
}

routes = {}
delivery_points = {}


def calculate_distance(lat1, lng1, lat2, lng2):
    """Calculate distance between two points using Haversine formula"""
    R = 6371  # Earth's radius in kilometers

    lat1_rad = math.radians(lat1)
    lat2_rad = math.radians(lat2)
    delta_lat = math.radians(lat2 - lat1)
    delta_lng = math.radians(lng2 - lng1)

    a = math.sin(delta_lat / 2) * math.sin(delta_lat / 2) + math.cos(
        lat1_rad
    ) * math.cos(lat2_rad) * math.sin(delta_lng / 2) * math.sin(delta_lng / 2)

    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    return R * c


def optimize_route(delivery_addresses, vehicle_location):
    """Simple route optimization using nearest neighbor algorithm"""
    if not delivery_addresses:
        return []

    current_location = vehicle_location
    optimized_route = []
    remaining_addresses = delivery_addresses.copy()

    while remaining_addresses:
        # Find nearest address
        nearest_address = min(
            remaining_addresses,
            key=lambda addr: calculate_distance(
                current_location["lat"],
                current_location["lng"],
                addr["lat"],
                addr["lng"],
            ),
        )

        # Add to route
        distance = calculate_distance(
            current_location["lat"],
            current_location["lng"],
            nearest_address["lat"],
            nearest_address["lng"],
        )

        optimized_route.append(
            {
                "address": nearest_address["address"],
                "coordinates": {
                    "lat": nearest_address["lat"],
                    "lng": nearest_address["lng"],
                },
                "order_id": nearest_address["order_id"],
                "distance_km": round(distance, 2),
                "estimated_time_minutes": round(distance * 2.5, 0),  # Rough estimate
            }
        )

        # Update current location and remove from remaining
        current_location = nearest_address
        remaining_addresses.remove(nearest_address)

    return optimized_route


@app.route("/api/v1/routes/optimize", methods=["POST"])
def create_optimized_route():
    """Create optimized route for deliveries"""
    try:
        data = request.get_json()

        # Validate request
        required_fields = ["vehicle_id", "delivery_addresses"]
        for field in required_fields:
            if field not in data:
                return jsonify({"error": f"Missing required field: {field}"}), 400

        vehicle_id = data["vehicle_id"]
        delivery_addresses = data["delivery_addresses"]
        priority = data.get("priority", "normal")

        # Check if vehicle exists
        if vehicle_id not in vehicles:
            return jsonify({"error": "Vehicle not found"}), 404

        vehicle = vehicles[vehicle_id]

        # Check vehicle capacity
        if len(delivery_addresses) > vehicle["capacity"]:
            return (
                jsonify(
                    {
                        "error": "Too many deliveries for vehicle capacity",
                        "max_capacity": vehicle["capacity"],
                        "requested": len(delivery_addresses),
                    }
                ),
                400,
            )

        # Generate route ID
        route_id = (
            f"RT{datetime.now().strftime('%Y%m%d%H%M%S')}{random.randint(100, 999)}"
        )

        # Optimize route
        optimized_stops = optimize_route(
            delivery_addresses, vehicle["current_location"]
        )

        # Calculate total route metrics
        total_distance = sum(stop["distance_km"] for stop in optimized_stops)
        total_time = sum(stop["estimated_time_minutes"] for stop in optimized_stops)

        # Create route record
        route_record = {
            "route_id": route_id,
            "vehicle_id": vehicle_id,
            "driver_name": vehicle["driver_name"],
            "status": "planned",
            "priority": priority,
            "created_at": datetime.now().isoformat(),
            "total_stops": len(optimized_stops),
            "total_distance_km": round(total_distance, 2),
            "estimated_total_time_minutes": int(total_time),
            "estimated_completion": (
                datetime.now() + timedelta(minutes=total_time)
            ).isoformat(),
            "optimized_stops": optimized_stops,
        }

        routes[route_id] = route_record

        # Update vehicle status
        vehicles[vehicle_id]["status"] = "assigned"
        vehicles[vehicle_id]["current_route"] = route_id

        print(
            f"[ROS] Route optimized: {route_id} for vehicle {vehicle_id} with {len(optimized_stops)} stops"
        )

        return (
            jsonify(
                {
                    "success": True,
                    "route_id": route_id,
                    "optimization_completed": True,
                    "route_details": route_record,
                }
            ),
            201,
        )

    except Exception as e:
        return jsonify({"error": f"Route optimization failed: {str(e)}"}), 500


@app.route("/api/v1/routes/<route_id>", methods=["GET"])
def get_route(route_id):
    """Get route details"""
    if route_id not in routes:
        return jsonify({"error": "Route not found"}), 404

    route = routes[route_id]
    print(f"[ROS] Route details requested: {route_id}")

    return jsonify({"success": True, "route": route})


@app.route("/api/v1/routes/<route_id>/status", methods=["PUT"])
def update_route_status(route_id):
    """Update route status"""
    try:
        if route_id not in routes:
            return jsonify({"error": "Route not found"}), 404

        data = request.get_json()
        new_status = data.get("status")

        if new_status not in ["planned", "in_progress", "completed", "cancelled"]:
            return jsonify({"error": "Invalid status"}), 400

        routes[route_id]["status"] = new_status
        routes[route_id]["updated_at"] = datetime.now().isoformat()

        # Update vehicle status if route completed
        if new_status == "completed":
            vehicle_id = routes[route_id]["vehicle_id"]
            vehicles[vehicle_id]["status"] = "available"
            vehicles[vehicle_id].pop("current_route", None)

        print(f"[ROS] Route status updated: {route_id} to {new_status}")

        return jsonify(
            {
                "success": True,
                "route_id": route_id,
                "status": new_status,
                "updated_at": routes[route_id]["updated_at"],
            }
        )

    except Exception as e:
        return jsonify({"error": f"Status update failed: {str(e)}"}), 500


@app.route("/api/v1/vehicles", methods=["GET"])
def get_vehicles():
    """Get all vehicles"""
    return jsonify({"success": True, "vehicles": list(vehicles.values())})


@app.route("/api/v1/vehicles/<vehicle_id>", methods=["GET"])
def get_vehicle(vehicle_id):
    """Get specific vehicle details"""
    if vehicle_id not in vehicles:
        return jsonify({"error": "Vehicle not found"}), 404

    vehicle = vehicles[vehicle_id]

    # Add current route info if assigned
    if vehicle["status"] == "assigned" and "current_route" in vehicle:
        route_id = vehicle["current_route"]
        vehicle["current_route_details"] = routes.get(route_id, {})

    return jsonify({"success": True, "vehicle": vehicle})


@app.route("/api/v1/vehicles/<vehicle_id>/location", methods=["PUT"])
def update_vehicle_location(vehicle_id):
    """Update vehicle location"""
    try:
        if vehicle_id not in vehicles:
            return jsonify({"error": "Vehicle not found"}), 404

        data = request.get_json()

        if "lat" not in data or "lng" not in data:
            return jsonify({"error": "Missing coordinates"}), 400

        vehicles[vehicle_id]["current_location"] = {
            "lat": float(data["lat"]),
            "lng": float(data["lng"]),
        }
        vehicles[vehicle_id]["location_updated_at"] = datetime.now().isoformat()

        print(f"[ROS] Vehicle location updated: {vehicle_id}")

        return jsonify(
            {
                "success": True,
                "vehicle_id": vehicle_id,
                "location": vehicles[vehicle_id]["current_location"],
            }
        )

    except Exception as e:
        return jsonify({"error": f"Location update failed: {str(e)}"}), 500


@app.route("/api/v1/routes/reoptimize/<route_id>", methods=["POST"])
def reoptimize_route(route_id):
    """Reoptimize existing route with new deliveries"""
    try:
        if route_id not in routes:
            return jsonify({"error": "Route not found"}), 404

        data = request.get_json()
        additional_deliveries = data.get("additional_deliveries", [])

        route = routes[route_id]
        vehicle = vehicles[route["vehicle_id"]]

        # Combine existing undelivered stops with new deliveries
        current_stops = route["optimized_stops"]
        undelivered_stops = [
            stop
            for stop in current_stops
            if stop.get("status", "pending") != "delivered"
        ]

        # Convert to delivery address format for optimization
        all_addresses = []

        # Add undelivered stops
        for stop in undelivered_stops:
            all_addresses.append(
                {
                    "address": stop["address"],
                    "lat": stop["coordinates"]["lat"],
                    "lng": stop["coordinates"]["lng"],
                    "order_id": stop["order_id"],
                }
            )

        # Add new deliveries
        all_addresses.extend(additional_deliveries)

        # Reoptimize
        optimized_stops = optimize_route(all_addresses, vehicle["current_location"])

        # Update route
        total_distance = sum(stop["distance_km"] for stop in optimized_stops)
        total_time = sum(stop["estimated_time_minutes"] for stop in optimized_stops)

        routes[route_id]["optimized_stops"] = optimized_stops
        routes[route_id]["total_stops"] = len(optimized_stops)
        routes[route_id]["total_distance_km"] = round(total_distance, 2)
        routes[route_id]["estimated_total_time_minutes"] = int(total_time)
        routes[route_id]["reoptimized_at"] = datetime.now().isoformat()

        print(
            f"[ROS] Route reoptimized: {route_id} with {len(additional_deliveries)} new deliveries"
        )

        return jsonify(
            {
                "success": True,
                "route_id": route_id,
                "reoptimization_completed": True,
                "added_deliveries": len(additional_deliveries),
                "total_stops": len(optimized_stops),
                "route_details": routes[route_id],
            }
        )

    except Exception as e:
        return jsonify({"error": f"Reoptimization failed: {str(e)}"}), 500


@app.route("/api/v1/health", methods=["GET"])
def health_check():
    """Health check endpoint"""
    return jsonify(
        {
            "service": "ROS",
            "status": "healthy",
            "protocol": "REST/JSON",
            "timestamp": datetime.now().isoformat(),
            "active_routes": len(
                [
                    r
                    for r in routes.values()
                    if r["status"] in ["planned", "in_progress"]
                ]
            ),
            "available_vehicles": len(
                [v for v in vehicles.values() if v["status"] == "available"]
            ),
            "total_vehicles": len(vehicles),
        }
    )


@app.route("/api/v1/routes", methods=["GET"])
def get_all_routes():
    """Get all routes with optional filtering"""
    status_filter = request.args.get("status")
    vehicle_filter = request.args.get("vehicle_id")

    filtered_routes = routes.values()

    if status_filter:
        filtered_routes = [r for r in filtered_routes if r["status"] == status_filter]

    if vehicle_filter:
        filtered_routes = [
            r for r in filtered_routes if r["vehicle_id"] == vehicle_filter
        ]

    return jsonify(
        {
            "success": True,
            "routes": list(filtered_routes),
            "total_count": len(filtered_routes),
        }
    )


if __name__ == "__main__":
    print("Starting ROS (Route Optimization System) - REST/JSON Server")
    print("API Base URL: http://localhost:5002/api/v1/")
    print("Health Check: http://localhost:5002/api/v1/health")
    print("Available endpoints:")
    print("  POST /api/v1/routes/optimize - Create optimized route")
    print("  GET  /api/v1/routes/<id> - Get route details")
    print("  PUT  /api/v1/routes/<id>/status - Update route status")
    print("  GET  /api/v1/vehicles - Get all vehicles")
    app.run(host="0.0.0.0", port=5002, debug=True)
