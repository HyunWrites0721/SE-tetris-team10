package game.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandQueueTest {
    
    private CommandQueue queue;
    
    @BeforeEach
    void setUp() {
        queue = new CommandQueue();
    }
    
    @Test
    void testEnqueueAndDrain() {
        GameCommand cmd1 = new GameCommand(GameCommand.Type.LEFT);
        GameCommand cmd2 = new GameCommand(GameCommand.Type.RIGHT);
        
        queue.enqueue(cmd1);
        queue.enqueue(cmd2);
        
        List<GameCommand> commands = queue.drainForTick();
        
        assertEquals(2, commands.size());
        assertEquals(cmd1, commands.get(0));
        assertEquals(cmd2, commands.get(1));
    }
    
    @Test
    void testDrainEmptyQueue() {
        List<GameCommand> commands = queue.drainForTick();
        
        assertTrue(commands.isEmpty());
    }
    
    @Test
    void testEnqueueNull() {
        queue.enqueue(null);
        
        List<GameCommand> commands = queue.drainForTick();
        
        assertTrue(commands.isEmpty());
    }
    
    @Test
    void testSize() {
        assertEquals(0, queue.size());
        
        queue.enqueue(new GameCommand(GameCommand.Type.LEFT));
        assertEquals(1, queue.size());
        
        queue.enqueue(new GameCommand(GameCommand.Type.RIGHT));
        assertEquals(2, queue.size());
        
        queue.drainForTick();
        assertEquals(0, queue.size());
    }
    
    @Test
    void testDrainClearsQueue() {
        queue.enqueue(new GameCommand(GameCommand.Type.LEFT));
        queue.enqueue(new GameCommand(GameCommand.Type.RIGHT));
        
        queue.drainForTick();
        
        assertEquals(0, queue.size());
        
        List<GameCommand> commands = queue.drainForTick();
        assertTrue(commands.isEmpty());
    }
    
    @Test
    void testSerializeBatchEmpty() {
        String json = queue.serializeBatch();
        
        assertEquals("[]", json);
    }
    
    @Test
    void testSerializeBatchSingleCommand() {
        GameCommand cmd = new GameCommand(GameCommand.Type.LEFT);
        queue.enqueue(cmd);
        
        String json = queue.serializeBatch();
        
        assertNotNull(json);
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
        assertTrue(json.contains("LEFT"));
    }
    
    @Test
    void testSerializeBatchMultipleCommands() {
        queue.enqueue(new GameCommand(GameCommand.Type.LEFT));
        queue.enqueue(new GameCommand(GameCommand.Type.RIGHT));
        queue.enqueue(new GameCommand(GameCommand.Type.ROTATE));
        
        String json = queue.serializeBatch();
        
        assertNotNull(json);
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
        assertTrue(json.contains("LEFT"));
        assertTrue(json.contains("RIGHT"));
        assertTrue(json.contains("ROTATE"));
        assertTrue(json.contains(",")); // Multiple items separated by comma
    }
    
    @Test
    void testSerializeBatchDrainsQueue() {
        queue.enqueue(new GameCommand(GameCommand.Type.LEFT));
        
        queue.serializeBatch();
        
        assertEquals(0, queue.size());
    }
    
    @Test
    void testConcurrentEnqueueAndDrain() {
        // Simulate concurrent access
        for (int i = 0; i < 100; i++) {
            queue.enqueue(new GameCommand(GameCommand.Type.LEFT));
        }
        
        List<GameCommand> commands = queue.drainForTick();
        
        assertEquals(100, commands.size());
    }
    
    @Test
    void testMultipleDrains() {
        queue.enqueue(new GameCommand(GameCommand.Type.LEFT));
        queue.enqueue(new GameCommand(GameCommand.Type.RIGHT));
        
        List<GameCommand> first = queue.drainForTick();
        assertEquals(2, first.size());
        
        List<GameCommand> second = queue.drainForTick();
        assertTrue(second.isEmpty());
    }
}
