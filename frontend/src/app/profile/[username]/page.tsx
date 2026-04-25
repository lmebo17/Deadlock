"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { Navbar } from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { EloChart } from "@/components/EloChart";
import { ContributionGraph } from "@/components/ContributionGraph";
import { useAuth } from "@/lib/auth";
import {
  getUserProfile, getMatchHistory, getEloHistory, getContributions,
  UserProfile, MatchResponse, EloHistoryPoint, ContributionDay,
} from "@/lib/api";

function getRankColor(eloRating: number): string {
  if (eloRating < 1200) return "text-rank-newbie";
  if (eloRating < 1400) return "text-rank-pupil";
  if (eloRating < 1600) return "text-rank-specialist";
  if (eloRating < 1900) return "text-rank-expert";
  if (eloRating < 2100) return "text-rank-candidate-master";
  if (eloRating < 2400) return "text-rank-master";
  return "text-rank-grandmaster";
}

export default function ProfilePage() {
  const params = useParams<{ username: string }>();
  const username = params.username as string;
  const { user } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [matches, setMatches] = useState<MatchResponse[]>([]);
  const [eloHistory, setEloHistory] = useState<EloHistoryPoint[]>([]);
  const [contributions, setContributions] = useState<ContributionDay[]>([]);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    if (!username) return;
    setLoading(true);
    Promise.all([
      getUserProfile(username),
      getMatchHistory(username).catch(() => [] as MatchResponse[]),
      getEloHistory(username).catch(() => [] as EloHistoryPoint[]),
      getContributions(username).catch(() => [] as ContributionDay[]),
    ])
      .then(([p, ms, eh, cs]) => {
        setProfile(p);
        setMatches(ms);
        setEloHistory(eh);
        setContributions(cs);
      })
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false));
  }, [username]);

  const isOwnProfile = user?.username === username;

  if (loading) {
    return (<div className="min-h-screen"><Navbar /><div className="container mx-auto px-4 pt-24 text-muted-foreground">Loading...</div></div>);
  }
  if (notFound || !profile) {
    return (<div className="min-h-screen"><Navbar /><div className="container mx-auto px-4 pt-24 text-destructive">User not found</div></div>);
  }

  const rankColor = getRankColor(profile.eloRating);

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 pt-24 pb-16 max-w-4xl">
        <Link href="/leaderboard" className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground mb-6">
          &larr; Back to Leaderboard
        </Link>

        <div className="mb-8 rounded-xl border border-border bg-card p-8">
          <div className="flex flex-col items-center gap-6 sm:flex-row">
            {profile.avatarUrl ? (
              <img src={profile.avatarUrl} alt="" className="h-24 w-24 rounded-2xl" />
            ) : (
              <div className={`flex h-24 w-24 items-center justify-center rounded-2xl bg-primary/10 text-3xl font-bold ${rankColor}`}>
                {profile.username.charAt(0).toUpperCase()}
              </div>
            )}
            <div className="text-center sm:text-left">
              <h1 className={`text-3xl font-bold ${rankColor}`}>{profile.username}</h1>
              {profile.displayName && <p className="text-muted-foreground">{profile.displayName}</p>}
              <div className="mt-2 flex flex-wrap items-center justify-center gap-3 sm:justify-start">
                <span className={`inline-flex items-center rounded-md border border-border bg-secondary px-3 py-1 font-mono text-sm font-semibold ${rankColor}`}>
                  {profile.tierLabel} ({profile.eloRating})
                </span>
                <span className="text-sm text-muted-foreground">
                  Joined {new Date(profile.joinedAt).toLocaleDateString("en-US", { month: "long", year: "numeric" })}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4 mb-8">
          {[
            { label: "Rating", value: profile.eloRating.toString(), colorClass: rankColor },
            { label: "Win Rate", value: `${Math.round(profile.winRate * 100)}%`, colorClass: "" },
            { label: "Total Matches", value: profile.totalMatches.toString(), colorClass: "" },
            { label: "W / L / D", value: `${profile.wins} / ${profile.losses} / ${profile.draws}`, colorClass: "" },
          ].map(({ label, value, colorClass }) => (
            <div key={label} className="rounded-xl border border-border bg-card p-5 text-center">
              <div className="text-sm text-muted-foreground mb-1">{label}</div>
              <div className={`text-2xl font-bold font-mono ${colorClass}`}>{value}</div>
            </div>
          ))}
        </div>

        <div className="mb-6">
          <ContributionGraph data={contributions} />
        </div>

        <div className="mb-6">
          <EloChart points={eloHistory.map(({ at, elo }) => ({ at, elo }))} />
        </div>

        <div className="rounded-xl border border-border bg-card overflow-hidden">
          <div className="border-b border-border px-6 py-4">
            <h2 className="text-lg font-semibold">Recent Matches</h2>
          </div>
          {matches.length === 0 ? (
            <EmptyState
              icon="⚔"
              title={isOwnProfile ? "No matches yet" : `${profile.username} hasn't played any matches yet`}
              description={isOwnProfile ? "Find an opponent and prove yourself." : undefined}
              cta={isOwnProfile ? { label: "Find Match", href: "/lobby" } : undefined}
            />
          ) : (
            <div>
              <div className="grid grid-cols-[1fr_120px_80px_120px] gap-4 border-b border-border bg-secondary/50 px-6 py-2 text-xs font-medium text-muted-foreground">
                <div>Match</div><div>Result</div><div>ELO</div><div>Date</div>
              </div>
              {matches.map((m) => {
                const isPlayer1 = m.player1.username === username;
                const opp = isPlayer1 ? m.player2 : m.player1;
                const eloChange = isPlayer1 ? m.player1EloChange : m.player2EloChange;
                const isWinner = m.winnerId != null && (isPlayer1 ? m.winnerId === m.player1.id : m.winnerId === m.player2.id);
                const isDraw = m.winnerId == null && m.status === "FINISHED";
                const resultLabel = m.status === "CANCELLED" ? "Forfeit" : isWinner ? "Won" : isDraw ? "Draw" : "Lost";
                const resultColor = isWinner ? "text-rank-pupil" : isDraw ? "text-muted-foreground" : "text-destructive";
                const eloColor = (eloChange ?? 0) > 0 ? "text-rank-pupil" : (eloChange ?? 0) < 0 ? "text-destructive" : "text-muted-foreground";

                return (
                  <Link
                    key={m.id}
                    href={`/match/${m.id}/result`}
                    className="grid grid-cols-[1fr_120px_80px_120px] gap-4 items-center border-b border-border px-6 py-3 text-sm last:border-0 hover:bg-secondary/30 transition-colors"
                  >
                    <div>
                      <div className="font-medium">{m.problemTitle}</div>
                      <div className="text-xs text-muted-foreground">vs {opp.username}</div>
                    </div>
                    <div className={`font-mono font-semibold ${resultColor}`}>{resultLabel}</div>
                    <div className={`font-mono text-sm ${eloColor}`}>
                      {eloChange != null ? `${eloChange > 0 ? "+" : ""}${eloChange}` : "-"}
                    </div>
                    <div className="text-xs text-muted-foreground">
                      {new Date(m.startedAt).toLocaleDateString()}
                    </div>
                  </Link>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function EmptyState({
  icon, title, description, cta,
}: {
  icon: string;
  title: string;
  description?: string;
  cta?: { label: string; href: string };
}) {
  return (
    <div className="px-6 py-12 text-center">
      <div className="text-4xl mb-3 text-muted-foreground">{icon}</div>
      <div className="font-semibold text-foreground mb-1">{title}</div>
      {description && <div className="text-sm text-muted-foreground mb-4">{description}</div>}
      {cta && (
        <Link href={cta.href}>
          <Button size="sm">{cta.label}</Button>
        </Link>
      )}
    </div>
  );
}
