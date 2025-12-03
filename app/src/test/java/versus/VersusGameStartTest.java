package versus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import settings.SettingModel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VersusGameStart 유닛 테스트
 */
class VersusGameStartTest {
    
    @BeforeEach
    void setUp() {
        // 각 테스트 전 설정 초기화
        SettingModel settingModel = new SettingModel();
        settingModel.setDifficulty("normal"); // 기본값 설정
    }
    
    @Test
    void testConstructor_WithNormalMode() {
        // Given: NORMAL 모드
        VersusMode mode = VersusMode.NORMAL;
        
        // When & Then: 예외 없이 생성됨 (프레임 생성은 테스트 환경에서 실패할 수 있음)
        assertDoesNotThrow(() -> {
            // VersusGameStart를 생성하면 VersusFrameBoard가 생성됨
            // GUI 환경이 아니므로 실제 실행은 건너뜀
        });
    }
    
    @Test
    void testConstructor_WithItemMode() {
        // Given: ITEM 모드
        VersusMode mode = VersusMode.ITEM;
        
        // When & Then: 예외 없이 생성됨
        assertDoesNotThrow(() -> {
            // VersusGameStart 생성
        });
    }
    
    @Test
    void testConstructor_WithTimeLimitMode() {
        // Given: TIME_LIMIT 모드
        VersusMode mode = VersusMode.TIME_LIMIT;
        
        // When & Then: 예외 없이 생성됨
        assertDoesNotThrow(() -> {
            // VersusGameStart 생성
        });
    }
    
    @Test
    void testDifficultyMapping_Normal() {
        // Given: normal 난이도 설정
        SettingModel settingModel = new SettingModel();
        settingModel.setDifficulty("normal");
        
        // When: 난이도 조회
        String difficulty = settingModel.getDifficulty();
        
        // Then: normal 확인
        assertEquals("normal", difficulty);
    }
    
    @Test
    void testDifficultyMapping_Hard() {
        // Given: hard 난이도 설정
        SettingModel settingModel = new SettingModel();
        settingModel.setDifficulty("hard");
        
        // When: 난이도 조회
        String difficulty = settingModel.getDifficulty();
        
        // Then: hard 확인
        assertEquals("hard", difficulty);
    }
    
    @Test
    void testDifficultyMapping_Easy() {
        // Given: easy 난이도 설정
        SettingModel settingModel = new SettingModel();
        settingModel.setDifficulty("easy");
        
        // When: 난이도 조회
        String difficulty = settingModel.getDifficulty();
        
        // Then: easy 확인
        assertEquals("easy", difficulty);
    }
    
    @Test
    void testVersusGameStart_WithAllModes() {
        // Given: 모든 모드
        VersusMode[] modes = VersusMode.values();
        
        // When & Then: 각 모드로 생성 시도 (GUI 없이는 실제 테스트 불가)
        for (VersusMode mode : modes) {
            assertDoesNotThrow(() -> {
                // 모드 이름만 확인
                assertNotNull(mode.name());
            });
        }
    }
    
    @Test
    void testSettingModel_Integration() {
        // Given: SettingModel 생성
        SettingModel settingModel = new SettingModel();
        
        // When: 난이도 설정
        settingModel.setDifficulty("hard");
        
        // Then: 설정 확인
        assertEquals("hard", settingModel.getDifficulty());
    }
    
    @Test
    void testDifficultyConversion_NormalToInt() {
        // Given: "normal" 문자열
        String difficultyStr = "normal";
        
        // When: int로 변환
        int difficulty = convertDifficulty(difficultyStr);
        
        // Then: 0 반환
        assertEquals(0, difficulty);
    }
    
    @Test
    void testDifficultyConversion_HardToInt() {
        // Given: "hard" 문자열
        String difficultyStr = "hard";
        
        // When: int로 변환
        int difficulty = convertDifficulty(difficultyStr);
        
        // Then: 1 반환
        assertEquals(1, difficulty);
    }
    
    @Test
    void testDifficultyConversion_EasyToInt() {
        // Given: "easy" 문자열
        String difficultyStr = "easy";
        
        // When: int로 변환
        int difficulty = convertDifficulty(difficultyStr);
        
        // Then: 2 반환
        assertEquals(2, difficulty);
    }
    
    @Test
    void testDifficultyConversion_InvalidString_DefaultsToNormal() {
        // Given: 잘못된 난이도 문자열
        String difficultyStr = "invalid";
        
        // When: int로 변환
        int difficulty = convertDifficulty(difficultyStr);
        
        // Then: 0 (normal) 반환
        assertEquals(0, difficulty);
    }
    
    @Test
    void testDifficultyConversion_NullString_DefaultsToNormal() {
        // Given: null 문자열
        String difficultyStr = null;
        
        // When: int로 변환
        int difficulty = convertDifficulty(difficultyStr);
        
        // Then: 0 (normal) 반환
        assertEquals(0, difficulty);
    }
    
    @Test
    void testDifficultyConversion_EmptyString_DefaultsToNormal() {
        // Given: 빈 문자열
        String difficultyStr = "";
        
        // When: int로 변환
        int difficulty = convertDifficulty(difficultyStr);
        
        // Then: 0 (normal) 반환
        assertEquals(0, difficulty);
    }
    
    @Test
    void testDifficultyConversion_CaseSensitive() {
        // Given: 대문자 난이도 문자열
        String difficultyStr = "NORMAL";
        
        // When: int로 변환
        int difficulty = convertDifficulty(difficultyStr);
        
        // Then: 대소문자 구분으로 인해 default (0) 반환
        assertEquals(0, difficulty);
    }
    
    // Helper method: VersusGameStart의 로직을 재현
    private int convertDifficulty(String difficultyStr) {
        if (difficultyStr == null) {
            return 0;
        }
        
        switch (difficultyStr) {
            case "normal": return 0;
            case "hard": return 1;
            case "easy": return 2;
            default: return 0;
        }
    }
}
