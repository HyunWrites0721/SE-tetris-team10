package p2p;

import blocks.Block;

/**
 * P2P 원격 게임의 상태를 저장하는 단순 클래스
 * GameController 없이 네트워크 이벤트만으로 화면 업데이트
 */
public class RemoteGameState {
    private int[][] board;  // 게임 보드 (고정된 블록들)
    private Block currentBlock;  // 현재 떨어지는 블록 (임시)
    private int currentX;
    private int currentY;
    private int score;
    
    public RemoteGameState(int rows, int cols) {
        this.board = new int[rows][cols];
        this.score = 0;
    }
    
    // 보드 상태 업데이트
    public void setBoard(int[][] board) {
        this.board = board;
    }
    
    public int[][] getBoard() {
        return board;
    }
    
    // 현재 블록 위치 업데이트
    public void updateCurrentBlock(int x, int y) {
        this.currentX = x;
        this.currentY = y;
    }
    
    public int getCurrentX() {
        return currentX;
    }
    
    public int getCurrentY() {
        return currentY;
    }
    
    // 블록 배치 (보드에 고정)
    public void placeBlock(int x, int y, int blockType) {
        // 블록을 보드에 추가
        // 간단히 해당 위치에 블록 타입 저장
        if (x >= 0 && x < board[0].length && y >= 0 && y < board.length) {
            board[y][x] = blockType + 1;  // 0은 빈 칸, 1-7은 블록
        }
    }
    
    // 줄 삭제
    public void clearLine(int lineY) {
        if (lineY < 0 || lineY >= board.length) return;
        
        // 해당 줄 삭제하고 위의 줄들을 아래로 이동
        for (int y = lineY; y > 0; y--) {
            board[y] = board[y - 1].clone();
        }
        board[0] = new int[board[0].length];
    }
    
    // 점수 업데이트
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getScore() {
        return score;
    }
}
