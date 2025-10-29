package game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScoreBoard 테스트")
public class ScoreBoardTest {

    private ScoreBoard scoreBoard;

    @BeforeEach
    void setUp() {
        scoreBoard = new ScoreBoard();
    }

    @AfterEach
    void tearDown() {
        scoreBoard = null;
    }

    @Test
    @DisplayName("ScoreBoard 생성 테스트")
    void testScoreBoardCreation() {
        assertNotNull(scoreBoard, "ScoreBoard가 생성되어야 함");
    }

    @Test
    @DisplayName("초기 하이스코어 값 테스트")
    void testInitialHighScore() {
        assertEquals(0, scoreBoard.getHighScore(), "초기 하이스코어는 0이어야 함");
    }

    @Test
    @DisplayName("하이스코어 설정 테스트")
    void testSetHighScore() {
        int testScore = 1000;
        scoreBoard.setHighScore(testScore);
        assertEquals(testScore, scoreBoard.getHighScore(), "설정한 하이스코어가 반환되어야 함");
    }

    @Test
    @DisplayName("하이스코어 여러 번 설정 테스트")
    void testMultipleSetHighScore() {
        // 첫 번째 설정
        int firstScore = 500;
        scoreBoard.setHighScore(firstScore);
        assertEquals(firstScore, scoreBoard.getHighScore(), "첫 번째 하이스코어가 올바르게 설정되어야 함");

        // 두 번째 설정
        int secondScore = 1500;
        scoreBoard.setHighScore(secondScore);
        assertEquals(secondScore, scoreBoard.getHighScore(), "두 번째 하이스코어가 올바르게 설정되어야 함");

        // 세 번째 설정 (더 낮은 점수)
        int thirdScore = 300;
        scoreBoard.setHighScore(thirdScore);
        assertEquals(thirdScore, scoreBoard.getHighScore(), "세 번째 하이스코어가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("음수 하이스코어 설정 테스트")
    void testNegativeHighScore() {
        int negativeScore = -100;
        scoreBoard.setHighScore(negativeScore);
        assertEquals(negativeScore, scoreBoard.getHighScore(), "음수 하이스코어도 설정될 수 있어야 함");
    }

    @Test
    @DisplayName("0 하이스코어 설정 테스트")
    void testZeroHighScore() {
        // 먼저 양수 값으로 설정
        scoreBoard.setHighScore(1000);
        assertEquals(1000, scoreBoard.getHighScore(), "양수 하이스코어가 설정되어야 함");

        // 0으로 재설정
        scoreBoard.setHighScore(0);
        assertEquals(0, scoreBoard.getHighScore(), "하이스코어가 0으로 재설정되어야 함");
    }

    @Test
    @DisplayName("매우 큰 하이스코어 설정 테스트")
    void testLargeHighScore() {
        int largeScore = Integer.MAX_VALUE;
        scoreBoard.setHighScore(largeScore);
        assertEquals(largeScore, scoreBoard.getHighScore(), "매우 큰 하이스코어가 설정되어야 함");
    }

    @Test
    @DisplayName("매우 작은 하이스코어 설정 테스트")
    void testSmallHighScore() {
        int smallScore = Integer.MIN_VALUE;
        scoreBoard.setHighScore(smallScore);
        assertEquals(smallScore, scoreBoard.getHighScore(), "매우 작은 하이스코어가 설정되어야 함");
    }

    @Test
    @DisplayName("getter와 setter 일관성 테스트")
    void testGetterSetterConsistency() {
        int[] testScores = {0, 100, 500, 1000, 2500, 10000, -50, -1000};
        
        for (int score : testScores) {
            scoreBoard.setHighScore(score);
            assertEquals(score, scoreBoard.getHighScore(), 
                "점수 " + score + "에 대해 getter와 setter가 일관성을 가져야 함");
        }
    }

    @Test
    @DisplayName("연속 설정 테스트")
    void testConsecutiveSettings() {
        // 연속적으로 점수를 설정하고 확인
        for (int i = 0; i < 100; i++) {
            int score = i * 100;
            scoreBoard.setHighScore(score);
            assertEquals(score, scoreBoard.getHighScore(), 
                "연속 설정 " + i + "번째에서 점수가 올바르게 설정되어야 함");
        }
    }

    @Test
    @DisplayName("초기 상태 다시 확인 테스트")
    void testInitialStateAgain() {
        // 새로운 ScoreBoard 인스턴스 생성
        ScoreBoard newScoreBoard = new ScoreBoard();
        assertEquals(0, newScoreBoard.getHighScore(), "새 인스턴스의 초기 하이스코어는 0이어야 함");
        
        // 기존 인스턴스와 독립적인지 확인
        scoreBoard.setHighScore(500);
        assertEquals(0, newScoreBoard.getHighScore(), "새 인스턴스는 기존 인스턴스와 독립적이어야 함");
        assertEquals(500, scoreBoard.getHighScore(), "기존 인스턴스의 값은 유지되어야 함");
    }

    @Test
    @DisplayName("하이스코어 불변성 테스트")
    void testHighScoreImmutability() {
        int originalScore = 1000;
        scoreBoard.setHighScore(originalScore);
        
        // getter로 값을 가져온 후 수정해도 원본에 영향이 없는지 확인
        int retrievedScore = scoreBoard.getHighScore();
        retrievedScore = retrievedScore + 500; // 지역 변수 수정
        
        // 원본 값은 변경되지 않아야 함
        assertEquals(originalScore, scoreBoard.getHighScore(), 
            "getter로 가져온 값을 수정해도 원본 값은 변경되지 않아야 함");
    }

    @Test
    @DisplayName("동일한 값 반복 설정 테스트")
    void testSameValueRepeatedSetting() {
        int score = 750;
        
        // 같은 값을 여러 번 설정
        for (int i = 0; i < 10; i++) {
            scoreBoard.setHighScore(score);
            assertEquals(score, scoreBoard.getHighScore(), 
                "동일한 값을 " + (i + 1) + "번째 설정할 때도 올바르게 저장되어야 함");
        }
    }

    @Test
    @DisplayName("경계값 테스트")
    void testBoundaryValues() {
        // Integer의 경계값들 테스트
        int[] boundaryValues = {
            Integer.MIN_VALUE,
            Integer.MIN_VALUE + 1,
            -1,
            0,
            1,
            Integer.MAX_VALUE - 1,
            Integer.MAX_VALUE
        };
        
        for (int value : boundaryValues) {
            scoreBoard.setHighScore(value);
            assertEquals(value, scoreBoard.getHighScore(), 
                "경계값 " + value + "가 올바르게 설정되어야 함");
        }
    }

    @Test
    @DisplayName("실수 값을 정수로 변환하여 설정 테스트")
    void testFloatingPointToIntegerConversion() {
        // 실수 값을 정수로 변환해서 설정하는 테스트
        double[] floatingValues = {
            9992.2,
            1000.7,
            -500.9,
            0.5,
            -0.3,
            999.999,
            -1000.1
        };
        
        int[] expectedIntValues = {
            (int) 9992.2,  // 9992
            (int) 1000.7,  // 1000
            (int) -500.9,  // -500
            (int) 0.5,     // 0
            (int) -0.3,    // 0
            (int) 999.999, // 999
            (int) -1000.1  // -1000
        };
        
        for (int i = 0; i < floatingValues.length; i++) {
            double originalValue = floatingValues[i];
            int convertedValue = (int) originalValue;
            int expectedValue = expectedIntValues[i];
            
            // 실수를 정수로 변환하여 설정
            scoreBoard.setHighScore(convertedValue);
            
            assertEquals(expectedValue, scoreBoard.getHighScore(), 
                "실수 " + originalValue + "을(를) 정수 " + expectedValue + "(으)로 변환하여 설정해야 함");
            assertEquals(convertedValue, scoreBoard.getHighScore(),
                "변환된 값 " + convertedValue + "이(가) 올바르게 저장되어야 함");
        }
    }

    @Test
    @DisplayName("Math.round()를 사용한 실수 반올림 테스트")
    void testFloatingPointRounding() {
        // Math.round()를 사용해서 실수를 반올림하는 테스트
        double[] floatingValues = {
            9992.2,   // 9992
            9992.5,   // 9993 (반올림)
            9992.7,   // 9993 (반올림)
            1000.4,   // 1000
            1000.6,   // 1001 (반올림)
            -500.3,   // -500
            -500.7,   // -501 (음수는 절댓값이 커지는 방향으로 반올림)
            0.4,      // 0
            0.6       // 1 (반올림)
        };
        
        for (double originalValue : floatingValues) {
            long roundedLong = Math.round(originalValue);
            int roundedInt = (int) roundedLong;
            
            scoreBoard.setHighScore(roundedInt);
            
            assertEquals(roundedInt, scoreBoard.getHighScore(),
                "실수 " + originalValue + "을(를) 반올림한 값 " + roundedInt + "이(가) 올바르게 설정되어야 함");
        }
    }

    @Test
    @DisplayName("극한 실수 값 처리 테스트")
    void testExtremeFloatingPointValues() {
        // 극한 실수 값들에 대한 테스트
        double[] extremeValues = {
            Double.MAX_VALUE,
            Double.MIN_VALUE,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NaN
        };
        
        for (double extremeValue : extremeValues) {
            try {
                int convertedValue;
                if (Double.isNaN(extremeValue)) {
                    convertedValue = 0; // NaN은 0으로 처리
                } else if (Double.isInfinite(extremeValue)) {
                    convertedValue = extremeValue > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                } else {
                    convertedValue = (int) extremeValue;
                }
                
                scoreBoard.setHighScore(convertedValue);
                assertEquals(convertedValue, scoreBoard.getHighScore(),
                    "극한값 " + extremeValue + " 처리 후 " + convertedValue + "이(가) 설정되어야 함");
                    
            } catch (Exception e) {
                // 예외가 발생할 수 있는 경우도 처리
                assertNotNull(e, "극한값 처리 중 예외가 발생할 수 있음: " + extremeValue);
            }
        }
    }
}