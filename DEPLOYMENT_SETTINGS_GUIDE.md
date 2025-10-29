# Tetris 게임 배포 및 설정 관리 가이드

## 배포 방법

### 1. JAR 파일로 배포 (모든 플랫폼)

```bash
./gradlew jar
```

생성된 파일: `app/build/libs/app.jar` (약 3.23 MB)

**실행 방법:**
```bash
java -jar app.jar
```

### 2. Windows EXE 파일로 배포

```bash
./gradlew createExe
```

생성된 파일: `app/build/launch4j/Tetris.exe` (약 3.3 MB)

**실행 방법:**
- 더블 클릭하여 실행
- 또는 명령 프롬프트에서: `Tetris.exe`

## 설정 파일 관리

### 자동 설정 관리 시스템

게임은 첫 실행 시 자동으로 설정 디렉토리를 생성하고 관리합니다:

**설정 저장 위치:**
- **macOS/Linux**: `~/.tetris/`
- **Windows**: `C:\Users\사용자명\.tetris\`

**저장되는 파일:**
```
~/.tetris/
├── SettingSave.json       # 게임 설정 (난이도, 화면 크기, 조작키 등)
├── HighScore.json         # 최고 점수 기록
├── DefaultSetting.json    # 기본 설정 (초기화용)
└── HighScoreDefault.json  # 기본 점수 (초기화용)
```

### 설정이 유지되는 이유

1. **외부 저장**: 설정이 JAR/EXE 내부가 아닌 사용자 홈 디렉토리에 저장됩니다
2. **읽기/쓰기 가능**: 외부 파일이므로 언제든 수정 가능합니다
3. **업데이트 안전**: 새 버전의 JAR/EXE로 교체해도 설정은 그대로 유지됩니다

### 첫 실행 과정

```
1. 게임 시작
   ↓
2. ~/.tetris/ 폴더 확인
   ↓
3. 폴더가 없으면 자동 생성
   ↓
4. JAR/EXE 내부의 기본 파일을 ~/.tetris/로 복사
   ↓
5. 이후 모든 설정 읽기/쓰기는 ~/.tetris/에서 수행
```

## 배포 시 주의사항

### ✅ 배포하면 되는 것
- JAR 파일 또는 EXE 파일 **하나만** 배포하면 됩니다
- 별도의 설정 파일을 함께 배포할 필요 없습니다
- 게임이 첫 실행 시 모든 것을 자동으로 설정합니다

### ❌ 배포하지 않아도 되는 것
- `src/` 폴더
- `build/` 폴더 (JAR/EXE 제외)
- JSON 설정 파일들 (게임에 이미 포함됨)

## 사용자 설정 초기화

게임 내에서 "설정 초기화" 기능을 사용하면:
- `~/.tetris/DefaultSetting.json`의 내용으로 복원됩니다
- 사용자가 수동으로 파일을 삭제할 필요 없습니다

## 사용자 데이터 완전 삭제

사용자가 게임을 완전히 제거하고 싶다면:

**macOS/Linux:**
```bash
rm -rf ~/.tetris
```

**Windows:**
1. 파일 탐색기 열기
2. 주소창에 `%USERPROFILE%\.tetris` 입력
3. 폴더 삭제

## GitHub Release 배포

### 1. JAR + EXE 함께 배포 (권장)

Release 페이지에 다음 파일들을 업로드:

```
Tetris-v1.0.0/
├── Tetris.jar          # macOS/Linux/Windows
└── Tetris.exe          # Windows 전용
```

### 2. Release 설명 예시

```markdown
## Tetris v1.0.0

### 다운로드
- **Windows 사용자**: `Tetris.exe` 다운로드
- **macOS/Linux 사용자**: `Tetris.jar` 다운로드

### 실행 방법
- **Windows**: `Tetris.exe` 더블 클릭
- **macOS/Linux**: 터미널에서 `java -jar Tetris.jar`
  - Java 17 이상 필요

### 설정 저장 위치
게임 설정은 자동으로 사용자 홈 디렉토리의 `.tetris` 폴더에 저장됩니다.
- Windows: `C:\Users\사용자명\.tetris\`
- macOS/Linux: `~/.tetris/`

### 주요 기능
- 일반 모드 / 아이템 모드
- 난이도 선택 (쉬움/보통/어려움)
- 최고 점수 기록
- 설정 자동 저장
```

## 개발자를 위한 정보

### ConfigManager 클래스

설정 관리는 `settings.ConfigManager` 클래스가 담당합니다:

```java
// 설정 파일 경로 가져오기
String settingsPath = ConfigManager.getSettingsPath();
String highScorePath = ConfigManager.getHighScorePath();

// 기본 설정 파일 경로
String defaultSettingsPath = ConfigManager.getDefaultSettingsPath();
String defaultHighScorePath = ConfigManager.getDefaultHighScorePath();

// 설정 디렉토리
String configDir = ConfigManager.getConfigDir(); // ~/.tetris/
```

### 새로운 설정 파일 추가하기

1. `app/src/main/resources/data/`에 기본 파일 추가
2. `ConfigManager.java`에 파일 이름 상수 추가
3. `static {}` 블록에서 `initializeFile()` 호출
4. getter 메서드 추가

## 문제 해결

### Q: 설정이 저장되지 않아요
**A:** 다음을 확인하세요:
- 홈 디렉토리에 쓰기 권한이 있는지 확인
- `~/.tetris/` 폴더가 생성되었는지 확인
- 게임을 재시작해보세요

### Q: 설정을 초기화하고 싶어요
**A:** 게임 내 "설정" → "초기화" 버튼을 사용하거나, `~/.tetris/` 폴더를 삭제하세요.

### Q: Windows에서 EXE 실행 시 보안 경고가 나와요
**A:** "추가 정보" → "실행" 클릭하면 됩니다. (코드 서명이 없어서 나타나는 경고입니다)

## 라이선스 및 배포

이 게임은 자유롭게 배포 가능합니다. 
JAR 또는 EXE 파일만 공유하면 사용자가 바로 플레이할 수 있습니다.
