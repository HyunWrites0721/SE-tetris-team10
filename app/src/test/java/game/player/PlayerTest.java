package game.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import game.core.GameState;
import game.player.engine.GameEngine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerTest {
    
    @Mock
    private GameEngine mockEngine;
    
    @Mock
    private InputController mockInput;
    
    @Mock
    private PlayerView mockView;
    
    @Mock
    private GameState mockState;
    
    private PlayerId playerId;
    private Player player;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        playerId = PlayerId.PLAYER1;
        player = new Player(playerId, mockEngine, mockState, mockInput, mockView);
    }
    
    @Test
    void testGetId() {
        assertEquals(playerId, player.getId());
    }
    
    @Test
    void testStart() {
        player.start();
        
        verify(mockInput).bind(player);
        verify(mockView).setPlayer(player);
        verify(mockView, atLeastOnce()).requestRender(any());
    }
    
    @Test
    void testStartTwice() {
        player.start();
        player.start();
        
        // bind should be called only once
        verify(mockInput, times(1)).bind(player);
    }
    
    @Test
    void testStop() {
        player.start();
        player.stop();
        
        verify(mockInput).unbind();
    }
    
    @Test
    void testPauseAndResume() {
        player.start();
        player.pause();
        player.resume();
        
        // Should not crash
        assertNotNull(player.getState());
    }
    
    @Test
    void testEnqueueCommand() {
        GameCommand cmd = new GameCommand(GameCommand.Type.LEFT);
        player.enqueueCommand(cmd);
        
        // Command should be queued, no immediate effect
        assertNotNull(player);
    }
    
    @Test
    void testEnqueueNullCommand() {
        player.enqueueCommand(null);
        
        // Should not crash
        assertNotNull(player);
    }
    
    @Test
    void testApplyCommand() {
        GameCommand cmd = new GameCommand(GameCommand.Type.ROTATE);
        GameState newState = mock(GameState.class);
        
        when(mockEngine.command(any(), eq(cmd))).thenReturn(newState);
        
        player.applyCommand(cmd);
        
        verify(mockEngine).command(mockState, cmd);
        verify(mockView, atLeastOnce()).requestRender(any());
    }
    
    @Test
    void testTick() {
        player.start();
        
        GameCommand cmd1 = new GameCommand(GameCommand.Type.LEFT);
        GameCommand cmd2 = new GameCommand(GameCommand.Type.RIGHT);
        
        GameState state1 = mock(GameState.class);
        GameState state2 = mock(GameState.class);
        GameState state3 = mock(GameState.class);
        
        when(mockEngine.command(any(), eq(cmd1))).thenReturn(state1);
        when(mockEngine.command(any(), eq(cmd2))).thenReturn(state2);
        when(mockEngine.step(any(), anyLong())).thenReturn(state3);
        
        player.enqueueCommand(cmd1);
        player.enqueueCommand(cmd2);
        
        GameState result = player.tick(100L);
        
        verify(mockEngine).command(any(), eq(cmd1));
        verify(mockEngine).command(any(), eq(cmd2));
        verify(mockEngine).step(any(), eq(100L));
        assertNotNull(result);
    }
    
    @Test
    void testTickWhenPaused() {
        player.start();
        player.pause();
        
        GameState initialState = player.getState();
        GameState result = player.tick(100L);
        
        // When paused, state should not change
        assertEquals(initialState, result);
        verify(mockEngine, never()).step(any(), anyLong());
    }
    
    @Test
    void testGetState() {
        assertEquals(mockState, player.getState());
    }
    
    @Test
    void testSetState() {
        GameState newState = mock(GameState.class);
        
        player.setState(newState);
        
        assertEquals(newState, player.getState());
        verify(mockView, atLeastOnce()).requestRender(newState);
    }
    
    @Test
    void testStartWithNullInput() {
        Player playerNoInput = new Player(playerId, mockEngine, mockState, null, mockView);
        
        // Should not crash
        playerNoInput.start();
        
        verify(mockView).setPlayer(playerNoInput);
    }
    
    @Test
    void testStartWithNullView() {
        Player playerNoView = new Player(playerId, mockEngine, mockState, mockInput, null);
        
        // Should not crash
        playerNoView.start();
        
        verify(mockInput).bind(playerNoView);
    }
    
    @Test
    void testStopWithNullInput() {
        Player playerNoInput = new Player(playerId, mockEngine, mockState, null, mockView);
        
        // Should not crash
        playerNoInput.stop();
    }
    
    @Test
    void testTickProcessesMultipleCommands() {
        player.start();
        
        GameCommand cmd1 = new GameCommand(GameCommand.Type.LEFT);
        GameCommand cmd2 = new GameCommand(GameCommand.Type.RIGHT);
        GameCommand cmd3 = new GameCommand(GameCommand.Type.ROTATE);
        
        when(mockEngine.command(any(), any())).thenReturn(mockState);
        when(mockEngine.step(any(), anyLong())).thenReturn(mockState);
        
        player.enqueueCommand(cmd1);
        player.enqueueCommand(cmd2);
        player.enqueueCommand(cmd3);
        
        player.tick(100L);
        
        verify(mockEngine, times(3)).command(any(), any());
        verify(mockEngine, times(1)).step(any(), anyLong());
    }
}
