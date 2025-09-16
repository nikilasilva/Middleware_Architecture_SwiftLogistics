"use client";

interface CancelledOrderModalProps {
    isOpen: boolean;
    orderNumber: string;
    onClose: () => void;
}

export default function CancelledOrderModal({
    isOpen,
    orderNumber,
    onClose,
}: CancelledOrderModalProps) {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4">
                {/* Cancelled Icon */}
                <div className="flex items-center justify-center w-12 h-12 mx-auto mb-4 bg-red-100 rounded-full">
                    <svg className="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </div>

                {/* Header */}
                <div className="text-center mb-4">
                    <h3 className="text-lg font-semibold text-gray-900">Order Cancelled</h3>
                </div>

                {/* Message */}
                <div className="text-center mb-6">
                    <p className="text-gray-600">
                        Order <strong>{orderNumber}</strong> has been cancelled.
                        Status details are no longer available for cancelled orders.
                    </p>
                </div>

                {/* Action Button */}
                <div className="flex justify-center">
                    <button
                        onClick={onClose}
                        className="px-6 py-2 bg-gray-600 hover:bg-gray-700 text-white rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
                    >
                        OK
                    </button>
                </div>
            </div>
        </div>
    );
}