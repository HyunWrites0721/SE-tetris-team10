package game;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JPanel;

import blocks.Block;
import game.core.GameState;
import game.panels.GameBoardPanel;
import game.panels.HighScorePanel;
import game.panels.NextBlockPanel;
import game.panels.ScorePanel;

/**
 * 게임 화면 컨테이너
 * - GameBoardPanel, NextBlockPanel, ScorePanel, HighScorePanel을 조합
 */
public class GameView extends JPanel {

    public double scale = 1.0;
    public int ROWS = 20;
    public int COLS = 10;
    public int CELL_SIZE = 30;
    
    // NEXT 패널 크기
    public int NEXT_ROWS = 6;
    public int NEXT_COLS = 6;
    
    // 하위 패널들
    private GameBoardPanel gameBoardPanel;
    private NextBlockPanel nextBlockPanel;
    private ScorePanel scorePanel;
    private HighScorePanel highScorePanel;
    private game.panels.AttackPreviewPanel attackPreviewPanel;
    
    private FrameBoard frameBoard;
    private boolean showHighScore = true;  // HighScore 패널 표시 여부
    private boolean showAttackPreview = false;  // AttackPreview 패널 표시 여부 (대전/P2P 모드)
    
    // 생성자 (기본: HighScore 표시, AttackPreview 숨김)
    public GameView(boolean item) {
        this(item, true, false);
    }
    
    // 생성자 (HighScore 표시 여부 선택 가능)
    public GameView(boolean item, boolean showHighScore) {
        this(item, showHighScore, false);
    }
    
    // 생성자 (HighScore, AttackPreview 표시 여부 선택 가능)
    public GameView(boolean item, boolean showHighScore, boolean showAttackPreview) {
        this.showHighScore = showHighScore;
        this.showAttackPreview = showAttackPreview;
        setLayout(null); // 절대 위치 지정
        setOpaque(false); // 투명하게 설정하여 배경이 보이도록
        
        // 패널들 생성
        gameBoardPanel = new GameBoardPanel();
        nextBlockPanel = new NextBlockPanel();
        scorePanel = new ScorePanel();
        
        if (showHighScore) {
            highScorePanel = new HighScorePanel();
        }
        
        if (showAttackPreview) {
            attackPreviewPanel = new game.panels.AttackPreviewPanel();
        }
        
        // 레이아웃 설정
        layoutPanels();
        
        // 패널들 추가
        add(gameBoardPanel);
        add(nextBlockPanel);
        add(scorePanel);
        
        if (showAttackPreview && attackPreviewPanel != null) {
            add(attackPreviewPanel);
        }
        
        if (showHighScore && highScorePanel != null) {
            add(highScorePanel);
        }
        
        // 크기 변경 시 중앙 정렬 유지
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutPanels();
            }
        });
    }
    
    /**
     * 패널들의 위치와 크기 설정
     */
    private void layoutPanels() {
        int boardWidth = COLS * CELL_SIZE;
        int boardHeight = ROWS * CELL_SIZE;
        int rightPanelWidth = NEXT_COLS * CELL_SIZE;
        
        // 전체 크기 계산
        int totalWidth = boardWidth + rightPanelWidth;
        int totalHeight = boardHeight;
        
        // 중앙 정렬을 위한 여백 계산
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int offsetX = 0;
        int offsetY = 0;
        
        if (panelWidth > 0 && panelHeight > 0) {
            offsetX = Math.max(0, (panelWidth - totalWidth) / 2);
            offsetY = Math.max(0, (panelHeight - totalHeight) / 2);
        }
        
        // 게임 보드 (왼쪽) - 중앙 정렬 적용
        gameBoardPanel.setBounds(offsetX, offsetY, boardWidth, boardHeight);
        gameBoardPanel.setCellSize(CELL_SIZE);
        
        // NEXT 패널 (오른쪽 상단) - 중앙 정렬 적용
        int nextHeight = NEXT_ROWS * CELL_SIZE;
        nextBlockPanel.setBounds(offsetX + boardWidth, offsetY, rightPanelWidth, nextHeight);
        nextBlockPanel.setCellSize(CELL_SIZE);
        
        // SCORE 패널 (NEXT 아래) - 중앙 정렬 적용
        int scoreHeight = 4 * CELL_SIZE;
        int scoreY = nextHeight;
        scorePanel.setBounds(offsetX + boardWidth, offsetY + scoreY, rightPanelWidth, scoreHeight);
        scorePanel.setCellSize(CELL_SIZE);
        
        int currentY = scoreY + scoreHeight;
        
        // ATTACK PREVIEW 패널 (SCORE 아래) - 대전/P2P 모드에서만 표시
        if (showAttackPreview && attackPreviewPanel != null) {
            int attackPreviewHeight = 8 * CELL_SIZE;
            attackPreviewPanel.setBounds(offsetX + boardWidth, offsetY + currentY, rightPanelWidth, attackPreviewHeight);
            attackPreviewPanel.setCellSize(CELL_SIZE);
            currentY += attackPreviewHeight;
        }
        
        // HIGHSCORE 패널 (ATTACK PREVIEW 또는 SCORE 아래) - 중앙 정렬 적용 (옵션)
        if (showHighScore && highScorePanel != null) {
            int highScoreHeight = 4 * CELL_SIZE;
            highScorePanel.setBounds(offsetX + boardWidth, offsetY + currentY, rightPanelWidth, highScoreHeight);
            highScorePanel.setCellSize(CELL_SIZE);
        }
        
        // 전체 크기 설정
        setPreferredSize(new Dimension(totalWidth, totalHeight));
    }
    
    /**
     * 스케일 변환 (화면 크기 조절)
     */
    public void convertScale(double scale) {
        this.scale = scale;
        
        // 셀 크기 재계산
        int maxBoardWidth = (int)(600 * scale * 0.7);
        int maxBoardHeight = (int)(600 * scale * 0.95);
        
        int cellByWidth = maxBoardWidth / (COLS + NEXT_COLS + 2);
        int cellByHeight = maxBoardHeight / (ROWS + 1);
        
        CELL_SIZE = Math.min(cellByWidth, cellByHeight);
        CELL_SIZE = Math.max(CELL_SIZE, 15);
        
        // 폰트 크기 조정
        double ratio = (start.StartFrame.screenRatio > 0) ? start.StartFrame.screenRatio : 1.2;
        int fontSize = Math.max(12, (int)(24 * ratio));
        
        // 각 패널에 적용
        gameBoardPanel.setCellSize(CELL_SIZE);
        nextBlockPanel.setCellSize(CELL_SIZE);
        nextBlockPanel.setFontSize(fontSize);
        scorePanel.setCellSize(CELL_SIZE);
        scorePanel.setFontSize(fontSize);
        
        if (showAttackPreview && attackPreviewPanel != null) {
            attackPreviewPanel.setCellSize(CELL_SIZE);
            attackPreviewPanel.setFontSize(fontSize);
        }
        
        if (showHighScore && highScorePanel != null) {
            highScorePanel.setCellSize(CELL_SIZE);
            highScorePanel.setFontSize(fontSize);
        }
        
        // 레이아웃 다시 설정
        layoutPanels();
        
        // 상위 프레임 크기 조절
        if (getParent() != null && getParent().getParent() instanceof FrameBoard) {
            FrameBoard frameBoard = (FrameBoard) getParent().getParent();
            frameBoard.updateFrameSize(scale);
        }
        
        repaint();
    }
    
    // ==================== 기존 호환성 메서드들 ====================
    
    /**
     * GameState 기반 렌더링 (새로운 방식)
     */
    public void render(GameState state) {
        gameBoardPanel.render(state);
        if (state.getNextBlock() != null) {
            nextBlockPanel.setNextBlock(state.getNextBlock());
        }
    }
    
    /**
     * 현재 떨어지는 블록 설정 (deprecated - render(GameState) 사용 권장)
     * 하위 호환성을 위해 유지
     */
    @Deprecated
    public void setFallingBlock(Block block) {
        // 하위 호환성을 위해 빈 메서드로 유지
        // 실제로는 render(GameState)를 통해 렌더링됨
    }
    
    /**
     * 다음 블록 설정
     */
    public void setNextBlock(Block block) {
        nextBlockPanel.setNextBlock(block);
    }
    
    /**
     * 점수 설정
     */
    public void setScore(int score) {
        scorePanel.setScore(score);
    }
    
    /**
     * 점수 조회
     */
    public int getScore() {
        return scorePanel.getScore();
    }
    
    /**
     * 최고 점수 설정
     */
    public void setHighScore(int highScore) {
        if (showHighScore && highScorePanel != null) {
            highScorePanel.setHighScore(highScore);
        }
    }
    
    /**
     * FrameBoard 설정 (호환성)
     */
    public void setFrameBoard(FrameBoard frameBoard) {
        this.frameBoard = frameBoard;
    }
    
    /**
     * 화면 전체 갱신 (호환성)
     */
    public void repaintBlock() {
        repaint();
    }
    
    /**
     * 공격 미리보기 업데이트 (대전/P2P 모드)
     */
    public void updateAttackPreview(java.util.List<game.model.AttackPreviewItem> items) {
        if (showAttackPreview && attackPreviewPanel != null) {
            attackPreviewPanel.updateQueue(items);
        }
    }
    
}
