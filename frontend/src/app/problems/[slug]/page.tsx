"use client";

import { useEffect, useState, useRef } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import dynamic from "next/dynamic";
import { Navbar } from "@/components/Navbar";
import { MarkdownRenderer } from "@/components/MarkdownRenderer";
import { DifficultyBadge } from "@/components/DifficultyBadge";
import { getProblemBySlug, ProblemDetailResponse, submitCode, getSubmission, SubmissionResponse, getMySubmissions, getStarterCode, runCode, RunResult } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { Button } from "@/components/ui/button";

const CodeEditor = dynamic(
  () => import("@/components/CodeEditor").then((m) => ({ default: m.CodeEditor })),
  { ssr: false }
);

export default function ProblemDetailPage() {
  const params = useParams();
  const slug = params.slug as string;
  const { isAuthenticated } = useAuth();
  const [problem, setProblem] = useState<ProblemDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [submissions, setSubmissions] = useState<SubmissionResponse[]>([]);
  const [verdictFilter, setVerdictFilter] = useState<string>("ALL");
  const [languageFilter, setLanguageFilter] = useState<string>("ALL");

  const refreshHistory = async () => {
    if (isAuthenticated) {
      try {
        const subs = await getMySubmissions(slug);
        setSubmissions(subs);
      } catch { /* ignore if not authenticated */ }
    }
  };

  useEffect(() => {
    if (!slug) return;
    getProblemBySlug(slug)
      .then(setProblem)
      .catch(() => setProblem(null))
      .finally(() => setLoading(false));
  }, [slug]);

  useEffect(() => { refreshHistory(); }, [slug, isAuthenticated]);

  if (loading) {
    return (
      <div className="min-h-screen">
        <Navbar />
        <div className="container mx-auto px-4 pt-24">
          <div className="text-muted-foreground">Loading...</div>
        </div>
      </div>
    );
  }

  if (!problem) {
    return (
      <div className="min-h-screen">
        <Navbar />
        <div className="container mx-auto px-4 pt-24">
          <div className="text-destructive">Problem not found</div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="container mx-auto px-4 pt-24 pb-16 max-w-4xl">
        <Link href="/problems" className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground mb-6">
          &larr; Back to Problems
        </Link>
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-4 mb-2">
            <h1 className="text-3xl font-bold">{problem.title}</h1>
            <DifficultyBadge rating={problem.rating} tierLabel={problem.tierLabel} />
          </div>
          <div className="flex gap-4 text-sm text-muted-foreground">
            <span>Time limit: {problem.timeLimitMs}ms</span>
            <span>Memory limit: {problem.memoryLimitMb}MB</span>
          </div>
        </div>

        {/* Description */}
        <section className="mb-8">
          <MarkdownRenderer content={problem.description} />
        </section>

        {/* Input Format */}
        <section className="mb-6">
          <h2 className="text-xl font-semibold mb-2">Input</h2>
          <MarkdownRenderer content={problem.inputFormat} />
        </section>

        {/* Output Format */}
        <section className="mb-6">
          <h2 className="text-xl font-semibold mb-2">Output</h2>
          <MarkdownRenderer content={problem.outputFormat} />
        </section>

        {/* Constraints */}
        <section className="mb-8">
          <h2 className="text-xl font-semibold mb-2">Constraints</h2>
          <MarkdownRenderer content={problem.constraints} />
        </section>

        {/* Sample Test Cases */}
        <section>
          <h2 className="text-xl font-semibold mb-4">Examples</h2>
          {problem.sampleTestCases.map((tc) => (
            <div key={tc.index} className="mb-6 grid grid-cols-2 gap-4">
              <div>
                <div className="mb-1 text-sm font-medium text-muted-foreground">Input</div>
                <pre className="rounded-lg border border-border bg-secondary/50 p-4 font-mono text-sm overflow-x-auto">
                  {tc.input}
                </pre>
              </div>
              <div>
                <div className="mb-1 text-sm font-medium text-muted-foreground">Output</div>
                <pre className="rounded-lg border border-border bg-secondary/50 p-4 font-mono text-sm overflow-x-auto">
                  {tc.output}
                </pre>
              </div>
            </div>
          ))}
        </section>

        {/* Submission History */}
        {isAuthenticated && (() => {
          const filtered = submissions.filter((sub) => {
            const v = sub.verdict || sub.status;
            if (verdictFilter !== "ALL" && v !== verdictFilter) return false;
            if (languageFilter !== "ALL" && sub.language !== languageFilter) return false;
            return true;
          });
          return (
            <section className="mt-8 rounded-xl border border-border bg-card overflow-hidden">
              <div className="border-b border-border px-6 py-4 flex flex-wrap items-center justify-between gap-3">
                <h2 className="text-lg font-semibold">My Submissions</h2>
                {submissions.length > 0 && (
                  <div className="flex items-center gap-2">
                    <select
                      value={verdictFilter}
                      onChange={(e) => setVerdictFilter(e.target.value)}
                      className="rounded-md border border-border bg-secondary px-2 py-1 text-xs"
                    >
                      <option value="ALL">All verdicts</option>
                      <option value="ACCEPTED">Accepted</option>
                      <option value="WRONG_ANSWER">Wrong answer</option>
                      <option value="TLE">TLE</option>
                      <option value="MLE">MLE</option>
                      <option value="RUNTIME_ERROR">Runtime error</option>
                      <option value="COMPILE_ERROR">Compile error</option>
                      <option value="JUDGING">Judging</option>
                      <option value="PENDING">Pending</option>
                    </select>
                    <select
                      value={languageFilter}
                      onChange={(e) => setLanguageFilter(e.target.value)}
                      className="rounded-md border border-border bg-secondary px-2 py-1 text-xs"
                    >
                      <option value="ALL">All languages</option>
                      <option value="PYTHON">Python</option>
                      <option value="JAVA">Java</option>
                      <option value="CPP">C++</option>
                    </select>
                  </div>
                )}
              </div>
              {submissions.length === 0 ? (
                <div className="px-6 py-12 text-center">
                  <div className="text-3xl mb-2 text-muted-foreground">↳</div>
                  <div className="font-semibold mb-1">No submissions yet</div>
                  <div className="text-sm text-muted-foreground">Submit a solution below to see your attempts here.</div>
                </div>
              ) : filtered.length === 0 ? (
                <div className="px-6 py-10 text-center">
                  <div className="font-semibold mb-1">No submissions match these filters</div>
                  <div className="text-sm text-muted-foreground">Try changing the verdict or language filter.</div>
                </div>
              ) : (
                <div>
                  <div className="grid grid-cols-[40px_120px_80px_80px_1fr] gap-4 border-b border-border bg-secondary/50 px-6 py-2 text-xs font-medium text-muted-foreground">
                    <div>#</div><div>Verdict</div><div>Lang</div><div>Time</div><div>Submitted</div>
                  </div>
                  {filtered.map((sub) => {
                    const verdictColor = sub.verdict === "ACCEPTED" ? "text-rank-pupil"
                      : sub.status === "COMPLETED" ? "text-destructive" : "text-muted-foreground";
                    const originalIndex = submissions.findIndex((s) => s.id === sub.id);
                    return (
                      <div key={sub.id} className="grid grid-cols-[40px_120px_80px_80px_1fr] gap-4 items-center border-b border-border px-6 py-3 text-sm last:border-0">
                        <div className="font-mono text-muted-foreground">{submissions.length - originalIndex}</div>
                        <div className={`font-mono font-semibold ${verdictColor}`}>{sub.verdict || sub.status}</div>
                        <div className="text-muted-foreground">{sub.language}</div>
                        <div className="text-muted-foreground">{sub.executionTimeMs ? `${sub.executionTimeMs}ms` : "-"}</div>
                        <div className="text-muted-foreground text-xs">{new Date(sub.submittedAt).toLocaleString()}</div>
                      </div>
                    );
                  })}
                </div>
              )}
            </section>
          );
        })()}

        {/* Submit Section */}
        <section className="mt-8 rounded-xl border border-border bg-card p-6">
          <h2 className="text-xl font-semibold mb-4">Submit Solution</h2>
          {!isAuthenticated ? (
            <p className="text-muted-foreground">Login to submit solutions</p>
          ) : (
            <SubmitForm slug={slug} onJudged={refreshHistory} />
          )}
        </section>
      </div>
    </div>
  );
}

function SubmitForm({ slug, onJudged }: { slug: string; onJudged: () => void }) {
  const [language, setLanguage] = useState("PYTHON");
  const [code, setCode] = useState("");
  const [previousStarter, setPreviousStarter] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [running, setRunning] = useState(false);
  const [result, setResult] = useState<SubmissionResponse | null>(null);
  const [runResult, setRunResult] = useState<RunResult | null>(null);
  const submittingRef = useRef(false);
  const runningRef = useRef(false);

  useEffect(() => {
      getStarterCode(slug, language)
          .then(({ code: starter }) => {
              if (!code || code === previousStarter) {
                  setCode(starter);
              }
              setPreviousStarter(starter);
          })
          .catch(() => {});
  }, [slug, language]);

  const handleSubmit = async () => {
    if (!code.trim() || submittingRef.current) return;
    submittingRef.current = true;
    setSubmitting(true);
    setResult(null);
    setRunResult(null);
    try {
      const { id } = await submitCode(slug, language, code);
      const poll = async () => {
        const sub = await getSubmission(id);
        if (sub.status === "COMPLETED") {
          setResult(sub);
          setSubmitting(false);
          submittingRef.current = false;
          onJudged();
        } else {
          setTimeout(poll, 1500);
        }
      };
      poll();
    } catch {
      setSubmitting(false);
      submittingRef.current = false;
    }
  };

  const handleRun = async () => {
    if (!code.trim() || runningRef.current || submittingRef.current) return;
    runningRef.current = true;
    setRunning(true);
    setRunResult(null);
    try {
      const r = await runCode(slug, language, code);
      setRunResult(r);
    } catch {
      // ignore
    } finally {
      setRunning(false);
      runningRef.current = false;
    }
  };

  const verdictColor = result?.verdict === "ACCEPTED" ? "text-rank-pupil"
    : result?.verdict ? "text-destructive" : "";

  return (
    <div className="space-y-4">
      <div className="flex gap-3 items-center">
        <select
          value={language}
          onChange={(e) => setLanguage(e.target.value)}
          className="rounded-lg border border-border bg-secondary px-3 py-2 text-sm"
        >
          <option value="PYTHON">Python</option>
          <option value="JAVA">Java</option>
          <option value="CPP">C++</option>
        </select>
      </div>
      <CodeEditor language={language} value={code} onChange={setCode} onSubmit={handleSubmit} onRun={handleRun} />
      <div className="flex items-center gap-3 flex-wrap">
        <Button onClick={handleSubmit} disabled={submitting || running || !code.trim()}>
          {submitting ? "Judging..." : "Submit"}
        </Button>
        <Button variant="outline" onClick={handleRun} disabled={running || submitting || !code.trim()}>
          {running ? "Running..." : "Run samples"}
        </Button>
        <span className="text-xs text-muted-foreground">
          ⌘/Ctrl+Enter submit · ⌘/Ctrl+Shift+Enter run
        </span>
        {result && (
          <div className="flex items-center gap-2">
            <span className={`font-mono font-bold ${verdictColor}`}>{result.verdict}</span>
            {result.failedTestCase && (
              <span className="text-sm text-muted-foreground">on test {result.failedTestCase}</span>
            )}
            {result.executionTimeMs && (
              <span className="text-sm text-muted-foreground">{result.executionTimeMs}ms</span>
            )}
          </div>
        )}
      </div>
      {runResult && <RunResultPanel result={runResult} />}
    </div>
  );
}

function RunResultPanel({ result }: { result: RunResult }) {
  if (result.compileError) {
    return (
      <div className="rounded-lg border border-destructive/40 bg-destructive/5 p-4">
        <div className="font-semibold text-destructive mb-2">Compile error</div>
        <pre className="font-mono text-xs whitespace-pre-wrap text-destructive">
          {result.compileErrorMessage}
        </pre>
      </div>
    );
  }
  const passed = result.tests.filter((t) => t.passed).length;
  const total = result.tests.length;
  const allPassed = passed === total && total > 0;

  return (
    <div className="rounded-lg border border-border bg-card p-4">
      <div className={`text-sm font-semibold mb-3 ${allPassed ? "text-rank-pupil" : "text-destructive"}`}>
        {passed} / {total} sample{total !== 1 ? "s" : ""} passed
        <span className="ml-2 text-xs font-normal text-muted-foreground">
          {result.totalExecutionTimeMs}ms
        </span>
      </div>
      <div className="space-y-3">
        {result.tests.map((t) => {
          const status = t.passed ? "Pass" : t.timedOut ? "TLE" : t.runtimeError ? "Runtime error" : "Wrong answer";
          const color = t.passed ? "text-rank-pupil" : "text-destructive";
          return (
            <details key={t.index} className="rounded border border-border" open={!t.passed}>
              <summary className="cursor-pointer px-3 py-2 text-sm flex items-center gap-3">
                <span className={`font-mono font-bold ${color}`}>{status}</span>
                <span className="text-muted-foreground">Sample {t.index}</span>
              </summary>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-2 px-3 pb-3 text-xs">
                <Block label="Input" content={t.input} />
                <Block label="Expected" content={t.expected} />
                <Block label="Your output" content={t.timedOut ? "(timed out)" : t.actual} />
              </div>
              {t.stderr && t.stderr.trim() && (
                <div className="px-3 pb-3">
                  <Block label="stderr" content={t.stderr} variant="error" />
                </div>
              )}
            </details>
          );
        })}
      </div>
    </div>
  );
}

function Block({ label, content, variant }: { label: string; content: string; variant?: "error" }) {
  return (
    <div>
      <div className="text-muted-foreground mb-1">{label}</div>
      <pre className={`rounded border border-border p-2 font-mono whitespace-pre-wrap break-words max-h-40 overflow-y-auto ${
        variant === "error" ? "bg-destructive/10 text-destructive" : "bg-secondary/50"
      }`}>
        {content}
      </pre>
    </div>
  );
}
