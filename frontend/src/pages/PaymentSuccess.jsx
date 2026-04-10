import React, { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

export default function PaymentSuccess() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const sessionId = searchParams.get("session_id");

  useEffect(() => {
    const timer = setTimeout(() => navigate("/"), 4000);
    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="page-center">
      <div className="auth-container" style={{ textAlign: "center" }}>
        <div style={{ fontSize: "2.5rem", marginBottom: "1rem" }}>✓</div>
        <h2>Payment Successful</h2>
        <p className="auth-subtitle" style={{ marginTop: "0.5rem" }}>
          Your subscription is now active
        </p>
        <p className="text-muted" style={{ fontSize: "0.72rem", marginTop: "1rem" }}>
          Redirecting you home in a few seconds...
        </p>
        <button
          className="btn btn-primary"
          style={{ marginTop: "1.5rem" }}
          onClick={() => navigate("/")}
        >
          Go to Dashboard →
        </button>
      </div>
    </div>
  );
}