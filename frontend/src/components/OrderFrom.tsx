"use client";

import { useState } from "react";
import { ArrowLeft, Package } from "lucide-react";
import { cmsApi } from "@/lib/api";

interface OrderFormProps {
  onBack: () => void;
}

export default function OrderForm({ onBack }: OrderFormProps) {
  const [formData, setFormData] = useState({
    clientId: "CLIENT001",
    recipientName: "",
    recipientAddress: "",
    recipientPhone: "",
    packageDetails: "",
  });
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const response = await cmsApi.createOrder(formData);
      setSuccess("Order created successfully!");
      setFormData({
        clientId: "CLIENT001",
        recipientName: "",
        recipientAddress: "",
        recipientPhone: "",
        packageDetails: "",
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
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  return (
    <div className="min-h-screen bg-white text-black">
      {/* Header */}
      <header className="bg-primary shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div className="flex items-center">
              <button
                onClick={onBack}
                className="text-white hover:text-gray-200 mr-4"
              >
                <ArrowLeft className="h-6 w-6" />
              </button>
              <Package className="h-8 w-8 text-white mr-3" />
              <h1 className="text-2xl font-bold text-white">
                Create New Order
              </h1>
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
              <label
                htmlFor="clientId"
                className="block text-sm font-medium text-black mb-2"
              >
                Client
              </label>
              <select
                id="clientId"
                name="clientId"
                value={formData.clientId}
                onChange={handleChange}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-primary"
                required
              >
                <option value="CLIENT001">TechMart Electronics</option>
                <option value="CLIENT002">Fashion Hub Lanka</option>
              </select>
            </div>

            {/* Recipient Name */}
            <div>
              <label
                htmlFor="recipientName"
                className="block text-sm font-medium text-black mb-2"
              >
                Recipient Name
              </label>
              <input
                type="text"
                id="recipientName"
                name="recipientName"
                value={formData.recipientName}
                onChange={handleChange}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-primary"
                required
              />
            </div>

            {/* Recipient Address */}
            <div>
              <label
                htmlFor="recipientAddress"
                className="block text-sm font-medium text-black mb-2"
              >
                Delivery Address
              </label>
              <textarea
                id="recipientAddress"
                name="recipientAddress"
                value={formData.recipientAddress}
                onChange={handleChange}
                rows={3}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-primary"
                required
              />
            </div>

            {/* Recipient Phone */}
            <div>
              <label
                htmlFor="recipientPhone"
                className="block text-sm font-medium text-black mb-2"
              >
                Phone Number
              </label>
              <input
                type="tel"
                id="recipientPhone"
                name="recipientPhone"
                value={formData.recipientPhone}
                onChange={handleChange}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-primary"
                required
              />
            </div>

            {/* Package Details */}
            <div>
              <label
                htmlFor="packageDetails"
                className="block text-sm font-medium text-black mb-2"
              >
                Package Details
              </label>
              <textarea
                id="packageDetails"
                name="packageDetails"
                value={formData.packageDetails}
                onChange={handleChange}
                rows={3}
                placeholder="Describe the package contents..."
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-primary"
                required
              />
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
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
