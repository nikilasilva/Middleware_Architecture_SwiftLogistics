"use client";

import { useState } from "react";
import { OrderData } from "@/types";

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

interface EditOrderProps {
  order: Order;
  onSave: (updatedOrder: Order) => void;
  onCancel: () => void;
}

export default function EditOrder({ order, onSave, onCancel }: EditOrderProps) {
  const [editedOrder, setEditedOrder] = useState<Order>({ ...order });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setEditedOrder(prev => ({ ...prev, [name]: value }));
  };

  const handleItemChange = (index: number, field: string, value: string | number) => {
    setEditedOrder(prev => {
      const newItems = [...(prev.items || [])];
      newItems[index] = { ...newItems[index], [field]: value };
      return { ...prev, items: newItems };
    });
  };

  const addItem = () => {
    setEditedOrder(prev => ({
      ...prev,
      items: [...(prev.items || []), { itemId: "", description: "", quantity: 1, weightKg: 0 }]
    }));
  };

  const removeItem = (index: number) => {
    setEditedOrder(prev => {
      const newItems = [...(prev.items || [])];
      newItems.splice(index, 1);
      return { ...prev, items: newItems };
    });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(editedOrder);
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <h2 className="text-xl font-bold mb-4">Edit Order #{order.trackingNumber}</h2>
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-black mb-1">Recipient Name</label>
              <input
                type="text"
                name="recipientName"
                value={editedOrder.recipientName}
                onChange={handleInputChange}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black"
                required
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-black mb-1">Recipient Phone</label>
              <input
                type="text"
                name="recipientPhone"
                value={editedOrder.recipientPhone}
                onChange={handleInputChange}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black"
                required
              />
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-black mb-1">Recipient Address</label>
            <input
              type="text"
              name="recipientAddress"
              value={editedOrder.recipientAddress}
              onChange={handleInputChange}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black"
              required
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-black mb-1">Notes</label>
            <textarea
              name="notes"
              value={editedOrder.notes || ""}
              onChange={handleInputChange}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black"
              rows={3}
            />
          </div>
          
          <div>
            <div className="flex justify-between items-center mb-2">
              <label className="block text-sm font-medium text-black">Items</label>
              <button 
                type="button" 
                onClick={addItem}
                className="text-sm bg-blue-500 text-white px-3 py-1 rounded"
              >
                Add Item
              </button>
            </div>
            
            {editedOrder.items?.map((item, index) => (
              <div key={index} className="grid grid-cols-1 md:grid-cols-4 gap-2 mb-2 p-2 border rounded">
                <div>
                  <label className="block text-xs text-black mb-1">Item ID</label>
                  <input
                    type="text"
                    value={item.itemId}
                    onChange={(e) => handleItemChange(index, "itemId", e.target.value)}
                    className="w-full border border-gray-300 rounded px-2 py-1 text-black text-sm"
                    required
                  />
                </div>
                
                <div>
                  <label className="block text-xs text-black mb-1">Description</label>
                  <input
                    type="text"
                    value={item.description}
                    onChange={(e) => handleItemChange(index, "description", e.target.value)}
                    className="w-full border border-gray-300 rounded px-2 py-1 text-black text-sm"
                    required
                  />
                </div>
                
                <div>
                  <label className="block text-xs text-black mb-1">Quantity</label>
                  <input
                    type="number"
                    value={item.quantity}
                    onChange={(e) => handleItemChange(index, "quantity", parseInt(e.target.value) || 1)}
                    className="w-full border border-gray-300 rounded px-2 py-1 text-black text-sm"
                    min="1"
                    required
                  />
                </div>
                
                <div className="flex items-end">
                  <div className="flex-grow">
                    <label className="block text-xs text-black mb-1">Weight (kg)</label>
                    <input
                      type="number"
                      step="0.01"
                      value={item.weightKg}
                      onChange={(e) => handleItemChange(index, "weightKg", parseFloat(e.target.value) || 0)}
                      className="w-full border border-gray-300 rounded px-2 py-1 text-black text-sm"
                      min="0"
                      required
                    />
                  </div>
                  <button 
                    type="button" 
                    onClick={() => removeItem(index)}
                    className="ml-2 text-red-500 hover:text-red-700"
                  >
                    ×
                  </button>
                </div>
              </div>
            ))}
          </div>
          
          <div className="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              onClick={onCancel}
              className="px-4 py-2 border border-gray-300 rounded-lg text-black hover:bg-gray-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-black text-white rounded-lg hover:bg-gray-800"
            >
              Save Changes
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}