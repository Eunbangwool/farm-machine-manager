#!/bin/bash
# ===================================================================
# Farm Machine Manager - Git 초기 셋업
# ===================================================================
# 한 번만 실행하세요. 이미 git이 초기화돼있으면 그냥 종료합니다.
# ===================================================================

set -e

# 스크립트가 있는 폴더의 상위(프로젝트 루트)로 이동
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_DIR"

echo "🌾 Farm Machine Manager - Git 초기 셋업"
echo "==================================="
echo "프로젝트 위치: $PROJECT_DIR"
echo ""

# ----------------------------------------------------------------
# git 설치 확인
# ----------------------------------------------------------------
if ! command -v git &> /dev/null; then
    echo "❌ git이 설치되어 있지 않습니다."
    echo ""
    echo "Mac에서 git 설치:"
    echo "  방법 1: Xcode Command Line Tools (가장 쉬움)"
    echo "    터미널에서: xcode-select --install"
    echo ""
    echo "  방법 2: Homebrew 사용"
    echo "    brew install git"
    exit 1
fi

# ----------------------------------------------------------------
# 이미 초기화되었는지 확인
# ----------------------------------------------------------------
if [ -d .git ]; then
    echo "ℹ️  이미 git이 초기화되어 있습니다."
    echo ""
    echo "현재 상태:"
    git status --short --branch
    echo ""
    echo "GitHub remote 확인:"
    if git remote get-url origin &> /dev/null; then
        echo "  ✅ origin: $(git remote get-url origin)"
    else
        echo "  ⚠️  GitHub remote가 설정되지 않았습니다."
        echo "     아래의 'GitHub에 push하기' 단계를 따라하세요."
    fi
    exit 0
fi

# ----------------------------------------------------------------
# .gitignore 확인
# ----------------------------------------------------------------
if [ ! -f .gitignore ]; then
    echo "❌ .gitignore 파일이 없습니다."
    echo "   Claude가 제공한 .gitignore 파일을 프로젝트 루트에 두고 다시 실행하세요."
    exit 1
fi
echo "✅ .gitignore 확인됨"

# ----------------------------------------------------------------
# git 초기화
# ----------------------------------------------------------------
echo ""
echo "📦 git 저장소 초기화 중..."
git init -b main -q

# 처음 커밋자 정보가 없으면 안내
if ! git config user.email &> /dev/null; then
    echo ""
    echo "⚙️  git 사용자 정보가 없습니다. 한 번만 설정:"
    echo "     git config --global user.name \"내 이름\""
    echo "     git config --global user.email \"내@이메일.com\""
    echo ""
    read -p "지금 설정하시겠어요? [Y/n] " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Nn]$ ]]; then
        read -p "이름: " GIT_NAME
        read -p "이메일: " GIT_EMAIL
        git config --global user.name "$GIT_NAME"
        git config --global user.email "$GIT_EMAIL"
        echo "✅ 설정 완료"
    fi
fi

# ----------------------------------------------------------------
# 초기 커밋
# ----------------------------------------------------------------
echo ""
echo "📝 초기 커밋 생성 중..."
git add .
git commit -m "Initial commit (v6 baseline)" -q
echo "✅ 초기 커밋 완료"

# ----------------------------------------------------------------
# 결과 출력
# ----------------------------------------------------------------
echo ""
echo "=========================================="
echo "✅ Git 초기화 완료!"
echo "=========================================="
echo ""
echo "📤 다음 단계: GitHub에 push 하기"
echo ""

if command -v gh &> /dev/null; then
    echo "🎯 옵션 A — GitHub CLI 사용 (추천, 명령어 2줄)"
    echo "  1. 로그인 (한 번만):"
    echo "     gh auth login"
    echo "  2. 저장소 생성 + push:"
    echo "     gh repo create farm-machine-manager --private --source=. --push"
    echo ""
fi

echo "📋 옵션 B — 브라우저에서 직접"
echo "  1. https://github.com/new 접속"
echo "  2. Repository name: farm-machine-manager"
echo "     Visibility: Private 추천 (Public도 가능)"
echo "     README, .gitignore, license는 모두 추가하지 마세요 (이미 있음)"
echo "  3. Create repository 클릭"
echo "  4. 다음 화면에서 URL 복사 (예: https://github.com/USERNAME/farm-machine-manager.git)"
echo "  5. 이 터미널에서:"
echo "     git remote add origin <복사한URL>"
echo "     git push -u origin main"
echo ""
echo "✨ push 완료 후, GitHub URL을 Claude에게 공유해주세요!"
echo "   (그래야 Claude가 다음 업데이트를 정확히 만들 수 있어요)"
