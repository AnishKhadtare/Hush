import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../services/api";
import { useToast } from "../context/ToastContext";
import { parseError } from "../utils/parseError";

const FREE_TIER = {
  planName: "Free",
  price: 0,
  maxMembersInRoom: 10,
  maxRoomDurationInMinutes: 60,
};

const PLANS = [
  {
    id: null,
    planName: "Free",
    price: 0,
    priceLabel: "₹0",
    periodLabel: "forever",
    maxMembersInRoom: 10,
    maxRoomDurationInMinutes: 60,
    features: [
      "Up to 10 members per room",
      "1 hour room duration",
      "5 km discovery radius",
      "Basic chat",
    ],
    highlight: false,
    badge: null,
  },
  {
    id: 1, // planId in your DB
    planName: "Basic",
    price: 199,
    priceLabel: "₹199",
    periodLabel: "per month",
    maxMembersInRoom: 15,
    maxRoomDurationInMinutes: 120,
    features: [
      "Up to 15 members per room",
      "2 hours room duration",
      "5 km discovery radius",
      "Priority chat delivery",
    ],
    highlight: false,
    badge: "POPULAR",
  },
  {
    id: 2, // planId in your DB
    planName: "Premium",
    price: 299,
    priceLabel: "₹299",
    periodLabel: "per month",
    maxMembersInRoom: 25,
    maxRoomDurationInMinutes: 240,
    features: [
      "Up to 25 members per room",
      "4 hours room duration",
      "5 km discovery radius",
      "Priority chat delivery",
      "Longer room history",
    ],
    highlight: true,
    badge: "BEST VALUE",
  },
];

export default function Subscription() {
  const navigate = useNavigate();
  const toast = useToast();
  const [currentSub, setCurrentSub] = useState(null);
  const [loading, setLoading] = useState(true);
  const [checkingOut, setCheckingOut] = useState(null);

  useEffect(() => {
    apiFetch("/subscription/getCurrentSubscription")
      .then(setCurrentSub)
      .catch(() => setCurrentSub(null)) // null = free tier
      .finally(() => setLoading(false));
  }, []);

  const handleCheckout = async (planId) => {
    setCheckingOut(planId);
    try {
      const res = await apiFetch("/payments/checkout", {
        method: "POST",
        body: JSON.stringify({ planId }),
      });
      window.location.href = res.message;  // ✅ extract URL from {message: "https://..."}
    } catch (err) {
      toast.error(parseError(err, "Failed to initiate checkout. Please try again."));
    } finally {
      setCheckingOut(null);
    }
  };

  const activePlanName = currentSub?.planName ?? "Free";

  return (
    <div className="page-full">
      {/* Header */}
      <div className="dashboard-header">
        <div className="dashboard-brand">
          <div className="brand-mark">⬡</div>
          <div>
            <div className="brand-name">Nexus</div>
            <span className="brand-tag">subscription</span>
          </div>
        </div>
        <button className="btn btn-secondary" onClick={() => navigate("/")}>
          ← Back
        </button>
      </div>

      {/* Hero */}
      <div className="sub-hero">
        <h1 className="sub-title">Choose your plan</h1>
        <p className="sub-subtitle">
          {loading
            ? "Loading your plan..."
            : `Currently on · `}
          {!loading && (
            <span className="sub-current-badge">{activePlanName}</span>
          )}
        </p>
      </div>

      {/* Plan cards */}
      <div className="plans-grid">
        {PLANS.map((plan, i) => {
          const isActive = plan.planName === activePlanName;
          const isFree = plan.id === null;

          return (
            <div
              key={plan.planName}
              className={`plan-card ${plan.highlight ? "plan-card--highlight" : ""} ${isActive ? "plan-card--active" : ""}`}
              style={{ animationDelay: `${i * 0.1}s` }}
            >
              {plan.badge && (
                <div className={`plan-badge ${plan.highlight ? "plan-badge--amber" : ""}`}>
                  {plan.badge}
                </div>
              )}

              <div className="plan-header">
                <div className="plan-name">{plan.planName}</div>
                <div className="plan-price">
                  <span className="plan-price-amount">{plan.priceLabel}</span>
                  <span className="plan-price-period">/{plan.periodLabel}</span>
                </div>
              </div>

              <div className="plan-divider" />

              <ul className="plan-features">
                {plan.features.map((f) => (
                  <li key={f} className="plan-feature">
                    <span className="plan-feature-check">✓</span>
                    {f}
                  </li>
                ))}
              </ul>

              <div className="plan-limits">
                <div className="plan-limit-item">
                  <span className="plan-limit-label">Members</span>
                  <span className="plan-limit-value">{plan.maxMembersInRoom}</span>
                </div>
                <div className="plan-limit-item">
                  <span className="plan-limit-label">Duration</span>
                  <span className="plan-limit-value">
                    {plan.maxRoomDurationInMinutes / 60}h
                  </span>
                </div>
              </div>

              {isActive ? (
                <button className="btn btn-secondary" disabled>
                  ✓ Current Plan
                </button>
              ) : isFree ? (
                <button className="btn btn-secondary" disabled>
                  Free Forever
                </button>
              ) : (
                <button
                  className={`btn ${plan.highlight ? "btn-amber" : "btn-primary"}`}
                  onClick={() => handleCheckout(plan.id)}
                  disabled={checkingOut === plan.id}
                >
                  {checkingOut === plan.id ? "Redirecting..." : `Upgrade to ${plan.planName} →`}
                </button>
              )}
            </div>
          );
        })}
      </div>

      <p className="sub-note">
        Payments are securely processed by Stripe. Cancel anytime from your Stripe dashboard.
      </p>
    </div>
  );
}