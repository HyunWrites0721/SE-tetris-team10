package game.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import blocks.Block;

/**
 * 다음 블록 미리보기 패널
 */
public class NextBlockPanel extends JPanel {
    
    private static final int ROWS = 6;
    private static final int COLS = 6;
    private static final int HEADER_ROWS = 2;  // "NEXT" 텍스트 영역
    
    private int cellSize = 30;
    private int fontSize = 24;
    
    private Block nextBlock;
    
    public NextBlockPanel() {
        setOpaque(false);
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
    
    /**
     * 폰트 크기 설정
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        repaint();
    }
    
    /**
     * 다음 블록 설정
     */
    public void setNextBlock(Block block) {
        this.nextBlock = block;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        int width = COLS * cellSize;
        int height = ROWS * cellSize;
        int headerHeight = HEADER_ROWS * cellSize;
        
        // 배경 그리기
        g2d.setColor(new Color(255, 248, 235));
        g2d.fillRect(0, 0, width, height);
        
        // "NEXT" 텍스트
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        g2d.setColor(Color.BLACK);
        String text = "NEXT";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (width - fm.stringWidth(text)) / 2;
        int textY = (headerHeight - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, textX, textY);
        
        // 헤더 구분선
        g2d.drawLine(0, headerHeight, width, headerHeight);
        
        // 다음 블록 그리기
        if (nextBlock != null) {
            int[][] shape = nextBlock.getShape();
            Color color = nextBlock.getColor();
            
            // 블록을 중앙에 배치
            int blockWidth = shape[0].length;
            int blockHeight = shape.length;
            int blockAreaHeight = height - headerHeight;
            
            int startX = (width - blockWidth * cellSize) / 2;
            int startY = headerHeight + (blockAreaHeight - blockHeight * cellSize) / 2;
            
            g2d.setColor(color);
            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[row].length; col++) {
                    if (shape[row][col] == 1) {
                        g2d.fillRect(
                            startX + col * cellSize,
                            startY + row * cellSize,
                            cellSize,
                            cellSize
                        );
                    }
                }
            }
        }
        
        // 테두리
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, width - 1, height - 1);
    }
}
