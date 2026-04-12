const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...options?.headers,
    },
    ...options,
  });

  if (!res.ok) {
    throw new Error(`API error: ${res.status}`);
  }

  return res.json();
}

export interface ProblemResponse {
  id: number;
  title: string;
  slug: string;
  rating: number;
  tierLabel: string;
  timeLimitMs: number;
  memoryLimitMb: number;
  testCaseCount: number;
  sampleCount: number;
}

export interface ProblemDetailResponse extends ProblemResponse {
  description: string;
  inputFormat: string;
  outputFormat: string;
  constraints: string;
  sampleTestCases: { index: number; input: string; output: string }[];
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

export async function getProblems(
  page = 0,
  size = 20,
  minRating = 0,
  maxRating = 4000,
  search = ""
): Promise<PageResponse<ProblemResponse>> {
  return apiFetch(
    `/api/problems?page=${page}&size=${size}&minRating=${minRating}&maxRating=${maxRating}&search=${encodeURIComponent(search)}`
  );
}

export async function getProblemBySlug(slug: string): Promise<ProblemDetailResponse> {
  return apiFetch(`/api/problems/${slug}`);
}

export interface LeaderboardEntry {
  rank: number;
  id: number;
  username: string;
  avatarUrl: string;
  eloRating: number;
  tierLabel: string;
}

export interface UserProfile {
  id: number;
  username: string;
  displayName: string;
  avatarUrl: string;
  eloRating: number;
  tierLabel: string;
  totalMatches: number;
  wins: number;
  losses: number;
  draws: number;
  winRate: number;
  joinedAt: string;
}

export async function getLeaderboard(page = 0, size = 50): Promise<PageResponse<LeaderboardEntry>> {
  return apiFetch(`/api/leaderboard?page=${page}&size=${size}`);
}

export async function getUserProfile(username: string): Promise<UserProfile> {
  return apiFetch(`/api/users/${username}`);
}

export interface SubmissionResponse {
  id: number;
  problemSlug: string;
  language: string;
  status: string;
  verdict: string | null;
  failedTestCase: number | null;
  executionTimeMs: number | null;
  submittedAt: string;
}

export async function submitCode(slug: string, language: string, code: string): Promise<{ id: number }> {
  return apiFetch(`/api/problems/${slug}/submit`, {
    method: "POST",
    body: JSON.stringify({ language, code }),
  });
}

export async function getSubmission(id: number): Promise<SubmissionResponse> {
  return apiFetch(`/api/submissions/${id}`);
}

export async function getMySubmissions(slug: string): Promise<SubmissionResponse[]> {
  return apiFetch(`/api/problems/${slug}/submissions`);
}

export async function getStarterCode(slug: string, language: string): Promise<{ code: string; language: string }> {
  return apiFetch(`/api/problems/${slug}/starter?language=${language}`);
}
