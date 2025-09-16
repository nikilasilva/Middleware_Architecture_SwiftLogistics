import React from "react";



type StatusType = typeof statusSteps[number]["key"];

interface StatusStepperProps {
  status: StatusType;
}

const statusSteps = [
  { key: "pending", label: "Pending" },
  { key: "assigned", label: "Assigned" },
  { key: "in_transit", label: "In Transit" },
  { key: "delivered", label: "Delivered" },
  { key: "cancelled", label: "Cancelled" },
];

export default function StatusStepper({ status }: StatusStepperProps) {
  // Find the current step index
  const currentIdx = statusSteps.findIndex((s) => s.key === status);

  return (
    <div className="flex items-center w-full max-w-xs">
      {statusSteps.map((step, idx) => {
        // If cancelled, highlight only cancelled
        const isActive = status === "cancelled"
          ? step.key === "cancelled"
          : idx <= currentIdx && status !== "cancelled";
        const isLast = idx === statusSteps.length - 1;
        return (
          <React.Fragment key={step.key}>
            <div
              className={`flex flex-col items-center`}
              style={{ minWidth: 60 }}
            >
              <div
                className={`rounded-full w-6 h-6 flex items-center justify-center text-xs font-bold border-2 transition-colors
                  ${isActive ? (step.key === "cancelled" ? "bg-red-500 border-red-500 text-white" : "bg-blue-600 border-blue-600 text-white") : "bg-gray-200 border-gray-300 text-gray-500"}
                `}
              >
                {idx + 1}
              </div>
              <span className={`mt-1 text-xs ${isActive ? "text-black font-semibold" : "text-gray-400"}`}>{step.label}</span>
            </div>
            {!isLast && (
              <div
                className={`flex-1 h-1  rounded-full transition-colors duration-200 ${
                  isActive && status !== "cancelled" ? "bg-blue-600" : "bg-gray-300"
                }`}
                style={{ minWidth: 24 }}
              ></div>
            )}
          </React.Fragment>
        );
      })}
    </div>
  );
}
