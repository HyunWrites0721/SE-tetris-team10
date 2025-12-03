package game.events;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventBus 테스트")
class EventBusTest {
    
    private EventBus eventBus;
    
    @BeforeEach
    void setUp() {
        eventBus = new EventBus();
    }
    
    @AfterEach
    void tearDown() {
        if (eventBus != null) {
            eventBus.shutdown();
        }
    }
    
    @Test
    @DisplayName("EventBus 생성 테스트")
    void testEventBusCreation() {
        EventBus syncBus = new EventBus();
        assertNotNull(syncBus);
        syncBus.shutdown();
        
        EventBus asyncBus = new EventBus(true);
        assertNotNull(asyncBus);
        asyncBus.shutdown();
    }
    
    @Test
    @DisplayName("이벤트 구독 및 발행 테스트")
    void testSubscribeAndPublish() {
        AtomicInteger callCount = new AtomicInteger(0);
        
        EventListener<ScoreUpdateEvent> listener = event -> {
            callCount.incrementAndGet();
            assertEquals(100, event.getNewScore());
        };
        
        eventBus.subscribe(ScoreUpdateEvent.class, listener);
        eventBus.publish(new ScoreUpdateEvent(100));
        
        assertEquals(1, callCount.get());
    }
    
    @Test
    @DisplayName("여러 리스너 등록 테스트")
    void testMultipleListeners() {
        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);
        AtomicInteger count3 = new AtomicInteger(0);
        
        eventBus.subscribe(ScoreUpdateEvent.class, event -> count1.incrementAndGet());
        eventBus.subscribe(ScoreUpdateEvent.class, event -> count2.incrementAndGet());
        eventBus.subscribe(ScoreUpdateEvent.class, event -> count3.incrementAndGet());
        
        assertEquals(3, eventBus.getListenerCount(ScoreUpdateEvent.class));
        
        eventBus.publish(new ScoreUpdateEvent(200));
        
        assertEquals(1, count1.get());
        assertEquals(1, count2.get());
        assertEquals(1, count3.get());
    }
    
    @Test
    @DisplayName("우선순위 기반 리스너 실행 순서 테스트")
    void testListenerPriority() {
        List<Integer> executionOrder = new ArrayList<>();
        
        // 우선순위: 낮을수록 먼저 실행
        eventBus.subscribe(ScoreUpdateEvent.class, event -> executionOrder.add(3), 30);
        eventBus.subscribe(ScoreUpdateEvent.class, event -> executionOrder.add(1), 10);
        eventBus.subscribe(ScoreUpdateEvent.class, event -> executionOrder.add(2), 20);
        
        eventBus.publish(new ScoreUpdateEvent(100));
        
        assertEquals(3, executionOrder.size());
        assertEquals(1, executionOrder.get(0));
        assertEquals(2, executionOrder.get(1));
        assertEquals(3, executionOrder.get(2));
    }
    
    @Test
    @DisplayName("리스너 구독 해제 테스트")
    void testUnsubscribe() {
        AtomicInteger callCount = new AtomicInteger(0);
        EventListener<ScoreUpdateEvent> listener = event -> callCount.incrementAndGet();
        
        eventBus.subscribe(ScoreUpdateEvent.class, listener);
        assertEquals(1, eventBus.getListenerCount(ScoreUpdateEvent.class));
        
        eventBus.publish(new ScoreUpdateEvent(100));
        assertEquals(1, callCount.get());
        
        boolean removed = eventBus.unsubscribe(ScoreUpdateEvent.class, listener);
        assertTrue(removed);
        assertEquals(0, eventBus.getListenerCount(ScoreUpdateEvent.class));
        
        eventBus.publish(new ScoreUpdateEvent(200));
        assertEquals(1, callCount.get()); // 더 이상 증가하지 않음
    }
    
    @Test
    @DisplayName("존재하지 않는 리스너 해제 테스트")
    void testUnsubscribeNonExistentListener() {
        EventListener<ScoreUpdateEvent> listener = event -> {};
        
        boolean removed = eventBus.unsubscribe(ScoreUpdateEvent.class, listener);
        assertFalse(removed);
    }
    
    @Test
    @DisplayName("null 이벤트 발행 테스트")
    void testPublishNullEvent() {
        assertDoesNotThrow(() -> {
            eventBus.publish(null);
        });
    }
    
    @Test
    @DisplayName("리스너 없는 이벤트 발행 테스트")
    void testPublishWithNoListeners() {
        assertDoesNotThrow(() -> {
            eventBus.publish(new ScoreUpdateEvent(100));
        });
        
        assertEquals(0, eventBus.getListenerCount(ScoreUpdateEvent.class));
    }
    
    @Test
    @DisplayName("리스너에서 예외 발생 시 다른 리스너 실행 테스트")
    void testListenerExceptionHandling() {
        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);
        
        eventBus.subscribe(ScoreUpdateEvent.class, event -> {
            count1.incrementAndGet();
            throw new RuntimeException("Test exception");
        });
        
        eventBus.subscribe(ScoreUpdateEvent.class, event -> {
            count2.incrementAndGet();
        });
        
        assertDoesNotThrow(() -> {
            eventBus.publish(new ScoreUpdateEvent(100));
        });
        
        assertEquals(1, count1.get());
        assertEquals(1, count2.get()); // 예외 발생해도 다음 리스너 실행됨
    }
    
    @Test
    @DisplayName("비동기 EventBus 테스트")
    void testAsyncEventBus() throws InterruptedException {
        EventBus asyncBus = new EventBus(true);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger callCount = new AtomicInteger(0);
        
        asyncBus.subscribe(ScoreUpdateEvent.class, event -> {
            callCount.incrementAndGet();
            latch.countDown();
        });
        
        asyncBus.publish(new ScoreUpdateEvent(100));
        
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertTrue(completed);
        assertEquals(1, callCount.get());
        
        asyncBus.shutdown();
    }
    
    @Test
    @DisplayName("여러 이벤트 타입 동시 처리 테스트")
    void testMultipleEventTypes() {
        AtomicInteger scoreCount = new AtomicInteger(0);
        AtomicInteger levelCount = new AtomicInteger(0);
        
        eventBus.subscribe(ScoreUpdateEvent.class, event -> scoreCount.incrementAndGet());
        eventBus.subscribe(LevelUpEvent.class, event -> levelCount.incrementAndGet());
        
        eventBus.publish(new ScoreUpdateEvent(100));
        eventBus.publish(new LevelUpEvent(2, 1));
        eventBus.publish(new ScoreUpdateEvent(200));
        
        assertEquals(2, scoreCount.get());
        assertEquals(1, levelCount.get());
    }
    
    @Test
    @DisplayName("EventBus shutdown 테스트")
    void testShutdown() {
        EventBus asyncBus = new EventBus(true);
        
        asyncBus.subscribe(ScoreUpdateEvent.class, event -> {});
        assertEquals(1, asyncBus.getListenerCount(ScoreUpdateEvent.class));
        
        asyncBus.shutdown();
        
        // shutdown 후 리스너가 제거됨
        assertEquals(0, asyncBus.getListenerCount(ScoreUpdateEvent.class));
    }
    
    @Test
    @DisplayName("같은 리스너를 여러 번 등록 테스트")
    void testMultipleSubscriptionsSameListener() {
        AtomicInteger callCount = new AtomicInteger(0);
        EventListener<ScoreUpdateEvent> listener = event -> callCount.incrementAndGet();
        
        eventBus.subscribe(ScoreUpdateEvent.class, listener);
        eventBus.subscribe(ScoreUpdateEvent.class, listener);
        eventBus.subscribe(ScoreUpdateEvent.class, listener);
        
        assertEquals(3, eventBus.getListenerCount(ScoreUpdateEvent.class));
        
        eventBus.publish(new ScoreUpdateEvent(100));
        assertEquals(3, callCount.get());
    }
    
    @Test
    @DisplayName("우선순위 없이 기본 우선순위로 등록 테스트")
    void testDefaultPriority() {
        AtomicInteger callCount = new AtomicInteger(0);
        
        eventBus.subscribe(ScoreUpdateEvent.class, event -> callCount.incrementAndGet());
        
        eventBus.publish(new ScoreUpdateEvent(100));
        
        assertEquals(1, callCount.get());
    }
    
    @Test
    @DisplayName("동기 모드 이벤트 즉시 처리 테스트")
    void testSynchronousExecution() {
        AtomicInteger callCount = new AtomicInteger(0);
        
        eventBus.subscribe(ScoreUpdateEvent.class, event -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            callCount.incrementAndGet();
        });
        
        long startTime = System.currentTimeMillis();
        eventBus.publish(new ScoreUpdateEvent(100));
        long endTime = System.currentTimeMillis();
        
        // 동기 모드에서는 이벤트 처리가 완료될 때까지 대기
        assertTrue(endTime - startTime >= 100);
        assertEquals(1, callCount.get());
    }
}
