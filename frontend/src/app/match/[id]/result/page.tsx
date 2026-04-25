"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import dynamic from "next/dynamic";
import Link from "next/link";
import { Navbar } from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/lib/auth";
import {
  getMatch, getMatchSubmissions,
  MatchResponse, SubmissionResponse,
} from "@/lib/api";

const CodeDiff = dynamic(
  () => import("@/components/CodeDiff").then((m) => ({ default: m.CodeDiff })),
  { ssr: false }
);

function getRankColor(elo: number): string {
  if (elo < 1200) return "text-rank-newbie";
  if (elo < 1400) return "text-rank-pupil";
  if (elo < 1600) return "text-rank-specialist";
  if (elo < 1900) return "text-rank-expert";
  if (elo < 2100) return "text-rank-candidate-master";
  if (elo < 2400) return "text-rank-master";
  return "text-rank-grandmaster";
}

function verdictColor(v: string | null): string {
  if (v === "ACCEPTED") return "text-rank-pupil";
  if (v) return "text-destructive";
  return "text-muted-foreground";
}

export default function MatchResultPage() {
  const params = useParams();
  const router = useRouter();
  const { user } = useAuth();
  const matchId = Number(params.id);

  const [match, setMatch] = useState<MatchResponse | null>(null);
  const [submissions, setSubmissions] = useState<SubmissionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState<"split" | "diff">("split");

  useEffect(() => {
    if (!matchId || isNaN(matchId)) return;
    Promise.all([getMatch(matchId), getMatchSubmissions(matchId)])
      .then(([m, subs]) => {
        setMatch(m);
        setSubmissions(subs);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [matchId]);

  if (loading) {
    return (
      <div className="min-h-screen">
        <Navbar />
        <div className="container mx-auto pt-24 text-muted-foreground">Loading result...</div>
      </div>
    );
  }

  if (!match || !user) {
    return (
      <div className="min-h-screen">
        <Navbar />
        <div className="container mx-auto pt-24 text-destructive">Match not found</div>
      </div>
    );
  }

  const me = match.player1.id === user.id ? match.player1 : match.player2;
  const opponent = match.player1.id === user.id ? match.player2 : match.player1;
  const myEloChange = match.player1.id === user.id ? match.player1EloChange : match.player2EloChange;
  const opponentEloChange = match.player1.id === user.id ? match.player2EloChange : match.player1EloChange;

  const isWinner = match.winnerId === user.id;
  const isDraw = match.winnerId == null && match.status === "FINISHED";
  const isCancelled = match.status === "CANCELLED";

  const mySubmissions = submissions.filter((s) => s.userId === user.id);
  const opponentSubmissions = submissions.filter((s) => s.userId === opponent.id);
  const myWinning = mySubmissions.find((s) => s.verdict === "ACCEPTED");
  const opponentWinning = opponentSubmissions.find((s) => s.verdict === "ACCEPTED");

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 pt-24 pb-16 max-w-5xl">
        <Link href="/lobby" className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground mb-6">
          &larr; Back to Lobby
        </Link>

        {/* Hero result */}
        <div className="rounded-2xl border border-border bg-card p-10 text-center mb-6">
          <div className="text-sm font-medium text-muted-foreground uppercase tracking-widest mb-3">
            {isCancelled ? "Forfeit" : "Match Complete"}
          </div>
          <h1 className="text-5xl font-bold mb-4">
            {isWinner ? <span className="text-rank-pupil">Victory</span>
              : isDraw ? <span className="text-muted-foreground">Draw</span>
              : <span className="text-destructive">Defeat</span>}
          </h1>
          <div className="text-muted-foreground">{match.problemTitle}</div>
        </div>

        {/* Players + ELO */}
        <div className="grid grid-cols-2 gap-4 mb-6">
          <PlayerResultCard
            user={me}
            eloChange={myEloChange ?? 0}
            won={isWinner}
            draw={isDraw}
            label="You"
          />
          <PlayerResultCard
            user={opponent}
            eloChange={opponentEloChange ?? 0}
            won={!isWinner && !isDraw}
            draw={isDraw}
            label="Opponent"
          />
        </div>

        {/* Solutions */}
        {(() => {
          const mySub = myWinning ?? mySubmissions[mySubmissions.length - 1] ?? null;
          const oppSub = opponentWinning ?? null;
          const canDiff = !!mySub && !!oppSub && mySub.language === oppSub.language;

          return (
            <>
              <div className="mb-3 flex items-center justify-between">
                <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wide">Solutions</h2>
                {canDiff && (
                  <div className="flex rounded-lg border border-border bg-card p-0.5">
                    <button
                      type="button"
                      onClick={() => setViewMode("split")}
                      className={`rounded-md px-3 py-1 text-xs font-medium transition-colors ${
                        viewMode === "split" ? "bg-secondary text-foreground" : "text-muted-foreground hover:text-foreground"
                      }`}
                    >
                      Split
                    </button>
                    <button
                      type="button"
                      onClick={() => setViewMode("diff")}
                      className={`rounded-md px-3 py-1 text-xs font-medium transition-colors ${
                        viewMode === "diff" ? "bg-secondary text-foreground" : "text-muted-foreground hover:text-foreground"
                      }`}
                    >
                      Diff
                    </button>
                  </div>
                )}
              </div>

              {viewMode === "diff" && canDiff ? (
                <div className="mb-6">
                  <CodeDiff
                    language={mySub!.language}
                    original={mySub!.code}
                    modified={oppSub!.code}
                    originalLabel={`You · ${mySub!.verdict ?? mySub!.status}`}
                    modifiedLabel={`${opponent.username} · ${oppSub!.verdict ?? oppSub!.status}`}
                  />
                </div>
              ) : (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-6">
                  <SolutionPanel
                    title="Your solution"
                    submission={mySub}
                    totalAttempts={mySubmissions.length}
                  />
                  <SolutionPanel
                    title="Opponent's solution"
                    submission={oppSub}
                    totalAttempts={opponentSubmissions.length}
                    hideCodeIfNotAccepted
                  />
                </div>
              )}
            </>
          );
        })()}

        <div className="flex justify-center gap-3">
          <Button onClick={() => router.push("/lobby")} size="lg">
            Play Again
          </Button>
          <Link href={`/profile/${me.username}`}>
            <Button variant="outline" size="lg">View Profile</Button>
          </Link>
        </div>
      </div>
    </div>
  );
}

function PlayerResultCard({
  user, eloChange, won, draw, label,
}: {
  user: { username: string; avatarUrl: string; eloRating: number };
  eloChange: number;
  won: boolean;
  draw: boolean;
  label: string;
}) {
  const newElo = user.eloRating + eloChange;
  const ringColor = won ? "ring-rank-pupil" : draw ? "ring-muted-foreground" : "ring-destructive";
  const changeColor = eloChange > 0 ? "text-rank-pupil" : eloChange < 0 ? "text-destructive" : "text-muted-foreground";

  return (
    <div className={`rounded-xl border border-border bg-card p-6 ring-2 ${ringColor}`}>
      <div className="text-xs font-medium text-muted-foreground uppercase mb-3">{label}</div>
      <div className="flex items-center gap-3 mb-4">
        {user.avatarUrl && (
          <img src={user.avatarUrl} alt="" className="h-12 w-12 rounded-full" />
        )}
        <div>
          <div className="font-bold">{user.username}</div>
          <div className={`text-xs font-mono ${getRankColor(user.eloRating)}`}>
            {user.eloRating}
          </div>
        </div>
      </div>
      <div className="flex items-baseline gap-2">
        <span className="text-3xl font-mono font-bold">{newElo}</span>
        <span className={`text-lg font-mono font-bold ${changeColor}`}>
          {eloChange > 0 ? "+" : ""}{eloChange}
        </span>
      </div>
    </div>
  );
}

function SolutionPanel({
  title, submission, totalAttempts, hideCodeIfNotAccepted,
}: {
  title: string;
  submission: SubmissionResponse | null;
  totalAttempts: number;
  hideCodeIfNotAccepted?: boolean;
}) {
  if (!submission) {
    return (
      <div className="rounded-xl border border-border bg-card p-5">
        <div className="text-sm font-semibold mb-3">{title}</div>
        <div className="rounded-lg border border-dashed border-border px-4 py-8 text-center">
          <div className="text-2xl text-muted-foreground mb-1">∅</div>
          <div className="text-sm font-medium">No submissions</div>
          <div className="text-xs text-muted-foreground mt-1">
            {totalAttempts === 0 ? "Never attempted" : `${totalAttempts} attempt${totalAttempts !== 1 ? "s" : ""}, none accepted`}
          </div>
        </div>
      </div>
    );
  }
  const isAccepted = submission.verdict === "ACCEPTED";
  const showCode = !hideCodeIfNotAccepted || isAccepted;

  return (
    <div className="rounded-xl border border-border bg-card p-5">
      <div className="flex items-center justify-between mb-2">
        <div className="text-sm font-semibold">{title}</div>
        <div className="text-xs text-muted-foreground">
          {totalAttempts} attempt{totalAttempts !== 1 ? "s" : ""}
        </div>
      </div>
      <div className="flex items-center gap-3 mb-3 text-xs">
        <span className={`font-mono font-bold ${verdictColor(submission.verdict)}`}>
          {submission.verdict ?? submission.status}
        </span>
        <span className="text-muted-foreground">{submission.language}</span>
        {submission.executionTimeMs != null && (
          <span className="text-muted-foreground">{submission.executionTimeMs}ms</span>
        )}
      </div>
      {showCode ? (
        <pre className="rounded-lg border border-border bg-secondary/50 p-3 font-mono text-xs overflow-x-auto max-h-80">
          {submission.code}
        </pre>
      ) : (
        <div className="text-xs text-muted-foreground italic">
          Code hidden — opponent did not solve
        </div>
      )}
    </div>
  );
}
