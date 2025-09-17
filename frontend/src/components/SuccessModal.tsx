"use client";

interface SuccessModalProps {
    isOpen: boolean;
    title: string;
    message: string;
    buttonText?: string;
    onClose: () => void;
}

export default function SuccessModal({
    isOpen,
    title,
    message,
    buttonText = "OK",
    onClose,
}: SuccessModalProps) {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4">
                {/* Success Icon */}
                <div className="flex items-center justify-center w-12 h-12 mx-auto mb-4 bg-green-100 rounded-full">
                    <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                </div>

                {/* Header */}
                <div className="text-center mb-4">
                    <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
                </div>

                {/* Message */}
                <div className="text-center mb-6">
                    <p className="text-gray-600">{message}</p>
                </div>

                {/* Action Button */}
                <div className="flex justify-center">
                    <button
                        onClick={onClose}
                        className="px-6 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2"
                    >
                        {buttonText}
                    </button>
                </div>
            </div>
        </div>
    );
}