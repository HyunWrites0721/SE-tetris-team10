package game.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 게임 로그를 파일과 콘솔에 동시에 출력하는 유틸리티
 */
public class GameLogger {
    private static PrintWriter logWriter;
    private static final String LOG_FILE = "tetris_debug.log";
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    static {
        try {
            logWriter = new PrintWriter(new FileWriter(LOG_FILE, false), true); // false = 덮어쓰기
            log("=== 게임 로그 시작 ===");
        } catch (IOException e) {
            System.err.println("로그 파일 생성 실패: " + e.getMessage());
        }
    }
    
    /**
     * 로그 메시지 출력 (콘솔 + 파일)
     */
    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String logMessage = "[" + timestamp + "] " + message;
        
        // 콘솔 출력
        System.out.println(logMessage);
        
        // 파일 출력
        if (logWriter != null) {
            logWriter.println(logMessage);
            logWriter.flush();
        }
    }
    
    /**
     * 에러 로그 출력
     */
    public static void error(String message) {
        log("❌ ERROR: " + message);
    }
    
    /**
     * 디버그 로그 출력
     */
    public static void debug(String message) {
        log("[DEBUG] " + message);
    }
    
    /**
     * 로그 파일 닫기
     */
    public static void close() {
        if (logWriter != null) {
            log("=== 게임 로그 종료 ===");
            logWriter.close();
        }
    }
}
