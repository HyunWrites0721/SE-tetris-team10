package game;

import java.awt.*;

import javax.swing.JPanel;


import blocks.Block;

public class GameView extends JPanel {

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

    private int x,y;

    // 현재 화면에 렌더링할 떨어지는 블록
    private Block fallingBlock;
    private GameModel gameModel;
    private FrameBoard frameBoard;

    // 타이머/모델에서 현재 블록을 전달받아 저장하고 즉시 리페인트
    public void setFallingBlock(Block block) {
        this.fallingBlock = block;
        repaint();
    }

    // GameModel을 바인딩하여 쌓인 블록(boardArray)을 그릴 수 있게 함
    public void setGameModel(GameModel model) {
        this.gameModel = model;
        repaint();
    }

    public void convertScale(double scale) {
        this.scale = scale;
        CELL_SIZE = (int)(30 * scale);
        MARGIN = (int)(2 * scale);
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
        x = (getWidth() - boardWidth) / 3;
        y = MARGIN * CELL_SIZE;

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

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(new BasicStroke(STROKE_WIDTH * 0.67f)); // 격자용 얇은 선

        // 투명도 설정
        float alpha = 0.7f;
        float beta = 0.2f;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // 배경 그리기
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                g2d.setColor(new Color(255, 248, 235));
                g2d.fillRect(x + col * CELL_SIZE, y + row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        // 넥스트 배경 그리기
        for (int row = NEXT_MARGIN; row < NEXT_ROWS + NEXT_MARGIN; row++) {
            for (int col = COLS; col < COLS + NEXT_COLS; col++) {
                g2d.setColor(new Color(255, 248, 235));
                g2d.fillRect(x + col * CELL_SIZE, y + row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        // 격자 무늬 그리기
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, beta));
        g2d.setStroke(new BasicStroke(STROKE_WIDTH));
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawRect(x + col * CELL_SIZE, y + row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        // 넥스트 격자 무늬 그리기
        for (int row = NEXT_MARGIN; row < NEXT_ROWS + NEXT_MARGIN; row++) {
            for (int col = COLS; col < COLS + NEXT_COLS; col++) {
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawRect(x + col * CELL_SIZE, y + row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        // 테두리 설정
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setStroke(new BasicStroke(STROKE_WIDTH));

        // 보드 테두리 (윗쪽 제외)
        g2d.setColor(Color.BLACK);
        g2d.drawLine(x, y, x, y + boardHeight);                 // 좌측
        g2d.drawLine(x + boardWidth, y, x + boardWidth, y + boardHeight); // 우측
        g2d.drawLine(x, y + boardHeight, x + boardWidth, y + boardHeight); // 하단

        // 넥스트 테두리
        g2d.drawRect(x + boardWidth, y + NEXT_MARGIN * CELL_SIZE, nextWidth, nextHeight);
        g2d.drawRect(x + boardWidth, NEXT_MARGIN * CELL_SIZE, nextWidth, NEXT_MARGIN * CELL_SIZE);


    


        // 먼저 쌓인 블록을 그린 후 (각 셀의 고유 색상 사용)
            if (gameModel != null && gameModel.getBoard() != null) {
                stackBlock(g2d, gameModel.getBoard(), gameModel.getColorBoard());
            }
        // 그 위에 현재 떨어지는 블록을 그림
             if (fallingBlock != null) {
            
                drawBlock(g2d, fallingBlock);
            }

        g2d.dispose();
    }

    public void drawBlock(Graphics g, Block block) {
        
        int positionX = block.getX();
        int positionY = block.getY();
        Color color = block.getColor();
        int[][] shape = block.getShape();

        g.setColor(color);
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != 0) {
                    int drawX = x + (positionX - 1 + col) * CELL_SIZE;
                    int drawY = y + (positionY - 2 + row) * CELL_SIZE;

                    g.fillRect(drawX, drawY, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    // 외부에서 보드 상태 변경(예: lineClear) 후 호출하여 전체 보드를 다시 그리기
    public void repaintBlock() {
        repaint();
    }
    

    public void stackBlock(Graphics g, int[][] boardArray, int[][] colorBoard) {
        if (boardArray == null || boardArray.length == 0) return;

        // GameModel 보드(예: 23x12): 상단 2줄 숨김, 좌우 1칸 벽, 하단 1줄 벽
        int rows = boardArray.length;      // 23
        int cols = boardArray[0].length;   // 12
        int innerTop = 2;                  // 보이는 영역 시작 행
        int innerBottom = rows - 2;        // 보이는 영역 끝 행(포함)
        int innerLeft = 1;                 // 보이는 영역 시작 열
        int innerRight = cols - 2;         // 보이는 영역 끝 열(포함)

        for (int row = innerTop; row <= innerBottom; row++) {
            for (int col = innerLeft; col <= innerRight; col++) {
                if (boardArray[row][col] != 0) {
                    int rgb = (colorBoard != null) ? colorBoard[row][col] : 0;
                    if (rgb == 0) {
                        // 색 정보가 없으면 기본 회색
                        g.setColor(new Color(100, 100, 100));
                    } else {
                        g.setColor(new Color(rgb, true));
                    }
                    int visibleCol = col - innerLeft; // 0..9
                    int visibleRow = row - innerTop;  // 0..19
                    int drawX = x + visibleCol * CELL_SIZE;
                    int drawY = y + visibleRow * CELL_SIZE;
                    g.fillRect(drawX, drawY, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }




}
