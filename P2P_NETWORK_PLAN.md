# P2P 네트워크 대전 모드 구현 계획

## 📋 목차
1. [요구사항 분석](#1-요구사항-분석)
2. [기술 스택](#2-기술-스택)
3. [시스템 아키텍처](#3-시스템-아키텍처)
4. [구현 단계](#4-구현-단계)
5. [네트워크 프로토콜](#5-네트워크-프로토콜)
6. [파일 구조](#6-파일-구조)
7. [개발 우선순위](#7-개발-우선순위)
8. [테스트 계획](#8-테스트-계획)

---

## 1. 요구사항 분석

### 1.1 기능 요구사항 (스크린샷 기반)

#### ✅ P2P 대전 모드 개발
- 시작 메뉴에서 P2P 대전 모드를 골라 시작
- **2대의 PC**에서 네트워크로 연결하여 대전 진행
- 일반 대전 모드와 동일한 게임 방식

#### ✅ 서버/클라이언트 역할
- **서버 선택 시**: IP주소를 화면에 표시하고 대기
- **클라이언트 선택 시**: IP주소 입력하여 서버에 접속
- 연결 완료 후 서버/클라이언트가 이를 알려주고 대기

#### ✅ 게임 시작 방식
- 서버에 행당하는 플레이어가 "게임 시작" 버튼을 눌러 대전 시작
- 일반, 아이템, 시간제한 모드 선택 가능

#### ✅ 화면 동기화
- 내가 플레이 하는 화면과 상대방이 플레이 하는 화면이 모두 보여야 함
- 각 플레이어의 조작에 따라 블록 등은 실시간으로 양측 화면에 동시 표시
- **동일 로컬 네트워크 기준 키 입력-화면 표시 지연 200ms 이하**

#### ✅ 승패 처리
- 게임 완료되면 대전 모드와 동일하게 승패 표시
- 네트워크 연결 직후의 대기상태로 돌아감 (재대전 가능)

### 1.2 추가 고려사항 (스크린샷 기반)

#### 네트워크 문제 처리
- 단순히 네트워크가 느려 전송이 지연되는 경우: **"랙 걸림 상태"** 표시
- 네트워크 연결 자체가 일정 시간 이상 끊어진 경우: 에러 메시지 표시 후 P2P 대전 모드를 처음 들어갔을 때 화면으로 돌아감

#### 개발 팁
- 전송 지연이나 연결 끊김의 기준은 게임 플레이가 원활하게 될 수 있도록 직접 정의하여 관리
- 편리한 접속을 위해 최근 접속 IP를 저장하고 P2P 대전 모드에서 클라이언트가 이를 확인하여 입력할 수 있게 함
- 네트워크 접속 과정이 잘 진행되는지 확인할 수 있도록 적절한 메시지를 플레이어에게 표시
- **자동화 된 테스트 작성**

---

## 2. 기술 스택

### 2.1 네트워크 통신
- **Java Socket API**: TCP 기반 양방향 통신
  - `ServerSocket`: 서버측 연결 수락
  - `Socket`: 클라이언트-서버 통신
- **ObjectInputStream/ObjectOutputStream**: 객체 직렬화 통신

### 2.2 직렬화
- Java Serializable 인터페이스 활용
- 기존 이벤트 시스템 (GameEvent) 이미 Serializable 구현됨

### 2.3 멀티스레딩
- **송신 스레드**: 게임 이벤트를 상대방에게 전송
- **수신 스레드**: 상대방 이벤트를 받아서 처리
- **타임아웃 감지 스레드**: 연결 상태 모니터링

---

## 3. 시스템 아키텍처

### 3.1 현재 로컬 대전 모드 구조
```
VersusFrameBoard (1200x600)
├─ GameController1 (Player 1)
│  ├─ EventBus
│  └─ GameView1
└─ GameController2 (Player 2)
   ├─ EventBus
   └─ GameView2
```

### 3.2 P2P 네트워크 대전 모드 구조
```
P2PVersusFrameBoard (1200x600)
├─ LocalGameController (나의 게임)
│  ├─ EventBus (local)
│  ├─ GameView (내 화면)
│  └─ NetworkEventPublisher → NetworkManager
│
├─ RemoteGameController (상대방 게임 - 읽기 전용)
│  ├─ EventBus (remote)
│  └─ GameView (상대방 화면)
│
└─ NetworkManager
   ├─ Role (SERVER / CLIENT)
   ├─ ConnectionManager (Socket 관리)
   ├─ MessageSender (송신 스레드)
   ├─ MessageReceiver (수신 스레드)
   ├─ ConnectionMonitor (연결 상태 감시)
   └─ EventSynchronizer (이벤트 동기화)
```

### 3.3 이벤트 흐름

#### 내 조작 → 상대방 화면
```
키 입력 → LocalGameController
  ↓
이벤트 발생 (BlockMovedEvent, LineClearedEvent 등)
  ↓
NetworkEventPublisher가 감지
  ↓
MessageSender를 통해 직렬화하여 전송
  ↓
상대방의 MessageReceiver가 수신
  ↓
역직렬화하여 RemoteGameController의 EventBus에 발행
  ↓
상대방 화면에 내 게임 상태 표시
```

#### 상대방 조작 → 내 화면 (역방향 동일)

---

## 4. 구현 단계

### Phase 1: 네트워크 인프라 구축 (기본 연결) ⭐ 우선순위 높음

#### Step 1.1: 네트워크 패키지 구조 생성
```
app/src/main/java/network/
├── NetworkManager.java           # 네트워크 총괄 관리
├── ConnectionManager.java        # Socket 연결 관리
├── MessageSender.java            # 송신 스레드
├── MessageReceiver.java          # 수신 스레드
├── ConnectionMonitor.java        # 연결 상태 모니터링
├── NetworkConfig.java            # 네트워크 설정 (포트, 타임아웃 등)
└── NetworkRole.java              # enum (SERVER, CLIENT)
```

#### Step 1.2: 기본 메시지 클래스 정의
```java
// 네트워크로 전송될 모든 메시지의 기본 클래스
public abstract class NetworkMessage implements Serializable {
    private long timestamp;
    private String messageId;
    private MessageType type;
}

// 메시지 타입
enum MessageType {
    GAME_EVENT,      // 게임 이벤트 (블록 이동, 줄 삭제 등)
    CONNECTION,      // 연결 관련
    GAME_CONTROL,    // 게임 시작/종료
    HEARTBEAT,       // 연결 유지 확인
    ATTACK           // 공격 줄
}
```

#### Step 1.3: 연결 UI 구현
```
app/src/main/java/p2p/
├── P2PMenuFrame.java             # P2P 메뉴 (서버/클라이언트 선택)
├── P2PServerSetupFrame.java      # 서버 설정 화면 (IP 표시, 대기)
├── P2PClientSetupFrame.java      # 클라이언트 설정 화면 (IP 입력)
└── P2PConnectionDialog.java      # 연결 진행 상태 다이얼로그
```

**구현 내용:**
- StartFrame에 "P2P 대전" 버튼 추가
- P2PMenuFrame: "서버로 호스트" vs "클라이언트로 참가" 선택
- 서버 화면: 로컬 IP 자동 표시, 포트 번호 표시, "대기 중..." 상태
- 클라이언트 화면: IP 입력 필드, 포트 입력 필드, "최근 접속 IP" 표시, "연결" 버튼

#### Step 1.4: Socket 연결 구현
```java
// ConnectionManager.java
public class ConnectionManager {
    private ServerSocket serverSocket;  // 서버용
    private Socket socket;              // 클라이언트/연결된 소켓
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    // 서버로 시작
    public void startServer(int port) throws IOException;
    
    // 클라이언트로 연결
    public void connectToServer(String host, int port) throws IOException;
    
    // 연결 종료
    public void disconnect();
}
```

**테스트:**
- [ ] 서버 시작 → IP 표시 확인
- [ ] 클라이언트 연결 → 성공 메시지 확인
- [ ] 양쪽 모두 "연결됨" 상태 표시
- [ ] 잘못된 IP 입력 시 에러 처리

---

### Phase 2: 게임 이벤트 동기화 ⭐ 우선순위 높음

#### Step 2.1: 네트워크 이벤트 래퍼 생성
```java
// 기존 GameEvent를 네트워크로 전송하기 위한 래퍼
public class GameEventMessage extends NetworkMessage {
    private GameEvent gameEvent;  // 기존 이벤트 객체
    private int playerNumber;     // 1 or 2 (어느 플레이어 이벤트인지)
    
    public GameEventMessage(GameEvent event, int player) {
        super(MessageType.GAME_EVENT);
        this.gameEvent = event;
        this.playerNumber = player;
    }
}
```

#### Step 2.2: EventSynchronizer 구현
```java
public class EventSynchronizer {
    private EventBus localEventBus;    // 내 게임
    private EventBus remoteEventBus;   // 상대방 게임
    private MessageSender sender;
    
    // 내 이벤트를 상대방에게 전송
    public void publishLocalEvent(GameEvent event) {
        localEventBus.publish(event);  // 내 게임에 적용
        sender.send(new GameEventMessage(event, 1));  // 상대방에게 전송
    }
    
    // 상대방 이벤트를 내 화면에 표시
    public void handleRemoteEvent(GameEventMessage message) {
        remoteEventBus.publish(message.getGameEvent());
    }
}
```

#### Step 2.3: 송수신 스레드 구현
```java
// MessageSender.java
public class MessageSender implements Runnable {
    private BlockingQueue<NetworkMessage> messageQueue;
    private ObjectOutputStream out;
    
    @Override
    public void run() {
        while (connected) {
            NetworkMessage msg = messageQueue.take();
            out.writeObject(msg);
            out.flush();
        }
    }
    
    public void send(NetworkMessage msg) {
        messageQueue.offer(msg);
    }
}

// MessageReceiver.java
public class MessageReceiver implements Runnable {
    private ObjectInputStream in;
    private MessageHandler handler;
    
    @Override
    public void run() {
        while (connected) {
            NetworkMessage msg = (NetworkMessage) in.readObject();
            handler.handle(msg);
        }
    }
}
```

#### Step 2.4: P2PVersusFrameBoard 구현
```java
public class P2PVersusFrameBoard extends JFrame {
    private GameController localController;   // 내 게임
    private GameController remoteController;  // 상대방 게임 (읽기 전용)
    private NetworkManager networkManager;
    private EventSynchronizer synchronizer;
    
    // 내 키 입력만 처리
    private P2PLocalKeyListener localKeyListener;
    
    // 상대방 게임은 네트워크 이벤트로만 업데이트
}
```

**구현 내용:**
- 로컬 게임: 사용자 입력 처리 + 이벤트 발생 → 네트워크 전송
- 리모트 게임: 네트워크 수신만으로 업데이트 (입력 차단)
- 양쪽 화면 모두 표시 (로컬: 왼쪽, 리모트: 오른쪽)

**테스트:**
- [ ] 내 블록 이동 → 상대방 화면에도 표시
- [ ] 상대방 블록 이동 → 내 화면에 표시
- [ ] 줄 삭제 이벤트 동기화
- [ ] 공격 줄 전송 및 수신

---

### Phase 3: 게임 시작/종료 제어 ⭐ 우선순위 중

#### Step 3.1: 게임 제어 메시지 정의
```java
public class GameControlMessage extends NetworkMessage {
    public enum ControlType {
        START_GAME,      // 게임 시작
        PAUSE_GAME,      // 일시정지
        RESUME_GAME,     // 재개
        END_GAME,        // 게임 종료
        READY,           // 준비 완료
        MODE_SELECT      // 모드 선택 (NORMAL, ITEM, TIME_LIMIT)
    }
    
    private ControlType controlType;
    private VersusMode selectedMode;  // 선택된 게임 모드
}
```

#### Step 3.2: 게임 시작 프로토콜
```
1. 양쪽 연결 완료
2. 서버가 모드 선택 (NORMAL/ITEM/TIME_LIMIT)
3. MODE_SELECT 메시지 전송 → 클라이언트 수신
4. 양쪽 모두 READY 메시지 교환
5. 서버가 START_GAME 메시지 전송
6. 양쪽 동시에 게임 시작
```

#### Step 3.3: P2PWaitingRoom 구현
```java
public class P2PWaitingRoom extends JFrame {
    // 서버용: 모드 선택 UI + "게임 시작" 버튼
    // 클라이언트용: "상대방 대기 중..." 표시
    
    private JComboBox<VersusMode> modeSelector;  // 서버만 활성화
    private JButton startButton;                  // 서버만 활성화
    private JLabel statusLabel;                   // 연결 상태 표시
}
```

**테스트:**
- [ ] 서버가 모드 선택 → 클라이언트에 반영
- [ ] 서버가 "게임 시작" → 양쪽 동시 시작
- [ ] 게임 종료 후 대기실로 복귀

---

### Phase 4: 공격 시스템 동기화 ⭐ 우선순위 중

#### Step 4.1: 공격 메시지 정의
```java
public class AttackMessage extends NetworkMessage {
    private int attackLines;           // 공격 줄 수
    private int[][] blockPattern;      // 블록 패턴
    private int blockX;                // 블록 X 위치
    
    public AttackMessage(int lines, int[][] pattern, int x) {
        super(MessageType.ATTACK);
        this.attackLines = lines;
        this.blockPattern = pattern;
        this.blockX = x;
    }
}
```

#### Step 4.2: 공격 동기화 로직
```
Player 1이 줄 삭제
  ↓
LineClearedEvent 발생
  ↓
VersusAttackManager.calculateAttackLines()
  ↓
AttackMessage 생성 → 네트워크 전송
  ↓
Player 2가 수신
  ↓
GameController.addAttackLines() 호출
  ↓
Player 2 화면에 공격 줄 추가
```

**테스트:**
- [ ] 내가 줄 삭제 → 상대방에게 공격 줄 추가
- [ ] 상대방이 줄 삭제 → 내게 공격 줄 추가
- [ ] 공격 줄 최대 10개 제한 확인

---

### Phase 5: 연결 안정성 및 에러 처리 ⭐ 우선순위 높음

#### Step 5.1: Heartbeat 시스템
```java
public class HeartbeatManager {
    private static final long HEARTBEAT_INTERVAL = 1000;  // 1초마다
    private static final long TIMEOUT_THRESHOLD = 5000;   // 5초 응답 없으면 타임아웃
    
    private long lastReceivedTime;
    
    public void sendHeartbeat() {
        sender.send(new HeartbeatMessage());
    }
    
    public void updateLastReceived() {
        lastReceivedTime = System.currentTimeMillis();
    }
    
    public boolean isTimeout() {
        return System.currentTimeMillis() - lastReceivedTime > TIMEOUT_THRESHOLD;
    }
}
```

#### Step 5.2: 연결 상태 모니터링
```java
public class ConnectionMonitor implements Runnable {
    private HeartbeatManager heartbeat;
    private ConnectionState state;
    
    @Override
    public void run() {
        while (true) {
            if (heartbeat.isTimeout()) {
                state = ConnectionState.DISCONNECTED;
                handleDisconnection();
            } else if (isLagging()) {
                state = ConnectionState.LAGGING;
                showLagWarning();
            }
            Thread.sleep(500);
        }
    }
}
```

#### Step 5.3: 네트워크 지연 감지
```java
public class LatencyMonitor {
    private static final long LAG_THRESHOLD = 200;  // 200ms
    
    private Queue<Long> latencyHistory = new LinkedList<>();
    
    public void recordLatency(long latency) {
        latencyHistory.offer(latency);
        if (latencyHistory.size() > 10) {
            latencyHistory.poll();
        }
    }
    
    public boolean isLagging() {
        return getAverageLatency() > LAG_THRESHOLD;
    }
    
    public long getAverageLatency() {
        return latencyHistory.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0);
    }
}
```

#### Step 5.4: 에러 처리
```java
public enum DisconnectionReason {
    NETWORK_TIMEOUT,      // 네트워크 끊김
    OPPONENT_QUIT,        // 상대방 종료
    CONNECTION_ERROR      // 연결 오류
}

public class DisconnectionHandler {
    public void handleDisconnection(DisconnectionReason reason) {
        // 현재 게임 중단
        // 에러 메시지 표시
        // P2P 메뉴로 돌아가기
    }
}
```

**UI 표시:**
- "랙 걸림 상태" - 노란색 경고 표시 (200ms 이상 지연)
- "연결 끊김" - 빨간색 에러 다이얼로그 (5초 이상 무응답)

**테스트:**
- [ ] 네트워크 케이블 뽑기 → "연결 끊김" 표시
- [ ] 지연 시뮬레이션 → "랙 걸림" 표시
- [ ] 상대방 강제 종료 → 메뉴로 복귀

---

### Phase 6: 지연 최적화 및 동기화 ⭐ 우선순위 중

#### Step 6.1: 이벤트 필터링
```java
// 모든 이벤트를 전송하면 너무 많음
// 필요한 이벤트만 선별하여 전송
public class EventFilter {
    private static final Set<Class<?>> SYNC_EVENTS = Set.of(
        BlockMovedEvent.class,
        BlockRotatedEvent.class,
        BlockLandedEvent.class,
        LineClearedEvent.class,
        GameOverEvent.class,
        ScoreUpdateEvent.class
        // TickEvent는 전송하지 않음 (로컬에서 각자 관리)
    );
    
    public boolean shouldSync(GameEvent event) {
        return SYNC_EVENTS.contains(event.getClass());
    }
}
```

#### Step 6.2: 버퍼링 및 배치 전송
```java
// 작은 이벤트들을 모아서 한 번에 전송
public class MessageBatcher {
    private List<NetworkMessage> buffer = new ArrayList<>();
    private static final int BATCH_SIZE = 5;
    private static final long BATCH_TIMEOUT = 50;  // 50ms
    
    public void add(NetworkMessage msg) {
        buffer.add(msg);
        if (buffer.size() >= BATCH_SIZE) {
            flush();
        }
    }
    
    public void flush() {
        if (!buffer.isEmpty()) {
            sender.send(new BatchMessage(buffer));
            buffer.clear();
        }
    }
}
```

#### Step 6.3: 입력 예측 (선택적)
```java
// 상대방 블록이 계속 아래로 떨어지는 것은 예측 가능
// 네트워크 지연을 줄이기 위해 로컬에서 예측
public class InputPredictor {
    public GameState predictNextState(GameState current) {
        // 현재 블록이 계속 내려간다고 가정
        // 실제 이벤트 수신 시 보정
    }
}
```

---

### Phase 7: UI/UX 개선 ⭐ 우선순위 낮음

#### Step 7.1: 최근 접속 IP 저장
```java
public class ConnectionHistory {
    private static final String HISTORY_FILE = "connection_history.json";
    private List<String> recentIPs = new ArrayList<>();
    
    public void saveConnection(String ip) {
        recentIPs.add(0, ip);  // 맨 앞에 추가
        if (recentIPs.size() > 5) {
            recentIPs.remove(5);  // 최대 5개만 저장
        }
        saveToFile();
    }
    
    public List<String> getRecentIPs() {
        return new ArrayList<>(recentIPs);
    }
}
```

#### Step 7.2: 연결 진행 상태 표시
```java
public class ConnectionProgressDialog extends JDialog {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    public void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    // "서버 시작 중..."
    // "클라이언트 연결 중..."
    // "핸드셰이크 진행 중..."
    // "연결 완료!"
}
```

#### Step 7.3: 네트워크 상태 인디케이터
```java
// 게임 화면 우측 상단에 표시
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

---

### Phase 8: 자동화 테스트 ⭐ 우선순위 높음

#### Step 8.1: 단위 테스트
```java
// ConnectionManagerTest.java
@Test
public void testServerConnection() {
    ConnectionManager server = new ConnectionManager();
    server.startServer(12345);
    assertTrue(server.isServerRunning());
}

@Test
public void testClientConnection() {
    ConnectionManager client = new ConnectionManager();
    client.connectToServer("localhost", 12345);
    assertTrue(client.isConnected());
}

// MessageSerializationTest.java
@Test
public void testGameEventSerialization() {
    LineClearedEvent event = new LineClearedEvent(3);
    GameEventMessage msg = new GameEventMessage(event, 1);
    
    byte[] serialized = serialize(msg);
    GameEventMessage deserialized = deserialize(serialized);
    
    assertEquals(3, ((LineClearedEvent)deserialized.getGameEvent()).getLinesCleared());
}
```

#### Step 8.2: 통합 테스트
```java
// P2PIntegrationTest.java
@Test
public void testFullGameFlow() {
    // 1. 서버 시작
    P2PServer server = new P2PServer();
    server.start(12345);
    
    // 2. 클라이언트 연결
    P2PClient client = new P2PClient();
    client.connect("localhost", 12345);
    
    // 3. 게임 시작
    server.startGame(VersusMode.NORMAL);
    
    // 4. 이벤트 동기화 확인
    server.moveBlock();
    Thread.sleep(100);
    assertTrue(client.getRemoteBlock().isMoved());
}
```

#### Step 8.3: 네트워크 시뮬레이션 테스트
```java
// NetworkSimulator.java
public class NetworkSimulator {
    // 지연 시뮬레이션
    public void simulateLatency(int milliseconds) {
        Thread.sleep(milliseconds);
    }
    
    // 패킷 손실 시뮬레이션
    public void simulatePacketLoss(double lossRate) {
        if (Math.random() < lossRate) {
            // 메시지 버림
        }
    }
    
    // 연결 끊김 시뮬레이션
    public void simulateDisconnection() {
        connectionManager.forceDisconnect();
    }
}

@Test
public void testHighLatency() {
    simulator.simulateLatency(300);  // 300ms 지연
    assertTrue(monitor.isLagging());
}
```

---

## 5. 네트워크 프로토콜

### 5.1 메시지 형식

#### 공통 헤더
```
[4 bytes] 메시지 길이
[1 byte]  메시지 타입
[8 bytes] 타임스탬프
[변동]    페이로드 (직렬화된 객체)
```

#### 메시지 타입별 페이로드

**GAME_EVENT (0x01)**
```json
{
  "playerNumber": 1,
  "eventType": "LineClearedEvent",
  "eventData": {
    "linesCleared": 3
  }
}
```

**ATTACK (0x02)**
```json
{
  "attackLines": 3,
  "blockPattern": [[1,1,1,1]],
  "blockX": 5
}
```

**GAME_CONTROL (0x03)**
```json
{
  "controlType": "START_GAME",
  "mode": "NORMAL"
}
```

**HEARTBEAT (0x04)**
```json
{
  "timestamp": 1700000000000
}
```

### 5.2 핸드셰이크 프로토콜

```
Client → Server: CONNECT_REQUEST
Server → Client: CONNECT_ACCEPT (또는 CONNECT_REJECT)
Client → Server: HANDSHAKE_ACK
Server → Client: READY

[대기실 상태]

Server → Client: MODE_SELECT (mode: NORMAL)
Client → Server: READY_ACK
Server → Client: START_GAME
Client → Server: START_ACK

[게임 진행]
```

### 5.3 에러 처리 프로토콜

```
ANY → ANY: HEARTBEAT (1초마다)
ANY → ANY: HEARTBEAT_ACK

[5초간 응답 없음]
→ CONNECTION_TIMEOUT
→ 게임 중단
→ 에러 메시지 표시
→ 메뉴로 복귀
```

---

## 6. 파일 구조

### 6.1 새로 추가될 파일

```
app/src/main/java/
├── network/                          # 네트워크 레이어
│   ├── NetworkManager.java
│   ├── ConnectionManager.java
│   ├── MessageSender.java
│   ├── MessageReceiver.java
│   ├── ConnectionMonitor.java
│   ├── HeartbeatManager.java
│   ├── LatencyMonitor.java
│   ├── EventSynchronizer.java
│   ├── EventFilter.java
│   ├── MessageBatcher.java
│   ├── DisconnectionHandler.java
│   ├── NetworkConfig.java
│   └── NetworkRole.java
│
├── network/messages/                 # 네트워크 메시지
│   ├── NetworkMessage.java
│   ├── MessageType.java
│   ├── GameEventMessage.java
│   ├── AttackMessage.java
│   ├── GameControlMessage.java
│   ├── HeartbeatMessage.java
│   ├── BatchMessage.java
│   └── ConnectionMessage.java
│
├── p2p/                              # P2P 대전 UI
│   ├── P2PMenuFrame.java             # P2P 메뉴
│   ├── P2PServerSetupFrame.java      # 서버 설정
│   ├── P2PClientSetupFrame.java      # 클라이언트 설정
│   ├── P2PWaitingRoom.java           # 대기실
│   ├── P2PVersusFrameBoard.java      # P2P 게임 화면
│   ├── P2PGameStart.java             # 진입점
│   ├── P2PKeyListener.java           # 로컬 키 입력만 처리
│   ├── ConnectionProgressDialog.java # 연결 진행 상태
│   ├── NetworkStatusIndicator.java   # 네트워크 상태 표시
│   └── ConnectionHistory.java        # 최근 접속 IP
│
└── p2p/controller/                   # P2P 게임 컨트롤러
    ├── P2PGameController.java        # P2P용 게임 컨트롤러
    └── RemoteGameController.java     # 상대방 게임 (읽기 전용)
```

### 6.2 수정될 파일

```
app/src/main/java/
├── start/StartFrame.java             # "P2P 대전" 버튼 추가
├── events/                           # 기존 이벤트들 Serializable 확인
│   ├── LineClearedEvent.java
│   ├── ScoreUpdateEvent.java
│   └── GameOverEvent.java
└── blocks/Block.java                 # Serializable 확인
```

### 6.3 테스트 파일

```
app/src/test/java/
├── network/
│   ├── ConnectionManagerTest.java
│   ├── MessageSerializationTest.java
│   ├── HeartbeatManagerTest.java
│   └── LatencyMonitorTest.java
│
├── p2p/
│   ├── P2PIntegrationTest.java
│   ├── NetworkSimulatorTest.java
│   └── EventSynchronizationTest.java
│
└── utils/
    └── NetworkSimulator.java          # 테스트용 네트워크 시뮬레이터
```

---

## 7. 개발 우선순위

### 🔴 Critical (필수, 먼저 구현)
1. **Phase 1**: 네트워크 인프라 구축
   - ConnectionManager (Socket 연결)
   - 기본 UI (서버/클라이언트 선택)
   - 연결 성공/실패 처리

2. **Phase 2**: 게임 이벤트 동기화
   - NetworkMessage 정의
   - MessageSender/Receiver 스레드
   - P2PVersusFrameBoard 구현

3. **Phase 5**: 연결 안정성
   - Heartbeat 시스템
   - 연결 끊김 감지
   - 타임아웃 처리

4. **Phase 8**: 자동화 테스트
   - 단위 테스트
   - 통합 테스트

### 🟡 Important (중요, 그 다음 구현)
5. **Phase 3**: 게임 시작/종료 제어
   - 대기실 구현
   - 게임 모드 선택

6. **Phase 4**: 공격 시스템 동기화
   - AttackMessage
   - 공격 줄 전송/수신

7. **Phase 6**: 지연 최적화
   - 이벤트 필터링
   - 배치 전송

### 🟢 Nice-to-have (선택, 시간 있으면)
8. **Phase 7**: UI/UX 개선
   - 최근 접속 IP
   - 연결 진행 상태
   - 네트워크 상태 인디케이터

---

## 8. 테스트 계획

### 8.1 개발 단계별 테스트

#### Phase 1 테스트
- [ ] 서버 시작 → 포트 리스닝 확인
- [ ] 클라이언트 연결 → Socket 연결 확인
- [ ] 양방향 통신 → 메시지 송수신 확인
- [ ] 잘못된 IP → 에러 처리 확인
- [ ] 포트 사용 중 → 에러 처리 확인

#### Phase 2 테스트
- [ ] 블록 이동 → 상대방 화면 동기화
- [ ] 블록 회전 → 상대방 화면 동기화
- [ ] 블록 착지 → 상대방 화면 동기화
- [ ] 줄 삭제 → 상대방 화면 동기화
- [ ] 점수 증가 → 상대방 화면 동기화

#### Phase 5 테스트
- [ ] 정상 연결 → Heartbeat 정상
- [ ] 네트워크 끊김 → 5초 내 감지
- [ ] 지연 발생 → "랙 걸림" 표시
- [ ] 재연결 → 대기실로 복귀

### 8.2 성능 테스트

#### 지연 시간 측정
```
목표: 키 입력 → 상대방 화면 표시 < 200ms

측정 항목:
- 직렬화 시간
- 네트워크 전송 시간
- 역직렬화 시간
- 이벤트 처리 시간
- 화면 렌더링 시간
```

#### 부하 테스트
```
시나리오:
1. 빠르게 블록 이동 (초당 10회)
2. 동시에 여러 이벤트 발생
3. 네트워크 대역폭 제한

확인 사항:
- 메시지 큐 오버플로우
- 메모리 사용량
- CPU 사용량
```

### 8.3 엣지 케이스 테스트

- [ ] 서버 먼저 종료 → 클라이언트 처리
- [ ] 클라이언트 먼저 종료 → 서버 처리
- [ ] 게임 중 네트워크 끊김
- [ ] 게임 중 높은 지연 (500ms+)
- [ ] 동시에 양쪽에서 이벤트 발생
- [ ] IP 변경 (Wi-Fi 재연결)

### 8.4 회귀 테스트

- [ ] 로컬 대전 모드 정상 작동
- [ ] 싱글 플레이 모드 정상 작동
- [ ] 기존 기능 모두 정상

---

## 9. 예상 이슈 및 대응 방안

### 9.1 기술적 이슈

#### 이슈 1: 직렬화 문제
- **문제**: Block 클래스가 Serializable 아닐 수 있음
- **해결**: Block 및 모든 이벤트 클래스에 `implements Serializable` 추가
- **대안**: JSON 직렬화 (Gson 라이브러리)

#### 이슈 2: 동기화 불일치
- **문제**: 양쪽 게임 상태가 다를 수 있음
- **해결**: 
  - 주기적으로 전체 게임 상태 동기화
  - 체크섬으로 상태 일치 확인
  - 불일치 시 재동기화

#### 이슈 3: 네트워크 지연
- **문제**: 200ms 목표 달성 어려움
- **해결**:
  - 이벤트 필터링 (불필요한 이벤트 전송 X)
  - 배치 전송
  - 압축 (선택적)
  - 입력 예측 (선택적)

#### 이슈 4: 방화벽/NAT 문제
- **문제**: 외부 네트워크에서 연결 불가
- **해결**:
  - UPnP 자동 포트 포워딩
  - 수동 포트 포워딩 가이드 제공
  - 현재는 로컬 네트워크만 지원

### 9.2 게임 로직 이슈

#### 이슈 5: 공격 줄 중복 적용
- **문제**: 네트워크 재전송으로 공격 줄 중복
- **해결**: 메시지 ID로 중복 제거

#### 이슈 6: 게임 오버 타이밍
- **문제**: 양쪽 게임 오버 시점 다름
- **해결**: 게임 오버 이벤트 즉시 전송, 승자 판정은 서버에서

---

## 10. 마일스톤

### Milestone 1: 기본 연결 (1주)
- [ ] 네트워크 패키지 구조 생성
- [ ] ConnectionManager 구현
- [ ] P2P 메뉴 UI 구현
- [ ] 서버/클라이언트 연결 성공

### Milestone 2: 이벤트 동기화 (2주)
- [ ] NetworkMessage 정의
- [ ] MessageSender/Receiver 구현
- [ ] P2PVersusFrameBoard 구현
- [ ] 기본 게임 이벤트 동기화

### Milestone 3: 안정성 확보 (1주)
- [ ] Heartbeat 구현
- [ ] 연결 끊김 처리
- [ ] 지연 감지 및 표시
- [ ] 기본 테스트 작성

### Milestone 4: 게임 제어 (1주)
- [ ] 대기실 구현
- [ ] 게임 시작/종료 제어
- [ ] 모드 선택 동기화

### Milestone 5: 공격 시스템 (1주)
- [ ] AttackMessage 구현
- [ ] 공격 줄 동기화
- [ ] 전체 게임 플레이 테스트

### Milestone 6: 최적화 및 완성 (1주)
- [ ] 성능 최적화
- [ ] UI/UX 개선
- [ ] 통합 테스트
- [ ] 문서 작성

**총 예상 기간: 7주**

---

## 11. 참고 자료

### 11.1 Java Socket 프로그래밍
- Java Socket API 문서
- ObjectInputStream/ObjectOutputStream 사용법
- 멀티스레딩 베스트 프랙티스

### 11.2 네트워크 게임 개발
- Client-Server vs P2P 아키텍처
- 게임 상태 동기화 기법
- 네트워크 지연 보상 (Lag Compensation)
- Dead Reckoning (입력 예측)

### 11.3 테스트
- JUnit 5 문서
- Mockito (네트워크 모킹)
- 네트워크 시뮬레이션 도구

---

## 12. 체크리스트

### 시작 전 확인
- [ ] 현재 로컬 대전 모드 정상 작동 확인
- [ ] Block 클래스 Serializable 여부 확인
- [ ] 모든 GameEvent Serializable 여부 확인
- [ ] 개발 환경 Java 17 확인

### Phase별 완료 체크
- [ ] Phase 1: 기본 연결 완료
- [ ] Phase 2: 이벤트 동기화 완료
- [ ] Phase 3: 게임 제어 완료
- [ ] Phase 4: 공격 시스템 완료
- [ ] Phase 5: 안정성 확보 완료
- [ ] Phase 6: 최적화 완료
- [ ] Phase 7: UI/UX 개선 완료
- [ ] Phase 8: 테스트 완료

### 최종 검수
- [ ] 200ms 지연 목표 달성
- [ ] 네트워크 끊김 정상 처리
- [ ] 모든 게임 모드 정상 작동
- [ ] 자동화 테스트 통과
- [ ] 문서 작성 완료

---

## 13. 다음 단계

1. **팀 미팅**: 계획 검토 및 역할 분담
2. **환경 설정**: 개발 환경 구축, 의존성 추가
3. **Phase 1 시작**: ConnectionManager 구현부터 시작
4. **일일 스탠드업**: 진행 상황 공유 및 이슈 논의
5. **주간 리뷰**: 마일스톤 달성 여부 점검

**Good Luck! 🚀**
