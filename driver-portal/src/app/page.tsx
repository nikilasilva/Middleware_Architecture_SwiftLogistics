"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { fetchDriverOrders, updateOrderStatusByDriver, DriverOrder } from "@/lib/driverApi";

function getCookie(name: string): string {
  if (typeof document === "undefined") return "";
  const match = document.cookie.match(new RegExp("(^| )" + name + "=([^;]+)"));
  return match ? decodeURIComponent(match[2]) : "";
}

export default function DriverDashboard() {
  const [driverName, setDriverName] = useState("");
  const [driverId, setDriverId] = useState("");
  const [orders, setOrders] = useState<DriverOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showRoute, setShowRoute] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<DriverOrder | null>(null);
  const [updating, setUpdating] = useState<string | null>(null); // Track which order is being updated
  const router = useRouter();

  useEffect(() => {
    const isSignedIn = getCookie("driverSignedIn");
    if (!isSignedIn) {
      router.push("/login");
      return;
    }

    const cookieDriverName = getCookie("driverName") || "Driver";
    const cookieDriverId = getCookie("driverId") || "DRIVER001";

    setDriverName(cookieDriverName);
    setDriverId(cookieDriverId);

    // Fetch real orders when component loads
    loadDriverOrders(cookieDriverId);
  }, [router]);

  const loadDriverOrders = async (driverIdParam: string) => {
    try {
      setLoading(true);
      setError(null);

      console.log("Loading orders for driver:", driverIdParam);
      const response = await fetchDriverOrders(driverIdParam);

      if (response.success) {
        setOrders(response.orders);
        console.log(`Loaded ${response.orders.length} orders for driver ${driverIdParam}`);
      } else {
        setError(response.error || "Failed to load orders");
      }
    } catch (err) {
      console.error("Error loading driver orders:", err);
      setError(`Failed to load orders: ${(err as Error).message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleShowRoute = (order: DriverOrder) => {
    setSelectedOrder(order);
    setShowRoute(true);
  };

  const handleLogout = () => {
    document.cookie = "driverSignedIn=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    document.cookie = "driverName=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    document.cookie = "driverId=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    router.push("/login");
  };

  const updateOrderStatus = async (orderId: string, newStatus: string) => {
    try {
      setUpdating(orderId);
      console.log(`Driver ${driverId} updating order ${orderId} to status: ${newStatus}`);

      const response = await updateOrderStatusByDriver(orderId, newStatus, driverId);

      if (response.success) {
        // Update local state
        setOrders(prev =>
          prev.map(order =>
            order.orderId === orderId
              ? { ...order, status: newStatus as "assigned" | "in_transit" | "delivered" }
              : order
          )
        );

        console.log(`Order ${orderId} status updated to ${newStatus}`);
      } else {
        throw new Error(response.error || "Failed to update status");
      }
    } catch (err) {
      console.error("Error updating order status:", err);
      alert(`Failed to update order status: ${(err as Error).message}`);
    } finally {
      setUpdating(null);
    }
  };

  const refreshOrders = () => {
    loadDriverOrders(driverId);
  };

  const formatDate = (timestamp?: number | string) => {
    if (!timestamp) return 'N/A';
    const date = typeof timestamp === 'number' ? new Date(timestamp) : new Date(timestamp);
    return date.toLocaleString();
  };

  if (!driverName) {
    return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
  }

  return (
    <div className="min-h-screen bg-white text-black">
      <header className="bg-black py-4 px-6 flex items-center justify-between">
        <div className="flex items-center">
          <div className="h-8 w-8 bg-white rounded mr-3 flex items-center justify-center">
            <span className="text-black font-bold text-sm">🚚</span>
          </div>
          <h1 className="text-2xl font-bold text-white">Driver Portal</h1>
        </div>
        <div className="flex items-center gap-4">
          <button
            onClick={refreshOrders}
            disabled={loading}
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition-colors disabled:opacity-50"
          >
            {loading ? "Loading..." : "Refresh"}
          </button>
          <span className="text-white font-semibold">Welcome, {driverName}</span>
          <button
            onClick={handleLogout}
            className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700 transition-colors"
          >
            Logout
          </button>
        </div>
      </header>

      <main className="max-w-4xl mx-auto py-8 px-4">
        <div className="mb-6">
          <h2 className="text-2xl font-bold mb-2">Your Delivery Orders</h2>
          <p className="text-gray-600">
            Manage your assigned deliveries • Driver ID: {driverId}
          </p>
        </div>

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
            <div className="flex justify-between items-center">
              <div>
                <strong>Error:</strong> {error}
              </div>
              <button
                onClick={refreshOrders}
                className="underline hover:no-underline"
              >
                Try Again
              </button>
            </div>
          </div>
        )}

        {loading ? (
          <div className="text-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-black mx-auto mb-4"></div>
            <p className="text-gray-500 text-lg">Loading your orders...</p>
          </div>
        ) : orders.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">📦</div>
            <p className="text-gray-500 text-lg mb-4">No orders assigned to you.</p>
            <button
              onClick={refreshOrders}
              className="bg-blue-600 text-white px-6 py-3 rounded hover:bg-blue-700 transition-colors"
            >
              Check for New Orders
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => (
              <div
                key={order.orderId}
                className="border rounded-lg p-6 shadow-sm bg-gray-50 hover:shadow-md transition-shadow"
              >
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h3 className="font-semibold text-lg mb-2">Order {order.orderId}</h3>
                    <div className="flex gap-2 items-center">
                      <span className={`px-3 py-1 rounded-full text-sm font-medium ${order.status === 'assigned' ? 'bg-yellow-100 text-yellow-800' :
                        order.status === 'in_transit' ? 'bg-blue-100 text-blue-800' :
                          order.status === 'delivered' ? 'bg-green-100 text-green-800' :
                            'bg-gray-100 text-gray-800'
                        }`}>
                        {order.status.replace('_', ' ').toUpperCase()}
                      </span>
                      {order.assignedAt && (
                        <span className="text-xs text-gray-500">
                          Assigned: {formatDate(order.assignedAt)}
                        </span>
                      )}
                    </div>
                  </div>
                  <button
                    className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition-colors"
                    onClick={() => handleShowRoute(order)}
                  >
                    View Route
                  </button>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                  <div>
                    <p><strong>Recipient:</strong> {order.recipient}</p>
                    <p><strong>Phone:</strong> {order.phone}</p>
                    {order.clientId && (
                      <p><strong>Client ID:</strong> {order.clientId}</p>
                    )}
                  </div>
                  <div>
                    <p><strong>Address:</strong> {order.address}</p>
                    <p><strong>Package:</strong> {order.packageDetails}</p>
                    {order.pickupAddress && (
                      <p><strong>Pickup:</strong> {order.pickupAddress}</p>
                    )}
                  </div>
                </div>

                {/* Items List */}
                {order.items && order.items.length > 0 && (
                  <div className="mb-4 p-3 bg-white rounded border">
                    <h4 className="font-medium mb-2">Items to Deliver:</h4>
                    <ul className="text-sm text-gray-600 space-y-1">
                      {order.items.map((item, index) => (
                        <li key={item.itemId || index}>
                          • {item.description} (ID: {item.itemId}, Qty: {item.quantity}, Weight: {item.weightKg}kg)
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                <div className="flex gap-3 flex-wrap">
                  {order.status === "assigned" && (
                    <button
                      className="bg-black text-white px-4 py-2 rounded font-medium 
                         hover:bg-gray-900 transition-colors shadow-sm disabled:opacity-50"
                      onClick={() => updateOrderStatus(order.orderId, "in_transit")}
                      disabled={updating === order.orderId}
                    >
                      {updating === order.orderId ? "Starting..." : "Start Delivery"}
                    </button>
                  )}
                  {order.status === "in_transit" && (
                    <button
                      className="bg-black text-white px-4 py-2 rounded font-medium 
                         hover:bg-gray-900 transition-colors shadow-sm disabled:opacity-50"
                      onClick={() => updateOrderStatus(order.orderId, "delivered")}
                      disabled={updating === order.orderId}
                    >
                      {updating === order.orderId ? "Updating..." : "Mark as Delivered"}
                    </button>
                  )}
                  <button
                    className="border border-black text-black px-4 py-2 rounded font-medium 
                       hover:bg-black hover:text-white transition-colors shadow-sm"
                    onClick={() => {
                      if (order.phone && order.phone !== "Phone not available") {
                        window.open(`tel:${order.phone}`);
                      } else {
                        alert("Phone number not available");
                      }
                    }}
                  >
                    Contact Customer
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Route Modal */}
      {showRoute && selectedOrder && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full">
            <div className="p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">
                  Route for {selectedOrder.orderId}
                </h3>
                <button
                  onClick={() => setShowRoute(false)}
                  className="text-gray-400 hover:text-gray-600 transition-colors"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>

              <div className="space-y-4 mb-6">
                <div>
                  <h4 className="font-medium mb-2">Delivery Address:</h4>
                  <p className="text-gray-700">{selectedOrder.address}</p>
                </div>

                {selectedOrder.pickupAddress && (
                  <div>
                    <h4 className="font-medium mb-2">Pickup Address:</h4>
                    <p className="text-gray-700">{selectedOrder.pickupAddress}</p>
                  </div>
                )}

                <div>
                  <h4 className="font-medium mb-2">Route Information:</h4>
                  <p className="text-gray-700">
                    {selectedOrder.routeInfo || selectedOrder.route || "Route details will be provided by dispatch"}
                  </p>
                </div>

                <div>
                  <h4 className="font-medium mb-2">Contact Information:</h4>
                  <p className="text-gray-700">
                    <strong>{selectedOrder.recipient}</strong><br />
                    {selectedOrder.phone}
                  </p>
                </div>
              </div>

              <div className="flex gap-3">
                <button
                  onClick={() => setShowRoute(false)}
                  className="flex-1 border border-black text-black py-2 rounded font-medium 
                     hover:bg-black hover:text-white transition-colors shadow-sm"
                >
                  Close
                </button>
                <button
                  className="flex-1 bg-black text-white py-2 rounded font-medium 
                     hover:bg-gray-900 transition-colors shadow-sm"
                  onClick={() => {
                    const address = encodeURIComponent(selectedOrder.address);
                    window.open(`https://www.google.com/maps/search/${address}`, '_blank');
                  }}
                >
                  Open in Maps
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}