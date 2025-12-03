package game.listeners;

import game.ScoreBoard;
import game.events.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScoreBoardListener 테스트")
class ScoreBoardListenerTest {
    
    private ScoreBoard scoreBoard;
    private ScoreBoardListener listener;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    void setUp() {
        scoreBoard = new ScoreBoard();
        listener = new ScoreBoardListener(scoreBoard);
        
        // System.out 캡처 설정
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }
    
    @Test
    @DisplayName("ScoreBoardListener 생성 테스트")
    void testScoreBoardListenerCreation() {
        ScoreBoardListener listener = new ScoreBoardListener(scoreBoard);
        assertNotNull(listener);
    }
    
    @Test
    @DisplayName("null ScoreBoard로 생성")
    void testScoreBoardListenerWithNullScoreBoard() {
        ScoreBoardListener listener = new ScoreBoardListener(null);
        assertNotNull(listener);
        
        // null ScoreBoard에서는 아무 작업도 하지 않아야 함
        listener.onEvent(new LineClearedEvent(new int[]{0}, 100, 1));
    }
    
    @Test
    @DisplayName("LINE_CLEARED 이벤트 - 최고점수 미만")
    void testLineClearedEventBelowHighScore() {
        scoreBoard.setHighScore(1000);
        LineClearedEvent event = new LineClearedEvent(new int[]{0}, 500, 1);
        
        listener.onEvent(event);
        
        assertEquals(1000, scoreBoard.getHighScore());
        String output = outputStream.toString();
        assertTrue(output.contains("Score updated: +500"));
        assertTrue(output.contains("Lines: +1"));
    }
    
    @Test
    @DisplayName("LINE_CLEARED 이벤트 - 최고점수 갱신")
    void testLineClearedEventNewHighScore() {
        scoreBoard.setHighScore(500);
        LineClearedEvent event = new LineClearedEvent(new int[]{0, 1, 2}, 1500, 1);
        
        listener.onEvent(event);
        
        assertEquals(1500, scoreBoard.getHighScore());
        String output = outputStream.toString();
        assertTrue(output.contains("Score updated: +1500"));
        assertTrue(output.contains("Lines: +3"));
    }
    
    @Test
    @DisplayName("LINE_CLEARED 이벤트 - 여러 줄 클리어")
    void testLineClearedEventMultipleLines() {
        LineClearedEvent event = new LineClearedEvent(new int[]{0, 1, 2, 3}, 800, 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Lines: +4"));
    }
    
    @Test
    @DisplayName("LEVEL_UP 이벤트 처리")
    void testLevelUpEvent() {
        LevelUpEvent event = new LevelUpEvent(5, 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Level up!"));
        assertTrue(output.contains("New level: 5"));
    }
    
    @Test
    @DisplayName("GAME_OVER 이벤트 - 최고점수 미만")
    void testGameOverEventBelowHighScore() {
        scoreBoard.setHighScore(5000);
        GameOverEvent event = new GameOverEvent(3000, 1);
        
        listener.onEvent(event);
        
        assertEquals(5000, scoreBoard.getHighScore());
        String output = outputStream.toString();
        assertTrue(output.contains("Game Over!"));
        assertTrue(output.contains("Final score: 3000"));
    }
    
    @Test
    @DisplayName("GAME_OVER 이벤트 - 최고점수 갱신")
    void testGameOverEventNewHighScore() {
        scoreBoard.setHighScore(3000);
        GameOverEvent event = new GameOverEvent(5000, 1);
        
        listener.onEvent(event);
        
        assertEquals(5000, scoreBoard.getHighScore());
        String output = outputStream.toString();
        assertTrue(output.contains("Game Over!"));
        assertTrue(output.contains("Final score: 5000"));
    }
    
    @Test
    @DisplayName("BLOCK_PLACED 이벤트 처리")
    void testBlockPlacedEvent() {
        BlockPlacedEvent event = new BlockPlacedEvent(5, 10, 1, 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Block placed at (5, 10)"));
    }
    
    @Test
    @DisplayName("알 수 없는 이벤트 타입 처리")
    void testUnknownEventType() {
        ScoreUpdateEvent event = new ScoreUpdateEvent(1000);
        
        listener.onEvent(event);
        
        // 아무 것도 처리하지 않아야 함
    }
    
    @Test
    @DisplayName("여러 이벤트 연속 처리")
    void testMultipleEvents() {
        listener.onEvent(new LineClearedEvent(new int[]{0}, 100, 1));
        listener.onEvent(new LevelUpEvent(2, 1));
        listener.onEvent(new BlockPlacedEvent(5, 10, 1, 1));
        
        String output = outputStream.toString();
        assertTrue(output.contains("Score updated"));
        assertTrue(output.contains("Level up!"));
        assertTrue(output.contains("Block placed"));
    }
    
    @Test
    @DisplayName("점수 누적 테스트")
    void testScoreAccumulation() {
        scoreBoard.setHighScore(0);
        
        listener.onEvent(new LineClearedEvent(new int[]{0}, 100, 1));
        assertEquals(100, scoreBoard.getHighScore());
        
        listener.onEvent(new LineClearedEvent(new int[]{1}, 200, 1));
        assertEquals(200, scoreBoard.getHighScore());
        
        listener.onEvent(new LineClearedEvent(new int[]{2, 3}, 500, 1));
        assertEquals(500, scoreBoard.getHighScore());
    }
    
    @Test
    @DisplayName("멀티플레이어 - 다른 플레이어 이벤트")
    void testMultiPlayerEvents() {
        scoreBoard.setHighScore(1000);
        
        // 플레이어 1의 점수
        listener.onEvent(new LineClearedEvent(new int[]{0}, 1500, 1));
        assertEquals(1500, scoreBoard.getHighScore());
        
        // 플레이어 2의 점수
        listener.onEvent(new LineClearedEvent(new int[]{0}, 2000, 2));
        assertEquals(2000, scoreBoard.getHighScore());
    }
    
    @Test
    void tearDown() {
        // System.out 복원
        System.setOut(originalOut);
    }
}
