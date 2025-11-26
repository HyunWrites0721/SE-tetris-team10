package network;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.*;

/**
 * ConnectionManager 간단 테스트
 */
public class ConnectionManagerTest {
    
    @Test
    @DisplayName("연결 실패 테스트")
    public void testConnectionFailure() {
        ConnectionManager client = new ConnectionManager();
        
        // 존재하지 않는 서버에 연결 시도
        assertThrows(ConnectionException.class, () -> {
            client.connectToServer("localhost", 9999);
        });
        
        assertFalse(client.isConnected());
        client.disconnect();
    }
    
    @Test
    @DisplayName("기본 연결 테스트")
    public void testBasicConnection() throws Exception {
        ConnectionManager server = new ConnectionManager();
        ConnectionManager client = new ConnectionManager();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        try {
            // 서버 시작
            executor.submit(() -> {
                try {
                    server.startServer(12346); // 다른 포트 사용
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
            Thread.sleep(500);
            
            // 클라이언트 연결
            executor.submit(() -> {
                try {
                    client.connectToServer("localhost", 12346);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
            Thread.sleep(1000);
            
            // 연결 확인
            assertTrue(client.isConnected());
            assertTrue(server.isConnected());
            
        } finally {
            server.disconnect();
            client.disconnect();
            executor.shutdownNow();
        }
    }
}
