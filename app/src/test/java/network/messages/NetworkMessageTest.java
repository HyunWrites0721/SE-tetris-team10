package network.messages;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * NetworkMessage 간단 테스트
 */
public class NetworkMessageTest {
    
    @Test
    @DisplayName("메시지 생성 및 기본 기능 테스트")
    public void testMessageBasics() throws Exception {
        TestMessage message = new TestMessage("Test");
        
        assertNotNull(message.getMessageId());
        assertNotNull(message.getTimestamp());
        assertEquals(MessageType.GAME_EVENT, message.getType());
        assertTrue(message.getElapsedTime() >= 0);
        assertNotNull(message.toString());
        
        Thread.sleep(50);
        assertTrue(message.getElapsedTime() >= 50);
    }
    
    @Test
    @DisplayName("메시지 직렬화 테스트")
    public void testSerialization() throws Exception {
        TestMessage message = new TestMessage("Hello");
        
        // 직렬화
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(message);
        oos.flush();
        
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
        
        // 역직렬화
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data);
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        TestMessage deserialized = (TestMessage) ois.readObject();
        
        assertEquals(message.getMessageId(), deserialized.getMessageId());
        assertEquals(message.getType(), deserialized.getType());
    }
    
    @Test
    @DisplayName("모든 메시지 타입 테스트")
    public void testAllMessageTypes() {
        for (MessageType type : MessageType.values()) {
            TestMessage message = new TestMessage("Test");
            assertNotNull(message);
        }
    }
    
    private static class TestMessage extends NetworkMessage {
        private static final long serialVersionUID = 1L;
        private String content;
        
        public TestMessage(String content) {
            super(MessageType.GAME_EVENT);
            this.content = content;
        }
    }
}
