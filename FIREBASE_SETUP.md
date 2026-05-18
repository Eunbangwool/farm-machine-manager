# Firebase 실시간 동기화 설정 가이드

이 앱은 Firebase Firestore를 사용해 여러 폰에서 데이터를 자동 동기화할 수 있어요.
아래 단계를 따라 5분이면 설정 완료.

## 1. Firebase 프로젝트 생성

1. https://console.firebase.google.com 접속 (Google 계정 로그인)
2. **프로젝트 추가** 클릭
3. 프로젝트 이름: `farm-machine-manager` (또는 원하는 이름)
4. Google Analytics: 비활성화 추천 (필요 없음)
5. **프로젝트 만들기**

## 2. Android 앱 등록

1. 프로젝트 대시보드에서 **Android 아이콘** 클릭
2. **Android 패키지 이름**: `com.example.farmmachinemanager` ← 정확히 일치해야 함
3. 앱 닉네임: `농기계 관리` (선택)
4. **앱 등록** 클릭

## 3. google-services.json 다운로드 + 배치

1. **google-services.json 다운로드** 클릭 → 파일이 ~/Downloads에 저장됨
2. Mac 터미널에서:
   ```
   mv ~/Downloads/google-services.json /Users/soorin/AndroidStudioProjects/FarmMachineManager/app/
   ```
3. 그 다음 단계는 **SDK 추가**라고 나오는데 우리는 이미 추가했으니 **다음**, **다음**, **콘솔로 이동** 클릭

## 4. Firestore 활성화

1. Firebase 콘솔 왼쪽 메뉴 → **빌드** → **Firestore Database**
2. **데이터베이스 만들기**
3. 위치: `asia-northeast3 (서울)` 선택
4. 보안 규칙: **테스트 모드로 시작** 선택 (30일 후 만료, 그 안에 규칙 설정)
5. **사용 설정**

## 5. (중요) 보안 규칙 설정

기본 테스트 모드는 30일 후 만료돼요. 그 전에 더 안전한 규칙으로:

1. Firestore Database → **규칙** 탭
2. 아래 규칙으로 교체:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 농장 코드만 알면 누구나 읽고 쓸 수 있음 (간단한 공유 모델)
    // 더 강한 보안 필요하면 Authentication 도입 후 규칙 강화
    match /farms/{farmCode}/{document=**} {
      allow read, write: if farmCode.matches('[0-9]{6}');
    }
  }
}
```

3. **게시** 클릭

## 6. 앱 빌드 + 푸시

```
cd /Users/soorin/AndroidStudioProjects/FarmMachineManager
git add app/google-services.json
git commit -m "Add Firebase config"
git push
```

⚠️ google-services.json은 공개 저장소에 올려도 안전해요 (위 보안 규칙 기준).
   실제로는 클라이언트 식별자일 뿐, 실제 보안은 Firestore 규칙이 담당.

## 7. 앱에서 농장 코드 설정

1. APK 빌드 완료 후 폰에 설치
2. **설정** 화면 진입 → **동기화** 섹션
3. **새 농장 코드 생성** 클릭 → 6자리 코드 표시됨 (예: `493728`)
4. 다른 폰에서도 같은 앱 설치 후 → **다른 폰 코드로 참여** → 같은 코드 입력
5. 이제 두 폰이 실시간으로 동기화 됨!

## 동작 확인

- 폰 A에서 기계 등록 → 폰 B에 즉시 표시 (앱 켜져 있을 때)
- 오프라인 변경도 자동 캐시되어 온라인 복귀 시 동기화

## 트러블슈팅

**Q: 빌드는 됐는데 Settings에서 "Firebase 미설정"으로 나와요**
- `app/google-services.json` 파일이 정확한 위치에 있는지 확인
- 패키지 이름이 `com.example.farmmachinemanager`와 정확히 일치하는지 확인
- 앱 한 번 완전 종료 후 다시 실행

**Q: "연결됨"인데 데이터가 안 보여요**
- 다른 폰과 같은 농장 코드인지 확인
- 인터넷 연결 확인
- Firestore 보안 규칙 위 단계 5번 그대로 적용됐는지 확인

## 다음 단계 (다음 patch에서 추가 예정)

이번 patch에서는 **Machine 데이터만** Firestore 동기화 됩니다.
다음 patch에서 정비 기록, 소모품 정보도 동기화 추가 예정.
