package settings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ConfigManager 클래스 테스트
 */
class ConfigManagerTest {

    @BeforeAll
    static void setup() {
        // ConfigManager의 static 초기화가 먼저 실행되도록 보장
        assertNotNull(ConfigManager.getConfigDir());
    }

    @Test
    void testGetConfigDir_NotNull() {
        String configDir = ConfigManager.getConfigDir();
        assertNotNull(configDir, "설정 디렉토리 경로는 null이 아니어야 함");
    }

    @Test
    void testGetConfigDir_IsValidPath() {
        String configDir = ConfigManager.getConfigDir();
        Path path = Paths.get(configDir);
        assertTrue(Files.exists(path), "설정 디렉토리가 존재해야 함");
        assertTrue(Files.isDirectory(path), "설정 디렉토리는 디렉토리여야 함");
    }

    @Test
    void testGetConfigDir_InUserHome() {
        String configDir = ConfigManager.getConfigDir();
        String userHome = System.getProperty("user.home");
        assertTrue(configDir.startsWith(userHome), "설정 디렉토리는 사용자 홈 디렉토리 하위에 있어야 함");
    }

    @Test
    void testGetConfigDir_ContainsTetrisFolder() {
        String configDir = ConfigManager.getConfigDir();
        assertTrue(configDir.contains(".tetris"), "설정 디렉토리는 .tetris 폴더를 포함해야 함");
    }

    @Test
    void testGetSettingsPath_NotNull() {
        String settingsPath = ConfigManager.getSettingsPath();
        assertNotNull(settingsPath, "설정 파일 경로는 null이 아니어야 함");
    }

    @Test
    void testGetSettingsPath_FileExists() {
        String settingsPath = ConfigManager.getSettingsPath();
        Path path = Paths.get(settingsPath);
        assertTrue(Files.exists(path), "설정 파일이 존재해야 함");
        assertTrue(Files.isRegularFile(path), "설정 파일은 일반 파일이어야 함");
    }

    @Test
    void testGetSettingsPath_HasCorrectName() {
        String settingsPath = ConfigManager.getSettingsPath();
        assertTrue(settingsPath.endsWith("SettingSave.json"), "설정 파일명은 SettingSave.json이어야 함");
    }

    @Test
    void testGetSettingsPath_InConfigDir() {
        String settingsPath = ConfigManager.getSettingsPath();
        String configDir = ConfigManager.getConfigDir();
        assertTrue(settingsPath.startsWith(configDir), "설정 파일은 설정 디렉토리 내에 있어야 함");
    }

    @Test
    void testGetHighScorePath_NotNull() {
        String highScorePath = ConfigManager.getHighScorePath();
        assertNotNull(highScorePath, "하이스코어 파일 경로는 null이 아니어야 함");
    }

    @Test
    void testGetHighScorePath_FileExists() {
        String highScorePath = ConfigManager.getHighScorePath();
        Path path = Paths.get(highScorePath);
        assertTrue(Files.exists(path), "하이스코어 파일이 존재해야 함");
        assertTrue(Files.isRegularFile(path), "하이스코어 파일은 일반 파일이어야 함");
    }

    @Test
    void testGetHighScorePath_HasCorrectName() {
        String highScorePath = ConfigManager.getHighScorePath();
        assertTrue(highScorePath.endsWith("HighScore.json"), "하이스코어 파일명은 HighScore.json이어야 함");
    }

    @Test
    void testGetHighScorePath_InConfigDir() {
        String highScorePath = ConfigManager.getHighScorePath();
        String configDir = ConfigManager.getConfigDir();
        assertTrue(highScorePath.startsWith(configDir), "하이스코어 파일은 설정 디렉토리 내에 있어야 함");
    }

    @Test
    void testGetDefaultSettingsPath_NotNull() {
        String defaultSettingsPath = ConfigManager.getDefaultSettingsPath();
        assertNotNull(defaultSettingsPath, "기본 설정 파일 경로는 null이 아니어야 함");
    }

    @Test
    void testGetDefaultSettingsPath_FileExists() {
        String defaultSettingsPath = ConfigManager.getDefaultSettingsPath();
        Path path = Paths.get(defaultSettingsPath);
        assertTrue(Files.exists(path), "기본 설정 파일이 존재해야 함");
        assertTrue(Files.isRegularFile(path), "기본 설정 파일은 일반 파일이어야 함");
    }

    @Test
    void testGetDefaultSettingsPath_HasCorrectName() {
        String defaultSettingsPath = ConfigManager.getDefaultSettingsPath();
        assertTrue(defaultSettingsPath.endsWith("DefaultSetting.json"), "기본 설정 파일명은 DefaultSetting.json이어야 함");
    }

    @Test
    void testGetDefaultSettingsPath_InConfigDir() {
        String defaultSettingsPath = ConfigManager.getDefaultSettingsPath();
        String configDir = ConfigManager.getConfigDir();
        assertTrue(defaultSettingsPath.startsWith(configDir), "기본 설정 파일은 설정 디렉토리 내에 있어야 함");
    }

    @Test
    void testGetDefaultHighScorePath_NotNull() {
        String defaultHighScorePath = ConfigManager.getDefaultHighScorePath();
        assertNotNull(defaultHighScorePath, "기본 하이스코어 파일 경로는 null이 아니어야 함");
    }

    @Test
    void testGetDefaultHighScorePath_FileExists() {
        String defaultHighScorePath = ConfigManager.getDefaultHighScorePath();
        Path path = Paths.get(defaultHighScorePath);
        assertTrue(Files.exists(path), "기본 하이스코어 파일이 존재해야 함");
        assertTrue(Files.isRegularFile(path), "기본 하이스코어 파일은 일반 파일이어야 함");
    }

    @Test
    void testGetDefaultHighScorePath_HasCorrectName() {
        String defaultHighScorePath = ConfigManager.getDefaultHighScorePath();
        assertTrue(defaultHighScorePath.endsWith("HighScoreDefault.json"), "기본 하이스코어 파일명은 HighScoreDefault.json이어야 함");
    }

    @Test
    void testGetDefaultHighScorePath_InConfigDir() {
        String defaultHighScorePath = ConfigManager.getDefaultHighScorePath();
        String configDir = ConfigManager.getConfigDir();
        assertTrue(defaultHighScorePath.startsWith(configDir), "기본 하이스코어 파일은 설정 디렉토리 내에 있어야 함");
    }

    @Test
    void testAllPaths_AreDifferent() {
        String settingsPath = ConfigManager.getSettingsPath();
        String highScorePath = ConfigManager.getHighScorePath();
        String defaultSettingsPath = ConfigManager.getDefaultSettingsPath();
        String defaultHighScorePath = ConfigManager.getDefaultHighScorePath();

        assertNotEquals(settingsPath, highScorePath, "설정 파일과 하이스코어 파일은 다른 경로여야 함");
        assertNotEquals(settingsPath, defaultSettingsPath, "설정 파일과 기본 설정 파일은 다른 경로여야 함");
        assertNotEquals(settingsPath, defaultHighScorePath, "설정 파일과 기본 하이스코어 파일은 다른 경로여야 함");
        assertNotEquals(highScorePath, defaultSettingsPath, "하이스코어 파일과 기본 설정 파일은 다른 경로여야 함");
        assertNotEquals(highScorePath, defaultHighScorePath, "하이스코어 파일과 기본 하이스코어 파일은 다른 경로여야 함");
        assertNotEquals(defaultSettingsPath, defaultHighScorePath, "기본 설정 파일과 기본 하이스코어 파일은 다른 경로여야 함");
    }

    @Test
    void testConfigFiles_AreReadable() throws Exception {
        String settingsPath = ConfigManager.getSettingsPath();
        String highScorePath = ConfigManager.getHighScorePath();
        String defaultSettingsPath = ConfigManager.getDefaultSettingsPath();
        String defaultHighScorePath = ConfigManager.getDefaultHighScorePath();

        assertDoesNotThrow(() -> Files.readString(Paths.get(settingsPath)), "설정 파일은 읽을 수 있어야 함");
        assertDoesNotThrow(() -> Files.readString(Paths.get(highScorePath)), "하이스코어 파일은 읽을 수 있어야 함");
        assertDoesNotThrow(() -> Files.readString(Paths.get(defaultSettingsPath)), "기본 설정 파일은 읽을 수 있어야 함");
        assertDoesNotThrow(() -> Files.readString(Paths.get(defaultHighScorePath)), "기본 하이스코어 파일은 읽을 수 있어야 함");
    }

    @Test
    void testConfigFiles_AreWritable() throws Exception {
        String settingsPath = ConfigManager.getSettingsPath();
        String highScorePath = ConfigManager.getHighScorePath();

        // 원본 내용 저장
        String originalSettings = Files.readString(Paths.get(settingsPath));
        String originalHighScore = Files.readString(Paths.get(highScorePath));

        try {
            // 쓰기 테스트
            assertDoesNotThrow(() -> Files.writeString(Paths.get(settingsPath), originalSettings), "설정 파일은 쓸 수 있어야 함");
            assertDoesNotThrow(() -> Files.writeString(Paths.get(highScorePath), originalHighScore), "하이스코어 파일은 쓸 수 있어야 함");
        } finally {
            // 원본 복구
            Files.writeString(Paths.get(settingsPath), originalSettings);
            Files.writeString(Paths.get(highScorePath), originalHighScore);
        }
    }

    @Test
    void testConfigFiles_ContainValidJSON() throws Exception {
        String settingsPath = ConfigManager.getSettingsPath();
        String highScorePath = ConfigManager.getHighScorePath();
        String defaultSettingsPath = ConfigManager.getDefaultSettingsPath();
        String defaultHighScorePath = ConfigManager.getDefaultHighScorePath();

        String settings = Files.readString(Paths.get(settingsPath));
        String highScore = Files.readString(Paths.get(highScorePath));
        String defaultSettings = Files.readString(Paths.get(defaultSettingsPath));
        String defaultHighScore = Files.readString(Paths.get(defaultHighScorePath));

        assertTrue(settings.trim().startsWith("{"), "설정 파일은 유효한 JSON이어야 함");
        assertTrue(highScore.trim().startsWith("{"), "하이스코어 파일은 유효한 JSON이어야 함");
        assertTrue(defaultSettings.trim().startsWith("{"), "기본 설정 파일은 유효한 JSON이어야 함");
        assertTrue(defaultHighScore.trim().startsWith("{"), "기본 하이스코어 파일은 유효한 JSON이어야 함");
    }

    @Test
    void testConfigDir_IsHidden() {
        String configDir = ConfigManager.getConfigDir();
        assertTrue(configDir.contains(".tetris"), "설정 디렉토리는 숨김 폴더(.tetris)여야 함");
    }

    @Test
    void testMultipleGetCalls_ReturnSamePath() {
        String dir1 = ConfigManager.getConfigDir();
        String dir2 = ConfigManager.getConfigDir();
        assertEquals(dir1, dir2, "여러 번 호출해도 같은 경로를 반환해야 함");

        String settings1 = ConfigManager.getSettingsPath();
        String settings2 = ConfigManager.getSettingsPath();
        assertEquals(settings1, settings2, "여러 번 호출해도 같은 경로를 반환해야 함");

        String highScore1 = ConfigManager.getHighScorePath();
        String highScore2 = ConfigManager.getHighScorePath();
        assertEquals(highScore1, highScore2, "여러 번 호출해도 같은 경로를 반환해야 함");
    }

    @Test
    void testConfigDir_HasCorrectPermissions() {
        String configDir = ConfigManager.getConfigDir();
        Path path = Paths.get(configDir);
        
        assertTrue(Files.isReadable(path), "설정 디렉토리는 읽을 수 있어야 함");
        assertTrue(Files.isWritable(path), "설정 디렉토리는 쓸 수 있어야 함");
        assertTrue(Files.isExecutable(path), "설정 디렉토리는 실행 가능해야 함");
    }

    @Test
    void testConfigFiles_NotEmpty() throws Exception {
        String settingsPath = ConfigManager.getSettingsPath();
        String highScorePath = ConfigManager.getHighScorePath();
        String defaultSettingsPath = ConfigManager.getDefaultSettingsPath();
        String defaultHighScorePath = ConfigManager.getDefaultHighScorePath();

        assertTrue(Files.size(Paths.get(settingsPath)) > 0, "설정 파일은 비어있지 않아야 함");
        assertTrue(Files.size(Paths.get(highScorePath)) > 0, "하이스코어 파일은 비어있지 않아야 함");
        assertTrue(Files.size(Paths.get(defaultSettingsPath)) > 0, "기본 설정 파일은 비어있지 않아야 함");
        assertTrue(Files.size(Paths.get(defaultHighScorePath)) > 0, "기본 하이스코어 파일은 비어있지 않아야 함");
    }
}
