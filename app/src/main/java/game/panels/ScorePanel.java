package game.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

/**
 * 점수 표시 패널
 */
public class ScorePanel extends JPanel {
    
    private static final int ROWS = 4;
    private static final int COLS = 6;
    private static final int HEADER_ROWS = 1;  // "SCORE" 텍스트 영역
    
    private int cellSize = 30;
    private int fontSize = 24;
    
    private int score = 0;
    
    public ScorePanel() {
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
     * 점수 설정
     */
    public void setScore(int score) {
        this.score = score;
        repaint();
    }
    
    /**
     * 점수 조회
     */
    public int getScore() {
        return score;
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
        
        // "SCORE" 텍스트
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        g2d.setColor(Color.BLACK);
        String text = "SCORE";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (width - fm.stringWidth(text)) / 2;
        int textY = (headerHeight - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, textX, textY);
        
        // 헤더 구분선
        g2d.drawLine(0, headerHeight, width, headerHeight);
        
        // 점수 표시
        String scoreText = String.valueOf(score);
        int scoreX = (width - fm.stringWidth(scoreText)) / 2;
        int scoreY = headerHeight + (height - headerHeight) / 2 + fm.getAscent() / 2;
        g2d.drawString(scoreText, scoreX, scoreY);
        
        // 테두리
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, width - 1, height - 1);
    }
}
