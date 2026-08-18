# UI 리스킨 — 인디고 테마

## 목적

누밋(noomit)에서 fork한 프론트엔드의 색상·사이드바 스타일을 Prodio 고유 디자인으로 교체한다.

## 범위

**이번에 하는 것**
- CSS 토큰 교체 (primary 색상: 빨강 → 인디고)
- 사이드바 배경색 교체 (따뜻한 브라운 → 쿨 다크)
- 라이트/다크 모드 모두 반영

**이번에 하지 않는 것**
- 레이아웃 구조 변경
- 카드·버튼·타이포 전체 리디자인
- 새 컴포넌트 추가

## 변경 파일

### 1. `frontend/features/theme/themes/light.css`

```css
--color-primary: #5b67d8;       /* #c93a35 → 인디고 */
--color-primary-hover: #4556c8; /* #a82a25 → 인디고 다크 */
```

`--color-danger: #d93d35` 는 에러/경고 전용이므로 유지.

### 2. `frontend/features/theme/themes/dark.css`

```css
--color-primary: #818cf8;        /* #e0524d → 인디고400 (다크모드용 밝은 톤) */
--color-primary-hover: #a5b4fc;  /* #f06a65 → 인디고300 */
```

### 3. `frontend/features/admin/components/AdminSidebar.module.css`

사이드바 내 하드코딩된 따뜻한 브라운 계열 색상을 쿨 다크로 교체.

| 선택자/속성 | 현재 값 | 변경 값 |
|------------|---------|---------|
| `.sidebar` background | `#1c1a17` | `#111827` |
| `.sidebar` border-right color | `#2c2a27` | `#1f2937` |
| `.logoSub` color | `#817d76` | `#6b7280` |
| `.toggleButton` color | `#a8a7a5` | `#9ca3af` |
| `.navItem` color (비활성) | `#a8a7a5` | `#9ca3af` |
| `.profileEmail` color | `#a8a7a5` | `#9ca3af` |

활성 탭(`.navItem[data-active="true"]`)은 `var(--color-primary)` 참조 중이므로 토큰 교체만으로 자동 반영됨.

## 결과 이미지

- 사이드바: 쿨 다크 배경 (`#111827`) + 인디고 활성 탭
- 로고 배지: 인디고 (`#5b67d8`)
- 버튼/링크 액센트: 인디고
- 다크모드: 인디고400 (`#818cf8`) 로 밝기 보정

## 라이트/다크 모드

기존 `data-theme` 토글 구조 유지. 토큰만 바뀌므로 모드 전환 로직 변경 없음.
