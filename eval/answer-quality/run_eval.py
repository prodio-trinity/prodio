#!/usr/bin/env python3
"""
RAG QA 답변 품질(3단계) 평가 — LLM-judge.

POST /api/admin/stats/ask를 호출해 실제 라우팅+검색+답변 생성 전체 파이프라인을 태우고,
받은 답변을 dataset.json의 rubric과 함께 Gemini에 판정 프롬프트로 넣어 PASS/FAIL을 받는다.
1·2단계와 달리 판정 자체가 LLM 호출이라 확률적이고(재현이 완벽히 보장되진 않음) 비용도 든다 —
그래서 60건(1단계와 동일 질문)으로 제한했다.

표준 라이브러리만 사용(urllib, json) — pip/npm 설치 불필요. GEMINI_API_KEY는 환경변수로만
읽고 코드에 값이 없다.

사용법:
    export GEMINI_API_KEY='본인 키'
    python3 run_eval.py [--base-url http://localhost:8080] [--dataset dataset.json]
"""
import argparse
import http.cookiejar
import json
import os
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

DEFAULT_BASE_URL = "http://localhost:8080"
DEFAULT_ADMIN_EMAIL = "admin@prodio.com"
DEFAULT_ADMIN_PASSWORD = "admin1234"

GEMINI_BASE_URL = "https://generativelanguage.googleapis.com"
JUDGE_MODEL = "gemini-flash-lite-latest"  # 앱이 실제로 쓰는 채팅 모델과 동일


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
            with self.opener.open(req, timeout=30) as resp:
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

    def ask(self, question: str, retries: int = 2):
        for attempt in range(retries + 1):
            try:
                _, resp = self._call("POST", "/api/admin/stats/ask", {"question": question})
                return resp["data"]["response"]
            except urllib.error.HTTPError as e:
                if e.code in (429, 500) and attempt < retries:
                    wait = 20 * (attempt + 1)
                    print(f"    ({e.code}, {wait}s 대기 후 재시도)", file=sys.stderr)
                    time.sleep(wait)
                    continue
                raise
            except (urllib.error.URLError, TimeoutError) as e:
                if attempt < retries:
                    print(f"    (timeout/연결 오류: {e}, 10s 대기 후 재시도)", file=sys.stderr)
                    time.sleep(10)
                    continue
                raise


def judge(question: str, rubric: str, answer: str, api_key: str, retries: int = 2) -> dict:
    prompt = f"""당신은 RAG 챗봇의 답변 품질을 채점하는 평가자입니다.

[질문]
{question}

[판정 기준]
{rubric}

[실제 답변]
{answer}

위 판정 기준을 실제 답변이 충족하는지 평가하세요. 다음 JSON 형식으로만 답하세요, 다른 텍스트는 쓰지 마세요:
{{"pass": true 또는 false, "reason": "한 문장 이유"}}"""

    body = json.dumps({"contents": [{"parts": [{"text": prompt}]}]}).encode("utf-8")

    for attempt in range(retries + 1):
        req = urllib.request.Request(
            f"{GEMINI_BASE_URL}/v1beta/models/{JUDGE_MODEL}:generateContent",
            data=body, method="POST",
            headers={"Content-Type": "application/json", "x-goog-api-key": api_key},
        )
        try:
            with urllib.request.urlopen(req, timeout=30) as resp:
                payload = json.loads(resp.read().decode("utf-8"))
            text = payload["candidates"][0]["content"]["parts"][0]["text"]
            text = text.strip().removeprefix("```json").removeprefix("```").removesuffix("```").strip()
            parsed = json.loads(text)
            return {"pass": bool(parsed["pass"]), "reason": parsed.get("reason", "")}
        except urllib.error.HTTPError as e:
            if e.code in (429, 500, 503) and attempt < retries:
                wait = 15 * (attempt + 1)
                print(f"    (judge {e.code}, {wait}s 대기 후 재시도)", file=sys.stderr)
                time.sleep(wait)
                continue
            raise RuntimeError(f"judge API error {e.code}: {e.read().decode('utf-8')[:300]}")
        except (KeyError, json.JSONDecodeError) as e:
            raise RuntimeError(f"judge 응답 파싱 실패: {e}, raw={text!r}")


def run(base_url: str, dataset_path: Path, email: str, password: str, api_key: str,
        limit: int = None, category: str = None, ids: list = None):
    dataset = json.loads(dataset_path.read_text(encoding="utf-8"))
    cases = dataset["cases"]
    if ids:
        wanted = set(ids)
        cases = [c for c in cases if c["id"] in wanted]
    if category:
        cases = [c for c in cases if c["category"] == category]
    if limit:
        cases = cases[:limit]
    dataset = {**dataset, "cases": cases}
    client = ApiClient(base_url)
    print(f"로그인 중... ({base_url})")
    client.login(email, password)

    details = []
    for i, case in enumerate(dataset["cases"]):
        if i > 0:
            time.sleep(2)

        try:
            answer = client.ask(case["question"])
        except (urllib.error.HTTPError, urllib.error.URLError, TimeoutError) as e:
            print(f"[ERR ] {case['id']:>4}  ask 실패: {e}  ({case['question']})")
            details.append({**case, "answer": None, "pass": None, "reason": f"ask 실패: {e}"})
            continue
        except KeyboardInterrupt:
            print("\n중단됨 — 지금까지 결과만 저장합니다.", file=sys.stderr)
            break

        try:
            verdict = judge(case["question"], case["rubric"], answer, api_key)
        except RuntimeError as e:
            print(f"[ERR ] {case['id']:>4}  judge 실패: {e}  ({case['question']})")
            details.append({**case, "answer": answer, "pass": None, "reason": str(e)})
            continue
        except KeyboardInterrupt:
            print("\n중단됨 — 지금까지 결과만 저장합니다.", file=sys.stderr)
            break

        mark = "PASS" if verdict["pass"] else "FAIL"
        details.append({**case, "answer": answer, "pass": verdict["pass"], "reason": verdict["reason"]})
        print(f"[{mark}] {case['id']:>4}  {case['question']}")
        if not verdict["pass"]:
            print(f"       답변: {answer[:150]}")
            print(f"       사유: {verdict['reason']}")

    graded = [d for d in details if d["pass"] is not None]
    passed = [d for d in graded if d["pass"]]

    by_cat = {}
    for d in graded:
        by_cat.setdefault(d["category"], []).append(d)

    summary = {
        "total": len(details),
        "graded": len(graded),
        "errors": len(details) - len(graded),
        "passed": len(passed),
        "pass_rate": len(passed) / len(graded) if graded else None,
        "by_category": {
            cat: {
                "total": len(items),
                "passed": sum(1 for d in items if d["pass"]),
                "pass_rate": sum(1 for d in items if d["pass"]) / len(items),
            }
            for cat, items in by_cat.items()
        },
        "failed_ids": [d["id"] for d in graded if not d["pass"]],
    }

    return {
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "base_url": base_url,
        "judge_model": JUDGE_MODEL,
        "summary": summary,
        "cases": details,
    }


def print_summary(result):
    s = result["summary"]
    print("\n=== 전체 요약 ===")
    print(f"total={s['total']}  graded={s['graded']}  errors={s['errors']}  "
          f"passed={s['passed']}  pass_rate={s['pass_rate']:.1%}" if s["pass_rate"] is not None
          else f"total={s['total']}  graded=0")

    print("\n=== 카테고리별 ===")
    for cat, m in s["by_category"].items():
        print(f"{cat:<14} {m['passed']}/{m['total']}  ({m['pass_rate']:.1%})")

    if s["failed_ids"]:
        print(f"\n=== FAIL 케이스 — {len(s['failed_ids'])}건 ===")
        print(", ".join(s["failed_ids"]))


def main():
    parser = argparse.ArgumentParser(description="RAG QA 답변 품질 평가")
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL)
    parser.add_argument("--dataset", default=str(Path(__file__).parent / "dataset.json"))
    parser.add_argument("--email", default=DEFAULT_ADMIN_EMAIL)
    parser.add_argument("--password", default=DEFAULT_ADMIN_PASSWORD)
    parser.add_argument("--out", default=None, help="결과 JSON 저장 경로 (기본: results/<timestamp>.json)")
    parser.add_argument("--limit", type=int, default=None, help="앞에서부터 N개 케이스만 실행 (디버깅용)")
    parser.add_argument("--category", default=None,
                         help="특정 카테고리만 실행: structured/unstructured/mixed/edge")
    parser.add_argument("--ids", default=None, help="특정 케이스 id만 콤마로 구분해서 실행 (예: U04,U08)")
    args = parser.parse_args()

    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print("GEMINI_API_KEY 환경변수가 없습니다. `export GEMINI_API_KEY=...` 후 다시 실행하세요.", file=sys.stderr)
        sys.exit(1)

    ids = [i.strip() for i in args.ids.split(",")] if args.ids else None
    result = run(args.base_url, Path(args.dataset), args.email, args.password, api_key,
                 args.limit, args.category, ids)
    print_summary(result)

    out_path = Path(args.out) if args.out else Path(__file__).parent / "results" / f"{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"\n결과 저장: {out_path}")


if __name__ == "__main__":
    main()
