#!/usr/bin/env python3
"""
RAG QA 검색 품질(searchNotes) 평가 — recall@k/precision@k.

RagQaService.searchNotesEval()/POST /api/admin/stats/search-eval을 호출해 실제 임베딩 검색을
수행하되(embed 호출 1번만 발생, 답변 생성은 안 함) 매치된 refId/sourceType만 dataset.json의
정답 집합과 직접 비교한다. LLM-judge 없이 집합 연산으로 채점하는 결정론적 평가라 재현 가능하다.

정답이 있는 케이스(ORDER_NOTE/PRODUCTION_MEMO/CLIENT_MEMO/ALL)는 recall@k, precision@k를 계산하고,
정답이 없는 negative 케이스(expected == {})는 애초에 "correct/total" 계산이 성립하지 않아
(0/N을 정답률로 잘못 해석하기 쉬움) 별도 섹션으로 분리해 top-1 distance만 참고용으로 기록한다.

사용법:
    python3 run_eval.py [--base-url http://localhost:8080] [--dataset dataset.json]
"""
import argparse
import http.cookiejar
import json
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

DEFAULT_BASE_URL = "http://localhost:8080"
DEFAULT_ADMIN_EMAIL = "admin@prodio.com"
DEFAULT_ADMIN_PASSWORD = "admin1234"
TOP_K = 5


class ApiClient:
    def __init__(self, base_url: str):
        self.base_url = base_url
        self.cj = http.cookiejar.CookieJar()
        self.opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(self.cj))
        self.csrf_header = None
        self.csrf_token = None

    def _raw(self, method: str, path: str, body=None, headers=None):
        data = json.dumps(body).encode("utf-8") if body is not None else None
        req = urllib.request.Request(self.base_url + path, data=data, method=method)
        req.add_header("Content-Type", "application/json")
        for k, v in (headers or {}).items():
            req.add_header(k, v)
        try:
            with self.opener.open(req) as resp:
                raw = resp.read()
                return resp.status, (json.loads(raw) if raw else None)
        except urllib.error.HTTPError as e:
            raw = e.read()
            print(f"!! {method} {path} -> {e.code}: {raw[:500]}", file=sys.stderr)
            raise

    def _call(self, method: str, path: str, body=None):
        return self._raw(method, path, body, {self.csrf_header: self.csrf_token})

    def login(self, email: str, password: str):
        _, csrf = self._raw("GET", "/auth/csrf")
        self.csrf_header, self.csrf_token = csrf["headerName"], csrf["token"]
        self._call("POST", "/auth/login", {"email": email, "password": password})
        _, csrf = self._raw("GET", "/auth/csrf")
        self.csrf_token = csrf["token"]

    def search(self, query: str, source_type: str, retries: int = 2):
        for attempt in range(retries + 1):
            try:
                _, resp = self._call("POST", "/api/admin/stats/search-eval",
                                      {"query": query, "sourceType": source_type})
                return resp["data"]["matches"]
            except urllib.error.HTTPError as e:
                if e.code in (429, 500) and attempt < retries:
                    wait = 15 * (attempt + 1)
                    print(f"    ({e.code}, {wait}s 대기 후 재시도)", file=sys.stderr)
                    time.sleep(wait)
                    continue
                raise


def expected_pairs(expected: dict) -> set:
    """{"ORDER_NOTE": [1,2]} -> {("ORDER_NOTE",1), ("ORDER_NOTE",2)}. sourceType별로 refId 공간이
    다르므로(ORDER_NOTE의 order_id=5와 CLIENT_MEMO의 client_id=5는 별개) 타입까지 묶어서 비교한다."""
    pairs = set()
    for source_type, ids in expected.items():
        for ref_id in ids:
            pairs.add((source_type, ref_id))
    return pairs


def retrieved_pairs(matches: list) -> list:
    return [(m["sourceType"], m["refId"]) for m in matches]


def score_case(case: dict, matches: list):
    expected = expected_pairs(case["expected"])
    retrieved = retrieved_pairs(matches)
    retrieved_set = set(retrieved)

    hit = expected & retrieved_set
    recall = len(hit) / len(expected) if expected else None
    precision = len(hit) / len(retrieved) if retrieved else None

    return {
        "recall_at_k": recall,
        "precision_at_k": precision,
        "hit_count": len(hit),
        "expected_count": len(expected),
        "retrieved_count": len(retrieved),
        "missed": sorted(expected - retrieved_set),
    }


def run(base_url: str, dataset_path: Path, email: str, password: str):
    dataset = json.loads(dataset_path.read_text())
    client = ApiClient(base_url)
    print(f"로그인 중... ({base_url})")
    client.login(email, password)

    positive_details, negative_details = [], []

    for i, case in enumerate(dataset["cases"]):
        if i > 0:
            time.sleep(1)  # embed 전용 호출이라 generateContent보다 가벼워 pacing을 짧게 둔다.
        is_negative = not case["expected"]

        try:
            matches = client.search(case["question"], case["source_type"])
        except urllib.error.HTTPError as e:
            print(f"    !! {case['id']} 최종 실패({e.code}), 스킵하고 계속 진행", file=sys.stderr)
            matches = None

        if matches is None:
            detail = {"id": case["id"], "cluster": case["cluster"], "question": case["question"], "error": True}
            (negative_details if is_negative else positive_details).append(detail)
            print(f"[ERR] {case['id']:>8}  {case['question']}")
            continue

        if is_negative:
            top1 = matches[0] if matches else None
            detail = {
                "id": case["id"], "question": case["question"],
                "top1_distance": top1["distance"] if top1 else None,
                "retrieved": matches,
            }
            negative_details.append(detail)
            dist_display = f"{detail['top1_distance']:.4f}" if detail["top1_distance"] is not None else "N/A"
            print(f"[NEG] {case['id']:>8}  top1_distance={dist_display}  {case['question']}")
            continue

        scored = score_case(case, matches)
        detail = {
            "id": case["id"], "cluster": case["cluster"], "source_type": case["source_type"],
            "question": case["question"], "expected": case["expected"], "retrieved": matches,
            **scored,
        }
        positive_details.append(detail)
        mark = "OK" if scored["recall_at_k"] == 1.0 else ("PARTIAL" if scored["hit_count"] > 0 else "MISS")
        print(f"[{mark:7}] {case['id']:>8}  recall={scored['recall_at_k']:.2f}  "
              f"precision={scored['precision_at_k']:.2f}  {case['question']}")

    def avg(key, details):
        vals = [d[key] for d in details if key in d and d[key] is not None]
        return sum(vals) / len(vals) if vals else None

    by_type = {}
    for d in positive_details:
        t = d.get("source_type", "?")
        by_type.setdefault(t, []).append(d)

    summary = {
        "overall": {
            "mean_recall_at_k": avg("recall_at_k", positive_details),
            "mean_precision_at_k": avg("precision_at_k", positive_details),
            "cases": len(positive_details),
        },
        "by_source_type": {
            t: {
                "mean_recall_at_k": avg("recall_at_k", ds),
                "mean_precision_at_k": avg("precision_at_k", ds),
                "cases": len(ds),
            }
            for t, ds in by_type.items()
        },
        "zero_recall_ids": [d["id"] for d in positive_details if d.get("recall_at_k") == 0.0],
    }

    result = {
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "base_url": base_url,
        "top_k": TOP_K,
        "summary": summary,
        "positive_cases": positive_details,
        "negative_cases": negative_details,
    }
    return result


def print_summary(result):
    s = result["summary"]
    print("\n=== 전체 요약 ===")
    o = s["overall"]
    print(f"cases={o['cases']}  mean recall@{result['top_k']}={o['mean_recall_at_k']:.3f}  "
          f"mean precision@{result['top_k']}={o['mean_precision_at_k']:.3f}")

    print("\n=== sourceType별 ===")
    for t, m in s["by_source_type"].items():
        print(f"{t:<16} cases={m['cases']:<4} recall@{result['top_k']}={m['mean_recall_at_k']:.3f}  "
              f"precision@{result['top_k']}={m['mean_precision_at_k']:.3f}")

    if s["zero_recall_ids"]:
        print(f"\n=== recall 0.0 (완전 실패) — {len(s['zero_recall_ids'])}건 ===")
        print(", ".join(s["zero_recall_ids"]))

    neg = result["negative_cases"]
    dists = [d["top1_distance"] for d in neg if d.get("top1_distance") is not None]
    if dists:
        print(f"\n=== Negative 케이스 (정답 없음, {len(neg)}건) — top1 distance 참고용 ===")
        print(f"평균 top1 distance: {sum(dists) / len(dists):.4f}  (참고: positive 케이스 hit들의 분포와 비교해서 판단)")


def main():
    parser = argparse.ArgumentParser(description="RAG QA 검색 품질 평가")
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL)
    parser.add_argument("--dataset", default=str(Path(__file__).parent / "dataset.json"))
    parser.add_argument("--email", default=DEFAULT_ADMIN_EMAIL)
    parser.add_argument("--password", default=DEFAULT_ADMIN_PASSWORD)
    parser.add_argument("--out", default=None, help="결과 JSON 저장 경로 (기본: results/<timestamp>.json)")
    args = parser.parse_args()

    result = run(args.base_url, Path(args.dataset), args.email, args.password)
    print_summary(result)

    out_path = Path(args.out) if args.out else Path(__file__).parent / "results" / f"{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text(json.dumps(result, ensure_ascii=False, indent=2))
    print(f"\n결과 저장: {out_path}")


if __name__ == "__main__":
    main()
