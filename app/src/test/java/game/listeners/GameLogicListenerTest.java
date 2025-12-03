package game.listeners;

import game.events.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameLogicListener 테스트")
class GameLogicListenerTest {
    
    private GameLogicListener listener;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    void setUp() {
        listener = new GameLogicListener();
        
        // System.out 캡처 설정
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }
    
    @Test
    @DisplayName("LINE_CLEARED 이벤트 처리 - 테트리스 (4줄)")
    void testLineClearedEventTetris() {
        LineClearedEvent event = new LineClearedEvent(new int[]{0, 1, 2, 3}, 800, 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Tetris achieved"));
        assertTrue(output.contains("Special bonus applied"));
    }
    
    @Test
    @DisplayName("LINE_CLEARED 이벤트 처리 - 더블/트리플 (2-3줄)")
    void testLineClearedEventMultiple() {
        LineClearedEvent event = new LineClearedEvent(new int[]{5, 6}, 200, 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Multi-line clear"));
        assertTrue(output.contains("Speed adjustment applied"));
    }
    
    @Test
    @DisplayName("LINE_CLEARED 이벤트 처리 - 싱글 (1줄)")
    void testLineClearedEventSingle() {
        LineClearedEvent event = new LineClearedEvent(new int[]{7}, 100, 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Processing 1 line clear(s)"));
        assertFalse(output.contains("Tetris achieved"));
        assertFalse(output.contains("Multi-line clear"));
    }
    
    @Test
    @DisplayName("LEVEL_UP 이벤트 처리 - 일반 레벨")
    void testLevelUpEvent() {
        LevelUpEvent event = new LevelUpEvent(3, 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Level 3 reached"));
        assertTrue(output.contains("Adjusting game difficulty"));
    }
    
    @Test
    @DisplayName("LEVEL_UP 이벤트 처리 - 마일스톤 레벨 (5의 배수)")
    void testLevelUpEventMilestone() {
        LevelUpEvent event = new LevelUpEvent(10, 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Level 10 reached"));
        assertTrue(output.contains("Milestone level reached"));
        assertTrue(output.contains("Special effects activated"));
    }
    
    @Test
    @DisplayName("ITEM_ACTIVATED 이벤트 처리 - CLEAR_LINE")
    void testItemActivatedEventClearLine() {
        ItemActivatedEvent event = new ItemActivatedEvent("CLEAR_LINE", 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Line clear item activated"));
        assertTrue(output.contains("Processing line removal"));
    }
    
    @Test
    @DisplayName("ITEM_ACTIVATED 이벤트 처리 - SLOW_DOWN")
    void testItemActivatedEventSlowDown() {
        ItemActivatedEvent event = new ItemActivatedEvent("SLOW_DOWN", 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Slow down item activated"));
        assertTrue(output.contains("Reducing game speed"));
    }
    
    @Test
    @DisplayName("ITEM_ACTIVATED 이벤트 처리 - DOUBLE_SCORE")
    void testItemActivatedEventDoubleScore() {
        ItemActivatedEvent event = new ItemActivatedEvent("DOUBLE_SCORE", 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Double score item activated"));
        assertTrue(output.contains("Next score will be doubled"));
    }
    
    @Test
    @DisplayName("ITEM_ACTIVATED 이벤트 처리 - 알 수 없는 아이템")
    void testItemActivatedEventUnknown() {
        ItemActivatedEvent event = new ItemActivatedEvent("UNKNOWN_ITEM", 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Unknown item activated"));
        assertTrue(output.contains("UNKNOWN_ITEM"));
    }
    
    @Test
    @DisplayName("GAME_OVER 이벤트 처리")
    void testGameOverEvent() {
        GameOverEvent event = new GameOverEvent(5000, 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Game over for player 1"));
        assertTrue(output.contains("final score: 5000"));
        assertTrue(output.contains("Saving game statistics"));
    }
    
    @Test
    @DisplayName("알 수 없는 이벤트 타입 처리")
    void testUnknownEventType() {
        ScoreUpdateEvent event = new ScoreUpdateEvent(1000);
        
        listener.onEvent(event);
        
        // 아무 것도 출력되지 않아야 함
        String output = outputStream.toString();
        assertTrue(output.isEmpty() || output.trim().isEmpty());
    }
    
    @Test
    @DisplayName("여러 이벤트 연속 처리")
    void testMultipleEvents() {
        listener.onEvent(new LineClearedEvent(new int[]{0}, 100, 1));
        listener.onEvent(new LevelUpEvent(2, 1));
        listener.onEvent(new ItemActivatedEvent("CLEAR_LINE", 1));
        
        String output = outputStream.toString();
        assertTrue(output.contains("Processing 1 line clear(s)"));
        assertTrue(output.contains("Level 2 reached"));
        assertTrue(output.contains("Line clear item activated"));
    }
    
    @Test
    void tearDown() {
        // System.out 복원
        System.setOut(originalOut);
    }
}
