# P2P 네트워크 대전 구현 현황 및 다음 단계

## 📊 현재 구현 상태 확인 (2025년 11월 26일)

### ✅ Phase 1: 네트워크 인프라 구축 - **완료**
- [x] `network/` 패키지 생성
- [x] `ConnectionManager.java` - Socket 연결 관리
- [x] `MessageSender.java` - 송신 스레드
- [x] `MessageReceiver.java` - 수신 스레드
- [x] `ConnectionMonitor.java` - 연결 상태 모니터링 **⚠️ Heartbeat만 구현됨**
- [x] `NetworkConfig.java` - 설정 상수
- [x] `NetworkRole.java` - SERVER/CLIENT enum
- [x] `NetworkManager.java` - 네트워크 총괄
- [x] P2P UI 구현
  - [x] `P2PMenuFrame.java` - 서버/클라이언트 선택
  - [x] `P2PServerSetupFrame.java` - 서버 설정
  - [x] `P2PClientSetupFrame.java` - 클라이언트 설정
  - [x] `P2PConnectionDialog.java` - 연결 진행 상태

**테스트:**
- [x] `ConnectionManagerTest.java`
- [x] `NetworkManagerTest.java`
- [x] `P2PHandshakeIntegrationTest.java`

---

### ✅ Phase 2: 게임 이벤트 동기화 - **완료**
- [x] `network/messages/` 패키지
  - [x] `NetworkMessage.java` - 기본 메시지 클래스
  - [x] `MessageType.java` - 메시지 타입 enum
  - [x] `GameEventMessage.java` - 게임 이벤트 래퍼
- [x] `EventSynchronizer.java` - 이벤트 동기화 로직
- [x] `P2PVersusFrameBoard.java` - P2P 게임 화면
- [x] `RemoteGamePanel.java` - 상대방 게임 표시
- [x] `RemoteGameState.java` - 상대방 게임 상태
- [x] `P2PKeyListener.java` - 로컬 키 입력만 처리

**테스트:**
- [x] `NetworkMessageTest.java`
- [x] `GameEventMessageTest.java`

---

### ✅ Phase 3: 게임 시작/종료 제어 - **완료**
- [x] `GameControlMessage.java` - 게임 제어 메시지
  - [x] START_GAME, PAUSE_GAME, RESUME_GAME 등
- [x] `P2PWaitingRoom.java` - 대기실 구현
- [x] 게임 시작 프로토콜 구현
  - [x] 모드 선택 동기화
  - [x] READY 메시지 교환
  - [x] 양쪽 동시 시작

---

### ✅ Phase 4: 공격 시스템 동기화 - **완료**
- [x] `AttackMessage.java` - 공격 메시지
  - [x] attackLines, playerId, blockPattern, blockX
- [x] 공격 동기화 로직 (`EventSynchronizer.java`)
  - [x] LineClearedEvent → AttackMessage 생성
  - [x] 블록 패턴 포함하여 전송
  - [x] 상대방 GameController.addAttackLines() 호출

**확인된 코드:**
```java
// EventSynchronizer.java line 119-138
if (event instanceof LineClearedEvent) {
    int linesCleared = ((LineClearedEvent)event).getLinesCleared();
    int attackLines = versus.VersusAttackManager.calculateAttackLines(linesCleared);
    if (attackLines > 0) {
        Object[] info = myController.getLastBlockInfo();
        int[][] pattern = (int[][])info[0];
        int blockX = (int)info[1];
        AttackMessage am = new AttackMessage(attackLines, myPlayerId, pattern, blockX);
        sender.sendMessage(am);
    }
}
```

---

## 🚧 미구현 Phase (작업 필요)

### ⚠️ Phase 5: 연결 안정성 및 에러 처리 - **부분 구현**

#### ✅ 이미 구현된 것:
1. **Heartbeat 시스템** (`ConnectionMonitor.java`)
   - [x] 1초마다 Heartbeat 전송
   - [x] Heartbeat 수신 확인
   - [x] `onHeartbeatReceived()` 메서드

2. **기본 상태 모니터링**
   - [x] `ConnectionState` enum (CONNECTED, LAGGING, TIMEOUT)
   - [x] `checkConnectionState()` - 타임아웃/지연 감지
   - [x] `NetworkConfig.HEARTBEAT_TIMEOUT = 5000` (5초)
   - [x] `NetworkConfig.LAG_THRESHOLD = 200` (200ms)

#### ❌ 구현 필요한 것:

1. **LatencyMonitor 구현** - 새 파일 생성 필요
   ```java
   // network/LatencyMonitor.java (신규 파일)
   public class LatencyMonitor {
       private Queue<Long> latencyHistory = new LinkedList<>();
       
       public void recordLatency(long latency) {
           latencyHistory.offer(latency);
           if (latencyHistory.size() > 10) {
               latencyHistory.poll();
           }
       }
       
       public boolean isLagging() {
           return getAverageLatency() > NetworkConfig.LAG_THRESHOLD;
       }
       
       public long getAverageLatency() {
           return latencyHistory.stream()
               .mapToLong(Long::longValue)
               .average()
               .orElse(0);
       }
   }
   ```

2. **DisconnectionHandler 구현** - 새 파일 생성 필요
   ```java
   // network/DisconnectionHandler.java (신규 파일)
   public class DisconnectionHandler {
       public enum DisconnectionReason {
           NETWORK_TIMEOUT,
           OPPONENT_QUIT,
           CONNECTION_ERROR
       }
       
       public void handleDisconnection(DisconnectionReason reason) {
           // 현재 게임 중단
           // 에러 메시지 표시
           // P2P 메뉴로 돌아가기
       }
   }
   ```

3. **ConnectionMonitor 개선** - 기존 파일 수정
   - [ ] LatencyMonitor 통합
   - [ ] 지연 시간 측정 로직 추가
   - [ ] 상태 변경 시 UI 알림

4. **UI 에러 표시** - P2PVersusFrameBoard 수정
   - [ ] "랙 걸림 상태" 노란색 경고 (200ms 이상)
   - [ ] "연결 끊김" 빨간색 다이얼로그 (5초 무응답)
   - [ ] DisconnectionHandler 연동

5. **MessageReceiver 개선** - 기존 파일 수정
   - [ ] Heartbeat 수신 시 타임스탬프 기록
   - [ ] 왕복 시간(RTT) 계산
   - [ ] LatencyMonitor에 기록

---

### ❌ Phase 6: 지연 최적화 및 동기화 - **미구현**

#### 필요한 작업:

1. **EventFilter 구현** - 새 파일 생성
   ```java
   // network/EventFilter.java (신규 파일)
   public class EventFilter {
       private static final Set<Class<?>> SYNC_EVENTS = Set.of(
           BlockMovedEvent.class,
           BlockRotatedEvent.class,
           BlockLandedEvent.class,
           LineClearedEvent.class,
           GameOverEvent.class,
           ScoreUpdateEvent.class
       );
       
       public boolean shouldSync(GameEvent event) {
           return SYNC_EVENTS.contains(event.getClass());
       }
   }
   ```

2. **MessageBatcher 구현** - 새 파일 생성 (선택적)
   ```java
   // network/MessageBatcher.java (신규 파일)
   public class MessageBatcher {
       private List<NetworkMessage> buffer = new ArrayList<>();
       private static final int BATCH_SIZE = 5;
       private static final long BATCH_TIMEOUT = 50;
       
       public void add(NetworkMessage msg);
       public void flush();
   }
   ```

3. **EventSynchronizer 최적화** - 기존 파일 수정
   - [ ] EventFilter 적용
   - [ ] 불필요한 이벤트 전송 차단 (TickEvent 등)
   - [ ] 메시지 배치 전송 (선택적)

4. **성능 측정 추가**
   - [ ] 이벤트 전송 시간 측정
   - [ ] 직렬화/역직렬화 시간 측정
   - [ ] 로그로 성능 기록

---

### ❌ Phase 8: 자동화 테스트 - **부분 구현**

#### ✅ 이미 구현된 테스트:
- [x] `ConnectionManagerTest.java` - 기본 연결 테스트
- [x] `NetworkManagerTest.java` - 네트워크 매니저 테스트
- [x] `NetworkMessageTest.java` - 메시지 직렬화 테스트
- [x] `GameEventMessageTest.java` - 게임 이벤트 메시지 테스트
- [x] `P2PHandshakeIntegrationTest.java` - 통합 테스트

#### ❌ 추가 필요한 테스트:

1. **네트워크 시뮬레이션 테스트**
   ```java
   // test/utils/NetworkSimulator.java (신규 파일)
   public class NetworkSimulator {
       public void simulateLatency(int milliseconds);
       public void simulatePacketLoss(double lossRate);
       public void simulateDisconnection();
   }
   ```

2. **HeartbeatManagerTest.java** - 신규 파일
   - [ ] Heartbeat 전송 테스트
   - [ ] 타임아웃 감지 테스트
   - [ ] 상태 변경 테스트

3. **LatencyMonitorTest.java** - 신규 파일
   - [ ] 지연 시간 기록 테스트
   - [ ] 평균 계산 테스트
   - [ ] 랙 판정 테스트

4. **EventSynchronizationTest.java** - 신규 파일
   - [ ] 이벤트 동기화 테스트
   - [ ] 공격 시스템 테스트
   - [ ] 양방향 통신 테스트

5. **고부하 테스트**
   - [ ] 빠른 블록 이동 (초당 10회)
   - [ ] 동시 다발 이벤트
   - [ ] 메시지 큐 오버플로우 테스트

---

### ❌ Phase 7: UI/UX 개선 - **미구현**

#### 필요한 작업:

1. **ConnectionHistory 구현** - 새 파일
   ```java
   // p2p/ConnectionHistory.java (신규 파일)
   public class ConnectionHistory {
       private static final String HISTORY_FILE = "connection_history.json";
       private List<String> recentIPs = new ArrayList<>();
       
       public void saveConnection(String ip);
       public List<String> getRecentIPs();
   }
   ```

2. **ConnectionProgressDialog 개선** - 기존 파일 수정
   - [ ] 진행 상태 바 추가
   - [ ] 상태 메시지 업데이트
   - [ ] "서버 시작 중..." → "핸드셰이크 진행 중..." → "연결 완료!"

3. **NetworkStatusIndicator 구현** - 새 파일
   ```java
   // p2p/NetworkStatusIndicator.java (신규 파일)
   public class NetworkStatusIndicator extends JPanel {
       private ConnectionState state;
       private long latency;
       
       @Override
       protected void paintComponent(Graphics g) {
           // GREEN: 정상 (< 100ms)
           // YELLOW: 지연 (100-200ms)
           // RED: 랙 (> 200ms)
           // GRAY: 연결 끊김
       }
   }
   ```

4. **P2PClientSetupFrame 개선** - 기존 파일 수정
   - [ ] 최근 접속 IP 드롭다운 추가
   - [ ] ConnectionHistory 연동

5. **P2PVersusFrameBoard 개선** - 기존 파일 수정
   - [ ] NetworkStatusIndicator 추가 (우측 상단)
   - [ ] 연결 상태 실시간 표시

---

## 🎯 당신이 구현할 순서: Phase 5 → 6 → 8 → 7

### 📍 Step 1: Phase 5 완성 (연결 안정성) - **최우선**

#### 작업 1-1: LatencyMonitor 구현
```bash
# 새 파일 생성
app/src/main/java/network/LatencyMonitor.java
```

**구현 내용:**
- [ ] `recordLatency(long latency)` - 지연 시간 기록
- [ ] `getAverageLatency()` - 평균 지연 계산
- [ ] `isLagging()` - 랙 판정 (200ms 기준)
- [ ] 최근 10개 지연 시간 유지 (Queue)

**예상 코드:**
```java
package network;

import java.util.LinkedList;
import java.util.Queue;

public class LatencyMonitor {
    private final Queue<Long> latencyHistory = new LinkedList<>();
    private static final int HISTORY_SIZE = 10;
    
    public synchronized void recordLatency(long latency) {
        latencyHistory.offer(latency);
        if (latencyHistory.size() > HISTORY_SIZE) {
            latencyHistory.poll();
        }
        System.out.println("📊 현재 지연: " + latency + "ms, 평균: " + getAverageLatency() + "ms");
    }
    
    public synchronized long getAverageLatency() {
        if (latencyHistory.isEmpty()) return 0;
        return (long) latencyHistory.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0);
    }
    
    public boolean isLagging() {
        return getAverageLatency() > NetworkConfig.LAG_THRESHOLD;
    }
    
    public synchronized long getLastLatency() {
        return latencyHistory.isEmpty() ? 0 : ((LinkedList<Long>)latencyHistory).getLast();
    }
}
```

---

#### 작업 1-2: DisconnectionHandler 구현
```bash
# 새 파일 생성
app/src/main/java/network/DisconnectionHandler.java
```

**구현 내용:**
- [ ] `DisconnectionReason` enum 정의
- [ ] `handleDisconnection(reason)` - 연결 끊김 처리
- [ ] 게임 중단 로직
- [ ] 에러 다이얼로그 표시
- [ ] P2P 메뉴로 복귀

**예상 코드:**
```java
package network;

import javax.swing.*;
import java.awt.*;

public class DisconnectionHandler {
    
    public enum DisconnectionReason {
        NETWORK_TIMEOUT("네트워크 연결이 끊어졌습니다."),
        OPPONENT_QUIT("상대방이 게임을 종료했습니다."),
        CONNECTION_ERROR("연결 오류가 발생했습니다.");
        
        private final String message;
        
        DisconnectionReason(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    private final JFrame currentFrame;
    
    public DisconnectionHandler(JFrame currentFrame) {
        this.currentFrame = currentFrame;
    }
    
    /**
     * 연결 끊김 처리
     */
    public void handleDisconnection(DisconnectionReason reason) {
        System.err.println("🔴 연결 끊김: " + reason);
        
        // 현재 게임 중단
        stopCurrentGame();
        
        // 에러 메시지 표시 (Swing EDT에서 실행)
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                currentFrame,
                reason.getMessage() + "\n\nP2P 대전 메뉴로 돌아갑니다.",
                "연결 끊김",
                JOptionPane.ERROR_MESSAGE
            );
            
            // P2P 메뉴로 복귀
            returnToP2PMenu();
        });
    }
    
    private void stopCurrentGame() {
        // 게임 컨트롤러 정지 로직
        // (P2PVersusFrameBoard에서 구현 필요)
    }
    
    private void returnToP2PMenu() {
        // 현재 프레임 닫기
        currentFrame.dispose();
        
        // P2P 메뉴 열기
        SwingUtilities.invokeLater(() -> {
            new p2p.P2PMenuFrame().setVisible(true);
        });
    }
}
```

---

#### 작업 1-3: ConnectionMonitor 개선
```bash
# 기존 파일 수정
app/src/main/java/network/ConnectionMonitor.java
```

**수정 내용:**
- [ ] LatencyMonitor 추가
- [ ] RTT 측정 로직
- [ ] DisconnectionHandler 연동
- [ ] 상태 변경 시 UI 콜백

**수정할 부분:**
```java
public class ConnectionMonitor extends Thread {
    private final MessageSender sender;
    private final LatencyMonitor latencyMonitor;  // ✅ 추가
    private final DisconnectionHandler disconnectionHandler;  // ✅ 추가
    
    private volatile boolean running = true;
    private volatile long lastHeartbeatReceived;
    private volatile long lastHeartbeatSent;  // ✅ 추가 (RTT 측정용)
    private volatile ConnectionState currentState;
    private ConnectionStateListener stateListener;
    
    public ConnectionMonitor(MessageSender sender, JFrame frame) {
        this.sender = sender;
        this.latencyMonitor = new LatencyMonitor();  // ✅ 추가
        this.disconnectionHandler = new DisconnectionHandler(frame);  // ✅ 추가
        this.lastHeartbeatReceived = System.currentTimeMillis();
        this.currentState = ConnectionState.CONNECTED;
        setDaemon(true);
        setName("ConnectionMonitor-Thread");
    }
    
    /**
     * Heartbeat 수신 시 호출
     */
    public void onHeartbeatReceived() {
        long now = System.currentTimeMillis();
        lastHeartbeatReceived = now;
        
        // ✅ RTT 계산 및 기록
        long rtt = now - lastHeartbeatSent;
        latencyMonitor.recordLatency(rtt);
        
        updateState(ConnectionState.CONNECTED);
    }
    
    private void sendHeartbeat() {
        lastHeartbeatSent = System.currentTimeMillis();  // ✅ 전송 시간 기록
        HeartbeatMessage heartbeat = new HeartbeatMessage();
        sender.sendMessage(heartbeat);
    }
    
    private void checkConnectionState() {
        long timeSinceLastHeartbeat = System.currentTimeMillis() - lastHeartbeatReceived;
        
        if (timeSinceLastHeartbeat > NetworkConfig.HEARTBEAT_TIMEOUT) {
            // ✅ 타임아웃 → DisconnectionHandler 호출
            updateState(ConnectionState.TIMEOUT);
            disconnectionHandler.handleDisconnection(
                DisconnectionHandler.DisconnectionReason.NETWORK_TIMEOUT
            );
            shutdown();
        } else if (latencyMonitor.isLagging()) {  // ✅ 변경: LatencyMonitor 사용
            updateState(ConnectionState.LAGGING);
        } else {
            updateState(ConnectionState.CONNECTED);
        }
    }
    
    // ✅ 추가: LatencyMonitor getter
    public LatencyMonitor getLatencyMonitor() {
        return latencyMonitor;
    }
}
```

---

#### 작업 1-4: P2PVersusFrameBoard에 UI 표시 추가
```bash
# 기존 파일 수정
app/src/main/java/p2p/P2PVersusFrameBoard.java
```

**수정 내용:**
- [ ] ConnectionStateListener 구현
- [ ] "랙 걸림" 경고 표시
- [ ] "연결 끊김" 처리

**추가할 코드:**
```java
// P2PVersusFrameBoard.java 내부
private JLabel connectionStatusLabel;  // ✅ 추가

private void setupUI() {
    // ... 기존 코드 ...
    
    // ✅ 연결 상태 표시 레이블 추가
    connectionStatusLabel = new JLabel("연결 상태: 정상");
    connectionStatusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
    connectionStatusLabel.setForeground(Color.GREEN);
    
    // 상단 패널에 추가
    JPanel topPanel = new JPanel();
    topPanel.add(connectionStatusLabel);
    add(topPanel, BorderLayout.NORTH);
}

private void setupNetworkSync() {
    // ... 기존 코드 ...
    
    // ✅ ConnectionStateListener 등록
    networkManager.getConnectionMonitor().setStateListener(newState -> {
        SwingUtilities.invokeLater(() -> {
            switch (newState) {
                case CONNECTED:
                    connectionStatusLabel.setText("연결 상태: 정상");
                    connectionStatusLabel.setForeground(Color.GREEN);
                    break;
                case LAGGING:
                    connectionStatusLabel.setText("⚠️ 랙 걸림 상태");
                    connectionStatusLabel.setForeground(Color.ORANGE);
                    break;
                case TIMEOUT:
                    connectionStatusLabel.setText("🔴 연결 끊김");
                    connectionStatusLabel.setForeground(Color.RED);
                    break;
            }
        });
    });
}
```

---

#### 작업 1-5: 테스트 작성
```bash
# 새 파일 생성
app/src/test/java/network/LatencyMonitorTest.java
app/src/test/java/network/DisconnectionHandlerTest.java
```

**LatencyMonitorTest.java:**
```java
package network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LatencyMonitorTest {
    
    @Test
    void testRecordLatency() {
        LatencyMonitor monitor = new LatencyMonitor();
        
        monitor.recordLatency(50);
        monitor.recordLatency(100);
        monitor.recordLatency(150);
        
        long avg = monitor.getAverageLatency();
        assertEquals(100, avg);
    }
    
    @Test
    void testLagDetection() {
        LatencyMonitor monitor = new LatencyMonitor();
        
        // 정상 지연
        monitor.recordLatency(50);
        assertFalse(monitor.isLagging());
        
        // 랙 발생 (> 200ms)
        monitor.recordLatency(250);
        monitor.recordLatency(300);
        assertTrue(monitor.isLagging());
    }
    
    @Test
    void testHistoryLimit() {
        LatencyMonitor monitor = new LatencyMonitor();
        
        // 15개 기록 (10개만 유지)
        for (int i = 0; i < 15; i++) {
            monitor.recordLatency(i * 10);
        }
        
        // 마지막 10개의 평균
        long avg = monitor.getAverageLatency();
        assertTrue(avg > 50); // 최신 값들의 평균
    }
}
```

---

### 📍 Step 2: Phase 6 구현 (지연 최적화)

#### 작업 2-1: EventFilter 구현
```bash
# 새 파일 생성
app/src/main/java/network/EventFilter.java
```

**구현 내용:**
```java
package network;

import game.events.*;
import java.util.Set;

public class EventFilter {
    
    private static final Set<Class<?>> SYNC_EVENTS = Set.of(
        // 동기화 필요한 이벤트
        LineClearedEvent.class,
        GameOverEvent.class,
        ScoreUpdateEvent.class
        // TickEvent, BlockMovedEvent 등은 제외 (너무 많음)
    );
    
    /**
     * 이벤트를 네트워크로 전송해야 하는지 판단
     */
    public boolean shouldSync(GameEvent event) {
        return SYNC_EVENTS.contains(event.getClass());
    }
    
    /**
     * 동기화 대상 이벤트 목록
     */
    public Set<Class<?>> getSyncEvents() {
        return SYNC_EVENTS;
    }
}
```

---

#### 작업 2-2: EventSynchronizer에 필터 적용
```bash
# 기존 파일 수정
app/src/main/java/p2p/EventSynchronizer.java
```

**수정할 부분:**
```java
public class EventSynchronizer implements MessageReceiver.MessageListener {
    
    private final EventFilter eventFilter = new EventFilter();  // ✅ 추가
    
    public void publishLocalEvent(GameEvent event) {
        // ✅ 필터 적용
        if (!eventFilter.shouldSync(event)) {
            // 로컬에만 발행
            localEventBus.publish(event);
            return;
        }
        
        // 로컬 발행 + 네트워크 전송
        localEventBus.publish(event);
        
        // GameEventMessage로 감싸서 전송
        GameEventMessage message = new GameEventMessage(event, myPlayerId);
        boolean sent = sender.sendMessage(message);
        
        if (!sent) {
            System.err.println("❌ 이벤트 전송 실패: " + event.getClass().getSimpleName());
        }
        
        // ... 기존 AttackMessage 처리 코드 ...
    }
}
```

---

#### 작업 2-3: 성능 측정 추가
```bash
# EventSynchronizer.java에 성능 로깅 추가
```

**추가할 코드:**
```java
public void publishLocalEvent(GameEvent event) {
    long startTime = System.nanoTime();  // ✅ 시작 시간
    
    // ... 기존 로직 ...
    
    long endTime = System.nanoTime();  // ✅ 종료 시간
    long elapsedMs = (endTime - startTime) / 1_000_000;
    
    if (elapsedMs > 10) {  // 10ms 이상 걸리면 경고
        System.err.println("⚠️ 이벤트 처리 지연: " + event.getClass().getSimpleName() 
            + " (" + elapsedMs + "ms)");
    }
}
```

---

### 📍 Step 3: Phase 8 구현 (자동화 테스트)

#### 작업 3-1: NetworkSimulator 구현
```bash
# 새 파일 생성
app/src/test/java/utils/NetworkSimulator.java
```

**구현 내용:**
```java
package utils;

public class NetworkSimulator {
    
    /**
     * 지연 시뮬레이션
     */
    public void simulateLatency(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 패킷 손실 시뮬레이션
     */
    public boolean simulatePacketLoss(double lossRate) {
        return Math.random() < lossRate;
    }
    
    /**
     * 랜덤 네트워크 지연
     */
    public void simulateRandomLatency(int minMs, int maxMs) {
        int delay = minMs + (int)(Math.random() * (maxMs - minMs));
        simulateLatency(delay);
    }
}
```

---

#### 작업 3-2: 통합 테스트 추가
```bash
# 새 파일 생성
app/src/test/java/network/EventSynchronizationTest.java
```

**구현 내용:**
```java
package network;

import org.junit.jupiter.api.Test;
import game.events.*;
import utils.NetworkSimulator;
import static org.junit.jupiter.api.Assertions.*;

class EventSynchronizationTest {
    
    @Test
    void testEventSync() throws Exception {
        // 서버/클라이언트 설정
        // 이벤트 전송
        // 수신 확인
    }
    
    @Test
    void testAttackSync() throws Exception {
        // 공격 메시지 전송
        // 상대방 보드에 줄 추가 확인
    }
    
    @Test
    void testHighLatency() throws Exception {
        NetworkSimulator simulator = new NetworkSimulator();
        
        // 300ms 지연 시뮬레이션
        simulator.simulateLatency(300);
        
        // 랙 감지 확인
    }
}
```

---

### 📍 Step 4: Phase 7 구현 (UI/UX 개선)

#### 작업 4-1: ConnectionHistory 구현
```bash
# 새 파일 생성
app/src/main/java/p2p/ConnectionHistory.java
```

**구현 내용:**
```java
package p2p;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.util.*;

public class ConnectionHistory {
    private static final String HISTORY_FILE = "connection_history.json";
    private static final int MAX_HISTORY = 5;
    private List<String> recentIPs;
    private final Gson gson;
    
    public ConnectionHistory() {
        this.gson = new Gson();
        load();
    }
    
    public void saveConnection(String ip) {
        // 중복 제거
        recentIPs.remove(ip);
        
        // 맨 앞에 추가
        recentIPs.add(0, ip);
        
        // 최대 5개만 유지
        if (recentIPs.size() > MAX_HISTORY) {
            recentIPs = recentIPs.subList(0, MAX_HISTORY);
        }
        
        save();
    }
    
    public List<String> getRecentIPs() {
        return new ArrayList<>(recentIPs);
    }
    
    private void load() {
        // JSON 파일에서 로드
        // 없으면 빈 리스트
    }
    
    private void save() {
        // JSON 파일로 저장
    }
}
```

---

#### 작업 4-2: NetworkStatusIndicator 구현
```bash
# 새 파일 생성
app/src/main/java/p2p/NetworkStatusIndicator.java
```

**구현 내용:**
```java
package p2p;

import javax.swing.*;
import java.awt.*;
import network.ConnectionState;

public class NetworkStatusIndicator extends JPanel {
    private ConnectionState state = ConnectionState.CONNECTED;
    private long latency = 0;
    
    public NetworkStatusIndicator() {
        setPreferredSize(new Dimension(150, 30));
        setOpaque(false);
    }
    
    public void updateState(ConnectionState state, long latency) {
        this.state = state;
        this.latency = latency;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 상태에 따른 색상
        Color color;
        String text;
        
        switch (state) {
            case CONNECTED:
                if (latency < 100) {
                    color = new Color(0, 200, 0);  // 초록
                    text = "✓ " + latency + "ms";
                } else {
                    color = new Color(255, 200, 0);  // 노랑
                    text = "⚠ " + latency + "ms";
                }
                break;
            case LAGGING:
                color = new Color(255, 150, 0);  // 주황
                text = "⚠ 랙 (" + latency + "ms)";
                break;
            case TIMEOUT:
                color = new Color(255, 0, 0);  // 빨강
                text = "✗ 끊김";
                break;
            default:
                color = Color.GRAY;
                text = "?";
        }
        
        // 원 그리기
        g2d.setColor(color);
        g2d.fillOval(5, 5, 20, 20);
        
        // 텍스트 그리기
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        g2d.drawString(text, 30, 20);
    }
}
```

---

#### 작업 4-3: P2PVersusFrameBoard에 NetworkStatusIndicator 추가
```bash
# 기존 파일 수정
app/src/main/java/p2p/P2PVersusFrameBoard.java
```

**추가할 코드:**
```java
private NetworkStatusIndicator statusIndicator;  // ✅ 추가

private void setupUI() {
    // ... 기존 코드 ...
    
    // ✅ 네트워크 상태 인디케이터
    statusIndicator = new NetworkStatusIndicator();
    
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(statusIndicator, BorderLayout.EAST);
    
    add(topPanel, BorderLayout.NORTH);
}

private void setupNetworkSync() {
    // ... 기존 코드 ...
    
    // ✅ 상태 변경 시 인디케이터 업데이트
    networkManager.getConnectionMonitor().setStateListener(newState -> {
        long latency = networkManager.getConnectionMonitor()
            .getLatencyMonitor()
            .getLastLatency();
        
        SwingUtilities.invokeLater(() -> {
            statusIndicator.updateState(newState, latency);
        });
    });
}
```

---

## 📝 작업 체크리스트

### Phase 5: 연결 안정성 (1주)
- [ ] LatencyMonitor.java 구현
- [ ] DisconnectionHandler.java 구현
- [ ] ConnectionMonitor.java 개선
- [ ] P2PVersusFrameBoard.java UI 추가
- [ ] LatencyMonitorTest.java 작성
- [ ] DisconnectionHandlerTest.java 작성
- [ ] 통합 테스트 (네트워크 끊김 시뮬레이션)

### Phase 6: 지연 최적화 (3일)
- [ ] EventFilter.java 구현
- [ ] EventSynchronizer.java 필터 적용
- [ ] 성능 측정 로깅 추가
- [ ] EventFilterTest.java 작성

### Phase 8: 자동화 테스트 (4일)
- [ ] NetworkSimulator.java 구현
- [ ] EventSynchronizationTest.java 작성
- [ ] 고부하 테스트 작성
- [ ] 엣지 케이스 테스트

### Phase 7: UI/UX 개선 (3일)
- [ ] ConnectionHistory.java 구현
- [ ] NetworkStatusIndicator.java 구현
- [ ] P2PClientSetupFrame.java 개선
- [ ] P2PVersusFrameBoard.java 인디케이터 추가
- [ ] ConnectionProgressDialog.java 개선

---

## 🎯 예상 일정

| Phase | 작업 기간 | 난이도 |
|-------|----------|--------|
| Phase 5 | 5-7일 | ⭐⭐⭐ 중 |
| Phase 6 | 2-3일 | ⭐⭐ 쉬움 |
| Phase 8 | 3-4일 | ⭐⭐⭐ 중 |
| Phase 7 | 2-3일 | ⭐⭐ 쉬움 |

**총 예상 기간: 12-17일 (약 2-3주)**

---

## 💡 개발 팁

1. **Phase 5부터 시작** - 연결 안정성이 가장 중요
2. **작은 단위로 테스트** - 각 클래스 완성 후 즉시 테스트
3. **로그 활용** - System.out.println으로 상태 추적
4. **실제 네트워크 테스트** - 두 PC로 실제 테스트 필수
5. **Git 커밋 자주** - 각 작업 완료 시마다 커밋

---

## 🚀 시작하기

```bash
# 1. Phase 5 브랜치 생성
git checkout -b phase5-connection-stability

# 2. 첫 번째 파일 생성
touch app/src/main/java/network/LatencyMonitor.java

# 3. 구현 시작!
```

**Good Luck! 화이팅! 🎮**
