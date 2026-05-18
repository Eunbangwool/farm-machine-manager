#!/bin/bash
# ===================================================================
# Farm Machine Manager - Claude 업데이트 적용
# ===================================================================
# 사용법:
#   ./apply-update.sh                       # ~/Downloads에서 최신 .patch 자동 검색
#   ./apply-update.sh ~/Downloads/v7.patch  # 특정 파일 지정
# ===================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_DIR"

# ----------------------------------------------------------------
# 사전 점검
# ----------------------------------------------------------------
if [ ! -d .git ]; then
    echo "❌ 이 폴더는 git 저장소가 아닙니다."
    echo "   먼저 ./scripts/init-git.sh 를 실행하세요."
    exit 1
fi

# ----------------------------------------------------------------
# 패치 파일 찾기
# ----------------------------------------------------------------
PATCH_FILE="${1:-}"

if [ -z "$PATCH_FILE" ]; then
    # ~/Downloads에서 가장 최근에 받은 .patch 검색
    PATCH_FILE=$(ls -t ~/Downloads/*.patch 2>/dev/null | head -1 || true)

    if [ -z "$PATCH_FILE" ]; then
        echo "❌ ~/Downloads 폴더에서 .patch 파일을 찾지 못했습니다."
        echo ""
        echo "사용법:"
        echo "  $0                          # ~/Downloads에서 자동 검색"
        echo "  $0 /path/to/file.patch     # 직접 경로 지정"
        exit 1
    fi

    echo "📦 패치 파일: $PATCH_FILE"
fi

if [ ! -f "$PATCH_FILE" ]; then
    echo "❌ 패치 파일을 찾지 못했습니다: $PATCH_FILE"
    exit 1
fi

# ----------------------------------------------------------------
# 작업 트리 청결 확인
# ----------------------------------------------------------------
if [ -n "$(git status --porcelain)" ]; then
    echo ""
    echo "⚠️  커밋되지 않은 변경사항이 있습니다:"
    git status --short
    echo ""
    echo "권장: 이대로 진행하면 변경사항과 패치가 섞일 수 있어요."
    read -p "그래도 계속하시겠어요? [y/N] " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "취소했습니다."
        echo ""
        echo "팁: 현재 변경사항을 일단 커밋하고 다시 시도하세요:"
        echo "  git add . && git commit -m '내 작업'"
        exit 1
    fi
fi

# ----------------------------------------------------------------
# 변경 예정 요약
# ----------------------------------------------------------------
echo ""
echo "📋 변경될 파일들:"
echo "─────────────────"
git apply --stat "$PATCH_FILE"
echo ""

read -p "적용할까요? [Y/n] " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Nn]$ ]]; then
    echo "취소했습니다."
    exit 0
fi

# ----------------------------------------------------------------
# 패치 적용 (3-way merge로 충돌 가능성 줄이기)
# ----------------------------------------------------------------
echo ""
echo "🔧 패치 적용 중..."

if git apply --3way "$PATCH_FILE"; then
    echo "✅ 패치 적용 성공!"
else
    echo ""
    echo "❌ 패치 적용 중 충돌이 발생했어요."
    echo ""
    echo "다음 파일에 충돌 마커(<<<<<<<, =======, >>>>>>>)가 있을 수 있어요:"
    git diff --name-only --diff-filter=U
    echo ""
    echo "해결 방법:"
    echo "  1. 위 파일들을 열어서 충돌 부분을 직접 수정"
    echo "  2. 수정 후: git add <파일> && git commit"
    echo ""
    echo "또는 전체 취소:"
    echo "  git apply -R --3way \"$PATCH_FILE\""
    exit 1
fi

echo ""
echo "📝 적용된 파일들:"
git status --short
echo ""

# ----------------------------------------------------------------
# 자동 커밋 + push
# ----------------------------------------------------------------
read -p "커밋하고 GitHub에 push 할까요? [Y/n] " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Nn]$ ]]; then
    # 패치 파일명에서 버전 추출 (예: v7.patch → v7)
    VERSION=$(basename "$PATCH_FILE" .patch)

    git add .
    git commit -m "Apply $VERSION update from Claude" -q
    echo "✅ 커밋 완료: $VERSION"

    if git remote get-url origin &> /dev/null; then
        echo "📤 GitHub에 push 중..."
        if git push 2>&1; then
            echo "✅ push 완료"
        else
            echo "⚠️  push 실패 - 수동으로 시도하세요: git push"
        fi
    else
        echo "💾 로컬 커밋 완료 (GitHub remote 미설정)"
        echo "   GitHub 연결: ./scripts/init-git.sh 의 안내 참조"
    fi
fi

echo ""
echo "🚀 완료! Android Studio로 돌아가서 빌드를 다시 실행하세요."
echo "   Android Studio는 자동으로 변경된 파일을 감지합니다."
