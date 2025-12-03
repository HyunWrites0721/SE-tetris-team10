package game.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KeyboardInputControllerTest {
    
    @Mock
    private Component mockComponent;
    
    @Mock
    private Player mockPlayer;
    
    private KeyboardInputController controller;
    private KeyListener capturedListener;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new KeyboardInputController(mockComponent);
        
        // Capture the KeyListener that gets added
        doAnswer(invocation -> {
            capturedListener = invocation.getArgument(0);
            return null;
        }).when(mockComponent).addKeyListener(any(KeyListener.class));
    }
    
    @Test
    void testBind() {
        controller.bind(mockPlayer);
        
        verify(mockComponent).addKeyListener(any(KeyListener.class));
        assertNotNull(capturedListener);
    }
    
    @Test
    void testUnbind() {
        controller.bind(mockPlayer);
        controller.unbind();
        
        verify(mockComponent).removeKeyListener(any(KeyListener.class));
    }
    
    @Test
    void testLeftArrowKey() {
        controller.bind(mockPlayer);
        
        KeyEvent event = createKeyEvent(KeyEvent.VK_LEFT);
        capturedListener.keyPressed(event);
        
        ArgumentCaptor<GameCommand> captor = ArgumentCaptor.forClass(GameCommand.class);
        verify(mockPlayer).enqueueCommand(captor.capture());
        
        assertEquals(GameCommand.Type.LEFT, captor.getValue().getType());
    }
    
    @Test
    void testRightArrowKey() {
        controller.bind(mockPlayer);
        
        KeyEvent event = createKeyEvent(KeyEvent.VK_RIGHT);
        capturedListener.keyPressed(event);
        
        ArgumentCaptor<GameCommand> captor = ArgumentCaptor.forClass(GameCommand.class);
        verify(mockPlayer).enqueueCommand(captor.capture());
        
        assertEquals(GameCommand.Type.RIGHT, captor.getValue().getType());
    }
    
    @Test
    void testUpArrowKey() {
        controller.bind(mockPlayer);
        
        KeyEvent event = createKeyEvent(KeyEvent.VK_UP);
        capturedListener.keyPressed(event);
        
        ArgumentCaptor<GameCommand> captor = ArgumentCaptor.forClass(GameCommand.class);
        verify(mockPlayer).enqueueCommand(captor.capture());
        
        assertEquals(GameCommand.Type.ROTATE, captor.getValue().getType());
    }
    
    @Test
    void testDownArrowKey() {
        controller.bind(mockPlayer);
        
        KeyEvent event = createKeyEvent(KeyEvent.VK_DOWN);
        capturedListener.keyPressed(event);
        
        ArgumentCaptor<GameCommand> captor = ArgumentCaptor.forClass(GameCommand.class);
        verify(mockPlayer).enqueueCommand(captor.capture());
        
        assertEquals(GameCommand.Type.SOFT_DROP, captor.getValue().getType());
    }
    
    @Test
    void testSpaceKey() {
        controller.bind(mockPlayer);
        
        KeyEvent event = createKeyEvent(KeyEvent.VK_SPACE);
        capturedListener.keyPressed(event);
        
        ArgumentCaptor<GameCommand> captor = ArgumentCaptor.forClass(GameCommand.class);
        verify(mockPlayer).enqueueCommand(captor.capture());
        
        assertEquals(GameCommand.Type.HARD_DROP, captor.getValue().getType());
    }
    
    @Test
    void testWASDKeys() {
        controller.bind(mockPlayer);
        
        // Test A key (LEFT)
        capturedListener.keyPressed(createKeyEvent(KeyEvent.VK_A));
        ArgumentCaptor<GameCommand> captor = ArgumentCaptor.forClass(GameCommand.class);
        verify(mockPlayer, atLeastOnce()).enqueueCommand(captor.capture());
        assertEquals(GameCommand.Type.LEFT, captor.getValue().getType());
        
        // Test D key (RIGHT)
        capturedListener.keyPressed(createKeyEvent(KeyEvent.VK_D));
        verify(mockPlayer, atLeastOnce()).enqueueCommand(captor.capture());
        assertEquals(GameCommand.Type.RIGHT, captor.getValue().getType());
        
        // Test W key (ROTATE)
        capturedListener.keyPressed(createKeyEvent(KeyEvent.VK_W));
        verify(mockPlayer, atLeastOnce()).enqueueCommand(captor.capture());
        assertEquals(GameCommand.Type.ROTATE, captor.getValue().getType());
        
        // Test S key (SOFT_DROP)
        capturedListener.keyPressed(createKeyEvent(KeyEvent.VK_S));
        verify(mockPlayer, atLeastOnce()).enqueueCommand(captor.capture());
        assertEquals(GameCommand.Type.SOFT_DROP, captor.getValue().getType());
    }
    
    @Test
    void testUnknownKey() {
        controller.bind(mockPlayer);
        
        KeyEvent event = createKeyEvent(KeyEvent.VK_ESCAPE);
        capturedListener.keyPressed(event);
        
        verify(mockPlayer, never()).enqueueCommand(any());
    }
    
    @Test
    void testKeyPressedBeforeBind() {
        // Create controller but don't bind
        KeyEvent event = createKeyEvent(KeyEvent.VK_LEFT);
        
        // Should not crash even though listener is not bound
        assertDoesNotThrow(() -> {
            if (capturedListener != null) {
                capturedListener.keyPressed(event);
            }
        });
    }
    
    @Test
    void testKeyPressedAfterUnbind() {
        controller.bind(mockPlayer);
        KeyListener listener = capturedListener;
        controller.unbind();
        
        KeyEvent event = createKeyEvent(KeyEvent.VK_LEFT);
        listener.keyPressed(event);
        
        // Should not enqueue command after unbind
        verify(mockPlayer, never()).enqueueCommand(any());
    }
    
    @Test
    void testNullComponent() {
        KeyboardInputController nullController = new KeyboardInputController(null);
        
        // Should not crash
        assertDoesNotThrow(() -> {
            nullController.bind(mockPlayer);
            nullController.unbind();
        });
    }
    
    private KeyEvent createKeyEvent(int keyCode) {
        Component dummyComponent = new JPanel();
        return new KeyEvent(
            dummyComponent,
            KeyEvent.KEY_PRESSED,
            System.currentTimeMillis(),
            0,
            keyCode,
            KeyEvent.CHAR_UNDEFINED
        );
    }
}
