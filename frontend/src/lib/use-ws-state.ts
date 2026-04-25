"use client";

import { useEffect, useState } from "react";
import { getMatchWebSocket, WSConnectionState } from "./match-ws";

export function useWSState(): WSConnectionState {
  const [state, setState] = useState<WSConnectionState>("DISCONNECTED");

  useEffect(() => {
    const ws = getMatchWebSocket();
    setState(ws.getState());
    return ws.onStateChange(setState);
  }, []);

  return state;
}
