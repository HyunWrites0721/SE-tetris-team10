package blocks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class BlockTest {
    
    private Path settingsDir;
    private Path settingsPath;
    private String originalContent;
    
    @BeforeEach
    public void setUp() throws Exception {
        // 테스트용 디렉토리 및 파일 경로 설정
        settingsDir = Paths.get("app/src/main/java/settings/data");
        settingsPath = settingsDir.resolve("SettingSave.json");
        
        // 디렉토리가 없으면 생성
        if (!Files.exists(settingsDir)) {
            Files.createDirectories(settingsDir);
        }
        
        // 원본 설정 백업
        if (Files.exists(settingsPath)) {
            originalContent = Files.readString(settingsPath);
        }
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        // 원본 설정 복원
        if (originalContent != null && Files.exists(settingsPath)) {
            Files.writeString(settingsPath, originalContent);
        }
    }
    
    @Test
    public void testColorBlindModeSetting() throws Exception {
        // 1. 일반 모드 테스트
        String jsonFalse = "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}";
        Files.writeString(settingsPath, jsonFalse);
        
        IBlock blockNormal = new IBlock();
        Color normalColor = blockNormal.getColor();
        assertNotNull(normalColor, "Normal mode color should not be null");
        System.out.println("Normal mode color: " + normalColor);
        
        // 2. 색맹 모드 테스트
        String jsonTrue = "{\"colorBlindMode\":true,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}";
        Files.writeString(settingsPath, jsonTrue);
        
        IBlock blockColorBlind = new IBlock();
        Color colorBlindColor = blockColorBlind.getColor();
        assertNotNull(colorBlindColor, "Color blind mode color should not be null");
        System.out.println("Color blind mode color: " + colorBlindColor);
        
        // 두 모드에서 다른 색상이 사용되는지 확인
        assertNotEquals(normalColor, colorBlindColor, 
            "Colors should be different between normal and colorblind mode");
    }
    
    @Test
    public void testDifficultyBlockSpawn() throws Exception {
        // Easy 모드에서 I블록 생성 확률 테스트
        String jsonEasy = "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"easy\"}";
        Files.writeString(settingsPath, jsonEasy);
        
        int iBlockCount = 0;
        int totalBlocks = 1000;
        
        for (int i = 0; i < totalBlocks; i++) {
            Block block = Block.spawn();
            if (block instanceof IBlock) {
                iBlockCount++;
            }
        }
        
        // Easy 모드에서 I블록이 약 17% 정도 나와야 함 (1.2/7 ≈ 0.171)
        double iBlockRatio = (double) iBlockCount / totalBlocks;
        System.out.println("Easy mode I-block ratio: " + iBlockRatio);
        assertTrue(iBlockRatio > 0.14, "Easy mode: I-block should appear more than 14%, but was " + iBlockRatio);
        assertTrue(iBlockRatio < 0.20, "Easy mode: I-block should appear less than 20%, but was " + iBlockRatio);
    }
    
    @Test
    public void testEasyModeBlockDistribution() throws Exception {
        // Easy 모드 설정
        String jsonEasy = "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"easy\"}";
        Files.writeString(settingsPath, jsonEasy);
        
        // 설정 리로드
        Block.reloadSettings();
        
        int totalBlocks = 1000;
        int[] blockCounts = new int[7]; // I, J, L, O, S, T, Z
        
        // 1000번 블록 생성
        for (int i = 0; i < totalBlocks; i++) {
            Block block = Block.spawn();
            if (block instanceof IBlock) blockCounts[0]++;
            else if (block instanceof JBlock) blockCounts[1]++;
            else if (block instanceof LBlock) blockCounts[2]++;
            else if (block instanceof OBlock) blockCounts[3]++;
            else if (block instanceof SBlock) blockCounts[4]++;
            else if (block instanceof TBlock) blockCounts[5]++;
            else if (block instanceof ZBlock) blockCounts[6]++;
        }
        
        // Easy 모드 기대값 계산
        // 가중치: I=1.2, 나머지=0.933
        // 총합 = 1.2 + 6*0.933 = 6.798
        double totalWeight = 1.2 + 6 * 0.933;
        double[] expectedRatios = new double[7];
        expectedRatios[0] = 1.2 / totalWeight;      // I블록: ~17.6%
        for (int i = 1; i < 7; i++) {
            expectedRatios[i] = 0.933 / totalWeight; // 나머지: ~13.7%
        }
        
        String[] blockNames = {"I", "J", "L", "O", "S", "T", "Z"};
        System.out.println("\n=== Easy Mode Block Distribution ===");
        
        for (int i = 0; i < 7; i++) {
            double actualRatio = (double) blockCounts[i] / totalBlocks;
            double expectedRatio = expectedRatios[i];
            double difference = Math.abs(actualRatio - expectedRatio);
            
            System.out.printf("%s Block - Expected: %.3f (%.1f%%), Actual: %.3f (%.1f%%), Diff: %.3f (%.1f%%)\n",
                blockNames[i], expectedRatio, expectedRatio * 100, 
                actualRatio, actualRatio * 100, difference, difference * 100);
            
            // 오차범위 5% 이내인지 확인
            assertTrue(difference <= 0.05, 
                String.format("Easy mode: %s-block distribution error %.1f%% exceeds 5%%", 
                    blockNames[i], difference * 100));
        }
    }
    
    @Test
    public void testNormalModeBlockDistribution() throws Exception {
        // Normal 모드 설정
        String jsonNormal = "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}";
        Files.writeString(settingsPath, jsonNormal);
        
        // 설정 리로드
        Block.reloadSettings();
        
        int totalBlocks = 1000;
        int[] blockCounts = new int[7]; // I, J, L, O, S, T, Z
        
        // 1000번 블록 생성
        for (int i = 0; i < totalBlocks; i++) {
            Block block = Block.spawn();
            if (block instanceof IBlock) blockCounts[0]++;
            else if (block instanceof JBlock) blockCounts[1]++;
            else if (block instanceof LBlock) blockCounts[2]++;
            else if (block instanceof OBlock) blockCounts[3]++;
            else if (block instanceof SBlock) blockCounts[4]++;
            else if (block instanceof TBlock) blockCounts[5]++;
            else if (block instanceof ZBlock) blockCounts[6]++;
        }
        
        // Normal 모드 기대값: 모든 블록 동일 (1/7 = ~14.3%)
        double expectedRatio = 1.0 / 7.0;
        
        String[] blockNames = {"I", "J", "L", "O", "S", "T", "Z"};
        System.out.println("\n=== Normal Mode Block Distribution ===");
        
        for (int i = 0; i < 7; i++) {
            double actualRatio = (double) blockCounts[i] / totalBlocks;
            double difference = Math.abs(actualRatio - expectedRatio);
            
            System.out.printf("%s Block - Expected: %.3f (%.1f%%), Actual: %.3f (%.1f%%), Diff: %.3f (%.1f%%)\n",
                blockNames[i], expectedRatio, expectedRatio * 100, 
                actualRatio, actualRatio * 100, difference, difference * 100);
            
            // 오차범위 5% 이내인지 확인
            assertTrue(difference <= 0.05, 
                String.format("Normal mode: %s-block distribution error %.1f%% exceeds 5%%", 
                    blockNames[i], difference * 100));
        }
    }
    
    @Test
    public void testHardModeBlockDistribution() throws Exception {
        // Hard 모드 설정
        String jsonHard = "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"hard\"}";
        Files.writeString(settingsPath, jsonHard);
        
        // 설정 리로드
        Block.reloadSettings();
        
        int totalBlocks = 1000;
        int[] blockCounts = new int[7]; // I, J, L, O, S, T, Z
        
        // 1000번 블록 생성
        for (int i = 0; i < totalBlocks; i++) {
            Block block = Block.spawn();
            if (block instanceof IBlock) blockCounts[0]++;
            else if (block instanceof JBlock) blockCounts[1]++;
            else if (block instanceof LBlock) blockCounts[2]++;
            else if (block instanceof OBlock) blockCounts[3]++;
            else if (block instanceof SBlock) blockCounts[4]++;
            else if (block instanceof TBlock) blockCounts[5]++;
            else if (block instanceof ZBlock) blockCounts[6]++;
        }
        
        // Hard 모드 기대값 계산
        // 가중치: I=0.8, 나머지=1.033
        // 총합 = 0.8 + 6*1.033 = 7.0
        double totalWeight = 0.8 + 6 * 1.033;
        double[] expectedRatios = new double[7];
        expectedRatios[0] = 0.8 / totalWeight;       // I블록: ~11.4%
        for (int i = 1; i < 7; i++) {
            expectedRatios[i] = 1.033 / totalWeight;  // 나머지: ~14.8%
        }
        
        String[] blockNames = {"I", "J", "L", "O", "S", "T", "Z"};
        System.out.println("\n=== Hard Mode Block Distribution ===");
        
        for (int i = 0; i < 7; i++) {
            double actualRatio = (double) blockCounts[i] / totalBlocks;
            double expectedRatio = expectedRatios[i];
            double difference = Math.abs(actualRatio - expectedRatio);
            
            System.out.printf("%s Block - Expected: %.3f (%.1f%%), Actual: %.3f (%.1f%%), Diff: %.3f (%.1f%%)\n",
                blockNames[i], expectedRatio, expectedRatio * 100, 
                actualRatio, actualRatio * 100, difference, difference * 100);
            
            // 오차범위 5% 이내인지 확인
            assertTrue(difference <= 0.05, 
                String.format("Hard mode: %s-block distribution error %.1f%% exceeds 5%%", 
                    blockNames[i], difference * 100));
        }
    }
}
