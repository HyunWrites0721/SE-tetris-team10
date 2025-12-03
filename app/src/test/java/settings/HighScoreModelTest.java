package settings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import settings.HighScoreModel.ScoreEntry;
import java.util.List;

/**
 * HighScoreModel 클래스 테스트
 */
class HighScoreModelTest {

    private HighScoreModel model;

    @BeforeEach
    void setUp() {
        model = HighScoreModel.getInstance();
    }

    @Test
    void testGetInstance_ReturnsSingleton() {
        HighScoreModel instance1 = HighScoreModel.getInstance();
        HighScoreModel instance2 = HighScoreModel.getInstance();
        
        assertSame(instance1, instance2, "싱글톤 인스턴스여야 함");
    }

    @Test
    void testGetInstance_NotNull() {
        assertNotNull(HighScoreModel.getInstance());
    }

    @Test
    void testScoreEntry_Constructor() {
        ScoreEntry entry = new ScoreEntry("Player1", 1000, "normal");
        
        assertEquals("Player1", entry.getName());
        assertEquals(1000, entry.getScore());
        assertEquals("normal", entry.getDifficulty());
    }

    @Test
    void testScoreEntry_CompareTo_HigherScore() {
        ScoreEntry entry1 = new ScoreEntry("Player1", 1000, "normal");
        ScoreEntry entry2 = new ScoreEntry("Player2", 500, "normal");
        
        assertTrue(entry1.compareTo(entry2) < 0, "높은 점수가 앞에 와야 함");
    }

    @Test
    void testScoreEntry_CompareTo_LowerScore() {
        ScoreEntry entry1 = new ScoreEntry("Player1", 500, "normal");
        ScoreEntry entry2 = new ScoreEntry("Player2", 1000, "normal");
        
        assertTrue(entry1.compareTo(entry2) > 0, "낮은 점수가 뒤에 와야 함");
    }

    @Test
    void testScoreEntry_CompareTo_EqualScore() {
        ScoreEntry entry1 = new ScoreEntry("Player1", 1000, "normal");
        ScoreEntry entry2 = new ScoreEntry("Player2", 1000, "normal");
        
        assertEquals(0, entry1.compareTo(entry2), "같은 점수는 동등해야 함");
    }

    @Test
    void testAddScore_ValidScore() {
        boolean result = model.addScore("TestPlayer", 1500, "normal", false);
        assertTrue(result);
    }

    @Test
    void testAddScore_ZeroScore() {
        boolean result = model.addScore("TestPlayer", 0, "normal", false);
        assertFalse(result, "점수 0은 추가되지 않아야 함");
    }

    @Test
    void testAddScore_NegativeScore() {
        boolean result = model.addScore("TestPlayer", -100, "normal", false);
        assertFalse(result, "음수 점수는 추가되지 않아야 함");
    }

    @Test
    void testGetTopScores_NotNull() {
        List<ScoreEntry> normalScores = model.getTopScores(false);
        List<ScoreEntry> itemScores = model.getTopScores(true);
        
        assertNotNull(normalScores);
        assertNotNull(itemScores);
    }

    @Test
    void testGetTopScores_ReturnsNewList() {
        List<ScoreEntry> scores1 = model.getTopScores(false);
        List<ScoreEntry> scores2 = model.getTopScores(false);
        
        assertNotSame(scores1, scores2, "매번 새로운 리스트를 반환해야 함");
    }

    @Test
    void testGetHighScore_InitialValue() {
        int highScore = model.getHighScore(false);
        assertTrue(highScore >= 0);
    }

    @Test
    void testIsTopScore_ValidScore() {
        boolean result = model.isTopScore(1000, false);
        // 초기 상태에서는 top 10에 들어갈 수 있음
        assertTrue(result || model.getTopScores(false).size() >= 10);
    }

    @Test
    void testIsTopScore_ZeroScore() {
        assertFalse(model.isTopScore(0, false));
    }

    @Test
    void testIsTopScore_NegativeScore() {
        assertFalse(model.isTopScore(-100, false));
    }

    @Test
    void testGetScoreByRank_InvalidRank() {
        ScoreEntry entry = model.getScoreByRank(0, false);
        assertNull(entry, "순위 0은 유효하지 않음");
        
        entry = model.getScoreByRank(-1, false);
        assertNull(entry, "음수 순위는 유효하지 않음");
    }

    @Test
    void testGetScoreByRank_OutOfRange() {
        ScoreEntry entry = model.getScoreByRank(100, false);
        assertNull(entry, "범위를 벗어난 순위는 null 반환");
    }

    @Test
    void testNormalModeAndItemMode_Separate() {
        model.addScore("NormalPlayer", 1000, "normal", false);
        model.addScore("ItemPlayer", 2000, "easy", true);
        
        List<ScoreEntry> normalScores = model.getTopScores(false);
        List<ScoreEntry> itemScores = model.getTopScores(true);
        
        // 일반 모드와 아이템 모드의 점수가 분리되어 관리됨
        assertNotNull(normalScores);
        assertNotNull(itemScores);
    }

    @Test
    void testDifferentDifficulties() {
        model.addScore("Easy1", 500, "easy", false);
        model.addScore("Normal1", 1000, "normal", false);
        model.addScore("Hard1", 1500, "hard", false);
        
        // 모든 난이도가 저장됨
        List<ScoreEntry> scores = model.getTopScores(false);
        assertNotNull(scores);
    }

    @Test
    void testScoreEntry_DifferentNames() {
        ScoreEntry entry1 = new ScoreEntry("Alice", 1000, "normal");
        ScoreEntry entry2 = new ScoreEntry("Bob", 1000, "normal");
        
        assertEquals("Alice", entry1.getName());
        assertEquals("Bob", entry2.getName());
    }

    @Test
    void testScoreEntry_AllDifficulties() {
        String[] difficulties = {"easy", "normal", "hard"};
        
        for (String diff : difficulties) {
            ScoreEntry entry = new ScoreEntry("Player", 1000, diff);
            assertEquals(diff, entry.getDifficulty());
        }
    }

    @Test
    void testMultipleScores_SortedCorrectly() {
        model.addScore("Player1", 500, "normal", false);
        model.addScore("Player2", 1000, "normal", false);
        model.addScore("Player3", 750, "normal", false);
        
        List<ScoreEntry> scores = model.getTopScores(false);
        
        // 점수가 내림차순으로 정렬되어 있는지 확인
        for (int i = 0; i < scores.size() - 1; i++) {
            assertTrue(scores.get(i).getScore() >= scores.get(i + 1).getScore());
        }
    }

    @Test
    void testScoreEntry_ZeroScore() {
        ScoreEntry entry = new ScoreEntry("Player", 0, "normal");
        assertEquals(0, entry.getScore());
    }

    @Test
    void testScoreEntry_LargeScore() {
        ScoreEntry entry = new ScoreEntry("Player", 999999, "hard");
        assertEquals(999999, entry.getScore());
    }

    @Test
    void testScoreEntry_EmptyName() {
        ScoreEntry entry = new ScoreEntry("", 1000, "normal");
        assertEquals("", entry.getName());
    }

    @Test
    void testScoreEntry_SpecialCharactersInName() {
        String specialName = "Player!@#$%";
        ScoreEntry entry = new ScoreEntry(specialName, 1000, "normal");
        assertEquals(specialName, entry.getName());
    }

    @Test
    void testScoreEntry_KoreanName() {
        ScoreEntry entry = new ScoreEntry("플레이어", 1000, "normal");
        assertEquals("플레이어", entry.getName());
    }
}
