package game.core;

import blocks.Block;
import blocks.item.WeightBlock;

/**
 * 게임의 순수 로직만 담당하는 엔진 클래스
 * UI 의존성이 전혀 없으며, GameState를 입력받아 새로운 GameState를 반환
 */
public class GameEngine {
    
    private static final int ROWS = 23;
    private static final int COLS = 12;
    private static final int INNER_TOP = 2;
    private static final int INNER_BOTTOM = ROWS - 2;
    private static final int INNER_LEFT = 1;
    private static final int INNER_RIGHT = COLS - 2;
    
    // 난이도별 점수 가중치 배열 (0: normal=1.0, 1: hard=1.1, 2: easy=0.9)
    private static final double[] DIFFICULTY_MULTIPLIERS = {1.0, 1.1, 0.9};
    
    private final int difficulty;
    
    public GameEngine(int difficulty) {
        this.difficulty = difficulty;
    }
    
    /**
     * 블록을 왼쪽으로 이동
     */
    public GameState moveLeft(GameState state) {
        Block currentBlock = state.getCurrentBlock();
        int[][] board = state.getBoardArray();
        
        if (currentBlock != null && currentBlock.canMoveLeft(board)) {
            currentBlock.moveLeft(board);
        }
        
        return state; // 현재는 동일한 state 반환, 추후 불변 패턴으로 변경 예정
    }
    
    /**
     * 블록을 오른쪽으로 이동
     */
    public GameState moveRight(GameState state) {
        Block currentBlock = state.getCurrentBlock();
        int[][] board = state.getBoardArray();
        
        if (currentBlock != null && currentBlock.canMoveRight(board)) {
            currentBlock.moveRight(board);
        }
        
        return state;
    }
    
    /**
     * 블록을 아래로 이동 (소프트 드롭)
     */
    public GameState moveDown(GameState state) {
        Block currentBlock = state.getCurrentBlock();
        int[][] board = state.getBoardArray();
        
        if (currentBlock != null && currentBlock.canMoveDown(board)) {
            currentBlock.moveDown(board);
        }
        
        return state;
    }
    
    /**
     * 블록 회전
     */
    public GameState rotate(GameState state) {
        Block currentBlock = state.getCurrentBlock();
        
        if (currentBlock != null) {
            currentBlock.getRotatedShape();
        }
        
        return state;
    }
    
    /**
     * 하드 드롭 거리 계산
     */
    public int calculateHardDropDistance(GameState state) {
        Block currentBlock = state.getCurrentBlock();
        int[][] board = state.getBoardArray();
        
        if (currentBlock == null) return 0;
        
        // WeightBlock은 하드드롭 거리 계산 불가
        if (currentBlock instanceof WeightBlock) {
            return 0;
        }
        
        return currentBlock.hardDrop(board);
    }
    
    /**
     * 블록이 고정될 수 있는지 확인
     */
    public boolean canPlacePiece(GameState state) {
        Block currentBlock = state.getCurrentBlock();
        int[][] board = state.getBoardArray();
        
        return currentBlock != null && !currentBlock.canMoveDown(board);
    }
    
    /**
     * 라인 클리어 가능한 줄 찾기
     */
    public java.util.List<Integer> findFullLines(int[][] board) {
        java.util.List<Integer> fullLines = new java.util.ArrayList<>();
        
        for (int row = INNER_TOP; row <= INNER_BOTTOM; row++) {
            boolean isFull = true;
            for (int col = INNER_LEFT; col <= INNER_RIGHT; col++) {
                if (board[row][col] == 0) {
                    isFull = false;
                    break;
                }
            }
            if (isFull) {
                fullLines.add(row);
            }
        }
        
        return fullLines;
    }
    
    /**
     * 라인 클리어 점수 계산
     */
    public int calculateLineClearScore(int linesCleared, int currentLevel) {
        if (linesCleared <= 0) return 0;
        
        int baseScore = 0;
        switch (linesCleared) {
            case 1: baseScore = 100; break;
            case 2: baseScore = 300; break;
            case 3: baseScore = 500; break;
            case 4: baseScore = 800; break;
            default: baseScore = 100 * linesCleared; break;
        }
        
        int levelMultiplier = currentLevel;
        double difficultyMultiplier = getDifficultyMultiplier();
        
        return (int) Math.round(baseScore * levelMultiplier * difficultyMultiplier);
    }
    
    /**
     * 게임 오버 체크
     */
    public boolean isGameOver(int[][] board) {
        // 상단 감지 영역: row 2-3, col 3-7 에 블록이 있으면 게임 오버
        for (int row = INNER_TOP; row < INNER_TOP + 2; row++) {
            for (int col = 3; col < 8; col++) {
                if (board[row][col] != 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 레벨업 체크 및 계산
     */
    public int calculateLevel(int totalLinesCleared) {
        return (totalLinesCleared / 10) + 1;
    }
    
    /**
     * 아이템 블록 생성 조건 체크
     */
    public boolean shouldSpawnItemBlock(GameState state, int divisor) {
        if (!state.isItemMode()) return false;
        
        int lineClearCount = state.getLineClearCount();
        int itemGenerateCount = state.getItemGenerateCount();
        
        return (lineClearCount / divisor) > itemGenerateCount;
    }
    
    /**
     * 유효한 위치인지 확인
     */
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }
    
    /**
     * 난이도 가중치 반환
     */
    private double getDifficultyMultiplier() {
        if (difficulty >= 0 && difficulty < DIFFICULTY_MULTIPLIERS.length) {
            return DIFFICULTY_MULTIPLIERS[difficulty];
        }
        return DIFFICULTY_MULTIPLIERS[0];
    }
    
    /**
     * 보드 초기화 (벽 설정)
     */
    public int[][] initializeBoard() {
        int[][] board = new int[ROWS][COLS];
        
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (j == 0 || j == COLS - 1 || i == ROWS - 1) {
                    board[i][j] = 10; // 벽
                } else {
                    board[i][j] = 0;
                }
            }
        }
        
        return board;
    }
    
    /**
     * 특정 행을 제거하고 위의 행들을 아래로 이동
     */
    public void clearLine(int[][] board, int[][] colorBoard, int lineNumber) {
        // 해당 줄부터 위로 한 칸씩 내림
        for (int row = lineNumber; row > INNER_TOP; row--) {
            for (int col = INNER_LEFT; col <= INNER_RIGHT; col++) {
                board[row][col] = board[row - 1][col];
                colorBoard[row][col] = colorBoard[row - 1][col];
            }
        }
        
        // 최상단 줄은 빈 공간으로
        for (int col = INNER_LEFT; col <= INNER_RIGHT; col++) {
            board[INNER_TOP][col] = 0;
            colorBoard[INNER_TOP][col] = 0;
        }
    }
    
    /**
     * 자동 낙하 점수 계산
     */
    public int calculateAutoDropScore(int speedLevel) {
        int speedMultiplier = speedLevel + 1;
        double difficultyMultiplier = getDifficultyMultiplier();
        return (int) Math.round(1 * speedMultiplier * difficultyMultiplier);
    }
}
