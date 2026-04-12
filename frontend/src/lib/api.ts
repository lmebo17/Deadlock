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
