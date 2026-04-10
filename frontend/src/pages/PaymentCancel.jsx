import React from "react";
import { useNavigate } from "react-router-dom";

export default function PaymentCancel() {
  const navigate = useNavigate();

  return (
    <div className="page-center">
      <div className="auth-container" style={{ textAlign: "center" }}>
        <div style={{ fontSize: "2.5rem", marginBottom: "1rem" }}>✕</div>
        <h2>Payment Cancelled</h2>
        <p className="auth-subtitle" style={{ marginTop: "0.5rem" }}>
          No charges were made
        </p>
        <div style={{ display: "flex", gap: "0.75rem", marginTop: "1.5rem" }}>
          <button className="btn btn-secondary" onClick={() => navigate("/")}>
            Back to Dashboard
          </button>
          <button className="btn btn-primary" onClick={() => navigate("/subscription")}>
            View Plans →
          </button>
        </div>
      </div>
    </div>
  );
}