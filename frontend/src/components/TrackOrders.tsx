"use client";

import StatusStepper from "./StatusStepper";
import EditOrder from "./EditOrder";
import { useState, useEffect } from "react";
import { OrderData } from "@/types";
import { cancelOrder, fetchOrdersByClient } from "@/lib/ordersApi";
import { ApiOrder } from "@/types/order";
import ConfirmationModal from "./ConfirmationModal";

interface Order extends OrderData {
  id: string;
  status: "pending" | "assigned" | "in_transit" | "delivered" | "cancelled";
  createdAt: string;
  estimatedDelivery?: string;
  trackingNumber: string;
  driverName?: string;
  vehicleId?: string;
  items?: Array<{
    itemId: string;
    description: string;
    quantity: number;
    weightKg: number;
  }>;
  notes?: string;
}

interface TrackOrdersProps {
  onBack: () => void;
}

// Helper function to get a cookie value
function getCookie(name: string): string {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop()?.split(';').shift() || '';
  return '';
}

export default function TrackOrders({ onBack }: TrackOrdersProps) {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [editingOrder, setEditingOrder] = useState<Order | null>(null);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [orderToCancel, setOrderToCancel] = useState<string>("");

  // Get clientId from cookie
  const clientId = getCookie('clientId');

  useEffect(() => {
    async function loadOrders() {
      try {
        setLoading(true);

        if (!clientId) {
          console.error("No client ID found in cookie");
          setOrders([]);
          return;
        }

        const data = await fetchOrdersByClient(clientId);

        // Transform the data to match your Order interface with proper typing
        const transformedOrders: Order[] = data.orders.map((order: ApiOrder) => ({
          id: order.orderId || order.internalOrderId || '',
          clientId: order.clientId || clientId,
          recipientName: order.recipient || order.recipientName || '',
          recipientAddress: order.address || order.recipientAddress || '',
          recipientPhone: order.phone || order.recipientPhone || '',
          packageDetails: order.packageDetails || '',
          status: mapStatus(order.status || order.overallStatus || 'PENDING'),
          createdAt: order.createdAt || new Date().toISOString(),
          estimatedDelivery: order.estimatedDelivery,
          trackingNumber: order.trackingNumber || order.orderId || '',
          driverName: order.driverName,
          vehicleId: order.vehicleId,
          items: order.items || [],
          notes: order.notes
        }));

        setOrders(transformedOrders);
      } catch (error) {
        console.error("Failed to fetch orders:", error);
        setOrders([]);
      } finally {
        setLoading(false);
      }
    }
    loadOrders();
  }, [clientId]);

  function mapStatus(backendStatus: string): "pending" | "assigned" | "in_transit" | "delivered" | "cancelled" {
    const statusMap: Record<string, "pending" | "assigned" | "in_transit" | "delivered" | "cancelled"> = {
      'PENDING': 'pending',
      'PROCESSING': 'assigned',
      'ASSIGNED': 'assigned',
      'IN_TRANSIT': 'in_transit',
      'DELIVERED': 'delivered',
      'CANCELLED': 'cancelled'
    };

    return statusMap[backendStatus.toUpperCase()] || 'pending';
  }

  // const fetchOrders = async () => {
  //   try {
  //     setLoading(true);
  //     // Replace with actual API call
  //     const mockOrders: Order[] = [
  //       {
  //         id: "1",
  //         clientId: "CLIENT001",
  //         recipientName: "John Doe",
  //         recipientAddress: "123 Main St, Colombo",
  //         recipientPhone: "+94-77-123-4567",
  //         packageDetails: "#1: Wireless Mouse (ID: ITEM001, Qty: 2, Weight: 0.2kg); #2: Keyboard (ID: ITEM002, Qty: 1, Weight: 0.8kg)",
  //         status: "in_transit",
  //         createdAt: "2024-01-15T10:30:00Z",
  //         estimatedDelivery: "2024-01-16T15:00:00Z",
  //         trackingNumber: "SL001234",
  //         driverName: "Saman Silva",
  //         vehicleId: "VH001",
  //         items: [
  //           { itemId: "ITEM001", description: "Wireless Mouse", quantity: 2, weightKg: 0.2 },
  //           { itemId: "ITEM002", description: "Keyboard", quantity: 1, weightKg: 0.8 }
  //         ],
  //         notes: "Leave at the front desk if not home"
  //       },
  //       {
  //         id: "2",
  //         clientId: "CLIENT002",
  //         recipientName: "Jane Smith",
  //         recipientAddress: "456 Galle Road, Kandy",
  //         recipientPhone: "+94-71-987-6543",
  //         packageDetails: "#1: T-shirt (ID: ITEM003, Qty: 3, Weight: 0.15kg)",
  //         status: "delivered",
  //         createdAt: "2024-01-14T08:15:00Z",
  //         estimatedDelivery: "2024-01-15T12:00:00Z",
  //         trackingNumber: "SL001235",
  //         driverName: "Kamal Perera",
  //         vehicleId: "VH002",
  //         items: [
  //           { itemId: "ITEM003", description: "T-shirt", quantity: 3, weightKg: 0.15 }
  //         ],
  //         notes: "Ring the bell twice"
  //       },
  //       {
  //         id: "3",
  //         clientId: "CLIENT001",
  //         recipientName: "Bob Johnson",
  //         recipientAddress: "789 Hill Street, Galle",
  //         recipientPhone: "+94-75-555-1234",
  //         packageDetails: "#1: Mobile Phone Case (ID: ITEM004, Qty: 1, Weight: 0.05kg)",
  //         status: "pending",
  //         createdAt: "2024-01-16T14:20:00Z",
  //         trackingNumber: "SL001236",
  //         items: [
  //           { itemId: "ITEM004", description: "Mobile Phone Case", quantity: 1, weightKg: 0.05 }
  //         ],
  //         notes: "Call before delivery"
  //       },
  //     ];
  //     setOrders(mockOrders);
  //   } catch (error) {
  //     console.error("Error fetching orders:", error);
  //   } finally {
  //     setLoading(false);
  //   }
  // };

  const handleEditOrder = (order: Order) => {
    if (order.status === "pending") {
      setEditingOrder(order);
    }
  };

  const handleSaveOrder = (updatedOrder: Order) => {
    // Update packageDetails based on items
    const packageDetails = updatedOrder.items
      ?.map((item, index) => `#${index + 1}: ${item.description} (ID: ${item.itemId}, Qty: ${item.quantity}, Weight: ${item.weightKg}kg)`)
      .join("; ");

    const orderWithUpdatedDetails = {
      ...updatedOrder,
      packageDetails: packageDetails || updatedOrder.packageDetails
    };

    console.log("Saving order:", orderWithUpdatedDetails);

    setOrders(prev =>
      prev.map(order =>
        order.id === orderWithUpdatedDetails.id ? orderWithUpdatedDetails : order
      )
    );
    setEditingOrder(null);
  };

  const handleCancelEdit = () => {
    setEditingOrder(null);
  };

  const handleCancelOrder = async (orderId: string) => {
    console.log("Cancelling order ID:", orderId);

    // Show confirmation dialog    
    setOrderToCancel(orderId);
    setShowCancelModal(true);
  };

  // Handle confirmation from modal
  const handleConfirmCancel = async () => {
    setShowCancelModal(false);

    if (!orderToCancel) return;

    try {
      console.log("Cancelling order ID:", orderToCancel);

      // Use the API function
      const result = await cancelOrder(orderToCancel);
      console.log("Cancel order response:", result);

      if (result.success) {
        // Update local state to reflect cancellation
        setOrders(prev =>
          prev.map(order =>
            order.id === orderToCancel ? { ...order, status: "cancelled" } : order
          )
        );

        // Show success message (you can also create a success modal for this)
        alert(result.message || "Order cancelled successfully!");
      } else {
        throw new Error(result.error || "Failed to cancel order");
      }

    } catch (error) {
      console.error("Error cancelling order:", error);
      alert(`Failed to cancel order: ${(error as Error).message}`);
    } finally {
      setOrderToCancel("");
    }
  };

  // ADD: Handle cancel from modal
  const handleCancelFromModal = () => {
    setShowCancelModal(false);
    setOrderToCancel("");
  };

  const filteredOrders = orders.filter((order) => {
    const matchesSearch =
      order.trackingNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      order.recipientName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      order.recipientAddress.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus =
      statusFilter === "all" || order.status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="min-h-screen bg-white text-black">
      {/* Header */}
      <header className="bg-black shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div className="flex items-center">
              <button
                onClick={onBack}
                className="text-white hover:text-gray-300 mr-4 transition-colors"
              >
                ← Back
              </button>
              <div className="h-8 w-8 bg-white rounded mr-3 flex items-center justify-center">
                <span className="text-black font-bold text-sm">🚚</span>
              </div>
              <h1 className="text-2xl font-bold text-white">Track Orders</h1>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Search and Filter Controls */}
        <div className="mb-8 grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-black mb-2">
              Search Orders
            </label>
            <input
              type="text"
              placeholder="Search by tracking number, recipient name, or address..."
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-black focus:border-transparent"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-black mb-2">
              Filter by Status
            </label>
            <select
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-black focus:border-transparent"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="all">All Statuses</option>
              <option value="pending">Pending</option>
              <option value="assigned">Assigned</option>
              <option value="in_transit">In Transit</option>
              <option value="delivered">Delivered</option>
              <option value="cancelled">Cancelled</option>
            </select>
          </div>
        </div>

        {/* Orders List */}
        {loading ? (
          <div className="flex justify-center items-center py-12">
            <div className="text-lg">Loading orders...</div>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredOrders.length === 0 ? (
              <div className="text-center py-12">
                <div className="text-gray-500 text-lg">
                  No orders found matching your criteria
                </div>
              </div>
            ) : (
              filteredOrders.map((order) => (
                <div
                  key={order.id}
                  className={`card p-6 cursor-pointer transition-all ${order.status === "pending" ? "hover:shadow-md" : ""}`}
                  onClick={() => handleEditOrder(order)}
                >
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {/* Column 1: Order Info - JSON details */}
                    <div>
                      <div className="flex items-center justify-between mb-2">
                        <h3 className="text-lg font-semibold">
                          Tracking: {order.trackingNumber}
                        </h3>
                        {order.status === "pending" && (
                          <span className="text-xs bg-yellow-100 text-yellow-800 px-2 py-1 rounded">
                            Click to edit
                          </span>
                        )}
                      </div>
                      <div className="mb-1 text-gray-600">
                        <strong>Client ID:</strong> {order.clientId}
                      </div>
                      <div className="mb-1 text-gray-600">
                        <strong>Recipient:</strong> {order.recipientName}
                      </div>
                      <div className="mb-1 text-gray-600">
                        <strong>Address:</strong> {order.recipientAddress}
                      </div>
                      <div className="mb-1 text-gray-600">
                        <strong>Phone:</strong> {order.recipientPhone}
                      </div>
                      <div className="mb-1 text-gray-600">
                        <strong>Package Details:</strong> {order.packageDetails}
                      </div>
                      {order.items && order.items.length > 0 && (
                        <div className="mb-1 text-gray-600">
                          <strong>Items:</strong>
                          <ul className="ml-4 list-disc">
                            {order.items.map((item) => (
                              <li key={item.itemId}>
                                {item.description} (ID: {item.itemId}, Qty: {item.quantity}, Weight: {item.weightKg}kg)
                              </li>
                            ))}
                          </ul>
                        </div>
                      )}
                      {order.notes && (
                        <div className="mb-1 text-gray-600">
                          <strong>Notes:</strong> {order.notes}
                        </div>
                      )}
                    </div>

                    {/* Column 2: Contact/Driver Info, Status, and Actions */}
                    <div className="flex flex-col gap-2 justify-between">
                      <div>
                        <p className="text-gray-600 mb-1">
                          <strong>Created:</strong> {formatDate(order.createdAt)}
                        </p>
                        {order.estimatedDelivery && (
                          <p className="text-gray-600 mb-1">
                            <strong>Est. Delivery:</strong> {formatDate(order.estimatedDelivery)}
                          </p>
                        )}
                        {order.driverName && (
                          <p className="text-gray-600 mb-1">
                            <strong>Driver:</strong> {order.driverName}
                          </p>
                        )}
                        {order.vehicleId && (
                          <p className="text-gray-600 mb-1">
                            <strong>Vehicle:</strong> {order.vehicleId}
                          </p>
                        )}
                        <div className="mb-2">
                          <StatusStepper status={order.status} />
                        </div>
                      </div>
                      <div className="flex flex-col gap-2">
                        {order.driverName && (
                          <button
                            className="btn-secondary text-sm py-1 w-full"
                            onClick={(e) => {
                              e.stopPropagation();
                              // Handle contact driver logic
                            }}
                          >
                            Contact Driver
                          </button>
                        )}
                        {/* Cancel Order Button */}
                        {order.status !== "cancelled" && order.status !== "delivered" && (
                          <button
                            className="text-sm py-1 w-full border border-red-500 text-red-500 rounded hover:bg-red-50 transition-colors"
                            onClick={(e) => {
                              e.stopPropagation();
                              handleCancelOrder(order.id);
                            }}
                          >
                            Cancel Order
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* Summary Stats */}
        <div className="mt-12 grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="bg-gray-50 rounded-lg p-4 text-center">
            <div className="text-2xl font-bold text-black">{orders.length}</div>
            <div className="text-sm text-gray-600">Total Orders</div>
          </div>
          <div className="bg-green-50 rounded-lg p-4 text-center">
            <div className="text-2xl font-bold text-green-600">
              {orders.filter((o) => o.status === "delivered").length}
            </div>
            <div className="text-sm text-gray-600">Delivered</div>
          </div>
          <div className="bg-blue-50 rounded-lg p-4 text-center">
            <div className="text-2xl font-bold text-blue-600">
              {orders.filter((o) => o.status === "in_transit").length}
            </div>
            <div className="text-sm text-gray-600">In Transit</div>
          </div>
          <div className="bg-yellow-50 rounded-lg p-4 text-center">
            <div className="text-2xl font-bold text-yellow-600">
              {orders.filter((o) => o.status === "pending").length}
            </div>
            <div className="text-sm text-gray-600">Pending</div>
          </div>
        </div>
      </main>

      {/* Edit Order Modal */}
      {editingOrder && (
        <EditOrder
          order={editingOrder}
          onSave={handleSaveOrder}
          onCancel={handleCancelEdit}
        />
      )}

      <ConfirmationModal
        isOpen={showCancelModal}
        title="Cancel Order"
        message={`Are you sure you want to cancel order ${orderToCancel}? This action cannot be undone and will notify all relevant parties.`}
        confirmText="Yes, Cancel Order"
        cancelText="Keep Order"
        confirmButtonColor="bg-red-600 hover:bg-red-700 focus:ring-red-500"
        onConfirm={handleConfirmCancel}
        onCancel={handleCancelFromModal}
      />
    </div>
  );
}