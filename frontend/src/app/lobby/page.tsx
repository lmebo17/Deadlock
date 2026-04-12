"use client";

import { Navbar } from "@/components/Navbar";
import { useAuth } from "@/lib/auth";
import { Button } from "@/components/ui/button";

function getRankColor(eloRating: number): string {
  if (eloRating < 1200) return "text-rank-newbie";
  if (eloRating < 1400) return "text-rank-pupil";
  if (eloRating < 1600) return "text-rank-specialist";
  if (eloRating < 1900) return "text-rank-expert";
  if (eloRating < 2100) return "text-rank-candidate-master";
  if (eloRating < 2400) return "text-rank-master";
  return "text-rank-grandmaster";
}

export default function LobbyPage() {
  const { user } = useAuth();

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

          <Button size="lg" className="gap-2 px-12 py-7 text-lg" disabled>
            Find Match
          </Button>
          <p className="mt-4 text-sm text-muted-foreground">Matchmaking coming soon</p>
        </div>
      </div>
    </div>
  );
}
