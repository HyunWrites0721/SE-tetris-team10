package game.listeners;

import game.GameView;
import game.events.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UIUpdateListener 테스트")
class UIUpdateListenerTest {
    
    // GameView를 직접 구현한 테스트용 클래스
    private static class TestGameView extends GameView {
        private AtomicInteger repaintCount = new AtomicInteger(0);
        
        public TestGameView() {
            super(false); // item 사용 안 함
        }
        
        @Override
        public void repaintBlock() {
            repaintCount.incrementAndGet();
        }
        
        public int getRepaintCount() {
            return repaintCount.get();
        }
        
        public void resetRepaintCount() {
            repaintCount.set(0);
        }
    }
    
    private TestGameView testGameView;
    private UIUpdateListener listener;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    void setUp() {
        testGameView = new TestGameView();
        listener = new UIUpdateListener(testGameView);
        
        // System.out 캡처 설정
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }
    
    @Test
    @DisplayName("UIUpdateListener 생성 테스트")
    void testUIUpdateListenerCreation() {
        UIUpdateListener listener = new UIUpdateListener(testGameView);
        assertNotNull(listener);
    }
    
    @Test
    @DisplayName("null GameView로 생성")
    void testUIUpdateListenerWithNullGameView() {
        UIUpdateListener listener = new UIUpdateListener(null);
        assertNotNull(listener);
        
        // null GameView에서는 아무 작업도 하지 않아야 함
        listener.onEvent(new BlockPlacedEvent(5, 10, 1, 1));
    }
    
    @Test
    @DisplayName("BLOCK_PLACED 이벤트 처리")
    void testBlockPlacedEvent() throws InterruptedException {
        BlockPlacedEvent event = new BlockPlacedEvent(5, 10, 1, 1);
        
        listener.onEvent(event);
        
        // EDT에서 실행되므로 약간 대기
        Thread.sleep(100);
        
        assertTrue(testGameView.getRepaintCount() >= 1);
        String output = outputStream.toString();
        assertTrue(output.contains("UI: Block placed at (5, 10)"));
    }
    
    @Test
    @DisplayName("LINE_CLEARED 이벤트 처리")
    void testLineClearedEvent() throws InterruptedException {
        LineClearedEvent event = new LineClearedEvent(new int[]{0, 1, 2}, 300, 1);
        
        listener.onEvent(event);
        
        Thread.sleep(100);
        
        assertTrue(testGameView.getRepaintCount() >= 1);
        String output = outputStream.toString();
        assertTrue(output.contains("UI: Line cleared animation for 3 lines"));
    }
    
    @Test
    @DisplayName("LEVEL_UP 이벤트 처리")
    void testLevelUpEvent() throws InterruptedException {
        LevelUpEvent event = new LevelUpEvent(5, 1);
        
        listener.onEvent(event);
        
        Thread.sleep(100);
        
        assertTrue(testGameView.getRepaintCount() >= 1);
        String output = outputStream.toString();
        assertTrue(output.contains("UI: Level up effect for level 5"));
    }
    
    @Test
    @DisplayName("ITEM_ACTIVATED 이벤트 처리")
    void testItemActivatedEvent() throws InterruptedException {
        ItemActivatedEvent event = new ItemActivatedEvent("CLEAR_LINE", 1);
        
        listener.onEvent(event);
        
        Thread.sleep(100);
        
        assertTrue(testGameView.getRepaintCount() >= 1);
        String output = outputStream.toString();
        assertTrue(output.contains("UI: Item effect for CLEAR_LINE"));
    }
    
    @Test
    @DisplayName("GAME_OVER 이벤트 처리")
    void testGameOverEvent() throws InterruptedException {
        GameOverEvent event = new GameOverEvent(10000, 1);
        
        listener.onEvent(event);
        
        Thread.sleep(100);
        
        assertTrue(testGameView.getRepaintCount() >= 1);
        String output = outputStream.toString();
        assertTrue(output.contains("UI: Game over screen with score 10000"));
    }
    
    @Test
    @DisplayName("알 수 없는 이벤트 타입 처리")
    void testUnknownEventType() throws InterruptedException {
        testGameView.resetRepaintCount();
        ScoreUpdateEvent event = new ScoreUpdateEvent(1000);
        
        listener.onEvent(event);
        
        Thread.sleep(100);
        
        // repaintBlock이 호출되지 않아야 함
        assertEquals(0, testGameView.getRepaintCount());
    }
    
    @Test
    @DisplayName("여러 이벤트 연속 처리")
    void testMultipleEvents() throws InterruptedException {
        listener.onEvent(new BlockPlacedEvent(5, 10, 1, 1));
        listener.onEvent(new LineClearedEvent(new int[]{0}, 100, 1));
        listener.onEvent(new LevelUpEvent(2, 1));
        
        Thread.sleep(200);
        
        assertTrue(testGameView.getRepaintCount() >= 3);
        String output = outputStream.toString();
        assertTrue(output.contains("UI: Block placed"));
        assertTrue(output.contains("UI: Line cleared"));
        assertTrue(output.contains("UI: Level up"));
    }
    
    @Test
    @DisplayName("EDT에서 UI 업데이트 실행 확인")
    void testUIUpdateOnEDT() throws InterruptedException {
        testGameView.resetRepaintCount();
        BlockPlacedEvent event = new BlockPlacedEvent(5, 10, 1, 1);
        
        listener.onEvent(event);
        
        // EDT에서 실행되므로 즉시 호출되지 않을 수 있음
        Thread.sleep(10);
        
        // 대기 후 호출 확인
        Thread.sleep(100);
        assertTrue(testGameView.getRepaintCount() >= 1);
    }
    
    @Test
    @DisplayName("GameView가 null일 때 예외 발생하지 않음")
    void testNullGameViewNoException() {
        UIUpdateListener listenerWithNull = new UIUpdateListener(null);
        
        assertDoesNotThrow(() -> {
            listenerWithNull.onEvent(new BlockPlacedEvent(5, 10, 1, 1));
        });
    }
    
    @Test
    @DisplayName("다양한 블록 위치 처리")
    void testVariousBlockPositions() throws InterruptedException {
        listener.onEvent(new BlockPlacedEvent(0, 0, 1, 1));
        listener.onEvent(new BlockPlacedEvent(9, 19, 2, 1));
        listener.onEvent(new BlockPlacedEvent(-1, -1, 3, 1));
        
        Thread.sleep(200);
        
        String output = outputStream.toString();
        assertTrue(output.contains("(0, 0)"));
        assertTrue(output.contains("(9, 19)"));
        assertTrue(output.contains("(-1, -1)"));
    }
    
    @Test
    void tearDown() {
        // System.out 복원
        System.setOut(originalOut);
    }
}
