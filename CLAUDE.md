# Claude Code 작업 규칙 (farm-machine-manager / 농식이)

## firestore.rules — 공유 프로젝트 + 농돌이(dealerships) 블록 보존

> 세 앱(농작이 `farm-work-manager`, 농식이 `farm-machine-manager`, **농돌이**
> `farmmachine-agency-manager`)은 **같은 Firebase 프로젝트 `farm-machine-manager-prod`**
> 를 공유한다. Firestore 규칙은 **파일 전체 단위 deploy** 라, 이 repo 에서 배포하면
> 다른 앱의 규칙이 통째로 사라질 수 있다.

`firestore.rules` 에 다음이 **반드시 보존**되어야 한다:

1. `match /dealerships/{code} { ... }` 블록 + `dlrMember` / `dlrManager` / `dlrOwner` 헬퍼
   — **농돌이(대리점 앱)** 가 의존. 삭제/수정 금지.
2. `match /farms/{farmCode}` 및 farms 하위 블록 — 농작이가 의존.

**규칙**:
- 위 블록들은 **삭제/수정 금지**. 새 규칙은 분리된 위치에 추가.
- dealerships 블록은 farm-work-manager 와 **양쪽에 동일하게** 존재해야 한다
  (둘 다 배포 소스가 될 수 있음). 변경 시 양 repo sync.
- rules 작업 후 확인:
  `grep -c "match /dealerships/{code}" firestore.rules` → **1 이상**,
  `grep -c "match /farms/{farmCode}" firestore.rules` → **1 이상**.
- 가능하면 배포는 농작이(`farm-work-manager/firestore.rules`)에서 일원화.
