"use client";

import { useState } from "react";
import TrackOrders from "../components/TrackOrders";
import Analytics from "../components/Analytics";
import OrderForm from "../components/OrderFrom"; // Corrected import statement

export default function Home() {
  const [currentView, setCurrentView] = useState<
    "dashboard" | "createOrder" | "trackOrders" | "analytics"
  >("dashboard");

  const showOrderForm = () => setCurrentView("createOrder");
  const showDashboard = () => setCurrentView("dashboard");
  const showTrackOrders = () => setCurrentView("trackOrders");
  const showAnalytics = () => setCurrentView("analytics");

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
              <button
                onClick={() => {
                  // Remove the signedin cookie
                  document.cookie = 'signedin=; path=/; max-age=0';
                  window.location.href = '/login';
                }}
                className="ml-4 bg-white text-black px-4 py-2 rounded shadow hover:bg-gray-200 transition-colors font-semibold"
              >
                Logout
              </button>
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
              <button
                onClick={showTrackOrders}
                className="btn-primary w-full py-3"
              >
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
              <button
                onClick={showAnalytics}
                className="btn-primary w-full py-3"
              >
                View Reports
              </button>
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



  return (
    <div>
      {currentView === "dashboard" && <DashboardWithNavigation />}
      {currentView === "createOrder" && <OrderForm onBack={showDashboard} />}
      {currentView === "trackOrders" && <TrackOrders onBack={showDashboard} />}
      {currentView === "analytics" && <Analytics onBack={showDashboard} />}
    </div>
  );
}
