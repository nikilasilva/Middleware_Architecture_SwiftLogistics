/* eslint-disable @typescript-eslint/no-explicit-any */
"use client";
import { useState, useEffect, useCallback } from "react";
import { getOrderStatus } from "@/lib/ordersApi";

interface OrderStatusDetails {
    success: boolean;
    orderId: string;
    cmsStatus?: string;
    wmsStatus?: string;
    rosStatus?: string;
    overallStatus?: string;
    customerMessage?: string;
    warehouseInfo?: string;
    routeInfo?: string;
    clientInfo?: string;
    estimatedDelivery?: string;
    lastUpdated?: string;
    trackingHistory?: Array<{
        timestamp: string;
        status: string;
        location?: string;
        notes?: string;
    }>;
    queriedBy?: string;
    queryTimestamp?: number;
}

interface OrderStatusModalProps {
    isOpen: boolean;
    orderId: string;
    orderDetails?: any; // The order object from your list
    onClose: () => void;
}

export default function OrderStatusModal({
    isOpen,
    orderId,
    orderDetails,
    onClose,
}: OrderStatusModalProps) {
    const [statusDetails, setStatusDetails] = useState<OrderStatusDetails | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Use useCallback to memoize the function and fix useEffect dependency
    const fetchOrderStatus = useCallback(async () => {
        if (!orderId) return;

        setLoading(true);
        setError(null);

        try {
            console.log("Fetching status for order:", orderId);

            // Use the API function from ordersApi.ts
            const data = await getOrderStatus(orderId);
            console.log("Order status response:", data);

            setStatusDetails(data);
        } catch (err) {
            console.error("Error fetching order status:", err);
            setError(`Failed to load status details: ${(err as Error).message}`);
        } finally {
            setLoading(false);
        }
    }, [orderId]); // Only depends on orderId

    // Fetch detailed status when modal opens
    useEffect(() => {
        if (isOpen && orderId) {
            fetchOrderStatus();
        }
    }, [isOpen, orderId, fetchOrderStatus]); // Now includes fetchOrderStatus

    const formatTimestamp = (timestamp?: number) => {
        if (!timestamp) return 'N/A';
        return new Date(timestamp).toLocaleString();
    };

    const getStatusColor = (status?: string) => {
        if (!status) return 'bg-gray-100 text-gray-800';

        switch (status.toUpperCase()) {
            case 'DELIVERED': return 'bg-green-100 text-green-800';
            case 'IN_TRANSIT': return 'bg-blue-100 text-blue-800';
            case 'PROCESSING': return 'bg-yellow-100 text-yellow-800';
            case 'PENDING': return 'bg-orange-100 text-orange-800';
            case 'CANCELLED': return 'bg-red-100 text-red-800';
            case 'CONFIRMED': return 'bg-green-100 text-green-800';
            case 'IN_WAREHOUSE': return 'bg-blue-100 text-blue-800';
            case 'ROUTE_OPTIMIZED': return 'bg-purple-100 text-purple-800';
            case 'ROUTE_NOT_FOUND': return 'bg-yellow-100 text-yellow-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    // Reset state when modal closes
    useEffect(() => {
        if (!isOpen) {
            setStatusDetails(null);
            setError(null);
            setLoading(false);
        }
    }, [isOpen]);

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b border-gray-200">
                    <div>
                        <h2 className="text-xl font-semibold text-gray-900">Order Status Details</h2>
                        <p className="text-sm text-gray-600">Order ID: {orderId}</p>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600 transition-colors"
                    >
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                {/* Content */}
                <div className="p-6">
                    {loading ? (
                        <div className="flex items-center justify-center py-12">
                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                            <span className="ml-3 text-gray-600">Loading status details...</span>
                        </div>
                    ) : error ? (
                        <div className="text-center py-12">
                            <div className="text-red-600 mb-4">❌ {error}</div>
                            <button
                                onClick={fetchOrderStatus}
                                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                            >
                                Retry
                            </button>
                        </div>
                    ) : statusDetails ? (
                        <div className="space-y-6">
                            {/* Overall Status */}
                            <div className="bg-gray-50 rounded-lg p-4">
                                <h3 className="text-lg font-medium text-gray-900 mb-3">Overall Status</h3>
                                <div className="flex items-center gap-4">
                                    <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(statusDetails.overallStatus)}`}>
                                        {statusDetails.overallStatus || 'Unknown'}
                                    </span>
                                    {statusDetails.customerMessage && (
                                        <span className="text-gray-600">{statusDetails.customerMessage}</span>
                                    )}
                                </div>
                            </div>

                            {/* System Status Breakdown */}
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                {/* CMS Status */}
                                <div className="border border-gray-200 rounded-lg p-4">
                                    <h4 className="font-medium text-gray-900 mb-2">📋 Customer Management</h4>
                                    <span className={`px-2 py-1 rounded text-sm ${getStatusColor(statusDetails.cmsStatus)}`}>
                                        {statusDetails.cmsStatus || 'N/A'}
                                    </span>
                                    {statusDetails.clientInfo && (
                                        <p className="text-sm text-gray-600 mt-2">{statusDetails.clientInfo}</p>
                                    )}
                                </div>

                                {/* WMS Status */}
                                <div className="border border-gray-200 rounded-lg p-4">
                                    <h4 className="font-medium text-gray-900 mb-2">📦 Warehouse</h4>
                                    <span className={`px-2 py-1 rounded text-sm ${getStatusColor(statusDetails.wmsStatus)}`}>
                                        {statusDetails.wmsStatus || 'N/A'}
                                    </span>
                                    {statusDetails.warehouseInfo && (
                                        <p className="text-sm text-gray-600 mt-2">{statusDetails.warehouseInfo}</p>
                                    )}
                                </div>

                                {/* ROS Status */}
                                <div className="border border-gray-200 rounded-lg p-4">
                                    <h4 className="font-medium text-gray-900 mb-2">🚚 Route Optimization</h4>
                                    <span className={`px-2 py-1 rounded text-sm ${getStatusColor(statusDetails.rosStatus)}`}>
                                        {statusDetails.rosStatus || 'N/A'}
                                    </span>
                                    {statusDetails.routeInfo && (
                                        <p className="text-sm text-gray-600 mt-2">{statusDetails.routeInfo}</p>
                                    )}
                                </div>
                            </div>

                            {/* Order Details from Frontend */}
                            {orderDetails && (
                                <div className="border border-gray-200 rounded-lg p-4">
                                    <h4 className="font-medium text-gray-900 mb-3">📋 Order Information</h4>
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                                        <div>
                                            <p><strong>Recipient:</strong> {orderDetails.recipientName}</p>
                                            <p><strong>Address:</strong> {orderDetails.recipientAddress}</p>
                                            <p><strong>Phone:</strong> {orderDetails.recipientPhone}</p>
                                        </div>
                                        <div>
                                            <p><strong>Client ID:</strong> {orderDetails.clientId}</p>
                                            <p><strong>Tracking:</strong> {orderDetails.trackingNumber}</p>
                                            <p><strong>Created:</strong> {new Date(orderDetails.createdAt).toLocaleString()}</p>
                                        </div>
                                    </div>

                                    {/* Items */}
                                    {orderDetails.items && orderDetails.items.length > 0 && (
                                        <div className="mt-4">
                                            <h5 className="font-medium text-gray-900 mb-2">📦 Items</h5>
                                            <ul className="space-y-1">
                                                {orderDetails.items.map((item: any, index: number) => (
                                                    <li key={item.itemId || index} className="text-sm text-gray-600">
                                                        • {item.description} (ID: {item.itemId}, Qty: {item.quantity}, Weight: {item.weightKg}kg)
                                                    </li>
                                                ))}
                                            </ul>
                                        </div>
                                    )}
                                </div>
                            )}

                            {/* Delivery Information */}
                            {(statusDetails.estimatedDelivery || orderDetails?.estimatedDelivery) && (
                                <div className="border border-gray-200 rounded-lg p-4">
                                    <h4 className="font-medium text-gray-900 mb-2">🚚 Delivery Information</h4>
                                    <p className="text-sm text-gray-600">
                                        <strong>Estimated Delivery:</strong> {statusDetails.estimatedDelivery || orderDetails?.estimatedDelivery}
                                    </p>
                                    {orderDetails?.driverName && (
                                        <p className="text-sm text-gray-600">
                                            <strong>Driver:</strong> {orderDetails.driverName}
                                        </p>
                                    )}
                                    {orderDetails?.vehicleId && (
                                        <p className="text-sm text-gray-600">
                                            <strong>Vehicle:</strong> {orderDetails.vehicleId}
                                        </p>
                                    )}
                                </div>
                            )}

                            {/* Timestamps */}
                            <div className="bg-gray-50 rounded-lg p-4">
                                <h4 className="font-medium text-gray-900 mb-2">🕒 System Information</h4>
                                <div className="text-sm text-gray-600 space-y-1">
                                    <p><strong>Last Updated:</strong> {statusDetails.lastUpdated || formatTimestamp(statusDetails.queryTimestamp)}</p>
                                    <p><strong>Queried By:</strong> {statusDetails.queriedBy || 'order-service'}</p>
                                    <p><strong>Status Check:</strong> {formatTimestamp(statusDetails.queryTimestamp)}</p>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="text-center py-12">
                            <p className="text-gray-600">No status details available</p>
                        </div>
                    )}
                </div>

                {/* Footer */}
                <div className="flex justify-between items-center p-6 border-t border-gray-200">
                    <button
                        onClick={fetchOrderStatus}
                        disabled={loading}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                    >
                        {loading ? 'Refreshing...' : 'Refresh Status'}
                    </button>
                    <button
                        onClick={onClose}
                        className="px-6 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
                    >
                        Close
                    </button>
                </div>
            </div>
        </div>
    );
}