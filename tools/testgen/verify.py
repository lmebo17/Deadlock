#!/usr/bin/env python3
"""Cross-validate solutions against brute-force for problems that have brute.py."""
import subprocess, sys, os, json

SEED_DIR = os.path.join(os.path.dirname(__file__), "..", "..", "backend", "src", "main", "resources", "seed")


def run_script(script, input_data=None, args=None, timeout=60):
    cmd = [sys.executable, script] + (args or [])
    result = subprocess.run(cmd, input=input_data, capture_output=True, text=True, timeout=timeout)
    if result.returncode != 0:
        raise RuntimeError(f"{script} failed: {result.stderr[:200]}")
    return result.stdout


def verify_problem(slug, base_dir, num_tests=20):
    problem_dir = os.path.join(base_dir, "problems", slug)
    meta = json.load(open(os.path.join(problem_dir, "meta.json")))
    solution = os.path.join(problem_dir, "solution.py")
    brute = os.path.join(problem_dir, "brute.py")
    generator = os.path.join(problem_dir, "generator.py")

    if not os.path.exists(brute):
        return None  # no brute force to verify against

    mismatches = 0
    for seed in range(num_tests):
        inp = run_script(generator, args=[str(seed + 1000)])  # use different seeds than generation
        sol_out = run_script(solution, input_data=inp)
        brute_out = run_script(brute, input_data=inp, timeout=60)

        if sol_out.strip() != brute_out.strip():
            mismatches += 1
            print(f"  MISMATCH seed={seed+1000}")
            print(f"    Input:    {inp[:80].strip()}")
            print(f"    Solution: {sol_out[:80].strip()}")
            print(f"    Brute:    {brute_out[:80].strip()}")

    return mismatches


def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    problems_dir = os.path.join(base_dir, "problems")
    slugs = sorted([d for d in os.listdir(problems_dir)
                    if os.path.isdir(os.path.join(problems_dir, d))])

    num_tests = int(sys.argv[1]) if len(sys.argv) > 1 else 20

    print(f"Verifying {len(slugs)} problems with {num_tests} random tests each\n")

    verified = 0
    skipped = 0
    failed = 0

    for slug in slugs:
        brute_path = os.path.join(problems_dir, slug, "brute.py")
        if not os.path.exists(brute_path):
            skipped += 1
            continue

        print(f"[{verified+failed+1}] {slug}")
        try:
            mismatches = verify_problem(slug, base_dir, num_tests)
            if mismatches == 0:
                print(f"  PASSED")
                verified += 1
            else:
                print(f"  FAILED ({mismatches} mismatches)")
                failed += 1
        except Exception as e:
            print(f"  ERROR: {e}")
            failed += 1

    print(f"\n{'='*50}")
    print(f"Verified: {verified}, Failed: {failed}, Skipped (no brute): {skipped}")


if __name__ == "__main__":
    main()
