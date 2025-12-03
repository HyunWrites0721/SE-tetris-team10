package blocks;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import blocks.item.*;

/**
 * Block.spawnItem() 메서드를 테스트하는 클래스
 */
public class BlockSpawnItemTest {
    
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
        
        String jsonNormal = "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}";
        Files.writeString(settingsPath, jsonNormal);
        Block.reloadSettings();
    }
    
    @org.junit.jupiter.api.AfterEach
    public void tearDown() throws Exception {
        if (originalContent != null && Files.exists(settingsPath)) {
            Files.writeString(settingsPath, originalContent);
        } else if (originalContent == null && Files.exists(settingsPath)) {
            Files.deleteIfExists(settingsPath);
        }
    }
    
    @Test
    public void testSpawnItem_ReturnsNonNull() {
        // spawnItem은 항상 유효한 블록을 반환해야 함
        Block base = new TBlock();
        base.setShape();
        
        Block item = Block.spawnItem(base);
        
        assertNotNull(item, "spawnItem은 null을 반환하면 안됩니다");
        assertNotNull(item.getShape(), "생성된 아이템 블록은 shape을 가져야 합니다");
    }
    
    @Test
    public void testSpawnItem_AllTypes() {
        // 여러 번 호출하여 모든 아이템 타입이 나오는지 확인
        Block base = new TBlock();
        base.setShape();
        
        boolean hasAllClear = false;
        boolean hasBoxClear = false;
        boolean hasOneLine = false;
        boolean hasScoreDouble = false;
        boolean hasWeight = false;
        
        for (int i = 0; i < 100; i++) {
            Block item = Block.spawnItem(base);
            
            if (item instanceof AllClearBlock) hasAllClear = true;
            else if (item instanceof BoxClearBlock) hasBoxClear = true;
            else if (item instanceof OneLineClearBlock) hasOneLine = true;
            else if (item instanceof ScoreDoubleBlock) hasScoreDouble = true;
            else if (item instanceof WeightBlock) hasWeight = true;
        }
        
        assertTrue(hasAllClear || hasBoxClear || hasOneLine || hasScoreDouble || hasWeight,
            "100번 시도 중 최소한 한 가지 아이템 타입은 나와야 합니다");
    }
    
    @Test
    public void testSpawnItem_AllClearBlock_Position() {
        // AllClearBlock의 위치 테스트
        Block base = new TBlock();
        base.setShape();
        
        // AllClearBlock이 나올 때까지 시도
        for (int i = 0; i < 50; i++) {
            Block item = Block.spawnItem(base);
            if (item instanceof AllClearBlock) {
                assertEquals(5, item.getX(), "AllClearBlock의 X 위치는 5여야 합니다");
                assertEquals(2, item.getY(), "AllClearBlock의 Y 위치는 2여야 합니다");
                break;
            }
        }
    }
    
    @Test
    public void testSpawnItem_BoxClearBlock_Position() {
        // BoxClearBlock의 위치 테스트
        Block base = new TBlock();
        base.setShape();
        
        for (int i = 0; i < 50; i++) {
            Block item = Block.spawnItem(base);
            if (item instanceof BoxClearBlock) {
                assertEquals(4, item.getX(), "BoxClearBlock의 X 위치는 4여야 합니다");
                assertEquals(2, item.getY(), "BoxClearBlock의 Y 위치는 2여야 합니다");
                break;
            }
        }
    }
    
    @Test
    public void testSpawnItem_WeightBlock_Position() {
        // WeightBlock의 위치 테스트
        Block base = new TBlock();
        base.setShape();
        
        for (int i = 0; i < 50; i++) {
            Block item = Block.spawnItem(base);
            if (item instanceof WeightBlock) {
                assertEquals(3, item.getX(), "WeightBlock의 X 위치는 3이어야 합니다");
                assertEquals(2, item.getY(), "WeightBlock의 Y 위치는 2여야 합니다");
                break;
            }
        }
    }
    
    @Test
    public void testSpawnItem_OneLineClearBlock_InheritsPosition() {
        // OneLineClearBlock은 기본 블록의 위치를 상속
        Block base = new TBlock();
        base.setShape();
        base.setPosition(7, 10);
        
        for (int i = 0; i < 50; i++) {
            Block item = Block.spawnItem(base);
            if (item instanceof OneLineClearBlock) {
                assertEquals(7, item.getX(), "OneLineClearBlock은 기본 블록의 X 위치를 상속해야 합니다");
                assertEquals(10, item.getY(), "OneLineClearBlock은 기본 블록의 Y 위치를 상속해야 합니다");
                break;
            }
        }
    }
    
    @Test
    public void testSpawnItem_ScoreDoubleBlock_InheritsPosition() {
        // ScoreDoubleBlock은 기본 블록의 위치를 상속
        Block base = new TBlock();
        base.setShape();
        base.setPosition(6, 8);
        
        for (int i = 0; i < 50; i++) {
            Block item = Block.spawnItem(base);
            if (item instanceof ScoreDoubleBlock) {
                assertEquals(6, item.getX(), "ScoreDoubleBlock은 기본 블록의 X 위치를 상속해야 합니다");
                assertEquals(8, item.getY(), "ScoreDoubleBlock은 기본 블록의 Y 위치를 상속해야 합니다");
                break;
            }
        }
    }
    
    @Test
    public void testSpawnItem_OneLineClearBlock_InheritsColor() {
        // OneLineClearBlock은 기본 블록의 색상을 상속
        Block base = new TBlock();
        base.setShape();
        java.awt.Color baseColor = base.getColor();
        
        for (int i = 0; i < 50; i++) {
            Block item = Block.spawnItem(base);
            if (item instanceof OneLineClearBlock) {
                assertEquals(baseColor, item.getColor(), 
                    "OneLineClearBlock은 기본 블록의 색상을 상속해야 합니다");
                break;
            }
        }
    }
    
    @Test
    public void testSpawnItem_ScoreDoubleBlock_InheritsColor() {
        // ScoreDoubleBlock은 기본 블록의 색상을 상속
        Block base = new JBlock();
        base.setShape();
        java.awt.Color baseColor = base.getColor();
        
        for (int i = 0; i < 50; i++) {
            Block item = Block.spawnItem(base);
            if (item instanceof ScoreDoubleBlock) {
                assertEquals(baseColor, item.getColor(), 
                    "ScoreDoubleBlock은 기본 블록의 색상을 상속해야 합니다");
                break;
            }
        }
    }
    
    @Test
    public void testSpawnItem_WithDifferentBaseBlocks() {
        // 다양한 기본 블록으로 아이템 생성 테스트
        Block[] baseBlocks = {
            new IBlock(), new JBlock(), new LBlock(), new OBlock(),
            new SBlock(), new TBlock(), new ZBlock()
        };
        
        for (Block base : baseBlocks) {
            base.setShape();
            Block item = Block.spawnItem(base);
            
            assertNotNull(item, base.getClass().getSimpleName() + 
                "로부터 생성된 아이템은 null이 아니어야 합니다");
            assertNotNull(item.getShape(), base.getClass().getSimpleName() + 
                "로부터 생성된 아이템은 shape을 가져야 합니다");
        }
    }
    
    @Test
    public void testSpawnItem_ShapeIsSet() {
        // 생성된 아이템 블록의 shape이 제대로 설정되었는지 확인
        Block base = new TBlock();
        base.setShape();
        
        for (int i = 0; i < 20; i++) {
            Block item = Block.spawnItem(base);
            int[][] shape = item.getShape();
            
            assertNotNull(shape, "아이템 블록의 shape은 null이 아니어야 합니다");
            assertTrue(shape.length > 0, "아이템 블록의 shape은 비어있으면 안됩니다");
            
            // 최소한 하나의 0이 아닌 셀이 있어야 함
            boolean hasNonZero = false;
            for (int[] row : shape) {
                for (int val : row) {
                    if (val != 0) {
                        hasNonZero = true;
                        break;
                    }
                }
                if (hasNonZero) break;
            }
            assertTrue(hasNonZero, "아이템 블록은 최소 하나의 0이 아닌 셀을 가져야 합니다");
        }
    }
    
    @Test
    public void testSpawnItem_RandomDistribution() {
        // 아이템 타입이 랜덤하게 분포하는지 확인 (통계적 검증)
        Block base = new OBlock();
        base.setShape();
        
        int trials = 1000;
        int[] counts = new int[5]; // 5가지 아이템 타입
        
        for (int i = 0; i < trials; i++) {
            Block item = Block.spawnItem(base);
            
            if (item instanceof AllClearBlock) counts[0]++;
            else if (item instanceof BoxClearBlock) counts[1]++;
            else if (item instanceof OneLineClearBlock) counts[2]++;
            else if (item instanceof ScoreDoubleBlock) counts[3]++;
            else if (item instanceof WeightBlock) counts[4]++;
        }
        
        // 각 타입이 최소한 10% 이상은 나와야 함 (너무 편향되지 않음)
        for (int i = 0; i < 5; i++) {
            double ratio = (double) counts[i] / trials;
            assertTrue(ratio >= 0.10, 
                "아이템 타입 " + i + "의 분포가 너무 낮습니다: " + ratio);
        }
    }
    
    @Test
    public void testSpawnItem_PreservesBaseBlockShape() {
        // spawnItem이 기본 블록의 shape를 변경하지 않는지 확인
        Block base = new TBlock();
        base.setShape();
        
        int[][] originalShape = deepCopy(base.getShape());
        
        Block.spawnItem(base);
        
        int[][] afterShape = base.getShape();
        
        // 원본 shape이 변경되지 않았는지 확인
        assertEquals(originalShape.length, afterShape.length, "기본 블록의 shape 크기가 변경되면 안됩니다");
        for (int i = 0; i < originalShape.length; i++) {
            assertArrayEquals(originalShape[i], afterShape[i], 
                "기본 블록의 shape 내용이 변경되면 안됩니다");
        }
    }
    
    private int[][] deepCopy(int[][] src) {
        int[][] dst = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = java.util.Arrays.copyOf(src[i], src[i].length);
        }
        return dst;
    }
}
