package game;

import javax.swing.*;

import blocks.Block;

import java.awt.*;

public class GameModel extends JPanel {

    private final GameView gameBoard;
    private final int ROWS = 23;
    private final int COLS = 12;

    private final int INNER_TOP = 2;
    private final int INNER_BOTTOM = ROWS - 2;
    private final int INNER_LEFT = 1;
    private final int INNER_RIGHT = COLS - 2;

    private final char TEXT = 'O';

    private GameTimer gameTimer;

    public int[][] board ;   // 게임 보드를 나타내는 2차원 배열
    protected Block currentBlock;  // 플레이어가 현재 조작하는 블록
    private Block nextBlock;


    public int[][] boardArray = new int[ROWS][COLS];



    public GameModel(GameView gameBoard) {
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

    

    public Block getCurrentBlock(){  // 현재블록 getter
        return currentBlock;
    }

    public int[][] getBoard(){  // 게임보드 getter
        return board;
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
    

    // 하드 드롭
    public void HardDrop() {
        if (currentBlock != null) {  // 현재 블록이 있는지 확인
           /* while (!checkCollision(0,1)){  // y로 한 칸 내려갔을때 충돌이 일어나지 않으면 (while문이라서 충돌이 일어나기 전까지 계속 내려감)
                currentBlock.setPosition(currentBlock.getX(), currentBlock.getY()+1);  // y좌표 +1 을 해 한 칸 내려 새로운 위치를 설정
            } */ 
            currentBlock.hardDrop(board);
            placePiece();  // 블록을 보드에 고정
            spawnNewBlock();  // 그리고 다음 블록을 생성
        //  repaint();
        }
    }

    // 블록을 보드에 고정시키는 역할
    protected void placePiece() {
        int[][] shape = currentBlock.getShape();
        int x = currentBlock.getX();
        int y = currentBlock.getY();

        for (int row = 0 ; row < shape.length ; row++){
            for (int col = 0 ; col < shape[row].length ; col++){
                if (shape[row][col] != 0){
                    board[y + row] [x + col] = 1;
                }
            }
        }
        // checkLines();  // 줄이 다 찼는지 확인
    }

    protected void spawnNewBlock(){
        if (nextBlock == null) {  
            nextBlock = Block.spawn();  // 이 부근 즈음에 nextBlockFrame이랑 연결시키면 되지 않을까?
        }  // 다음 블록 없으면 생성
        currentBlock = nextBlock;  // 생성한 nextBlock을 currentBlock으로 설정
        nextBlock = Block.spawn(); // 새로운 nextBlock 생성
    }

    protected boolean canMoveto(int targetRow, int targetCol, int[][] shape){
        for (int row = 0 ; row < shape.length ; row++){
            for (int col = 0 ; col < shape[row].length ; col++){
                if (shape[row][col] == 0) 
                    continue;    // 블록에서 0인 부분은 검사하지 않아도 됨.
                if (targetRow + row < 2 || targetRow + row >= ROWS-1)  // 윗 2줄 숨겨진 공간 || 바닥 아래 1줄이상  벗어나는지 확인 
                    return false;
                if (targetCol + col < 1 || targetCol + col >= COLS-1) // 좌 1줄 숨겨진 공간 || 우 1줄 숨겨진 공간 벗어나는지 확인
                    return false;
                if(board[targetRow + row][targetCol + col] == 1) // 보드에 이미 다른 칸이 채워져 있는지 검사
                    return false;
            }
        }
        return true;
    }

    protected boolean canRotate(){
    // 기본 회전 시도
    if (canMoveto(currentBlock.getY(), currentBlock.getX(), currentBlock.Rotateshape())) {
        return true;
    }
    
    // 왼쪽으로 한 칸 이동 후 회전 시도
    if (canMoveto(currentBlock.getY(), currentBlock.getX() - 1, currentBlock.Rotateshape())) {
        currentBlock.setPosition(currentBlock.getX() - 1, currentBlock.getY());
        return true;
    }
    
    // 오른쪽으로 한 칸 이동 후 회전 시도
    if (canMoveto(currentBlock.getY(), currentBlock.getX() + 1, currentBlock.Rotateshape())) {
        currentBlock.setPosition(currentBlock.getX() + 1, currentBlock.getY());
        return true;
    }
    
    return false;
   
    }

    public void Rotate90() {
        // if(isGameOver() == true) return;
        if(canRotate() == true){
            currentBlock.getRotatedShape();
            repaint();
        }
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