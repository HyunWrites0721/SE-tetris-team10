package game;

import javax.swing.*;
import java.awt.*;

public class BlockText extends JPanel {

    private final GameBoard gameBoard;
    private final int ROWS = 23;
    private final int COLS = 12;

    private final int INNER_TOP = 2;
    private final int INNER_BOTTOM = ROWS - 2;
    private final int INNER_LEFT = 1;
    private final int INNER_RIGHT = COLS - 2;

    private final char TEXT = 'O';



    public int[][] boardArray = new int[ROWS][COLS];


    public BlockText(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    setOpaque(false);
    setVisible(true);
        // 생성 시 왼쪽, 오른쪽, 아랫벽을 true로 초기화
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (j == 0 || j == COLS - 1 || i == ROWS - 1) {
                    boardArray[i][j] = 1; // 벽
                } else {
                    boardArray[i][j] = 0;
                }
            }
        }
        repaint();
    }

    public void setBlockText(int row, int col) {
        if (isValidPosition(row, col)) {
            boardArray[row][col] = 1;
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
        // 내부 그리기 범위: rows 2..ROWS-2 (2..21), cols 1..COLS-2 (1..10)
        for (int row = 0; row <= INNER_BOTTOM; row++) {
            for (int col = INNER_LEFT; col <= INNER_RIGHT; col++) {
                if (boardArray[row][col] == 1) {
                    int x = xOffset + (col - INNER_LEFT) * cellSize + (cellSize - fm.charWidth(TEXT)) / 2;
                    int y = yOffset + (row - INNER_TOP) * cellSize + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
                    g.setColor(Color.BLACK); // O를 더 잘 보이게
                    g.drawString(String.valueOf(TEXT), x, y);
                }
            }
        }
    }


    public void boardInit() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (j == 0 || j == COLS - 1 || i == ROWS - 1) {
                    boardArray[i][j] = 1; // 벽
                } else {
                    boardArray[i][j] = 0;
                }
            }
        }


        repaint();
    }
    



    public boolean isValidPosition(int row, int col) {
        if (boardArray[row][col] == 1) {
            return false;
        }
        else if (boardArray[row][col] == 0) {
            return true;
        }
        return row >= 0 && row < ROWS-1 && col >= 1 && col < COLS-1;
    }


    public void oneLineClear(int row) {

            for(int col = 1; col < COLS-1; col++) {
                if(boardArray[row][col] == 0) {
                    break;
                }
                else if (col == COLS - 2 && boardArray[row][col] == 1) {
                    // 해당 행이 모두 1인 경우
                    // 위의 모든 행을 한 칸씩 아래로 이동
                    for (int r = row; r > 0; r--) {
                        System.arraycopy(boardArray[r - 1], 0, boardArray[r], 0, COLS);
                    }
                    // 최상단 행은 모두 0으로 초기화
                    for (int c = 0; c < COLS; c++) {
                        boardArray[0][c] = 0;
                    }
                    boardArray[0][0] = 1; // 왼쪽 벽
                    boardArray[0][COLS - 1] = 1; // 오른쪽 벽
                    repaint();

                } 
            }

        }

    public boolean isGameOver() {
        // 상단 감지 영역: row 0..(INNER_TOP-1), col 3..7 에 1이 있으면 게임 오버
        for (int row = 0; row < INNER_TOP; row++) {
            for (int col = 3; col < 8; col++) {
                if (boardArray[row][col] == 1) {
                    return true;


                }
            }
        }
        return false;
 
    }
    


}