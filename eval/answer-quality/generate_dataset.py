#!/usr/bin/env python3
"""
1단계와 동일한 eval/routing/dataset.json 60개 질문을 재사용해 케이스별 rubric을 붙인
dataset.json을 생성한다. 같은 질문을 쓰는 이유는 단계 간 실패 원인을 비교하기 위함
(예: 이 질문이 1단계 라우팅은 통과했는데 3단계 답변에서 틀렸다 등).

rubric은 dataset.json에 이미 있는 정답 메타데이터(expected_status/expected_source_types/
expected_date_rule)를 최대한 재사용해서, 손으로 60개를 새로 쓰는 대신 근거 있게 자동 생성한다.
"""
import json
from pathlib import Path

ROUTING_DATASET_PATH = Path(__file__).parent.parent / "routing" / "dataset.json"

ANTI_FABRICATION = (
    "도구 결과에 없는 값을 사실인 것처럼 지어내면 안 된다. 특히 구체적인 원화 금액을 "
    "언급했다면 그 수치가 실제로 제공된 데이터에 근거해야 한다 — 이 통계 도구는 원화 매출액을 "
    "제공하지 않으므로, 매출/금액을 묻는 질문이어도 건수·수량만 안내하고 금액 데이터는 없다는 "
    "점을 밝혀야지 그럴듯한 금액을 지어내면 안 된다."
)

MONEY_KEYWORDS = ["매출", "얼마"]

SOURCE_TYPE_KR = {
    "ORDER_NOTE": "주문 노트",
    "CLIENT_MEMO": "거래처 메모",
    "PRODUCTION_MEMO": "생산 메모",
    "ALL": "노트/메모",
}

STATUS_KR = {
    "PENDING": "결제 대기",
    "IN_PRODUCTION": "생산 중",
    "IN_DELIVERY": "배송 중",
    "COMPLETED": "완료",
    "CANCELLED": "취소",
}

EDGE_RUBRICS = {
    "E1": "인사와 자기소개 요청에 자연스럽게 응답하고, 이 시스템이 할 수 있는 기능(주문/생산 "
          "통계 조회, 노트/메모 검색 등)을 간단히 소개해야 한다. 실제 주문 데이터나 수치를 "
          "지어내서 언급하면 안 된다.",
    "E2": "이름을 묻는 질문에 자연스럽게 응답해야 하며, 실제 주문/거래처 데이터를 지어내서 "
          "언급하면 안 된다.",
    "E3": "'가나다전자'라는 존재하지 않는 거래처에 대해 관련 정보를 찾을 수 없다는 취지로 "
          "답해야 하며, 존재하지 않는 메모나 내용을 지어내면 절대 안 된다.",
    "E4": "시스템 사용법을 자연스럽게 안내해야 하며, 실제 주문 데이터나 수치를 지어내서 "
          "언급하면 안 된다.",
    "E5": "감사 인사에 자연스럽게 응답해야 하며, 불필요하게 주문/통계 데이터를 지어내서 "
          "붙이면 안 된다.",
}


def structured_rubric(case):
    parts = [
        "답변이 질문에서 요구하는 주문/생산 통계를 구체적인 수치(건수 또는 수량)로 제시해야 한다."
    ]
    status = case.get("expected_status")
    if status:
        status_kr = STATUS_KR.get(status, status)
        parts.append(f"특히 상태가 '{status_kr}'({status})인 주문에 대한 정보여야 하고, "
                      f"다른 상태의 주문 수치를 '{status_kr}'라고 잘못 제시하면 안 된다.")
    date_rule = case.get("expected_date_rule")
    if date_rule and date_rule != "none":
        parts.append("질문에서 요구한 기간에 해당하는 데이터여야 하고, 다른 기간의 수치를 "
                      "섞어서 제시하면 안 된다.")
    if any(kw in case["question"] for kw in MONEY_KEYWORDS):
        parts.append(ANTI_FABRICATION)
    else:
        parts.append("도구 결과에 없는 수치를 지어내면 안 된다.")
    return " ".join(parts)


def unstructured_rubric(case):
    types = case.get("expected_source_types") or ["ALL"]
    type_kr = "/".join(SOURCE_TYPE_KR.get(t, t) for t in types if t != "ALL") or "노트/메모"
    return (
        f"답변이 실제 검색된 {type_kr} 내용에 근거해서 질문과 관련된 구체적인 내용(주문/거래처/"
        f"생산 건 등)을 언급해야 한다. 검색 결과에 없는 세부 정보(날짜, 이름, 사유 등)를 지어내면 "
        f"안 되고, 관련 내용을 못 찾았다면 못 찾았다고 솔직히 답해야 한다."
    )


COUNT_KEYWORDS = ["몇 건", "몇 개", "건수", "개수"]


def mixed_rubric(case):
    """
    최초 버전은 structured_rubric()을 그대로 재사용해 모든 mixed 질문에 "수치를 제시해야
    한다"를 강제했는데, 실제로 돌려보니 mixed 15개 중 다수(M01/M03/M04/M11/M13/M14 등)가
    "사유가 뭐야", "~있어?"처럼 수치가 아니라 사유/존재 여부를 묻는 질문이라 잘못된 FAIL을
    유발했다. 그래서 질문에 실제로 수량 표현(몇 건/몇 개)이 있을 때만 수치 요구 rubric을
    붙이도록 고쳤다.

    금액(매출/얼마) 키워드는 별개로 다룬다 — "수치를 내라"가 아니라 "지어내지 말라"만
    요구해야 한다. M10("이번 달 매출 얼마고, 취소 사유는 뭐였어?")처럼 금액은 없다고 정직히
    답하고 사유만 정확히 준 답변을, 수치 키워드와 묶어서 채점하면 "건수를 안 줬다"는 잘못된
    이유로 또 FAIL 처리된다 — 질문이 애초에 건수를 물은 적이 없는데도.
    """
    question = case["question"]
    has_count_kw = any(kw in question for kw in COUNT_KEYWORDS)
    has_money_kw = any(kw in question for kw in MONEY_KEYWORDS)

    parts = []
    if has_count_kw:
        parts.append(structured_rubric(case))
    else:
        parts.append(
            "답변이 질문이 실제로 묻는 내용(취소/지연 등의 사유, 특정 조건에 해당하는 "
            "주문·거래처의 존재 여부와 구체적인 내용 등)에 정확히 답해야 한다. 질문이 "
            "수치를 요구하지 않았다면 건수를 굳이 제시하지 않아도 된다."
        )
        if has_money_kw:
            parts.append(ANTI_FABRICATION)
    parts.append(
        "검색된 노트/메모나 통계 데이터가 필요한 질문이면 실제 결과에 근거해야 하고, "
        "결과에 없는 세부 사유나 내용을 지어내면 안 된다."
    )
    return " ".join(parts)


def build_rubric(case):
    if case["category"] == "edge":
        return EDGE_RUBRICS.get(case["id"], "질문 의도에 맞게 자연스럽게 응답해야 하고, "
                                              "실제 데이터를 지어내면 안 된다.")
    if case["category"] == "structured":
        return structured_rubric(case)
    if case["category"] == "unstructured":
        return unstructured_rubric(case)
    if case["category"] == "mixed":
        return mixed_rubric(case)
    raise ValueError(f"unknown category: {case['category']}")


def main():
    routing_dataset = json.loads(ROUTING_DATASET_PATH.read_text(encoding="utf-8"))
    cases = []
    for case in routing_dataset["cases"]:
        cases.append({
            "id": case["id"],
            "category": case["category"],
            "question": case["question"],
            "rubric": build_rubric(case),
        })

    dataset = {
        "description": (
            "RAG QA 답변 품질 평가(3단계) 데이터셋. eval/routing/dataset.json과 동일한 60개 "
            "질문을 재사용한다 — 같은 질문이 1단계(라우팅)/2단계(검색)는 통과했는데 3단계(답변 "
            "생성)에서 실패하는 경우를 구분해서 볼 수 있게 하기 위함. rubric은 각 질문의 카테고리와 "
            "1단계 정답 메타데이터(expected_status/expected_source_types/expected_date_rule)를 "
            "근거로 자동 생성했다."
        ),
        "cases": cases,
    }

    out_path = Path(__file__).parent / "dataset.json"
    out_path.write_text(json.dumps(dataset, ensure_ascii=False, indent=2), encoding="utf-8")

    by_cat = {}
    for c in cases:
        by_cat[c["category"]] = by_cat.get(c["category"], 0) + 1
    print(f"generated {len(cases)} cases -> {out_path}")
    print("by category:", by_cat)


if __name__ == "__main__":
    main()
