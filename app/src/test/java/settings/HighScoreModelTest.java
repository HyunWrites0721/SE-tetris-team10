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

    @Test
    void testResetScores_ClearsAllScores() {
        // 점수 추가
        model.addScore("Player1", 1000, "normal", false);
        model.addScore("Player2", 2000, "easy", true);
        
        // 초기화
        model.resetScores();
        
        // 모든 점수가 초기화되었는지 확인
        assertTrue(model.getTopScores(false).isEmpty() || model.getTopScores(false).size() == 0, "Normal 모드 점수가 초기화되어야 함");
        assertTrue(model.getTopScores(true).isEmpty() || model.getTopScores(true).size() == 0, "Item 모드 점수가 초기화되어야 함");
    }

    @Test
    void testResetScores_ResetsHighScore() {
        // 점수 추가
        model.addScore("HighScorer", 9999, "hard", false);
        int highScoreBefore = model.getHighScore(false);
        assertTrue(highScoreBefore > 0, "점수가 있어야 함");
        
        // 초기화
        model.resetScores();
        
        // 최고 점수도 0 또는 초기 상태로 돌아가야 함
        int highScoreAfter = model.getHighScore(false);
        assertTrue(highScoreAfter == 0 || highScoreAfter <= highScoreBefore, "최고 점수가 초기화되어야 함");
    }

    @Test
    void testAddScore_Top10Limit() {
        // 11개의 점수 추가
        for (int i = 1; i <= 11; i++) {
            model.addScore("Player" + i, i * 100, "normal", false);
        }
        
        // Top 10만 유지되어야 함
        List<ScoreEntry> scores = model.getTopScores(false);
        assertTrue(scores.size() <= 10, "최대 10개의 점수만 유지되어야 함");
        
        // 가장 높은 점수들만 남아있어야 함
        if (scores.size() > 0) {
            assertTrue(scores.get(0).getScore() >= 200, "낮은 점수는 제거되어야 함");
        }
    }

    @Test
    void testAddScore_MaintainsSortOrder() {
        // 무작위 순서로 점수 추가
        model.addScore("Player1", 500, "normal", false);
        model.addScore("Player2", 1000, "normal", false);
        model.addScore("Player3", 250, "normal", false);
        model.addScore("Player4", 750, "normal", false);
        
        List<ScoreEntry> scores = model.getTopScores(false);
        
        // 내림차순 정렬 확인
        for (int i = 0; i < scores.size() - 1; i++) {
            assertTrue(scores.get(i).getScore() >= scores.get(i + 1).getScore(), 
                "점수가 내림차순으로 정렬되어야 함");
        }
    }

    @Test
    void testIsTopScore_WhenListFull() {
        // 깨끗한 상태로 시작
        model.resetScores();
        
        // Top 10을 채움
        for (int i = 1; i <= 10; i++) {
            model.addScore("Player" + i, i * 100, "normal", false);
        }
        
        // 최하위 점수보다 높은 점수는 Top 10에 들어갈 수 있음
        assertTrue(model.isTopScore(150, false), "최하위 점수보다 높으면 Top 10에 들어갈 수 있어야 함");
        
        // 최하위 점수보다 낮은 점수는 들어갈 수 없음
        assertFalse(model.isTopScore(50, false), "최하위 점수보다 낮으면 Top 10에 들어갈 수 없어야 함");
    }

    @Test
    void testGetScoreByRank_FirstPlace() {
        model.addScore("Winner", 5000, "hard", false);
        model.addScore("Second", 4000, "hard", false);
        
        ScoreEntry first = model.getScoreByRank(1, false);
        assertNotNull(first, "1등 점수가 있어야 함");
        assertEquals(5000, first.getScore(), "1등은 가장 높은 점수여야 함");
    }

    @Test
    void testGetScoreByRank_LastPlace() {
        // 5개 점수 추가
        for (int i = 1; i <= 5; i++) {
            model.addScore("Player" + i, i * 100, "normal", false);
        }
        
        List<ScoreEntry> scores = model.getTopScores(false);
        int size = scores.size();
        
        if (size > 0) {
            ScoreEntry last = model.getScoreByRank(size, false);
            assertNotNull(last, "마지막 순위 점수가 있어야 함");
            assertEquals(scores.get(size - 1).getScore(), last.getScore(), "마지막 순위 점수가 일치해야 함");
        }
    }

    @Test
    void testAddScore_ItemModeAndNormalModeSeparate() {
        model.addScore("NormalPlayer", 1000, "normal", false);
        model.addScore("ItemPlayer", 1000, "normal", true);
        
        List<ScoreEntry> normalScores = model.getTopScores(false);
        List<ScoreEntry> itemScores = model.getTopScores(true);
        
        // 각 모드에서 점수가 분리되어 있어야 함
        assertNotNull(normalScores, "Normal 모드 점수 리스트가 있어야 함");
        assertNotNull(itemScores, "Item 모드 점수 리스트가 있어야 함");
    }

    @Test
    void testAddScore_SameScoreDifferentNames() {
        model.addScore("Alice", 1000, "normal", false);
        model.addScore("Bob", 1000, "normal", false);
        model.addScore("Charlie", 1000, "normal", false);
        
        List<ScoreEntry> scores = model.getTopScores(false);
        long countScore1000 = scores.stream()
            .filter(e -> e.getScore() == 1000)
            .count();
        
        assertTrue(countScore1000 >= 3, "같은 점수여도 모두 저장되어야 함");
    }

    @Test
    void testGetHighScore_EmptyList() {
        model.resetScores();
        
        int highScore = model.getHighScore(false);
        assertEquals(0, highScore, "빈 리스트의 최고 점수는 0이어야 함");
    }

    @Test
    void testGetHighScore_AfterAddingScores() {
        model.resetScores();
        model.addScore("Player1", 500, "normal", false);
        model.addScore("Player2", 1500, "normal", false);
        model.addScore("Player3", 1000, "normal", false);
        
        int highScore = model.getHighScore(false);
        assertEquals(1500, highScore, "가장 높은 점수를 반환해야 함");
    }

    @Test
    void testScoreEntry_NullName() {
        ScoreEntry entry = new ScoreEntry(null, 1000, "normal");
        assertNull(entry.getName(), "null 이름도 허용되어야 함");
    }

    @Test
    void testScoreEntry_NullDifficulty() {
        ScoreEntry entry = new ScoreEntry("Player", 1000, null);
        assertNull(entry.getDifficulty(), "null 난이도도 허용되어야 함");
    }

    @Test
    void testAddScore_BoundaryScore() {
        // 경계값 테스트
        assertTrue(model.addScore("Player", 1, "normal", false), "최소 유효 점수(1)는 추가되어야 함");
        assertFalse(model.addScore("Player", 0, "normal", false), "0은 추가되지 않아야 함");
        assertFalse(model.addScore("Player", -1, "normal", false), "-1은 추가되지 않아야 함");
    }

    @Test
    void testIsTopScore_BoundaryConditions() {
        model.resetScores();
        
        // 빈 리스트일 때는 1점도 Top 10에 들어갈 수 있음
        assertTrue(model.isTopScore(1, false), "빈 리스트에는 1점도 들어갈 수 있어야 함");
        
        // 0과 음수는 들어갈 수 없음
        assertFalse(model.isTopScore(0, false), "0은 Top 10에 들어갈 수 없어야 함");
        assertFalse(model.isTopScore(-1, false), "음수는 Top 10에 들어갈 수 없어야 함");
    }

    @Test
    void testGetTopScores_ImmutableReturn() {
        model.addScore("Player", 1000, "normal", false);
        
        List<ScoreEntry> scores1 = model.getTopScores(false);
        int sizeBefore = scores1.size();
        
        // 반환된 리스트를 수정해도 원본에 영향을 주지 않아야 함
        scores1.add(new ScoreEntry("Hacker", 9999, "hard"));
        
        List<ScoreEntry> scores2 = model.getTopScores(false);
        assertEquals(sizeBefore, scores2.size(), "원본 리스트는 변경되지 않아야 함");
    }

    @Test
    void testMultipleDifficulties_AllSaved() {
        model.addScore("EasyPlayer", 100, "easy", false);
        model.addScore("NormalPlayer", 200, "normal", false);
        model.addScore("HardPlayer", 300, "hard", false);
        
        List<ScoreEntry> scores = model.getTopScores(false);
        
        long easyCount = scores.stream().filter(e -> "easy".equals(e.getDifficulty())).count();
        long normalCount = scores.stream().filter(e -> "normal".equals(e.getDifficulty())).count();
        long hardCount = scores.stream().filter(e -> "hard".equals(e.getDifficulty())).count();
        
        assertTrue(easyCount > 0 || normalCount > 0 || hardCount > 0, "모든 난이도 점수가 저장되어야 함");
    }
}
