#!/usr/bin/env python3
"""
RAG QA 함수 호출 라우팅 평가.

RagQaService.route()/POST /api/admin/stats/route-eval을 호출해 실제 DB/임베딩 없이
Gemini의 tool 선택(+인자)만 확인하고, dataset.json의 라벨과 비교해 confusion matrix와
F1을 계산한다. Gemini API를 호출하므로 백엔드가 로컬에서 떠 있어야 하고, 실행마다
케이스 수만큼 실제 API 비용이 든다(임베딩/검색 비용은 없음 — route()가 dry-run이라
실제 조회를 안 타기 때문).

사용법:
    python3 run_eval.py [--base-url http://localhost:8080] [--dataset dataset.json]
"""
import argparse
import json
import sys
import time
import urllib.error
import urllib.request
import http.cookiejar
from datetime import datetime, timezone
from pathlib import Path

from sklearn.metrics import confusion_matrix, classification_report

DEFAULT_BASE_URL = "http://localhost:8080"
DEFAULT_ADMIN_EMAIL = "admin@prodio.com"
DEFAULT_ADMIN_PASSWORD = "admin1234"
TOOL_LABELS = ["queryOrderStats", "searchNotes", "NONE"]


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
        _, me = self._call("POST", "/auth/login", {"email": email, "password": password})
        _, csrf = self._raw("GET", "/auth/csrf")
        self.csrf_token = csrf["token"]
        return me["data"]

    def route(self, question: str, retries: int = 2):
        """
        백엔드(GeminiClient)가 이미 429/5xx를 내부적으로 최대 3번 재시도한다 — 그 위에 여기서도
        짧게 재시도를 겹치면 실요청량이 배로 불어나 rate limit을 계속 재유발한다. 그래서 여기서는
        재시도 횟수를 적게, 대기는 길게 둬서 순간 트래픽 폭주를 만들지 않는다.
        """
        for attempt in range(retries + 1):
            try:
                _, resp = self._call("POST", "/api/admin/stats/route-eval", {"question": question})
                return resp["data"]["calls"]
            except urllib.error.HTTPError as e:
                if e.code in (429, 500) and attempt < retries:
                    wait = 30 * (attempt + 1)
                    print(f"    ({e.code}, {wait}s 대기 후 재시도)", file=sys.stderr)
                    time.sleep(wait)
                    continue
                raise


def predict(calls):
    """calls(list of {name, args}) -> (predicted_tool, predicted_args_of_first_call)"""
    if not calls:
        return "NONE", {}
    return calls[0]["name"], calls[0]["args"]


def source_type_ok(case, predicted_args):
    expected = case.get("expected_source_types")
    if not expected:
        return None  # 이 케이스는 sourceType을 채점 대상으로 안 둠
    actual = predicted_args.get("sourceType") or "ALL"  # 생략하면 RagQaService가 ALL로 취급
    return actual.upper() in expected


def status_ok(case, predicted_args):
    expected = case.get("expected_status")
    if not expected:
        return None
    actual = predicted_args.get("status")
    return actual is not None and actual.upper() == expected.upper()


def run(base_url: str, dataset_path: Path, email: str, password: str):
    dataset = json.loads(dataset_path.read_text())
    client = ApiClient(base_url)
    print(f"로그인 중... ({base_url})")
    client.login(email, password)

    y_true, y_pred = [], []
    details = []

    for i, case in enumerate(dataset["cases"]):
        if i > 0:
            time.sleep(5)  # 유료 티어 기준 페이싱. 가끔 나오는 5xx는 아래에서 개별 실패로 처리하고 계속 진행한다.
        expected_raw = case["expected_tool"]
        # expected_tool이 배열이면 "여러 경로가 다 정답"인 애매한 케이스 — 첫 항목을 confusion matrix
        # 대표 라벨로 쓰고, 채점은 배열 중 하나라도 맞으면 정답으로 친다.
        expected_list = expected_raw if isinstance(expected_raw, list) else [expected_raw]
        canonical_expected = expected_list[0]

        try:
            calls = client.route(case["question"])
            predicted_tool, predicted_args = predict(calls)
        except urllib.error.HTTPError as e:
            # 재시도까지 소진된 개별 질문 실패 — 이 질문 하나 때문에 나머지 59개 결과를 날리지 않도록
            # ERROR로 기록만 하고 다음 질문으로 넘어간다.
            print(f"    !! {case['id']} 최종 실패({e.code}), ERROR로 기록하고 계속 진행", file=sys.stderr)
            calls, predicted_tool, predicted_args = None, "ERROR", {}

        y_true.append(canonical_expected)
        y_pred.append(predicted_tool)

        detail = {
            "id": case["id"],
            "category": case["category"],
            "question": case["question"],
            "expected_tool": expected_raw,
            "predicted_tool": predicted_tool,
            "tool_correct": predicted_tool in expected_list,
            "all_calls": calls,
        }
        st_ok = source_type_ok(case, predicted_args)
        if st_ok is not None:
            detail["source_type_correct"] = st_ok
        status_ok_value = status_ok(case, predicted_args)
        if status_ok_value is not None:
            detail["status_correct"] = status_ok_value

        details.append(detail)
        mark = "OK" if detail["tool_correct"] else "FAIL"
        expected_display = "|".join(expected_list)
        print(f"[{mark}] {case['id']:>4}  기대={expected_display:<28} 실제={predicted_tool:<16} {case['question']}")

    labels = sorted(set(y_true) | set(y_pred), key=lambda t: TOOL_LABELS.index(t) if t in TOOL_LABELS else 99)
    cm = confusion_matrix(y_true, y_pred, labels=labels)
    report = classification_report(y_true, y_pred, labels=labels, output_dict=True, zero_division=0)

    result = {
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "base_url": base_url,
        "labels": labels,
        "confusion_matrix": cm.tolist(),
        "classification_report": report,
        "overall_accuracy": report["accuracy"],
        "cases": details,
    }
    return result


def print_summary(result):
    print("\n=== Confusion Matrix ===")
    labels = result["labels"]
    print("행=실제(기대), 열=예측")
    header = "".ljust(18) + "".join(l.ljust(18) for l in labels)
    print(header)
    for label, row in zip(labels, result["confusion_matrix"]):
        print(label.ljust(18) + "".join(str(v).ljust(18) for v in row))

    print("\n=== Classification Report ===")
    for label in labels:
        m = result["classification_report"][label]
        print(f"{label:<18} precision={m['precision']:.2f}  recall={m['recall']:.2f}  f1={m['f1-score']:.2f}  support={int(m['support'])}")

    print(f"\n전체 정확도(accuracy): {result['overall_accuracy']:.2%}")


def main():
    parser = argparse.ArgumentParser(description="RAG QA 함수 호출 라우팅 평가")
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
