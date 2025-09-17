"use client"

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';

const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const router = useRouter();

  // Mock user data
  const mockUser = {
    email: 'user@example.com',
    password: '123',
    clientId: 'CLIENT001'
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      setError('Please enter both email and password.');
      return;
    }
    if (email === mockUser.email && password === mockUser.password) {
      setError('');
      // Set a mock signed-in cookie (expires in 1 day)
      document.cookie = 'signedin=true; path=/; max-age=86400';
      document.cookie = `clientId=${mockUser.clientId}; path=/; max-age=${60*60*24*7}`;
      router.push('/'); // Redirect to home page
    } else {
      setError('Invalid email or password.');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-white">
      <div className="w-full max-w-md">
        {/* Header to match home page */}
        <header className="bg-black rounded-t-lg shadow-lg items-center">
          <div className="flex flex-col items-center px-6 py-5">
            <div className="h-8 w-8 bg-white rounded mr-3 flex items-center justify-center">
              <span className="text-black font-bold text-sm">SL</span>
            </div>
            <h1 className="text-2xl font-bold text-white text-center">SwiftLogistics</h1>
          </div>
        </header>
        <form
          onSubmit={handleSubmit}
          className="bg-white p-8 rounded-b-lg shadow-lg border-t-0"
        >
          <h2 className="text-xl font-bold mb-6 text-center text-black">Login to your account</h2>
          {error && <div className="mb-4 text-red-500">{error}</div>}
          <div className="mb-4">
            <label className="block mb-1 font-medium text-black">Email</label>
            <input
              type="email"
              className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-black"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="mb-6">
            <label className="block mb-1 font-medium text-black">Password</label>
            <input
              type="password"
              className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-black"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
            />
          </div>
          <button
            type="submit"
            className="w-full bg-black text-white py-2 rounded hover:bg-gray-900 transition-colors font-semibold"
          >
            Login
          </button>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
