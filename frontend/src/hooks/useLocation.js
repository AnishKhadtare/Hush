import { useEffect, useState } from "react";

export default function useLocation() {
  const [coords, setCoords] = useState(null);

  useEffect(() => {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setCoords({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        });
      },
      (error) => {
        console.error("Location error:", error);
      },
      { enableHighAccuracy: true }
    );
  }, []);

  return coords;
}
