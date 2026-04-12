"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Navbar } from "@/components/Navbar";
import { DifficultyBadge } from "@/components/DifficultyBadge";
import { Button } from "@/components/ui/button";
import { getProblems, ProblemResponse, PageResponse } from "@/lib/api";

const TIERS = [
  { label: "All", min: 0, max: 4000 },
  { label: "Beginner", min: 800, max: 1000 },
  { label: "Easy", min: 1100, max: 1400 },
  { label: "Medium", min: 1500, max: 1800 },
  { label: "Hard", min: 1900, max: 2200 },
  { label: "Expert", min: 2300, max: 4000 },
];

export default function ProblemsPage() {
  const [problems, setProblems] = useState<PageResponse<ProblemResponse> | null>(null);
  const [page, setPage] = useState(0);
  const [selectedTier, setSelectedTier] = useState(0);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    const tier = TIERS[selectedTier];
    getProblems(page, 20, tier.min, tier.max, search)
      .then(setProblems)
      .catch(() => setProblems(null))
      .finally(() => setLoading(false));
  }, [page, selectedTier, search]);

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 pt-24 pb-16">
        <h1 className="mb-2 text-4xl font-bold">Problems</h1>
        <p className="mb-8 text-muted-foreground">Browse algorithmic challenges by difficulty</p>

        {/* Filters */}
        <div className="mb-6 flex flex-wrap items-center gap-3">
          {TIERS.map((tier, i) => (
            <button
              key={tier.label}
              onClick={() => { setSelectedTier(i); setPage(0); }}
              className={`rounded-lg border px-4 py-2 text-sm font-medium transition-all ${
                selectedTier === i
                  ? "border-primary bg-primary/10 text-primary"
                  : "border-border text-muted-foreground hover:text-foreground"
              }`}
            >
              {tier.label}
            </button>
          ))}
          <input
            type="text"
            placeholder="Search by title..."
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(0); }}
            className="ml-auto rounded-lg border border-border bg-card px-4 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>

        {/* Table */}
        <div className="rounded-xl border border-border bg-card overflow-hidden">
          <div className="grid grid-cols-[60px_1fr_160px] gap-4 border-b border-border bg-secondary/50 px-6 py-3 text-sm font-medium text-muted-foreground">
            <div>#</div>
            <div>Title</div>
            <div>Difficulty</div>
          </div>

          {loading ? (
            <div className="px-6 py-12 text-center text-muted-foreground">Loading...</div>
          ) : problems && problems.content.length > 0 ? (
            problems.content.map((p, i) => (
              <Link
                key={p.id}
                href={`/problems/${p.slug}`}
                className="grid grid-cols-[60px_1fr_160px] gap-4 items-center border-b border-border px-6 py-4 transition-colors hover:bg-secondary/30 last:border-0"
              >
                <div className="font-mono text-muted-foreground">{problems.number * problems.size + i + 1}</div>
                <div className="font-medium">{p.title}</div>
                <div><DifficultyBadge rating={p.rating} tierLabel={p.tierLabel} /></div>
              </Link>
            ))
          ) : (
            <div className="px-6 py-12 text-center text-muted-foreground">No problems found</div>
          )}
        </div>

        {/* Pagination */}
        {problems && problems.totalPages > 1 && (
          <div className="mt-6 flex items-center justify-center gap-2">
            <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>
              Previous
            </Button>
            <span className="text-sm text-muted-foreground">
              Page {page + 1} of {problems.totalPages}
            </span>
            <Button variant="outline" size="sm" disabled={page >= problems.totalPages - 1} onClick={() => setPage(p => p + 1)}>
              Next
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
