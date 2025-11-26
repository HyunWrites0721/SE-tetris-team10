package network;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * NetworkManager 간단 테스트
 */
public class NetworkManagerTest {
    
    @Test
    @DisplayName("NetworkManager 기본 생성 테스트")
    public void testBasicCreation() {
        NetworkManager manager = new NetworkManager();
        
        assertNotNull(manager);
        assertEquals(ConnectionState.DISCONNECTED, manager.getState());
        assertFalse(manager.isConnected());
        
        manager.disconnect();
    }
    
    @Test
    @DisplayName("서버-클라이언트 연결 테스트")
    public void testServerClientConnection() throws Exception {
        NetworkManager server = new NetworkManager();
        NetworkManager client = new NetworkManager();
        
        try {
            // 서버 시작 (별도 스레드)
            Thread serverThread = new Thread(() -> {
                try {
                    server.startAsServer(12347);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            serverThread.start();
            
            Thread.sleep(1000);
            
            // 클라이언트 연결
            client.connectAsClient("localhost", 12347);
            
            Thread.sleep(1000);
            
            // 연결 확인
            assertTrue(server.isConnected());
            assertTrue(client.isConnected());
            assertEquals(NetworkRole.SERVER, server.getRole());
            assertEquals(NetworkRole.CLIENT, client.getRole());
            
        } finally {
            client.disconnect();
            server.disconnect();
        }
    }
}
