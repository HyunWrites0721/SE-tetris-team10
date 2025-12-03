package p2p;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import blocks.*;

/**
 * RemoteGamePanel 통합 테스트
 * Mock 없이 실제 객체로 테스트
 */
class RemoteGamePanelIntegrationTest {

    private RemoteGamePanel remotePanel;
    
    @BeforeEach
    void setUp() {
        remotePanel = new RemoteGamePanel();
    }
    
    @Test
    void testConstructor() {
        assertNotNull(remotePanel);
    }
    
    @Test
    void testSpawnBlock_IBlock() {
        IBlock block = new IBlock();
        block.setShape();
        block.setPosition(4, 0);
        
        assertDoesNotThrow(() -> remotePanel.spawnBlock(block));
    }
    
    @Test
    void testSpawnBlock_OBlock() {
        OBlock block = new OBlock();
        block.setShape();
        block.setPosition(4, 0);
        
        assertDoesNotThrow(() -> remotePanel.spawnBlock(block));
    }
    
    @Test
    void testSpawnBlock_TBlock() {
        TBlock block = new TBlock();
        block.setShape();
        block.setPosition(4, 0);
        
        assertDoesNotThrow(() -> remotePanel.spawnBlock(block));
    }
    
    @Test
    void testSpawnBlock_LBlock() {
        LBlock block = new LBlock();
        block.setShape();
        block.setPosition(4, 0);
        
        assertDoesNotThrow(() -> remotePanel.spawnBlock(block));
    }
    
    @Test
    void testSpawnBlock_JBlock() {
        JBlock block = new JBlock();
        block.setShape();
        block.setPosition(4, 0);
        
        assertDoesNotThrow(() -> remotePanel.spawnBlock(block));
    }
    
    @Test
    void testSpawnBlock_SBlock() {
        SBlock block = new SBlock();
        block.setShape();
        block.setPosition(4, 0);
        
        assertDoesNotThrow(() -> remotePanel.spawnBlock(block));
    }
    
    @Test
    void testSpawnBlock_ZBlock() {
        ZBlock block = new ZBlock();
        block.setShape();
        block.setPosition(4, 0);
        
        assertDoesNotThrow(() -> remotePanel.spawnBlock(block));
    }
    
    @Test
    void testMoveBlock_WithoutSpawn() {
        // 블록 없이 이동 시도
        assertDoesNotThrow(() -> remotePanel.moveBlock(5, 10));
    }
    
    @Test
    void testMoveBlock_AfterSpawn() {
        IBlock block = new IBlock();
        block.setShape();
        remotePanel.spawnBlock(block);
        
        assertDoesNotThrow(() -> remotePanel.moveBlock(5, 10));
    }
    
    @Test
    void testMoveBlock_MultiplePositions() {
        TBlock block = new TBlock();
        block.setShape();
        remotePanel.spawnBlock(block);
        
        assertDoesNotThrow(() -> {
            remotePanel.moveBlock(3, 1);
            remotePanel.moveBlock(4, 2);
            remotePanel.moveBlock(5, 3);
        });
    }
    
    @Test
    void testRotateBlock_WithoutSpawn() {
        assertDoesNotThrow(() -> remotePanel.rotateBlock());
    }
    
    @Test
    void testRotateBlock_AfterSpawn() {
        TBlock block = new TBlock();
        block.setShape();
        remotePanel.spawnBlock(block);
        
        assertDoesNotThrow(() -> remotePanel.rotateBlock());
    }
    
    @Test
    void testRotateBlock_MultipleTimes() {
        IBlock block = new IBlock();
        block.setShape();
        remotePanel.spawnBlock(block);
        
        assertDoesNotThrow(() -> {
            remotePanel.rotateBlock();
            remotePanel.rotateBlock();
            remotePanel.rotateBlock();
            remotePanel.rotateBlock();
        });
    }
    
    @Test
    void testPlaceBlock_WithoutSpawn() {
        assertDoesNotThrow(() -> remotePanel.placeBlock());
    }
    
    @Test
    void testPlaceBlock_AfterSpawn() {
        OBlock block = new OBlock();
        block.setShape();
        block.setPosition(4, 18);
        remotePanel.spawnBlock(block);
        
        assertDoesNotThrow(() -> remotePanel.placeBlock());
    }
    
    @Test
    void testPlaceBlock_ThenSpawnNew() {
        IBlock block1 = new IBlock();
        block1.setShape();
        block1.setPosition(4, 18);
        remotePanel.spawnBlock(block1);
        remotePanel.placeBlock();
        
        TBlock block2 = new TBlock();
        block2.setShape();
        assertDoesNotThrow(() -> remotePanel.spawnBlock(block2));
    }
    
    @Test
    void testUpdateScore() {
        assertDoesNotThrow(() -> remotePanel.updateScore(1000));
        assertDoesNotThrow(() -> remotePanel.updateScore(0));
        assertDoesNotThrow(() -> remotePanel.updateScore(999999));
    }
    
    @Test
    void testClearLines_SingleLine() {
        int[] lines = {20};
        assertDoesNotThrow(() -> remotePanel.clearLines(lines));
    }
    
    @Test
    void testClearLines_MultipleLines() {
        int[] lines = {18, 19, 20};
        assertDoesNotThrow(() -> remotePanel.clearLines(lines));
    }
    
    @Test
    void testClearLines_EmptyArray() {
        int[] lines = {};
        assertDoesNotThrow(() -> remotePanel.clearLines(lines));
    }
    
    @Test
    void testClearLines_NullArray() {
        assertDoesNotThrow(() -> remotePanel.clearLines(null));
    }
    
    @Test
    void testApplyItemEffect_AllClear() {
        assertDoesNotThrow(() -> remotePanel.applyItemEffect("ALL_CLEAR"));
    }
    
    @Test
    void testApplyItemEffect_OneLine() {
        assertDoesNotThrow(() -> remotePanel.applyItemEffect("ONE_LINE_CLEAR"));
    }
    
    @Test
    void testApplyItemEffect_BoxClear() {
        assertDoesNotThrow(() -> remotePanel.applyItemEffect("BOX_CLEAR"));
    }
    
    @Test
    void testApplyItemEffect_WeightBlock() {
        assertDoesNotThrow(() -> remotePanel.applyItemEffect("WEIGHT_BLOCK"));
    }
    
    @Test
    void testApplyItemEffect_ClearLine() {
        assertDoesNotThrow(() -> remotePanel.applyItemEffect("CLEAR_LINE"));
    }
    
    @Test
    void testApplyItemEffect_Unknown() {
        assertDoesNotThrow(() -> remotePanel.applyItemEffect("UNKNOWN_ITEM"));
    }
    
    @Test
    void testApplyAttackVisual_WithPattern() {
        int[][] pattern = {{1,0,1,1,0,1,1,0,1,1}};
        assertDoesNotThrow(() -> remotePanel.applyAttackVisual(2, pattern, 3));
    }
    
    @Test
    void testApplyAttackVisual_WithNullPattern() {
        assertDoesNotThrow(() -> remotePanel.applyAttackVisual(3, null, 0));
    }
    
    @Test
    void testApplyAttackVisual_ZeroLines() {
        int[][] pattern = {{1,0,1}};
        assertDoesNotThrow(() -> remotePanel.applyAttackVisual(0, pattern, 0));
    }
    
    @Test
    void testCompleteGameFlow() {
        // 1. 블록 스폰
        IBlock block = new IBlock();
        block.setShape();
        block.setPosition(4, 0);
        remotePanel.spawnBlock(block);
        
        // 2. 이동
        remotePanel.moveBlock(4, 5);
        remotePanel.moveBlock(4, 10);
        
        // 3. 회전
        remotePanel.rotateBlock();
        
        // 4. 배치
        remotePanel.placeBlock();
        
        // 5. 점수 업데이트
        remotePanel.updateScore(100);
        
        assertDoesNotThrow(() -> {});
    }
    
    @Test
    void testMultipleBlockPlacements() {
        for (int i = 0; i < 3; i++) {
            OBlock block = new OBlock();
            block.setShape();
            block.setPosition(i * 2, 18);
            remotePanel.spawnBlock(block);
            remotePanel.placeBlock();
        }
        
        assertDoesNotThrow(() -> {});
    }
    
    @Test
    void testSetRemoteComponents_WithNull() {
        assertDoesNotThrow(() -> remotePanel.setRemoteComponents(null, null));
    }
}
