import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

//const WS_BASE = process.env.REACT_APP_WS_URL;
const WS_BASE = import.meta.env.VITE_WS_URL;

export function connectRoomSocket(roomId, onMessage, onMemberUpdate, onConnected) {
    const client = new Client({
        webSocketFactory: () => new SockJS(WS_BASE),
        reconnectDelay: 5000,
        connectHeaders: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        onConnect: () => {
            client.subscribe(`/topic/room/${roomId}/chat`, (msg) => {
                onMessage(JSON.parse(msg.body));
            });
            client.subscribe(`/topic/room/${roomId}/memberCount`, (msg) => {
                onMemberUpdate(JSON.parse(msg.body));
            });

            // ✅ notify RoomPage that connection + subscriptions are ready
            if (onConnected) onConnected(client);
        },
        onStompError: (frame) => console.error("STOMP error:", frame),
        onDisconnect: () => console.log("Disconnected"),
        onWebSocketError: (error) => console.error("WebSocket error:", error),
    });

    client.activate();
    return client;
}