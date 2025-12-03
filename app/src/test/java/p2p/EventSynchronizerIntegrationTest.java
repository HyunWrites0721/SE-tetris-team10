package p2p;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import game.events.*;
import network.MessageSender;
import network.messages.*;

import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * EventSynchronizer 통합 테스트
 * MessageSender 클래스를 확장하여 테스트
 */
class EventSynchronizerIntegrationTest {

    private EventSynchronizer synchronizer;
    private EventBus localEventBus;
    private EventBus remoteEventBus;
    private TestMessageSender testSender;
    private final int playerId = 1;
    
    // MessageSender를 확장한 테스트용 클래스
    private static class TestMessageSender extends MessageSender {
        private final AtomicInteger messageCount = new AtomicInteger(0);
        private NetworkMessage lastMessage = null;
        
        public TestMessageSender() throws Exception {
            super(new ObjectOutputStream(new ByteArrayOutputStream()));
        }
        
        @Override
        public boolean sendMessage(NetworkMessage message) {
            messageCount.incrementAndGet();
            lastMessage = message;
            return true; // 항상 성공
        }
        
        public int getMessageCount() {
            return messageCount.get();
        }
        
        public NetworkMessage getLastMessage() {
            return lastMessage;
        }
        
        public void reset() {
            messageCount.set(0);
            lastMessage = null;
        }
    }
    
    @BeforeEach
    void setUp() throws Exception {
        localEventBus = new EventBus();
        remoteEventBus = new EventBus();
        testSender = new TestMessageSender();
        
        synchronizer = new EventSynchronizer(
            localEventBus,
            remoteEventBus,
            testSender,
            playerId
        );
    }

    // ==================== 이벤트 발행 테스트 ====================
    
    @Test
    void testBlockMovedEventPublishing() throws InterruptedException {
        testSender.reset();
        
        BlockMovedEvent event = new BlockMovedEvent(5, 10, 0, 0); // blockType=0 (I), rotation=0
        localEventBus.publish(event);
        
        Thread.sleep(100); // 비동기 처리 대기
        
        assertTrue(testSender.getMessageCount() >= 1, "BlockMovedEvent should be sent");
    }
    
    @Test
    void testBlockRotatedEventPublishing() throws InterruptedException {
        testSender.reset();
        
        BlockRotatedEvent event = new BlockRotatedEvent(3, 7, 2, 90); // blockType=2 (T), rotation=90
        localEventBus.publish(event);
        
        Thread.sleep(100);
        
        assertTrue(testSender.getMessageCount() >= 1, "BlockRotatedEvent should be sent");
    }
    
    @Test
    void testBlockPlacedEventPublishing() throws InterruptedException {
        testSender.reset();
        
        BlockPlacedEvent event = new BlockPlacedEvent();
        localEventBus.publish(event);
        
        Thread.sleep(100);
        
        assertTrue(testSender.getMessageCount() >= 1, "BlockPlacedEvent should be sent");
    }
    
    @Test
    void testScoreUpdateEventPublishing() throws InterruptedException {
        testSender.reset();
        
        ScoreUpdateEvent event = new ScoreUpdateEvent(1000);
        localEventBus.publish(event);
        
        Thread.sleep(100);
        
        assertTrue(testSender.getMessageCount() >= 1, "ScoreUpdateEvent should be sent");
    }
    
    @Test
    void testGameOverEventPublishing() throws InterruptedException {
        testSender.reset();
        
        GameOverEvent event = new GameOverEvent(5000, 10);
        localEventBus.publish(event);
        
        Thread.sleep(100);
        
        assertTrue(testSender.getMessageCount() >= 1, "GameOverEvent should be sent");
    }
    
    @Test
    void testLevelUpEventPublishing() throws InterruptedException {
        testSender.reset();
        
        LevelUpEvent event = new LevelUpEvent(5, 3000);
        localEventBus.publish(event);
        
        Thread.sleep(100);
        
        assertTrue(testSender.getMessageCount() >= 1, "LevelUpEvent should be sent");
    }
    
    // ==================== 메시지 수신 테스트 ====================
    
    @Test
    void testReceiveGameEventMessage() throws InterruptedException {
        AtomicInteger receivedCount = new AtomicInteger(0);
        
        // 원격 EventBus에 리스너 등록
        remoteEventBus.subscribe(BlockMovedEvent.class, e -> {
            receivedCount.incrementAndGet();
        });
        
        // 네트워크 메시지 수신 시뮬레이션
        BlockMovedEvent event = new BlockMovedEvent(2, 4, 3, 180); // blockType=3 (L), rotation=180
        GameEventMessage message = new GameEventMessage(event, 2);
        
        synchronizer.onMessageReceived(message);
        
        Thread.sleep(100);
        
        assertEquals(1, receivedCount.get(), "Remote EventBus should receive the event");
    }
    
    @Test
    void testReceiveAttackMessage() throws InterruptedException {
        AtomicInteger attackCount = new AtomicInteger(0);
        
        // 원격 EventBus에 AttackEvent 리스너 등록
        remoteEventBus.subscribe(game.events.AttackEvent.class, e -> {
            attackCount.incrementAndGet();
        });
        
        // AttackMessage 수신 시뮬레이션
        int[][] pattern = {{1, 0, 1}, {1, 1, 0}};
        AttackMessage attackMsg = new AttackMessage(3, 2, pattern, 5);
        
        synchronizer.onMessageReceived(attackMsg);
        
        Thread.sleep(100);
        
        assertEquals(1, attackCount.get(), "Remote EventBus should receive AttackEvent");
    }
    
    // ==================== 라이프사이클 테스트 ====================
    
    @Test
    void testShutdown() {
        assertDoesNotThrow(() -> {
            synchronizer.shutdown();
        }, "Shutdown should not throw exception");
    }
    
    @Test
    void testConnectionLost() {
        assertDoesNotThrow(() -> {
            synchronizer.onConnectionLost();
        }, "Connection lost handling should not throw exception");
    }
    
    // ==================== 성능 테스트 ====================
    
    @Test
    void testMultipleEventsPublishing() throws InterruptedException {
        testSender.reset();
        
        // 다양한 이벤트 발행
        localEventBus.publish(new BlockMovedEvent(1, 2, 0, 0)); // blockType=0 (I)
        localEventBus.publish(new BlockRotatedEvent(3, 4, 2, 90)); // blockType=2 (T)
        localEventBus.publish(new BlockPlacedEvent());
        localEventBus.publish(new ScoreUpdateEvent(500));
        localEventBus.publish(new LevelUpEvent(2, 1000));
        
        Thread.sleep(300);
        
        assertTrue(testSender.getMessageCount() >= 5, "All events should be sent");
    }
    
    @Test
    void testEventFiltering() throws InterruptedException {
        testSender.reset();
        
        // TICK 이벤트는 필터링되어야 함
        localEventBus.publish(new TickEvent());
        
        Thread.sleep(100);
        
        assertEquals(0, testSender.getMessageCount(), "TICK events should be filtered");
    }
    
    // ==================== 통합 시나리오 테스트 ====================
    
    @Test
    void testCompleteGameFlow() throws InterruptedException {
        testSender.reset();
        AtomicInteger remoteEventCount = new AtomicInteger(0);
        
        // 원격 EventBus에 모든 이벤트 리스너 등록
        remoteEventBus.subscribe(GameEvent.class, e -> {
            remoteEventCount.incrementAndGet();
        });
        
        // 로컬에서 게임 플레이 시뮬레이션
        localEventBus.publish(new BlockMovedEvent(5, 10, 0, 0)); // blockType=0 (I)
        Thread.sleep(50);
        
        localEventBus.publish(new BlockRotatedEvent(5, 10, 0, 90)); // blockType=0 (I), rotation=90
        Thread.sleep(50);
        
        localEventBus.publish(new BlockPlacedEvent());
        Thread.sleep(50);
        
        localEventBus.publish(new ScoreUpdateEvent(200));
        Thread.sleep(50);
        
        // 원격에서 이벤트 수신 시뮬레이션
        BlockMovedEvent remoteEvent = new BlockMovedEvent(3, 8, 2, 0); // blockType=2 (T)
        GameEventMessage remoteMsg = new GameEventMessage(remoteEvent, 2);
        synchronizer.onMessageReceived(remoteMsg);
        
        Thread.sleep(200);
        
        // 로컬 이벤트가 전송되었는지 확인
        assertTrue(testSender.getMessageCount() >= 4, "All local events should be sent");
        
        // 원격 이벤트가 수신되었는지 확인
        assertTrue(remoteEventCount.get() >= 1, "Remote events should be received");
    }
}
