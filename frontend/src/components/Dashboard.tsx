'use client';

import { useState, useEffect } from 'react';
import { Truck, Package, Users, Activity } from 'lucide-react';
import { cmsApi, rosApi } from '@/lib/api';

interface SystemHealth {
  cms: any;
  ros: any;
}

export default function Dashboard() {
  const [systemHealth, setSystemHealth] = useState<SystemHealth | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkSystemHealth();
  }, []);

  const checkSystemHealth = async () => {
    try {
      const [cmsHealth, rosHealth] = await Promise.all([
        cmsApi.healthCheck(),
        rosApi.healthCheck()
      ]);

      setSystemHealth({
        cms: cmsHealth,
        ros: rosHealth
      });
    } catch (error) {
      console.error('Error checking system health:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white text-black">
      {/* Header */}
      <header className="bg-primary shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div className="flex items-center">
              <Package className="h-8 w-8 text-white mr-3" />
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
        {/* System Status Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
            <div className="flex items-center">
              <Users className="h-8 w-8 text-primary mr-3" />
              <div>
                <h3 className="text-lg font-semibold text-black">CMS System</h3>
                <p className="text-sm text-gray-600">
                  Status: {systemHealth?.cms?.status || 'Unknown'}
                </p>
                <p className="text-sm text-gray-600">
                  Orders: {systemHealth?.cms?.orders_count || 0}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
            <div className="flex items-center">
              <Truck className="h-8 w-8 text-primary mr-3" />
              <div>
                <h3 className="text-lg font-semibold text-black">ROS System</h3>
                <p className="text-sm text-gray-600">
                  Status: {systemHealth?.ros?.status || 'Unknown'}
                </p>
                <p className="text-sm text-gray-600">
                  Vehicles: {systemHealth?.ros?.total_vehicles || 0}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
            <div className="flex items-center">
              <Activity className="h-8 w-8 text-primary mr-3" />
              <div>
                <h3 className="text-lg font-semibold text-black">System Health</h3>
                <p className="text-sm text-gray-600">
                  All systems operational
                </p>
                <p className="text-sm text-gray-600">
                  Last checked: {new Date().toLocaleTimeString()}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
            <h3 className="text-xl font-semibold text-black mb-4">Create New Order</h3>
            <p className="text-gray-600 mb-4">
              Create a new delivery order in the system
            </p>
            <button className="bg-primary hover:bg-primary-dark text-white font-medium py-2 px-4 rounded-lg transition-colors">
              Create Order
            </button>
          </div>

          <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
            <h3 className="text-xl font-semibold text-black mb-4">Track Deliveries</h3>
            <p className="text-gray-600 mb-4">
              View and track existing delivery orders
            </p>
            <button className="bg-primary hover:bg-primary-dark text-white font-medium py-2 px-4 rounded-lg transition-colors">
              Track Orders
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}