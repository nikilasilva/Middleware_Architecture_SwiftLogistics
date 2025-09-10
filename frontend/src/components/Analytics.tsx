"use client";

import { useState, useEffect } from "react";
import { SystemHealth } from "../types";

interface AnalyticsData {
  totalOrders: number;
  completedDeliveries: number;
  activeRoutes: number;
  averageDeliveryTime: number;
  deliverySuccess: number;
  monthlyGrowth: number;
  recentActivity: Array<{
    id: string;
    action: string;
    timestamp: string;
    details: string;
  }>;
  deliveryStats: Array<{
    date: string;
    delivered: number;
    pending: number;
  }>;
}

interface AnalyticsProps {
  onBack: () => void;
}

export default function Analytics({ onBack }: AnalyticsProps) {
  const [analyticsData, setAnalyticsData] = useState<AnalyticsData | null>(
    null
  );
  const [systemHealth, setSystemHealth] = useState<SystemHealth | null>(null);
  const [loading, setLoading] = useState(true);
  const [timeRange, setTimeRange] = useState("7days");

  useEffect(() => {
    fetchAnalyticsData();
    fetchSystemHealth();
  }, [timeRange]);

  const fetchAnalyticsData = async () => {
    try {
      setLoading(true);
      // Replace with actual API call
      const mockData: AnalyticsData = {
        totalOrders: 1247,
        completedDeliveries: 1189,
        activeRoutes: 23,
        averageDeliveryTime: 24.5,
        deliverySuccess: 95.3,
        monthlyGrowth: 12.8,
        recentActivity: [
          {
            id: "1",
            action: "Order Delivered",
            timestamp: "2024-01-16T15:30:00Z",
            details: "SL001234 delivered to John Doe",
          },
          {
            id: "2",
            action: "Route Optimized",
            timestamp: "2024-01-16T14:15:00Z",
            details: "Route VH001 optimized with 8 stops",
          },
          {
            id: "3",
            action: "New Order Created",
            timestamp: "2024-01-16T13:45:00Z",
            details: "Order SL001237 created for TechMart",
          },
        ],
        deliveryStats: [
          { date: "2024-01-10", delivered: 45, pending: 8 },
          { date: "2024-01-11", delivered: 52, pending: 12 },
          { date: "2024-01-12", delivered: 38, pending: 6 },
          { date: "2024-01-13", delivered: 61, pending: 15 },
          { date: "2024-01-14", delivered: 47, pending: 9 },
          { date: "2024-01-15", delivered: 55, pending: 11 },
          { date: "2024-01-16", delivered: 43, pending: 7 },
        ],
      };
      setAnalyticsData(mockData);
    } catch (error) {
      console.error("Error fetching analytics:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchSystemHealth = async () => {
    try {
      // Replace with actual API call
      const mockHealth: SystemHealth = {
        cms: {
          status: "online",
          orders_count: 1247,
          clients_count: 45,
          protocol: "HTTP",
          service: "cms",
          timestamp: new Date().toISOString(),
        },
        ros: {
          status: "online",
          active_routes: 23,
          available_vehicles: 15,
          total_vehicles: 38,
          protocol: "HTTP",
          service: "ros",
          timestamp: new Date().toISOString(),
        },
        wms: {
          status: "online",
          total_packages: 892,
          zones: {
            zone_a: { packages: 234, capacity: 500 },
            zone_b: { packages: 456, capacity: 600 },
            zone_c: { packages: 202, capacity: 400 },
          },
        },
      };
      setSystemHealth(mockHealth);
    } catch (error) {
      console.error("Error fetching system health:", error);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
    });
  };

  const formatTime = (dateString: string) => {
    return new Date(dateString).toLocaleTimeString("en-US", {
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-lg">Loading analytics...</div>
      </div>
    );
  }

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
                <span className="text-black font-bold text-sm">📊</span>
              </div>
              <h1 className="text-2xl font-bold text-white">
                Analytics Dashboard
              </h1>
            </div>
            <div>
              <select
                className="bg-white text-black px-3 py-1 rounded border"
                value={timeRange}
                onChange={(e) => setTimeRange(e.target.value)}
              >
                <option value="7days">Last 7 Days</option>
                <option value="30days">Last 30 Days</option>
                <option value="90days">Last 90 Days</option>
              </select>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Key Metrics */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="card p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Orders</p>
                <p className="text-3xl font-bold text-black">
                  {analyticsData?.totalOrders}
                </p>
              </div>
              <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                <span className="text-blue-600 text-xl">📦</span>
              </div>
            </div>
          </div>

          <div className="card p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Completed Deliveries</p>
                <p className="text-3xl font-bold text-green-600">
                  {analyticsData?.completedDeliveries}
                </p>
              </div>
              <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                <span className="text-green-600 text-xl">✅</span>
              </div>
            </div>
          </div>

          <div className="card p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Active Routes</p>
                <p className="text-3xl font-bold text-orange-600">
                  {analyticsData?.activeRoutes}
                </p>
              </div>
              <div className="w-12 h-12 bg-orange-100 rounded-full flex items-center justify-center">
                <span className="text-orange-600 text-xl">🚚</span>
              </div>
            </div>
          </div>

          <div className="card p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Success Rate</p>
                <p className="text-3xl font-bold text-purple-600">
                  {analyticsData?.deliverySuccess}%
                </p>
              </div>
              <div className="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center">
                <span className="text-purple-600 text-xl">🎯</span>
              </div>
            </div>
          </div>
        </div>

        {/* Charts and Additional Info */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Delivery Stats Chart */}
          <div className="card p-6">
            <h3 className="text-lg font-semibold mb-4">
              Daily Delivery Performance
            </h3>
            <div className="space-y-3">
              {analyticsData?.deliveryStats.map((stat, index) => (
                <div key={index} className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">
                    {formatDate(stat.date)}
                  </span>
                  <div className="flex space-x-4">
                    <span className="text-sm">
                      <span className="text-green-600">✓ {stat.delivered}</span>
                    </span>
                    <span className="text-sm">
                      <span className="text-yellow-600">⏳ {stat.pending}</span>
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* System Health */}
          <div className="card p-6">
            <h3 className="text-lg font-semibold mb-4">System Health</h3>
            <div className="space-y-4">
              {systemHealth?.cms && (
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-medium">CMS System</p>
                    <p className="text-sm text-gray-600">
                      {systemHealth.cms.orders_count} orders,{" "}
                      {systemHealth.cms.clients_count} clients
                    </p>
                  </div>
                  <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                </div>
              )}

              {systemHealth?.ros && (
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-medium">ROS System</p>
                    <p className="text-sm text-gray-600">
                      {systemHealth.ros.available_vehicles}/
                      {systemHealth.ros.total_vehicles} vehicles available
                    </p>
                  </div>
                  <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                </div>
              )}

              {systemHealth?.wms && (
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-medium">WMS System</p>
                    <p className="text-sm text-gray-600">
                      {systemHealth.wms.total_packages} packages in warehouse
                    </p>
                  </div>
                  <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Recent Activity */}
        <div className="card p-6">
          <h3 className="text-lg font-semibold mb-4">Recent Activity</h3>
          <div className="space-y-3">
            {analyticsData?.recentActivity.map((activity) => (
              <div
                key={activity.id}
                className="flex items-center justify-between border-b border-gray-100 pb-3"
              >
                <div>
                  <p className="font-medium">{activity.action}</p>
                  <p className="text-sm text-gray-600">{activity.details}</p>
                </div>
                <div className="text-sm text-gray-500">
                  {formatTime(activity.timestamp)}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Performance Metrics */}
        <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="card p-6 text-center">
            <div className="text-2xl font-bold text-blue-600">
              {analyticsData?.averageDeliveryTime}h
            </div>
            <div className="text-sm text-gray-600">Average Delivery Time</div>
          </div>
          <div className="card p-6 text-center">
            <div className="text-2xl font-bold text-green-600">
              +{analyticsData?.monthlyGrowth}%
            </div>
            <div className="text-sm text-gray-600">Monthly Growth</div>
          </div>
          <div className="card p-6 text-center">
            <div className="text-2xl font-bold text-purple-600">
              {analyticsData?.deliverySuccess}%
            </div>
            <div className="text-sm text-gray-600">Customer Satisfaction</div>
          </div>
        </div>
      </main>
    </div>
  );
}
