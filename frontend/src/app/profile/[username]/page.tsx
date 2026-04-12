"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { Navbar } from "@/components/Navbar";
import { getUserProfile, UserProfile } from "@/lib/api";

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
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    if (!username) return;
    setLoading(true);
    getUserProfile(username)
      .then(setProfile)
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false));
  }, [username]);

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

        <div className="rounded-xl border border-border bg-card overflow-hidden">
          <div className="border-b border-border px-6 py-4">
            <h2 className="text-lg font-semibold">Recent Matches</h2>
          </div>
          <div className="px-6 py-12 text-center text-muted-foreground">No matches played yet</div>
        </div>
      </div>
    </div>
  );
}
