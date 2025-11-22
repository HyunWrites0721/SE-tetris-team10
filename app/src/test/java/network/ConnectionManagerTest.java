package network;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;

/**
 * ConnectionManager 테스트
 */
public class ConnectionManagerTest {
    
    private ConnectionManager server;
    private ConnectionManager client;
    private ExecutorService executor;
    
    @BeforeEach
    public void setUp() {
        server = new ConnectionManager();
        client = new ConnectionManager();
        executor = Executors.newFixedThreadPool(2);
    }
    
    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.disconnect();
        }
        if (client != null) {
            client.disconnect();
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }
    
    @Test
    @DisplayName("서버 시작 테스트")
    public void testServerStart() throws Exception {
        // 서버를 별도 스레드에서 시작
        Future<?> serverFuture = executor.submit(() -> {
            try {
                server.startServer(NetworkConfig.DEFAULT_PORT);
            } catch (ConnectionException e) {
                fail("서버 시작 실패: " + e.getMessage());
            }
        });
        
        // 서버가 시작될 때까지 대기
        Thread.sleep(500);
        
        // 서버가 실행 중인지 확인
        assertTrue(server.isServerRunning(), "서버가 실행 중이어야 함");
        assertEquals(NetworkRole.SERVER, server.getRole(), "역할이 SERVER여야 함");
        assertNotNull(server.getLocalAddress(), "로컬 주소가 있어야 함");
        
        // 클라이언트 연결
        client.connectToServer("localhost", NetworkConfig.DEFAULT_PORT);
        
        // 서버 accept 완료 대기
        serverFuture.get(2, TimeUnit.SECONDS);
        
        // 양쪽 모두 연결되었는지 확인
        assertTrue(server.isConnected(), "서버가 연결되어야 함");
        assertTrue(client.isConnected(), "클라이언트가 연결되어야 함");
    }
    
    @Test
    @DisplayName("클라이언트 연결 테스트")
    public void testClientConnection() throws Exception {
        // 서버를 별도 스레드에서 시작
        executor.submit(() -> {
            try {
                server.startServer(NetworkConfig.DEFAULT_PORT);
            } catch (ConnectionException e) {
                fail("서버 시작 실패: " + e.getMessage());
            }
        });
        
        // 서버가 시작될 때까지 대기
        Thread.sleep(500);
        
        // 클라이언트 연결
        client.connectToServer("localhost", NetworkConfig.DEFAULT_PORT);
        
        // 연결 확인
        assertTrue(client.isConnected(), "클라이언트가 연결되어야 함");
        assertTrue(client.isClient(), "역할이 CLIENT여야 함");
        assertEquals(NetworkRole.CLIENT, client.getRole());
        assertEquals("localhost", client.getRemoteAddress());
    }
    
    @Test
    @DisplayName("연결 실패 테스트 - 잘못된 포트")
    public void testConnectionFailure() {
        // 존재하지 않는 서버에 연결 시도
        assertThrows(ConnectionException.class, () -> {
            client.connectToServer("localhost", 9999);
        }, "잘못된 포트 연결 시 예외 발생해야 함");
        
        assertFalse(client.isConnected(), "연결 실패 시 isConnected가 false여야 함");
    }
    
    @Test
    @DisplayName("연결 종료 테스트")
    public void testDisconnection() throws Exception {
        // 서버 시작
        executor.submit(() -> {
            try {
                server.startServer(NetworkConfig.DEFAULT_PORT);
            } catch (ConnectionException e) {
                fail("서버 시작 실패");
            }
        });
        
        Thread.sleep(500);
        
        // 클라이언트 연결
        client.connectToServer("localhost", NetworkConfig.DEFAULT_PORT);
        
        // 연결 확인
        assertTrue(client.isConnected());
        
        // 연결 종료
        client.disconnect();
        
        // 종료 확인
        assertFalse(client.isConnected(), "disconnect 후 isConnected가 false여야 함");
        assertEquals(ConnectionState.DISCONNECTED, client.getState());
    }
    
    @Test
    @DisplayName("스트림 가져오기 테스트")
    public void testStreamAccess() throws Exception {
        // 서버 시작
        executor.submit(() -> {
            try {
                server.startServer(NetworkConfig.DEFAULT_PORT);
            } catch (ConnectionException e) {
                fail("서버 시작 실패");
            }
        });
        
        Thread.sleep(500);
        
        // 클라이언트 연결
        client.connectToServer("localhost", NetworkConfig.DEFAULT_PORT);
        
        Thread.sleep(100);
        
        // 스트림 확인
        assertNotNull(client.getOutputStream(), "OutputStream이 null이 아니어야 함");
        assertNotNull(client.getInputStream(), "InputStream이 null이 아니어야 함");
        assertNotNull(server.getOutputStream(), "서버 OutputStream이 null이 아니어야 함");
        assertNotNull(server.getInputStream(), "서버 InputStream이 null이 아니어야 함");
    }
    
    @Test
    @DisplayName("로컬 IP 주소 가져오기 테스트")
    public void testGetLocalIPAddress() throws Exception {
        // 서버 시작
        executor.submit(() -> {
            try {
                server.startServer(NetworkConfig.DEFAULT_PORT);
            } catch (ConnectionException e) {
                fail("서버 시작 실패");
            }
        });
        
        Thread.sleep(500);
        
        // 로컬 IP 확인
        String localIP = server.getLocalAddress();
        assertNotNull(localIP, "로컬 IP가 null이 아니어야 함");
        assertFalse(localIP.isEmpty(), "로컬 IP가 비어있지 않아야 함");
        assertTrue(localIP.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"), "IP 형식이어야 함");
        
        System.out.println("Local IP: " + localIP);
    }
}
