package game;

import java.awt.*;
import javax.swing.JPanel;
import blocks.Block;
import javax.swing.Timer;

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


    protected Block currentBlock;  // 플레이어가 현재 조작하는 블록
    private Block nextBlock;
    public int[][] board ;   // 게임 보드를 나타내는 2차원 배열
    private GameTimer gameTimer;

    public GameBoard() {   // 게임보드 초기화
        board = new int[ROWS][COLS];  // 게임보드 크기 설정
        gameTimer = new GameTimer(this);
        spawnNewBlock();  // 첫 번째 블록 & 다음 블록 생성
        gameTimer.start();
    
    }

    public Block getCurrentBlock(){  // 현재블록 getter
        return currentBlock;
    }

    public int[][] getBoard(){  // 게임보드 getter
        return board;
    
    }
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
