"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { Navbar } from "@/components/Navbar";
import { MarkdownRenderer } from "@/components/MarkdownRenderer";
import { DifficultyBadge } from "@/components/DifficultyBadge";
import { getProblemBySlug, ProblemDetailResponse, submitCode, getSubmission, SubmissionResponse } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { Button } from "@/components/ui/button";

export default function ProblemDetailPage() {
  const params = useParams();
  const slug = params.slug as string;
  const { isAuthenticated } = useAuth();
  const [problem, setProblem] = useState<ProblemDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!slug) return;
    getProblemBySlug(slug)
      .then(setProblem)
      .catch(() => setProblem(null))
      .finally(() => setLoading(false));
  }, [slug]);

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

        {/* Submit Section */}
        <section className="mt-8 rounded-xl border border-border bg-card p-6">
          <h2 className="text-xl font-semibold mb-4">Submit Solution</h2>
          {!isAuthenticated ? (
            <p className="text-muted-foreground">Login to submit solutions</p>
          ) : (
            <SubmitForm slug={slug} />
          )}
        </section>
      </div>
    </div>
  );
}

function SubmitForm({ slug }: { slug: string }) {
  const [language, setLanguage] = useState("PYTHON");
  const [code, setCode] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<SubmissionResponse | null>(null);

  const handleSubmit = async () => {
    if (!code.trim()) return;
    setSubmitting(true);
    setResult(null);
    try {
      const { id } = await submitCode(slug, language, code);
      const poll = async () => {
        const sub = await getSubmission(id);
        if (sub.status === "COMPLETED") {
          setResult(sub);
          setSubmitting(false);
        } else {
          setTimeout(poll, 1500);
        }
      };
      poll();
    } catch {
      setSubmitting(false);
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
      <textarea
        value={code}
        onChange={(e) => setCode(e.target.value)}
        placeholder="Write your solution here..."
        rows={12}
        className="w-full rounded-lg border border-border bg-background px-4 py-3 font-mono text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
      />
      <div className="flex items-center gap-4">
        <Button onClick={handleSubmit} disabled={submitting || !code.trim()}>
          {submitting ? "Judging..." : "Submit"}
        </Button>
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
    </div>
  );
}
