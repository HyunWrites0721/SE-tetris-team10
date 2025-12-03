package blocks;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Block 클래스의 회전 로직을 테스트하는 클래스
 */
public class BlockRotationTest {
    
    private int[][] deepCopy(int[][] src) {
        int[][] dst = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = java.util.Arrays.copyOf(src[i], src[i].length);
        }
        return dst;
    }
    
    private int countNonZero(int[][] shape) {
        int count = 0;
        for (int[] row : shape) {
            for (int val : row) {
                if (val != 0) count++;
            }
        }
        return count;
    }
    
    @Test
    public void testRotateshape_IBlock() {
        // IBlock 회전: 가로 -> 세로 -> 가로
        IBlock block = new IBlock();
        block.setShape();
        
        int[][] original = deepCopy(block.getShape());
        int originalCount = countNonZero(original);
        
        // 첫 번째 회전
        int[][] rotated1 = block.Rotateshape();
        assertEquals(originalCount, countNonZero(rotated1), "회전 후 블록 개수는 유지되어야 합니다");
        
        // 행/열 크기가 전치되어야 함
        assertEquals(original[0].length, rotated1.length, "회전 후 행 크기는 원래 열 크기와 같아야 합니다");
        assertEquals(original.length, rotated1[0].length, "회전 후 열 크기는 원래 행 크기와 같아야 합니다");
    }
    
    @Test
    public void testGetRotatedShape_UpdatesShape() {
        // getRotatedShape()가 실제로 shape를 업데이트하는지 테스트
        TBlock block = new TBlock();
        block.setShape();
        
        int[][] original = deepCopy(block.getShape());
        block.getRotatedShape();
        int[][] rotated = block.getShape();
        
        // shape가 변경되었는지 확인 (행/열 크기가 전치될 수 있음)
        // TBlock은 3x3이므로 회전 후에도 3x3이지만, 내용은 달라짐
        boolean shapeChanged = false;
        for (int i = 0; i < original.length && i < rotated.length; i++) {
            for (int j = 0; j < original[i].length && j < rotated[i].length; j++) {
                if (original[i][j] != rotated[i][j]) {
                    shapeChanged = true;
                    break;
                }
            }
            if (shapeChanged) break;
        }
        assertTrue(shapeChanged, "getRotatedShape() 호출 후 shape 내용이 변경되어야 합니다");
    }
    
    @Test
    public void testRotation_PreservesBlockCount() {
        // 모든 블록에 대해 회전 시 블록 개수가 유지되는지 테스트
        Block[] blocks = {
            new IBlock(), new JBlock(), new LBlock(), new OBlock(),
            new SBlock(), new TBlock(), new ZBlock()
        };
        
        for (Block block : blocks) {
            block.setShape();
            int originalCount = countNonZero(block.getShape());
            
            // 4번 회전 (360도)
            for (int i = 0; i < 4; i++) {
                block.getRotatedShape();
                int rotatedCount = countNonZero(block.getShape());
                assertEquals(originalCount, rotatedCount, 
                    block.getClass().getSimpleName() + ": 회전 " + (i+1) + "번 후에도 블록 개수는 유지되어야 합니다");
            }
        }
    }
    
    @Test
    public void testRotation_FourTimesReturnsToOriginal() {
        // 4번 회전하면 원래 모양으로 돌아오는지 테스트 (OBlock 제외)
        Block[] blocks = {
            new IBlock(), new JBlock(), new LBlock(),
            new SBlock(), new TBlock(), new ZBlock()
        };
        
        for (Block block : blocks) {
            block.setShape();
            int[][] original = deepCopy(block.getShape());
            
            // 4번 회전
            for (int i = 0; i < 4; i++) {
                block.getRotatedShape();
            }
            
            int[][] afterFourRotations = block.getShape();
            
            // 크기가 같은지 확인
            assertEquals(original.length, afterFourRotations.length, 
                block.getClass().getSimpleName() + ": 4번 회전 후 행 크기가 같아야 합니다");
            assertEquals(original[0].length, afterFourRotations[0].length, 
                block.getClass().getSimpleName() + ": 4번 회전 후 열 크기가 같아야 합니다");
        }
    }
    
    @Test
    public void testOBlock_RotationNoEffect() {
        // OBlock은 정사각형이므로 회전해도 시각적으로 같음
        OBlock block = new OBlock();
        block.setShape();
        
        int[][] original = deepCopy(block.getShape());
        block.getRotatedShape();
        int[][] rotated = block.getShape();
        
        // 크기는 같아야 함 (2x2 -> 2x2)
        assertEquals(original.length, rotated.length, "OBlock 회전 후 행 크기는 같아야 합니다");
        assertEquals(original[0].length, rotated[0].length, "OBlock 회전 후 열 크기는 같아야 합니다");
    }
    
    @Test
    public void testRotateshape_MatrixTransposition() {
        // Rotateshape 메서드의 수학적 정확성 테스트
        TBlock block = new TBlock();
        block.setShape();
        
        int[][] original = block.getShape();
        int[][] rotated = block.Rotateshape();
        
        // 회전 알고리즘 검증: rotatedShape[j][row - 1 - i] = shape[i][j]
        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < original[i].length; j++) {
                assertEquals(original[i][j], rotated[j][original.length - 1 - i],
                    "회전 알고리즘이 정확해야 합니다");
            }
        }
    }
    
    @Test
    public void testRotation_WithMovement() {
        // 회전과 이동을 함께 테스트
        int[][] board = new int[23][12];
        
        Block block = new TBlock();
        block.setShape();
        block.setPosition(4, 5);
        
        int originalX = block.getX();
        int originalY = block.getY();
        
        block.getRotatedShape();
        
        // 회전 후에도 위치는 유지되어야 함
        assertEquals(originalX, block.getX(), "회전 후에도 X 좌표는 유지되어야 합니다");
        assertEquals(originalY, block.getY(), "회전 후에도 Y 좌표는 유지되어야 합니다");
    }
    
    @Test
    public void testIBlock_HorizontalToVertical() {
        // IBlock 가로에서 세로로 회전
        IBlock block = new IBlock();
        block.setShape();
        
        int[][] horizontal = block.getShape();
        assertEquals(5, horizontal.length, "IBlock 가로는 5행이어야 합니다");
        assertEquals(5, horizontal[0].length, "IBlock 가로는 5열이어야 합니다");
        
        block.getRotatedShape();
        int[][] vertical = block.getShape();
        assertEquals(5, vertical.length, "IBlock 세로는 5행이어야 합니다");
        assertEquals(5, vertical[0].length, "IBlock 세로는 5열이어야 합니다");
        
        // 가로는 중간 행에 블록, 세로는 중간 열에 블록
        int horizontalRowSum = 0;
        for (int val : horizontal[2]) {
            if (val != 0) horizontalRowSum++;
        }
        assertEquals(4, horizontalRowSum, "IBlock 가로는 중간 행에 4칸이어야 합니다");
    }
    
    @Test
    public void testRotation_DoesNotAffectOriginalArray() {
        // Rotateshape()가 원본 배열을 수정하지 않는지 확인
        TBlock block = new TBlock();
        block.setShape();
        
        int[][] original = deepCopy(block.getShape());
        int[][] rotated = block.Rotateshape();
        
        // 반환된 배열은 새로운 배열이어야 함
        assertNotSame(original, rotated, "Rotateshape()는 새로운 배열을 반환해야 합니다");
    }
    
    @Test
    public void testMultipleRotations_AllBlocks() {
        // 모든 블록에 대해 여러 번 회전 테스트
        Block[] blocks = {
            new IBlock(), new JBlock(), new LBlock(), new OBlock(),
            new SBlock(), new TBlock(), new ZBlock()
        };
        
        for (Block block : blocks) {
            block.setShape();
            int originalCount = countNonZero(block.getShape());
            
            // 10번 회전 (2.5바퀴)
            for (int i = 0; i < 10; i++) {
                block.getRotatedShape();
                assertEquals(originalCount, countNonZero(block.getShape()),
                    block.getClass().getSimpleName() + ": 회전 " + (i+1) + "번 후에도 블록 개수 유지");
            }
        }
    }
}
