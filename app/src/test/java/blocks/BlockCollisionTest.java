package blocks;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Block 클래스의 충돌 감지 로직을 테스트하는 클래스
 */
public class BlockCollisionTest {
    
    private int[][] emptyBoard;
    private int[][] partialBoard;
    private final int BOARD_WIDTH = 12;
    private final int BOARD_HEIGHT = 23;
    
    @BeforeEach
    public void setUp() {
        // 빈 보드 생성
        emptyBoard = new int[BOARD_HEIGHT][BOARD_WIDTH];
        
        // 일부 블록이 쌓인 보드 생성 (바닥에 몇 줄 채워짐)
        partialBoard = new int[BOARD_HEIGHT][BOARD_WIDTH];
        // 맨 아래 2줄을 채움
        for (int row = BOARD_HEIGHT - 2; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                partialBoard[row][col] = 1;
            }
        }
        // 몇 개 빈 공간 만들기
        partialBoard[BOARD_HEIGHT - 1][5] = 0;
        partialBoard[BOARD_HEIGHT - 2][3] = 0;
    }
    
    @Test
    public void testCheckCollision_LeftWall() {
        // 왼쪽 벽 충돌 검사
        // OBlock은 2x2이므로 더 간단함
        Block block = new OBlock();
        block.setShape();
        block.setPosition(-1, 5); // 왼쪽 벽을 넘어선 위치
        
        assertTrue(block.checkCollision(emptyBoard), "왼쪽 벽을 넘으면 충돌이 감지되어야 합니다");
    }
    
    @Test
    public void testCheckCollision_RightWall() {
        // 오른쪽 벽 충돌 검사
        Block block = new IBlock();
        block.setShape();
        block.setPosition(BOARD_WIDTH - 1, 5); // IBlock은 5칸이므로 오른쪽 벽을 넘음
        
        assertTrue(block.checkCollision(emptyBoard), "오른쪽 벽을 넘으면 충돌이 감지되어야 합니다");
    }
    
    @Test
    public void testCheckCollision_Bottom() {
        // 바닥 충돌 검사
        Block block = new OBlock();
        block.setShape();
        block.setPosition(5, BOARD_HEIGHT - 1); // 바닥을 넘어선 위치 (OBlock은 2x2)
        
        assertTrue(block.checkCollision(emptyBoard), "바닥을 넘으면 충돌이 감지되어야 합니다");
    }
    
    @Test
    public void testCheckCollision_NoCollision() {
        // 충돌이 없는 경우
        Block block = new TBlock();
        block.setShape();
        block.setPosition(4, 5); // 안전한 위치
        
        assertFalse(block.checkCollision(emptyBoard), "충돌이 없으면 false를 반환해야 합니다");
    }
    
    @Test
    public void testCheckCollision_WithOtherBlocks() {
        // 다른 블록과의 충돌 검사
        Block block = new OBlock();
        block.setShape();
        block.setPosition(4, BOARD_HEIGHT - 3); // OBlock은 2x2이므로 -3 위치면 바닥 2줄 위
        
        // partialBoard에서 바닥 2줄이 채워져 있으므로, 블록이 BOARD_HEIGHT-4 위치에 있으면 충돌 없음
        block.setPosition(4, BOARD_HEIGHT - 4);
        assertFalse(block.checkCollision(partialBoard), "쌓인 블록 위는 충돌이 없어야 합니다");
        
        block.setPosition(4, BOARD_HEIGHT - 2); // 쌓인 블록과 겹침
        assertTrue(block.checkCollision(partialBoard), "쌓인 블록과 겹치면 충돌이 감지되어야 합니다");
    }
    
    @Test
    public void testCheckCollision_TopEdge() {
        // 보드 위쪽 경계 테스트 (y < 0인 경우는 게임 오버 상황)
        Block block = new TBlock();
        block.setShape();
        block.setPosition(4, -1); // 보드 위쪽
        
        // 보드 위쪽에서는 boardRow가 음수이므로 충돌 검사에서 제외됨
        assertFalse(block.checkCollision(emptyBoard), "보드 위쪽은 충돌 검사에서 제외됩니다");
    }
    
    @Test
    public void testCanMoveDown_EmptySpace() {
        // 아래로 이동 가능한 경우
        Block block = new TBlock();
        block.setShape();
        block.setPosition(4, 5);
        
        assertTrue(block.canMoveDown(emptyBoard), "아래에 공간이 있으면 이동 가능해야 합니다");
    }
    
    @Test
    public void testCanMoveDown_Blocked() {
        // 아래로 이동 불가능한 경우 (바닥)
        Block block = new OBlock();
        block.setShape();
        block.setPosition(5, BOARD_HEIGHT - 2); // OBlock은 2x2이므로 바닥에 닿음
        
        assertFalse(block.canMoveDown(emptyBoard), "바닥에 닿으면 이동 불가능해야 합니다");
    }
    
    @Test
    public void testCanMoveDown_BlockedByOtherBlocks() {
        // 아래로 이동 불가능한 경우 (다른 블록)
        Block block = new OBlock();
        block.setShape();
        block.setPosition(4, BOARD_HEIGHT - 4); // 쌓인 블록 바로 위
        
        assertFalse(block.canMoveDown(partialBoard), "쌓인 블록 위에서는 이동 불가능해야 합니다");
    }
    
    @Test
    public void testCanMoveLeft_EmptySpace() {
        // 왼쪽으로 이동 가능한 경우
        Block block = new TBlock();
        block.setShape();
        block.setPosition(5, 5);
        
        assertTrue(block.canMoveLeft(emptyBoard), "왼쪽에 공간이 있으면 이동 가능해야 합니다");
    }
    
    @Test
    public void testCanMoveLeft_Blocked() {
        // 왼쪽으로 이동 불가능한 경우 (왼쪽 벽)
        Block block = new TBlock();
        block.setShape();
        block.setPosition(0, 5);
        
        assertFalse(block.canMoveLeft(emptyBoard), "왼쪽 벽에 닿으면 이동 불가능해야 합니다");
    }
    
    @Test
    public void testCanMoveRight_EmptySpace() {
        // 오른쪽으로 이동 가능한 경우
        Block block = new TBlock();
        block.setShape();
        block.setPosition(4, 5);
        
        assertTrue(block.canMoveRight(emptyBoard), "오른쪽에 공간이 있으면 이동 가능해야 합니다");
    }
    
    @Test
    public void testCanMoveRight_Blocked() {
        // 오른쪽으로 이동 불가능한 경우 (오른쪽 벽)
        Block block = new TBlock();
        block.setShape();
        block.setPosition(BOARD_WIDTH - 3, 5); // TBlock은 3칸
        
        assertFalse(block.canMoveRight(emptyBoard), "오른쪽 벽에 닿으면 이동 불가능해야 합니다");
    }
    
    @Test
    public void testCanMoveRight_BlockedByOtherBlocks() {
        // 오른쪽에 다른 블록이 있는 경우
        int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        // 오른쪽에 블록 배치
        for (int row = 15; row < 20; row++) {
            board[row][8] = 1;
        }
        
        Block block = new OBlock();
        block.setShape();
        block.setPosition(6, 18); // OBlock 2x2, 오른쪽에 블록이 있음
        
        assertFalse(block.canMoveRight(board), "오른쪽에 블록이 있으면 이동 불가능해야 합니다");
    }
    
    @Test
    public void testCanMoveLeft_BlockedByOtherBlocks() {
        // 왼쪽에 다른 블록이 있는 경우
        int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        // 왼쪽에 블록 배치
        for (int row = 15; row < 20; row++) {
            board[row][3] = 1;
        }
        
        Block block = new OBlock();
        block.setShape();
        block.setPosition(4, 18); // OBlock 2x2, 왼쪽에 블록이 있음
        
        assertFalse(block.canMoveLeft(board), "왼쪽에 블록이 있으면 이동 불가능해야 합니다");
    }
    
    @Test
    public void testCheckCollision_RotatedBlock() {
        // 회전된 블록의 충돌 검사
        Block block = new IBlock();
        block.setShape();
        block.getRotatedShape(); // 세로로 회전 (5x5 -> 5x5지만 블록 위치가 바뀜)
        block.setPosition(5, 0);
        
        // 회전된 IBlock은 세로로 4칸 (5x5 배열에서 중앙 열)
        // 바닥에 닿지 않는 위치면 충돌 없음
        block.setPosition(5, BOARD_HEIGHT - 6);
        assertFalse(block.checkCollision(emptyBoard), "회전된 블록도 충돌 검사가 정상 작동해야 합니다");
        
        // 바닥에 닿으면 충돌
        block.setPosition(5, BOARD_HEIGHT - 4);
        assertTrue(block.checkCollision(emptyBoard), "회전된 블록도 바닥 충돌을 감지해야 합니다");
    }
}
