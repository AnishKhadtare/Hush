import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../services/api";
import useLocation from "../hooks/useLocation";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { parseError } from "../utils/parseError";

export default function Dashboard() {
  const coords = useLocation();
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [planName, setPlanName] = useState("Free");
  const navigate = useNavigate();
  const { logout } = useAuth();
  const toast = useToast();

  useEffect(() => {
    // Load current plan name for header badge
    apiFetch("/subscription/getCurrentSubscription")
      .then((sub) => { if (sub?.planName) setPlanName(sub.planName); })
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!coords) return;
    setLoading(true);
    apiFetch(`/rooms/getNearByRooms?longitude=${coords.longitude}&latitude=${coords.latitude}`)
      .then(setRooms)
      .catch((err) => toast.error(parseError(err, "Failed to load nearby rooms.")))
      .finally(() => setLoading(false));
  }, [coords]);

  const joinRoom = async (roomId) => {
    try {
      await apiFetch(`/rooms/join/${roomId}`, { method: "POST" });
      navigate(`/room/${roomId}`);
    } catch (err) {
      toast.error(parseError(err, "Could not join room. It may be full or closed."));
    }
  };

  return (
    <div className="page-full">
      <div className="dashboard-header">
        <div className="dashboard-brand">
          <div className="brand-mark">⬡</div>
          <div>
            <div className="brand-name">Nexus</div>
            <span className="brand-tag">nearby rooms</span>
          </div>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", flexWrap: "wrap" }}>
          <div className="dashboard-status">
            <span className="status-dot" />
            {coords ? "located" : "locating..."}
          </div>

          {/* ✅ Plan badge → navigates to subscription */}
          <button
            className="plan-dash-badge"
            onClick={() => navigate("/subscription")}
            title="Manage subscription"
          >
            {planName}
          </button>

          <button className="btn btn-secondary" onClick={() => navigate("/create")}>
            + New Room
          </button>
          <button className="btn btn-secondary btn-icon" onClick={logout} title="Logout">
            ⎋
          </button>
        </div>
      </div>

      <div className="section-header">
        <span className="section-title">
          {loading
            ? "scanning area"
            : `${rooms.length} room${rooms.length !== 1 ? "s" : ""} nearby`}
        </span>
      </div>

      <div className="rooms-grid">
        {loading && (
          <div className="empty-state">
            <div className="empty-state-icon">◌</div>
            <div>Scanning nearby rooms...</div>
          </div>
        )}

        {!loading && rooms.length === 0 && (
          <div className="empty-state">
            <div className="empty-state-icon">⬡</div>
            <div>No rooms nearby</div>
            <div style={{ marginTop: "0.5rem", fontSize: "0.7rem" }}>
              Be the first —{" "}
              <span
                style={{ color: "var(--cyan)", cursor: "pointer" }}
                onClick={() => navigate("/create")}
              >
                create a room
              </span>
            </div>
          </div>
        )}

        {rooms.map((room, i) => (
          <div
            key={room.id}
            className="room-card"
            style={{ animationDelay: `${i * 0.06}s` }}
          >
            <div className="room-card-top">
              <h3>{room.label}</h3>
              {room.tag && <span className="room-tag">{room.tag}</span>}
            </div>
            {room.description && <p>{room.description}</p>}
            <div className="room-card-meta">
              <span className="room-meta-item">
                <span className="member-dot" />
                {room.roomMemberCount ?? 0} / {room.maxNumberOfMembers}
              </span>
              <span className="room-meta-item"># {room.id}</span>
            </div>
            <button className="btn btn-secondary" onClick={() => joinRoom(room.id)}>
              Join Room →
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}