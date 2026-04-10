import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { ToastProvider } from "./context/ToastContext";
import ProtectedRoute from "./routes/ProtectedRoute";

import Signup from "./pages/Signup";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import CreateRoom from "./pages/CreateRoom";
import RoomPage from "./pages/RoomPage";
import Subscription from "./pages/Subscription";
import PaymentSuccess from "./pages/PaymentSuccess";
import PaymentCancel from "./pages/PaymentCancel";

export default function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <Router>
          <Routes>
            <Route path="/signup" element={<Signup />} />
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
            <Route path="/create" element={<ProtectedRoute><CreateRoom /></ProtectedRoute>} />
            <Route path="/room/:id" element={<ProtectedRoute><RoomPage /></ProtectedRoute>} />
            <Route path="/subscription" element={<ProtectedRoute><Subscription /></ProtectedRoute>} />
            <Route path="/payment/success" element={<PaymentSuccess />} />  {/* ✅ no ProtectedRoute — Stripe redirects here without token */}
            <Route path="/payment/cancel" element={<PaymentCancel />} />    {/* ✅ same */}
          </Routes>
        </Router>
      </ToastProvider>
    </AuthProvider>
  );
}