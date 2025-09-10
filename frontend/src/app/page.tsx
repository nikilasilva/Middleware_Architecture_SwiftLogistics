"use client";

import { useState } from "react";

export default function Home() {
  const [currentView, setCurrentView] = useState<"dashboard" | "createOrder">(
    "dashboard"
  );

  const showOrderForm = () => setCurrentView("createOrder");
  const showDashboard = () => setCurrentView("dashboard");

  const DashboardWithNavigation = () => (
    <div className="min-h-screen bg-white text-black">
      {/* Header */}
      <header className="bg-black shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div className="flex items-center">
              <div className="h-8 w-8 bg-white rounded mr-3 flex items-center justify-center">
                <span className="text-black font-bold text-sm">SL</span>
              </div>
              <h1 className="text-2xl font-bold text-white">SwiftLogistics</h1>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-white">Client Portal</span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

        {/* Welcome Section */}
        <div className="text-center mb-12">
          <h2 className="text-4xl font-bold text-black mb-4">
            Welcome to SwiftLogistics
          </h2>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Manage your deliveries with our comprehensive logistics platform.
            Create orders, track shipments, and optimize routes all in one
            place.
          </p>
        </div>

        {/* Action Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <div className="card p-8 hover:scale-105 transform transition-transform">
            <div className="text-center">
              <div className="w-16 h-16 bg-black rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-white text-2xl">📦</span>
              </div>
              <h3 className="text-xl font-semibold text-black mb-4">
                Create Order
              </h3>
              <p className="text-gray-600 mb-6">
                Start a new delivery order with detailed recipient information
              </p>
              <button
                onClick={showOrderForm}
                className="btn-primary w-full py-3"
              >
                Create New Order
              </button>
            </div>
          </div>

          <div className="card p-8 hover:scale-105 transform transition-transform">
            <div className="text-center">
              <div className="w-16 h-16 bg-black rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-white text-2xl">🚚</span>
              </div>
              <h3 className="text-xl font-semibold text-black mb-4">
                Track Orders
              </h3>
              <p className="text-gray-600 mb-6">
                Monitor your shipments and get real-time delivery updates
              </p>
              <button className="btn-primary w-full py-3">
                Track Shipments
              </button>
            </div>
          </div>

          <div className="card p-8 hover:scale-105 transform transition-transform">
            <div className="text-center">
              <div className="w-16 h-16 bg-black rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-white text-2xl">📊</span>
              </div>
              <h3 className="text-xl font-semibold text-black mb-4">
                Analytics
              </h3>
              <p className="text-gray-600 mb-6">
                View delivery statistics and performance metrics
              </p>
              <button className="btn-primary w-full py-3">View Reports</button>
            </div>
          </div>
        </div>

        {/* System Status */}
        <div className="mt-12 bg-gray-50 rounded-lg p-6 border border-gray-200">
          <h3 className="text-lg font-semibold text-black mb-4">
            System Status
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="flex items-center p-3 bg-white rounded border">
              <div className="w-3 h-3 bg-green-500 rounded-full mr-3"></div>
              <span className="text-black font-medium">CMS System: Online</span>
            </div>
            <div className="flex items-center p-3 bg-white rounded border">
              <div className="w-3 h-3 bg-green-500 rounded-full mr-3"></div>
              <span className="text-black font-medium">ROS System: Online</span>
            </div>
            <div className="flex items-center p-3 bg-white rounded border">
              <div className="w-3 h-3 bg-green-500 rounded-full mr-3"></div>
              <span className="text-black font-medium">WMS System: Online</span>
            </div>
          </div>
        </div>
      </main>
    </div>
  );

  const OrderForm = ({ onBack }: { onBack: () => void }) => (
    <div className="min-h-screen bg-white text-black">
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
              <h1 className="text-2xl font-bold text-white">
                Create New Order
              </h1>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="card p-6">
          <form className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-black mb-2">
                Client
              </label>
              <select className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-black focus:border-transparent">
                <option>TechMart Electronics</option>
                <option>Fashion Hub Lanka</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-black mb-2">
                Recipient Name
              </label>
              <input
                type="text"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-black focus:border-transparent"
                placeholder="Enter recipient name"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-black mb-2">
                Delivery Address
              </label>
              <textarea
                rows={3}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-black focus:border-transparent resize-none"
                placeholder="Enter delivery address"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-black mb-2">
                Phone Number
              </label>
              <input
                type="tel"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-black focus:border-transparent"
                placeholder="Enter phone number"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-black mb-2">
                Package Details
              </label>
              <textarea
                rows={3}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-black focus:border-transparent resize-none"
                placeholder="Describe package contents and special instructions"
                required
              />
            </div>

            <div className="flex gap-4 pt-4">
              <button
                type="button"
                onClick={onBack}
                className="btn-secondary flex-1"
              >
                Cancel
              </button>
              <button type="submit" className="btn-primary flex-1">
                Create Order
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );

  return (
    <div>
      {currentView === "dashboard" && <DashboardWithNavigation />}
      {currentView === "createOrder" && <OrderForm onBack={showDashboard} />}
    </div>
  );
}
