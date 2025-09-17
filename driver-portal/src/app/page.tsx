/* eslint-disable @typescript-eslint/no-explicit-any */
"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

function getCookie(name: string): string {
  if (typeof document === "undefined") return "";
  const match = document.cookie.match(new RegExp("(^| )" + name + "=([^;]+)"));
  return match ? decodeURIComponent(match[2]) : "";
}

const mockOrders = [
  {
    orderId: "ORD001",
    recipient: "John Doe",
    address: "456 Kandy Road, Kandy",
    phone: "0771234567",
    packageDetails: "Package for ORD001",
    route: "Start: Warehouse → Colombo → Kandy (ETA: 2h 30m)",
    status: "assigned"
  },
  {
    orderId: "ORD002",
    recipient: "Jane Silva",
    address: "789 Galle Road, Galle",
    phone: "0779876543",
    packageDetails: "Package for ORD002",
    route: "Start: Warehouse → Colombo → Galle (ETA: 3h 10m)",
    status: "in_transit"
  },
];

export default function DriverDashboard() {
  const [driverName, setDriverName] = useState("");
  const [showRoute, setShowRoute] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<any>(null);
  const router = useRouter();

  useEffect(() => {
    const isSignedIn = getCookie("driverSignedIn");
    if (!isSignedIn) {
      router.push("/login");
      return;
    }
    setDriverName(getCookie("driverName") || "Driver");
  }, [router]);

  const handleShowRoute = (order: any) => {
    setSelectedOrder(order);
    setShowRoute(true);
  };

  const handleLogout = () => {
    document.cookie = "driverSignedIn=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    document.cookie = "driverName=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    document.cookie = "driverId=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    router.push("/login");
  };

  const updateOrderStatus = (orderId: string, newStatus: string) => {
    // In a real app, this would call your backend API
    console.log(`Updating order ${orderId} to status: ${newStatus}`);
  };

  if (!driverName) {
    return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
  }

  return (
    <div className="min-h-screen bg-white text-black">
      <header className="bg-black py-4 px-6 flex items-center justify-between">
        <div className="flex items-center">
          <div className="h-8 w-8 bg-white rounded mr-3 flex items-center justify-center">
            <span className="text-black font-bold text-sm">SL</span>
          </div>
          <h1 className="text-2xl font-bold text-white">Driver Portal</h1>
        </div>
        <div className="flex items-center gap-4">
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
          <p className="text-gray-600">Manage your assigned deliveries</p>
        </div>

        {mockOrders.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-500 text-lg">No orders assigned to you.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {mockOrders.map((order) => (
              <div
                key={order.orderId}
                className="border rounded-lg p-6 shadow-sm bg-gray-50 hover:shadow-md transition-shadow"
              >
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h3 className="font-semibold text-lg mb-2">Order {order.orderId}</h3>
                    <span className={`px-3 py-1 rounded-full text-sm font-medium ${order.status === 'assigned' ? 'bg-yellow-100 text-yellow-800' :
                      order.status === 'in_transit' ? 'bg-blue-100 text-blue-800' :
                        order.status === 'delivered' ? 'bg-green-100 text-green-800' :
                          'bg-gray-100 text-gray-800'
                      }`}>
                      {order.status.replace('_', ' ').toUpperCase()}
                    </span>
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
                  </div>
                  <div>
                    <p><strong>Address:</strong> {order.address}</p>
                    <p><strong>Package:</strong> {order.packageDetails}</p>
                  </div>
                </div>

                <div className="flex gap-3">
                  {order.status === "assigned" && (
                    <button
                      className="bg-black text-white px-4 py-2 rounded font-medium 
                 hover:bg-gray-900 transition-colors shadow-sm"
                      onClick={() => updateOrderStatus(order.orderId, "in_transit")}
                    >
                      Start Delivery
                    </button>
                  )}
                  {order.status === "in_transit" && (
                    <button
                      className="bg-black text-white px-4 py-2 rounded font-medium 
                 hover:bg-gray-900 transition-colors shadow-sm"
                      onClick={() => updateOrderStatus(order.orderId, "delivered")}
                    >
                      Mark as Delivered
                    </button>
                  )}
                  <button
                    className="border border-black text-black px-4 py-2 rounded font-medium 
               hover:bg-black hover:text-white transition-colors shadow-sm"
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

              <div className="mb-4">
                <h4 className="font-medium mb-2">Delivery Address:</h4>
                <p className="text-gray-700">{selectedOrder.address}</p>
              </div>

              <div className="mb-6">
                <h4 className="font-medium mb-2">Route Information:</h4>
                <p className="text-gray-700">{selectedOrder.route}</p>
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