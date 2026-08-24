#!/usr/bin/env python3
"""
RAG QA 함수 호출 라우팅 평가.

RagQaService.route()/POST /api/admin/stats/route-eval을 호출해 실제 DB/임베딩 없이
Gemini의 tool 선택(+인자)만 확인하고, dataset.json의 라벨과 비교해 confusion matrix와
F1을 계산한다. Gemini API를 호출하므로 백엔드가 로컬에서 떠 있어야 하고, 실행마다
케이스 수만큼 실제 API 비용이 든다(임베딩/검색 비용은 없음 — route()가 dry-run이라
실제 조회를 안 타기 때문).

tool 선택 외에 sourceType/status/date(from,to) 인자 정확도도 case별로 채점해
argument_accuracy로 집계한다. date는 dataset.json의 expected_date_rule(예: this_month,
last_year, month:7)을 실행 시점의 실제 날짜(KST) 기준으로 매번 새로 계산해서 비교하므로
하드코딩된 날짜가 시간이 지나 어긋날 걱정 없이 언제 재실행해도 유효하다.

사용법:
    python3 run_eval.py [--base-url http://localhost:8080] [--dataset dataset.json]
"""
import argparse
import calendar
import json
import sys
import time
import urllib.error
import urllib.request
import http.cookiejar
from datetime import date, datetime, timedelta, timezone
from pathlib import Path
from zoneinfo import ZoneInfo

from sklearn.metrics import confusion_matrix, classification_report

KST = ZoneInfo("Asia/Seoul")

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


def _month_range(year: int, month: int):
    last_day = calendar.monthrange(year, month)[1]
    return date(year, month, 1), date(year, month, last_day)


def expected_date_window(rule: str, today: date):
    """
    'expected_date_rule' -> (from_date, to_date_min, to_date_max), 전부 실행 시점의 today를
    기준으로 다시 계산한다(하드코딩된 날짜를 쓰면 eval을 나중에 재실행할 때마다 값이 어긋난다).
    to는 진행 중인 기간(이번 달/올해 등)에서 '오늘까지'와 '기간 끝까지' 둘 다 자연스러운 해석이라
    범위로 받아들인다. rule이 'none'이면 애초에 from/to가 없어야 정답이라는 뜻으로 None을 반환한다.
    """
    if rule == "none":
        return None
    if rule == "this_month":
        start = today.replace(day=1)
        return start, today, _month_range(today.year, today.month)[1]
    if rule == "last_month":
        last_month_end = today.replace(day=1) - timedelta(days=1)
        return last_month_end.replace(day=1), last_month_end, last_month_end
    if rule == "this_year":
        return date(today.year, 1, 1), today, date(today.year, 12, 31)
    if rule == "last_year":
        y = today.year - 1
        return date(y, 1, 1), date(y, 12, 31), date(y, 12, 31)
    if rule == "this_week":
        start = today - timedelta(days=today.weekday())  # 월요일 시작
        return start, today, today
    if rule == "recent_7_days":
        return today - timedelta(days=7), today, today
    if rule == "last_3_months":
        y, m = today.year, today.month - 3
        while m <= 0:
            m += 12
            y -= 1
        return date(y, m, 1), today, today
    if rule.startswith("month:"):
        m = int(rule.split(":")[1])
        y = today.year if m <= today.month else today.year - 1
        start, end = _month_range(y, m)
        to_max = min(end, today) if (y, m) == (today.year, today.month) else end
        return start, to_max, end
    if rule.startswith("year:"):
        y = int(rule.split(":")[1])
        return date(y, 1, 1), date(y, 12, 31), date(y, 12, 31)
    if rule.startswith("range:"):
        _, m1, m2 = rule.split(":")
        y = today.year
        start = date(y, int(m1), 1)
        end = _month_range(y, int(m2))[1]
        to_max = min(end, today) if int(m2) >= today.month else end
        return start, to_max, end
    raise ValueError(f"알 수 없는 expected_date_rule: {rule}")


def date_ok(case, predicted_args, today: date):
    rule = case.get("expected_date_rule")
    if rule is None:
        return None  # 이 케이스는 날짜를 채점 대상으로 안 둠

    window = expected_date_window(rule, today)
    actual_from, actual_to = predicted_args.get("from"), predicted_args.get("to")

    if window is None:
        return actual_from is None and actual_to is None

    if actual_from is None or actual_to is None:
        return False
    try:
        af, at = date.fromisoformat(actual_from), date.fromisoformat(actual_to)
    except ValueError:
        return False

    exp_from, to_min, to_max = window
    return af == exp_from and to_min <= at <= to_max


def run(base_url: str, dataset_path: Path, email: str, password: str):
    dataset = json.loads(dataset_path.read_text())
    client = ApiClient(base_url)
    print(f"로그인 중... ({base_url})")
    client.login(email, password)

    y_true, y_pred = [], []
    details = []
    today = datetime.now(KST).date()

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
        date_ok_value = date_ok(case, predicted_args, today)
        if date_ok_value is not None:
            detail["date_correct"] = date_ok_value

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
        "argument_accuracy": summarize_argument_accuracy(details),
        "cases": details,
    }
    return result


def summarize_argument_accuracy(details):
    """
    tool 선택 자체는 confusion matrix로 잡히지만, sourceType/status/date 같은 인자 정확도는
    case별로 detail에만 남고 그동안 어디에도 집계되지 않았다 — 그래서 인자 관련 회귀(예: 이번에
    고친 날짜 그라운딩 버그)가 나도 confusion matrix나 accuracy엔 안 잡히고 조용히 통과됐다.
    여기서 세 인자를 동일한 방식으로 집계해 요약/리포트에 노출한다.
    """
    summary = {}
    for key in ("source_type_correct", "status_correct", "date_correct"):
        graded = [d for d in details if key in d]
        if not graded:
            continue
        correct = sum(1 for d in graded if d[key])
        summary[key.removesuffix("_correct")] = {
            "correct": correct,
            "total": len(graded),
            "accuracy": correct / len(graded),
            "failed_ids": [d["id"] for d in graded if not d[key]],
        }
    return summary


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

    print("\n=== 인자(Argument) 정확도 ===")
    for name, stats in result["argument_accuracy"].items():
        fail_note = f"  실패: {', '.join(stats['failed_ids'])}" if stats["failed_ids"] else ""
        print(f"{name:<12} {stats['correct']}/{stats['total']} ({stats['accuracy']:.0%}){fail_note}")


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
