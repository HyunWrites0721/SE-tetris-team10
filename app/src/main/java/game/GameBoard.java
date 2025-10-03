package game;

import java.awt.*;
import javax.swing.JPanel;

public class GameBoard extends JPanel {

    public double scale = 1.0;
    public int ROWS = 20;
    public int COLS = 10;
    public int CELL_SIZE = 30;
    public int MARGIN = 2;

    public int NEXT_ROWS = 4;
    public int NEXT_COLS = 6;
    public int NEXT_MARGIN = 2;

    public int FONT_SIZE = 48;
    public int STROKE_WIDTH = 3;


    public void convertScale(double scale) {
        this.scale = scale;
        // 격자 개수는 고정, 셀 크기와 기타 요소들만 스케일링
        // ROWS = 20; // 격자 행 개수 고정
        // COLS = 10; // 격자 열 개수 고정
        CELL_SIZE = (int)(30 * scale);
        MARGIN = (int)(2 * scale);
        
        // NEXT_ROWS = (int)(4 * scale);
        // NEXT_COLS = (int)(6 * scale);
        NEXT_MARGIN = (int)(2 * scale);
        
        FONT_SIZE = (int)(48 * scale);
        STROKE_WIDTH = (int)(3 * scale);
        
        repaint();
    }
    


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int boardWidth = COLS * CELL_SIZE;
        int boardHeight = ROWS * CELL_SIZE;
        int x = (getWidth() - boardWidth) / 3;
        int y = MARGIN * CELL_SIZE;

        int nextWidth = NEXT_COLS * CELL_SIZE;
        int nextHeight = NEXT_ROWS * CELL_SIZE;



        // "NEXT" 텍스트 그리기
        g.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
        g.setColor(Color.BLACK);
        String nextText = "NEXT";
        FontMetrics fm = g.getFontMetrics();
        int textX = x + boardWidth + (nextWidth - fm.stringWidth(nextText)) / 2;
        int textY = y + (NEXT_MARGIN * CELL_SIZE - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(nextText, textX, textY);

        // Graphics2D로 변환
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(new BasicStroke(STROKE_WIDTH * 0.67f)); // 격자용 얇은 선

        // 투명도 설정
        float alpha = 0.7f;
        float beta = 0.2f; // 투명도(0.0f~1.0f, 값이 낮을수록 더 투명)
        // 배경 투명도 설정
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // 배경 그리기
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                g2d.setColor(new Color(255,248,235));
                g2d.fillRect(x + col * CELL_SIZE, y + row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
        // 넥스트 배경 그리기
        for (int row = NEXT_MARGIN; row < NEXT_ROWS + NEXT_MARGIN; row++) {
            for (int col = COLS; col < COLS + NEXT_COLS; col++) {
                g2d.setColor(new Color(255,248,235));
                g2d.fillRect(x + col * CELL_SIZE, y + row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
        // 격자 무늬 그리기
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, beta));
        g2d.setStroke(new BasicStroke(STROKE_WIDTH));
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                g2d.setColor(new Color(0,0,0,100));
                g2d.drawRect(x + col * CELL_SIZE, y + row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        // 넥스트 격자 무늬 그리기
        for (int row = NEXT_MARGIN; row < NEXT_ROWS + NEXT_MARGIN; row++) {
            for (int col = COLS; col < COLS + NEXT_COLS; col++) {
                g2d.setColor(new Color(0,0,0,100));
                g2d.drawRect(x + col * CELL_SIZE, y + row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
        // 테두리 설정
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // 불투명도 원래대로
        g2d.setStroke(new BasicStroke(STROKE_WIDTH));

        // 테두리 그리기 (윗쪽 테두리 제외)
        g2d.setColor(Color.BLACK);
        // 좌측 테두리
        g2d.drawLine(x, y, x, y + boardHeight);
        // 우측 테두리
        g2d.drawLine(x + boardWidth, y, x + boardWidth, y + boardHeight);
        // 하단 테두리
        g2d.drawLine(x, y + boardHeight, x + boardWidth, y + boardHeight);

        // 넥스트 테두리 그리기
        g2d.drawRect(x + boardWidth, y + NEXT_MARGIN * CELL_SIZE, nextWidth, nextHeight); // 다음 블록 미리보기 테두리
        g2d.drawRect(x + boardWidth, NEXT_MARGIN * CELL_SIZE, nextWidth, NEXT_MARGIN * CELL_SIZE); // 다음 블록 미리보기 테두리 상단 라인

        // Graphics2D 객체 해제
        g2d.dispose();

    }

}
