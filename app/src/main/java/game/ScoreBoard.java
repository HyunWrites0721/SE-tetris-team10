package game;

import javax.swing.*;
import java.awt.*;

public class ScoreBoard extends JPanel {
    private int scoreWidth;
    private int scoreHeight;
    private int cellSize;
    private int strokeWidth;
    private int score;
    
    // GameView와 동일한 방식으로 기본 폰트 크기와 실제 폰트 크기 분리
    public int BASE_FONT_SIZE = 24;  // 기본 폰트 크기
    public int FONT_SIZE = 24;       // 실제 적용될 폰트 크기

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
        // SettingModel에서 화면 크기 설정을 읽어와서 적절한 비율 적용
        double actualScreenRatio = start.StartFrame.screenRatio; // StartFrame에서 설정된 값 사용
        
        // 기본 셀 크기 계산 
        cellSize = (int)(30 * scale);
        
        // NEXT 패널과 동일한 너비 사용하되, 실제 screenRatio 적용
        scoreWidth = (int)(6 * cellSize * actualScreenRatio);
        scoreHeight = (int)(4 * cellSize * actualScreenRatio);
        
        // GameView와 동일한 방식으로 폰트 크기 계산 
        FONT_SIZE = (int)(BASE_FONT_SIZE * actualScreenRatio);
        
        // 선 두께를 실제 screenRatio에 맞춰 조정
        strokeWidth = Math.max(1, (int)(2 * scale * actualScreenRatio));
        
        // 디버깅용 로그
        System.out.println("ScoreBoard - scale: " + scale + ", screenRatio: " + actualScreenRatio + 
                          ", FONT_SIZE: " + FONT_SIZE + ", width: " + scoreWidth + ", height: " + scoreHeight);
        
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // 실제 컴포넌트 크기 사용 (setBounds로 설정된 실제 크기)
        int actualWidth = getWidth();
        int actualHeight = getHeight();
        
        // 실제 크기가 0이면 계산된 크기 사용
        if (actualWidth <= 0) actualWidth = scoreWidth;
        if (actualHeight <= 0) actualHeight = scoreHeight;
        
        // 안티앨리어싱 설정
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 헤더 높이를 실제 크기 기준으로 계산
        int headerHeight = actualHeight / 2; // 실제 높이의 절반
        
        // 격자용 얇은 선 설정
        g2d.setStroke(new BasicStroke(strokeWidth * 0.67f));

        // 투명도 설정
        float alpha = 0.7f;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // SCORE 텍스트 배경 (NEXT 패널과 동일한 스타일)
        g2d.setColor(new Color(255, 248, 235));
        g2d.fillRect(0, 0, actualWidth, headerHeight);

        // 점수 표시 영역 배경
        g2d.fillRect(0, headerHeight, actualWidth, actualHeight - headerHeight);

        // 투명도 복구 및 굵은 테두리 설정 (NEXT 패널과 동일)
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.setColor(Color.BLACK);

        // 전체 외곽 테두리 (굵은 선)
        g2d.drawRect(0, 0, actualWidth, actualHeight);
        
        // SCORE 영역 구분선 (NEXT 패널 스타일과 동일)
        g2d.drawRect(0, 0, actualWidth, headerHeight);

        // "SCORE" 텍스트 그리기 (GameView와 동일한 방식으로 FONT_SIZE 사용)
        g2d.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
        String scoreText = "SCORE";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (actualWidth - fm.stringWidth(scoreText)) / 2;
        int textY = (headerHeight - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(scoreText, textX, textY);

        // 점수 표시 (중앙 정렬)
        String scoreString = String.valueOf(score);
        int scoreX = (actualWidth - fm.stringWidth(scoreString)) / 2;
        int scoreAreaHeight = actualHeight - headerHeight;
        int scoreY = headerHeight + (scoreAreaHeight - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(scoreString, scoreX, scoreY);
        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        // 실제 크기가 설정되어 있으면 그것을 사용, 아니면 계산된 크기 사용
        int actualWidth = getWidth();
        int actualHeight = getHeight();
        
        if (actualWidth > 0 && actualHeight > 0) {
            return new Dimension(actualWidth, actualHeight);
        }
        return new Dimension(scoreWidth, scoreHeight);
    }
}