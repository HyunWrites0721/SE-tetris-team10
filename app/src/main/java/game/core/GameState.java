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
    
    // 라인 클리어 애니메이션
    private final boolean lineClearAnimating;
    private final boolean flashBlack;
    private final java.util.List<Integer> flashingRows;
    
    // AllClear 애니메이션
    private final boolean allClearAnimating;
    private final boolean allClearFlashBlack;
    
    // BoxClear 애니메이션
    private final boolean boxClearAnimating;
    private final boolean boxFlashBlack;
    private final java.util.List<int[]> boxFlashCenters;
    
    // Weight 애니메이션
    private final boolean weightAnimating;
    
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
        
        // 애니메이션 상태
        this.lineClearAnimating = builder.lineClearAnimating;
        this.flashBlack = builder.flashBlack;
        this.flashingRows = builder.flashingRows != null ? new java.util.ArrayList<>(builder.flashingRows) : new java.util.ArrayList<>();
        this.allClearAnimating = builder.allClearAnimating;
        this.allClearFlashBlack = builder.allClearFlashBlack;
        this.boxClearAnimating = builder.boxClearAnimating;
        this.boxFlashBlack = builder.boxFlashBlack;
        this.boxFlashCenters = builder.boxFlashCenters != null ? new java.util.ArrayList<>(builder.boxFlashCenters) : new java.util.ArrayList<>();
        this.weightAnimating = builder.weightAnimating;
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
    
    // 애니메이션 상태 getters
    public boolean isLineClearAnimating() {
        return lineClearAnimating;
    }
    
    public boolean isFlashBlack() {
        return flashBlack;
    }
    
    public java.util.List<Integer> getFlashingRows() {
        return new java.util.ArrayList<>(flashingRows);
    }
    
    public boolean isAllClearAnimating() {
        return allClearAnimating;
    }
    
    public boolean isAllClearFlashBlack() {
        return allClearFlashBlack;
    }
    
    public boolean isBoxClearAnimating() {
        return boxClearAnimating;
    }
    
    public boolean isBoxFlashBlack() {
        return boxFlashBlack;
    }
    
    public java.util.List<int[]> getBoxFlashCenters() {
        return new java.util.ArrayList<>(boxFlashCenters);
    }
    
    public boolean isWeightAnimating() {
        return weightAnimating;
    }
    
    public boolean isRowFlashing(int row) {
        return flashingRows.contains(row);
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
        
        // 애니메이션 상태 - 기본값
        private boolean lineClearAnimating = false;
        private boolean flashBlack = false;
        private java.util.List<Integer> flashingRows = new java.util.ArrayList<>();
        private boolean allClearAnimating = false;
        private boolean allClearFlashBlack = false;
        private boolean boxClearAnimating = false;
        private boolean boxFlashBlack = false;
        private java.util.List<int[]> boxFlashCenters = new java.util.ArrayList<>();
        private boolean weightAnimating = false;
        
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
        
        // 애니메이션 상태 설정 메서드들
        public Builder lineClearAnimating(boolean lineClearAnimating) {
            this.lineClearAnimating = lineClearAnimating;
            return this;
        }
        
        public Builder flashBlack(boolean flashBlack) {
            this.flashBlack = flashBlack;
            return this;
        }
        
        public Builder flashingRows(java.util.List<Integer> flashingRows) {
            this.flashingRows = flashingRows != null ? new java.util.ArrayList<>(flashingRows) : new java.util.ArrayList<>();
            return this;
        }
        
        public Builder allClearAnimating(boolean allClearAnimating) {
            this.allClearAnimating = allClearAnimating;
            return this;
        }
        
        public Builder allClearFlashBlack(boolean allClearFlashBlack) {
            this.allClearFlashBlack = allClearFlashBlack;
            return this;
        }
        
        public Builder boxClearAnimating(boolean boxClearAnimating) {
            this.boxClearAnimating = boxClearAnimating;
            return this;
        }
        
        public Builder boxFlashBlack(boolean boxFlashBlack) {
            this.boxFlashBlack = boxFlashBlack;
            return this;
        }
        
        public Builder boxFlashCenters(java.util.List<int[]> boxFlashCenters) {
            this.boxFlashCenters = boxFlashCenters != null ? new java.util.ArrayList<>(boxFlashCenters) : new java.util.ArrayList<>();
            return this;
        }
        
        public Builder weightAnimating(boolean weightAnimating) {
            this.weightAnimating = weightAnimating;
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
                .lastLineClearScore(this.lastLineClearScore)
                .lineClearAnimating(this.lineClearAnimating)
                .flashBlack(this.flashBlack)
                .flashingRows(this.flashingRows)
                .allClearAnimating(this.allClearAnimating)
                .allClearFlashBlack(this.allClearFlashBlack)
                .boxClearAnimating(this.boxClearAnimating)
                .boxFlashBlack(this.boxFlashBlack)
                .boxFlashCenters(this.boxFlashCenters)
                .weightAnimating(this.weightAnimating);
    }
    
    @Override
    public String toString() {
        return String.format("GameState[score=%d, level=%d, linesCleared=%d, itemMode=%b]",
                score, currentLevel, totalLinesCleared, itemMode);
    }
}
