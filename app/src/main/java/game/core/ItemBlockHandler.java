package game.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import game.GameView;

/**
 * 특수 아이템 블록 효과를 처리하는 클래스
 * 
 * 책임:
 * - AllClear (값 2): 보드 전체 초기화
 * - BoxClear (값 3): 5x5 영역 폭발
 * - OneLineClear (값 4): 특정 행 클리어
 * - WeightBlock (값 5): 드릴 효과로 뚫고 내려가기
 */
public class ItemBlockHandler {
    
    private static final int ROWS = 24;
    private static final int COLS = 12;
    private static final int INNER_TOP = 2;
    private static final int INNER_BOTTOM = 21;
    private static final int INNER_LEFT = 1;
    private static final int INNER_RIGHT = 10;
    
    private final GameView view;
    private final AnimationManager animationManager;
    
    /**
     * ItemBlockHandler 생성자
     * 
     * @param view 게임 뷰
     * @param animationManager 애니메이션 관리자
     */
    public ItemBlockHandler(GameView view, AnimationManager animationManager) {
        this.view = view;
        this.animationManager = animationManager;
    }
    
    /**
     * 특수 블록 처리 (placeBlock 결과값에 따라)
     * 
     * @param specialType placeBlock에서 반환한 특수 타입 (0=일반, 2=AllClear, 3=BoxClear, 4=OneLineClear)
     * @param state 현재 게임 상태
     * @param onComplete 처리 완료 후 콜백 (새로운 GameState 전달)
     */
    public void handleSpecialBlock(int specialType, GameState state, java.util.function.Consumer<GameState> onComplete) {
        switch (specialType) {
            case 2:  // AllClear
                handleAllClear(state, onComplete);
                break;
            case 3:  // BoxClear
                handleBoxClear(state, onComplete);
                break;
            case 4:  // OneLineClear
                handleOneLineClear(state, onComplete);
                break;
            default:
                // 일반 블록: 즉시 콜백
                if (onComplete != null) {
                    onComplete.accept(state);
                }
                break;
        }
    }
    
    // ==================== AllClear (값 2) ====================
    
    /**
     * AllClear 효과: 보드 전체 초기화
     * 점수: 500 * (speedLevel + 1) * difficultyMultiplier
     */
    private void handleAllClear(GameState state, java.util.function.Consumer<GameState> onComplete) {
        // 점수 계산
        int speedLevel = calculateSpeedLevel(state);
        double difficultyMultiplier = getDifficultyMultiplier(state);
        int allClearScore = (int) Math.round(500 * (speedLevel + 1) * difficultyMultiplier);
        
        // 애니메이션 시작
        animationManager.startAllClearAnimation(() -> {
            // 애니메이션 완료 후: 보드 초기화
            int[][] newBoard = state.getBoardArray();
            int[][] newColorBoard = state.getColorBoard();
            
            // 내부 영역 초기화 (벽은 유지)
            for (int r = INNER_TOP; r <= INNER_BOTTOM; r++) {
                for (int c = INNER_LEFT; c <= INNER_RIGHT; c++) {
                    newBoard[r][c] = 0;
                    newColorBoard[r][c] = 0;
                }
            }
            
            // 새로운 GameState 생성
            GameState newState = new GameState.Builder(
                newBoard,
                newColorBoard,
                state.getCurrentBlock(),
                state.getNextBlock(),
                state.isItemMode()
            )
                .score(state.getScore() + allClearScore)
                .totalLinesCleared(state.getTotalLinesCleared())
                .currentLevel(state.getCurrentLevel())
                .lineClearCount(state.getLineClearCount())
                .itemGenerateCount(state.getItemGenerateCount())
                .blocksSpawned(state.getBlocksSpawned())
                .lastLineClearScore(allClearScore)
                .build();
            
            // 애니메이션 상태 동기화
            newState = animationManager.applyAnimationState(newState);
            
            if (view != null) view.repaintBlock();
            
            if (onComplete != null) {
                onComplete.accept(newState);
            }
        });
    }
    
    // ==================== BoxClear (값 3) ====================
    
    /**
     * BoxClear 효과: 5x5 영역 폭발 후 중력 적용
     * 점수: 2줄 클리어와 동일
     */
    private void handleBoxClear(GameState state, java.util.function.Consumer<GameState> onComplete) {
        int[][] board = state.getBoardArray();
        
        // 중심점 찾기 (값이 3인 셀)
        List<int[]> centers = new ArrayList<>();
        for (int r = INNER_TOP; r <= INNER_BOTTOM; r++) {
            for (int c = INNER_LEFT; c <= INNER_RIGHT; c++) {
                if (board[r][c] == 3) {
                    centers.add(new int[]{r, c});
                }
            }
        }
        
        if (centers.isEmpty()) {
            if (onComplete != null) {
                onComplete.accept(state);
            }
            return;
        }
        
        // 점수 계산 (2줄 클리어와 동일)
        int boxClearScore = calculateLineClearScore(2, state);
        
        // 애니메이션 시작
        animationManager.startBoxClearAnimation(centers, () -> {
            // 애니메이션 완료 후: 5x5 영역 클리어
            int[][] newBoard = state.getBoardArray();
            int[][] newColorBoard = state.getColorBoard();
            
            for (int[] center : centers) {
                clearBox5x5(newBoard, newColorBoard, center[0], center[1]);
            }
            
            // 중력 적용
            applyGravity(newBoard, newColorBoard);
            
            // 새로운 GameState 생성
            GameState newState = new GameState.Builder(
                newBoard,
                newColorBoard,
                state.getCurrentBlock(),
                state.getNextBlock(),
                state.isItemMode()
            )
                .score(state.getScore() + boxClearScore)
                .totalLinesCleared(state.getTotalLinesCleared())
                .currentLevel(state.getCurrentLevel())
                .lineClearCount(state.getLineClearCount())
                .itemGenerateCount(state.getItemGenerateCount())
                .blocksSpawned(state.getBlocksSpawned())
                .lastLineClearScore(boxClearScore)
                .build();
            
            // 애니메이션 상태 동기화
            newState = animationManager.applyAnimationState(newState);
            
            if (view != null) view.repaintBlock();
            
            if (onComplete != null) {
                onComplete.accept(newState);
            }
        });
    }
    
    /**
     * 5x5 영역 클리어 (벽은 유지)
     */
    private void clearBox5x5(int[][] board, int[][] colorBoard, int centerRow, int centerCol) {
        int r1 = Math.max(INNER_TOP, centerRow - 2);
        int r2 = Math.min(INNER_BOTTOM, centerRow + 2);
        int c1 = Math.max(INNER_LEFT, centerCol - 2);
        int c2 = Math.min(INNER_RIGHT, centerCol + 2);
        
        for (int r = r1; r <= r2; r++) {
            for (int c = c1; c <= c2; c++) {
                // 바닥/좌우 벽은 유지
                if (r == ROWS - 1 || c == 0 || c == COLS - 1) continue;
                board[r][c] = 0;
                colorBoard[r][c] = 0;
            }
        }
    }
    
    // ==================== OneLineClear (값 4) ====================
    
    /**
     * OneLineClear 효과: 특정 행들을 클리어
     * 점수: (지워진 줄 수 + 1)로 계산
     */
    private void handleOneLineClear(GameState state, java.util.function.Consumer<GameState> onComplete) {
        int[][] board = state.getBoardArray();
        
        // 값이 4인 행 찾기
        HashSet<Integer> rowsWithFour = new HashSet<>();
        for (int r = INNER_TOP; r <= INNER_BOTTOM; r++) {
            for (int c = INNER_LEFT; c <= INNER_RIGHT; c++) {
                if (board[r][c] == 4) {
                    rowsWithFour.add(r);
                }
            }
        }
        
        if (rowsWithFour.isEmpty()) {
            if (onComplete != null) {
                onComplete.accept(state);
            }
            return;
        }
        
        List<Integer> rows = new ArrayList<>(rowsWithFour);
        Collections.sort(rows, Collections.reverseOrder());  // 역순 정렬
        
        // 점수 계산 (지워진 줄 수 + 1)
        int oneLineClearScore = calculateLineClearScore(rows.size() + 1, state);
        
        // 애니메이션 시작
        animationManager.startLineClearAnimation(rows, () -> {
            // 애니메이션 완료 후: 행 삭제 및 위를 내림
            int[][] newBoard = state.getBoardArray();
            int[][] newColorBoard = state.getColorBoard();
            
            int linesCleared = rows.size();
            int newLineClearCount = state.getLineClearCount() + linesCleared;
            
            for (int r : rows) {
                clearRowForce(newBoard, newColorBoard, r);
            }
            
            // 아이템 대기 플래그 확인
            boolean itemPending = false;
            if (state.isItemMode()) {
                int divisor = 3;  // GameModel의 divisor 값
                if (newLineClearCount / divisor > state.getItemGenerateCount()) {
                    itemPending = true;
                }
            }
            
            // 새로운 GameState 생성
            GameState newState = new GameState.Builder(
                newBoard,
                newColorBoard,
                state.getCurrentBlock(),
                state.getNextBlock(),
                state.isItemMode()
            )
                .score(state.getScore() + oneLineClearScore)
                .totalLinesCleared(state.getTotalLinesCleared())
                .currentLevel(state.getCurrentLevel())
                .lineClearCount(newLineClearCount)
                .itemGenerateCount(state.getItemGenerateCount())
                .blocksSpawned(state.getBlocksSpawned())
                .lastLineClearScore(oneLineClearScore)
                .build();
            
            // 애니메이션 상태 동기화
            newState = animationManager.applyAnimationState(newState);
            
            if (view != null) view.repaintBlock();
            
            if (onComplete != null) {
                onComplete.accept(newState);
            }
        });
    }
    
    /**
     * 특정 행을 강제로 삭제하고 위를 내림
     */
    private void clearRowForce(int[][] board, int[][] colorBoard, int targetRow) {
        if (targetRow < INNER_TOP || targetRow > INNER_BOTTOM) return;
        
        // 위쪽 행들을 한 칸씩 내림
        for (int r = targetRow; r > INNER_TOP; r--) {
            for (int c = INNER_LEFT; c <= INNER_RIGHT; c++) {
                board[r][c] = board[r - 1][c];
                colorBoard[r][c] = colorBoard[r - 1][c];
            }
        }
        
        // 최상단 행 초기화
        for (int c = INNER_LEFT; c <= INNER_RIGHT; c++) {
            board[INNER_TOP][c] = 0;
            colorBoard[INNER_TOP][c] = 0;
        }
    }
    
    // ==================== WeightBlock (값 5) ====================
    
    /**
     * WeightBlock 드릴 효과 시작
     * 
     * @param state 현재 게임 상태
     * @param onComplete 드릴 완료 후 콜백
     */
    public void handleWeightBlock(GameState state, java.util.function.Consumer<GameState> onComplete) {
        // WeightBlock 로직은 별도 구현 필요 (현재 블록이 아래로 드릴하며 내려가는 복잡한 로직)
        // 일단 기본 구조만 작성
        
        animationManager.startWeightAnimation(() -> {
            // 매 프레임마다 블록을 한 칸씩 아래로 이동
            // TODO: 실제 드릴 로직 구현
        }, () -> {
            // 드릴 완료
            if (onComplete != null) {
                onComplete.accept(state);
            }
        });
    }
    
    // ==================== 유틸리티 메서드 ====================
    
    /**
     * 중력 적용: 공중에 떠 있는 블록들을 아래로 내림
     */
    private void applyGravity(int[][] board, int[][] colorBoard) {
        for (int c = INNER_LEFT; c <= INNER_RIGHT; c++) {
            int writeR = INNER_BOTTOM;  // 아래쪽부터 채움
            for (int r = INNER_BOTTOM; r >= INNER_TOP; r--) {
                int v = board[r][c];
                if (v != 0) {
                    if (r != writeR) {
                        board[writeR][c] = v;
                        colorBoard[writeR][c] = colorBoard[r][c];
                        board[r][c] = 0;
                        colorBoard[r][c] = 0;
                    }
                    writeR--;
                }
            }
            // 위쪽 남은 부분 0으로 초기화
            for (int r = writeR; r >= INNER_TOP; r--) {
                board[r][c] = 0;
                colorBoard[r][c] = 0;
            }
        }
    }
    
    /**
     * 라인 클리어 점수 계산
     */
    private int calculateLineClearScore(int linesCleared, GameState state) {
        int baseScore;
        switch (linesCleared) {
            case 1: baseScore = 100; break;
            case 2: baseScore = 300; break;
            case 3: baseScore = 500; break;
            case 4: baseScore = 800; break;
            default: baseScore = linesCleared * 100; break;
        }
        
        int level = state.getCurrentLevel();
        double difficultyMultiplier = getDifficultyMultiplier(state);
        
        return (int) Math.round(baseScore * level * difficultyMultiplier);
    }
    
    /**
     * 속도 레벨 계산
     */
    private int calculateSpeedLevel(GameState state) {
        int blocksSpawned = state.getBlocksSpawned();
        int linesCleared = state.getLineClearCount();
        
        int level = Math.max(blocksSpawned / 30, linesCleared / 5);
        return Math.min(level, 6);
    }
    
    /**
     * 난이도 배율 계산 (itemMode에 따라)
     */
    private double getDifficultyMultiplier(GameState state) {
        return state.isItemMode() ? 0.7 : 1.0;
    }
}
