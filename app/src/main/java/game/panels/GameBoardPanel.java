package game.panels;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.JPanel;

import blocks.Block;
import game.GameModel;
import game.core.GameState;

/**
 * 메인 게임 보드 패널 (20x10)
 * - 쌓인 블록 렌더링
 * - 현재 떨어지는 블록 렌더링
 * - 애니메이션 효과 (라인 클리어, 박스 클리어 등)
 */
public class GameBoardPanel extends JPanel {
    
    private static final int ROWS = 20;
    private static final int COLS = 10;
    private int cellSize = 30;
    private int strokeWidth = 3;
    
    // 렌더링할 데이터
    private GameModel gameModel;
    private GameState currentState;
    private Block fallingBlock;
    
    public GameBoardPanel() {
        setOpaque(false); // 투명 배경
        setPreferredSize(new Dimension(COLS * cellSize, ROWS * cellSize));
    }
    
    /**
     * 셀 크기 설정 (스케일링 지원)
     */
    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
        setPreferredSize(new Dimension(COLS * cellSize, ROWS * cellSize));
        revalidate();
    }
    
    public int getCellSize() {
        return cellSize;
    }
    
    /**
     * GameModel 설정 (기존 방식 호환)
     */
    public void setGameModel(GameModel model) {
        this.gameModel = model;
        repaint();
    }
    
    /**
     * GameState 기반 렌더링 (새로운 방식)
     */
    public void render(GameState state) {
        this.currentState = state;
        repaint();
    }
    
    /**
     * 현재 떨어지는 블록 설정
     */
    public void setFallingBlock(Block block) {
        this.fallingBlock = block;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // GameState 우선 사용, 없으면 GameModel 사용
        if (currentState != null) {
            paintFromState(g2d);
        } else if (gameModel != null) {
            paintFromModel(g2d);
        }
    }
    
    /**
     * GameState 기반 렌더링
     */
    private void paintFromState(Graphics2D g2d) {
        int[][] board = currentState.getBoardArray();
        int[][] colorBoard = currentState.getColorBoard();
        Block currentBlock = currentState.getCurrentBlock();
        
        // 배경 그리기
        drawBackground(g2d);
        
        // 쌓인 블록 그리기
        stackBlockFromState(g2d, board, colorBoard);
        
        // 현재 떨어지는 블록 그리기
        if (currentBlock != null) {
            drawBlock(g2d, currentBlock);
        }
        
        // 애니메이션 효과
        drawAnimations(g2d, currentState);
        
        // 격자 및 테두리
        drawGrid(g2d);
        drawBorder(g2d);
    }
    
    /**
     * GameModel 기반 렌더링 (기존 방식 호환)
     */
    private void paintFromModel(Graphics2D g2d) {
        int[][] board = gameModel.getBoard();
        
        // 배경 그리기
        drawBackground(g2d);
        
        // 쌓인 블록 그리기
        for (int row = 2; row < ROWS + 2; row++) {
            for (int col = 1; col < COLS + 1; col++) {
                if (board[row][col] > 0 && board[row][col] < 10) {
                    Color blockColor = getBlockColor(board[row][col]);
                    g2d.setColor(blockColor);
                    g2d.fillRect(
                        (col - 1) * cellSize,
                        (row - 2) * cellSize,
                        cellSize,
                        cellSize
                    );
                }
            }
        }
        
        // 현재 떨어지는 블록
        if (fallingBlock != null) {
            drawBlock(g2d, fallingBlock);
        }
        
        // 격자 및 테두리
        drawGrid(g2d);
        drawBorder(g2d);
    }
    
    /**
     * 배경 그리기
     */
    private void drawBackground(Graphics2D g2d) {
        g2d.setColor(new Color(240, 240, 255));
        g2d.fillRect(0, 0, COLS * cellSize, ROWS * cellSize);
    }
    
    /**
     * GameState에서 쌓인 블록 그리기
     */
    private void stackBlockFromState(Graphics2D g2d, int[][] board, int[][] colorBoard) {
        for (int row = 2; row < ROWS + 2; row++) {
            for (int col = 1; col < COLS + 1; col++) {
                if (board[row][col] > 0 && board[row][col] < 10) {
                    Color blockColor;
                    if (colorBoard != null && colorBoard[row][col] != 0) {
                        blockColor = new Color(colorBoard[row][col]);
                    } else {
                        blockColor = getBlockColor(board[row][col]);
                    }
                    
                    g2d.setColor(blockColor);
                    g2d.fillRect(
                        (col - 1) * cellSize,
                        (row - 2) * cellSize,
                        cellSize,
                        cellSize
                    );
                }
            }
        }
    }
    
    /**
     * 현재 떨어지는 블록 그리기
     */
    private void drawBlock(Graphics2D g2d, Block block) {
        int[][] shape = block.getShape();
        Color color = block.getColor();
        
        g2d.setColor(color);
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1) {
                    int drawX = (block.getX() + col - 1) * cellSize;
                    int drawY = (block.getY() + row - 2) * cellSize;
                    g2d.fillRect(drawX, drawY, cellSize, cellSize);
                }
            }
        }
    }
    
    /**
     * 애니메이션 효과 그리기
     */
    private void drawAnimations(Graphics2D g2d, GameState state) {
        float alpha = 0.7f;
        
        // 라인 클리어 애니메이션
        if (state.isLineClearAnimating()) {
            List<Integer> flashingRows = state.getFlashingRows();
            boolean flashBlack = state.isFlashBlack();
            
            if (flashingRows != null && !flashingRows.isEmpty()) {
                Color flashColor = flashBlack ? Color.BLACK : Color.WHITE;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(flashColor);
                
                for (int row : flashingRows) {
                    g2d.fillRect(
                        0,
                        (row - 2) * cellSize,
                        COLS * cellSize,
                        cellSize
                    );
                }
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
        
        // All Clear 애니메이션
        if (state.isAllClearAnimating()) {
            boolean flashBlack = state.isAllClearFlashBlack();
            Color flashColor = flashBlack ? Color.BLACK : Color.WHITE;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(flashColor);
            g2d.fillRect(0, 0, COLS * cellSize, ROWS * cellSize);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        
        // Box Clear 애니메이션
        if (state.isBoxClearAnimating()) {
            List<int[]> centers = state.getBoxFlashCenters();
            boolean flashBlack = state.isBoxFlashBlack();
            
            if (centers != null && !centers.isEmpty()) {
                Color flashColor = flashBlack ? Color.BLACK : Color.WHITE;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(flashColor);
                
                for (int[] center : centers) {
                    int centerX = center[1];  // col
                    int centerY = center[0];  // row
                    int startRow = Math.max(2, centerY - 2);
                    int endRow = Math.min(ROWS + 1, centerY + 3);
                    int startCol = Math.max(1, centerX - 2);
                    int endCol = Math.min(COLS, centerX + 3);
                    
                    for (int row = startRow; row <= endRow; row++) {
                        for (int col = startCol; col <= endCol; col++) {
                            g2d.fillRect(
                                (col - 1) * cellSize,
                                (row - 2) * cellSize,
                                cellSize,
                                cellSize
                            );
                        }
                    }
                }
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
    }
    
    /**
     * 격자 그리기
     */
    private void drawGrid(Graphics2D g2d) {
        float alpha = 0.3f;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.setColor(new Color(0, 0, 0, 100));
        
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                g2d.drawRect(col * cellSize, row * cellSize, cellSize, cellSize);
            }
        }
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    /**
     * 테두리 그리기
     */
    private void drawBorder(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.setColor(Color.BLACK);
        
        int width = COLS * cellSize;
        int height = ROWS * cellSize;
        
        // 좌측, 우측, 하단만 (상단 제외)
        g2d.drawLine(0, 0, 0, height);                    // 좌측
        g2d.drawLine(width, 0, width, height);            // 우측
        g2d.drawLine(0, height, width, height);           // 하단
    }
    
    /**
     * 블록 타입별 색상 반환
     */
    private Color getBlockColor(int blockType) {
        switch (blockType) {
            case 1: return new Color(255, 0, 0);      // I - 빨강
            case 2: return new Color(255, 165, 0);    // J - 주황
            case 3: return new Color(255, 255, 0);    // L - 노랑
            case 4: return new Color(0, 255, 0);      // O - 초록
            case 5: return new Color(0, 0, 255);      // S - 파랑
            case 6: return new Color(75, 0, 130);     // T - 남색
            case 7: return new Color(238, 130, 238);  // Z - 보라
            default: return Color.GRAY;
        }
    }
}
