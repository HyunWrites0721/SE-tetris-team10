package game;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import blocks.Block;

public class GameView extends JPanel {

    public double scale = 1.0;
    public int ROWS = 20;
    public int COLS = 10;
    public int BASE_CELL_SIZE = 30;  // 기본 셀 크기
    public int CELL_SIZE = 30;       // 실제 적용될 셀 크기
    public int MARGIN = 2;

    public int NEXT_ROWS = 6;  // NEXT 영역 높이를 6칸으로 통일
    public int NEXT_COLS = 6;  // NEXT 영역 너비를 6칸으로 통일
    public int NEXT_MARGIN = 2;
    
    // 최대 프레임 크기 (600x600 기준)
    private static final int MAX_FRAME_WIDTH = 600;
    private static final int MAX_FRAME_HEIGHT = 600;

    public int BASE_FONT_SIZE = 24;  // 기본 폰트 크기
    public int FONT_SIZE = 24;       // 실제 적용될 폰트 크기
    public int STROKE_WIDTH = 3;

    private int x,y;

    // 현재 화면에 렌더링할 떨어지는 블록
    private Block fallingBlock;
    private Block nextBlock;  // 다음 블록 저장
    private GameModel gameModel;
    private FrameBoard frameBoard;

    // 타이머/모델에서 현재 블록을 전달받아 저장하고 즉시 리페인트
    public void setFallingBlock(Block block) {
        this.fallingBlock = block;
        repaint();
    }

    // 다음 블록을 설정하는 메서드
    public void setNextBlock(Block block) {
        this.nextBlock = block;
        repaint();
    }

    // GameModel을 바인딩하여 쌓인 블록(boardArray)을 그릴 수 있게 함
    public void setGameModel(GameModel model) {
        this.gameModel = model;
        repaint();
    }

    public void convertScale(double scale) {
        this.scale = scale;
        
        // 화면 크기에 맞는 최적의 셀 크기 계산
        int maxBoardWidth = (int)(MAX_FRAME_WIDTH * scale * 0.7);  // 전체 화면의 70%를 게임판에 할당
        int maxBoardHeight = (int)(MAX_FRAME_HEIGHT * scale * 0.95); // 전체 화면의 95%를 높이로 사용
        
        // 너비와 높이 중 더 제한적인 요소를 기준으로 셀 크기 계산
        int cellByWidth = maxBoardWidth / (COLS + NEXT_COLS + 2); // +2는 여백용
        int cellByHeight = maxBoardHeight / (ROWS + 1); // +1은 여백용
        
        // 더 작은 값을 선택하여 셀 크기 결정
        CELL_SIZE = Math.min(cellByWidth, cellByHeight);
        
        // 최소 셀 크기 보장 (너무 작아지지 않도록)
        CELL_SIZE = Math.max(CELL_SIZE, 15);
        
    // 나머지 크기들도 screenRatio에 맞춰 조정 (screenRatio 미설정 대비 안전값 적용)
    MARGIN = Math.max(1, CELL_SIZE / 15);
    NEXT_MARGIN = MARGIN;
    double ratio = (start.StartFrame.screenRatio > 0) ? start.StartFrame.screenRatio : 1.2;
    FONT_SIZE = Math.max(12, (int)(BASE_FONT_SIZE * ratio));
        STROKE_WIDTH = Math.max(1, CELL_SIZE / 10);
        
        // 상위 프레임의 크기도 함께 조절
        if (getParent() != null && getParent().getParent() instanceof FrameBoard) {
            FrameBoard frameBoard = (FrameBoard) getParent().getParent();
            frameBoard.updateFrameSize(scale);
        }
        
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int boardWidth = COLS * CELL_SIZE;
        int boardHeight = ROWS * CELL_SIZE;
        // 왼쪽 여백을 4로 나누어 더 많은 공간을 우측에 확보
        x = (getWidth() - boardWidth) / 4;
        y = MARGIN * CELL_SIZE;

        int nextWidth = NEXT_COLS * CELL_SIZE;
        int nextHeight = NEXT_ROWS * CELL_SIZE;

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
    // NEXT 텍스트 영역 테두리 (NEXT 영역 상단 헤더)
    g2d.drawRect(x + boardWidth, y, nextWidth, NEXT_MARGIN * CELL_SIZE);

    // "NEXT" 텍스트를 모든 배경/격자 위에 그리기 (덮이지 않도록 순서 조정)
    g.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
    g.setColor(Color.BLACK);
    String nextText = "NEXT";
    FontMetrics fm = g.getFontMetrics();
    int textX = x + boardWidth + (nextWidth - fm.stringWidth(nextText)) / 2;
    int textY = y + (NEXT_MARGIN * CELL_SIZE - fm.getHeight()) / 2 + fm.getAscent();
    g.drawString(nextText, textX, textY);

        // Next 블록 그리기
        if (nextBlock != null) {
            int[][] shape = nextBlock.getShape();
            Color color = nextBlock.getColor();
            
            // Next 영역의 중앙에 블록 그리기
            int blockWidth = shape[0].length * CELL_SIZE;
            int blockHeight = shape.length * CELL_SIZE;
            int startX = x + boardWidth + (nextWidth - blockWidth) / 2;
            int startY = y + NEXT_MARGIN * CELL_SIZE + (nextHeight - blockHeight) / 2;
            
            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[row].length; col++) {
                    int v = shape[row][col];
                    if (v != 0) {
                        int cx = startX + col * CELL_SIZE;
                        int cy = startY + row * CELL_SIZE;
                        // 원래 블록 색으로 채운다
                        g2d.setColor(color);
                        g2d.fillRect(cx, cy, CELL_SIZE, CELL_SIZE);
                        // v==4 인 경우 텍스트 'L'을 오버레이
                        if (v == 4) {
                            drawLText(g2d, cx, cy, CELL_SIZE);
                        } else if (v == 5) {
                            drawTwoText(g2d, cx, cy, CELL_SIZE);
                        }
                    }
                }
            }
        }

    


        // 먼저 쌓인 블록을 그린 후 (각 셀의 고유 색상 사용)
            if (gameModel != null && gameModel.getBoard() != null) {
                stackBlock(g2d, gameModel.getBoard(), gameModel.getColorBoard());
            }
        // 그 위에 현재 떨어지는 블록을 그림
             if (fallingBlock != null) {
            
                drawBlock(g2d, fallingBlock);
            }

        // 아이템 애니메이션 오버레이를 마지막에 그림 (블록 위에 덮어쓰기)
        if (gameModel != null) {
            // AllClear: 보드 전체를 검게 플래시
            if (gameModel.isAllClearAnimating() && gameModel.isAllClearFlashBlack()) {
                Graphics2D overlay = (Graphics2D) g2d.create();
                try {
                    overlay.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
                    overlay.setColor(Color.BLACK);
                    overlay.fillRect(x, y, boardWidth, boardHeight);
                } finally {
                    overlay.dispose();
                }
            }

            // BoxClear: 각 5x5 폭발 영역을 검게 플래시
            if (gameModel.isBoxClearAnimating() && gameModel.isBoxFlashBlack()) {
                int rows = gameModel.getBoard().length;     // 23
                int cols = gameModel.getBoard()[0].length;  // 12
                int innerTop = 2;
                int innerBottom = rows - 2;
                int innerLeft = 1;
                int innerRight = cols - 2;

                g2d.setColor(Color.BLACK);
                for (int row = innerTop; row <= innerBottom; row++) {
                    for (int col = innerLeft; col <= innerRight; col++) {
                        if (gameModel.isCellInBoxFlash(row, col)) {
                            int visibleCol = col - innerLeft; // 0..9
                            int visibleRow = row - innerTop;  // 0..19
                            int drawX = x + visibleCol * CELL_SIZE;
                            int drawY = y + visibleRow * CELL_SIZE;
                            g2d.fillRect(drawX, drawY, CELL_SIZE, CELL_SIZE);
                        }
                    }
                }
            }
        }

        g2d.dispose();
    }

    public void drawBlock(Graphics g, Block block) {
        
        int positionX = block.getX();
        int positionY = block.getY();
        Color color = block.getColor();
        int[][] shape = block.getShape();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                int v = shape[row][col];
                if (v != 0) {
                    int drawX = x + (positionX - 1 + col) * CELL_SIZE;
                    int drawY = y + (positionY - 2 + row) * CELL_SIZE;

                    // 원래 블록 색으로 채운다
                    g.setColor(color);
                    g.fillRect(drawX, drawY, CELL_SIZE, CELL_SIZE);
                    // v==4 인 경우 텍스트 'L', v==5 인 경우 텍스트 '2'를 오버레이
                    if (g instanceof Graphics2D) {
                        Graphics2D g2 = (Graphics2D) g;
                        if (v == 4) {
                            drawLText(g2, drawX, drawY, CELL_SIZE);
                        } else if (v == 5) {
                            drawTwoText(g2, drawX, drawY, CELL_SIZE);
                        }
                    }
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
                int v = boardArray[row][col];
                if (v == 0) continue;

                int visibleCol = col - innerLeft; // 0..9
                int visibleRow = row - innerTop;  // 0..19
                int drawX = x + visibleCol * CELL_SIZE;
                int drawY = y + visibleRow * CELL_SIZE;

                // 라인 클리어 플래시(검은색) 표시: 해당 행이 플래시 대상이고 현재 블랙 단계라면 검은색으로 채우고 다음 셀로
                if (gameModel != null && gameModel.isLineClearAnimating() && gameModel.isFlashBlack() && gameModel.isRowFlashing(row)) {
                    g.setColor(Color.BLACK);
                    g.fillRect(drawX, drawY, CELL_SIZE, CELL_SIZE);
                    continue;
                }

                int rgb = (colorBoard != null) ? colorBoard[row][col] : 0;
                if (rgb == 0) {
                    // 색 정보가 없으면 기본 회색
                    g.setColor(new Color(100, 100, 100));
                } else {
                    g.setColor(new Color(rgb, true));
                }
                g.fillRect(drawX, drawY, CELL_SIZE, CELL_SIZE);
                // v==4 인 경우 텍스트 'L', v==5 인 경우 텍스트 '2'를 오버레이
                if (g instanceof Graphics2D) {
                    Graphics2D g2 = (Graphics2D) g;
                    if (v == 4) {
                        drawLText(g2, drawX, drawY, CELL_SIZE);
                    } else if (v == 5) {
                        drawTwoText(g2, drawX, drawY, CELL_SIZE);
                    }
                }
            }
        }
    }


    // 보조: OneLineClearBlock의 특수 셀(v==4)에 L마크 오버레이를 그린다 (두께 약 2px)
    // 왼쪽 변(세로) + 아래 변(가로)을 채운 사각형으로 표시
    // 보조: v==4 셀에 원래 색을 유지한 채 중앙에 'L' 텍스트를 표시
    private void drawLText(Graphics2D g2d, int cellX, int cellY, int cellSize) {
        drawItemText(g2d, "L", cellX, cellY, cellSize);
    }

    // 보조: v==5 셀에 중앙에 '2' 텍스트를 표시
    private void drawTwoText(Graphics2D g2d, int cellX, int cellY, int cellSize) {
        drawItemText(g2d, "2", cellX, cellY, cellSize);
    }

    // 공통: 셀 중앙에 굵은 텍스트를 표시
    private void drawItemText(Graphics2D g2d, String s, int cellX, int cellY, int cellSize) {
        Color prevColor = g2d.getColor();
        Font prevFont = g2d.getFont();
        try {
            int fs = Math.max(12, (int)(cellSize * 0.75));
            g2d.setFont(new Font("Arial", Font.BOLD, fs));
            g2d.setColor(Color.BLACK);
            FontMetrics fm = g2d.getFontMetrics();
            int tx = cellX + (cellSize - fm.stringWidth(s)) / 2;
            int ty = cellY + (cellSize - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(s, tx, ty);
        } finally {
            g2d.setColor(prevColor);
            g2d.setFont(prevFont);
        }
    }

}
