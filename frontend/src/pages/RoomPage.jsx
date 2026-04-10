import React, { useEffect, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { apiFetch } from "../services/api";
import { connectRoomSocket } from "../services/socket";
import { useAuth } from "../context/AuthContext";

import { useToast } from "../context/ToastContext";
import { parseError } from "../utils/parseError";

export default function RoomPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { currentUserId } = useAuth();
  const toast = useToast();

  const [messages, setMessages] = useState([]);
  const [memberCount, setMemberCount] = useState(0);
  const [input, setInput] = useState("");
  const [client, setClient] = useState(null);
  const [room, setRoom] = useState(null);
  const bottomRef = useRef(null);

  useEffect(() => {
    const loadMessages = async () => {
      try {
        const data = await apiFetch(`/rooms/${id}/messages?page=0&size=50`);
        setMessages(data.content.reverse());
      } catch (err) {
        toast.error(parseError(err, "Failed to load chat history."));
      }
    };

    const loadRoom = async () => {
      try {
        const data = await apiFetch(`/rooms/getRoom/${id}`);
        setRoom(data);
        setMemberCount(data.roomMemberCount);
      } catch (err) {
        toast.error(parseError(err, "Failed to load room details."));
      }
    };

    loadMessages();
    loadRoom();

    const stompClient = connectRoomSocket(
      id,
      (msg) => setMessages((prev) => [...prev, msg]),
      (update) => setMemberCount(update.count ?? update),
      (connectedClient) => setClient(connectedClient)
    );

    return () => {
      if (stompClient) stompClient.deactivate();
    };
  }, [id]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const isOwner = room?.ownerId === currentUserId;

  const sendMessage = () => {
    if (!client)          return toast.error("Not connected. Please wait.");
    if (!input.trim())    return;
    try {
      client.publish({
        destination: `/app/room/${id}/chat`,
        body: JSON.stringify({ content: input }),
      });
      setInput("");
    } catch {
      toast.error("Failed to send message. Please try again.");
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const leaveRoom = async () => {
    try {
      await apiFetch(`/rooms/leaveRoom/${id}`, { method: "POST" });
      navigate("/");
    } catch (err) {
      toast.error(parseError(err, "Failed to leave room."));
    }
  };

  const closeRoom = async () => {
    try {
      await apiFetch(`/rooms/closeRoom/${id}`, { method: "POST" });
      navigate("/");
    } catch (err) {
      toast.error(parseError(err, "Failed to close room."));
    }
  };

  return (
    <div className="room-layout">
      {/* Header */}
      <div className="room-header">
        <div className="room-header-left">
          <button className="back-btn" onClick={() => navigate("/")}>←</button>
          <div>
            <h2>{room?.label ?? `Room #${id}`}</h2>
            <span className="room-id-badge">#{id} · {room?.tag ?? "—"}</span>
          </div>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: "0.625rem" }}>
          <div className="member-count">
            <span className="member-dot" />
            {memberCount} / {room?.maxNumberOfMembers ?? "?"}
          </div>
          <div className="room-actions">
            {isOwner && (
              <button className="btn btn-amber" onClick={closeRoom}>Close</button>
            )}
            <button className="btn btn-danger" onClick={leaveRoom}>Leave</button>
          </div>
        </div>
      </div>

      {/* Messages */}
      <div className="chat-box">
        {messages.length === 0 && (
          <div style={{
            textAlign: "center", color: "var(--text-2)",
            fontSize: "0.75rem", marginTop: "2rem", letterSpacing: "0.06em"
          }}>
            No messages yet. Say something.
          </div>
        )}
        {messages.map((msg, index) => {
          const isOwn = msg.senderId === currentUserId;
          return (
            <div key={msg.id ?? index} className={`chat-message ${isOwn ? "own" : "other"}`}>
              <div className="chat-bubble">{msg.content}</div>
              <span className="chat-meta">
                {msg.createdAt
                  ? new Date(msg.createdAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
                  : ""}
              </span>
            </div>
          );
        })}
        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <div className="chat-input-area">
        <div className="chat-input-wrap">
          <input
            className="chat-input"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Type a message..."
          />
        </div>
        <button className="send-btn" onClick={sendMessage}>↑</button>
      </div>
    </div>
  );
}