package game.core;

import blocks.Block;
import blocks.item.WeightBlock;
import game.events.EventBus;
import game.events.TickEvent;
import game.events.EventListener;

/**
 * 게임의 순수 로직만 담당하는 엔진 클래스
 * UI 의존성이 전혀 없으며, GameState를 입력받아 새로운 GameState를 반환
 * EventBus를 통해 게임 이벤트를 처리
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
    private final EventBus eventBus;
    
    public GameEngine(int difficulty) {
        this(difficulty, null);
    }
    
    public GameEngine(int difficulty, EventBus eventBus) {
        this.difficulty = difficulty;
        this.eventBus = eventBus;
        
        // 이벤트 리스너 등록
        if (eventBus != null) {
            setupEventListeners();
        }
    }
    
    /**
     * 이벤트 리스너 설정
     */
    private void setupEventListeners() {
        // 틱 이벤트 리스너 등록 - 게임 엔진이 틱을 받아서 로직 처리
        eventBus.subscribe(TickEvent.class, new EventListener<TickEvent>() {
            @Override
            public void onEvent(TickEvent event) {
                // 게임 엔진에서 틱 이벤트 처리 로직
                handleTickEvent(event);
            }
        }, 1); // GameTimer 다음 우선순위로 처리
    }
    
    /**
     * 틱 이벤트 처리
     * @param event 틱 이벤트
     */
    protected void handleTickEvent(TickEvent event) {
        // 게임 엔진에서 틱별로 처리해야 할 로직이 있다면 여기에 구현
        // 예: 레벨업 체크, 통계 업데이트 등
        System.out.println("GameEngine received tick: Level=" + event.getCurrentLevel() + 
                          ", Speed=" + event.getSpeedLevel() + 
                          ", Delta=" + event.getDeltaTime() + "ms");
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
            int[][] board = state.getBoardArray();
            int origX = currentBlock.getX();
            int origY = currentBlock.getY();

            // compute rotated shape without applying it
            int[][] rotated = currentBlock.Rotateshape();

            // try no-kick first, then small horizontal kicks (simple wall-kick)
            int[] kicks = new int[] {0, -1, 1, -2, 2};
            for (int kick : kicks) {
                int tryX = origX + kick;
                if (!collides(rotated, board, tryX, origY)) {
                    // apply rotation and adjust position if kicked
                    currentBlock.setPosition(tryX, origY);
                    currentBlock.getRotatedShape();
                    break;
                }
            }
        }
        
        return state;
    }

    // Check if the given shape placed at (x,y) would collide with board or walls
    private boolean collides(int[][] shape, int[][] board, int x, int y) {
        if (shape == null) return true;
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != 0) {
                    int br = y + row;
                    int bc = x + col;
                    // out of bounds
                    if (br < 0 || br >= ROWS || bc < 0 || bc >= COLS) return true;
                    // collision with existing cell (walls represented by non-zero in board)
                    if (board[br][bc] != 0) return true;
                }
            }
        }
        return false;
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
    
    /**
     * 라인 클리어 (애니메이션 없이 즉시 처리)
     * 나중에 AnimationManager로 분리 예정
     * 
     * @return 클리어된 라인 수
     */
    public int performLineClear(int[][] board, int[][] colorBoard) {
        int linesCleared = 0;
        
        for (int row = INNER_BOTTOM; row >= INNER_TOP; row--) {
            boolean isFull = true;
            for (int col = INNER_LEFT; col <= INNER_RIGHT; col++) {
                if (board[row][col] == 0) {
                    isFull = false;
                    break;
                }
            }
            
            if (isFull) {
                // 라인 제거 및 위 라인들 아래로 이동
                clearLine(board, colorBoard, row);
                linesCleared++;
                row++; // 같은 행 다시 체크 (위에서 내려온 라인)
            }
        }
        
        return linesCleared;
    }
    
    /**
     * 게임 오버 체크
     */
    public boolean checkGameOver(int[][] board) {
        // 상단 2줄(row 2-3)의 중앙 부분(col 3-7)에 블록이 있으면 게임 오버
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
     * 블록 고정 (블록을 보드에 배치)
     * 
     * @return 특수 블록 타입 (0: 일반, 2: AllClear, 3: BoxClear, 4: OneLineClear)
     */
    public int placeBlock(Block block, int[][] board, int[][] colorBoard) {
        if (block == null) return 0;
        
        int[][] shape = block.getShape();
        int x = block.getX();
        int y = block.getY();
        java.awt.Color color = block.getColor();
        int rgb = (color != null ? color.getRGB() : new java.awt.Color(100, 100, 100).getRGB());
        
        int specialType = 0;  // 특수 블록 타입
        
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != 0) {
                    int br = y + row;
                    int bc = x + col;
                    board[br][bc] = shape[row][col];
                    colorBoard[br][bc] = rgb;
                    
                    int value = shape[row][col];
                    if (value == 2 || value == 3 || value == 4) {
                        specialType = value;  // 특수 블록 감지
                    }
                }
            }
        }
        
        return specialType;
    }
    
    /**
     * 레벨 계산
     */
    public int calculateLevel(int totalLinesCleared) {
        // 2줄마다 1레벨, 최대 10레벨
        return Math.min((totalLinesCleared / 2) + 1, 10);
    }
}

