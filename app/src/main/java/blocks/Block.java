package blocks;

import java.awt.Color;
import java.lang.Math;
import game.GameBoard;

public abstract class Block {
    protected int [][] shape;
    private int x, y;
    public GameBoard gameBoard;
    private Color color;
    private Color[][] Colorset;
    public boolean setBlindColor = true; // 색맹모드 설정 여부
    // private Block currentBlock; gameboard로?
    
    // 게임 보드의 크기 상수
    private static final int BOARD_WIDTH = 12;
    private static final int BOARD_HEIGHT = 23;
    
    public abstract void setShape();
    public int[][] getShape() { return shape; }

    public int setBlindColor_1() {    // 단지 boolean 타입으로 받은 색맹모드 설정 값을 정수로 변환
        if (setBlindColor == true) return 1;
        else return 0;
    }
    
    
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
      switch(random) {
         case 0:  // IBlock (5×5)
            newBlock.setPosition(3, 0);
            break;
        case 3:  // OBlock (2×2)
            newBlock.setPosition(5, 2);
            break;
        case 1: case 2: case 4: case 5: case 6:  // 나머지 블록 (3×3)
            newBlock.setPosition(4, 2);
            break;
        }
    return newBlock;
    }   

    //newBlock.setPosition(BOARD_WIDTH/2 - 2, 0); // 보드의 (3,0) 위치에 각 블록의 좌상단 부터 블록 생성
    
    
    public void hardDrop(int[][] board) {
        int dropDistance = 0;  // dropDistance는 블록이 아래로 몇칸이나 떨어질 수 있는가를 의미
        while (!checkCollision(board, 0, dropDistance + 1)) {  // 한 칸 내려갔을때 충돌이 생기는 지를 확인. 충돌이 안생기면 false가 리턴될 것이고 not 이니까 while9(true) 즉 무한 루프가 됨.    
            dropDistance++;  // 충돌 안생기면 +1
        }
        y += dropDistance; // 충돌 직전까지 +1이 계속 누적될 것이고. 충돌되면 y 좌표에 더함.
    }
    
     
    private boolean checkCollision(int[][] board, int deltaX, int deltaY) {   
        // deltaX: x축 방향으로 이동할 거리 (왼쪽: -1, 오른쪽: +1)
        // deltaY: y축 방향으로 이동할 거리 (위: -1, 아래: +1) 
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != 0) {
                    int newX = x + col + deltaX;
                    int newY = y + row + deltaY;
                    
                    //   왼쪽 벽 ||  오른 쪽 벽 충돌     ||   바닥 충돌
                    if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT) {
                        return true;
                    }
                    // 다른 블록과의 충돌 검사
                    // 블록이 보드 안에 생성되었고 해당 위치에 다른 블록이 이미 있으면 충돌
                    if (newY >= 0 && board[newY][newX] != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
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
                    int boardRow = y + row;    
                    int boardCol = x + col;		
                    // 블록을 보드 좌표로 변환 (x,y)는 현재 블록의 보드 상 위치 (좌상단 기준)
                    // row, col은 블록 내부의 좌표
                    // boardRow와 boardCol은 실제 보드 내의 좌표
                    
                    // 바닥에 닿았는지 검사
                    if (boardRow >= BOARD_HEIGHT)    //boardRow가 20이상이 되면 바닥에 닿은 것
                        return true;  // 충돌 발생시 true 반환
                    
                    
                    // 옆 벽에 닿았는지 검사
                    if (boardCol < 0 || boardCol >= BOARD_WIDTH)  //boardCol이 0보다 작거나 10이상이면 벽에 닿은 것
                        return true;
                    
                    
                    // 다른 블록과 충돌하는지 검사
                    if (boardRow >= 0 && board[boardRow][boardCol] == 1)  // 보드 안에 블럭이 들어와있다 && 블록이 쌓인것이 있다.
                        return true;
                        /// 이걸로 블록이 쌓이고 맨 위 까지 닿았을때 충돌 감지가 될까?
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

    public int[][] Rotateshape() {
        int row = shape.length;  // 현재 블록의 세로 길이 (행 크기)
        int col = shape[0].length;  // 현재 블록의 가로 길이 (열 크기)
        int[][] rotatedShape = new int [col][row]; // 회전하면 가로/세로가 바뀜.
        for (int i = 0 ; i < row ; i++) {
            for (int j = 0 ; j < col ; j++) {
                rotatedShape[j][row - 1 - i] = shape[i][j];
            }
        }
        return rotatedShape;  // 회전 블럭 반환
    }

    public void getRotatedShape() {
        shape = Rotateshape();    // 회전한 블럭을 현재 블럭으로 설정
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) {   // 보드에서 위치 설정
        this.x = x;
        this.y = y;
    }


    
    protected void initColor(int setBlindColor_1, int colorIndex) {
        Colorset = new Color[2][];
        Colorset[0] = new Color[] {Color.green, Color.red, Color.blue, Color.orange, Color.yellow, Color.magenta, Color.pink};
        Colorset[1] = new Color[]{ new Color(0,158,115),   // green → bluish green
                                    new Color(213,94,0),    // red → vermilion
                                    new Color(0,114,178),   // blue
                                    new Color(230,159,0),   // orange
                                    new Color(240,228,66),  // yellow
                                    new Color(204,121,167), // magenta
                                    new Color(204,121,167) };  // pink → same tone
        this.color = Colorset[setBlindColor_1] [colorIndex];
    } 
    public void setColor(int setBlindColor_1,  int colorIndex) {
    	initColor(setBlindColor_1, colorIndex);
    }
    public Color getColor() { return color; }
}