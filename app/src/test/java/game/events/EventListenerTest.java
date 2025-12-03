package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventListener 테스트")
class EventListenerTest {
    
    @Test
    @DisplayName("EventListener가 함수형 인터페이스로 작동")
    void testEventListenerAsFunctionalInterface() {
        AtomicBoolean called = new AtomicBoolean(false);
        
        EventListener<ScoreUpdateEvent> listener = event -> {
            called.set(true);
        };
        
        ScoreUpdateEvent event = new ScoreUpdateEvent(100);
        listener.onEvent(event);
        
        assertTrue(called.get());
    }
    
    @Test
    @DisplayName("EventListener가 이벤트 데이터를 받음")
    void testEventListenerReceivesEventData() {
        AtomicInteger receivedScore = new AtomicInteger(0);
        
        EventListener<ScoreUpdateEvent> listener = event -> {
            receivedScore.set(event.getNewScore());
        };
        
        ScoreUpdateEvent event = new ScoreUpdateEvent(250);
        listener.onEvent(event);
        
        assertEquals(250, receivedScore.get());
    }
    
    @Test
    @DisplayName("여러 이벤트 타입에 대한 리스너 생성")
    void testListenerForDifferentEventTypes() {
        AtomicInteger scoreCount = new AtomicInteger(0);
        AtomicInteger levelCount = new AtomicInteger(0);
        AtomicInteger gameOverCount = new AtomicInteger(0);
        
        EventListener<ScoreUpdateEvent> scoreListener = event -> scoreCount.incrementAndGet();
        EventListener<LevelUpEvent> levelListener = event -> levelCount.incrementAndGet();
        EventListener<GameOverEvent> gameOverListener = event -> gameOverCount.incrementAndGet();
        
        scoreListener.onEvent(new ScoreUpdateEvent(100));
        scoreListener.onEvent(new ScoreUpdateEvent(200));
        levelListener.onEvent(new LevelUpEvent(2, 1));
        gameOverListener.onEvent(new GameOverEvent(500, 1));
        
        assertEquals(2, scoreCount.get());
        assertEquals(1, levelCount.get());
        assertEquals(1, gameOverCount.get());
    }
    
    @Test
    @DisplayName("EventListener가 null 이벤트를 처리")
    void testEventListenerWithNullEvent() {
        AtomicBoolean called = new AtomicBoolean(false);
        
        EventListener<ScoreUpdateEvent> listener = event -> {
            called.set(true);
            assertNull(event);
        };
        
        listener.onEvent(null);
        assertTrue(called.get());
    }
    
    @Test
    @DisplayName("메서드 참조를 사용한 EventListener")
    void testEventListenerWithMethodReference() {
        TestEventHandler handler = new TestEventHandler();
        
        EventListener<ScoreUpdateEvent> listener = handler::handleScoreUpdate;
        
        listener.onEvent(new ScoreUpdateEvent(300));
        
        assertEquals(300, handler.getLastScore());
        assertEquals(1, handler.getCallCount());
    }
    
    @Test
    @DisplayName("람다를 사용한 복잡한 이벤트 처리")
    void testComplexEventHandlingWithLambda() {
        AtomicReference<String> result = new AtomicReference<>("");
        
        EventListener<LineClearedEvent> listener = event -> {
            int[] lines = event.getClearedLines();
            int score = event.getScore();
            result.set(String.format("Cleared %d lines for %d points", 
                                    lines.length, score));
        };
        
        LineClearedEvent event = new LineClearedEvent(new int[]{1, 2, 3}, 300, 1);
        listener.onEvent(event);
        
        assertEquals("Cleared 3 lines for 300 points", result.get());
    }
    
    @Test
    @DisplayName("EventListener 체이닝")
    void testEventListenerChaining() {
        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);
        AtomicInteger count3 = new AtomicInteger(0);
        
        EventListener<ScoreUpdateEvent> listener1 = event -> count1.incrementAndGet();
        EventListener<ScoreUpdateEvent> listener2 = event -> count2.incrementAndGet();
        EventListener<ScoreUpdateEvent> listener3 = event -> count3.incrementAndGet();
        
        // 수동으로 여러 리스너 호출 (체이닝 시뮬레이션)
        ScoreUpdateEvent event = new ScoreUpdateEvent(100);
        listener1.onEvent(event);
        listener2.onEvent(event);
        listener3.onEvent(event);
        
        assertEquals(1, count1.get());
        assertEquals(1, count2.get());
        assertEquals(1, count3.get());
    }
    
    @Test
    @DisplayName("EventListener 예외 처리")
    void testEventListenerExceptionHandling() {
        EventListener<ScoreUpdateEvent> listener = event -> {
            throw new RuntimeException("Test exception");
        };
        
        ScoreUpdateEvent event = new ScoreUpdateEvent(100);
        
        assertThrows(RuntimeException.class, () -> {
            listener.onEvent(event);
        });
    }
    
    // 테스트용 헬퍼 클래스
    private static class TestEventHandler {
        private int lastScore = 0;
        private int callCount = 0;
        
        public void handleScoreUpdate(ScoreUpdateEvent event) {
            if (event != null) {
                lastScore = event.getNewScore();
            }
            callCount++;
        }
        
        public int getLastScore() {
            return lastScore;
        }
        
        public int getCallCount() {
            return callCount;
        }
    }
}
