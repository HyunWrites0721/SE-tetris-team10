# 테트리스 게임 배포 가이드

## 📦 실행 파일 생성 방법

### 방법 1: 실행 가능한 JAR 파일 (추천) ⭐

#### 장점:
- ✅ 크로스 플랫폼 (Windows, macOS, Linux 모두 실행 가능)
- ✅ 간단한 생성 과정
- ✅ JRE만 있으면 실행 가능

#### 생성 방법:
```bash
# 프로젝트 루트에서 실행
./gradlew clean jar

# 생성된 파일 위치
# app/build/libs/app.jar (약 3.2MB)
```

#### 실행 방법:
```bash
# 터미널에서 실행
java -jar app/build/libs/app.jar

# 또는 더블클릭으로 실행 (Java가 설치되어 있어야 함)
```

#### 배포 파일:
- `app.jar` - 모든 의존성이 포함된 실행 파일
- 사용자는 Java 17 이상 필요

---

### 방법 2: macOS .app 번들 (macOS 전용)

macOS 사용자를 위한 네이티브 앱 생성

#### 필요 도구:
```bash
# jpackage (Java 17에 포함)
which jpackage
```

#### 생성 명령어:
```bash
# 1. JAR 파일 먼저 생성
./gradlew clean jar

# 2. .app 번들 생성
jpackage \
  --input app/build/libs \
  --name Tetris \
  --main-jar app.jar \
  --main-class App \
  --type app-image \
  --icon icon.icns \
  --app-version 1.0 \
  --vendor "SE-tetris-team10"

# 생성된 파일: Tetris.app
```

#### 실행 방법:
- Finder에서 `Tetris.app` 더블클릭
- Applications 폴더로 이동 가능

---

### 방법 3: Windows .exe 파일 (Windows 전용)

#### 방법 3-1: Launch4j 사용
```bash
# 1. Launch4j 다운로드
# https://launch4j.sourceforge.net/

# 2. 설정 파일 생성 (launch4j.xml)
# 3. Launch4j로 exe 파일 생성
```

#### 방법 3-2: jpackage 사용 (Windows에서)
```bash
jpackage \
  --input app/build/libs \
  --name Tetris \
  --main-jar app.jar \
  --main-class App \
  --type exe \
  --win-console \
  --app-version 1.0
```

---

### 방법 4: 설치 프로그램 생성

#### macOS DMG 파일:
```bash
jpackage \
  --input app/build/libs \
  --name Tetris \
  --main-jar app.jar \
  --main-class App \
  --type dmg \
  --icon icon.icns \
  --app-version 1.0
```

#### Windows MSI 설치 파일:
```bash
jpackage \
  --input app/build/libs \
  --name Tetris \
  --main-jar app.jar \
  --main-class App \
  --type msi \
  --win-dir-chooser \
  --win-menu \
  --win-shortcut
```

---

## 🚀 빠른 시작 (사용자용)

### 요구사항:
- Java Runtime Environment (JRE) 17 이상
- macOS, Windows, 또는 Linux

### Java 설치 확인:
```bash
java -version
# java version "17.0.14" 또는 그 이상이어야 함
```

### Java 설치 (필요시):
- **macOS**: `brew install openjdk@17`
- **Windows**: [Adoptium](https://adoptium.net/) 에서 다운로드
- **Linux**: `sudo apt install openjdk-17-jre`

### 게임 실행:
```bash
java -jar app.jar
```

---

## 📝 배포 체크리스트

배포 전 확인사항:

- [ ] 모든 테스트 통과 (`./gradlew test`)
- [ ] JAR 파일 정상 실행 확인
- [ ] 리소스 파일 포함 확인 (이미지, 사운드 등)
- [ ] 설정 파일 기본값 확인
- [ ] README.md 업데이트
- [ ] 버전 번호 업데이트
- [ ] 라이선스 파일 포함

---

## 🔧 고급 설정

### 메모리 설정:
```bash
# 힙 메모리 증가
java -Xmx512m -jar app.jar

# GC 로그 활성화
java -Xlog:gc -jar app.jar
```

### 더블클릭 실행 설정 (macOS/Linux):

**실행 스크립트 생성 (run.sh)**:
```bash
#!/bin/bash
cd "$(dirname "$0")"
java -jar app.jar
```

**실행 권한 부여**:
```bash
chmod +x run.sh
```

### 더블클릭 실행 설정 (Windows):

**배치 파일 생성 (run.bat)**:
```batch
@echo off
java -jar app.jar
pause
```

---

## 📦 배포 패키지 구성 예시

```
Tetris-1.0/
├── app.jar                    # 실행 파일
├── README.md                  # 사용 설명서
├── LICENSE                    # 라이선스
├── ITEM_BLOCKS_SPECIFICATION.md  # 아이템 명세서
├── run.sh                     # macOS/Linux 실행 스크립트
├── run.bat                    # Windows 실행 스크립트
└── data/                      # 설정 파일 (선택사항)
    ├── HighScore.json
    └── SettingSave.json
```

---

## 🐛 문제 해결

### "java command not found" 오류:
- Java가 설치되지 않음
- 해결: Java 17 이상 설치

### "no main manifest attribute" 오류:
- JAR 파일에 Main-Class 정보 없음
- 해결: `./gradlew clean jar` 다시 실행

### 게임 창이 나타나지 않음:
- GUI 관련 문제
- 해결: 터미널에서 실행하여 오류 메시지 확인
  ```bash
  java -jar app.jar
  ```

### macOS에서 "손상된 파일" 경고:
```bash
# 보안 설정 우회
xattr -cr Tetris.app
```

---

## 📊 파일 크기 최적화

### ProGuard 사용 (선택사항):
```gradle
// build.gradle에 추가
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.guardsquare:proguard-gradle:7.3.2'
    }
}
```

### 불필요한 리소스 제거:
- 테스트 파일 제외
- 디버그 정보 제거
- 사용하지 않는 의존성 제거

---

## 🎮 게임 정보

- **이름**: Tetris
- **버전**: 1.0
- **개발팀**: SE-tetris-team10
- **Java 버전**: 17
- **파일 크기**: ~3.2MB (JAR)
- **플랫폼**: Windows, macOS, Linux

---

## 📄 라이선스 및 배포

배포 시 포함해야 할 파일:
- 소스 코드 라이선스
- 사용된 라이브러리 라이선스:
  - JUnit Jupiter
  - Google Gson
  - Google Guava

---

## 🔄 업데이트 배포

새 버전 배포 시:

1. **버전 번호 업데이트**:
   ```gradle
   // build.gradle
   version = '1.1'
   ```

2. **변경사항 문서화**:
   - CHANGELOG.md 작성
   - 주요 변경사항 나열

3. **릴리스 생성**:
   ```bash
   ./gradlew clean jar
   # app-1.1.jar 생성
   ```

4. **GitHub Release**:
   - Tag 생성: `v1.1`
   - JAR 파일 첨부
   - 릴리스 노트 작성

---

## 💡 추가 참고사항

### CI/CD 자동 빌드:
- GitHub Actions를 통한 자동 빌드
- 릴리스 시 자동 JAR 생성
- 크로스 플랫폼 테스트

### 코드 서명 (선택사항):
- macOS: Apple Developer Certificate
- Windows: Code Signing Certificate
- 보안 경고 방지

---

**문의**: [GitHub Repository](https://github.com/HyunWrites0721/SE-tetris-team10)
**문서 버전**: 1.0
**최종 업데이트**: 2025-10-29
