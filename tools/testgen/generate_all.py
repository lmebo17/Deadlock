#!/usr/bin/env python3
"""Generate and verify test cases for all problems."""
import subprocess, sys, os, json, shutil, time

SEED_DIR = os.path.join(os.path.dirname(__file__), "..", "..", "backend", "src", "main", "resources", "seed")


def run_script(script, input_data=None, args=None, timeout=30):
    cmd = [sys.executable, script] + (args or [])
    result = subprocess.run(cmd, input=input_data, capture_output=True, text=True, timeout=timeout)
    if result.returncode != 0:
        raise RuntimeError(f"{script} failed: {result.stderr[:200]}")
    return result.stdout


def process_problem(slug, base_dir):
    problem_dir = os.path.join(base_dir, "problems", slug)
    meta = json.load(open(os.path.join(problem_dir, "meta.json")))
    generator = os.path.join(problem_dir, "generator.py")
    solution = os.path.join(problem_dir, "solution.py")
    brute = os.path.join(problem_dir, "brute.py")

    test_count = meta.get("testCaseCount", 40)

    print(f"  Generating {test_count} test cases...")

    inputs = []
    outputs = []

    for i in range(test_count):
        inp = run_script(generator, args=[str(i)])
        out = run_script(solution, input_data=inp)
        inputs.append(inp)
        outputs.append(out)

    # Cross-validate with brute force if available (only on small tests)
    if os.path.exists(brute):
        print(f"  Cross-validating with brute.py (small tests only)...")
        validated = 0
        for i in range(min(test_count, 10)):  # only first 10 tests (smaller inputs)
            try:
                brute_out = run_script(brute, input_data=inputs[i], timeout=10)
                if brute_out.strip() != outputs[i].strip():
                    raise RuntimeError(
                        f"Mismatch on test {i+1}!\n"
                        f"  Input: {inputs[i][:100].strip()}\n"
                        f"  Solution: {outputs[i][:100].strip()}\n"
                        f"  Brute:    {brute_out[:100].strip()}")
                validated += 1
            except subprocess.TimeoutExpired:
                print(f"    Test {i+1} too slow for brute, skipping remaining")
                break
        print(f"  Cross-validated {validated} tests")

    # Write test files
    test_dir = os.path.join(SEED_DIR, "problems", slug, "tests")
    os.makedirs(test_dir, exist_ok=True)
    for i, (inp, out) in enumerate(zip(inputs, outputs)):
        idx = f"{i+1:02d}"
        with open(os.path.join(test_dir, f"{idx}-input.txt"), "w") as f:
            f.write(inp)
        with open(os.path.join(test_dir, f"{idx}-output.txt"), "w") as f:
            f.write(out)

    meta["testCaseCount"] = test_count
    return meta


def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    problems_dir = os.path.join(base_dir, "problems")
    slugs = sorted([d for d in os.listdir(problems_dir)
                    if os.path.isdir(os.path.join(problems_dir, d))])

    print(f"Found {len(slugs)} problems\n")

    all_meta = []
    errors = []
    start = time.time()
    for slug in slugs:
        print(f"[{len(all_meta)+1}/{len(slugs)}] {slug}")
        try:
            meta = process_problem(slug, base_dir)
            all_meta.append(meta)
            print(f"  OK\n")
        except Exception as e:
            print(f"  ERROR: {e}\n")
            errors.append((slug, str(e)))

    # Write problems.json
    os.makedirs(SEED_DIR, exist_ok=True)
    with open(os.path.join(SEED_DIR, "problems.json"), "w") as f:
        json.dump(all_meta, f, indent=2)

    elapsed = time.time() - start
    print(f"\n{'='*50}")
    print(f"Done: {len(all_meta)} problems, {len(errors)} errors")
    print(f"Time: {elapsed:.1f}s")
    if errors:
        print("Errors:")
        for slug, err in errors:
            print(f"  {slug}: {err}")


if __name__ == "__main__":
    main()
