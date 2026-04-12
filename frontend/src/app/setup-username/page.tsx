"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { apiFetch } from "@/lib/api";
import { Button } from "@/components/ui/button";

export default function SetupUsername() {
  const router = useRouter();
  const { user, refreshUser } = useAuth();
  const [username, setUsername] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setSubmitting(true);

    try {
      await apiFetch("/api/auth/username", {
        method: "POST",
        body: JSON.stringify({ username }),
      });
      await refreshUser();
      router.push("/lobby");
    } catch (err) {
      setError("Username is already taken or invalid. Use 3-20 characters, letters, numbers, and underscores.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center">
      <div className="mx-auto w-full max-w-md space-y-8 p-8">
        <div className="text-center">
          <h1 className="text-3xl font-bold">Choose Your Username</h1>
          <p className="mt-2 text-muted-foreground">
            This is how other players will see you on the leaderboard.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter username"
              minLength={3}
              maxLength={20}
              pattern="^[a-zA-Z0-9_]+$"
              required
              className="w-full rounded-lg border border-border bg-card px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>

          {error && (
            <p className="text-sm text-destructive">{error}</p>
          )}

          <Button type="submit" className="w-full" disabled={submitting || username.length < 3}>
            {submitting ? "Setting up..." : "Continue"}
          </Button>
        </form>
      </div>
    </div>
  );
}
