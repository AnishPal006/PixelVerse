import { useState, useEffect, useRef } from "react";
import SockJS from "sockjs-client";
import { Stomp } from "@stomp/stompjs";
import axios from "axios";
import "./App.css";

const SOCKET_URL = "http://localhost:8080/ws-pixel";
const API_URL = "http://localhost:8080/api/pixels";
const GRID_SIZE = 50;
const PRESET_COLORS = [
  "#FF0055",
  "#00FFFF",
  "#39FF14",
  "#FFE700",
  "#FFFFFF",
  "#000000",
  "#9D00FF",
  "#FF6600",
];

function App() {
  const [grid, setGrid] = useState({});
  const [selectedColor, setSelectedColor] = useState("#00FFFF");
  const [commentary, setCommentary] = useState("Initializing AI Link...");
  const [history, setHistory] = useState([]);
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
      client.subscribe("/topic/board", (message) => {
        const update = JSON.parse(message.body);
        if (update.x === -1) {
          setGrid({});
          setHistory([]);
        } else {
          setGrid((prev) => ({
            ...prev,
            [`${update.x}-${update.y}`]: update.color,
          }));
        }
      });

      client.subscribe("/topic/commentary", (message) => {
        setCommentary(message.body);
      });
    });

    stompClientRef.current = client;
    return () => {
      if (client) client.disconnect();
    };
  }, []);

  const handlePixelClick = (x, y) => {
    const key = `${x}-${y}`;
    const prevColor = grid[key] || "#1a1a1a";
    setHistory((prev) => [...prev, { x, y, color: prevColor }]);
    setGrid((prev) => ({ ...prev, [key]: selectedColor }));
    axios.post(API_URL, {
      x,
      y,
      color: selectedColor,
      userId: "user-" + Math.floor(Math.random() * 999),
    });
  };

  const handleUndo = () => {
    if (history.length === 0) return;
    const lastMove = history[history.length - 1];
    setHistory((prev) => prev.slice(0, -1));
    axios.post(API_URL, {
      x: lastMove.x,
      y: lastMove.y,
      color: lastMove.color,
      userId: "UNDO-ACTION",
    });
  };

  const handleClearBoard = () => {
    if (
      confirm(
        "⚠ WARNING: This will wipe the entire board for all users. Continue?"
      )
    ) {
      axios.delete(API_URL);
    }
  };

  return (
    <div className="app-container">
      <nav className="navbar">
        <div className="logo-section">
          <h1 className="brand-title">
            PIXEL <span className="highlight">VERSE</span>
          </h1>
        </div>
        <div className="live-badge">● LIVE CONNECTION</div>
      </nav>

      <div className="dashboard">
        {/* 🟢 COLUMN 1: AI ANALYST (LEFT) */}
        <div className="side-panel ai-panel">
          <h3>GEMINI ANALYST</h3>
          <div className="ai-terminal">
            <div className="terminal-header">
              <span className="blink">●</span> RECIEVING SIGNAL...
            </div>
            <div className="terminal-body">
              <span className="ai-icon">🤖</span>
              <p className="ai-message">{commentary}</p>
            </div>
          </div>
        </div>

        {/* 🟡 COLUMN 2: CANVAS (CENTER) */}
        <div className="canvas-container">
          <div
            className="pixel-grid"
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
                  style={{ backgroundColor: grid[key] || "#1a1a1a" }}
                  onMouseDown={() => handlePixelClick(x, y)}
                />
              );
            })}
          </div>
        </div>

        {/* 🔴 COLUMN 3: CONTROLS (RIGHT) */}
        <div className="side-panel control-panel">
          <div className="panel-section">
            <h3>PALETTE</h3>
            <div className="palette-grid">
              {PRESET_COLORS.map((c) => (
                <div
                  key={c}
                  className={`color-swatch ${
                    selectedColor === c ? "selected" : ""
                  }`}
                  style={{ backgroundColor: c }}
                  onClick={() => setSelectedColor(c)}
                />
              ))}
              <label className="custom-color-label">
                <input
                  type="color"
                  value={selectedColor}
                  onChange={(e) => setSelectedColor(e.target.value)}
                />
                <span>PICK</span>
              </label>
            </div>
          </div>

          <div className="panel-section actions">
            <h3>ACTIONS</h3>
            <button
              className="btn btn-undo"
              onClick={handleUndo}
              disabled={history.length === 0}
            >
              ↩ UNDO
            </button>
            <button className="btn btn-clear" onClick={handleClearBoard}>
              ☢ WIPE
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
