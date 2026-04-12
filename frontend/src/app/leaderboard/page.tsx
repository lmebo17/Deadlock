"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Navbar } from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/lib/auth";
import { getLeaderboard, LeaderboardEntry, PageResponse } from "@/lib/api";

function getRankColor(eloRating: number): string {
  if (eloRating < 1200) return "text-rank-newbie";
  if (eloRating < 1400) return "text-rank-pupil";
  if (eloRating < 1600) return "text-rank-specialist";
  if (eloRating < 1900) return "text-rank-expert";
  if (eloRating < 2100) return "text-rank-candidate-master";
  if (eloRating < 2400) return "text-rank-master";
  return "text-rank-grandmaster";
}

function MedalIcon({ rank }: { rank: number }) {
  if (rank === 1) return <span className="text-lg">🥇</span>;
  if (rank === 2) return <span className="text-lg">🥈</span>;
  if (rank === 3) return <span className="text-lg">🥉</span>;
  return <span className="font-mono text-muted-foreground">{rank}</span>;
}

export default function LeaderboardPage() {
  const { user } = useAuth();
  const [data, setData] = useState<PageResponse<LeaderboardEntry> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    getLeaderboard(page, 50)
      .then(setData)
      .catch(() => setData(null))
      .finally(() => setLoading(false));
  }, [page]);

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 pt-24 pb-16">
        <h1 className="mb-2 text-4xl font-bold">Leaderboard</h1>
        <p className="mb-8 text-muted-foreground">Top competitive programmers</p>

        <div className="rounded-xl border border-border bg-card overflow-hidden">
          <div className="grid grid-cols-[60px_1fr_140px_100px] gap-4 border-b border-border bg-secondary/50 px-6 py-3 text-sm font-medium text-muted-foreground">
            <div>#</div>
            <div>Player</div>
            <div>Rank</div>
            <div className="text-right">Rating</div>
          </div>

          {loading ? (
            <div className="px-6 py-12 text-center text-muted-foreground">Loading...</div>
          ) : data && data.content.length > 0 ? (
            data.content.map((entry) => {
              const isMe = user?.username === entry.username;
              return (
                <Link
                  key={entry.id}
                  href={`/profile/${entry.username}`}
                  className={`grid grid-cols-[60px_1fr_140px_100px] gap-4 items-center border-b border-border px-6 py-4 transition-colors hover:bg-secondary/30 last:border-0 ${
                    isMe ? "bg-primary/5 border-l-2 border-l-primary" : ""
                  }`}
                >
                  <div><MedalIcon rank={entry.rank} /></div>
                  <div className="flex items-center gap-3">
                    {entry.avatarUrl && (
                      <img src={entry.avatarUrl} alt="" className="h-8 w-8 rounded-full" />
                    )}
                    <span className={`font-medium ${isMe ? "text-primary" : ""}`}>
                      {entry.username}
                      {isMe && <span className="ml-2 text-xs text-muted-foreground">(you)</span>}
                    </span>
                  </div>
                  <div>
                    <span className={`inline-flex items-center rounded-md border border-border bg-secondary px-2 py-0.5 font-mono text-xs font-semibold ${getRankColor(entry.eloRating)}`}>
                      {entry.tierLabel}
                    </span>
                  </div>
                  <div className={`text-right font-mono font-bold ${getRankColor(entry.eloRating)}`}>
                    {entry.eloRating}
                  </div>
                </Link>
              );
            })
          ) : (
            <div className="px-6 py-12 text-center text-muted-foreground">No players yet</div>
          )}
        </div>

        {data && data.totalPages > 1 && (
          <div className="mt-6 flex items-center justify-center gap-2">
            <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>
              Previous
            </Button>
            <span className="text-sm text-muted-foreground">Page {page + 1} of {data.totalPages}</span>
            <Button variant="outline" size="sm" disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)}>
              Next
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
