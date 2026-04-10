import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { apiFetch } from "../services/api";
import useLocation from "../hooks/useLocation";
import { useToast } from "../context/ToastContext";
import { parseError } from "../utils/parseError";

export default function Signup() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const coords = useLocation();
  const toast = useToast();
  const navigate = useNavigate();

  const handleSignup = async () => {
    if (!coords)            return toast.error("Location not available yet. Please wait.");
    if (!username.trim())   return toast.error("Username is required.");
    if (password.length < 4) return toast.error("Password must be at least 4 characters.");

    setLoading(true);
    try {
      await apiFetch("/auth/signUp", {
        method: "POST",
        body: JSON.stringify({
          username,
          password,
          longitude: coords.longitude,
          latitude: coords.latitude,
        }),
      });
      toast.success("Account created! Please sign in.");
      navigate("/login");
    } catch (err) {
      toast.error(parseError(err, "Signup failed. Username may already be taken."));
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => { if (e.key === "Enter") handleSignup(); };

  return (
    <div className="page-center">
      <div className="auth-container">
        <div className="auth-logo">
          <div className="auth-logo-mark">⬡</div>
        </div>
        <h2>Create account</h2>
        <p className="auth-subtitle">
          {coords ? "location acquired ✓" : "acquiring location..."}
        </p>

        <div className="auth-fields">
          <div className="field-group">
            <label className="field-label">Username</label>
            <input
              placeholder="choose_a_handle"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              onKeyDown={handleKeyDown}
              autoComplete="username"
            />
          </div>
          <div className="field-group">
            <label className="field-label">Password</label>
            <input
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onKeyDown={handleKeyDown}
              autoComplete="new-password"
            />
          </div>
        </div>

        <button
          className="btn btn-primary"
          onClick={handleSignup}
          disabled={loading || !coords}
        >
          {loading ? "Creating..." : "Create Account →"}
        </button>

        <p className="auth-footer">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}