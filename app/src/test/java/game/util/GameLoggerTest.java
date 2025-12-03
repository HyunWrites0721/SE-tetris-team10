package game.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;

/**
 * GameLogger 유틸리티 클래스 테스트
 */
class GameLoggerTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testLog_OutputsToConsole() {
        String testMessage = "Test log message";
        GameLogger.log(testMessage);
        
        String output = outContent.toString();
        assertTrue(output.contains(testMessage), "로그 메시지가 콘솔에 출력되어야 함");
        assertTrue(output.contains("["), "타임스탬프 형식이 포함되어야 함");
        assertTrue(output.contains("]"), "타임스탬프 형식이 포함되어야 함");
    }

    @Test
    void testError_OutputsWithPrefix() {
        String errorMessage = "Test error message";
        GameLogger.error(errorMessage);
        
        String output = outContent.toString();
        assertTrue(output.contains(errorMessage), "에러 메시지가 포함되어야 함");
        assertTrue(output.contains("❌ ERROR:"), "에러 접두사가 포함되어야 함");
    }

    @Test
    void testDebug_OutputsWithPrefix() {
        String debugMessage = "Test debug message";
        GameLogger.debug(debugMessage);
        
        String output = outContent.toString();
        assertTrue(output.contains(debugMessage), "디버그 메시지가 포함되어야 함");
        assertTrue(output.contains("[DEBUG]"), "디버그 접두사가 포함되어야 함");
    }

    @Test
    void testLog_MultipleMessages() {
        GameLogger.log("Message 1");
        GameLogger.log("Message 2");
        GameLogger.log("Message 3");
        
        String output = outContent.toString();
        assertTrue(output.contains("Message 1"));
        assertTrue(output.contains("Message 2"));
        assertTrue(output.contains("Message 3"));
    }

    @Test
    void testLog_EmptyMessage() {
        assertDoesNotThrow(() -> GameLogger.log(""));
        
        String output = outContent.toString();
        assertTrue(output.contains("["), "타임스탬프는 출력되어야 함");
    }

    @Test
    void testLog_NullMessage() {
        assertDoesNotThrow(() -> GameLogger.log(null));
    }

    @Test
    void testError_EmptyMessage() {
        assertDoesNotThrow(() -> GameLogger.error(""));
        
        String output = outContent.toString();
        assertTrue(output.contains("❌ ERROR:"));
    }

    @Test
    void testDebug_EmptyMessage() {
        assertDoesNotThrow(() -> GameLogger.debug(""));
        
        String output = outContent.toString();
        assertTrue(output.contains("[DEBUG]"));
    }

    @Test
    void testLog_SpecialCharacters() {
        String specialMessage = "테스트 메시지 !@#$%^&*()";
        GameLogger.log(specialMessage);
        
        String output = outContent.toString();
        assertTrue(output.contains(specialMessage));
    }

    @Test
    void testLog_MultilineMessage() {
        String multilineMessage = "Line 1\nLine 2\nLine 3";
        GameLogger.log(multilineMessage);
        
        String output = outContent.toString();
        assertTrue(output.contains("Line 1"));
        assertTrue(output.contains("Line 2"));
        assertTrue(output.contains("Line 3"));
    }

    @Test
    void testClose_DoesNotThrowException() {
        assertDoesNotThrow(() -> GameLogger.close());
    }

    @Test
    void testTimestampFormat() {
        GameLogger.log("timestamp test");
        
        String output = outContent.toString();
        // HH:mm:ss.SSS 형식 확인 (예: [12:34:56.789])
        // 멀티라인 모드로 정규식 매칭
        assertTrue(output.matches("(?s).*\\[\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\].*"), 
            "타임스탬프 형식이 [HH:mm:ss.SSS] 형태여야 함. 실제: " + output);
    }

    @Test
    void testLogFile_CreatedOnStartup() {
        File logFile = new File("tetris_debug.log");
        assertTrue(logFile.exists() || logFile.canRead(), 
            "로그 파일이 생성되어야 하거나 읽을 수 있어야 함");
    }

    @Test
    void testConcurrentLogging() throws InterruptedException {
        Thread[] threads = new Thread[5];
        
        for (int i = 0; i < threads.length; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    GameLogger.log("Thread " + threadNum + " message " + j);
                }
            });
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        String output = outContent.toString();
        // 모든 스레드의 메시지가 출력되었는지 확인
        for (int i = 0; i < threads.length; i++) {
            assertTrue(output.contains("Thread " + i));
        }
    }

    @Test
    void testLogOrdering() {
        GameLogger.log("First");
        GameLogger.error("Second");
        GameLogger.debug("Third");
        
        String output = outContent.toString();
        int firstPos = output.indexOf("First");
        int secondPos = output.indexOf("Second");
        int thirdPos = output.indexOf("Third");
        
        assertTrue(firstPos < secondPos);
        assertTrue(secondPos < thirdPos);
    }
}
