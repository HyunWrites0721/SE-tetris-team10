package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ItemActivatedEvent 테스트")
class ItemActivatedEventTest {
    
    @Test
    @DisplayName("ItemActivatedEvent 생성 테스트")
    void testItemActivatedEventCreation() {
        ItemActivatedEvent event = new ItemActivatedEvent("ALL_CLEAR", 1);
        
        assertNotNull(event);
        assertEquals("ALL_CLEAR", event.getItemType());
        assertEquals(1, event.getPlayerId());
        assertEquals("ITEM_ACTIVATED", event.getEventType());
    }
    
    @Test
    @DisplayName("여러 아이템 타입 테스트")
    void testDifferentItemTypes() {
        String[] itemTypes = {"ALL_CLEAR", "BOX_CLEAR", "ONE_LINE_CLEAR", "WEIGHT"};
        
        for (String itemType : itemTypes) {
            ItemActivatedEvent event = new ItemActivatedEvent(itemType, 1);
            assertEquals(itemType, event.getItemType());
        }
    }
    
    @Test
    @DisplayName("직렬화/역직렬화 테스트")
    void testSerializationRoundTrip() {
        ItemActivatedEvent original = new ItemActivatedEvent("BOX_CLEAR", 2);
        
        byte[] serialized = original.serialize();
        
        ItemActivatedEvent deserialized = new ItemActivatedEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(original.getItemType(), deserialized.getItemType());
        assertEquals(original.getPlayerId(), deserialized.getPlayerId());
    }
    
    @Test
    @DisplayName("긴 아이템 이름 테스트")
    void testLongItemName() {
        String longItemName = "VERY_LONG_ITEM_NAME_FOR_TESTING";
        ItemActivatedEvent event = new ItemActivatedEvent(longItemName, 1);
        
        assertEquals(longItemName, event.getItemType());
        
        byte[] serialized = event.serialize();
        ItemActivatedEvent deserialized = new ItemActivatedEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(longItemName, deserialized.getItemType());
    }
    
    @Test
    @DisplayName("빈 아이템 이름 테스트")
    void testEmptyItemName() {
        ItemActivatedEvent event = new ItemActivatedEvent("", 1);
        
        assertEquals("", event.getItemType());
        
        byte[] serialized = event.serialize();
        ItemActivatedEvent deserialized = new ItemActivatedEvent();
        deserialized.deserialize(serialized);
        
        assertEquals("", deserialized.getItemType());
    }
    
    @Test
    @DisplayName("toString 테스트")
    void testToString() {
        ItemActivatedEvent event = new ItemActivatedEvent("ALL_CLEAR", 1);
        
        String str = event.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("ItemActivatedEvent"));
        assertTrue(str.contains("ALL_CLEAR"));
        assertTrue(str.contains("playerId=1"));
    }
}
