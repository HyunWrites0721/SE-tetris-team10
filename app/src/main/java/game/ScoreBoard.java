package game;

import javax.swing.*;
import java.awt.*;

public class ScoreBoard extends JPanel {
    private int scoreWidth;
    private int scoreHeight;
    private int cellSize;
    private int margin;
    private int fontSize;
    private int strokeWidth;
    private int score;

    public ScoreBoard() {
        setOpaque(false);
        convertScale(1.0); // 기본 스케일로 초기화
        score = 0;
    }

    public void setScore(int score) {
        this.score = score;
        repaint();
    }

    public void convertScale(double scale) {
        // 기본 셀 크기 계산
        cellSize = (int)(30 * scale);
        
        // NEXT 패널과 동일한 너비 사용하되, screenRatio 적용
        scoreWidth = (int)(6 * cellSize * start.StartFrame.screenRatio);
        scoreHeight = (int)(4 * cellSize * start.StartFrame.screenRatio);
        
        // 여백 크기 조정
        margin = (int)(2 * scale * start.StartFrame.screenRatio);
        
        // 폰트 크기를 패널 크기에 맞게 조정
        fontSize = (int)(Math.min(scoreHeight/2, scoreWidth/5));
        
        // 선 두께도 screenRatio에 맞춰 조정하되 최대값 제한
        strokeWidth = Math.min(3, Math.max(1, (int)(2 * start.StartFrame.screenRatio)));
        
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // 안티앨리어싱 설정
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 격자용 얇은 선 설정
        g2d.setStroke(new BasicStroke(strokeWidth * 0.67f));

        // 투명도 설정
        float alpha = 0.7f;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // SCORE 텍스트 배경
        g2d.setColor(new Color(255, 248, 235));
        g2d.fillRect(0, 0, scoreWidth, margin * cellSize);

        // 점수 표시 영역 배경
        g2d.fillRect(0, margin * cellSize, scoreWidth, scoreHeight - margin * cellSize);

        // 투명도 복구 및 테두리 설정
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.setColor(Color.BLACK);

        // 전체 외곽 테두리
        g2d.drawRect(0, 0, scoreWidth, scoreHeight);
        
        // SCORE 영역 구분선
        g2d.drawLine(0, margin * cellSize, scoreWidth, margin * cellSize);

        // "SCORE" 텍스트 그리기
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        String scoreText = "SCORE";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (scoreWidth - fm.stringWidth(scoreText)) / 2;
        int textY = (margin * cellSize - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(scoreText, textX, textY);

        // 점수가 0이 아닐 때만 표시
        if (score > 0) {
            String scoreString = String.valueOf(score);
            int scoreX = (scoreWidth - fm.stringWidth(scoreString)) / 2;
            int scoreY = margin * cellSize + (scoreHeight - margin * cellSize + fm.getHeight()) / 2;
            g2d.drawString(scoreString, scoreX, scoreY);
        }
        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(scoreWidth, scoreHeight);
    }
}