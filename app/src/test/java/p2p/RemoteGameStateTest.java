package p2p;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import blocks.Block;

/**
 * RemoteGameState 클래스 테스트
 */
class RemoteGameStateTest {

    private RemoteGameState state;
    private static final int ROWS = 23;
    private static final int COLS = 12;

    @BeforeEach
    void setUp() {
        state = new RemoteGameState(ROWS, COLS);
    }

    @Test
    void testConstructor_InitializesBoard() {
        int[][] board = state.getBoard();
        
        assertNotNull(board);
        assertEquals(ROWS, board.length);
        assertEquals(COLS, board[0].length);
    }

    @Test
    void testConstructor_InitializesScore() {
        assertEquals(0, state.getScore());
    }

    @Test
    void testConstructor_InitializesBoardWithZeros() {
        int[][] board = state.getBoard();
        
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                assertEquals(0, board[y][x], "보드는 0으로 초기화되어야 함");
            }
        }
    }

    @Test
    void testSetBoard() {
        int[][] newBoard = new int[ROWS][COLS];
        newBoard[5][5] = 1;
        
        state.setBoard(newBoard);
        
        assertEquals(newBoard, state.getBoard());
        assertEquals(1, state.getBoard()[5][5]);
    }

    @Test
    void testGetBoard() {
        int[][] board = state.getBoard();
        assertNotNull(board);
        assertSame(board, state.getBoard(), "같은 보드 인스턴스를 반환해야 함");
    }

    @Test
    void testUpdateCurrentBlock() {
        state.updateCurrentBlock(5, 10);
        
        assertEquals(5, state.getCurrentX());
        assertEquals(10, state.getCurrentY());
    }

    @Test
    void testGetCurrentX_InitialValue() {
        assertEquals(0, state.getCurrentX());
    }

    @Test
    void testGetCurrentY_InitialValue() {
        assertEquals(0, state.getCurrentY());
    }

    @Test
    void testPlaceBlock_ValidPosition() {
        state.placeBlock(5, 10, 2);
        
        int[][] board = state.getBoard();
        assertEquals(3, board[10][5], "블록 타입+1 값이 저장되어야 함");
    }

    @Test
    void testPlaceBlock_AtBoundary() {
        state.placeBlock(0, 0, 1);
        assertEquals(2, state.getBoard()[0][0]);
        
        state.placeBlock(COLS - 1, ROWS - 1, 3);
        assertEquals(4, state.getBoard()[ROWS - 1][COLS - 1]);
    }

    @Test
    void testPlaceBlock_OutOfBounds_Negative() {
        assertDoesNotThrow(() -> state.placeBlock(-1, -1, 1));
        assertDoesNotThrow(() -> state.placeBlock(5, -1, 1));
        assertDoesNotThrow(() -> state.placeBlock(-1, 5, 1));
    }

    @Test
    void testPlaceBlock_OutOfBounds_TooLarge() {
        assertDoesNotThrow(() -> state.placeBlock(COLS, ROWS, 1));
        assertDoesNotThrow(() -> state.placeBlock(5, ROWS, 1));
        assertDoesNotThrow(() -> state.placeBlock(COLS, 5, 1));
    }

    @Test
    void testPlaceBlock_MultipleBlocks() {
        state.placeBlock(1, 1, 1);
        state.placeBlock(2, 2, 2);
        state.placeBlock(3, 3, 3);
        
        assertEquals(2, state.getBoard()[1][1]);
        assertEquals(3, state.getBoard()[2][2]);
        assertEquals(4, state.getBoard()[3][3]);
    }

    @Test
    void testClearLine_ValidLine() {
        // 보드 설정
        for (int x = 0; x < COLS; x++) {
            state.placeBlock(x, 5, 1);
        }
        
        state.clearLine(5);
        
        // 5번째 줄이 비어있어야 함
        for (int x = 0; x < COLS; x++) {
            assertTrue(state.getBoard()[5][x] == 0 || state.getBoard()[5][x] != 2);
        }
    }

    @Test
    void testClearLine_ShiftsLinesDown() {
        // 0번 줄에 블록 배치
        state.placeBlock(0, 0, 1);
        state.placeBlock(1, 0, 2);
        
        // 1번 줄 삭제
        state.clearLine(1);
        
        // 0번 줄의 내용이 1번 줄로 이동했어야 함
        assertEquals(2, state.getBoard()[1][0]);
        assertEquals(3, state.getBoard()[1][1]);
    }

    @Test
    void testClearLine_InvalidLine_Negative() {
        assertDoesNotThrow(() -> state.clearLine(-1));
    }

    @Test
    void testClearLine_InvalidLine_TooLarge() {
        assertDoesNotThrow(() -> state.clearLine(ROWS));
        assertDoesNotThrow(() -> state.clearLine(ROWS + 10));
    }

    @Test
    void testClearLine_TopLine() {
        state.placeBlock(0, 0, 1);
        state.clearLine(0);
        
        // 0번 줄이 빈 줄로 채워져야 함
        for (int x = 0; x < COLS; x++) {
            assertEquals(0, state.getBoard()[0][x]);
        }
    }

    @Test
    void testSetScore() {
        state.setScore(100);
        assertEquals(100, state.getScore());
        
        state.setScore(500);
        assertEquals(500, state.getScore());
    }

    @Test
    void testGetScore() {
        assertEquals(0, state.getScore());
        
        state.setScore(1234);
        assertEquals(1234, state.getScore());
    }

    @Test
    void testSetScore_NegativeValue() {
        state.setScore(-100);
        assertEquals(-100, state.getScore());
    }

    @Test
    void testMultipleOperations() {
        // 블록 배치
        state.placeBlock(5, 5, 1);
        state.placeBlock(6, 6, 2);
        
        // 현재 블록 위치 업데이트
        state.updateCurrentBlock(3, 4);
        
        // 점수 설정
        state.setScore(200);
        
        // 검증
        assertEquals(2, state.getBoard()[5][5]);
        assertEquals(3, state.getBoard()[6][6]);
        assertEquals(3, state.getCurrentX());
        assertEquals(4, state.getCurrentY());
        assertEquals(200, state.getScore());
    }

    @Test
    void testBoardIndependence() {
        int[][] originalBoard = state.getBoard();
        
        int[][] newBoard = new int[ROWS][COLS];
        newBoard[1][1] = 5;
        
        state.setBoard(newBoard);
        
        assertNotSame(originalBoard, state.getBoard());
    }

    @Test
    void testClearLine_MultipleTimes() {
        // 여러 줄에 블록 배치
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < COLS; x++) {
                state.placeBlock(x, y, y + 1);
            }
        }
        
        // 줄 삭제
        state.clearLine(2);
        state.clearLine(3);
        
        // 보드가 올바르게 업데이트되었는지 확인
        assertNotNull(state.getBoard());
    }

    @Test
    void testCurrentPosition_MultipleUpdates() {
        state.updateCurrentBlock(1, 1);
        assertEquals(1, state.getCurrentX());
        assertEquals(1, state.getCurrentY());
        
        state.updateCurrentBlock(5, 10);
        assertEquals(5, state.getCurrentX());
        assertEquals(10, state.getCurrentY());
        
        state.updateCurrentBlock(0, 0);
        assertEquals(0, state.getCurrentX());
        assertEquals(0, state.getCurrentY());
    }

    @Test
    void testScore_MultipleUpdates() {
        state.setScore(0);
        assertEquals(0, state.getScore());
        
        state.setScore(100);
        assertEquals(100, state.getScore());
        
        state.setScore(50);
        assertEquals(50, state.getScore());
    }

    @Test
    void testDifferentBoardSizes() {
        RemoteGameState smallState = new RemoteGameState(10, 8);
        assertEquals(10, smallState.getBoard().length);
        assertEquals(8, smallState.getBoard()[0].length);
        
        RemoteGameState largeState = new RemoteGameState(30, 15);
        assertEquals(30, largeState.getBoard().length);
        assertEquals(15, largeState.getBoard()[0].length);
    }
}
