"use client";

import { useEffect, useState, useRef } from "react";
import { useRouter } from "next/navigation";
import { Navbar } from "@/components/Navbar";
import { useAuth } from "@/lib/auth";
import { Button } from "@/components/ui/button";
import { getMatchWebSocket, MatchEvent } from "@/lib/match-ws";
import { getActiveMatch } from "@/lib/api";
import { toast } from "sonner";

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
  const [matchFound, setMatchFound] = useState<MatchEvent | null>(null);
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
        setMatchFound(event);
        toast.success(`Match found vs ${event.opponentUsername}!`);
        // Show anticipation screen for 2.5 seconds, then navigate
        setTimeout(() => {
          router.push(`/match/${event.matchId}`);
        }, 2500);
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
            <div className="rounded-2xl border border-border bg-card p-10 space-y-6">
              <div>
                <div className="text-xs font-medium text-muted-foreground uppercase tracking-widest mb-2">
                  Searching for opponent
                </div>
                <div className="text-6xl font-mono font-bold text-primary tabular-nums">
                  {formatTime(waitTime)}
                </div>
              </div>

              {/* Animated radar dots */}
              <div className="flex items-center justify-center gap-1 h-3">
                <span className="h-2 w-2 rounded-full bg-primary animate-pulse" style={{ animationDelay: "0ms" }} />
                <span className="h-2 w-2 rounded-full bg-primary animate-pulse" style={{ animationDelay: "200ms" }} />
                <span className="h-2 w-2 rounded-full bg-primary animate-pulse" style={{ animationDelay: "400ms" }} />
              </div>

              <div className="space-y-2 text-sm">
                <div className="flex items-center justify-between text-muted-foreground">
                  <span>Mode</span>
                  <span className="font-medium text-foreground">
                    {TIME_CONTROLS.find((t) => t.value === timeControl)?.label} ·{" "}
                    {difficulty.charAt(0) + difficulty.slice(1).toLowerCase()}
                  </span>
                </div>
                <div className="flex items-center justify-between text-muted-foreground">
                  <span>ELO range</span>
                  <span className="font-mono text-foreground">
                    ±{eloRange}
                    {waitTime >= 30 && <span className="ml-1 text-xs text-rank-pupil">(expanded)</span>}
                  </span>
                </div>
              </div>

              <Button
                variant="outline"
                size="lg"
                onClick={handleCancel}
                className="w-full"
              >
                Cancel search
              </Button>
            </div>
          )}
        </div>

        {matchFound && user && (
          <div className="fixed inset-0 z-50 bg-background/95 backdrop-blur-sm flex items-center justify-center animate-in fade-in duration-300">
            <div className="text-center space-y-8 animate-in zoom-in-95 duration-500">
              <div className="text-sm font-medium text-muted-foreground uppercase tracking-widest">
                Match Found
              </div>
              <div className="flex items-center gap-12">
                {/* You */}
                <div className="text-center">
                  {user.avatarUrl && (
                    <img src={user.avatarUrl} alt="" className="h-24 w-24 rounded-full mx-auto mb-3 ring-4 ring-primary" />
                  )}
                  <div className="font-bold text-lg">{user.username}</div>
                  <div className={`text-sm font-mono ${getRankColor(user.eloRating)}`}>
                    {user.eloRating}
                  </div>
                </div>

                <div className="text-5xl font-bold text-primary animate-pulse">VS</div>

                {/* Opponent */}
                <div className="text-center">
                  {matchFound.opponentAvatarUrl && (
                    <img src={matchFound.opponentAvatarUrl} alt="" className="h-24 w-24 rounded-full mx-auto mb-3 ring-4 ring-destructive" />
                  )}
                  <div className="font-bold text-lg">{matchFound.opponentUsername}</div>
                  <div className={`text-sm font-mono ${getRankColor(matchFound.opponentElo ?? 1200)}`}>
                    {matchFound.opponentElo}
                  </div>
                </div>
              </div>
              <div className="text-muted-foreground text-sm">
                {matchFound.problemTitle}
              </div>
              <div className="text-xs text-muted-foreground animate-pulse">
                Entering arena...
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
