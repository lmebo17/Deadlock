import Link from "next/link";
import { Navbar } from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { Swords, TrendingUp, Trophy, Code, Users, Zap } from "lucide-react";

const features = [
  {
    icon: Swords,
    title: "1v1 Battles",
    description: "Race against opponents to solve algorithmic problems faster",
  },
  {
    icon: TrendingUp,
    title: "ELO Rating",
    description: "Competitive rating system inspired by chess",
  },
  {
    icon: Trophy,
    title: "Rank System",
    description: "From Newbie to Grandmaster — 7 ranks",
  },
  {
    icon: Code,
    title: "1000+ Problems",
    description: "Curated collection spanning arrays, trees, graphs, DP",
  },
  {
    icon: Users,
    title: "Global Leaderboard",
    description: "Compete with coders worldwide",
  },
  {
    icon: Zap,
    title: "Instant Matchmaking",
    description: "Get matched with opponents of similar skill in seconds",
  },
];

const ranks = [
  { name: "Newbie", range: "0 – 1199", color: "text-[hsl(215,12%,60%)] border-[hsl(215,12%,40%)] bg-[hsl(215,12%,14%)]" },
  { name: "Pupil", range: "1200 – 1399", color: "text-[hsl(160,84%,45%)] border-[hsl(160,84%,30%)] bg-[hsl(160,84%,8%)]" },
  { name: "Specialist", range: "1400 – 1599", color: "text-[hsl(187,92%,50%)] border-[hsl(187,92%,35%)] bg-[hsl(187,92%,8%)]" },
  { name: "Expert", range: "1600 – 1899", color: "text-[hsl(217,91%,65%)] border-[hsl(217,91%,40%)] bg-[hsl(217,91%,10%)]" },
  { name: "Candidate Master", range: "1900 – 2099", color: "text-[hsl(271,91%,70%)] border-[hsl(271,91%,45%)] bg-[hsl(271,91%,10%)]" },
  { name: "Master", range: "2100 – 2299", color: "text-[hsl(25,95%,58%)] border-[hsl(25,95%,38%)] bg-[hsl(25,95%,8%)]" },
  { name: "Grandmaster", range: "2300+", color: "text-[hsl(0,84%,65%)] border-[hsl(0,84%,40%)] bg-[hsl(0,84%,8%)]" },
];

export default function Home() {
  return (
    <div className="min-h-screen">
      <Navbar />

      {/* Hero Section */}
      <section className="relative flex min-h-screen items-center justify-center overflow-hidden pt-16">
        {/* Radial gradient glow */}
        <div
          className="pointer-events-none absolute inset-0"
          style={{
            background:
              "radial-gradient(ellipse 60% 40% at 50% 45%, hsl(160 84% 39% / 0.12) 0%, transparent 70%)",
          }}
        />

        <div className="relative z-10 mx-auto max-w-4xl px-4 text-center">
          <h1 className="text-5xl font-bold tracking-tight sm:text-7xl lg:text-8xl">
            Solve. Battle.{" "}
            <span className="text-primary">Dominate.</span>
          </h1>

          <p className="mx-auto mt-6 max-w-2xl text-lg text-muted-foreground sm:text-xl">
            Challenge opponents in real-time coding duels. Climb the ELO ladder
            from Newbie to Grandmaster.
          </p>

          <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
            <Link href="/lobby">
              <Button size="lg" className="w-full px-8 text-base sm:w-auto">
                Enter Arena
              </Button>
            </Link>
            <Link href="/leaderboard">
              <Button
                variant="outline"
                size="lg"
                className="w-full px-8 text-base sm:w-auto"
              >
                Leaderboard
              </Button>
            </Link>
          </div>

          {/* Live stats */}
          <div className="mt-12 flex flex-col items-center justify-center gap-2 text-sm text-muted-foreground sm:flex-row sm:gap-6">
            <span>
              <span className="font-semibold text-foreground">12,847</span>{" "}
              Active Players
            </span>
            <span className="hidden sm:inline text-border">|</span>
            <span>
              <span className="font-semibold text-foreground">1,024</span>{" "}
              Problems
            </span>
            <span className="hidden sm:inline text-border">|</span>
            <span>
              <span className="font-semibold text-primary">347</span>{" "}
              Live Duels
            </span>
          </div>
        </div>
      </section>

      {/* Features Grid */}
      <section className="mx-auto max-w-6xl px-4 pb-24 pt-8">
        <h2 className="mb-12 text-center text-3xl font-bold tracking-tight sm:text-4xl">
          Everything you need to{" "}
          <span className="text-primary">compete</span>
        </h2>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {features.map(({ icon: Icon, title, description }) => (
            <div
              key={title}
              className="group rounded-xl border border-border bg-card p-6 transition-colors duration-200 hover:border-primary/40"
            >
              <div className="mb-4 flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                <Icon className="h-5 w-5 text-primary" />
              </div>
              <h3 className="mb-2 font-semibold">{title}</h3>
              <p className="text-sm text-muted-foreground">{description}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Rank Preview Section */}
      <section className="mx-auto max-w-6xl px-4 pb-24">
        <h2 className="mb-4 text-center text-3xl font-bold tracking-tight sm:text-4xl">
          Climb the <span className="text-primary">Ranks</span>
        </h2>
        <p className="mb-12 text-center text-muted-foreground">
          Seven tiers stand between you and Grandmaster glory.
        </p>

        <div className="flex flex-wrap justify-center gap-3">
          {ranks.map(({ name, range, color }) => (
            <div
              key={name}
              className={`rounded-lg border px-4 py-3 text-center ${color}`}
            >
              <div className="text-sm font-semibold">{name}</div>
              <div className="mt-0.5 text-xs opacity-75">{range}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-border py-8 text-center text-sm text-muted-foreground">
        © 2026 Deadlock. Built for competitive programmers.
      </footer>
    </div>
  );
}
