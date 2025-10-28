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
    private GameTimer gameTimer;

    public int[][] board ;   // 게임 보드를 나타내는 2차원 배열
    protected Block currentBlock;  // 플레이어가 현재 조작하는 블록
    private Block nextBlock;
    // 쌓인 블록의 색상을 보관 (ARGB). 0이면 비어있음
    private int[][] colorBoard;


    public int[][] boardArray = new int[ROWS][COLS];

    private int totalLinesCleared = 0;  // 총 클리어한 라인 수
    private int currentLevel = 1;       // 현재 레벨
    private int lastLineClearScore = 0; // 마지막으로 얻은 라인 클리어 점수
    private int blocksSpawned = 0;      // 생성된 블록 개수 추가

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
        // 색상 보드 초기화
        colorBoard = new int[ROWS][COLS];
        repaint();
    // make external board reference point to internal array so other code using `board` works
    this.board = this.boardArray;
    // spawn the initial block safely (avoid calling overridable methods from constructor)
    this.nextBlock = Block.spawn();
    this.currentBlock = this.nextBlock;
    this.nextBlock = Block.spawn();
    if (gameBoard != null) {
        gameBoard.setNextBlock(nextBlock);
    }

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

    // @Override
    // protected void paintComponent(Graphics g) {



    //     super.paintComponent(g);
    //     if (!isVisible()) return;
    //     int cellSize = gameBoard.CELL_SIZE;
    //     // GameBoard의 x, y 오프셋과 동일하게 적용
    //     int xOffset = (gameBoard.getWidth() - gameBoard.COLS * cellSize) / 3;
    //     int yOffset = gameBoard.MARGIN * cellSize;
    //     g.setFont(new Font("Monospaced", Font.BOLD, cellSize - 2));
    //     FontMetrics fm = g.getFontMetrics();
    //     // 내부 그리기 범위: rows 2..ROWS-2 (2..21), cols 1..COLS-2 (1..10)
    //     for (int row = 0; row <= INNER_BOTTOM; row++) {
    //         for (int col = INNER_LEFT; col <= INNER_RIGHT; col++) {
    //             if (boardArray[row][col] == 1) {
    //                 int x = xOffset + (col - INNER_LEFT) * cellSize + (cellSize - fm.charWidth(TEXT)) / 2;
    //                 int y = yOffset + (row - INNER_TOP) * cellSize + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
    //                 g.setColor(Color.BLACK); // O를 더 잘 보이게
    //                 g.drawString(String.valueOf(TEXT), x, y);
    //             }
    //         }
    //     }
    // }


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
    public int HardDrop() {
        if (currentBlock != null) {  // 현재 블록이 있는지 확인
           /* while (!checkCollision(0,1)){  // y로 한 칸 내려갔을때 충돌이 일어나지 않으면 (while문이라서 충돌이 일어나기 전까지 계속 내려감)
                currentBlock.setPosition(currentBlock.getX(), currentBlock.getY()+1);  // y좌표 +1 을 해 한 칸 내려 새로운 위치를 설정
            } */ 
            int dropDistance = currentBlock.hardDrop(board);
            placePiece();  // 블록을 보드에 고정하고 라인 클리어 실행
            spawnNewBlock();  // 그리고 다음 블록을 생성
        //  repaint();
            return dropDistance;  // 드롭 거리 반환
        }
        return 0;   // 현재 블록이 없으면 0 반환
    }

    // 블록을 보드에 고정시키는 역할
    protected int placePiece() {
        int[][] shape = currentBlock.getShape();
        int x = currentBlock.getX();
        int y = currentBlock.getY();
        Color color = currentBlock.getColor();
        int rgb = (color != null ? color.getRGB() : new Color(100,100,100).getRGB());

        for (int row = 0 ; row < shape.length ; row++){
            for (int col = 0 ; col < shape[row].length ; col++){
                if (shape[row][col] != 0){
                    board[y + row] [x + col] = 1;
                    colorBoard[y + row][x + col] = rgb;
                }
            }
        }
            // 하드드롭에서도 즉시 라인 클리어 실행
            int linesCleared = lineClear();
            lastLineClearScore = 0;  // 기본값으로 초기화
            if (linesCleared > 0) {
                // 점수 계산을 레벨업보다 먼저 실행 (현재 레벨로 계산)
                lastLineClearScore = calculateLineClearScore(linesCleared);
                totalLinesCleared += linesCleared;
                levelUp();  // 레벨 업데이트는 점수 계산 후에
            }
        // checkLines();  // 줄이 다 찼는지 확인
        return lastLineClearScore;  // 라인 클리어 점수 반환
    }
 

    protected void spawnNewBlock(){
        if (nextBlock == null) {  
            nextBlock = Block.spawn();
        }
        currentBlock = nextBlock;  // 생성한 nextBlock을 currentBlock으로 설정
        nextBlock = Block.spawn(); // 새로운 nextBlock 생성
        blocksSpawned++;  // 생성된 블록 개수 증가
        
        // 속도 업데이트 확인
        checkSpeedIncrease();
        
        if (gameBoard != null) {
            gameBoard.setNextBlock(nextBlock); // GameView에 다음 블록 정보 전달
        }
    }

    // 게임 재시작 시 블록 생성 상태 초기화
    public void resetBlocks() {
        this.nextBlock = Block.spawn();
        this.currentBlock = this.nextBlock;
        this.nextBlock = Block.spawn();
        this.totalLinesCleared = 0;  // 총 클리어한 라인 수 초기화
        this.currentLevel = 1;       // 레벨 초기화
        this.lastLineClearScore = 0; // 라인 클리어 점수 초기화
        this.blocksSpawned = 0;      // 생성된 블록 개수 초기화
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


    // Checks all visible rows and clears any full lines (Tetris behavior).
    // Visible inner rows are from INNER_TOP .. INNER_BOTTOM (inclusive).
    public int lineClear() {
        boolean anyCleared = false;
        int clearedLine = 0;
                // 위에서 아래로 스캔 (보이는 영역): 2..ROWS-2
                for (int row = INNER_TOP; row <= INNER_BOTTOM; row++) {
            boolean full = true;
            for (int col = INNER_LEFT; col <= INNER_RIGHT; col++) {
                if (boardArray[row][col] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                // 위의 모든 행을 한 칸씩 아래로 이동 (색상 보드 포함)
                for (int r = row; r > 0; r--) {
                    System.arraycopy(boardArray[r - 1], 0, boardArray[r], 0, COLS);
                    System.arraycopy(colorBoard[r - 1], 0, colorBoard[r], 0, COLS);
                }
                // 최상단 행 초기화 (좌/우 벽 유지)
                for (int c = 0; c < COLS; c++) {
                    boardArray[0][c] = 0;
                    colorBoard[0][c] = 0;
                }
                boardArray[0][0] = 1; // 왼쪽 벽
                boardArray[0][COLS - 1] = 1; // 오른쪽 벽

                anyCleared = true;
                // 같은 row 인덱스에 새로운 줄이 내려왔으므로 다시 검사
                clearedLine++;
                row--;
            }
        }
        if (anyCleared) {
            // 속도 업데이트 확인
            checkSpeedIncrease();
            
            if (gameBoard != null) {
                gameBoard.repaintBlock();
            } else {
                repaint();
            }
        }
        return clearedLine;
    }
    
    public int levelUp() {
        // 총 클리어한 라인 수에 따라 레벨 계산 (0~9: 레벨1, 10~19: 레벨2, ..., 90~99: 레벨10)
        int newLevel = Math.min((totalLinesCleared / 2) + 1, 10);  // 최대 10레벨
        
        if (newLevel != currentLevel) {
            currentLevel = newLevel;
             System.out.println("Level Up! New Level: " + currentLevel);
            
            // 레벨이 올라갔을 때 추가 로직이 필요하면 여기에 구현
            // 예: 블록 떨어지는 속도 증가, 점수 보너스 등
        }
        
        return currentLevel;
    }

    public int[][] getColorBoard() {
        return colorBoard;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getTotalLinesCleared() {
        return totalLinesCleared;
    }

    public int getLastLineClearScore() {
        return lastLineClearScore;
    }

    public int getBlocksSpawned() {
        return blocksSpawned;
    }

    // 현재 속도 레벨 반환 (점수산정용도)
    public int getCurrentSpeedLevel() {
        // 블록 개수 기준으로 속도 레벨 계산
        int blockSpeedLevel = (blocksSpawned / 2 ) ;  // 20개마다 1레벨
        
        // 줄 삭제 기준으로 속도 레벨 계산 
        int lineSpeedLevel = (totalLinesCleared / 5 ) ;  // 5줄마다 1레벨

        // 둘 중 더 높은 레벨 사용 (최대 6레벨까지, 0-based이므로 6단계)
        return Math.min(Math.max(blockSpeedLevel, lineSpeedLevel), 6);
    }

    // 속도 증가 조건 확인 및 GameTimer에 알림
    private void checkSpeedIncrease() {
        // 블록 생성 개수 조건: 10, 20, 30, 40, 50, 60개 이상
        // 줄 삭제 개수 조건: 5, 10, 15, 20, 25, 30줄 이상
        int speedLevel = 0;
        
        // 블록 개수 기준으로 속도 레벨 계산
        int blockSpeedLevel = (blocksSpawned / 2);  // 20개마다 1레벨
        
        // 줄 삭제 기준으로 속도 레벨 계산 
        int lineSpeedLevel = (totalLinesCleared / 5);  // 5줄마다 1레벨
        
        // 둘 중 더 높은 레벨 사용 (최대 5레벨까지, 0(init)~5단계 이므로 총 6단계)
        speedLevel = Math.min(Math.max(blockSpeedLevel, lineSpeedLevel), 6);
        
        // GameTimer에 속도 업데이트 요청
        if (gameTimer != null) {
            gameTimer.updateSpeed(speedLevel);
        }
    }

    // 라인 클리어에 따른 점수 계산
    public int calculateLineClearScore(int linesCleared) {
        int baseScore = 0;
        //int speedMultiplier = getCurrentSpeedLevel() ;
        switch (linesCleared) {
            case 1:
                baseScore = 100;  // 싱글
                break;
            case 2:
                baseScore = 300;  // 더블
                break;
            case 3:
                baseScore = 500;  // 트리플
                break;
            case 4:
                baseScore = 800;  // 테트리스 (4줄)
                break;
            default:
                baseScore = 0;
                break;
        }
        return baseScore * currentLevel;  // 현재 레벨과 곱하기
        //return baseScore * currentLevel * speedMultiplier;  // 현재 레벨과 곱하기
    }

    public boolean isGameOver() {
        // 상단 감지 영역: row 0..(INNER_TOP-1), col 3..7 에 1이 있으면 게임 오버
        for (int row = INNER_TOP; row < INNER_TOP + 2; row++) {
            for (int col = 3; col < 8; col++) {
                if (boardArray[row][col] == 1) {
                    return true;


                }
            }
        }
        return false;
 
    }

    // GameTimer 참조 설정
    public void setGameTimer(GameTimer gameTimer) {
        this.gameTimer = gameTimer;
    }
    

    

}