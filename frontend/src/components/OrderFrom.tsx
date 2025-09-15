"use client";

import { useState } from "react";
import { ArrowLeft, Package } from "lucide-react";
import { cmsApi } from "@/lib/api";

interface OrderFormProps {
  onBack: () => void;
}

export default function OrderForm({ onBack }: OrderFormProps) {
  // Generate order date as now and delivery date two weeks from now
  const getDefaultOrderDate = () => {
    const date = new Date();
    return date.toISOString().slice(0, 16); // 'YYYY-MM-DDTHH:mm'
  };
  const getDefaultDeliveryDate = () => {
    const date = new Date();
    date.setDate(date.getDate() + 14);
    return date.toISOString().slice(0, 16);
  };

  const [formData, setFormData] = useState({
    clientId: "CLIENT001",
    pickupAddress: "123 Main St, Colombo",
    deliveryAddress: "456 Kandy Road, Kandy",
    clientName: "John Doe",
    clientEmail: "john.doe@example.com",
    recipientName: "Jane Smith",
    recipientAddress: "456 Kandy Road, Kandy",
    recipientPhone: "+94-77-123-4567",
    orderDate: getDefaultOrderDate(),
    deliveryDate: getDefaultDeliveryDate(),
    items: [
      {
        itemId: "ITEM001",
        description: "Wireless Mouse",
        quantity: 2,
        weightKg: 0.2
      },
      {
        itemId: "ITEM002",
        description: "Keyboard",
        quantity: 1,
        weightKg: 0.8
      }
    ],
    notes: "Leave at the front desk if not home"
  });

  // Calculate total weight from items
  const totalWeightKg = Number(
    formData.items.reduce(
      (sum, item) => sum + Number(item.weightKg) * Number(item.quantity),
      0
    ).toFixed(3)
  );
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      // Generate packageDetails from items array
      const packageDetails = formData.items
        .map(
          (item, idx) =>
            `#${idx + 1}: ${item.description} (ID: ${item.itemId}, Qty: ${item.quantity}, Weight: ${item.weightKg}kg)`
        )
        .join('; ');

      const orderPayload = {
        clientId: formData.clientId,
        recipientName: formData.recipientName,
        recipientAddress: formData.recipientAddress,
        recipientPhone: formData.recipientPhone,
        packageDetails
      };

      const response = await cmsApi.createOrder(orderPayload);
      setSuccess("Order created successfully!");
      setFormData({
        clientId: "CLIENT001",
        pickupAddress: "",
        deliveryAddress: "",
        clientName: "",
        clientEmail: "",
        recipientName: "",
        recipientAddress: "",
        recipientPhone: "",
        orderDate: getDefaultOrderDate(),
        deliveryDate: getDefaultDeliveryDate(),
        items: [
          {
            itemId: "",
            description: "",
            quantity: 1,
            weightKg: 0
          }
        ],
        notes: ""
      });
    } catch (err: any) {
      setError("Failed to create order. Please try again.");
      console.error("Order creation error:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
    >
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // For handling items array
  const handleItemChange = (idx: number, e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => {
      const items = [...prev.items];
      items[idx] = {
        ...items[idx],
        [name]: name === 'quantity' || name === 'weightKg' ? Number(value) : value
      };
      return { ...prev, items };
    });
  };

  const addItem = () => {
    setFormData((prev) => ({
      ...prev,
      items: [
        ...prev.items,
        { itemId: "", description: "", quantity: 1, weightKg: 0 }
      ]
    }));
  };

  const removeItem = (idx: number) => {
    setFormData((prev) => ({
      ...prev,
      items: prev.items.filter((_, i) => i !== idx)
    }));
  };

  const handleMockCreate = () => {
    setLoading(true);
    setError(null);
    setSuccess(null);
    setTimeout(() => {
      setSuccess("Order created successfully!");
      setLoading(false);
      setTimeout(() => {
        onBack(); // Go to home page after showing success
      }, 1200);
    }, 800);
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
              <h1 className="text-2xl font-bold text-white">Create New Order</h1>
            </div>
          </div>
        </div>
      </header>

      {/* Form */}
      <main className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Client Selection */}
            <div>
              <label htmlFor="clientId" className="block text-sm font-medium text-black mb-2">Client</label>
              <select id="clientId" name="clientId" value={formData.clientId} onChange={handleChange} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-primary" required>
                <option value="CLIENT001">TechMart Electronics</option>
                <option value="CLIENT002">Fashion Hub Lanka</option>
              </select>
            </div>
            {/* Pickup Address */}
            <div>
              <label htmlFor="pickupAddress" className="block text-sm font-medium text-black mb-2">Pickup Address</label>
              <input type="text" id="pickupAddress" name="pickupAddress" value={formData.pickupAddress} onChange={handleChange} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-primary" required />
            </div>
            {/* Delivery Address */}
            <div>
              <label htmlFor="deliveryAddress" className="block text-sm font-medium text-black mb-2">Delivery Address</label>
              <input type="text" id="deliveryAddress" name="deliveryAddress" value={formData.deliveryAddress} onChange={handleChange} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-primary" required />
            </div>
            {/* Client Name */}
            <div>
              <label htmlFor="clientName" className="block text-sm font-medium text-black mb-2">Client Name</label>
              <input type="text" id="clientName" name="clientName" value={formData.clientName} onChange={handleChange} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            {/* Client Email */}
            <div>
              <label htmlFor="clientEmail" className="block text-sm font-medium text-black mb-2">Client Email</label>
              <input type="email" id="clientEmail" name="clientEmail" value={formData.clientEmail} onChange={handleChange} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            {/* Recipient Name */}
            <div>
              <label htmlFor="recipientName" className="block text-sm font-medium text-black mb-2">Recipient Name</label>
              <input type="text" id="recipientName" name="recipientName" value={formData.recipientName} onChange={handleChange} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-primary" required />
            </div>
            {/* Recipient Address */}
            <div>
              <label htmlFor="recipientAddress" className="block text-sm font-medium text-black mb-2">Recipient Address</label>
              <input type="text" id="recipientAddress" name="recipientAddress" value={formData.recipientAddress} onChange={handleChange} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            {/* Recipient Phone */}
            <div>
              <label htmlFor="recipientPhone" className="block text-sm font-medium text-black mb-2">Recipient Phone</label>
              <input type="tel" id="recipientPhone" name="recipientPhone" value={formData.recipientPhone} onChange={handleChange} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            {/* Order Date (auto-generated, read-only) */}
            <div>
              <label htmlFor="orderDate" className="block text-sm font-medium text-black mb-2">Order Date</label>
              <input type="datetime-local" id="orderDate" name="orderDate" value={formData.orderDate} readOnly className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black bg-gray-100 cursor-not-allowed" />
            </div>
            {/* Delivery Date (auto-generated, read-only) */}
            <div>
              <label htmlFor="deliveryDate" className="block text-sm font-medium text-black mb-2">Delivery Date</label>
              <input type="datetime-local" id="deliveryDate" name="deliveryDate" value={formData.deliveryDate} readOnly className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black bg-gray-100 cursor-not-allowed" />
            </div>
            {/* Items */}
            <div>
              <label className="block text-sm font-medium text-black mb-2">Items</label>
              {formData.items.map((item, idx) => (
                <div key={idx} className="flex gap-2 mb-2 items-end">
                  <div className="flex flex-col w-1/5">
                    <label className="text-xs text-gray-600 mb-1">Item ID</label>
                    <input type="text" name="itemId" placeholder="Item ID" value={item.itemId} onChange={e => handleItemChange(idx, e)} className="border border-gray-300 rounded-lg px-2 py-1 text-black" />
                  </div>
                  <div className="flex flex-col w-2/5">
                    <label className="text-xs text-gray-600 mb-1">Description</label>
                    <input type="text" name="description" placeholder="Description" value={item.description} onChange={e => handleItemChange(idx, e)} className="border border-gray-300 rounded-lg px-2 py-1 text-black" />
                  </div>
                  <div className="flex flex-col w-1/6">
                    <label className="text-xs text-gray-600 mb-1">Qty</label>
                    <input type="number" name="quantity" placeholder="Qty" value={item.quantity} min={1} onChange={e => handleItemChange(idx, e)} className="border border-gray-300 rounded-lg px-2 py-1 text-black" />
                  </div>
                  <div className="flex flex-col w-1/6">
                    <label className="text-xs text-gray-600 mb-1">Weight</label>
                    <input type="number" name="weightKg" placeholder="Weight (kg)" value={item.weightKg} min={0} step={0.01} onChange={e => handleItemChange(idx, e)} className="border border-gray-300 rounded-lg px-2 py-1 text-black" />
                  </div>
                  <button type="button" onClick={() => removeItem(idx)} className="text-red-500 font-bold mb-4">&times;</button>
                </div>
              ))}
              <button type="button" onClick={addItem} className="mt-1 px-3 py-1 bg-gray-200 rounded text-sm">Add Item</button>
            </div>
            {/* Total Weight (auto-generated, read-only) */}
            <div>
              <label htmlFor="totalWeightKg" className="block text-sm font-medium text-black mb-2">Total Weight (kg)</label>
              <input type="number" id="totalWeightKg" name="totalWeightKg" value={totalWeightKg} readOnly className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black bg-gray-100 cursor-not-allowed" />
            </div>
            {/* Notes */}
            <div>
              <label htmlFor="notes" className="block text-sm font-medium text-black mb-2">Notes</label>
              <textarea id="notes" name="notes" value={formData.notes} onChange={handleChange} rows={2} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black" />
            </div>

            {/* Success/Error Messages */}
            {success && (
              <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                <p className="text-green-800">{success}</p>
              </div>
            )}

            {error && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                <p className="text-red-800">{error}</p>
              </div>
            )}

            {/* Submit Button */}
            <div className="flex gap-4">
              <button
                type="button"
                onClick={onBack}
                className="px-6 py-2 border border-gray-300 rounded-lg text-black hover:bg-gray-50 transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading}
                className="px-6 py-2 bg-primary hover:bg-primary-dark text-white rounded-lg transition-colors disabled:opacity-50"
              >
                {loading ? "Creating..." : "Create Order"}
              </button>
              <button
                type="button"
                onClick={handleMockCreate}
                disabled={loading}
                className="px-6 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors disabled:opacity-50"
              >
                {loading ? "Creating..." : "Create"}
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
