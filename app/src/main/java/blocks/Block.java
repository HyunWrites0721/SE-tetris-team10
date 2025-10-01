package blocks;

import java.awt.Color;
import java.lang.Math;

public abstract class Block {
    protected int [][] shape;
    private int x, y;
    
    private Color color;
    private Color[] Colorset;
    
    // private Block currentBlock; gameboard로?
    
    // 게임 보드의 크기 상수
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    
    public abstract void setShape();
    public int[][] getShape() { return shape; }
    
    public static Block spawn() {            // 랜덤으로 블록 지정
        int random = (int)(Math.random() * 7);
        Block newBlock = switch(random) {
            case 0 ->  new IBlock();
            case 1 ->  new JBlock();
            case 2 ->  new LBlock();
            case 3 ->  new OBlock();
            case 4 ->  new SBlock();
            case 5 ->  new TBlock();
            case 6 ->  new ZBlock();
            default -> new IBlock(); // 기본값으로 IBlock 반환
        };
        newBlock.setPosition(BOARD_WIDTH/2 - 2, 0); // 보드의 (3,0) 위치에 각 블록의 좌상단 부터 블록 생성
        return newBlock;
    }
    
    /*
    public void spawnNewBlock() {
    	currentBlock = spawn();
    	currentBlock.setPosition(BOARD_WIDTH/2 - 2, 0);  
    	// 보드의 (3,0) 위치에 각 블록의 좌상단 부터 블록 생성
    } */
    
    
    // 충돌 검사 메서드
    public boolean checkCollision(int[][] board) {
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1) {   // 블록이 1일때만 위치를 보드 좌표로 변환해서 충돌검사
                    int boardRow = y + row;    // 블록을 보드 좌표로 변환
                    int boardCol = x + col;		// 이하동문
                    
                    // 바닥에 닿았는지 검사
                    if (boardRow >= BOARD_HEIGHT)    //boardRow가 20이상이 되면 바닥에 닿은 것
                        return true;  // 충돌 발생시 true 반환
                    
                    
                    // 옆 벽에 닿았는지 검사
                    if (boardCol < 0 || boardCol >= BOARD_WIDTH)  //boardCol이 0보다 작거나 10이상이면 벽에 닿은 것
                        return true;
                    
                    
                    // 다른 블록과 충돌하는지 검사
                    if (boardRow >= 0 && board[boardRow][boardCol] == 1)  // 보드 안에 블럭이 들어와있다 && 블록이 쌓인것이 있다.
                        return true;
                }
            }
        }
        return false;  // 충돌 미발생시 false 반환
    }
    
    // 아래로 이동 가능한지 확인하는 메서드
    // 밑으로 더 내려갈 수 있음 -> checkCollsion에서 false를 반환 -> canMoveDown에서 true 반환
    // 밑에 무언가 있어서 내려갈 수 없음 -> checkCollison에서 true를 반환 -> canMoveDown에서 false를 반환
    public boolean canMoveDown(int[][] board) {
        y++;        // 임시적으로 한 칸 아래로 이동
        boolean collision = checkCollision(board);  // 충돌 검사
        y--;   // 위치 복구
        return !collision;  // 충돌이 없으면 true 반환
    }
    
    public void moveDown(int[][] board) {  // 진짜 밑으로 블럭 이동
        if(canMoveDown(board)) {
            y++;
           // repaint();
        }
    }
    
    
    public boolean canMoveRight(int[][] board) {
		x++;        // 임시적으로 한 칸 오른쪽으로 이동
		boolean collision = checkCollision(board);  // 충돌 검사
		x--;   // 위치 복구
		return !collision;  // 충돌이 없으면 true 반환
	}
    
    public void moveRight(int[][] board) {  // 진짜 오른쪽으로 블럭 이동
		if(canMoveRight(board)) {
			x++;
		   // repaint();
		}
	}
    
    public boolean canMoveLeft(int[][] board) {
    	x--;
    	boolean collision = checkCollision(board);  // 충돌 검사
    	x++;   // 위치 복구
    	return !collision;  // 충돌이 없으면 true 반환
    }
    
    public void moveLeft(int[][] board) {  // 진짜 왼쪽으로 블럭 이동
		if(canMoveLeft(board)) {
			x--;
		   // repaint();
		}
	}
    
    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) {   // 보드에서 위치 설정
        this.x = x;
        this.y = y;
    }
    
    protected void initColor(int colorIndex) {
        Colorset = new Color[] 
            {Color.CYAN, Color.BLUE, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.RED};
        this.color = Colorset[colorIndex];
    } 
    public void setColor(int colorIndex) {
    	initColor(colorIndex);
    }
    public Color getColor() { return color; }
}