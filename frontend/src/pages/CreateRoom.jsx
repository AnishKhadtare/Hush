import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../services/api";
import useLocation from "../hooks/useLocation";
import { useToast } from "../context/ToastContext";
import { parseError } from "../utils/parseError";

const FREE_LIMITS = {
  maxMembersInRoom: 10,
  maxRoomDurationInMinutes: 60,
  planName: "Free",
};

export default function CreateRoom() {
  const navigate = useNavigate();
  const coords = useLocation();
  const toast = useToast();
  const [loading, setLoading] = useState(false);
  const [planLimits, setPlanLimits] = useState(FREE_LIMITS);
  const [planLoaded, setPlanLoaded] = useState(false);

  const [form, setForm] = useState({
    label: "",
    description: "",
    maxNumberOfMembers: 10,
    roomCode: "",
    tag: "",
    roomDuration: 1,
  });

  useEffect(() => {
    apiFetch("/subscription/getCurrentSubscription")
      .then((sub) => {
        if (sub?.maxMembersInRoom) {
          const limits = {
            maxMembersInRoom: sub.maxMembersInRoom,
            maxRoomDurationInMinutes: sub.maxRoomDurationInMinutes,
            planName: sub.planName ?? "Paid",
          };
          setPlanLimits(limits);
          setForm((prev) => ({
            ...prev,
            maxNumberOfMembers: limits.maxMembersInRoom,
            roomDuration: limits.maxRoomDurationInMinutes / 60,
          }));
        }
      })
      .catch(() => {}) // free tier — keep defaults
      .finally(() => setPlanLoaded(true));
  }, []);

  const maxDurationHrs = planLimits.maxRoomDurationInMinutes / 60;

  const set = (key) => (e) => {
    let val = e.target.value;
    if (key === "maxNumberOfMembers" && Number(val) > planLimits.maxMembersInRoom) {
      toast.error(`Your ${planLimits.planName} plan allows max ${planLimits.maxMembersInRoom} members.`);
      val = planLimits.maxMembersInRoom;
    }
    if (key === "roomDuration" && Number(val) > maxDurationHrs) {
      toast.error(`Your ${planLimits.planName} plan allows max ${maxDurationHrs}h duration.`);
      val = maxDurationHrs;
    }
    setForm({ ...form, [key]: val });
  };

  const handleCreate = async () => {
    if (!coords)              return toast.error("Location not available yet.");
    if (!form.label.trim())   return toast.error("Room label is required.");
    const members  = Number(form.maxNumberOfMembers);
    const duration = Number(form.roomDuration);
    if (members < 2)                           return toast.error("Room needs at least 2 members.");
    if (members > planLimits.maxMembersInRoom) return toast.error(`Max ${planLimits.maxMembersInRoom} members on your plan.`);
    if (duration < 1)                          return toast.error("Minimum duration is 1 hour.");
    if (duration > maxDurationHrs)             return toast.error(`Max ${maxDurationHrs}h on your plan.`);

    setLoading(true);
    try {
      const response = await apiFetch("/rooms/create", {
        method: "POST",
        body: JSON.stringify({
          ...form,
          maxNumberOfMembers: members,
          roomDuration: duration,
          longitude: coords.longitude,
          latitude: coords.latitude,
        }),
      });
      toast.success("Room created!");
      navigate(`/room/${response.id}`);
    } catch (err) {
      toast.error(parseError(err, "Failed to create room. Please try again."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-center">
      <div className="create-room-container">
        <div className="create-room-header">
          <button className="back-btn" onClick={() => navigate("/")}>←</button>
          <div>
            <h2>Create Room</h2>
            <p className="text-muted">{coords ? "location ready ✓" : "acquiring location..."}</p>
          </div>
        </div>

        <div className="create-room-body">
          {planLoaded && (
            <div className="plan-limit-banner">
              <span className="plan-limit-banner-label">{planLimits.planName} plan</span>
              <span className="plan-limit-banner-info">
                {planLimits.maxMembersInRoom} members · {maxDurationHrs}h max
              </span>
              <button
                className="plan-limit-banner-upgrade"
                onClick={() => navigate("/subscription")}
              >
                {planLimits.planName === "Free" ? "Upgrade ↗" : "Manage ↗"}
              </button>
            </div>
          )}

          <div className="field-group">
            <label className="field-label">Room Label *</label>
            <input placeholder="e.g. Rooftop Vibes" onChange={set("label")} value={form.label} />
          </div>

          <div className="field-group">
            <label className="field-label">Description</label>
            <input placeholder="What's happening here?" onChange={set("description")} value={form.description} />
          </div>

          <div className="fields-row">
            <div className="field-group">
              <label className="field-label">
                Max Members
                <span className="field-limit-hint"> (max {planLimits.maxMembersInRoom})</span>
              </label>
              <input
                type="number" min={2} max={planLimits.maxMembersInRoom}
                value={form.maxNumberOfMembers} onChange={set("maxNumberOfMembers")}
              />
            </div>
            <div className="field-group">
              <label className="field-label">
                Duration (hrs)
                <span className="field-limit-hint"> (max {maxDurationHrs}h)</span>
              </label>
              <input
                type="number" step="1" min={1} max={maxDurationHrs}
                value={form.roomDuration} onChange={set("roomDuration")}
              />
            </div>
          </div>

          <div className="fields-row">
            <div className="field-group">
              <label className="field-label">Tag</label>
              <input placeholder="music, study, chill..." onChange={set("tag")} value={form.tag} />
            </div>
            <div className="field-group">
              <label className="field-label">Room Code (optional)</label>
              <input placeholder="private code" onChange={set("roomCode")} value={form.roomCode} />
            </div>
          </div>
        </div>

        <div className="create-room-footer">
          <button
            className="btn btn-primary"
            onClick={handleCreate}
            disabled={loading || !coords || !planLoaded}
          >
            {loading ? "Creating..." : "Launch Room →"}
          </button>
        </div>
      </div>
    </div>
  );
}