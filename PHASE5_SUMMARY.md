# Phase 5 구현 완료 ✅

## 📅 구현 일자
2025년 11월 26일

## 🎯 Phase 5 목표
**연결 안정성 강화** - 네트워크 지연 측정 및 연결 끊김 처리

---

## ✅ 구현 완료 항목

### 1. LatencyMonitor.java ⭐
**위치**: `app/src/main/java/network/LatencyMonitor.java`

**기능**:
- RTT(Round Trip Time) 측정 및 기록
- 최근 10개 지연 시간 히스토리 유지
- 평균/최소/최대 지연 시간 계산
- 랙(lag) 상태 판단 (200ms 기준)
- 네트워크 통계 출력

**주요 메서드**:
```java
public void recordLatency(long latency)      // 지연 시간 기록
public long getAverageLatency()              // 평균 지연 계산
public boolean isLagging()                   // 랙 여부 판단 (>200ms)
public long getMinLatency()                  // 최소 지연
public long getMaxLatency()                  // 최대 지연
public void printStats()                     // 통계 출력
public void reset()                          // 기록 초기화
```

**테스트**: `LatencyMonitorTest.java` (10개 테스트, 모두 통과 ✅)

---

### 2. DisconnectionHandler.java ⭐
**위치**: `app/src/main/java/network/DisconnectionHandler.java`

**기능**:
- 연결 끊김 상황 처리
- 사용자에게 에러 다이얼로그 표시
- P2P 메뉴로 자동 복귀
- 중복 처리 방지

**주요 메서드**:
```java
public void handleDisconnection(String reason, Window window)  // 일반 연결 끊김
public void handleTimeout(Window window)                       // 타임아웃
public void handleException(Exception ex, Window window)       // 예외 발생
public void handleNormalDisconnection(Window window)           // 정상 종료
public boolean isHandled()                                     // 처리 여부 확인
public void reset()                                            // 리셋
```

**특징**:
- UI 스레드에서 안전하게 다이얼로그 표시 (SwingUtilities.invokeLater)
- 콜백 패턴으로 유연한 처리 (onDisconnect Runnable)
- 중복 처리 방지 (isHandled 플래그)

**테스트**: `DisconnectionHandlerTest.java` (4개 테스트, 모두 통과 ✅)

---

### 3. ConnectionMonitor.java 수정 ⚙️
**변경 사항**:

#### 3.1 LatencyMonitor 통합
```java
private final LatencyMonitor latencyMonitor;
private volatile long lastHeartbeatSent;  // RTT 측정용

public ConnectionMonitor(MessageSender sender) {
    this.latencyMonitor = new LatencyMonitor();
    this.lastHeartbeatSent = System.currentTimeMillis();
    // ...
}
```

#### 3.2 RTT 측정 추가
```java
// Heartbeat 전송 시 시간 기록
private void sendHeartbeat() {
    lastHeartbeatSent = System.currentTimeMillis();
    HeartbeatMessage heartbeat = new HeartbeatMessage();
    sender.sendMessage(heartbeat);
}

// Heartbeat 수신 시 RTT 계산
public void onHeartbeatReceived() {
    long receivedTime = System.currentTimeMillis();
    long rtt = receivedTime - lastHeartbeatSent;  // RTT 계산
    latencyMonitor.recordLatency(rtt);            // 기록
    
    if (latencyMonitor.isLagging()) {
        updateState(ConnectionState.LAGGING);     // 랙 상태로 전환
    } else {
        updateState(ConnectionState.CONNECTED);
    }
}
```

#### 3.3 Getter 추가
```java
public LatencyMonitor getLatencyMonitor() {
    return latencyMonitor;
}
```

---

### 4. NetworkManager.java 수정 ⚙️
**변경 사항**:

#### 4.1 DisconnectionHandler 통합
```java
private DisconnectionHandler disconnectionHandler;

public void setDisconnectionHandler(DisconnectionHandler handler) {
    this.disconnectionHandler = handler;
}
```

#### 4.2 타임아웃 처리
```java
connectionMonitor.setStateListener(newState -> {
    if (newState == ConnectionState.TIMEOUT) {
        disconnect();
        
        if (disconnectionHandler != null && !disconnectionHandler.isHandled()) {
            disconnectionHandler.handleTimeout(null);
        }
    }
});
```

#### 4.3 연결 끊김 처리
```java
@Override
public void onConnectionLost() {
    state = ConnectionState.DISCONNECTED;
    disconnect();
    
    if (disconnectionHandler != null && !disconnectionHandler.isHandled()) {
        disconnectionHandler.handleDisconnection("상대방과의 연결이 끊어졌습니다.", null);
    }
}
```

#### 4.4 Getter 추가
```java
public ConnectionMonitor getConnectionMonitor() {
    return connectionMonitor;
}
```

---

### 5. P2PVersusFrameBoard.java 수정 🎨
**변경 사항**:

#### 5.1 네트워크 상태 UI 추가
```java
private JLabel networkStatusLabel;

// 하단에 상태 표시 패널 추가
JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
statusPanel.setBackground(Color.BLACK);
networkStatusLabel = new JLabel("⚫ 연결 확인 중...");
networkStatusLabel.setFont(FontManager.getKoreanFont(Font.PLAIN, 12));
networkStatusLabel.setForeground(Color.GRAY);
statusPanel.add(networkStatusLabel);
add(statusPanel, BorderLayout.SOUTH);
```

#### 5.2 실시간 네트워크 상태 모니터링
```java
private void startNetworkStatusMonitoring() {
    Timer statusTimer = new Timer(1000, e -> {
        ConnectionMonitor monitor = networkManager.getConnectionMonitor();
        LatencyMonitor latencyMonitor = monitor.getLatencyMonitor();
        ConnectionState state = monitor.getCurrentState();
        
        long avgLatency = latencyMonitor.getAverageLatency();
        String statusText;
        Color statusColor;
        
        switch (state) {
            case CONNECTED:
                statusText = "🟢 연결됨 (지연: " + avgLatency + "ms)";
                statusColor = new Color(0, 200, 0);
                break;
            case LAGGING:
                statusText = "🟡 랙 걸림 (지연: " + avgLatency + "ms)";
                statusColor = Color.ORANGE;
                break;
            case TIMEOUT:
                statusText = "🔴 연결 끊김";
                statusColor = Color.RED;
                break;
            default:
                statusText = "⚫ 연결 확인 중...";
                statusColor = Color.GRAY;
                break;
        }
        
        SwingUtilities.invokeLater(() -> {
            networkStatusLabel.setText(statusText);
            networkStatusLabel.setForeground(statusColor);
        });
    });
    
    statusTimer.start();
}
```

---

## 📊 테스트 결과

### LatencyMonitorTest (10/10 통과 ✅)
- ✅ `testRecordLatency()` - 지연 기록
- ✅ `testAverageLatency()` - 평균 계산
- ✅ `testMinMaxLatency()` - 최소/최대
- ✅ `testIsLagging_Normal()` - 정상 상태
- ✅ `testIsLagging_High()` - 랙 상태
- ✅ `testHistorySize()` - 히스토리 크기 제한
- ✅ `testReset()` - 초기화
- ✅ `testEmptyMonitor()` - 빈 상태
- ✅ `testPrintStats()` - 통계 출력

### DisconnectionHandlerTest (4/4 통과 ✅)
- ✅ `testIsHandled_Initial()` - 초기 상태
- ✅ `testReset()` - 리셋 기능
- ✅ `testNullCallback()` - Null 콜백 처리
- ✅ `testIsHandledAfterNormalDisconnection()` - 정상 종료 후 상태

---

## 🔄 동작 흐름

### 1. 정상 연결 시나리오
```
[1초마다]
1. ConnectionMonitor: sendHeartbeat() 호출
   → lastHeartbeatSent = 현재 시각 기록
   
2. 상대방: Heartbeat 수신 후 즉시 응답

3. ConnectionMonitor: onHeartbeatReceived() 호출
   → RTT = 수신시각 - 전송시각 계산
   → LatencyMonitor.recordLatency(RTT) 기록
   → 평균 지연 < 200ms → CONNECTED 상태 유지
   
4. P2PVersusFrameBoard: 상태 UI 업데이트
   → "🟢 연결됨 (지연: 50ms)"
```

### 2. 랙(Lag) 발생 시나리오
```
[네트워크 지연 증가]
1. RTT 측정: 250ms, 280ms, 300ms...
   → LatencyMonitor: 평균 지연 계산 = 276ms
   
2. LatencyMonitor.isLagging() = true (276ms > 200ms)
   → ConnectionMonitor: LAGGING 상태로 전환
   
3. P2PVersusFrameBoard: UI 업데이트
   → "🟡 랙 걸림 (지연: 276ms)" (주황색)
```

### 3. 연결 끊김 시나리오
```
[5초간 Heartbeat 응답 없음]
1. ConnectionMonitor: checkConnectionState()
   → timeSinceLastHeartbeat > 5000ms
   → TIMEOUT 상태로 전환
   
2. NetworkManager: StateListener 감지
   → disconnect() 호출
   → DisconnectionHandler.handleTimeout() 호출
   
3. DisconnectionHandler:
   → 에러 다이얼로그 표시: "상대방으로부터 응답이 없습니다."
   → 현재 창 닫기
   → P2P 메뉴로 복귀 (콜백 실행)
   
4. P2PVersusFrameBoard: UI 업데이트
   → "🔴 연결 끊김" (빨간색)
```

---

## 📈 성능 지표

### 지연 시간 기준
- ✅ **정상**: 0-200ms → 🟢 초록색
- ⚠️ **랙**: 200ms 초과 → 🟡 주황색
- ❌ **타임아웃**: 5초 응답 없음 → 🔴 빨간색

### 히스토리 크기
- 최근 10개 RTT 샘플 유지
- 메모리 효율적 (LinkedList 사용)
- 평균 계산 시간: O(n) = O(10) → 상수 시간

### UI 업데이트 주기
- 1초마다 상태 체크 (Timer)
- UI 스레드에서 안전하게 업데이트 (SwingUtilities)

---

## 🔧 코드 품질

### 설계 원칙
- ✅ **단일 책임 원칙**: LatencyMonitor = 지연 측정, DisconnectionHandler = 끊김 처리
- ✅ **의존성 주입**: DisconnectionHandler(Runnable callback)
- ✅ **스레드 안전성**: synchronized 메서드, volatile 변수
- ✅ **UI 스레드 안전성**: SwingUtilities.invokeLater 사용

### 에러 처리
- ✅ 중복 처리 방지 (isHandled 플래그)
- ✅ Null 안전성 (null 체크)
- ✅ 예외 처리 (try-catch)

---

## 📝 다음 단계 (Phase 6)

### Phase 6: 지연 최적화
**예상 기간**: 2-3일

**구현 항목**:
1. **EventFilter.java** 신규 생성
   - 불필요한 이벤트 필터링
   - SYNC_EVENTS Set 정의
   - TickEvent, MinorUpdateEvent 차단

2. **EventSynchronizer.java** 수정
   - EventFilter 적용
   - 전송 이벤트 수 감소
   - 성능 로깅 추가

**목표**:
- 네트워크 트래픽 50% 감소
- 평균 지연 시간 30% 개선
- CPU 사용률 감소

---

## 🎉 Phase 5 완료!

✅ **LatencyMonitor**: RTT 측정 및 랙 판정  
✅ **DisconnectionHandler**: 연결 끊김 처리 및 UI 복귀  
✅ **ConnectionMonitor**: RTT 측정 통합  
✅ **NetworkManager**: DisconnectionHandler 통합  
✅ **P2PVersusFrameBoard**: 실시간 네트워크 상태 표시  
✅ **테스트**: 14개 테스트 모두 통과  

**다음**: Phase 6 (지연 최적화) 구현 준비 완료! 🚀
