import { useState, useEffect, useRef } from "react";
import SockJS from "sockjs-client";
import { Stomp } from "@stomp/stompjs";
import axios from "axios";
import "./App.css";

const SOCKET_URL = "http://localhost:8080/ws-pixel";
const API_URL = "http://localhost:8080/api/pixels";
const GRID_SIZE = 50;

function App() {
  const [grid, setGrid] = useState({});
  const [selectedColor, setSelectedColor] = useState("#FF0000");
  const [commentary, setCommentary] = useState("Waiting for AI analysis..."); // <--- NEW STATE
  const stompClientRef = useRef(null);

  useEffect(() => {
    axios.get(API_URL).then((res) => {
      const initialMap = {};
      res.data.forEach((p) => {
        initialMap[`${p.x}-${p.y}`] = p.color;
      });
      setGrid(initialMap);
    });

    const socket = new SockJS(SOCKET_URL);
    const client = Stomp.over(socket);
    client.debug = () => {};

    client.connect({}, () => {
      // 1. Subscribe to Board Updates
      client.subscribe("/topic/board", (message) => {
        const update = JSON.parse(message.body);
        setGrid((prev) => ({
          ...prev,
          [`${update.x}-${update.y}`]: update.color,
        }));
      });

      // 2. Subscribe to AI Commentary (NEW)
      client.subscribe("/topic/commentary", (message) => {
        setCommentary(message.body); // Update the text
      });
    });

    stompClientRef.current = client;

    return () => {
      if (client) client.disconnect();
    };
  }, []);

  const handlePixelClick = (x, y) => {
    setGrid((prev) => ({ ...prev, [`${x}-${y}`]: selectedColor }));
    axios.post(API_URL, {
      x,
      y,
      color: selectedColor,
      userId: "user-" + Math.floor(Math.random() * 1000),
    });
  };

  return (
    <div className="app-container">
      <h1>PixelVerse Live 🎨</h1>

      {/* NEW: AI Commentary Box */}
      <div className="ai-commentary">
        🤖 <strong>AI Analyst:</strong> {commentary}
      </div>

      <div className="controls">
        <input
          type="color"
          value={selectedColor}
          onChange={(e) => setSelectedColor(e.target.value)}
          className="color-picker"
        />
        <span>Pick a Color</span>
      </div>

      <div
        className="grid"
        style={{ gridTemplateColumns: `repeat(${GRID_SIZE}, 1fr)` }}
      >
        {Array.from({ length: GRID_SIZE * GRID_SIZE }).map((_, i) => {
          const x = Math.floor(i / GRID_SIZE);
          const y = i % GRID_SIZE;
          const key = `${x}-${y}`;
          return (
            <div
              key={key}
              className="pixel"
              style={{ backgroundColor: grid[key] || "#ffffff" }}
              onMouseDown={() => handlePixelClick(x, y)}
            />
          );
        })}
      </div>
    </div>
  );
}

export default App;
