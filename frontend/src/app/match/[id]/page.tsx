"use client";

import { useEffect, useState, useRef, useCallback } from "react";
import { useParams, useRouter } from "next/navigation";
import dynamic from "next/dynamic";
import { Navbar } from "@/components/Navbar";
import { MarkdownRenderer } from "@/components/MarkdownRenderer";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/lib/auth";
import {
  getMatch, getProblemBySlug, getStarterCode, submitCode, getSubmission,
  MatchResponse, ProblemDetailResponse, SubmissionResponse,
} from "@/lib/api";
import { getMatchWebSocket, MatchEvent } from "@/lib/match-ws";

const CodeEditor = dynamic(
  () => import("@/components/CodeEditor").then((m) => ({ default: m.CodeEditor })),
  { ssr: false }
);

function formatTimer(seconds: number): string {
  if (seconds < 0) seconds = 0;
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${s.toString().padStart(2, "0")}`;
}

function getRankColor(elo: number): string {
  if (elo < 1200) return "text-rank-newbie";
  if (elo < 1400) return "text-rank-pupil";
  if (elo < 1600) return "text-rank-specialist";
  if (elo < 1900) return "text-rank-expert";
  if (elo < 2100) return "text-rank-candidate-master";
  if (elo < 2400) return "text-rank-master";
  return "text-rank-grandmaster";
}

export default function MatchPage() {
  const params = useParams();
  const router = useRouter();
  const { user } = useAuth();
  const matchId = Number(params.id);

  const [match, setMatch] = useState<MatchResponse | null>(null);
  const [problem, setProblem] = useState<ProblemDetailResponse | null>(null);
  const [language, setLanguage] = useState("PYTHON");
  const [code, setCode] = useState("");
  const [previousStarter, setPreviousStarter] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [verdict, setVerdict] = useState<SubmissionResponse | null>(null);
  const [opponentSubmissions, setOpponentSubmissions] = useState(0);
  const [opponentDisconnected, setOpponentDisconnected] = useState(false);
  const [secondsLeft, setSecondsLeft] = useState(0);
  const [matchEnded, setMatchEnded] = useState(false);
  const [endResult, setEndResult] = useState<MatchEvent | null>(null);
  const submittingRef = useRef(false);

  // Load match + problem
  useEffect(() => {
    if (!matchId || isNaN(matchId)) return;
    getMatch(matchId).then((m) => {
      setMatch(m);
      if (m.status === "FINISHED" || m.status === "CANCELLED") {
        setMatchEnded(true);
      }
      return getProblemBySlug(m.problemSlug);
    }).then(setProblem).catch(() => {});
  }, [matchId]);

  // Timer
  useEffect(() => {
    if (!match || matchEnded) return;
    const startedAt = new Date(match.startedAt).getTime();
    const endsAt = startedAt + match.durationSeconds * 1000;
    const tick = () => {
      const remaining = Math.floor((endsAt - Date.now()) / 1000);
      setSecondsLeft(remaining);
      if (remaining <= 0) {
        setMatchEnded(true);
      }
    };
    tick();
    const interval = setInterval(tick, 1000);
    return () => clearInterval(interval);
  }, [match, matchEnded]);

  // WebSocket subscriptions
  useEffect(() => {
    if (!matchId || isNaN(matchId) || !user) return;
    const ws = getMatchWebSocket();

    let mounted = true;
    ws.connect().then(() => {
      if (!mounted) return;

      ws.onPersonalEvent((event) => {
        if (event.type === "MATCH_END" && event.matchId === matchId) {
          setMatchEnded(true);
          setEndResult(event);
        } else if (event.type === "OPPONENT_SUBMITTED" && event.matchId === matchId) {
          if (event.submissionCount !== undefined) setOpponentSubmissions(event.submissionCount);
        }
      });

      ws.subscribeMatch(matchId, (event) => {
        if (event.type === "OPPONENT_DISCONNECTED") setOpponentDisconnected(true);
        if (event.type === "OPPONENT_RECONNECTED") setOpponentDisconnected(false);
        if (event.type === "MATCH_END") {
          setMatchEnded(true);
          if (!endResult) setEndResult(event);
        }
        if (event.type === "OPPONENT_SUBMITTED" && event.submissionCount !== undefined) {
          setOpponentSubmissions(event.submissionCount);
        }
      });
    });

    return () => { mounted = false; };
  }, [matchId, user, endResult]);

  // Load starter code
  useEffect(() => {
    if (!match) return;
    getStarterCode(match.problemSlug, language).then(({ code: starter }) => {
      if (!code || code === previousStarter) {
        setCode(starter);
      }
      setPreviousStarter(starter);
    }).catch(() => {});
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [match, language]);

  const handleSubmit = useCallback(async () => {
    if (!match || !code.trim() || submittingRef.current) return;
    submittingRef.current = true;
    setSubmitting(true);
    setVerdict(null);
    try {
      const { id } = await submitCode(match.problemSlug, language, code);
      const poll = async () => {
        const sub = await getSubmission(id);
        if (sub.status === "COMPLETED") {
          setVerdict(sub);
          setSubmitting(false);
          submittingRef.current = false;
        } else {
          setTimeout(poll, 1500);
        }
      };
      poll();
    } catch {
      setSubmitting(false);
      submittingRef.current = false;
    }
  }, [match, code, language]);

  if (!match || !problem || !user) {
    return (
      <div className="min-h-screen">
        <Navbar />
        <div className="container mx-auto pt-24 text-muted-foreground">Loading match...</div>
      </div>
    );
  }

  const me = match.player1.id === user.id ? match.player1 : match.player2;
  const opponent = match.player1.id === user.id ? match.player2 : match.player1;
  const verdictColor = verdict?.verdict === "ACCEPTED" ? "text-rank-pupil"
    : verdict?.verdict ? "text-destructive" : "";

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 pt-20 pb-6">
        {/* Top bar */}
        <div className="mb-4 flex items-center justify-between rounded-xl border border-border bg-card p-3">
          <PlayerCard player={me} label="You" />
          <div className="text-center">
            <div className={`text-3xl font-mono font-bold ${secondsLeft < 60 ? "text-destructive" : "text-primary"}`}>
              {formatTimer(secondsLeft)}
            </div>
            <div className="text-xs text-muted-foreground mt-1">
              {match.problemTitle}
            </div>
          </div>
          <PlayerCard player={opponent} label={opponentDisconnected ? "Disconnected" : `${opponentSubmissions} submissions`} reverse />
        </div>

        {/* Split: problem | editor */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {/* Left: problem */}
          <div className="rounded-xl border border-border bg-card p-6 max-h-[calc(100vh-220px)] overflow-y-auto">
            <h2 className="text-xl font-bold mb-2">{problem.title}</h2>
            <div className="text-xs text-muted-foreground mb-4">
              Time limit: {problem.timeLimitMs}ms · Memory: {problem.memoryLimitMb}MB
            </div>
            <MarkdownRenderer content={problem.description} />
            <h3 className="text-base font-semibold mt-4 mb-2">Input</h3>
            <MarkdownRenderer content={problem.inputFormat} />
            <h3 className="text-base font-semibold mt-4 mb-2">Output</h3>
            <MarkdownRenderer content={problem.outputFormat} />
            <h3 className="text-base font-semibold mt-4 mb-2">Constraints</h3>
            <MarkdownRenderer content={problem.constraints} />
            <h3 className="text-base font-semibold mt-4 mb-2">Examples</h3>
            {problem.sampleTestCases.map((tc) => (
              <div key={tc.index} className="mb-3 grid grid-cols-2 gap-2">
                <div>
                  <div className="text-xs text-muted-foreground mb-1">Input</div>
                  <pre className="rounded border border-border bg-secondary/50 p-2 font-mono text-xs overflow-x-auto">{tc.input}</pre>
                </div>
                <div>
                  <div className="text-xs text-muted-foreground mb-1">Output</div>
                  <pre className="rounded border border-border bg-secondary/50 p-2 font-mono text-xs overflow-x-auto">{tc.output}</pre>
                </div>
              </div>
            ))}
          </div>

          {/* Right: editor + submit */}
          <div className="space-y-3">
            <div className="flex gap-3 items-center">
              <select
                value={language}
                onChange={(e) => setLanguage(e.target.value)}
                disabled={matchEnded}
                className="rounded-lg border border-border bg-secondary px-3 py-2 text-sm"
              >
                <option value="PYTHON">Python</option>
                <option value="JAVA">Java</option>
                <option value="CPP">C++</option>
              </select>
            </div>
            <CodeEditor language={language} value={code} onChange={setCode} />
            <div className="flex items-center gap-4">
              <Button
                onClick={handleSubmit}
                disabled={submitting || !code.trim() || matchEnded}
                className="px-8"
              >
                {submitting ? "Judging..." : "Submit"}
              </Button>
              {verdict && (
                <div className="flex items-center gap-2">
                  <span className={`font-mono font-bold ${verdictColor}`}>{verdict.verdict}</span>
                  {verdict.failedTestCase && (
                    <span className="text-sm text-muted-foreground">test {verdict.failedTestCase}</span>
                  )}
                  {verdict.executionTimeMs && (
                    <span className="text-sm text-muted-foreground">{verdict.executionTimeMs}ms</span>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Match end overlay */}
        {matchEnded && (
          <MatchEndOverlay
            user={{ id: user.id, username: user.username || user.displayName }}
            endEvent={endResult}
            onClose={() => router.push("/lobby")}
          />
        )}
      </div>
    </div>
  );
}

function PlayerCard({ player, label, reverse }: { player: { username: string; avatarUrl: string; eloRating: number }; label: string; reverse?: boolean }) {
  return (
    <div className={`flex items-center gap-3 ${reverse ? "flex-row-reverse" : ""}`}>
      {player.avatarUrl && <img src={player.avatarUrl} alt="" className="h-10 w-10 rounded-full" />}
      <div className={reverse ? "text-right" : ""}>
        <div className="font-semibold">{player.username}</div>
        <div className={`text-xs font-mono ${getRankColor(player.eloRating)}`}>
          {player.eloRating} · {label}
        </div>
      </div>
    </div>
  );
}

function MatchEndOverlay({
  user, endEvent, onClose,
}: {
  user: { id: number; username: string };
  endEvent: MatchEvent | null;
  onClose: () => void;
}) {
  const isWinner = endEvent?.winnerId === user.id;
  const isDraw = endEvent?.winnerId == null && endEvent?.finalStatus !== "CANCELLED";
  const change = endEvent?.yourEloChange ?? 0;

  return (
    <div className="fixed inset-0 z-50 bg-background/90 backdrop-blur-sm flex items-center justify-center">
      <div className="rounded-2xl border border-border bg-card p-10 max-w-md w-full text-center space-y-6">
        <h2 className="text-4xl font-bold">
          {isWinner ? <span className="text-rank-pupil">Victory!</span>
            : isDraw ? <span className="text-muted-foreground">Draw</span>
            : <span className="text-destructive">Defeat</span>}
        </h2>
        <div className="text-lg">
          ELO: <span className={change >= 0 ? "text-rank-pupil" : "text-destructive"}>
            {change >= 0 ? "+" : ""}{change}
          </span>
          <span className="text-muted-foreground"> → {endEvent?.yourFinalElo ?? "?"}</span>
        </div>
        <Button onClick={onClose} size="lg">Back to Lobby</Button>
      </div>
    </div>
  );
}
