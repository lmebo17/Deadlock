"use client";

import { useMemo, useState } from "react";

export interface ContributionDay {
  date: string; // YYYY-MM-DD
  count: number;
}

interface ContributionGraphProps {
  data: ContributionDay[];
  weeks?: number;
}

const MONTH_LABELS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

function toLocalDateKey(d: Date): string {
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

function intensity(count: number): string {
  if (count === 0) return "bg-secondary/40";
  if (count <= 2) return "bg-primary/25";
  if (count <= 5) return "bg-primary/50";
  if (count <= 10) return "bg-primary/75";
  return "bg-primary";
}

function computeStreaks(byDay: Map<string, number>, days: Date[]): { current: number; longest: number } {
  let current = 0;
  let longest = 0;
  let run = 0;

  // Walk oldest → newest to find the longest run
  for (const d of days) {
    if ((byDay.get(toLocalDateKey(d)) ?? 0) > 0) {
      run++;
      if (run > longest) longest = run;
    } else {
      run = 0;
    }
  }

  // Current streak: walk newest → backwards from today (allow today empty if no submission yet)
  const today = toLocalDateKey(new Date());
  let cursor = new Date();
  // If today has no submissions, start from yesterday so a streak doesn't reset before the day ends
  if ((byDay.get(today) ?? 0) === 0) {
    cursor.setDate(cursor.getDate() - 1);
  }
  while (true) {
    const key = toLocalDateKey(cursor);
    if ((byDay.get(key) ?? 0) > 0) {
      current++;
      cursor.setDate(cursor.getDate() - 1);
    } else {
      break;
    }
  }

  return { current, longest };
}

export function ContributionGraph({ data, weeks = 53 }: ContributionGraphProps) {
  const [hover, setHover] = useState<{ key: string; count: number; left: number; top: number } | null>(null);

  const byDay = useMemo(() => {
    const m = new Map<string, number>();
    for (const d of data) m.set(d.date, d.count);
    return m;
  }, [data]);

  // Build a grid of weeks × 7 days, anchored so the right column ends today.
  const { columns, allDays, total } = useMemo(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    // Move back to the most recent Sunday so weeks start on Sunday
    const endCol = new Date(today);
    endCol.setDate(endCol.getDate() + (6 - today.getDay())); // pad current week to Saturday for display

    const totalDays = weeks * 7;
    const start = new Date(endCol);
    start.setDate(start.getDate() - (totalDays - 1));

    const days: Date[] = [];
    for (let i = 0; i < totalDays; i++) {
      const d = new Date(start);
      d.setDate(start.getDate() + i);
      days.push(d);
    }
    const cols: Date[][] = [];
    for (let w = 0; w < weeks; w++) {
      cols.push(days.slice(w * 7, w * 7 + 7));
    }
    let sum = 0;
    for (const d of days) {
      if (d > today) continue;
      sum += byDay.get(toLocalDateKey(d)) ?? 0;
    }
    return { columns: cols, allDays: days.filter((d) => d <= today), total: sum };
  }, [weeks, byDay]);

  const streaks = useMemo(() => computeStreaks(byDay, allDays), [byDay, allDays]);
  const today = useMemo(() => {
    const t = new Date();
    t.setHours(0, 0, 0, 0);
    return t;
  }, []);

  // Month labels: pick the first column of each month and place a label
  const monthLabels: { col: number; label: string }[] = [];
  let lastMonth = -1;
  columns.forEach((col, i) => {
    const firstDay = col[0];
    if (firstDay.getMonth() !== lastMonth) {
      monthLabels.push({ col: i, label: MONTH_LABELS[firstDay.getMonth()] });
      lastMonth = firstDay.getMonth();
    }
  });

  return (
    <div className="rounded-xl border border-border bg-card p-6">
      <div className="flex items-baseline justify-between mb-4 flex-wrap gap-2">
        <div>
          <div className="text-sm text-muted-foreground">Activity</div>
          <div className="text-lg font-semibold">{total} submission{total !== 1 ? "s" : ""} in the last year</div>
        </div>
        <div className="flex gap-4 text-xs">
          <div>
            <div className="text-muted-foreground">Current streak</div>
            <div className="font-mono font-bold text-base">
              {streaks.current} <span className="text-xs font-normal text-muted-foreground">day{streaks.current !== 1 ? "s" : ""}</span>
            </div>
          </div>
          <div>
            <div className="text-muted-foreground">Longest streak</div>
            <div className="font-mono font-bold text-base">
              {streaks.longest} <span className="text-xs font-normal text-muted-foreground">day{streaks.longest !== 1 ? "s" : ""}</span>
            </div>
          </div>
        </div>
      </div>

      <div className="relative overflow-x-auto">
        <div className="inline-block min-w-full">
          {/* Month labels */}
          <div className="flex pl-6 mb-1 text-xs text-muted-foreground select-none">
            {columns.map((_, i) => {
              const label = monthLabels.find((m) => m.col === i);
              return (
                <div key={i} className="w-3 mr-0.5 shrink-0">
                  {label ? label.label : ""}
                </div>
              );
            })}
          </div>

          <div className="flex">
            {/* Day-of-week labels */}
            <div className="flex flex-col mr-1 text-xs text-muted-foreground select-none w-5">
              <div className="h-3 mb-0.5"></div>
              <div className="h-3 mb-0.5">Mon</div>
              <div className="h-3 mb-0.5"></div>
              <div className="h-3 mb-0.5">Wed</div>
              <div className="h-3 mb-0.5"></div>
              <div className="h-3 mb-0.5">Fri</div>
              <div className="h-3 mb-0.5"></div>
            </div>

            {/* Grid */}
            <div className="flex">
              {columns.map((col, ci) => (
                <div key={ci} className="flex flex-col mr-0.5">
                  {col.map((d) => {
                    const key = toLocalDateKey(d);
                    const isFuture = d > today;
                    const count = byDay.get(key) ?? 0;
                    return (
                      <div
                        key={key}
                        className={`h-3 w-3 mb-0.5 rounded-sm ${isFuture ? "bg-transparent" : intensity(count)}`}
                        onMouseEnter={(e) => {
                          if (isFuture) return;
                          const rect = (e.target as HTMLElement).getBoundingClientRect();
                          const parent = (e.currentTarget.closest(".relative") as HTMLElement)?.getBoundingClientRect();
                          if (!parent) return;
                          setHover({
                            key,
                            count,
                            left: rect.left - parent.left + rect.width / 2,
                            top: rect.top - parent.top,
                          });
                        }}
                        onMouseLeave={() => setHover(null)}
                      />
                    );
                  })}
                </div>
              ))}
            </div>
          </div>

          {/* Legend */}
          <div className="flex items-center gap-1 mt-3 text-xs text-muted-foreground select-none pl-6">
            <span>Less</span>
            <div className="h-3 w-3 rounded-sm bg-secondary/40"></div>
            <div className="h-3 w-3 rounded-sm bg-primary/25"></div>
            <div className="h-3 w-3 rounded-sm bg-primary/50"></div>
            <div className="h-3 w-3 rounded-sm bg-primary/75"></div>
            <div className="h-3 w-3 rounded-sm bg-primary"></div>
            <span>More</span>
          </div>
        </div>

        {hover && (
          <div
            className="pointer-events-none absolute z-10 rounded-md border border-border bg-popover px-2 py-1 text-xs shadow-md whitespace-nowrap"
            style={{ left: hover.left, top: hover.top, transform: "translate(-50%, -120%)" }}
          >
            <span className="font-mono font-semibold">{hover.count}</span>
            <span className="text-muted-foreground"> submission{hover.count !== 1 ? "s" : ""} on </span>
            <span>{new Date(hover.key).toLocaleDateString()}</span>
          </div>
        )}
      </div>
    </div>
  );
}
