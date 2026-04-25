"use client";

import { useEffect, useState, useRef, useCallback } from "react";
import { useParams, useRouter } from "next/navigation";
import dynamic from "next/dynamic";
import { Navbar } from "@/components/Navbar";
import { MarkdownRenderer } from "@/components/MarkdownRenderer";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/lib/auth";
import {
  getMatch, getProblemBySlug, getStarterCode, submitCode, getSubmission, runCode,
  MatchResponse, ProblemDetailResponse, SubmissionResponse, RunResult,
} from "@/lib/api";
import { getMatchWebSocket, MatchEvent } from "@/lib/match-ws";
import { toast } from "sonner";

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
  const [opponentPulse, setOpponentPulse] = useState(false);
  const [opponentDisconnected, setOpponentDisconnected] = useState(false);
  const [secondsLeft, setSecondsLeft] = useState(0);
  const [matchEnded, setMatchEnded] = useState(false);
  const [endResult, setEndResult] = useState<MatchEvent | null>(null);
  const [mobileTab, setMobileTab] = useState<"problem" | "editor">("problem");
  const [running, setRunning] = useState(false);
  const [runResult, setRunResult] = useState<RunResult | null>(null);
  const submittingRef = useRef(false);
  const runningRef = useRef(false);

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

  // Fallback: poll match status in case we missed the WebSocket MATCH_END event
  useEffect(() => {
    if (!matchId || matchEnded) return;
    const interval = setInterval(async () => {
      try {
        const fresh = await getMatch(matchId);
        if (fresh.status === "FINISHED" || fresh.status === "CANCELLED") {
          setMatch(fresh);
          setMatchEnded(true);
          if (!endResult) {
            // Synthesize a MATCH_END event from REST data
            const isPlayer1 = fresh.player1.id === user?.id;
            setEndResult({
              type: "MATCH_END",
              matchId: fresh.id,
              winnerId: fresh.winnerId ?? undefined,
              yourEloChange: isPlayer1 ? (fresh.player1EloChange ?? 0) : (fresh.player2EloChange ?? 0),
              opponentEloChange: isPlayer1 ? (fresh.player2EloChange ?? 0) : (fresh.player1EloChange ?? 0),
              yourFinalElo: (isPlayer1 ? fresh.player1.eloRating : fresh.player2.eloRating) +
                            (isPlayer1 ? (fresh.player1EloChange ?? 0) : (fresh.player2EloChange ?? 0)),
              finalStatus: fresh.status,
            });
          }
        }
      } catch {}
    }, 3000);
    return () => clearInterval(interval);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [matchId, matchEnded, user?.id]);

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
          setOpponentPulse(true);
          setTimeout(() => setOpponentPulse(false), 1500);
          toast.info("Opponent just submitted", { id: "opponent-submitted", duration: 2000 });
        }
      });

      ws.subscribeMatch(matchId, (event) => {
        if (event.type === "OPPONENT_DISCONNECTED") {
          setOpponentDisconnected(true);
          toast.warning("Opponent disconnected — they have 30s to reconnect", { id: "opponent-presence" });
        }
        if (event.type === "OPPONENT_RECONNECTED") {
          setOpponentDisconnected(false);
          toast.success("Opponent reconnected", { id: "opponent-presence" });
        }
        if (event.type === "MATCH_END") {
          setMatchEnded(true);
          if (!endResult) setEndResult(event);
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

  const handleRun = useCallback(async () => {
    if (!match || matchEnded || !code.trim() || runningRef.current || submittingRef.current) return;
    runningRef.current = true;
    setRunning(true);
    setRunResult(null);
    try {
      const r = await runCode(match.problemSlug, language, code);
      setRunResult(r);
    } catch {
      // ignore
    } finally {
      setRunning(false);
      runningRef.current = false;
    }
  }, [match, matchEnded, code, language]);

  const handleSubmit = useCallback(async () => {
    if (!match || !code.trim() || submittingRef.current) return;
    submittingRef.current = true;
    setSubmitting(true);
    setVerdict(null);
    setRunResult(null);
    try {
      const { id } = await submitCode(match.problemSlug, language, code);
      const poll = async () => {
        const sub = await getSubmission(id);
        if (sub.status === "COMPLETED") {
          setVerdict(sub);
          setSubmitting(false);
          submittingRef.current = false;
          if (sub.verdict === "ACCEPTED") {
            toast.success("Accepted!");
          } else if (sub.verdict) {
            toast.error(`${sub.verdict}${sub.failedTestCase ? ` on test ${sub.failedTestCase}` : ""}`);
          }
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
        {/* Top bar (sticky) */}
        <div className="sticky top-16 z-40 mb-4 flex items-center justify-between rounded-xl border border-border bg-card/95 backdrop-blur-md p-3 shadow-sm">
          <PlayerCard player={me} label="You" />
          <div className="text-center px-2">
            <div
              className={`text-2xl sm:text-3xl font-mono font-bold ${
                matchEnded
                  ? "text-muted-foreground"
                  : secondsLeft < 30
                    ? "text-destructive animate-pulse"
                    : secondsLeft < 60
                      ? "text-destructive"
                      : "text-primary"
              }`}
            >
              {formatTimer(secondsLeft)}
            </div>
            <div className="hidden sm:block text-xs text-muted-foreground mt-1 truncate max-w-[16rem]">
              {match.problemTitle}
            </div>
          </div>
          <div className={opponentPulse ? "animate-pulse ring-2 ring-destructive rounded-lg" : ""}>
            <PlayerCard player={opponent} label={opponentDisconnected ? "Disconnected" : `${opponentSubmissions} submissions`} reverse />
          </div>
        </div>

        {/* Mobile tab switcher (hidden on lg+) */}
        <div className="lg:hidden mb-3 flex rounded-lg border border-border bg-card p-1">
          <button
            type="button"
            onClick={() => setMobileTab("problem")}
            className={`flex-1 rounded-md px-3 py-2 text-sm font-medium transition-colors ${
              mobileTab === "problem"
                ? "bg-secondary text-foreground"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            Problem
          </button>
          <button
            type="button"
            onClick={() => setMobileTab("editor")}
            className={`flex-1 rounded-md px-3 py-2 text-sm font-medium transition-colors ${
              mobileTab === "editor"
                ? "bg-secondary text-foreground"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            Editor
          </button>
        </div>

        {/* Split: problem | editor */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {/* Left: problem */}
          <div className={`rounded-xl border border-border bg-card p-6 max-h-[calc(100vh-220px)] overflow-y-auto ${mobileTab === "problem" ? "" : "hidden"} lg:block`}>
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
          <div className={`space-y-3 ${mobileTab === "editor" ? "" : "hidden"} lg:block`}>
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
            <CodeEditor
              language={language}
              value={code}
              onChange={setCode}
              onSubmit={matchEnded ? undefined : handleSubmit}
              onRun={matchEnded ? undefined : handleRun}
            />
            <div className="flex items-center gap-3 flex-wrap">
              <Button
                onClick={handleSubmit}
                disabled={submitting || running || !code.trim() || matchEnded}
                className="px-8"
              >
                {submitting ? "Judging..." : "Submit"}
              </Button>
              <Button
                variant="outline"
                onClick={handleRun}
                disabled={running || submitting || !code.trim() || matchEnded}
              >
                {running ? "Running..." : "Run"}
              </Button>
              <span className="text-xs text-muted-foreground hidden sm:inline">
                ⌘/Ctrl+Enter submit · ⌘/Ctrl+Shift+Enter run
              </span>
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
            {runResult && <RunPanel result={runResult} />}
          </div>
        </div>

        {/* Match end overlay */}
        {matchEnded && (
          <MatchEndOverlay
            matchId={matchId}
            user={{ id: user.id, username: user.username || user.displayName }}
            endEvent={endResult}
            onClose={() => router.push(`/match/${matchId}/result`)}
          />
        )}
      </div>
    </div>
  );
}

function PlayerCard({ player, label, reverse }: { player: { username: string; avatarUrl: string; eloRating: number }; label: string; reverse?: boolean }) {
  return (
    <div className={`flex items-center gap-2 sm:gap-3 min-w-0 ${reverse ? "flex-row-reverse" : ""}`}>
      {player.avatarUrl && <img src={player.avatarUrl} alt="" className="h-8 w-8 sm:h-10 sm:w-10 rounded-full shrink-0" />}
      <div className={`min-w-0 ${reverse ? "text-right" : ""}`}>
        <div className="font-semibold text-sm sm:text-base truncate max-w-[6rem] sm:max-w-none">{player.username}</div>
        <div className={`text-xs font-mono ${getRankColor(player.eloRating)} truncate`}>
          {player.eloRating} · {label}
        </div>
      </div>
    </div>
  );
}

function MatchEndOverlay({
  matchId, user, endEvent, onClose,
}: {
  matchId: number;
  user: { id: number; username: string };
  endEvent: MatchEvent | null;
  onClose: () => void;
}) {
  const isWinner = endEvent?.winnerId === user.id;
  const isDraw = endEvent?.winnerId == null && endEvent?.finalStatus !== "CANCELLED";
  const change = endEvent?.yourEloChange ?? 0;
  const [secondsLeft, setSecondsLeft] = useState(5);

  // Countdown ticker — only updates state, no side effects in setter
  useEffect(() => {
    const timer = setInterval(() => {
      setSecondsLeft((s) => Math.max(0, s - 1));
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  // Trigger redirect when countdown hits 0 (separate effect, runs after render)
  useEffect(() => {
    if (secondsLeft === 0) onClose();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [secondsLeft]);

  return (
    <div className="fixed inset-0 z-50 bg-background/95 backdrop-blur-sm flex items-center justify-center animate-in fade-in duration-300">
      <div className="rounded-2xl border border-border bg-card p-10 max-w-md w-full text-center space-y-6 animate-in zoom-in-95 duration-300">
        <h2 className="text-5xl font-bold">
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
        <Button onClick={onClose} size="lg" className="w-full">
          View Result {secondsLeft > 0 && `(${secondsLeft}s)`}
        </Button>
      </div>
    </div>
  );
}

function RunPanel({ result }: { result: RunResult }) {
  if (result.compileError) {
    return (
      <div className="rounded-lg border border-destructive/40 bg-destructive/5 p-3">
        <div className="font-semibold text-destructive text-sm mb-1">Compile error</div>
        <pre className="font-mono text-xs whitespace-pre-wrap text-destructive max-h-32 overflow-y-auto">
          {result.compileErrorMessage}
        </pre>
      </div>
    );
  }
  const passed = result.tests.filter((t) => t.passed).length;
  const total = result.tests.length;
  const allPassed = passed === total && total > 0;

  return (
    <div className="rounded-lg border border-border bg-card p-3">
      <div className={`text-sm font-semibold mb-2 ${allPassed ? "text-rank-pupil" : "text-destructive"}`}>
        {passed} / {total} samples passed
        <span className="ml-2 text-xs font-normal text-muted-foreground">
          {result.totalExecutionTimeMs}ms
        </span>
      </div>
      <div className="space-y-2">
        {result.tests.map((t) => {
          const status = t.passed ? "Pass" : t.timedOut ? "TLE" : t.runtimeError ? "Runtime error" : "Wrong answer";
          const color = t.passed ? "text-rank-pupil" : "text-destructive";
          return (
            <details key={t.index} className="rounded border border-border" open={!t.passed}>
              <summary className="cursor-pointer px-2 py-1.5 text-xs flex items-center gap-2">
                <span className={`font-mono font-bold ${color}`}>{status}</span>
                <span className="text-muted-foreground">Sample {t.index}</span>
              </summary>
              <div className="grid grid-cols-1 gap-2 px-2 pb-2 text-[11px]">
                <Block label="Input" content={t.input} />
                <Block label="Expected" content={t.expected} />
                <Block label="Your output" content={t.timedOut ? "(timed out)" : t.actual} />
                {t.stderr && t.stderr.trim() && <Block label="stderr" content={t.stderr} variant="error" />}
              </div>
            </details>
          );
        })}
      </div>
    </div>
  );
}

function Block({ label, content, variant }: { label: string; content: string; variant?: "error" }) {
  return (
    <div>
      <div className="text-muted-foreground mb-0.5">{label}</div>
      <pre className={`rounded border border-border p-1.5 font-mono whitespace-pre-wrap break-words max-h-24 overflow-y-auto ${
        variant === "error" ? "bg-destructive/10 text-destructive" : "bg-secondary/50"
      }`}>
        {content}
      </pre>
    </div>
  );
}
