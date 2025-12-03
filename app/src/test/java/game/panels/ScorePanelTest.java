package game.panels;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScorePanel 테스트")
class ScorePanelTest {
    
    private ScorePanel panel;
    
    @BeforeEach
    void setUp() {
        panel = new ScorePanel();
    }
    
    @Test
    @DisplayName("초기 점수는 0")
    void testInitialScore() {
        assertEquals(0, panel.getScore(), "초기 점수는 0이어야 함");
    }
    
    @Test
    @DisplayName("점수 설정 및 조회")
    void testSetAndGetScore() {
        panel.setScore(100);
        assertEquals(100, panel.getScore(), "설정한 점수가 반환되어야 함");
        
        panel.setScore(5000);
        assertEquals(5000, panel.getScore(), "점수가 업데이트되어야 함");
    }
    
    @Test
    @DisplayName("음수 점수 설정")
    void testNegativeScore() {
        panel.setScore(-100);
        assertEquals(-100, panel.getScore(), "음수 점수도 설정 가능해야 함");
    }
    
    @Test
    @DisplayName("매우 큰 점수 설정")
    void testLargeScore() {
        panel.setScore(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, panel.getScore(), "최대값 점수 설정 가능해야 함");
    }
    
    @Test
    @DisplayName("0으로 점수 리셋")
    void testResetScore() {
        panel.setScore(1000);
        panel.setScore(0);
        assertEquals(0, panel.getScore(), "점수가 0으로 리셋되어야 함");
    }
    
    @Test
    @DisplayName("셀 크기 설정")
    void testSetCellSize() {
        assertDoesNotThrow(() -> panel.setCellSize(50), "셀 크기 설정은 예외를 발생시키지 않아야 함");
        assertDoesNotThrow(() -> panel.setCellSize(10), "작은 셀 크기도 설정 가능해야 함");
    }
    
    @Test
    @DisplayName("폰트 크기 설정")
    void testSetFontSize() {
        assertDoesNotThrow(() -> panel.setFontSize(32), "폰트 크기 설정은 예외를 발생시키지 않아야 함");
        assertDoesNotThrow(() -> panel.setFontSize(12), "작은 폰트 크기도 설정 가능해야 함");
    }
}
