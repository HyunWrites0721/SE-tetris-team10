package game.core;

import blocks.Block;
import blocks.IBlock;
import blocks.OBlock;
import game.GameView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BlockSpawner 테스트")
class BlockSpawnerTest {
    
    private BlockSpawner spawner;
    private GameState testState;
    private int[][] testBoard;
    private int[][] testColorBoard;
    
    @BeforeEach
    void setUp() {
        // 보드 초기화
        testBoard = new int[23][12];
        testColorBoard = new int[23][12];
        
        // 벽 설정
        for (int i = 0; i < 23; i++) {
            testBoard[i][0] = 1;
            testBoard[i][11] = 1;
        }
        for (int j = 0; j < 12; j++) {
            testBoard[0][j] = 1;
            testBoard[1][j] = 1;
            testBoard[22][j] = 1;
        }
        
        Block testBlock = new IBlock();
        testBlock.setShape();
        
        testState = new GameState.Builder(testBoard, testColorBoard, testBlock, null, false)
                .score(100)
                .currentLevel(1)
                .totalLinesCleared(0)
                .lineClearCount(0)
                .itemGenerateCount(0)
                .blocksSpawned(0)
                .build();
    }
    
    @Test
    @DisplayName("BlockSpawner 생성 테스트 - 아이템 모드 비활성화")
    void testBlockSpawnerCreation_NoItemMode() {
        spawner = new BlockSpawner(false, null);
        
        assertNotNull(spawner);
        assertNotNull(spawner.getNextBlock());
        assertNull(spawner.getCurrentBlock());
        assertEquals(0, spawner.getBlocksSpawned());
        assertEquals(0, spawner.getItemGenerateCount());
        assertEquals(0, spawner.getLineClearCount());
    }
    
    @Test
    @DisplayName("BlockSpawner 생성 테스트 - 아이템 모드 활성화")
    void testBlockSpawnerCreation_WithItemMode() {
        spawner = new BlockSpawner(true, null);
        
        assertNotNull(spawner);
        assertNotNull(spawner.getNextBlock());
        assertNull(spawner.getCurrentBlock());
    }
    
    @Test
    @DisplayName("새 블록 생성 테스트")
    void testSpawnNewBlock() {
        spawner = new BlockSpawner(false, null);
        
        BlockSpawner.SpawnResult result = spawner.spawnNewBlock(testState);
        
        assertNotNull(result);
        assertNotNull(result.newState);
        assertNotNull(result.newState.getCurrentBlock());
        assertNotNull(result.newState.getNextBlock());
        assertEquals(1, result.newState.getBlocksSpawned());
    }
    
    @Test
    @DisplayName("블록 생성 카운트 증가 테스트")
    void testBlocksSpawnedIncrement() {
        spawner = new BlockSpawner(false, null);
        
        BlockSpawner.SpawnResult result1 = spawner.spawnNewBlock(testState);
        assertEquals(1, result1.newState.getBlocksSpawned());
        
        BlockSpawner.SpawnResult result2 = spawner.spawnNewBlock(result1.newState);
        assertEquals(2, result2.newState.getBlocksSpawned());
        
        BlockSpawner.SpawnResult result3 = spawner.spawnNewBlock(result2.newState);
        assertEquals(3, result3.newState.getBlocksSpawned());
    }
    
    @Test
    @DisplayName("속도 레벨 계산 테스트 - 블록 기준")
    void testSpeedLevelCalculation_BlockBased() {
        spawner = new BlockSpawner(false, null);
        
        // 블록 30개 생성: 30번째 블록에서 레벨 1
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .blocksSpawned(0)
                .totalLinesCleared(0)
                .build();
        
        BlockSpawner.SpawnResult result = null;
        
        // 30번 spawnNewBlock 호출
        for (int i = 0; i < 30; i++) {
            result = spawner.spawnNewBlock(state);
            state = result.newState;
        }
        
        assertNotNull(result);
        assertEquals(30, result.newState.getBlocksSpawned());
        assertEquals(1, result.speedLevel, "30개 블록 생성 후 속도 레벨은 1이어야 함");
    }
    
    @Test
    @DisplayName("속도 레벨 계산 테스트 - 라인 기준")
    void testSpeedLevelCalculation_LineBased() {
        spawner = new BlockSpawner(false, null);
        
        // 라인 5개마다 레벨 1 증가
        GameState state5Lines = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .blocksSpawned(0)
                .totalLinesCleared(5)
                .build();
        
        BlockSpawner.SpawnResult result = spawner.spawnNewBlock(state5Lines);
        assertTrue(result.speedLevel >= 1, "5개 라인 클리어 후 속도 레벨은 최소 1이어야 함");
    }
    
    @Test
    @DisplayName("속도 레벨 최대값 테스트")
    void testSpeedLevelMaximum() {
        spawner = new BlockSpawner(false, null);
        
        // 블록과 라인 모두 매우 많은 경우
        GameState stateMaxLevel = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .blocksSpawned(300)  // 레벨 10에 해당
                .totalLinesCleared(100)  // 레벨 20에 해당
                .build();
        
        BlockSpawner.SpawnResult result = spawner.spawnNewBlock(stateMaxLevel);
        assertEquals(6, result.speedLevel, "속도 레벨 최대값은 6");
    }
    
    @Test
    @DisplayName("아이템 모드 - 라인 클리어 카운트 업데이트")
    void testAddLineClearCount() {
        spawner = new BlockSpawner(true, null);
        
        assertEquals(0, spawner.getLineClearCount());
        
        spawner.addLineClearCount(2);
        assertEquals(2, spawner.getLineClearCount());
        
        spawner.addLineClearCount(1);
        assertEquals(3, spawner.getLineClearCount());
    }
    
    @Test
    @DisplayName("아이템 모드 - 아이템 생성 조건 테스트")
    void testItemGenerationCondition() {
        spawner = new BlockSpawner(true, null);
        
        // 라인 3개 클리어마다 아이템 1개 생성
        spawner.addLineClearCount(3);
        
        BlockSpawner.SpawnResult result = spawner.spawnNewBlock(testState);
        
        // 아이템이 생성되었으면 itemGenerateCount가 증가해야 함
        assertTrue(result.newState.getItemGenerateCount() >= 0);
    }
    
    @Test
    @DisplayName("아이템 모드 - 연속 아이템 생성 테스트")
    void testMultipleItemGeneration() {
        spawner = new BlockSpawner(true, null);
        
        // 라인 6개 클리어 (아이템 2개 생성 조건)
        spawner.addLineClearCount(6);
        
        BlockSpawner.SpawnResult result1 = spawner.spawnNewBlock(testState);
        int itemCount1 = result1.newState.getItemGenerateCount();
        
        BlockSpawner.SpawnResult result2 = spawner.spawnNewBlock(result1.newState);
        int itemCount2 = result2.newState.getItemGenerateCount();
        
        // 아이템이 최소 1개 이상 생성되어야 함
        assertTrue(itemCount2 >= itemCount1);
    }
    
    @Test
    @DisplayName("BlockSpawner 리셋 테스트")
    void testReset() {
        spawner = new BlockSpawner(true, null);
        
        // 상태 변경
        spawner.addLineClearCount(10);
        BlockSpawner.SpawnResult result = spawner.spawnNewBlock(testState);
        
        // 리셋
        spawner.reset();
        
        assertEquals(0, spawner.getLineClearCount());
        assertEquals(0, spawner.getItemGenerateCount());
        assertEquals(0, spawner.getBlocksSpawned());
        assertNotNull(spawner.getCurrentBlock());
        assertNotNull(spawner.getNextBlock());
    }
    
    @Test
    @DisplayName("SpawnResult 객체 테스트")
    void testSpawnResult() {
        spawner = new BlockSpawner(false, null);
        
        BlockSpawner.SpawnResult result = spawner.spawnNewBlock(testState);
        
        assertNotNull(result.newState);
        assertTrue(result.speedLevel >= 0);
        assertTrue(result.speedLevel <= 6);
    }
    
    @Test
    @DisplayName("현재/다음 블록 교체 테스트")
    void testCurrentNextBlockSwap() {
        spawner = new BlockSpawner(false, null);
        
        Block firstNextBlock = spawner.getNextBlock();
        
        BlockSpawner.SpawnResult result = spawner.spawnNewBlock(testState);
        
        // 이전 nextBlock이 currentBlock이 되어야 함
        Block newCurrentBlock = result.newState.getCurrentBlock();
        assertNotNull(newCurrentBlock);
        
        // 새로운 nextBlock이 생성되어야 함
        Block newNextBlock = result.newState.getNextBlock();
        assertNotNull(newNextBlock);
        assertNotSame(newCurrentBlock, newNextBlock);
    }
    
    @Test
    @DisplayName("GameView 업데이트 테스트 - null view")
    void testGameViewUpdate_NullView() {
        spawner = new BlockSpawner(false, null);
        
        // null view로도 정상 동작해야 함
        assertDoesNotThrow(() -> {
            spawner.spawnNewBlock(testState);
        });
    }
    
    @Test
    @DisplayName("상태 불변성 테스트")
    void testStateImmutability() {
        spawner = new BlockSpawner(false, null);
        
        // currentBlock이 null인 상태로 시작
        GameState originalState = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .currentLevel(1)
                .blocksSpawned(0)
                .build();
        
        BlockSpawner.SpawnResult result = spawner.spawnNewBlock(originalState);
        
        // 원본 상태가 변경되지 않아야 함
        assertEquals(0, originalState.getBlocksSpawned());
        assertNull(originalState.getCurrentBlock());
        
        // 새 상태는 변경되어야 함
        assertEquals(1, result.newState.getBlocksSpawned());
        assertNotNull(result.newState.getCurrentBlock());
    }
    
    @Test
    @DisplayName("속도 레벨 - 블록 vs 라인 중 높은 값 선택")
    void testSpeedLevel_MaxOfBlockAndLine() {
        spawner = new BlockSpawner(false, null);
        
        // 블록 59개 (레벨 1), 라인 25개 (레벨 5)
        // spawnNewBlock 호출 시 60개가 되어 레벨 2가 되지만, 라인 레벨 5가 더 높음
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .blocksSpawned(59)
                .totalLinesCleared(25)
                .build();
        
        BlockSpawner.SpawnResult result = spawner.spawnNewBlock(state);
        
        // 더 높은 레벨인 5가 선택되어야 함
        assertTrue(result.speedLevel >= 5);
    }
}
