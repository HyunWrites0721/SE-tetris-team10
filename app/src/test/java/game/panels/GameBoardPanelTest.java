package game.panels;

import blocks.Block;
import blocks.IBlock;
import blocks.JBlock;
import game.core.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameBoardPanel 테스트")
class GameBoardPanelTest {
    
    private GameBoardPanel panel;
    
    @BeforeEach
    void setUp() {
        panel = new GameBoardPanel();
    }
    
    @Test
    @DisplayName("초기 상태")
    void testInitialState() {
        assertNotNull(panel, "패널이 생성되어야 함");
        assertEquals(30, panel.getCellSize(), "초기 셀 크기는 30이어야 함");
    }
    
    @Test
    @DisplayName("셀 크기 설정 및 조회")
    void testSetAndGetCellSize() {
        panel.setCellSize(40);
        assertEquals(40, panel.getCellSize(), "설정한 셀 크기가 반환되어야 함");
        
        panel.setCellSize(20);
        assertEquals(20, panel.getCellSize(), "셀 크기가 업데이트되어야 함");
    }
    
    @Test
    @DisplayName("GameState 렌더링")
    void testRenderWithGameState() {
        int[][] board = new int[22][12];
        int[][] colorBoard = new int[22][12];
        GameState state = new GameState.Builder(board, colorBoard, null, null, false).build();
        assertDoesNotThrow(() -> panel.render(state), "GameState 렌더링은 예외를 발생시키지 않아야 함");
    }
    
    @Test
    @DisplayName("null GameState 렌더링")
    void testRenderWithNullState() {
        assertDoesNotThrow(() -> panel.render(null), "null GameState 렌더링은 예외를 발생시키지 않아야 함");
    }
    
    @Test
    @DisplayName("원격 블록 설정")
    void testSetRemoteBlock() {
        Block block = new IBlock();
        assertDoesNotThrow(() -> panel.setRemoteBlock(block), "원격 블록 설정은 예외를 발생시키지 않아야 함");
    }
    
    @Test
    @DisplayName("null 원격 블록 설정")
    void testSetNullRemoteBlock() {
        assertDoesNotThrow(() -> panel.setRemoteBlock(null), "null 원격 블록 설정은 예외를 발생시키지 않아야 함");
    }
    
    @Test
    @DisplayName("원격 블록 여러 번 설정")
    void testSetRemoteBlockMultipleTimes() {
        Block block1 = new IBlock();
        Block block2 = new JBlock();
        
        panel.setRemoteBlock(block1);
        panel.setRemoteBlock(block2);
        panel.setRemoteBlock(null);
        
        assertDoesNotThrow(() -> panel.repaint(), "여러 번 설정 후 repaint 가능해야 함");
    }
    
    @Test
    @DisplayName("GameState 렌더링 후 원격 블록 설정")
    void testRenderThenSetRemoteBlock() {
        int[][] board = new int[22][12];
        int[][] colorBoard = new int[22][12];
        GameState state = new GameState.Builder(board, colorBoard, null, null, false).build();
        panel.render(state);
        
        Block block = new IBlock();
        panel.setRemoteBlock(block);
        
        assertDoesNotThrow(() -> panel.repaint(), "GameState 렌더링 후 원격 블록 설정 가능해야 함");
    }
    
    @Test
    @DisplayName("셀 크기 변경 후 렌더링")
    void testResizeThenRender() {
        panel.setCellSize(25);
        int[][] board = new int[22][12];
        int[][] colorBoard = new int[22][12];
        GameState state = new GameState.Builder(board, colorBoard, null, null, false).build();
        
        assertDoesNotThrow(() -> panel.render(state), "크기 변경 후 렌더링이 가능해야 함");
    }
    
    @Test
    @DisplayName("작은 셀 크기 설정")
    void testSmallCellSize() {
        panel.setCellSize(10);
        assertEquals(10, panel.getCellSize(), "작은 셀 크기도 설정 가능해야 함");
    }
    
    @Test
    @DisplayName("큰 셀 크기 설정")
    void testLargeCellSize() {
        panel.setCellSize(100);
        assertEquals(100, panel.getCellSize(), "큰 셀 크기도 설정 가능해야 함");
    }
    
    @Test
    @DisplayName("연속 렌더링")
    void testContinuousRendering() {
        int[][] board = new int[22][12];
        int[][] colorBoard = new int[22][12];
        GameState state = new GameState.Builder(board, colorBoard, null, null, false).build();
        
        for (int i = 0; i < 10; i++) {
            assertDoesNotThrow(() -> panel.render(state), "연속 렌더링이 가능해야 함");
        }
    }
}
