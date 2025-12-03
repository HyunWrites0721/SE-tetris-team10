package blocks;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.Color;

/**
 * 개별 블록 클래스들의 shape, color, 기본 동작을 테스트하는 클래스
 */
public class IndividualBlocksTest {
    
    private Path settingsDir;
    private Path settingsPath;
    private String originalContent;
    
    @BeforeEach
    public void setUp() throws Exception {
        settingsPath = Paths.get(settings.ConfigManager.getSettingsPath());
        settingsDir = settingsPath.getParent();
        
        if (!Files.exists(settingsDir)) {
            Files.createDirectories(settingsDir);
        }
        
        if (Files.exists(settingsPath)) {
            originalContent = Files.readString(settingsPath);
        }
    }
    
    @org.junit.jupiter.api.AfterEach
    public void tearDown() throws Exception {
        if (originalContent != null && Files.exists(settingsPath)) {
            Files.writeString(settingsPath, originalContent);
        } else if (originalContent == null && Files.exists(settingsPath)) {
            Files.deleteIfExists(settingsPath);
        }
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
    public void testIBlock_Shape() {
        // IBlock은 5x5 배열에 가로 4칸
        IBlock block = new IBlock();
        block.setShape();
        int[][] shape = block.getShape();
        
        assertNotNull(shape, "Shape은 null이 아니어야 합니다");
        assertEquals(5, shape.length, "IBlock은 5행이어야 합니다");
        assertEquals(5, shape[0].length, "IBlock은 5열이어야 합니다");
        assertEquals(4, countNonZero(shape), "IBlock은 정확히 4칸을 점유해야 합니다");
        
        // 중앙 행에 1이 4개 연속으로 있어야 함
        int[] middleRow = shape[2];
        int consecutiveOnes = 0;
        for (int val : middleRow) {
            if (val == 1) consecutiveOnes++;
        }
        assertEquals(4, consecutiveOnes, "IBlock은 가로로 4개의 1이 연속되어야 합니다");
    }
    
    @Test
    public void testIBlock_Color() {
        // IBlock의 색상 테스트
        IBlock block = new IBlock();
        Color color = block.getColor();
        
        assertNotNull(color, "IBlock의 색상은 null이 아니어야 합니다");
    }
    
    @Test
    public void testJBlock_Shape() {
        // JBlock은 3x3 배열, L자 반대 모양
        JBlock block = new JBlock();
        block.setShape();
        int[][] shape = block.getShape();
        
        assertNotNull(shape);
        assertEquals(3, shape.length, "JBlock은 3행이어야 합니다");
        assertEquals(3, shape[0].length, "JBlock은 3열이어야 합니다");
        assertEquals(4, countNonZero(shape), "JBlock은 정확히 4칸을 점유해야 합니다");
    }
    
    @Test
    public void testJBlock_Color() {
        JBlock block = new JBlock();
        Color color = block.getColor();
        
        assertNotNull(color, "JBlock의 색상은 null이 아니어야 합니다");
    }
    
    @Test
    public void testLBlock_Shape() {
        // LBlock은 3x3 배열, L자 모양
        LBlock block = new LBlock();
        block.setShape();
        int[][] shape = block.getShape();
        
        assertNotNull(shape);
        assertEquals(3, shape.length, "LBlock은 3행이어야 합니다");
        assertEquals(3, shape[0].length, "LBlock은 3열이어야 합니다");
        assertEquals(4, countNonZero(shape), "LBlock은 정확히 4칸을 점유해야 합니다");
    }
    
    @Test
    public void testLBlock_Color() {
        LBlock block = new LBlock();
        Color color = block.getColor();
        
        assertNotNull(color, "LBlock의 색상은 null이 아니어야 합니다");
    }
    
    @Test
    public void testOBlock_Shape() {
        // OBlock은 2x2 정사각형
        OBlock block = new OBlock();
        block.setShape();
        int[][] shape = block.getShape();
        
        assertNotNull(shape);
        assertEquals(2, shape.length, "OBlock은 2행이어야 합니다");
        assertEquals(2, shape[0].length, "OBlock은 2열이어야 합니다");
        assertEquals(4, countNonZero(shape), "OBlock은 정확히 4칸을 점유해야 합니다");
        
        // 모든 칸이 1이어야 함
        for (int[] row : shape) {
            for (int val : row) {
                assertEquals(1, val, "OBlock의 모든 칸은 1이어야 합니다");
            }
        }
    }
    
    @Test
    public void testOBlock_Color() {
        OBlock block = new OBlock();
        Color color = block.getColor();
        
        assertNotNull(color, "OBlock의 색상은 null이 아니어야 합니다");
    }
    
    @Test
    public void testSBlock_Shape() {
        // SBlock은 3x3 배열, S자 모양
        SBlock block = new SBlock();
        block.setShape();
        int[][] shape = block.getShape();
        
        assertNotNull(shape);
        assertEquals(3, shape.length, "SBlock은 3행이어야 합니다");
        assertEquals(3, shape[0].length, "SBlock은 3열이어야 합니다");
        assertEquals(4, countNonZero(shape), "SBlock은 정확히 4칸을 점유해야 합니다");
    }
    
    @Test
    public void testSBlock_Color() {
        SBlock block = new SBlock();
        Color color = block.getColor();
        
        assertNotNull(color, "SBlock의 색상은 null이 아니어야 합니다");
    }
    
    @Test
    public void testTBlock_Shape() {
        // TBlock은 3x3 배열, T자 모양
        TBlock block = new TBlock();
        block.setShape();
        int[][] shape = block.getShape();
        
        assertNotNull(shape);
        assertEquals(3, shape.length, "TBlock은 3행이어야 합니다");
        assertEquals(3, shape[0].length, "TBlock은 3열이어야 합니다");
        assertEquals(4, countNonZero(shape), "TBlock은 정확히 4칸을 점유해야 합니다");
    }
    
    @Test
    public void testTBlock_Color() {
        TBlock block = new TBlock();
        Color color = block.getColor();
        
        assertNotNull(color, "TBlock의 색상은 null이 아니어야 합니다");
    }
    
    @Test
    public void testZBlock_Shape() {
        // ZBlock은 3x3 배열, Z자 모양
        ZBlock block = new ZBlock();
        block.setShape();
        int[][] shape = block.getShape();
        
        assertNotNull(shape);
        assertEquals(3, shape.length, "ZBlock은 3행이어야 합니다");
        assertEquals(3, shape[0].length, "ZBlock은 3열이어야 합니다");
        assertEquals(4, countNonZero(shape), "ZBlock은 정확히 4칸을 점유해야 합니다");
    }
    
    @Test
    public void testZBlock_Color() {
        ZBlock block = new ZBlock();
        Color color = block.getColor();
        
        assertNotNull(color, "ZBlock의 색상은 null이 아니어야 합니다");
    }
    
    @Test
    public void testAllBlocks_HaveFourCells() {
        // 모든 기본 블록은 정확히 4칸을 점유해야 함 (테트리스 규칙)
        Block[] blocks = {
            new IBlock(), new JBlock(), new LBlock(), new OBlock(),
            new SBlock(), new TBlock(), new ZBlock()
        };
        
        for (Block block : blocks) {
            block.setShape();
            int count = countNonZero(block.getShape());
            assertEquals(4, count, block.getClass().getSimpleName() + "은 정확히 4칸을 점유해야 합니다");
        }
    }
    
    @Test
    public void testAllBlocks_ColorNotNull() {
        // 모든 블록은 유효한 색상을 가져야 함
        Block[] blocks = {
            new IBlock(), new JBlock(), new LBlock(), new OBlock(),
            new SBlock(), new TBlock(), new ZBlock()
        };
        
        for (Block block : blocks) {
            Color color = block.getColor();
            assertNotNull(color, block.getClass().getSimpleName() + "의 색상은 null이 아니어야 합니다");
        }
    }
    
    @Test
    public void testAllBlocks_DifferentColors() {
        // 모든 블록은 서로 다른 색상을 가져야 함 (시각적 구분을 위해)
        Block[] blocks = {
            new IBlock(), new JBlock(), new LBlock(), new OBlock(),
            new SBlock(), new TBlock(), new ZBlock()
        };
        
        Color[] colors = new Color[blocks.length];
        for (int i = 0; i < blocks.length; i++) {
            colors[i] = blocks[i].getColor();
        }
        
        // 모든 색상이 고유한지 확인
        for (int i = 0; i < colors.length; i++) {
            for (int j = i + 1; j < colors.length; j++) {
                assertFalse(colors[i].equals(colors[j]), 
                    blocks[i].getClass().getSimpleName() + "와 " + 
                    blocks[j].getClass().getSimpleName() + "의 색상이 같아서는 안됩니다");
            }
        }
    }
    
    @Test
    public void testIBlock_RotationChangesOrientation() {
        // IBlock 회전 테스트 (가로 -> 세로)
        IBlock block = new IBlock();
        block.setShape();
        
        int[][] originalShape = deepCopy(block.getShape());
        block.getRotatedShape();
        int[][] rotatedShape = block.getShape();
        
        // 회전 후 행/열이 바뀌어야 함
        assertEquals(originalShape[0].length, rotatedShape.length, 
            "회전 후 행의 개수는 원래 열의 개수와 같아야 합니다");
        assertEquals(originalShape.length, rotatedShape[0].length, 
            "회전 후 열의 개수는 원래 행의 개수와 같아야 합니다");
        
        // 블록 개수는 유지되어야 함
        assertEquals(countNonZero(originalShape), countNonZero(rotatedShape), 
            "회전 후에도 블록 개수는 유지되어야 합니다");
    }
    
    @Test
    public void testOBlock_RotationNoChange() {
        // OBlock은 회전해도 모양이 같음
        OBlock block = new OBlock();
        block.setShape();
        
        int[][] originalShape = deepCopy(block.getShape());
        block.getRotatedShape();
        int[][] rotatedShape = block.getShape();
        
        // OBlock은 정사각형이므로 회전 후에도 크기가 같음
        assertEquals(originalShape.length, rotatedShape.length);
        assertEquals(originalShape[0].length, rotatedShape[0].length);
    }
    
    private int[][] deepCopy(int[][] src) {
        int[][] dst = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = java.util.Arrays.copyOf(src[i], src[i].length);
        }
        return dst;
    }
}
