package game.panels;

import blocks.IBlock;
import blocks.JBlock;
import blocks.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NextBlockPanel 테스트")
class NextBlockPanelTest {
    
    private NextBlockPanel panel;
    
    @BeforeEach
    void setUp() {
        panel = new NextBlockPanel();
    }
    
    @Test
    @DisplayName("초기 상태 - 다음 블록 없음")
    void testInitialState() {
        assertNotNull(panel, "패널이 생성되어야 함");
    }
    
    @Test
    @DisplayName("다음 블록 설정")
    void testSetNextBlock() {
        Block block = new IBlock();
        assertDoesNotThrow(() -> panel.setNextBlock(block), "블록 설정은 예외를 발생시키지 않아야 함");
    }
    
    @Test
    @DisplayName("null 블록 설정")
    void testSetNullBlock() {
        assertDoesNotThrow(() -> panel.setNextBlock(null), "null 블록 설정은 예외를 발생시키지 않아야 함");
    }
    
    @Test
    @DisplayName("여러 블록 순차 설정")
    void testSetMultipleBlocks() {
        Block block1 = new IBlock();
        Block block2 = new JBlock();
        
        panel.setNextBlock(block1);
        panel.setNextBlock(block2);
        
        assertDoesNotThrow(() -> panel.repaint(), "여러 블록 설정 후 repaint 가능해야 함");
    }
    
    @Test
    @DisplayName("셀 크기 설정")
    void testSetCellSize() {
        assertDoesNotThrow(() -> panel.setCellSize(35), "셀 크기 설정은 예외를 발생시키지 않아야 함");
        assertDoesNotThrow(() -> panel.setCellSize(20), "작은 셀 크기도 설정 가능해야 함");
    }
    
    @Test
    @DisplayName("폰트 크기 설정")
    void testSetFontSize() {
        assertDoesNotThrow(() -> panel.setFontSize(30), "폰트 크기 설정은 예외를 발생시키지 않아야 함");
        assertDoesNotThrow(() -> panel.setFontSize(18), "작은 폰트 크기도 설정 가능해야 함");
    }
    
    @Test
    @DisplayName("블록 설정 후 크기 변경")
    void testSetBlockThenResize() {
        Block block = new IBlock();
        panel.setNextBlock(block);
        panel.setCellSize(25);
        
        assertDoesNotThrow(() -> panel.repaint(), "블록 설정 후 크기 변경이 가능해야 함");
    }
}
