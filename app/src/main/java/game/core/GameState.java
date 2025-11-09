package game.core;

import blocks.Block;
import java.io.Serializable;

/**
 * 게임의 모든 상태를 담는 불변 객체
 * 네트워크 전송 및 게임 리플레이를 위해 직렬화를 담당하는 클래스
 */
public class GameState implements Serializable { //Serializable이 전송을 가능하게 함
    private static final long serialVersionUID = 1L;
    
    // 보드 상태
    private final int[][] boardArray;
    private final int[][] colorBoard;
    
    // 블록 상태
    private final Block currentBlock;
    private final Block nextBlock;
    
    // 게임 진행 상태
    private final int score;
    private final int totalLinesCleared;
    private final int currentLevel;
    private final int lineClearCount;
    private final int itemGenerateCount;
    private final int blocksSpawned;
    
    // 게임 모드
    private final boolean itemMode;
    
    // 애니메이션 상태
    private final boolean isAnimating;
    
    // 마지막 라인 클리어 점수
    private final int lastLineClearScore;
    
    /**
     * GameState 생성자
     */
    private GameState(Builder builder) {
        this.boardArray = deepCopy2D(builder.boardArray);
        this.colorBoard = deepCopy2D(builder.colorBoard);
        this.currentBlock = builder.currentBlock;
        this.nextBlock = builder.nextBlock;
        this.score = builder.score;
        this.totalLinesCleared = builder.totalLinesCleared;
        this.currentLevel = builder.currentLevel;
        this.lineClearCount = builder.lineClearCount;
        this.itemGenerateCount = builder.itemGenerateCount;
        this.blocksSpawned = builder.blocksSpawned;
        this.itemMode = builder.itemMode;
        this.isAnimating = builder.isAnimating;
        this.lastLineClearScore = builder.lastLineClearScore;
    }
    
    // Getters
    public int[][] getBoardArray() {
        return deepCopy2D(boardArray);
    }
    
    public int[][] getColorBoard() {
        return deepCopy2D(colorBoard);
    }
    
    public Block getCurrentBlock() {
        return currentBlock;
    }
    
    public Block getNextBlock() {
        return nextBlock;
    }
    
    public int getScore() {
        return score;
    }
    
    public int getTotalLinesCleared() {
        return totalLinesCleared;
    }
    
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public int getLineClearCount() {
        return lineClearCount;
    }
    
    public int getItemGenerateCount() {
        return itemGenerateCount;
    }
    
    public int getBlocksSpawned() {
        return blocksSpawned;
    }
    
    public boolean isItemMode() {
        return itemMode;
    }
    
    public boolean isAnimating() {
        return isAnimating;
    }
    
    public int getLastLineClearScore() {
        return lastLineClearScore;
    }
    
    /**
     * 2차원 배열 깊은 복사 -> 다른 메모리 주소로 복사함으로써 원래 상태는 보호
     */
    private static int[][] deepCopy2D(int[][] original) {
        if (original == null) return null;
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
    
    /**
     * Builder 패턴을 사용한 GameState 생성
     */
    public static class Builder {
        // Required parameters
        private int[][] boardArray;
        private int[][] colorBoard;
        private Block currentBlock;
        private Block nextBlock;
        private boolean itemMode;
        
        // Optional parameters - initialized to default values
        private int score = 0;
        private int totalLinesCleared = 0;
        private int currentLevel = 1;
        private int lineClearCount = 0;
        private int itemGenerateCount = 0;
        private int blocksSpawned = 0;
        private boolean isAnimating = false;
        private int lastLineClearScore = 0;
        
        public Builder(int[][] boardArray, int[][] colorBoard, Block currentBlock, Block nextBlock, boolean itemMode) {
            this.boardArray = boardArray;
            this.colorBoard = colorBoard;
            this.currentBlock = currentBlock;
            this.nextBlock = nextBlock;
            this.itemMode = itemMode;
        }
        
        public Builder score(int score) {
            this.score = score;
            return this;
        }
        
        public Builder totalLinesCleared(int totalLinesCleared) {
            this.totalLinesCleared = totalLinesCleared;
            return this;
        }
        
        public Builder currentLevel(int currentLevel) {
            this.currentLevel = currentLevel;
            return this;
        }
        
        public Builder lineClearCount(int lineClearCount) {
            this.lineClearCount = lineClearCount;
            return this;
        }
        
        public Builder itemGenerateCount(int itemGenerateCount) {
            this.itemGenerateCount = itemGenerateCount;
            return this;
        }
        
        public Builder blocksSpawned(int blocksSpawned) {
            this.blocksSpawned = blocksSpawned;
            return this;
        }
        
        public Builder isAnimating(boolean isAnimating) {
            this.isAnimating = isAnimating;
            return this;
        }
        
        public Builder lastLineClearScore(int lastLineClearScore) {
            this.lastLineClearScore = lastLineClearScore;
            return this;
        }
        
        public GameState build() {
            return new GameState(this);
        }
    }
    
    /**
     * 현재 상태를 기반으로 새로운 Builder 생성
     */
    public Builder toBuilder() {
        return new Builder(this.boardArray, this.colorBoard, this.currentBlock, this.nextBlock, this.itemMode)
                .score(this.score)
                .totalLinesCleared(this.totalLinesCleared)
                .currentLevel(this.currentLevel)
                .lineClearCount(this.lineClearCount)
                .itemGenerateCount(this.itemGenerateCount)
                .blocksSpawned(this.blocksSpawned)
                .isAnimating(this.isAnimating)
                .lastLineClearScore(this.lastLineClearScore);
    }
    
    @Override
    public String toString() {
        return String.format("GameState[score=%d, level=%d, linesCleared=%d, itemMode=%b]",
                score, currentLevel, totalLinesCleared, itemMode);
    }
}
