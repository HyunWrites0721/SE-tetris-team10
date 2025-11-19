# 대전 모드 (Versus Mode) 명세서

## 1. 개요

### 1.1 목적
- 한 PC에서 두 명의 플레이어가 동시에 대전할 수 있는 1 vs 1 테트리스 게임 모드
- 줄 삭제를 통한 공격 시스템으로 상대방을 방해하여 승부 결정

### 1.2 게임 방식
- **화면 구성**: 좌우 분할 화면 (1200x600 픽셀)
  - Player 1: 왼쪽 화면 (600x600)
  - Player 2: 오른쪽 화면 (600x600)
- **입력 방식**: 하나의 키보드를 양쪽 플레이어가 나눠 사용
- **승리 조건**: 상대방이 먼저 게임 오버될 때까지 생존

---

## 2. 시스템 아키텍처

### 2.1 주요 컴포넌트

#### 2.1.1 VersusFrameBoard
- **역할**: 대전 모드의 메인 컨테이너
- **크기**: 1200x600 픽셀 (2배 너비)
- **레이아웃**: GridLayout(1, 2) - 좌우 2개 패널
- **관리 요소**:
  - 2개의 독립적인 GameController (gameController1, gameController2)
  - 2개의 독립적인 GameView (gameView1, gameView2)
  - 2개의 VersusAttackManager (attackManager1, attackManager2)
  - VersusKeyListener (공유 키보드 입력)

#### 2.1.2 VersusMenuFrame
- **역할**: 대전 모드 선택 메뉴
- **게임 모드**:
  - NORMAL: 일반 대전
  - ITEM: 아이템 모드 대전
  - TIME_LIMIT: 시간 제한 대전 (미구현)

#### 2.1.3 VersusAttackManager
- **역할**: 공격 줄 관리
- **주요 기능**:
  - 공격 줄 계산: `calculateAttackLines(int linesCleared)`
  - 공격 줄 수신 및 큐 관리: `receiveAttack(int lines)`
  - 최대 대기 공격 줄: 10줄

#### 2.1.4 VersusKeyListener
- **역할**: 두 플레이어의 키보드 입력 분리 처리
- **키 매핑**: (아래 섹션 3.1 참조)

#### 2.1.5 VersusPauseBoard
- **역할**: 대전 모드 전용 일시정지 화면
- **특징**: FrameBoard 의존성 없이 독립 실행

#### 2.1.6 VersusResultBoard
- **역할**: 게임 종료 시 승자 표시
- **표시 정보**: 승리한 플레이어, 최종 점수

---

## 3. 게임 플레이

### 3.1 조작 방법

#### Player 1 (왼쪽)
| 키 | 동작 |
|---|---|
| W | 블록 회전 |
| A | 왼쪽 이동 |
| S | 소프트 드롭 (빠른 낙하) |
| D | 오른쪽 이동 |
| F | 하드 드롭 (즉시 착지) |

#### Player 2 (오른쪽)
| 키 | 동작 |
|---|---|
| I | 블록 회전 |
| J | 왼쪽 이동 |
| K | 소프트 드롭 (빠른 낙하) |
| L | 오른쪽 이동 |
| ; (세미콜론) | 하드 드롭 (즉시 착지) |

#### 공통
| 키 | 동작 |
|---|---|
| ESC | 일시정지/재개 토글 |

### 3.2 공격 시스템

#### 3.2.1 공격 조건
- **최소 줄 수**: 2줄 이상 삭제해야 공격 가능
- 1줄 삭제 시 공격 불가

#### 3.2.2 공격 줄 계산
```
삭제 줄 수 → 공격 줄 수
1줄 → 0줄 (공격 없음)
2줄 → 2줄
3줄 → 3줄
4줄 → 4줄
```

#### 3.2.3 공격 실행 메커니즘
1. Player가 줄을 삭제하면 `LineClearedEvent` 발생
2. `VersusFrameBoard.handleLineCleared()` 호출
3. `VersusAttackManager.calculateAttackLines()` - 공격 줄 수 계산
4. 상대방의 `VersusAttackManager.receiveAttack()` - 공격 줄 큐에 추가
5. 즉시 `GameController.addAttackLines()` - 상대방 보드에 공격 줄 추가

#### 3.2.4 공격 줄 특성
- **위치**: 보드 하단에 추가
- **색상**: 회색 (colorCode 8)
- **구멍**: 블록 패턴 모양대로 생성 (⚠️ 현재 구현 중 - 추후 수정 필요)
  - 현재: 블록 shape 정보를 저장하여 전달
  - 목표: 공격자가 놓은 블록의 정확한 형태대로 구멍 생성
- **효과**: 기존 블록들이 위로 밀려남

#### 3.2.5 공격 큐 시스템
- **최대 대기 줄**: 10줄
- **초과 처리**: 10줄을 넘는 공격은 버려짐 (truncate)
- **즉시 적용**: 큐에 추가된 공격 줄은 즉시 보드에 반영

### 3.3 점수 시스템
- **독립 점수**: 각 플레이어는 독립적인 점수 보유
- **실시간 동기화**: `ScoreUpdateEvent`를 통해 실시간 점수 갱신
- **HighScore 패널**: 대전 모드에서는 표시하지 않음
  - `GameView(boolean item, boolean showHighScore)` 생성자 사용
  - `showHighScore = false`로 설정

### 3.4 승패 결정
- **게임 오버**: 블록이 상단 경계를 넘어서면 해당 플레이어 패배
- **승자**: 상대방이 게임 오버되면 자동 승리
- **결과 화면**: VersusResultBoard에서 승자와 점수 표시

---

## 4. 이벤트 시스템

### 4.1 사용되는 이벤트

#### 4.1.1 LineClearedEvent
- **발생 시점**: 줄이 삭제될 때
- **구독자**: VersusFrameBoard
- **데이터**: 삭제된 줄 수 (`linesCleared`)
- **용도**: 공격 시스템 트리거

#### 4.1.2 ScoreUpdateEvent
- **발생 시점**: 점수가 변경될 때
- **발행자**: GameController.addScore()
- **구독자**: VersusFrameBoard (각 플레이어 패널)
- **용도**: 실시간 점수 동기화

#### 4.1.3 GameOverEvent
- **발생 시점**: 게임 오버 조건 충족 시
- **구독자**: VersusFrameBoard
- **용도**: 승패 판정 및 결과 화면 전환

#### 4.1.4 TickEvent
- **발생 시점**: 게임 루프의 각 틱마다
- **용도**: 블록 자동 낙하, 게임 상태 업데이트

---

## 5. 화면 구성

### 5.1 레이아웃 구조
```
VersusFrameBoard (1200x600)
├─ Player 1 Panel (600x600)
│  ├─ GameView (보드, 점수, Next 블록)
│  └─ GameController (게임 로직)
└─ Player 2 Panel (600x600)
   ├─ GameView (보드, 점수, Next 블록)
   └─ GameController (게임 로직)
```

### 5.2 GameView 구성 (각 플레이어)
- **보드**: 10x20 테트리스 보드
- **점수 패널**: 현재 점수 표시
- **Next 블록**: 다음에 나올 블록 미리보기
- **레벨/줄 정보**: 현재 레벨 및 삭제한 줄 수
- **HighScore 패널**: 대전 모드에서는 숨김

### 5.3 화면 비율
- `safeScreenRatio()`: 화면 크기에 따른 자동 스케일링
- 대전 모드는 2배 너비로 인해 비율 조정 필요

---

## 6. 파일 구조

### 6.1 새로 추가된 파일

```
app/src/main/java/versus/
├── VersusFrameBoard.java       # 대전 모드 메인 컨테이너
├── VersusMenuFrame.java        # 대전 모드 선택 메뉴
├── VersusMode.java             # 게임 모드 enum
├── VersusGameStart.java        # 대전 모드 시작 진입점
├── VersusKeyListener.java      # 키보드 입력 분리 처리
├── VersusAttackManager.java    # 공격 시스템 관리
├── VersusPauseBoard.java       # 대전 모드 일시정지
└── VersusResultBoard.java      # 결과 화면
```

### 6.2 수정된 파일

```
app/src/main/java/
├── start/StartFrame.java       # 대전 모드 버튼 추가
├── game/
│   ├── core/
│   │   └── GameController.java # addAttackLines() 메서드 추가
│   │                           # lastBlockPattern, lastBlockX 필드 추가
│   │                           # getLastBlockInfo() 메서드 추가
│   └── view/GameView.java      # showHighScore 파라미터 추가
└── events/
    └── ScoreUpdateEvent.java   # 점수 동기화 이벤트 (신규)
```

---

## 7. 주요 메서드

### 7.1 GameController

#### addAttackLines(int lines, int[][] blockPattern, int blockX)
```java
/**
 * 공격 줄 추가 (대전 모드용)
 * @param lines 추가할 줄 수
 * @param blockPattern 블록의 패턴 (shape)
 * @param blockX 블록의 X 위치
 */
```
- 보드 하단에 공격 줄 추가
- 기존 블록들을 위로 밀어올림
- 블록 패턴 모양대로 구멍 생성 (⚠️ 추후 수정 필요)

#### getLastBlockInfo()
```java
/**
 * 마지막 블록 패턴 정보 반환 (대전 모드 공격용)
 * @return [0]: 블록 패턴 배열, [1]: 블록 X 위치
 */
```

### 7.2 VersusAttackManager

#### calculateAttackLines(int linesCleared)
```java
/**
 * 줄 삭제로 인한 공격 계산
 * @param linesCleared 삭제한 줄 수
 * @return 상대방에게 보낼 공격 줄 수 (2줄 이상일 때만 공격 가능)
 */
```
- 2줄 이상 삭제 시에만 공격
- 삭제한 줄 수와 동일한 공격 줄 반환

#### receiveAttack(int lines)
```java
/**
 * 공격 줄 수신 및 큐에 추가
 * @param lines 받은 공격 줄 수
 * @return 실제로 추가된 공격 줄 수 (최대 10줄)
 */
```
- 최대 10줄 제한
- 초과분은 자동 버림

### 7.3 VersusFrameBoard

#### handleLineCleared(int player, int linesCleared)
```java
/**
 * 줄 삭제 처리 (공격 시스템)
 * @param player 줄을 삭제한 플레이어 (1 또는 2)
 * @param linesCleared 삭제한 줄 수
 */
```
- 공격 줄 계산
- 상대방 AttackManager에 공격 전달
- 즉시 상대방 보드에 공격 줄 추가

---

## 8. 난이도 설정

### 8.1 지원 난이도
- **NORMAL**: 보통 속도
- **HARD**: 빠른 속도
- **EASY**: 느린 속도

### 8.2 난이도 적용
- 각 플레이어는 동일한 난이도로 플레이
- VersusMenuFrame에서 선택한 난이도가 양쪽 모두 적용

---

## 9. 알려진 이슈 및 개선 사항

### 9.1 현재 알려진 문제

#### ⚠️ 구멍 패턴 생성 로직 (우선순위: 높음)
- **현상**: 블록 패턴대로 구멍이 정확히 생성되지 않음
- **원인**: 블록 shape 좌표계와 보드 좌표계의 매핑 오류
- **현재 구현**: 
  - `lastBlockPattern`과 `lastBlockX` 저장
  - `addAttackLines()`에서 패턴 적용 시도
- **필요한 수정**:
  - 블록이 실제로 보드에 배치된 정확한 열 위치 추적
  - shape 배열과 보드 배열의 좌표 변환 로직 수정
  - 패턴의 상하 반전 여부 확인

### 9.2 개선 가능한 부분

#### 공격 줄 시각 효과
- 공격 줄이 추가될 때 애니메이션 효과 추가 가능
- 현재: 즉시 추가
- 개선안: 밀려 올라가는 애니메이션

#### 콤보 시스템
- 연속으로 줄을 삭제하면 추가 공격 줄 보너스
- 현재: 단순 삭제 줄 수만큼 공격
- 개선안: 콤보 카운터 및 보너스 시스템

#### 공격 줄 큐 시각화
- 대기 중인 공격 줄 수를 화면에 표시
- 현재: 즉시 적용되어 큐가 비어있음
- 개선안: 공격 줄 미리보기 UI

#### TIME_LIMIT 모드
- 현재 미구현
- 일정 시간 후 더 높은 점수를 가진 플레이어 승리

---

## 10. 테스트 시나리오

### 10.1 기본 동작 테스트
1. ✅ StartFrame에서 "대전 모드" 버튼 클릭
2. ✅ VersusMenuFrame에서 모드 선택
3. ✅ 게임 시작 시 좌우 2개 화면 표시
4. ✅ 각 플레이어 독립적으로 블록 조작 가능
5. ✅ Next 블록이 각각 다르게 표시

### 10.2 조작 테스트
- ✅ Player 1: W/A/S/D/F 키로 블록 조작
- ✅ Player 2: I/J/K/L/; 키로 블록 조작
- ✅ ESC 키로 일시정지/재개

### 10.3 공격 시스템 테스트
- ✅ 1줄 삭제 → 공격 없음
- ✅ 2줄 삭제 → 상대방 보드에 2줄 추가
- ✅ 3줄 삭제 → 상대방 보드에 3줄 추가
- ✅ 4줄 삭제 → 상대방 보드에 4줄 추가
- ⚠️ 공격 줄의 구멍 패턴 확인 (추후 수정 필요)

### 10.4 승패 테스트
- ✅ 한쪽 플레이어 게임 오버 시 상대방 승리
- ✅ VersusResultBoard에서 승자 표시
- ✅ 최종 점수 표시

### 10.5 점수 시스템 테스트
- ✅ 각 플레이어 독립적으로 점수 획득
- ✅ 점수 실시간 업데이트
- ✅ HighScore 패널 숨김 확인

---

## 11. 참고 사항

### 11.1 디버그 로그
- `handleLineCleared()`: 공격 발생 시 콘솔 출력
  ```
  Player 1 attacks Player 2 with X lines
  ```
- `handleBlockLanding()`: 블록 배치 시 좌표 출력
  ```
  Block placed at x=5, y=10, specialType=0
  ```

### 11.2 개발 가이드
- 새로운 공격 효과 추가 시: `VersusAttackManager.calculateAttackLines()` 수정
- 키 매핑 변경 시: `VersusKeyListener` 수정
- 화면 레이아웃 변경 시: `VersusFrameBoard.createPlayerPanel()` 수정

### 11.3 의존성
- EventBus: 이벤트 기반 통신
- GameController: 게임 로직 처리
- GameView: 화면 렌더링
- Block: 블록 shape 정보

---

## 12. 버전 정보

- **작성일**: 2025년 11월 19일
- **프로젝트**: SE-tetris-team10
- **브랜치**: GameEngineRefact
- **대전 모드 버전**: 1.0 (Beta)

---

## 13. 향후 개발 계획

### Phase 1 (필수)
- [ ] 구멍 패턴 생성 로직 수정
- [ ] 공격 줄 추가 애니메이션

### Phase 2 (선택)
- [ ] 콤보 시스템 구현
- [ ] 공격 큐 시각화
- [ ] TIME_LIMIT 모드 구현

### Phase 3 (확장)
- [ ] 네트워크 대전 모드
- [ ] 리플레이 기능
- [ ] 통계 및 전적 시스템
