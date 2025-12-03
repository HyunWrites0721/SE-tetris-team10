package blocks;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Block 클래스의 이동 로직을 테스트하는 클래스
 */
public class BlockMovementTest {
    
    private int[][] emptyBoard;
    private int[][] partialBoard;
    private final int BOARD_WIDTH = 12;
    private final int BOARD_HEIGHT = 23;
    
    @BeforeEach
    public void setUp() {
        emptyBoard = new int[BOARD_HEIGHT][BOARD_WIDTH];
        
        partialBoard = new int[BOARD_HEIGHT][BOARD_WIDTH];
        // 바닥 2줄 채우기
        for (int row = BOARD_HEIGHT - 2; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                partialBoard[row][col] = 1;
            }
        }
    }
    
    @Test
    public void testMoveDown_Success() {
        // 아래로 이동 성공
        Block block = new TBlock();
        block.setShape();
        block.setPosition(4, 5);
        
        int originalY = block.getY();
        block.moveDown(emptyBoard);
        
        assertEquals(originalY + 1, block.getY(), "아래로 이동하면 Y 좌표가 1 증가해야 합니다");
    }
    
    @Test
    public void testMoveDown_Blocked() {
        // 아래로 이동 불가능 (바닥)
        Block block = new OBlock();
        block.setShape();
        block.setPosition(5, BOARD_HEIGHT - 2);
        
        int originalY = block.getY();
        block.moveDown(emptyBoard);
        
        assertEquals(originalY, block.getY(), "이동 불가능하면 위치가 변하지 않아야 합니다");
    }
    
    @Test
    public void testIsMoveDown_Success() {
        // isMoveDown 메서드 테스트 (성공)
        Block block = new TBlock();
        block.setShape();
        block.setPosition(4, 5);
        
        int originalY = block.getY();
        boolean moved = block.isMoveDown(emptyBoard);
        
        assertTrue(moved, "이동 가능하면 true를 반환해야 합니다");
        assertEquals(originalY + 1, block.getY(), "이동 성공 시 Y 좌표가 1 증가해야 합니다");
    }
    
    @Test
    public void testIsMoveDown_Failed() {
        // isMoveDown 메서드 테스트 (실패)
        Block block = new OBlock();
        block.setShape();
        block.setPosition(5, BOARD_HEIGHT - 2);
        
        int originalY = block.getY();
        boolean moved = block.isMoveDown(emptyBoard);
        
        assertFalse(moved, "이동 불가능하면 false를 반환해야 합니다");
        assertEquals(originalY, block.getY(), "이동 실패 시 위치가 변하지 않아야 합니다");
    }
    
    @Test
    public void testMoveLeft_Success() {
        // 왼쪽으로 이동 성공
        Block block = new TBlock();
        block.setShape();
        block.setPosition(5, 5);
        
        int originalX = block.getX();
        block.moveLeft(emptyBoard);
        
        assertEquals(originalX - 1, block.getX(), "왼쪽으로 이동하면 X 좌표가 1 감소해야 합니다");
    }
    
    @Test
    public void testMoveLeft_Blocked() {
        // 왼쪽으로 이동 불가능 (왼쪽 벽)
        Block block = new TBlock();
        block.setShape();
        block.setPosition(0, 5);
        
        int originalX = block.getX();
        block.moveLeft(emptyBoard);
        
        assertEquals(originalX, block.getX(), "이동 불가능하면 위치가 변하지 않아야 합니다");
    }
    
    @Test
    public void testMoveRight_Success() {
        // 오른쪽으로 이동 성공
        Block block = new TBlock();
        block.setShape();
        block.setPosition(4, 5);
        
        int originalX = block.getX();
        block.moveRight(emptyBoard);
        
        assertEquals(originalX + 1, block.getX(), "오른쪽으로 이동하면 X 좌표가 1 증가해야 합니다");
    }
    
    @Test
    public void testMoveRight_Blocked() {
        // 오른쪽으로 이동 불가능 (오른쪽 벽)
        Block block = new TBlock();
        block.setShape();
        block.setPosition(BOARD_WIDTH - 3, 5);
        
        int originalX = block.getX();
        block.moveRight(emptyBoard);
        
        assertEquals(originalX, block.getX(), "이동 불가능하면 위치가 변하지 않아야 합니다");
    }
    
    @Test
    public void testHardDrop_EmptyBoard() {
        // 빈 보드에서 하드드랍
        Block block = new TBlock();
        block.setShape();
        block.setPosition(4, 5);
        
        int originalY = block.getY();
        int dropDistance = block.hardDrop(emptyBoard);
        
        assertTrue(dropDistance > 0, "하드드랍 거리는 0보다 커야 합니다");
        assertEquals(originalY + dropDistance, block.getY(), "하드드랍 후 Y 좌표가 정확해야 합니다");
        assertFalse(block.canMoveDown(emptyBoard), "하드드랍 후에는 더 이상 아래로 이동할 수 없어야 합니다");
    }
    
    @Test
    public void testHardDrop_PartialBoard() {
        // 일부 블록이 쌓인 보드에서 하드드랍
        Block block = new OBlock();
        block.setShape();
        block.setPosition(4, 5);
        
        int dropDistance = block.hardDrop(partialBoard);
        
        assertTrue(dropDistance > 0, "하드드랍 거리는 0보다 커야 합니다");
        assertEquals(BOARD_HEIGHT - 4, block.getY(), "쌓인 블록 위에 정확히 착지해야 합니다");
        assertFalse(block.canMoveDown(partialBoard), "하드드랍 후에는 더 이상 아래로 이동할 수 없어야 합니다");
    }
    
    @Test
    public void testHardDrop_AlreadyAtBottom() {
        // 이미 바닥에 있는 경우
        Block block = new OBlock();
        block.setShape();
        block.setPosition(5, BOARD_HEIGHT - 2);
        
        int dropDistance = block.hardDrop(emptyBoard);
        
        assertEquals(0, dropDistance, "이미 바닥에 있으면 드랍 거리는 0이어야 합니다");
    }
    
    @Test
    public void testHardDrop_IBlock_Vertical() {
        // IBlock 세로 방향 하드드랍
        Block block = new IBlock();
        block.setShape();
        block.getRotatedShape(); // 세로로 회전
        block.setPosition(5, 0);
        
        int dropDistance = block.hardDrop(emptyBoard);
        
        // IBlock 세로는 5칸이므로 BOARD_HEIGHT - 5 위치에 착지
        assertEquals(BOARD_HEIGHT - 5, block.getY(), "IBlock 세로가 정확히 착지해야 합니다");
        assertTrue(dropDistance > 0, "하드드랍 거리는 0보다 커야 합니다");
    }
    
    @Test
    public void testMultipleMoves() {
        // 여러 번 이동 테스트
        Block block = new TBlock();
        block.setShape();
        block.setPosition(5, 5);
        
        block.moveLeft(emptyBoard);
        assertEquals(4, block.getX());
        
        block.moveRight(emptyBoard);
        assertEquals(5, block.getX());
        
        block.moveDown(emptyBoard);
        assertEquals(6, block.getY());
        
        block.moveDown(emptyBoard);
        assertEquals(7, block.getY());
    }
    
    @Test
    public void testSetPosition() {
        // setPosition 메서드 테스트
        Block block = new TBlock();
        block.setShape();
        
        block.setPosition(3, 7);
        assertEquals(3, block.getX(), "X 좌표가 정확히 설정되어야 합니다");
        assertEquals(7, block.getY(), "Y 좌표가 정확히 설정되어야 합니다");
        
        block.setPosition(10, 15);
        assertEquals(10, block.getX());
        assertEquals(15, block.getY());
    }
    
    @Test
    public void testGetXY() {
        // getX, getY 메서드 테스트
        Block block = new OBlock();
        block.setShape();
        block.setPosition(4, 8);
        
        assertEquals(4, block.getX(), "getX는 정확한 X 좌표를 반환해야 합니다");
        assertEquals(8, block.getY(), "getY는 정확한 Y 좌표를 반환해야 합니다");
    }
    
    @Test
    public void testMoveSequence_UntilBlocked() {
        // 막힐 때까지 이동 시퀀스
        Block block = new OBlock();
        block.setShape();
        block.setPosition(5, 0);
        
        int moveCount = 0;
        while (block.canMoveDown(emptyBoard)) {
            block.moveDown(emptyBoard);
            moveCount++;
        }
        
        assertTrue(moveCount > 0, "최소 한 번은 이동해야 합니다");
        assertEquals(BOARD_HEIGHT - 2, block.getY(), "OBlock은 바닥에서 2칸 위에 착지해야 합니다");
    }
}
