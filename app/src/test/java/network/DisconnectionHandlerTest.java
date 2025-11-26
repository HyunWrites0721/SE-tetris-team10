package network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DisconnectionHandler 테스트
 * 
 * 주의: 이 테스트는 UI 다이얼로그를 표시하므로 headless 환경에서는 실패할 수 있습니다.
 */
class DisconnectionHandlerTest {
    
    private DisconnectionHandler handler;
    private boolean callbackExecuted;
    
    @BeforeEach
    void setUp() {
        callbackExecuted = false;
        handler = new DisconnectionHandler(() -> {
            callbackExecuted = true;
        });
    }
    
    @Test
    void testIsHandled_Initial() {
        assertFalse(handler.isHandled(), "초기 상태에서는 처리되지 않음");
    }
    
    @Test
    void testReset() {
        // handleNormalDisconnection은 다이얼로그를 표시하지 않음
        handler.handleNormalDisconnection(null);
        
        // 잠시 대기
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {}
        
        // isHandled는 false지만 콜백은 실행됨
        assertTrue(callbackExecuted, "콜백이 실행되어야 함");
    }
    
    @Test
    void testNullCallback() {
        DisconnectionHandler nullHandler = new DisconnectionHandler(null);
        
        // 콜백이 null이어도 예외 없이 실행되어야 함
        assertDoesNotThrow(() -> {
            nullHandler.handleNormalDisconnection(null);
        });
    }
    
    @Test
    void testIsHandledAfterNormalDisconnection() {
        handler.handleNormalDisconnection(null);
        
        // handleNormalDisconnection은 isHandled를 true로 설정하지 않음
        assertFalse(handler.isHandled());
    }
}
