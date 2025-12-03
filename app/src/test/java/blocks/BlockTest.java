package blocks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach; // 아이템 블록 테스트는 분리됨
import org.junit.jupiter.api.Test;


public class BlockTest {
    
    private Path settingsDir;
    private Path settingsPath;
    private String originalContent;
    
    @BeforeEach
    public void setUp() throws Exception {
        // ConfigManager가 실제로 사용하는 경로 사용
        settingsPath = Paths.get(settings.ConfigManager.getSettingsPath());
        settingsDir = settingsPath.getParent();
        
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
        } else if (originalContent == null && Files.exists(settingsPath)) {
            // 테스트에서 생성한 설정 파일을 정리
            Files.deleteIfExists(settingsPath);
        }
    }
    
    // 공통 유틸: 2차원 배열에서 0이 아닌 셀 개수 세기
    private int countNonZero(int[][] a) {
        int cnt = 0;
        for (int[] row : a) {
            for (int v : row) {
                if (v != 0) cnt++;
            }
        }
        return cnt;
    }

    // 공통 유틸: 2차원 배열의 깊은 복사
    private int[][] deepCopy(int[][] src) {
        int[][] dst = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = java.util.Arrays.copyOf(src[i], src[i].length);
        }
        return dst;
    }

    // (참고) 아이템 관련 유틸과 테스트는 ItemBlockTest로 이동되었습니다.

    @Test
    public void testSpawnProducesValidShape() {
        // 블록 스폰 기본 검증: shape이 존재하고 0이 아닌 셀이 하나 이상 있어야 함
        Block b = Block.spawn();
        assertNotNull(b, "spawn()은 null을 리턴하면 안됩니다");
        b.setShape();
        assertNotNull(b.getShape(), "스폰된 블록의 shape은 null이면 안됩니다");
        assertTrue(countNonZero(b.getShape()) > 0, "스폰된 블록의 shape에는 0이 아닌 셀이 최소 1개 이상 있어야 합니다");
    }

    @Test
    public void testRotatePreservesCellCount() {
        // 각 기본 블록(I, J, L, O, S, T, Z)에 대해 회전 전/후 0이 아닌 셀의 개수가 동일해야 함
        Block[] blocks = new Block[] { new IBlock(), new JBlock(), new LBlock(), new OBlock(), new SBlock(), new TBlock(), new ZBlock() };
        for (Block b : blocks) {
            b.setShape();
            int[][] before = deepCopy(b.getShape());
            int beforeCount = countNonZero(before);
            // 회전 실행
            b.getRotatedShape();
            int[][] after = b.getShape();
            int afterCount = countNonZero(after);
            // 검증: 회전으로 셀의 개수는 유지되어야 함
            assertEquals(beforeCount, afterCount, b.getClass().getSimpleName() + ": 회전 전/후 셀 개수가 달라요");
            // 추가: 회전된 shape의 크기가 전치 관계인지(엄밀히는 모든 블록에 필요하진 않지만 일반적으로 가로/세로가 바뀌는지) 가벼운 체크
            assertTrue(after.length == before[0].length && after[0].length == before.length
                    || (after.length == before.length && after[0].length == before[0].length),
                b.getClass().getSimpleName() + ": 회전 후 행/열 크기가 비정상적입니다");
        }
    }

    @Test
    public void testMovementAndHardDropToBottom() {
        // 23x12 보드(게임 내부 상수와 동일 크기), 빈 보드에서 하드드랍 후 더 이상 아래로 이동할 수 없어야 함
        int rows = 23, cols = 12;
        int[][] board = new int[rows][cols];
        Block b = new TBlock(); // 임의의 기본 블록
        b.setShape();
        b.setPosition(5, 2); // 기본 스폰 위치와 동일하게

        // 하드드랍 수행
        int dropped = b.hardDrop(board);
        assertTrue(dropped >= 0, "하드드랍 거리는 0 이상이어야 합니다");
        // 바닥에 닿았으므로 더 이상 아래로 이동할 수 없어야 함
        assertFalse(b.canMoveDown(board), "하드드랍 후에는 밑으로 이동할 수 없어야 합니다");
    }

    // 아이템 관련 테스트는 ItemBlockTest로 이동되었습니다.
    
    @Test
    public void testDifficultyBlockSpawn() throws Exception {
        // Easy 모드에서 I블록 생성 확률 테스트
        String jsonEasy = "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"easy\"}";
        Files.writeString(settingsPath, jsonEasy);
        
        // 설정 리로드 및 강제 초기화
        Block.reloadSettings();
        // SettingModel도 새로 생성하여 파일을 다시 읽도록 함
        settings.SettingModel testModel = new settings.SettingModel();
        String loadedDifficulty = testModel.getDifficulty();
        System.out.println("Loaded difficulty: " + loadedDifficulty);
        assertEquals("easy", loadedDifficulty, "설정 파일이 제대로 로드되지 않았습니다");
        
        int iBlockCount = 0;
        int totalBlocks = 5000;  // 더 많은 샘플로 통계적 안정성 확보
        
        for (int i = 0; i < totalBlocks; i++) {
            Block block = Block.spawn();
            if (block instanceof IBlock) {
                iBlockCount++;
            }
        }
        
        // Easy 모드에서 I블록이 약 17.6% 정도 나와야 함 (1.2/6.798 ≈ 0.176)
        double iBlockRatio = (double) iBlockCount / totalBlocks;
        System.out.println("Easy mode I-block ratio: " + iBlockRatio);
        // 오차범위를 넉넉하게: 14% ~ 22% (±5%)
        assertTrue(iBlockRatio > 0.14, "Easy mode: I-block should appear more than 14%, but was " + iBlockRatio);
        assertTrue(iBlockRatio < 0.22, "Easy mode: I-block should appear less than 22%, but was " + iBlockRatio);
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
        
        int totalBlocks = 5000;
        int[] blockCounts = new int[7]; // I, J, L, O, S, T, Z
        
        // 5000번 블록 생성
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
