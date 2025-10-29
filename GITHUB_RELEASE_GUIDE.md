# GitHub Release로 JAR 파일 배포하기

## 🎯 추천 방법: GitHub Releases 사용

### 1단계: 릴리스 생성
1. GitHub 저장소 페이지로 이동
2. 오른쪽 사이드바에서 "Releases" 클릭
3. "Create a new release" 또는 "Draft a new release" 클릭

### 2단계: 릴리스 정보 입력
- **Tag version**: `v1.0` (예: v1.0, v1.1.0, v2.0.0)
- **Release title**: `Tetris v1.0 - Initial Release`
- **Description**: 릴리스 노트 작성
  ```markdown
  ## 🎮 Tetris Game v1.0
  
  ### 주요 기능
  - ✅ 일반 모드 (Normal Mode)
  - ✅ 아이템 모드 (Item Mode) - 5가지 특수 블록
  - ✅ 3가지 난이도 (Easy, Normal, Hard)
  - ✅ 7단계 속도 시스템
  - ✅ 하이스코어 저장
  - ✅ 키보드 네비게이션
  
  ### 실행 방법
  1. Java 17 이상 설치 필요
  2. `app.jar` 다운로드
  3. 더블클릭 또는 `java -jar app.jar` 실행
  
  ### 다운로드
  - **app.jar** (3.2MB) - 모든 플랫폼 (Windows, macOS, Linux)
  ```

### 3단계: JAR 파일 업로드
- "Attach binaries by dropping them here or selecting them" 영역에
- `app.jar` 파일을 드래그 앤 드롭

### 4단계: 릴리스 게시
- "Publish release" 클릭

---

## 📋 릴리스 노트 템플릿

```markdown
## 🎮 Tetris Game v1.0 - Initial Release

### ✨ 새로운 기능
- 듀얼 게임 모드 (Normal / Item)
- 5가지 아이템 블록 시스템
  - AllClearBlock: 전체 보드 클리어
  - BoxClearBlock: 5x5 영역 폭발
  - OneLineClearBlock: +1줄 보너스 점수
  - ScoreDoubleBlock: 점수 2배
  - WeightBlock: 관통 낙하
- 난이도 시스템 (Easy/Normal/Hard)
- 속도 시스템 (7단계)
- 하이스코어 보드 (모드별 분리)

### 📦 다운로드
- **app.jar** - 실행 파일 (모든 플랫폼)
- **소스코드** - zip, tar.gz

### 💻 시스템 요구사항
- Java Runtime Environment (JRE) 17 이상
- 운영체제: Windows 10+, macOS 10.14+, Linux

### 🚀 실행 방법
```bash
# 터미널/명령 프롬프트에서
java -jar app.jar

# 또는 파일을 더블클릭
```

### 📊 테스트 커버리지
- 전체: 69%
- blocks 패키지: 96%
- game 패키지: 72%
- 총 153개 테스트 통과

### 🐛 알려진 이슈
- 없음

### 👥 개발팀
SE-tetris-team10

### 📝 라이선스
[라이선스 정보]

---

**설치 문제?** [DISTRIBUTION_GUIDE.md](./DISTRIBUTION_GUIDE.md) 참조
**게임 가이드?** [ITEM_BLOCKS_SPECIFICATION.md](./ITEM_BLOCKS_SPECIFICATION.md) 참조
```

---

## 🔧 옵션 2: .gitignore 수정 (비추천)

JAR 파일을 Git에 포함시키려면:

### app.jar만 예외 처리:
```bash
# .gitignore 파일에 추가
!app.jar
```

### 또는 특정 디렉토리만 허용:
```bash
# .gitignore 수정
*.jar
!dist/*.jar
```

**주의**: 
- ❌ 빌드 파일을 Git에 포함하는 것은 권장되지 않음
- ❌ 저장소 크기가 불필요하게 커짐
- ❌ 매 빌드마다 커밋이 생성됨

---

## 🎯 GitHub Actions로 자동 빌드 (고급)

릴리스 시 자동으로 JAR 파일 생성:

### .github/workflows/release.yml 생성:
```yaml
name: Create Release

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build with Gradle
      run: ./gradlew clean jar
    
    - name: Create Release
      uses: softprops/action-gh-release@v1
      with:
        files: app/build/libs/app.jar
        body: |
          ## Tetris Game Release
          
          실행 방법: `java -jar app.jar`
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## 📱 빠른 시작 (사용자용)

GitHub 저장소에 다음 내용을 README.md에 추가:

```markdown
## 📥 다운로드

[최신 릴리스 다운로드](https://github.com/HyunWrites0721/SE-tetris-team10/releases/latest)

1. **app.jar** 파일 다운로드
2. Java 17 이상 설치 확인
3. 파일 실행:
   ```bash
   java -jar app.jar
   ```
   또는 파일 더블클릭
```

---

## 🔍 현재 상태 확인

릴리스가 생성되면:
- 저장소 메인 페이지 오른쪽에 "Releases" 표시
- 사용자가 쉽게 다운로드 가능
- 각 버전별 다운로드 통계 확인 가능
- 소스코드 zip/tar.gz 자동 생성

---

## ✅ 체크리스트

릴리스 전 확인:
- [ ] 모든 테스트 통과
- [ ] JAR 파일 정상 실행 확인
- [ ] 버전 번호 결정 (Semantic Versioning)
- [ ] 릴리스 노트 작성
- [ ] CHANGELOG.md 업데이트
- [ ] 문서 최신화 (README.md)

---

**다음 단계**: GitHub 저장소로 가서 릴리스 생성! 🚀
