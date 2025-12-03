package game.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandSerializerTest {
    
    @Test
    void testSerializeLeftCommand() {
        GameCommand cmd = new GameCommand(GameCommand.Type.LEFT);
        
        String json = CommandSerializer.toJson(cmd);
        
        assertNotNull(json);
        assertTrue(json.contains("LEFT"));
        assertTrue(json.contains("version"));
    }
    
    @Test
    void testSerializeRightCommand() {
        GameCommand cmd = new GameCommand(GameCommand.Type.RIGHT);
        
        String json = CommandSerializer.toJson(cmd);
        
        assertNotNull(json);
        assertTrue(json.contains("RIGHT"));
    }
    
    @Test
    void testSerializeRotateCommand() {
        GameCommand cmd = new GameCommand(GameCommand.Type.ROTATE);
        
        String json = CommandSerializer.toJson(cmd);
        
        assertNotNull(json);
        assertTrue(json.contains("ROTATE"));
    }
    
    @Test
    void testSerializeSoftDropCommand() {
        GameCommand cmd = new GameCommand(GameCommand.Type.SOFT_DROP);
        
        String json = CommandSerializer.toJson(cmd);
        
        assertNotNull(json);
        assertTrue(json.contains("SOFT_DROP"));
    }
    
    @Test
    void testSerializeHardDropCommand() {
        GameCommand cmd = new GameCommand(GameCommand.Type.HARD_DROP);
        
        String json = CommandSerializer.toJson(cmd);
        
        assertNotNull(json);
        assertTrue(json.contains("HARD_DROP"));
    }
    
    @Test
    void testDeserializeLeftCommand() {
        GameCommand original = new GameCommand(GameCommand.Type.LEFT);
        String json = CommandSerializer.toJson(original);
        
        GameCommand deserialized = CommandSerializer.fromJson(json);
        
        assertNotNull(deserialized);
        assertEquals(GameCommand.Type.LEFT, deserialized.getType());
    }
    
    @Test
    void testDeserializeRightCommand() {
        GameCommand original = new GameCommand(GameCommand.Type.RIGHT);
        String json = CommandSerializer.toJson(original);
        
        GameCommand deserialized = CommandSerializer.fromJson(json);
        
        assertNotNull(deserialized);
        assertEquals(GameCommand.Type.RIGHT, deserialized.getType());
    }
    
    @Test
    void testDeserializeRotateCommand() {
        GameCommand original = new GameCommand(GameCommand.Type.ROTATE);
        String json = CommandSerializer.toJson(original);
        
        GameCommand deserialized = CommandSerializer.fromJson(json);
        
        assertNotNull(deserialized);
        assertEquals(GameCommand.Type.ROTATE, deserialized.getType());
    }
    
    @Test
    void testDeserializeSoftDropCommand() {
        GameCommand original = new GameCommand(GameCommand.Type.SOFT_DROP);
        String json = CommandSerializer.toJson(original);
        
        GameCommand deserialized = CommandSerializer.fromJson(json);
        
        assertNotNull(deserialized);
        assertEquals(GameCommand.Type.SOFT_DROP, deserialized.getType());
    }
    
    @Test
    void testDeserializeHardDropCommand() {
        GameCommand original = new GameCommand(GameCommand.Type.HARD_DROP);
        String json = CommandSerializer.toJson(original);
        
        GameCommand deserialized = CommandSerializer.fromJson(json);
        
        assertNotNull(deserialized);
        assertEquals(GameCommand.Type.HARD_DROP, deserialized.getType());
    }
    
    @Test
    void testRoundTripAllCommandTypes() {
        for (GameCommand.Type type : GameCommand.Type.values()) {
            GameCommand original = new GameCommand(type);
            String json = CommandSerializer.toJson(original);
            GameCommand deserialized = CommandSerializer.fromJson(json);
            
            assertNotNull(deserialized, "Failed for type: " + type);
            assertEquals(type, deserialized.getType(), "Failed for type: " + type);
        }
    }
    
    @Test
    void testDeserializeInvalidJson() {
        String invalidJson = "{invalid json}";
        
        assertThrows(Exception.class, () -> {
            CommandSerializer.fromJson(invalidJson);
        });
    }
    
    @Test
    void testDeserializeEmptyJson() {
        String emptyJson = "{}";
        
        GameCommand result = CommandSerializer.fromJson(emptyJson);
        
        // Should return envelope with null command
        assertNull(result);
    }
    
    @Test
    void testVersionInSerialization() {
        GameCommand cmd = new GameCommand(GameCommand.Type.LEFT);
        String json = CommandSerializer.toJson(cmd);
        
        assertTrue(json.contains("\"version\":1"));
    }
    
    @Test
    void testManualJsonWithVersion() {
        String json = "{\"version\":1,\"command\":{\"type\":\"LEFT\"}}";
        
        GameCommand cmd = CommandSerializer.fromJson(json);
        
        assertNotNull(cmd);
        assertEquals(GameCommand.Type.LEFT, cmd.getType());
    }
}
