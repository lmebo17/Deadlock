export function DifficultyBadge({ rating, tierLabel }: { rating: number; tierLabel: string }) {
  const colorClass =
    rating <= 1000 ? "text-rank-newbie" :
    rating <= 1400 ? "text-rank-pupil" :
    rating <= 1800 ? "text-rank-specialist" :
    rating <= 2200 ? "text-rank-expert" :
    "text-rank-master";

  return (
    <span className={`inline-flex items-center gap-1.5 rounded-md border border-border bg-secondary px-2 py-0.5 font-mono text-sm font-semibold ${colorClass}`}>
      {tierLabel} ({rating})
    </span>
  );
}
