package game.core;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import game.GameView;

/**
 * 게임 애니메이션을 담당하는 클래스
 * 
 * 책임:
 * - 라인 클리어 깜빡임 애니메이션
 * - AllClear 애니메이션
 * - BoxClear 애니메이션  
 * - WeightBlock 드릴 애니메이션
 */
public class AnimationManager {
    
    private final GameView view;
    
    // 라인 클리어 애니메이션
    private boolean lineClearAnimating = false;
    private boolean flashBlack = false;
    private List<Integer> flashingRows = new ArrayList<>();
    private Timer lineClearTimer;
    
    // AllClear 애니메이션
    private boolean allClearAnimating = false;
    private boolean allClearFlashBlack = false;
    private Timer allClearTimer;
    
    // BoxClear 애니메이션
    private boolean boxClearAnimating = false;
    private boolean boxFlashBlack = false;
    private List<int[]> boxFlashCenters = new ArrayList<>();
    
    // WeightBlock 애니메이션
    private boolean weightAnimating = false;
    private Timer weightTimer;
    
    /**
     * AnimationManager 생성자
     * 
     * @param view 게임 뷰 (렌더링용)
     */
    public AnimationManager(GameView view) {
        this.view = view;
    }
    
    /**
     * 애니메이션 중인지 확인
     */
    public boolean isAnimating() {
        return lineClearAnimating || allClearAnimating || boxClearAnimating || weightAnimating;
    }
    
    // ==================== 라인 클리어 애니메이션 ====================
    
    /**
     * 라인 클리어 애니메이션 시작
     * 
     * @param fullLines 클리어할 라인 번호 리스트
     * @param onComplete 애니메이션 완료 후 콜백
     */
    public void startLineClearAnimation(List<Integer> fullLines, Runnable onComplete) {
        System.out.println("[AnimationManager] startLineClearAnimation 호출됨! fullLines=" + fullLines + ", lineClearAnimating=" + lineClearAnimating);
        
        if (lineClearAnimating || fullLines.isEmpty()) {
            System.out.println("[AnimationManager] 애니메이션 건너뜀 (이미 진행 중이거나 fullLines 비어있음)");
            if (onComplete != null) onComplete.run();
            return;
        }
        
        flashingRows.clear();
        flashingRows.addAll(fullLines);
        lineClearAnimating = true;
        flashBlack = true;
        
        System.out.println("[AnimationManager] 라인 클리어 애니메이션 시작! flashingRows=" + flashingRows + ", flashBlack=" + flashBlack);
        
        if (view != null) view.repaintBlock();
        
        // 기존 타이머 정지
        if (lineClearTimer != null) {
            lineClearTimer.stop();
        }
        
        // 120ms 후 실제 클리어 실행
        lineClearTimer = new Timer(120, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lineClearTimer.stop();
                lineClearTimer = null;
                flashBlack = false;
                flashingRows.clear();
                lineClearAnimating = false;
                
                if (view != null) view.repaintBlock();
                
                // 콜백 실행
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        lineClearTimer.setRepeats(false);
        lineClearTimer.start();
    }
    
    public boolean isLineClearAnimating() {
        return lineClearAnimating;
    }
    
    public boolean isFlashBlack() {
        return flashBlack;
    }
    
    public boolean isRowFlashing(int row) {
        return lineClearAnimating && flashingRows.contains(row);
    }
    
    // ==================== AllClear 애니메이션 ====================
    
    /**
     * AllClear 애니메이션 시작 (전체 보드 클리어)
     * 
     * @param onComplete 애니메이션 완료 후 콜백
     */
    public void startAllClearAnimation(Runnable onComplete) {
        if (allClearAnimating) {
            if (onComplete != null) onComplete.run();
            return;
        }
        
        allClearAnimating = true;
        allClearFlashBlack = true;
        
        if (view != null) view.repaintBlock();
        
        // 기존 타이머 정지
        if (allClearTimer != null) {
            allClearTimer.stop();
        }
        
        // 140ms 후 실제 클리어 실행
        allClearTimer = new Timer(140, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                allClearTimer.stop();
                allClearTimer = null;
                allClearFlashBlack = false;
                allClearAnimating = false;
                
                if (view != null) view.repaintBlock();
                
                // 콜백 실행
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        allClearTimer.setRepeats(false);
        allClearTimer.start();
    }
    
    public boolean isAllClearAnimating() {
        return allClearAnimating;
    }
    
    public boolean isAllClearFlashBlack() {
        return allClearAnimating && allClearFlashBlack;
    }
    
    // ==================== BoxClear 애니메이션 ====================
    
    /**
     * BoxClear 애니메이션 시작 (5x5 영역 폭발)
     * 
     * @param centers 폭발 중심점 리스트 [row, col]
     * @param onComplete 애니메이션 완료 후 콜백
     */
    public void startBoxClearAnimation(List<int[]> centers, Runnable onComplete) {
        if (boxClearAnimating || centers.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }
        
        boxFlashCenters.clear();
        boxFlashCenters.addAll(centers);
        boxClearAnimating = true;
        boxFlashBlack = true;
        
        if (view != null) view.repaintBlock();
        
        // 140ms 후 실제 폭발 실행
        Timer boxTimer = new Timer(140, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer)e.getSource()).stop();
                boxFlashBlack = false;
                boxFlashCenters.clear();
                boxClearAnimating = false;
                
                if (view != null) view.repaintBlock();
                
                // 콜백 실행
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        boxTimer.setRepeats(false);
        boxTimer.start();
    }
    
    public boolean isBoxClearAnimating() {
        return boxClearAnimating;
    }
    
    public boolean isBoxFlashBlack() {
        return boxClearAnimating && boxFlashBlack;
    }
    
    public boolean isCellInBoxFlash(int row, int col) {
        if (!boxClearAnimating) return false;
        
        final int INNER_TOP = 2;
        final int INNER_BOTTOM = 21;
        final int INNER_LEFT = 1;
        final int INNER_RIGHT = 10;
        
        for (int[] rc : boxFlashCenters) {
            int cr = rc[0], cc = rc[1];
            if (Math.abs(row - cr) <= 2 && Math.abs(col - cc) <= 2) {
                // 내부 영역만 유효 (벽 제외)
                if (row >= INNER_TOP && row <= INNER_BOTTOM && col >= INNER_LEFT && col <= INNER_RIGHT) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // ==================== WeightBlock 애니메이션 ====================
    
    /**
     * WeightBlock 드릴 애니메이션 시작
     * 
     * @param onFrame 매 프레임마다 호출 (블록 한 칸 아래로 이동)
     * @param onComplete 애니메이션 완료 후 콜백
     */
    public void startWeightAnimation(Runnable onFrame, Runnable onComplete) {
        if (weightAnimating) {
            if (onComplete != null) onComplete.run();
            return;
        }
        
        weightAnimating = true;
        
        final int delayMs = 60;  // 60ms마다 한 칸씩 이동
        weightTimer = new Timer(delayMs, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 매 프레임마다 콜백 호출
                if (onFrame != null) {
                    onFrame.run();
                }
            }
        });
        weightTimer.start();
    }
    
    /**
     * WeightBlock 애니메이션 정지
     */
    public void stopWeightAnimation() {
        weightAnimating = false;
        if (weightTimer != null) {
            weightTimer.stop();
            weightTimer = null;
        }
        if (view != null) {
            view.repaintBlock();
        }
    }
    
    public boolean isWeightAnimating() {
        return weightAnimating;
    }
    
    // ==================== 유틸리티 ====================
    
    /**
     * 모든 애니메이션 정지
     */
    public void stopAllAnimations() {
        if (lineClearTimer != null) {
            lineClearTimer.stop();
            lineClearTimer = null;
        }
        if (allClearTimer != null) {
            allClearTimer.stop();
            allClearTimer = null;
        }
        if (weightTimer != null) {
            weightTimer.stop();
            weightTimer = null;
        }
        
        lineClearAnimating = false;
        allClearAnimating = false;
        boxClearAnimating = false;
        weightAnimating = false;
        
        flashingRows.clear();
        boxFlashCenters.clear();
    }
    
    /**
     * GameState에 애니메이션 상태 반영
     */
    public GameState applyAnimationState(GameState state) {
        return new GameState.Builder(
            state.getBoardArray(),
            state.getColorBoard(),
            state.getCurrentBlock(),
            state.getNextBlock(),
            state.isItemMode()
        )
            .score(state.getScore())
            .totalLinesCleared(state.getTotalLinesCleared())
            .currentLevel(state.getCurrentLevel())
            .lineClearCount(state.getLineClearCount())
            .itemGenerateCount(state.getItemGenerateCount())
            .blocksSpawned(state.getBlocksSpawned())
            .lastLineClearScore(state.getLastLineClearScore())
            .lineClearAnimating(lineClearAnimating)
            .flashBlack(flashBlack)
            .flashingRows(new ArrayList<>(flashingRows))
            .allClearAnimating(allClearAnimating)
            .allClearFlashBlack(allClearFlashBlack)
            .boxClearAnimating(boxClearAnimating)
            .boxFlashBlack(boxFlashBlack)
            .boxFlashCenters(new ArrayList<>(boxFlashCenters))
            .weightAnimating(weightAnimating)
            .build();
    }
}
