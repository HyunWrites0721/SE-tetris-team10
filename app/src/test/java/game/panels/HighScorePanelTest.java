package game.panels;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HighScorePanel 테스트")
class HighScorePanelTest {
    
    private HighScorePanel panel;
    
    @BeforeEach
    void setUp() {
        panel = new HighScorePanel();
    }
    
    @Test
    @DisplayName("초기 최고 점수는 0")
    void testInitialHighScore() {
        assertEquals(0, panel.getHighScore(), "초기 최고 점수는 0이어야 함");
    }
    
    @Test
    @DisplayName("최고 점수 설정 및 조회")
    void testSetAndGetHighScore() {
        panel.setHighScore(500);
        assertEquals(500, panel.getHighScore(), "설정한 최고 점수가 반환되어야 함");
        
        panel.setHighScore(10000);
        assertEquals(10000, panel.getHighScore(), "최고 점수가 업데이트되어야 함");
    }
    
    @Test
    @DisplayName("음수 최고 점수 설정")
    void testNegativeHighScore() {
        panel.setHighScore(-500);
        assertEquals(-500, panel.getHighScore(), "음수 최고 점수도 설정 가능해야 함");
    }
    
    @Test
    @DisplayName("매우 큰 최고 점수 설정")
    void testLargeHighScore() {
        panel.setHighScore(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, panel.getHighScore(), "최대값 최고 점수 설정 가능해야 함");
    }
    
    @Test
    @DisplayName("0으로 최고 점수 리셋")
    void testResetHighScore() {
        panel.setHighScore(9999);
        panel.setHighScore(0);
        assertEquals(0, panel.getHighScore(), "최고 점수가 0으로 리셋되어야 함");
    }
    
    @Test
    @DisplayName("셀 크기 설정")
    void testSetCellSize() {
        assertDoesNotThrow(() -> panel.setCellSize(40), "셀 크기 설정은 예외를 발생시키지 않아야 함");
        assertDoesNotThrow(() -> panel.setCellSize(15), "작은 셀 크기도 설정 가능해야 함");
    }
    
    @Test
    @DisplayName("폰트 크기 설정")
    void testSetFontSize() {
        assertDoesNotThrow(() -> panel.setFontSize(28), "폰트 크기 설정은 예외를 발생시키지 않아야 함");
        assertDoesNotThrow(() -> panel.setFontSize(16), "작은 폰트 크기도 설정 가능해야 함");
    }
}
