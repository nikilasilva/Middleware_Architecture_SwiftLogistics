"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

const mockDriver = {
    email: "driver@swiftlogistics.lk",
    password: "driver123",
    name: "Nimal Perera",
    driverId: "DRIVER001",
};

export default function DriverLogin() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const router = useRouter();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError("");

        // Simulate API call delay
        await new Promise(resolve => setTimeout(resolve, 1000));

        if (!email || !password) {
            setError("Please enter both email and password.");
            setLoading(false);
            return;
        }

        if (email === mockDriver.email && password === mockDriver.password) {
            // Set cookies
            document.cookie = `driverSignedIn=true; path=/; max-age=86400`;
            document.cookie = `driverName=${mockDriver.name}; path=/; max-age=86400`;
            document.cookie = `driverId=${mockDriver.driverId}; path=/; max-age=86400`;

            router.push("/");
        } else {
            setError("Invalid email or password.");
        }
        setLoading(false);
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
            <div className="w-full max-w-md">
                <div className="bg-white rounded-lg shadow-lg overflow-hidden">
                    {/* Header */}
                    <div className="bg-black px-6 py-8 text-center">
                        <div className="h-12 w-12 bg-white rounded-full mx-auto mb-4 flex items-center justify-center">
                            <span className="text-black font-bold text-lg">🚚</span>
                        </div>
                        <h1 className="text-2xl font-bold text-white">SwiftLogistics</h1>
                        <p className="text-gray-300 mt-2">Driver Portal</p>
                    </div>

                    {/* Form */}
                    <form onSubmit={handleSubmit} className="p-6">
                        <h2 className="text-xl font-semibold mb-6 text-center text-gray-900">
                            Driver Login
                        </h2>

                        {error && (
                            <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
                                {error}
                            </div>
                        )}

                        <div className="mb-4">
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Email Address
                            </label>
                            <input
                                type="email"
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-black focus:border-transparent text-black"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="driver@swiftlogistics.lk"
                                required
                            />
                        </div>

                        <div className="mb-6">
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Password
                            </label>
                            <input
                                type="password"
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-black focus:border-transparent text-black"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="Enter your password"
                                required
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full bg-black text-white py-3 rounded-lg hover:bg-gray-900 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {loading ? "Signing in..." : "Sign In"}
                        </button>
                    </form>

                    {/* Demo Credentials */}
                    <div className="px-6 pb-6">
                        <div className="bg-gray-50 p-3 rounded-lg">
                            <p className="text-sm text-gray-600 mb-2"><strong>Demo Credentials:</strong></p>
                            <p className="text-sm text-gray-600">Email: driver@swiftlogistics.lk</p>
                            <p className="text-sm text-gray-600">Password: driver123</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}