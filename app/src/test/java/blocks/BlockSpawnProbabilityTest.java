package blocks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 난이도에 따른 I블록 생성 확률 테스트
 * - Easy: 기본 대비 20% 더 자주 (약 17.14%)
 * - Normal: 기본 확률 (14.29%)
 * - Hard: 기본 대비 20% 덜 자주 (약 11.43%)
 */
@DisplayName("난이도별 I블록 생성 확률 테스트")
class BlockSpawnProbabilityTest {
    
    private static final int TEST_COUNT = 10000; // 테스트할 블록 생성 횟수
    private static final double TOLERANCE = 0.02; // 오차 허용 범위 (±2%)
    
    private Path settingsPath;
    private String originalSettings;
    
    @BeforeEach
    void setUp() throws IOException {
        // 설정 파일 경로 가져오기
        settingsPath = Paths.get(settings.ConfigManager.getSettingsPath());
        
        // 원본 설정 백업
        if (Files.exists(settingsPath)) {
            originalSettings = Files.readString(settingsPath);
        }
        
        // 캐시 초기화
        Block.reloadSettings();
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() throws IOException {
        // 원본 설정 복원
        if (originalSettings != null) {
            Files.writeString(settingsPath, originalSettings);
        }
        
        // 캐시 초기화
        Block.reloadSettings();
    }
    
    @Test
    @DisplayName("Easy 난이도: I블록이 20% 더 자주 생성되어야 함")
    void testEasyDifficulty_IBlockSpawnRate() throws IOException {
        // Easy 난이도 설정
        setDifficulty("easy");
        Block.reloadSettings();
        
        // 블록 생성 및 카운팅
        int[] blockCounts = countBlockTypes(TEST_COUNT);
        
        // I블록 비율 계산
        double iBlockRate = (double) blockCounts[0] / TEST_COUNT;
        
        // Easy 난이도 예상 비율: 1/7 * 1.2 = 0.1714 (17.14%)
        double expectedRate = (1.0 / 7.0) * 1.2;
        
        System.out.println("=== Easy 난이도 테스트 ===");
        System.out.println("전체 블록 수: " + TEST_COUNT);
        System.out.println("I블록 생성 수: " + blockCounts[0]);
        System.out.println("실제 비율: " + String.format("%.2f%%", iBlockRate * 100));
        System.out.println("예상 비율: " + String.format("%.2f%%", expectedRate * 100));
        System.out.println("Normal 대비: +" + String.format("%.2f%%", ((iBlockRate / (1.0/7.0)) - 1) * 100));
        
        printAllBlockCounts(blockCounts);
        
        // 예상 비율과 실제 비율의 차이가 허용 범위 내인지 확인
        assertTrue(Math.abs(iBlockRate - expectedRate) < TOLERANCE,
            String.format("I블록 비율이 예상 범위를 벗어남. 예상: %.2f%%, 실제: %.2f%%", 
                expectedRate * 100, iBlockRate * 100));
    }
    
    @Test
    @DisplayName("Normal 난이도: I블록이 기본 확률로 생성되어야 함")
    void testNormalDifficulty_IBlockSpawnRate() throws IOException {
        // Normal 난이도 설정
        setDifficulty("normal");
        Block.reloadSettings();
        
        // 블록 생성 및 카운팅
        int[] blockCounts = countBlockTypes(TEST_COUNT);
        
        // I블록 비율 계산
        double iBlockRate = (double) blockCounts[0] / TEST_COUNT;
        
        // Normal 난이도 예상 비율: 1/7 = 0.1429 (14.29%)
        double expectedRate = 1.0 / 7.0;
        
        System.out.println("=== Normal 난이도 테스트 ===");
        System.out.println("전체 블록 수: " + TEST_COUNT);
        System.out.println("I블록 생성 수: " + blockCounts[0]);
        System.out.println("실제 비율: " + String.format("%.2f%%", iBlockRate * 100));
        System.out.println("예상 비율: " + String.format("%.2f%%", expectedRate * 100));
        
        printAllBlockCounts(blockCounts);
        
        // 예상 비율과 실제 비율의 차이가 허용 범위 내인지 확인
        assertTrue(Math.abs(iBlockRate - expectedRate) < TOLERANCE,
            String.format("I블록 비율이 예상 범위를 벗어남. 예상: %.2f%%, 실제: %.2f%%", 
                expectedRate * 100, iBlockRate * 100));
    }
    
    @Test
    @DisplayName("Hard 난이도: I블록이 20% 덜 자주 생성되어야 함")
    void testHardDifficulty_IBlockSpawnRate() throws IOException {
        // Hard 난이도 설정
        setDifficulty("hard");
        Block.reloadSettings();
        
        // 블록 생성 및 카운팅
        int[] blockCounts = countBlockTypes(TEST_COUNT);
        
        // I블록 비율 계산
        double iBlockRate = (double) blockCounts[0] / TEST_COUNT;
        
        // Hard 난이도 예상 비율: 1/7 * 0.8 = 0.1143 (11.43%)
        double expectedRate = (1.0 / 7.0) * 0.8;
        
        System.out.println("=== Hard 난이도 테스트 ===");
        System.out.println("전체 블록 수: " + TEST_COUNT);
        System.out.println("I블록 생성 수: " + blockCounts[0]);
        System.out.println("실제 비율: " + String.format("%.2f%%", iBlockRate * 100));
        System.out.println("예상 비율: " + String.format("%.2f%%", expectedRate * 100));
        System.out.println("Normal 대비: " + String.format("%.2f%%", ((iBlockRate / (1.0/7.0)) - 1) * 100));
        
        printAllBlockCounts(blockCounts);
        
        // 예상 비율과 실제 비율의 차이가 허용 범위 내인지 확인
        assertTrue(Math.abs(iBlockRate - expectedRate) < TOLERANCE,
            String.format("I블록 비율이 예상 범위를 벗어남. 예상: %.2f%%, 실제: %.2f%%", 
                expectedRate * 100, iBlockRate * 100));
    }
    
    @Test
    @DisplayName("난이도별 I블록 생성 비율 비교")
    void testCompareAllDifficulties() throws IOException {
        System.out.println("\n=== 난이도별 I블록 생성 비율 비교 ===");
        
        // Easy
        setDifficulty("easy");
        Block.reloadSettings();
        int[] easyCounts = countBlockTypes(TEST_COUNT);
        double easyRate = (double) easyCounts[0] / TEST_COUNT;
        
        // Normal
        setDifficulty("normal");
        Block.reloadSettings();
        int[] normalCounts = countBlockTypes(TEST_COUNT);
        double normalRate = (double) normalCounts[0] / TEST_COUNT;
        
        // Hard
        setDifficulty("hard");
        Block.reloadSettings();
        int[] hardCounts = countBlockTypes(TEST_COUNT);
        double hardRate = (double) hardCounts[0] / TEST_COUNT;
        
        System.out.println(String.format("Easy:   %.2f%% (목표: 17.14%%)", easyRate * 100));
        System.out.println(String.format("Normal: %.2f%% (목표: 14.29%%)", normalRate * 100));
        System.out.println(String.format("Hard:   %.2f%% (목표: 11.43%%)", hardRate * 100));
        
        System.out.println("\nNormal 대비:");
        System.out.println(String.format("Easy:   %+.2f%% (목표: +20%%)", ((easyRate / normalRate) - 1) * 100));
        System.out.println(String.format("Hard:   %+.2f%% (목표: -20%%)", ((hardRate / normalRate) - 1) * 100));
        
        // Easy가 Normal보다 많이 나와야 함
        assertTrue(easyRate > normalRate, "Easy 난이도에서 I블록이 더 자주 나와야 함");
        
        // Hard가 Normal보다 적게 나와야 함
        assertTrue(hardRate < normalRate, "Hard 난이도에서 I블록이 덜 나와야 함");
    }
    
    /**
     * 블록 타입별 생성 횟수 카운트
     * [0]=I, [1]=J, [2]=L, [3]=O, [4]=S, [5]=T, [6]=Z
     */
    private int[] countBlockTypes(int count) {
        int[] counts = new int[7];
        
        for (int i = 0; i < count; i++) {
            Block block = Block.spawn();
            String className = block.getClass().getSimpleName();
            
            switch (className) {
                case "IBlock": counts[0]++; break;
                case "JBlock": counts[1]++; break;
                case "LBlock": counts[2]++; break;
                case "OBlock": counts[3]++; break;
                case "SBlock": counts[4]++; break;
                case "TBlock": counts[5]++; break;
                case "ZBlock": counts[6]++; break;
            }
        }
        
        return counts;
    }
    
    /**
     * 모든 블록 타입의 생성 횟수 출력
     */
    private void printAllBlockCounts(int[] counts) {
        String[] blockNames = {"I", "J", "L", "O", "S", "T", "Z"};
        System.out.println("\n블록별 생성 횟수:");
        for (int i = 0; i < counts.length; i++) {
            double rate = (double) counts[i] / TEST_COUNT * 100;
            System.out.println(String.format("%s블록: %d회 (%.2f%%)", blockNames[i], counts[i], rate));
        }
        System.out.println();
    }
    
    /**
     * 설정 파일에 난이도 저장
     */
    private void setDifficulty(String difficulty) throws IOException {
        String json = String.format(
            "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"%s\"}",
            difficulty
        );
        Files.writeString(settingsPath, json);
    }
}
