package game;

import javax.swing.*;
import java.awt.*;

public class BlockText extends JPanel {

    private final GameBoard gameBoard;
    private final int ROWS = 20;
    private final int COLS = 10;

    private final String TEXT = "O";

    public boolean[][] boardArray = new boolean[ROWS][COLS];


    public BlockText(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    setOpaque(false);
    setVisible(true);
        // 생성 시 모든 칸을 비움 (텍스트 안 보이게)
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                boardArray[i][j] = false;
            }
        }
        repaint();
    }
    public void setBlockText(int row, int col) {
        if (isValidPosition(row, col)) {
            boardArray[row][col] = true;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!isVisible()) return;
        int cellSize = gameBoard.CELL_SIZE;
        // GameBoard의 x, y 오프셋과 동일하게 적용
        int xOffset = (gameBoard.getWidth() - gameBoard.COLS * cellSize) / 3;
        int yOffset = gameBoard.MARGIN * cellSize;
        g.setFont(new Font("Monospaced", Font.BOLD, cellSize - 2));
        FontMetrics fm = g.getFontMetrics();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (boardArray[row][col]) {
                    int x = xOffset + col * cellSize + (cellSize - fm.charWidth(TEXT.charAt(0))) / 2;
                    int y = yOffset + row * cellSize + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
                    g.drawString(TEXT, x, y);
                }
            }
        }
    }


    public void boardInit() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                boardArray[i][j] = false;

            }
        }
        repaint();
    }



    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }


    public void oneLineClear(int row) {

            for(int col = 0; col < COLS; col++) {
                if(boardArray[row][col] == false) {
                    break;
                }
                else if (col == COLS - 1 && boardArray[row][col] == true) {
                    // 해당 행이 모두 false인 경우
                    // 위의 모든 행을 한 칸씩 아래로 이동
                    for (int r = row; r > 0; r--) {
                        System.arraycopy(boardArray[r - 1], 0, boardArray[r], 0, COLS);
                    }
                    // 최상단 행은 모두 false로 초기화
                    for (int c = 0; c < COLS; c++) {
                        boardArray[0][c] = false;
                    }
                    repaint();

                } 
            }

        }
    


}