"use client";

import { useWSState } from "@/lib/use-ws-state";

export function WSStatus({ className = "" }: { className?: string }) {
  const state = useWSState();

  if (state === "DISCONNECTED") return null;

  const color = state === "CONNECTED" ? "bg-rank-pupil"
    : state === "CONNECTING" ? "bg-rank-master animate-pulse"
    : "bg-destructive animate-pulse";

  const label = state === "CONNECTED" ? "Live"
    : state === "CONNECTING" ? "Connecting"
    : "Reconnecting";

  return (
    <div className={`inline-flex items-center gap-1.5 text-xs text-muted-foreground ${className}`}>
      <span className={`h-2 w-2 rounded-full ${color}`} />
      <span>{label}</span>
    </div>
  );
}
