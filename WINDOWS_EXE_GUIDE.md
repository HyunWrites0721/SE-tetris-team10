# Windows EXE 배포 가이드

## ✅ 완료! Tetris.exe 생성 성공

### 📦 생성된 파일:
- **위치**: `app/build/launch4j/Tetris.exe`
- **크기**: 3.3 MB
- **타입**: Windows 실행 파일 (.exe)

---

## 🚀 빌드 방법

### Gradle로 EXE 파일 생성:
```bash
./gradlew createExe
```

생성된 파일: `app/build/launch4j/Tetris.exe`

---

## 💡 EXE vs JAR 비교

| 특징 | JAR | EXE |
|------|-----|-----|
| **크기** | 3.23 MB | 3.3 MB |
| **플랫폼** | 모든 OS | Windows만 |
| **실행** | java -jar 필요 | 더블클릭 |
| **Java 설치** | 필수 | 필수 (17+) |
| **아이콘** | 기본 Java 아이콘 | 커스텀 가능 |
| **배포** | 단일 파일 | 단일 파일 |

---

## 🎯 사용 방법 (Windows 사용자)

### 요구사항:
- ✅ Windows 10 이상
- ✅ Java Runtime Environment (JRE) 17 이상

### 실행:
1. **Tetris.exe** 더블클릭
2. Java가 없으면 오류 메시지 표시
   - "Java 17 or higher is required to run this application"
3. [Java 다운로드](https://adoptium.net/)

---

## 🔧 고급 설정 (build.gradle)

### 현재 설정:
```gradle
launch4j {
    mainClassName = 'App'
    outfile = 'Tetris.exe'
    productName = 'Tetris Game'
    fileDescription = 'Tetris Game by SE-tetris-team10'
    version = '1.0.0.0'
    jreMinVersion = '17'
    windowTitle = 'Tetris'
}
```

### 아이콘 추가하기:
1. **icon.ico** 파일 생성 (256x256 권장)
2. `app/` 디렉토리에 저장
3. `build.gradle`에 추가:
   ```gradle
   launch4j {
       icon = "${projectDir}/icon.ico"
       // ... 나머지 설정
   }
   ```

---

## 📦 배포 패키지 구성

### 옵션 1: EXE만 배포
```
Tetris/
└── Tetris.exe
```

### 옵션 2: 완전한 패키지
```
Tetris-v1.0-Windows/
├── Tetris.exe
├── README.txt
└── LICENSE.txt
```

---

## 🎨 추가 기능

### JRE 번들링 (선택사항)
Java를 포함한 독립 실행 파일:

```gradle
launch4j {
    bundledJrePath = 'jre'  // JRE 폴더 포함
    // ... 나머지 설정
}
```

**장점**:
- ✅ Java 설치 불필요
- ✅ 버전 호환성 보장

**단점**:
- ❌ 파일 크기 증가 (~100MB)
- ❌ 업데이트 필요 시 전체 재배포

---

## 🔍 EXE 파일 정보

### 메타데이터:
- **제품명**: Tetris Game
- **파일 설명**: Tetris Game by SE-tetris-team10
- **버전**: 1.0.0.0
- **회사**: SE-tetris-team10
- **저작권**: 2025 SE-tetris-team10

### 최소 요구사항:
- Windows 10 이상
- Java 17 이상
- 512MB RAM

---

## 📋 GitHub Release 배포

### 1. JAR와 EXE 모두 업로드:
```
Assets:
- app.jar (3.23 MB) - 모든 플랫폼
- Tetris.exe (3.3 MB) - Windows 전용
- Source code (zip)
- Source code (tar.gz)
```

### 2. Release 설명 업데이트:
```markdown
## 📥 다운로드

### 🎯 Windows 사용자
👉 **Tetris.exe** 다운로드 (더블클릭 실행)

### 🎯 Mac/Linux 사용자
👉 **app.jar** 다운로드 (`java -jar app.jar`)

### 💻 개발자
👉 **Source code** 다운로드
```

---

## 🐛 문제 해결

### "Java not found" 오류:
```
해결방법:
1. Java 17 설치: https://adoptium.net/
2. 환경 변수 확인 (JAVA_HOME)
3. 시스템 재시작
```

### EXE가 실행되지 않음:
```
확인 사항:
1. Java 17 이상 설치 여부
2. 바이러스 백신 차단 여부
3. 관리자 권한으로 실행
```

### "The registry refers to a nonexistent Java Runtime" 오류:
```
해결방법:
1. Java 재설치
2. JAVA_HOME 환경 변수 설정
3. 시스템 PATH에 Java 추가
```

---

## 🚀 자동 빌드 (GitHub Actions)

### .github/workflows/build-exe.yml:
```yaml
name: Build Windows EXE

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
    
    - name: Build JAR and EXE
      run: |
        ./gradlew clean jar
        ./gradlew createExe
    
    - name: Upload artifacts
      uses: actions/upload-artifact@v3
      with:
        name: release-files
        path: |
          app/build/libs/app.jar
          app/build/launch4j/Tetris.exe
    
    - name: Create Release
      uses: softprops/action-gh-release@v1
      with:
        files: |
          app/build/libs/app.jar
          app/build/launch4j/Tetris.exe
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## 📊 통계

### 빌드 시간:
- JAR: ~10초
- EXE: ~15초 (JAR + Launch4j 래핑)

### 파일 크기 비교:
- JAR: 3.23 MB
- EXE: 3.3 MB (+70 KB)
- 차이: Launch4j 래퍼 + 메타데이터

---

## ✅ 체크리스트

배포 전 확인:
- [ ] JAR 파일 정상 실행 확인
- [ ] EXE 파일 생성 완료
- [ ] Windows에서 EXE 테스트 (가능하면)
- [ ] Java 17 최소 버전 확인
- [ ] 메타데이터 확인 (버전, 회사명 등)
- [ ] README 업데이트
- [ ] GitHub Release 노트 작성
- [ ] 아이콘 추가 (선택사항)

---

## 🎉 결과

이제 다음 두 가지 배포 옵션을 모두 사용할 수 있습니다:

1. **app.jar** - 크로스 플랫폼 (Windows, macOS, Linux)
2. **Tetris.exe** - Windows 전용 (더블클릭 실행)

사용자가 선호하는 방식을 선택할 수 있습니다! 🚀
