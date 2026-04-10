import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { apiFetch } from "../services/api";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { parseError } from "../utils/parseError";

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const handleLogin = async () => {
    if (!username.trim()) return toast.error("Username is required.");
    if (!password.trim()) return toast.error("Password is required.");

    setLoading(true);
    try {
      const res = await apiFetch("/auth/login", {
        method: "POST",
        body: JSON.stringify({ username, password }),
      });
      login(res.token);
      navigate("/");
    } catch (err) {
      toast.error(parseError(err, "Invalid username or password."));
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => { if (e.key === "Enter") handleLogin(); };

  return (
    <div className="page-center">
      <div className="auth-container">
        <div className="auth-logo">
          <div className="auth-logo-mark">⬡</div>
        </div>
        <h2>Welcome back</h2>
        <p className="auth-subtitle">sign in to your account</p>

        <div className="auth-fields">
          <div className="field-group">
            <label className="field-label">Username</label>
            <input
              placeholder="your_handle"
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
              autoComplete="current-password"
            />
          </div>
        </div>

        <button className="btn btn-primary" onClick={handleLogin} disabled={loading}>
          {loading ? "Signing in..." : "Sign In →"}
        </button>

        <p className="auth-footer">
          No account? <Link to="/signup">Create one</Link>
        </p>
      </div>
    </div>
  );
}