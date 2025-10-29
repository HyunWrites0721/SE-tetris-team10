# 아이템 블록 명세서 (Item Blocks Specification)

## 개요
테트리스 게임에 특수 효과를 제공하는 5가지 아이템 블록 시스템입니다. 아이템 블록은 **아이템 모드(Item Mode)** 에서만 생성되며, 각 블록은 고유한 시각적 식별자와 특수 효과를 가집니다.

---

## 1. AllClearBlock (전체 클리어 블록)

### 📋 기본 정보
- **클래스**: `blocks.item.AllClearBlock`
- **식별값**: `2`
- **모양**: 2x2 정사각형
- **색상**: 특수 색상 (blindColor_1, colorIndex=7)

### 🎮 효과
- **발동 조건**: 블록이 보드에 배치될 때
- **효과**: 게임 보드의 모든 블록 제거 (벽 제외)
- **애니메이션**: 160ms 깜빡임 효과

### 💰 점수 계산
```
점수 = 500 × 속도 가중치 × 난이도 가중치
```
- 속도 가중치: (현재 속도 레벨 + 1), 범위: 1~7
- 난이도 가중치: Easy=0.9, Normal=1.0, Hard=1.1

### 📊 예시
- 속도 레벨 0, Normal 난이도: 500 × 1 × 1.0 = **500점**
- 속도 레벨 3, Hard 난이도: 500 × 4 × 1.1 = **2,200점**
- 속도 레벨 6, Easy 난이도: 500 × 7 × 0.9 = **3,150점**

---

## 2. BoxClearBlock (박스 폭발 블록)

### 📋 기본 정보
- **클래스**: `blocks.item.BoxClearBlock`
- **식별값**: `3`
- **모양**: 3x3 십자 형태 (중앙만 채워짐)
```
0 0 0
0 3 0
0 0 0
```
- **색상**: 특수 색상 (blindColor_1, colorIndex=7)

### 🎮 효과
- **발동 조건**: 블록이 보드에 배치될 때
- **효과**: 블록 중심으로부터 5x5 영역의 모든 블록 폭발 제거
- **애니메이션**: 120ms 깜빡임 효과 후 중력 적용
- **중력**: 폭발 후 위의 블록들이 아래로 낙하

### 💰 점수 계산
```
점수 = 2줄 클리어와 동일
점수 = 300 × 현재 레벨 × 난이도 가중치
```

### 📊 예시 (레벨 10 기준)
- Normal 난이도: 300 × 10 × 1.0 = **3,000점**
- Hard 난이도: 300 × 10 × 1.1 = **3,300점**
- Easy 난이도: 300 × 10 × 0.9 = **2,700점**

---

## 3. OneLineClearBlock (1줄 보너스 블록)

### 📋 기본 정보
- **클래스**: `blocks.item.OneLineClearBlock`
- **식별값**: `4`
- **모양**: 일반 테트로미노 모양 (7가지 중 하나에 'L' 마크 추가)
- **특징**: 기본 블록 형태를 유지하며, 랜덤으로 한 칸에 'L' 표시

### 🎮 효과
- **발동 조건**: 'L' 마크가 포함된 줄이 완전히 채워져 삭제될 때
- **효과**: 실제 지워진 줄보다 1줄 많은 점수 획득
- **애니메이션**: 일반 라인 클리어와 동일

### 💰 점수 계산
```
점수 = calculateLineClearScore(지워진 줄 수 + 1)
```

**기본 점수 테이블 (+1줄 보너스 적용)**:
| 실제 클리어 | 계산 줄수 | 기본 점수 |
|------------|----------|----------|
| 1줄 | 2줄 | 300 |
| 2줄 | 3줄 | 500 |
| 3줄 | 4줄 (테트리스) | 800 |
| 4줄 | 5줄 | 1200 |

### 📊 예시 (레벨 5, Normal 난이도)
- 1줄 클리어 + L블록: 300 × 5 × 1.0 = **1,500점** (원래 750점)
- 4줄 클리어 + L블록: 1200 × 5 × 1.0 = **6,000점** (원래 4,000점)

---

## 4. ScoreDoubleBlock (점수 2배 블록)

### 📋 기본 정보
- **클래스**: `blocks.item.ScoreDoubleBlock`
- **식별값**: `5`
- **모양**: 일반 테트로미노 모양 (7가지 중 하나에 'D' 마크 추가)
- **특징**: 기본 블록 형태를 유지하며, 랜덤으로 한 칸에 'D' 표시

### 🎮 효과
- **발동 조건**: 라인 클리어 발생 시
- **효과**: 'D' 마크가 포함된 줄의 점수를 2배로 계산
- **계산 방식**: 
  - 일반 줄: 정상 점수
  - D 포함 줄: 점수 × 2

### 💰 점수 계산
```
총 점수 = 일반줄 점수 + (D포함줄 점수 × 2)
```

### 📊 예시 (레벨 3, Normal 난이도, 2줄 클리어)
**케이스 1: D블록이 1줄에 포함**
- 일반 1줄: 100 × 3 × 1.0 = 300점
- D포함 1줄: 100 × 3 × 1.0 × 2 = 600점
- 총점: **900점** (원래 900점)

**케이스 2: D블록이 2줄에 포함**
- 일반 0줄: 0점
- D포함 2줄: 300 × 3 × 1.0 × 2 = 1,800점
- 총점: **1,800점** (원래 900점)

---

## 5. WeightBlock (무게 블록)

### 📋 기본 정보
- **클래스**: `blocks.item.WeightBlock`
- **식별값**: `6`
- **모양**: 4x4 크기의 하단 무거운 형태
```
0 0 0 0
0 0 0 0
0 6 6 0
6 6 6 6
```
- **색상**: 특수 색상 (blindColor_1, colorIndex=8)

### 🎮 효과
- **발동 조건**: 블록이 바닥이나 다른 블록에 닿을 때
- **특수 동작**: 
  1. 바닥까지 뚫고 내려가며 경로상의 모든 블록 제거
  2. 바닥에 도달하면 자동 소멸 (보드에 남지 않음)
  3. 새 블록 자동 생성
- **애니메이션**: 특수 낙하 애니메이션

### 💰 점수 계산
- **직접 점수 없음**: WeightBlock 자체로는 점수를 얻지 않음
- **간접 효과**: 블록을 제거하여 라인 클리어 가능성 증가

### ⚠️ 주의사항
- WeightBlock은 보드에 쌓이지 않고 소멸
- 경로상의 아이템 블록도 함께 제거
- 게임오버 방지 전략적 사용 가능

---

## 아이템 블록 생성 확률

### 아이템 모드(Item Mode)에서만 생성
```java
// 10% 확률로 아이템 블록 생성
if (itemMode && Math.random() < 0.1) {
    // 아이템 블록 종류 랜덤 선택
}
```

### 아이템 타입 분포
각 아이템은 균등한 확률로 생성됩니다:
- AllClearBlock: 20%
- BoxClearBlock: 20%
- OneLineClearBlock: 20%
- ScoreDoubleBlock: 20%
- WeightBlock: 20%

---

## 시각적 표현

### 게임 내 색상 코드
| 블록 타입 | 값 | 표시 | 색상 |
|----------|---|-----|------|
| AllClearBlock | 2 | A | 특수색 (7) |
| BoxClearBlock | 3 | B | 특수색 (7) |
| OneLineClearBlock | 4 | L | 기본 블록색 |
| ScoreDoubleBlock | 5 | D | 기본 블록색 |
| WeightBlock | 6 | W | 특수색 (8) |

### 애니메이션 타이밍
- AllClear 깜빡임: **160ms**
- BoxClear 깜빡임: **120ms**
- 일반 라인 클리어: **150ms**

---

## 난이도 시스템

### 난이도별 점수 배율
```java
private static final double[] DIFFICULTY_MULTIPLIERS = {1.0, 1.1, 0.9};
// [0] Normal: 1.0 (기본)
// [1] Hard: 1.1 (+10%)
// [2] Easy: 0.9 (-10%)
```

### 속도 시스템
- **7단계 속도 레벨** (0~6)
- 점수 가중치: (레벨 + 1), 즉 1배~7배
- 블록 30개 생성마다 또는 줄 5개 클리어마다 속도 증가

---

## 기술적 구현 세부사항

### 아이템 감지 및 처리 위치
```java
// GameModel.placePiece() 메서드에서 아이템 감지
- hasTwo (값 2) → startAllClearAnimation()
- hasThree (값 3) → startBoxClearAnimation()
- hasFour (값 4) → startItemRowFlashAndClear()
- hasFive (값 5) → performImmediateLineClear() with score doubling
- hasSix (값 6) → applyWeightEffectAndDespawn()
```

### 점수 계산 함수
```java
// GameModel.calculateLineClearScore(int linesCleared)
switch (linesCleared) {
    case 1: baseScore = 100;
    case 2: baseScore = 300;
    case 3: baseScore = 500;
    case 4: baseScore = 800; // 테트리스
    default: baseScore = 100 * linesCleared;
}
return baseScore × currentLevel × difficultyMultiplier;
```

---

## 버전 정보
- **작성일**: 2025-10-29
- **게임 버전**: SE-tetris-team10 (modeSep branch)
- **테스트 커버리지**: 
  - blocks.item 패키지: 97% (line coverage)
  - 전체: 69% (line coverage)

---

## 참고사항
1. 모든 아이템 블록은 `blocks.Block` 클래스를 상속
2. 아이템 효과는 블록이 보드에 배치(place)될 때 발동
3. 애니메이션 중에는 게임 타이머 일시정지
4. 점수는 항상 `lastLineClearScore`에 저장 후 `FrameBoard.increaseScore()`로 전달
