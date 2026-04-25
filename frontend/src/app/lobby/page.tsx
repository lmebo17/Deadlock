"use client";

import { useEffect, useState, useRef } from "react";
import { useRouter } from "next/navigation";
import { Navbar } from "@/components/Navbar";
import { useAuth } from "@/lib/auth";
import { Button } from "@/components/ui/button";
import { getMatchWebSocket, MatchEvent } from "@/lib/match-ws";
import { getActiveMatch } from "@/lib/api";

function getRankColor(eloRating: number): string {
  if (eloRating < 1200) return "text-rank-newbie";
  if (eloRating < 1400) return "text-rank-pupil";
  if (eloRating < 1600) return "text-rank-specialist";
  if (eloRating < 1900) return "text-rank-expert";
  if (eloRating < 2100) return "text-rank-candidate-master";
  if (eloRating < 2400) return "text-rank-master";
  return "text-rank-grandmaster";
}

const TIME_CONTROLS = [
  { value: "BLITZ", label: "Blitz", duration: "5 min" },
  { value: "RAPID", label: "Rapid", duration: "15 min" },
  { value: "CLASSICAL", label: "Classical", duration: "30 min" },
];

const DIFFICULTIES = ["ANY", "BEGINNER", "EASY", "MEDIUM", "HARD", "EXPERT"];

export default function LobbyPage() {
  const { user } = useAuth();
  const router = useRouter();
  const [timeControl, setTimeControl] = useState("RAPID");
  const [difficulty, setDifficulty] = useState("ANY");
  const [searching, setSearching] = useState(false);
  const [waitTime, setWaitTime] = useState(0);
  const waitTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    getActiveMatch().then((m) => {
      if (m) router.push(`/match/${m.id}`);
    }).catch(() => {});
  }, [router]);

  useEffect(() => {
    return () => {
      if (waitTimerRef.current) clearInterval(waitTimerRef.current);
    };
  }, []);

  const handleFindMatch = async () => {
    if (!user) return;
    const ws = getMatchWebSocket();
    await ws.connect();

    ws.onPersonalEvent((event: MatchEvent) => {
      if (event.type === "MATCH_FOUND" && event.matchId) {
        if (waitTimerRef.current) clearInterval(waitTimerRef.current);
        setSearching(false);
        router.push(`/match/${event.matchId}`);
      }
    });

    ws.joinQueue(timeControl, difficulty);
    setSearching(true);
    setWaitTime(0);
    waitTimerRef.current = setInterval(() => setWaitTime((t) => t + 1), 1000);
  };

  const handleCancel = () => {
    const ws = getMatchWebSocket();
    ws.leaveQueue();
    setSearching(false);
    if (waitTimerRef.current) clearInterval(waitTimerRef.current);
  };

  const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, "0")}`;
  };

  const eloRange = waitTime < 30 ? 200 : waitTime < 60 ? 400 : waitTime < 90 ? 600 : 800;

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 pt-24 pb-16">
        <div className="mx-auto max-w-md text-center">
          <h1 className="mb-2 text-4xl font-bold">
            Battle <span className="text-primary">Arena</span>
          </h1>
          <p className="mb-10 text-muted-foreground">Find an opponent and duel in real-time</p>

          {user && (
            <div className="mb-10 rounded-xl border border-border bg-card p-6">
              <div className="flex items-center justify-center gap-4">
                {user.avatarUrl && (
                  <img src={user.avatarUrl} alt="" className="h-14 w-14 rounded-full" />
                )}
                <div className="text-left">
                  <div className="font-semibold">{user.username}</div>
                  <span className={`inline-flex items-center rounded-md border border-border bg-secondary px-2 py-0.5 font-mono text-sm font-semibold ${getRankColor(user.eloRating)}`}>
                    {user.eloRating}
                  </span>
                </div>
              </div>
            </div>
          )}

          {!searching ? (
            <>
              <div className="mb-6">
                <h3 className="mb-3 text-sm font-medium text-muted-foreground uppercase tracking-wider">Time Control</h3>
                <div className="flex justify-center gap-3">
                  {TIME_CONTROLS.map((tc) => (
                    <button
                      key={tc.value}
                      onClick={() => setTimeControl(tc.value)}
                      className={`rounded-xl border px-6 py-3 transition-all ${
                        timeControl === tc.value
                          ? "border-primary bg-primary/10 text-primary"
                          : "border-border bg-card text-muted-foreground hover:border-primary/40"
                      }`}
                    >
                      <div className="font-semibold">{tc.label}</div>
                      <div className="text-xs opacity-60">{tc.duration}</div>
                    </button>
                  ))}
                </div>
              </div>

              <div className="mb-8">
                <h3 className="mb-3 text-sm font-medium text-muted-foreground uppercase tracking-wider">Difficulty</h3>
                <div className="flex justify-center gap-2 flex-wrap">
                  {DIFFICULTIES.map((d) => (
                    <button
                      key={d}
                      onClick={() => setDifficulty(d)}
                      className={`rounded-lg border px-4 py-2 text-sm font-medium transition-all ${
                        difficulty === d
                          ? "border-primary bg-primary/10 text-primary"
                          : "border-border text-muted-foreground hover:text-foreground"
                      }`}
                    >
                      {d.charAt(0) + d.slice(1).toLowerCase()}
                    </button>
                  ))}
                </div>
              </div>

              <Button size="lg" className="gap-2 px-12 py-7 text-lg" onClick={handleFindMatch}>
                Find Match
              </Button>
            </>
          ) : (
            <div className="space-y-6">
              <div className="text-2xl font-mono font-bold text-primary">{formatTime(waitTime)}</div>
              <div className="text-muted-foreground">
                Searching within ±{eloRange} ELO ({timeControl}, {difficulty})
              </div>
              <div className="mx-auto h-2 w-48 rounded-full bg-primary/20 overflow-hidden">
                <div className="h-full w-1/3 rounded-full bg-primary animate-pulse" />
              </div>
              <Button variant="outline" onClick={handleCancel}>Cancel</Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
