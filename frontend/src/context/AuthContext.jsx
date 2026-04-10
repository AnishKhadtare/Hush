import React, { createContext, useContext, useState } from "react";

const AuthContext = createContext();

// ✅ decode JWT payload without any library
function parseJwt(token) {
    try {
        const base64 = token.split(".")[1];
        return JSON.parse(atob(base64));
    } catch {
        return null;
    }
}

export function AuthProvider({ children }) {
    const [token, setToken] = useState(localStorage.getItem("token"));

    const login = (jwt) => {
        localStorage.setItem("token", jwt);
        setToken(jwt);
    };

    const logout = () => {
        localStorage.removeItem("token");
        setToken(null);
    };

    // ✅ expose currentUserId decoded from JWT
    const currentUserId = token ? Number(parseJwt(token)?.userId) : null;

    return (
        <AuthContext.Provider value={{ token, login, logout, currentUserId }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}