"use client";

import { useState } from "react";

export interface EloPoint {
  at: string;
  elo: number;
}

interface EloChartProps {
  points: EloPoint[];
  startingElo?: number;
}

export function EloChart({ points, startingElo = 1200 }: EloChartProps) {
  const [hover, setHover] = useState<{ i: number; x: number; y: number } | null>(null);

  if (points.length === 0) {
    return (
      <div className="rounded-xl border border-border bg-card p-6">
        <div className="font-semibold mb-1">ELO history</div>
        <div className="text-sm text-muted-foreground">No rated matches yet.</div>
      </div>
    );
  }

  // Prepend a starting point so a brand-new player still sees a line
  const series = [
    { at: "", elo: startingElo, isStart: true },
    ...points.map((p) => ({ ...p, isStart: false })),
  ];

  const w = 700;
  const h = 200;
  const padX = 40;
  const padY = 20;
  const innerW = w - padX * 2;
  const innerH = h - padY * 2;

  const elos = series.map((p) => p.elo);
  const minElo = Math.min(...elos);
  const maxElo = Math.max(...elos);
  const range = Math.max(maxElo - minElo, 100); // avoid divide-by-zero & flat lines

  const x = (i: number) =>
    series.length === 1 ? padX + innerW / 2 : padX + (i / (series.length - 1)) * innerW;
  const y = (elo: number) => padY + innerH - ((elo - minElo) / range) * innerH;

  const path = series
    .map((p, i) => `${i === 0 ? "M" : "L"} ${x(i).toFixed(1)} ${y(p.elo).toFixed(1)}`)
    .join(" ");

  const areaPath = `${path} L ${x(series.length - 1).toFixed(1)} ${(padY + innerH).toFixed(1)} L ${x(0).toFixed(1)} ${(padY + innerH).toFixed(1)} Z`;

  const current = points[points.length - 1].elo;
  const peak = Math.max(...elos);
  const startEloShown = series[0].elo;
  const change = current - startEloShown;

  return (
    <div className="rounded-xl border border-border bg-card p-6">
      <div className="flex items-baseline justify-between mb-4">
        <div>
          <div className="text-sm text-muted-foreground">ELO history</div>
          <div className="text-2xl font-mono font-bold">{current}</div>
        </div>
        <div className="text-right text-xs">
          <div className={change > 0 ? "text-rank-pupil" : change < 0 ? "text-destructive" : "text-muted-foreground"}>
            {change > 0 ? "+" : ""}{change} all-time
          </div>
          <div className="text-muted-foreground">peak {peak}</div>
        </div>
      </div>

      <div className="relative">
        <svg
          viewBox={`0 0 ${w} ${h}`}
          className="w-full h-auto"
          preserveAspectRatio="none"
          onMouseLeave={() => setHover(null)}
        >
          {/* gridlines */}
          {[0, 0.25, 0.5, 0.75, 1].map((t, i) => (
            <line
              key={i}
              x1={padX}
              x2={w - padX}
              y1={padY + innerH * t}
              y2={padY + innerH * t}
              stroke="currentColor"
              strokeOpacity={0.08}
              strokeDasharray="2 4"
            />
          ))}

          <path d={areaPath} fill="currentColor" fillOpacity={0.08} className="text-primary" />
          <path d={path} stroke="currentColor" strokeWidth={2} fill="none" className="text-primary" />

          {series.map((p, i) =>
            p.isStart ? null : (
              <circle
                key={i}
                cx={x(i)}
                cy={y(p.elo)}
                r={3}
                fill="currentColor"
                className="text-primary cursor-pointer"
                onMouseEnter={() => setHover({ i, x: x(i), y: y(p.elo) })}
              />
            )
          )}

          {/* y-axis labels */}
          <text x={padX - 8} y={padY + 4} fontSize={10} textAnchor="end" fill="currentColor" className="text-muted-foreground">
            {maxElo}
          </text>
          <text x={padX - 8} y={padY + innerH + 4} fontSize={10} textAnchor="end" fill="currentColor" className="text-muted-foreground">
            {minElo}
          </text>
        </svg>

        {hover && !series[hover.i].isStart && (
          <div
            className="pointer-events-none absolute z-10 rounded-md border border-border bg-popover px-2 py-1 text-xs shadow-md"
            style={{
              left: `${(hover.x / w) * 100}%`,
              top: `${(hover.y / h) * 100}%`,
              transform: "translate(-50%, -130%)",
            }}
          >
            <div className="font-mono font-semibold">{series[hover.i].elo}</div>
            <div className="text-muted-foreground">
              {new Date(series[hover.i].at).toLocaleDateString()}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
