package game.core;

import blocks.Block;
import game.GameView;

/**
 * 블록 생성 로직을 담당하는 클래스
 * 
 * 책임:
 * - 새로운 블록 생성
 * - 아이템 블록 생성 판정
 * - Next 블록 관리
 */
public class BlockSpawner {
    
    private Block currentBlock;
    private Block nextBlock;
    
    private final boolean itemMode;
    private final GameView view;
    
    // 아이템 생성 관련
    private int lineClearCount = 0;
    private int itemGenerateCount = 0;
    private int blocksSpawned = 0;
    private boolean itemPending = false;
    private final int divisor = 3;  // 라인 3개마다 아이템 1개
    
    /**
     * BlockSpawner 생성자
     * 
     * @param itemMode 아이템 모드 활성화 여부
     * @param view 뷰 (Next 블록 표시용)
     */
    public BlockSpawner(boolean itemMode, GameView view) {
        this.itemMode = itemMode;
        this.view = view;
        
        // 초기 블록 생성
        this.nextBlock = Block.spawn();
        this.currentBlock = null;
    }
    
    /**
     * 새로운 블록 생성
     * GameModel.spawnNewBlock()의 로직을 그대로 가져옴
     * 
     * @return 생성된 블록 정보를 담은 새로운 GameState
     */
    public SpawnResult spawnNewBlock(GameState currentState) {
        if (nextBlock == null) {
            nextBlock = Block.spawn();
        }
        
        currentBlock = nextBlock;  // nextBlock을 currentBlock으로
        
        // 아이템 모드 처리
        if (itemMode && this.itemPending) {
            // 애니메이션 지연으로 인한 itemPending 플래그 확인
            nextBlock = Block.spawn();
            nextBlock = Block.spawnItem(nextBlock);
            itemGenerateCount++;
            this.itemPending = false;
        } else if (itemMode && lineClearCount / divisor > itemGenerateCount) {
            // 라인 카운트 기준으로 아이템 생성
            nextBlock = Block.spawn();
            nextBlock = Block.spawnItem(nextBlock);
            itemGenerateCount++;
        } else {
            // 일반 블록 생성
            nextBlock = Block.spawn();
        }
        
        blocksSpawned++;
        
        // GameView 업데이트
        if (view != null) {
            view.setNextBlock(nextBlock);
        }
        
        // 새로운 상태 빌드 (새로운 Builder 생성)
        GameState newState = new GameState.Builder(
            currentState.getBoardArray(),
            currentState.getColorBoard(),
            currentBlock,  // 새로운 currentBlock
            nextBlock,     // 새로운 nextBlock
            currentState.isItemMode()
        )
            .score(currentState.getScore())
            .totalLinesCleared(currentState.getTotalLinesCleared())
            .currentLevel(currentState.getCurrentLevel())
            .lineClearCount(currentState.getLineClearCount())
            .itemGenerateCount(itemGenerateCount)
            .blocksSpawned(blocksSpawned)
            .lastLineClearScore(currentState.getLastLineClearScore())
            .build();
        
        // 속도 레벨 계산
        int speedLevel = calculateSpeedLevel(newState);
        
        return new SpawnResult(newState, speedLevel);
    }
    
    /**
     * 라인 클리어 카운트 업데이트
     */
    public void addLineClearCount(int lines) {
        this.lineClearCount += lines;
        
        // 아이템 생성 조건 체크
        if (itemMode && lineClearCount / divisor > itemGenerateCount) {
            this.itemPending = true;
        }
    }
    
    /**
     * 속도 레벨 계산
     * GameModel.getCurrentSpeedLevel()의 로직
     */
    private int calculateSpeedLevel(GameState state) {
        // 블록 30개마다 1레벨
        int blockSpeedLevel = state.getBlocksSpawned() / 30;
        
        // 라인 5줄마다 1레벨
        int lineSpeedLevel = state.getTotalLinesCleared() / 5;
        
        // 둘 중 높은 것 사용 (최대 레벨 6)
        return Math.min(Math.max(blockSpeedLevel, lineSpeedLevel), 6);
    }
    
    /**
     * 블록 리셋 (게임 재시작)
     */
    public void reset() {
        this.nextBlock = Block.spawn();
        this.currentBlock = this.nextBlock;
        this.nextBlock = Block.spawn();
        this.lineClearCount = 0;
        this.itemGenerateCount = 0;
        this.blocksSpawned = 0;
        this.itemPending = false;
        
        if (view != null) {
            view.setNextBlock(nextBlock);
        }
    }
    
    // Getters
    public Block getCurrentBlock() {
        return currentBlock;
    }
    
    public Block getNextBlock() {
        return nextBlock;
    }
    
    public int getBlocksSpawned() {
        return blocksSpawned;
    }
    
    public int getItemGenerateCount() {
        return itemGenerateCount;
    }
    
    public int getLineClearCount() {
        return lineClearCount;
    }
    
    /**
     * 블록 생성 결과를 담는 클래스
     */
    public static class SpawnResult {
        public final GameState newState;
        public final int speedLevel;
        
        public SpawnResult(GameState newState, int speedLevel) {
            this.newState = newState;
            this.speedLevel = speedLevel;
        }
    }
}
