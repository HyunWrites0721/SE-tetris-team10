package game.listeners;

import game.events.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NetworkListener 테스트")
class NetworkListenerTest {
    
    private NetworkListener listener;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    void setUp() {
        listener = new NetworkListener(true, "TestPlayer");
        
        // System.out 캡처 설정
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }
    
    @Test
    @DisplayName("NetworkListener 생성 - 네트워크 활성화")
    void testNetworkListenerCreationEnabled() {
        NetworkListener listener = new NetworkListener(true, "Player1");
        assertNotNull(listener);
    }
    
    @Test
    @DisplayName("NetworkListener 생성 - 네트워크 비활성화")
    void testNetworkListenerCreationDisabled() {
        NetworkListener listener = new NetworkListener(false, "Player1");
        assertNotNull(listener);
    }
    
    @Test
    @DisplayName("NetworkListener 생성 - null 플레이어 이름")
    void testNetworkListenerCreationNullName() {
        NetworkListener listener = new NetworkListener(true, null);
        assertNotNull(listener);
    }
    
    @Test
    @DisplayName("네트워크 활성화 상태에서 이벤트 전송")
    void testOnEventWithNetworkEnabled() {
        BlockPlacedEvent event = new BlockPlacedEvent(5, 10, 1, 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Network: Sending BLOCK_PLACED event"));
        assertTrue(output.contains("from TestPlayer"));
    }
    
    @Test
    @DisplayName("네트워크 비활성화 상태에서 이벤트 전송하지 않음")
    void testOnEventWithNetworkDisabled() {
        listener.setNetworkEnabled(false);
        BlockPlacedEvent event = new BlockPlacedEvent(5, 10, 1, 1);
        
        listener.onEvent(event);
        
        String output = outputStream.toString();
        assertFalse(output.contains("Network: Sending"));
    }
    
    @Test
    @DisplayName("setNetworkEnabled로 네트워크 상태 변경")
    void testSetNetworkEnabled() {
        listener.setNetworkEnabled(false);
        BlockPlacedEvent event1 = new BlockPlacedEvent(5, 10, 1, 1);
        listener.onEvent(event1);
        
        String output1 = outputStream.toString();
        assertFalse(output1.contains("Network: Sending"));
        
        outputStream.reset();
        
        listener.setNetworkEnabled(true);
        BlockPlacedEvent event2 = new BlockPlacedEvent(6, 11, 2, 1);
        listener.onEvent(event2);
        
        String output2 = outputStream.toString();
        assertTrue(output2.contains("Network: Sending"));
    }
    
    @Test
    @DisplayName("여러 이벤트 타입 전송")
    void testMultipleEventTypes() {
        listener.onEvent(new BlockPlacedEvent(5, 10, 1, 1));
        listener.onEvent(new LineClearedEvent(new int[]{0}, 100, 1));
        listener.onEvent(new LevelUpEvent(2, 1));
        
        String output = outputStream.toString();
        assertTrue(output.contains("BLOCK_PLACED"));
        assertTrue(output.contains("LINE_CLEARED"));
        assertTrue(output.contains("LEVEL_UP"));
    }
    
    @Test
    @DisplayName("네트워크로부터 BLOCK_PLACED 이벤트 수신")
    void testReceiveBlockPlacedEvent() {
        BlockPlacedEvent originalEvent = new BlockPlacedEvent(5, 10, 2, 1);
        byte[] data = originalEvent.serialize();
        
        GameEvent receivedEvent = listener.receiveEventFromNetwork("BLOCK_PLACED", data);
        
        assertNotNull(receivedEvent);
        assertInstanceOf(BlockPlacedEvent.class, receivedEvent);
        BlockPlacedEvent placedEvent = (BlockPlacedEvent) receivedEvent;
        assertEquals(5, placedEvent.getX());
        assertEquals(10, placedEvent.getY());
        assertEquals(2, placedEvent.getBlockType());
    }
    
    @Test
    @DisplayName("네트워크로부터 LINE_CLEARED 이벤트 수신")
    void testReceiveLineClearedEvent() {
        LineClearedEvent originalEvent = new LineClearedEvent(new int[]{0, 1}, 200, 1);
        byte[] data = originalEvent.serialize();
        
        GameEvent receivedEvent = listener.receiveEventFromNetwork("LINE_CLEARED", data);
        
        assertNotNull(receivedEvent);
        assertInstanceOf(LineClearedEvent.class, receivedEvent);
        LineClearedEvent lineEvent = (LineClearedEvent) receivedEvent;
        assertEquals(2, lineEvent.getClearedLines().length);
        assertEquals(200, lineEvent.getScore());
    }
    
    @Test
    @DisplayName("네트워크로부터 LEVEL_UP 이벤트 수신")
    void testReceiveLevelUpEvent() {
        LevelUpEvent originalEvent = new LevelUpEvent(5, 1);
        byte[] data = originalEvent.serialize();
        
        GameEvent receivedEvent = listener.receiveEventFromNetwork("LEVEL_UP", data);
        
        assertNotNull(receivedEvent);
        assertInstanceOf(LevelUpEvent.class, receivedEvent);
        LevelUpEvent levelEvent = (LevelUpEvent) receivedEvent;
        assertEquals(5, levelEvent.getNewLevel());
    }
    
    @Test
    @DisplayName("네트워크로부터 GAME_OVER 이벤트 수신")
    void testReceiveGameOverEvent() {
        GameOverEvent originalEvent = new GameOverEvent(10000, 1);
        byte[] data = originalEvent.serialize();
        
        GameEvent receivedEvent = listener.receiveEventFromNetwork("GAME_OVER", data);
        
        assertNotNull(receivedEvent);
        assertInstanceOf(GameOverEvent.class, receivedEvent);
        GameOverEvent gameOverEvent = (GameOverEvent) receivedEvent;
        assertEquals(10000, gameOverEvent.getFinalScore());
    }
    
    @Test
    @DisplayName("네트워크로부터 ITEM_ACTIVATED 이벤트 수신")
    void testReceiveItemActivatedEvent() {
        ItemActivatedEvent originalEvent = new ItemActivatedEvent("CLEAR_LINE", 1);
        byte[] data = originalEvent.serialize();
        
        GameEvent receivedEvent = listener.receiveEventFromNetwork("ITEM_ACTIVATED", data);
        
        assertNotNull(receivedEvent);
        assertInstanceOf(ItemActivatedEvent.class, receivedEvent);
        ItemActivatedEvent itemEvent = (ItemActivatedEvent) receivedEvent;
        assertEquals("CLEAR_LINE", itemEvent.getItemType());
    }
    
    @Test
    @DisplayName("알 수 없는 이벤트 타입 수신")
    void testReceiveUnknownEventType() {
        // System.err 캡처 설정
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errorStream));
        
        GameEvent receivedEvent = listener.receiveEventFromNetwork("UNKNOWN_EVENT", new byte[10]);
        
        assertNull(receivedEvent);
        String errorOutput = errorStream.toString();
        assertTrue(errorOutput.contains("Unknown event type"));
        
        // System.err 복원
        System.setErr(originalErr);
    }
    
    @Test
    @DisplayName("잘못된 데이터로 이벤트 수신")
    void testReceiveInvalidData() {
        byte[] invalidData = new byte[]{1, 2, 3};
        
        GameEvent receivedEvent = listener.receiveEventFromNetwork("BLOCK_PLACED", invalidData);
        
        // 역직렬화 실패 시 null 반환되어야 함
        String output = outputStream.toString();
        assertTrue(output.contains("Failed to deserialize") || receivedEvent == null);
    }
    
    @Test
    void tearDown() {
        // System.out 복원
        System.setOut(originalOut);
    }
}
