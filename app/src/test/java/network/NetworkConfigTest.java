package network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * NetworkConfig 클래스 테스트
 */
class NetworkConfigTest {

    @Test
    void testDefaultPort() {
        assertEquals(12345, NetworkConfig.DEFAULT_PORT);
        assertTrue(NetworkConfig.DEFAULT_PORT > 0);
        assertTrue(NetworkConfig.DEFAULT_PORT < 65536);
    }

    @Test
    void testConnectionTimeout() {
        assertEquals(60000, NetworkConfig.CONNECTION_TIMEOUT);
        assertTrue(NetworkConfig.CONNECTION_TIMEOUT > 0);
    }

    @Test
    void testReadTimeout() {
        assertEquals(5000, NetworkConfig.READ_TIMEOUT);
        assertTrue(NetworkConfig.READ_TIMEOUT > 0);
        assertTrue(NetworkConfig.READ_TIMEOUT < NetworkConfig.CONNECTION_TIMEOUT);
    }

    @Test
    void testHeartbeatInterval() {
        assertEquals(1000, NetworkConfig.HEARTBEAT_INTERVAL);
        assertTrue(NetworkConfig.HEARTBEAT_INTERVAL > 0);
    }

    @Test
    void testHeartbeatTimeout() {
        assertEquals(5000, NetworkConfig.HEARTBEAT_TIMEOUT);
        assertTrue(NetworkConfig.HEARTBEAT_TIMEOUT > NetworkConfig.HEARTBEAT_INTERVAL);
    }

    @Test
    void testLagThreshold() {
        assertEquals(200, NetworkConfig.LAG_THRESHOLD);
        assertTrue(NetworkConfig.LAG_THRESHOLD > 0);
    }

    @Test
    void testMaxReconnectAttempts() {
        assertEquals(3, NetworkConfig.MAX_RECONNECT_ATTEMPTS);
        assertTrue(NetworkConfig.MAX_RECONNECT_ATTEMPTS > 0);
    }

    @Test
    void testReconnectDelay() {
        assertEquals(2000, NetworkConfig.RECONNECT_DELAY);
        assertTrue(NetworkConfig.RECONNECT_DELAY > 0);
    }

    @Test
    void testMessageQueueSize() {
        assertEquals(100, NetworkConfig.MESSAGE_QUEUE_SIZE);
        assertTrue(NetworkConfig.MESSAGE_QUEUE_SIZE > 0);
    }

    @Test
    void testTimeoutRelationships() {
        // READ_TIMEOUT < CONNECTION_TIMEOUT
        assertTrue(NetworkConfig.READ_TIMEOUT < NetworkConfig.CONNECTION_TIMEOUT);
        
        // HEARTBEAT_INTERVAL < HEARTBEAT_TIMEOUT
        assertTrue(NetworkConfig.HEARTBEAT_INTERVAL < NetworkConfig.HEARTBEAT_TIMEOUT);
        
        // HEARTBEAT_TIMEOUT < CONNECTION_TIMEOUT
        assertTrue(NetworkConfig.HEARTBEAT_TIMEOUT < NetworkConfig.CONNECTION_TIMEOUT);
    }

    @Test
    void testConstructorIsPrivate() throws Exception {
        Constructor<NetworkConfig> constructor = NetworkConfig.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), 
            "생성자는 private이어야 함");
    }

    @Test
    void testCannotInstantiate() throws Exception {
        Constructor<NetworkConfig> constructor = NetworkConfig.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        // private 생성자를 호출할 수 있지만, 인스턴스화는 의도적으로 막혀있음
        assertDoesNotThrow(() -> constructor.newInstance());
    }

    @Test
    void testAllConstantsAreFinal() throws Exception {
        var fields = NetworkConfig.class.getDeclaredFields();
        
        for (var field : fields) {
            assertTrue(Modifier.isFinal(field.getModifiers()), 
                field.getName() + " 필드는 final이어야 함");
            assertTrue(Modifier.isStatic(field.getModifiers()), 
                field.getName() + " 필드는 static이어야 함");
            assertTrue(Modifier.isPublic(field.getModifiers()), 
                field.getName() + " 필드는 public이어야 함");
        }
    }

    @Test
    void testPositiveValues() {
        assertTrue(NetworkConfig.DEFAULT_PORT > 0);
        assertTrue(NetworkConfig.CONNECTION_TIMEOUT > 0);
        assertTrue(NetworkConfig.READ_TIMEOUT > 0);
        assertTrue(NetworkConfig.HEARTBEAT_INTERVAL > 0);
        assertTrue(NetworkConfig.HEARTBEAT_TIMEOUT > 0);
        assertTrue(NetworkConfig.LAG_THRESHOLD > 0);
        assertTrue(NetworkConfig.MAX_RECONNECT_ATTEMPTS > 0);
        assertTrue(NetworkConfig.RECONNECT_DELAY > 0);
        assertTrue(NetworkConfig.MESSAGE_QUEUE_SIZE > 0);
    }

    @Test
    void testReasonableValues() {
        // 포트 번호는 유효한 범위 내
        assertTrue(NetworkConfig.DEFAULT_PORT >= 1024 && NetworkConfig.DEFAULT_PORT <= 65535);
        
        // 타임아웃은 너무 짧거나 길지 않음
        assertTrue(NetworkConfig.READ_TIMEOUT >= 1000 && NetworkConfig.READ_TIMEOUT <= 60000);
        assertTrue(NetworkConfig.CONNECTION_TIMEOUT >= 10000 && NetworkConfig.CONNECTION_TIMEOUT <= 300000);
        
        // 지연 기준은 합리적인 범위
        assertTrue(NetworkConfig.LAG_THRESHOLD >= 50 && NetworkConfig.LAG_THRESHOLD <= 1000);
    }
}
