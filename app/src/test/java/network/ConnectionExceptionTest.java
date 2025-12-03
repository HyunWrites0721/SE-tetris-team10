package network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

/**
 * ConnectionException 클래스 테스트
 */
class ConnectionExceptionTest {

    @Test
    void testConstructor_WithMessage() {
        String message = "연결 실패";
        ConnectionException exception = new ConnectionException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructor_WithMessageAndCause() {
        String message = "네트워크 오류";
        Throwable cause = new IOException("소켓 연결 실패");
        
        ConnectionException exception = new ConnectionException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructor_WithNullMessage() {
        ConnectionException exception = new ConnectionException(null);
        assertNull(exception.getMessage());
    }

    @Test
    void testConstructor_WithNullCause() {
        String message = "오류 발생";
        ConnectionException exception = new ConnectionException(message, null);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionIsThrowable() {
        ConnectionException exception = new ConnectionException("테스트");
        assertThrows(ConnectionException.class, () -> {
            throw exception;
        });
    }

    @Test
    void testExceptionCanBeCaught() {
        try {
            throw new ConnectionException("테스트 예외");
        } catch (ConnectionException e) {
            assertEquals("테스트 예외", e.getMessage());
        }
    }

    @Test
    void testExceptionWithCauseCanBeCaught() {
        IOException cause = new IOException("원인");
        
        try {
            throw new ConnectionException("연결 오류", cause);
        } catch (ConnectionException e) {
            assertEquals("연결 오류", e.getMessage());
            assertTrue(e.getCause() instanceof IOException);
            assertEquals("원인", e.getCause().getMessage());
        }
    }

    @Test
    void testInheritance() {
        ConnectionException exception = new ConnectionException("테스트");
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void testStackTrace() {
        ConnectionException exception = new ConnectionException("스택 트레이스 테스트");
        StackTraceElement[] stackTrace = exception.getStackTrace();
        
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
    }

    @Test
    void testCausePreservesStackTrace() {
        Exception originalCause = new RuntimeException("원래 오류");
        ConnectionException exception = new ConnectionException("래핑된 오류", originalCause);
        
        assertSame(originalCause, exception.getCause());
        assertNotNull(originalCause.getStackTrace());
    }

    @Test
    void testMultipleExceptionChaining() {
        Exception root = new IOException("루트 원인");
        Exception middle = new RuntimeException("중간 원인", root);
        ConnectionException top = new ConnectionException("최종 오류", middle);
        
        assertEquals(middle, top.getCause());
        assertEquals(root, top.getCause().getCause());
    }

    @Test
    void testExceptionMessage_EmptyString() {
        ConnectionException exception = new ConnectionException("");
        assertEquals("", exception.getMessage());
    }

    @Test
    void testExceptionMessage_SpecialCharacters() {
        String message = "오류: !@#$%^&*()";
        ConnectionException exception = new ConnectionException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testExceptionMessage_Multiline() {
        String message = "첫 번째 줄\n두 번째 줄\n세 번째 줄";
        ConnectionException exception = new ConnectionException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testToString() {
        ConnectionException exception = new ConnectionException("테스트 메시지");
        String str = exception.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("ConnectionException"));
        assertTrue(str.contains("테스트 메시지"));
    }
}
