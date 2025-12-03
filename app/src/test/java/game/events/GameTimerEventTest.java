package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameTimerEvent 테스트")
class GameTimerEventTest {
    
    @Test
    @DisplayName("START 액션 테스트")
    void testStartAction() {
        GameTimerEvent event = new GameTimerEvent(GameTimerEvent.TimerAction.START);
        
        assertNotNull(event);
        assertEquals(GameTimerEvent.TimerAction.START, event.getAction());
        assertEquals(-1, event.getNewDelay());
        assertEquals(-1, event.getSpeedLevel());
        assertEquals("TIMER", event.getEventType());
    }
    
    @Test
    @DisplayName("STOP 액션 테스트")
    void testStopAction() {
        GameTimerEvent event = new GameTimerEvent(GameTimerEvent.TimerAction.STOP);
        
        assertEquals(GameTimerEvent.TimerAction.STOP, event.getAction());
    }
    
    @Test
    @DisplayName("PAUSE 액션 테스트")
    void testPauseAction() {
        GameTimerEvent event = new GameTimerEvent(GameTimerEvent.TimerAction.PAUSE);
        
        assertEquals(GameTimerEvent.TimerAction.PAUSE, event.getAction());
    }
    
    @Test
    @DisplayName("RESUME 액션 테스트")
    void testResumeAction() {
        GameTimerEvent event = new GameTimerEvent(GameTimerEvent.TimerAction.RESUME);
        
        assertEquals(GameTimerEvent.TimerAction.RESUME, event.getAction());
    }
    
    @Test
    @DisplayName("SPEED_CHANGE 액션 테스트")
    void testSpeedChangeAction() {
        GameTimerEvent event = new GameTimerEvent(
            GameTimerEvent.TimerAction.SPEED_CHANGE, 
            500, 
            5
        );
        
        assertEquals(GameTimerEvent.TimerAction.SPEED_CHANGE, event.getAction());
        assertEquals(500, event.getNewDelay());
        assertEquals(5, event.getSpeedLevel());
    }
    
    @Test
    @DisplayName("직렬화 테스트")
    void testSerialization() {
        GameTimerEvent event = new GameTimerEvent(
            GameTimerEvent.TimerAction.SPEED_CHANGE, 
            300, 
            7
        );
        
        byte[] serialized = event.serialize();
        
        assertNotNull(serialized);
        assertEquals(20, serialized.length); // 8 + 4 + 4 + 4
    }
    
    @Test
    @DisplayName("fromBytes 팩토리 메서드 테스트")
    void testFromBytes() {
        GameTimerEvent original = new GameTimerEvent(
            GameTimerEvent.TimerAction.SPEED_CHANGE, 
            400, 
            8
        );
        
        byte[] serialized = original.serialize();
        GameTimerEvent deserialized = GameTimerEvent.fromBytes(serialized);
        
        assertEquals(original.getAction(), deserialized.getAction());
        assertEquals(original.getNewDelay(), deserialized.getNewDelay());
        assertEquals(original.getSpeedLevel(), deserialized.getSpeedLevel());
    }
    
    @Test
    @DisplayName("deserialize 호출 시 예외 발생 테스트")
    void testDeserializeThrowsException() {
        GameTimerEvent event = new GameTimerEvent(GameTimerEvent.TimerAction.START);
        byte[] data = event.serialize();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            event.deserialize(data);
        });
    }
    
    @Test
    @DisplayName("모든 액션 타입 테스트")
    void testAllActionTypes() {
        GameTimerEvent.TimerAction[] actions = GameTimerEvent.TimerAction.values();
        
        for (GameTimerEvent.TimerAction action : actions) {
            GameTimerEvent event = new GameTimerEvent(action, 100, 1);
            assertEquals(action, event.getAction());
        }
    }
    
    @Test
    @DisplayName("toString 테스트")
    void testToString() {
        GameTimerEvent event = new GameTimerEvent(
            GameTimerEvent.TimerAction.SPEED_CHANGE, 
            500, 
            5
        );
        
        String str = event.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("GameTimerEvent"));
        assertTrue(str.contains("SPEED_CHANGE"));
        assertTrue(str.contains("newDelay=500"));
        assertTrue(str.contains("speedLevel=5"));
    }
}
