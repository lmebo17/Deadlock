"use client";

import Link from "next/link";
import { useAuth } from "@/lib/auth";
import { LoginButtons } from "./LoginButtons";
import { Button } from "@/components/ui/button";

export function Navbar() {
  const { user, loading, isAuthenticated, logout } = useAuth();

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 border-b border-border bg-background/80 backdrop-blur-xl">
      <div className="container mx-auto flex h-16 items-center justify-between px-4">
        <Link href="/" className="flex items-center gap-2">
          <span className="text-xl font-bold tracking-tight">
            Dead<span className="text-primary">lock</span>
          </span>
        </Link>

        <div className="flex items-center gap-4">
          <Link href="/leaderboard" className="text-sm text-muted-foreground hover:text-foreground">
            Leaderboard
          </Link>
          <Link href="/problems" className="text-sm text-muted-foreground hover:text-foreground">
            Problems
          </Link>

          {!loading && (
            isAuthenticated && user ? (
              <div className="flex items-center gap-3">
                <Link href="/lobby">
                  <Button size="sm" className="gap-2">
                    Find Match
                  </Button>
                </Link>
                <Link href={`/profile/${user.username || ""}`} className="flex items-center gap-2">
                  {user.avatarUrl && (
                    <img src={user.avatarUrl} alt="" className="h-8 w-8 rounded-full" />
                  )}
                  <span className="text-sm font-medium">
                    {user.username || user.displayName}
                  </span>
                </Link>
                <Button variant="ghost" size="sm" onClick={logout}>
                  Logout
                </Button>
              </div>
            ) : (
              <LoginButtons />
            )
          )}
        </div>
      </div>
    </nav>
  );
}
