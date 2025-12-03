package versus;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import game.core.GameController;

@DisplayName("VersusKeyListener 테스트")
class VersusKeyListenerTest {
    
    private VersusKeyListener keyListener;
    private VersusFrameBoard mockFrameBoard;
    private GameController mockGC1;
    private GameController mockGC2;
    private JPanel mockComponent;
    
    @BeforeEach
    void setUp() {
        mockFrameBoard = mock(VersusFrameBoard.class);
        mockGC1 = mock(GameController.class);
        mockGC2 = mock(GameController.class);
        mockComponent = new JPanel();
        
        when(mockFrameBoard.getGameController1()).thenReturn(mockGC1);
        when(mockFrameBoard.getGameController2()).thenReturn(mockGC2);
        when(mockFrameBoard.isPaused()).thenReturn(false);
        when(mockFrameBoard.isGameOver()).thenReturn(false);
        
        keyListener = new VersusKeyListener(mockFrameBoard);
    }
    
    @Test
    @DisplayName("ESC 키 - 일시정지 토글")
    void testEscapeKey_TogglesPause() {
        KeyEvent escEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                         System.currentTimeMillis(), 0, 
                                         KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
        
        keyListener.keyPressed(escEvent);
        
        verify(mockFrameBoard, times(1)).togglePause();
    }
    
    @Test
    @DisplayName("Player 1 - A 키 (왼쪽 이동)")
    void testPlayer1_AKey_MovesLeft() {
        KeyEvent aEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_A, 'a');
        
        keyListener.keyPressed(aEvent);
        
        verify(mockGC1, times(1)).moveLeft();
        verify(mockGC2, never()).moveLeft();
    }
    
    @Test
    @DisplayName("Player 1 - D 키 (오른쪽 이동)")
    void testPlayer1_DKey_MovesRight() {
        KeyEvent dEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_D, 'd');
        
        keyListener.keyPressed(dEvent);
        
        verify(mockGC1, times(1)).moveRight();
        verify(mockGC2, never()).moveRight();
    }
    
    @Test
    @DisplayName("Player 1 - S 키 (소프트 드롭)")
    void testPlayer1_SKey_MovesDown() {
        KeyEvent sEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_S, 's');
        
        keyListener.keyPressed(sEvent);
        
        verify(mockGC1, times(1)).moveDown();
        verify(mockGC2, never()).moveDown();
    }
    
    @Test
    @DisplayName("Player 1 - W 키 (회전)")
    void testPlayer1_WKey_Rotates() {
        KeyEvent wEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_W, 'w');
        
        keyListener.keyPressed(wEvent);
        
        verify(mockGC1, times(1)).rotate();
        verify(mockGC2, never()).rotate();
    }
    
    @Test
    @DisplayName("Player 1 - F 키 (하드 드롭)")
    void testPlayer1_FKey_HardDrops() {
        KeyEvent fEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_F, 'f');
        
        keyListener.keyPressed(fEvent);
        
        verify(mockGC1, times(1)).hardDrop();
        verify(mockGC2, never()).hardDrop();
    }
    
    @Test
    @DisplayName("Player 2 - J 키 (왼쪽 이동)")
    void testPlayer2_JKey_MovesLeft() {
        KeyEvent jEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_J, 'j');
        
        keyListener.keyPressed(jEvent);
        
        verify(mockGC2, times(1)).moveLeft();
        verify(mockGC1, never()).moveLeft();
    }
    
    @Test
    @DisplayName("Player 2 - L 키 (오른쪽 이동)")
    void testPlayer2_LKey_MovesRight() {
        KeyEvent lEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_L, 'l');
        
        keyListener.keyPressed(lEvent);
        
        verify(mockGC2, times(1)).moveRight();
        verify(mockGC1, never()).moveRight();
    }
    
    @Test
    @DisplayName("Player 2 - K 키 (소프트 드롭)")
    void testPlayer2_KKey_MovesDown() {
        KeyEvent kEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_K, 'k');
        
        keyListener.keyPressed(kEvent);
        
        verify(mockGC2, times(1)).moveDown();
        verify(mockGC1, never()).moveDown();
    }
    
    @Test
    @DisplayName("Player 2 - I 키 (회전)")
    void testPlayer2_IKey_Rotates() {
        KeyEvent iEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_I, 'i');
        
        keyListener.keyPressed(iEvent);
        
        verify(mockGC2, times(1)).rotate();
        verify(mockGC1, never()).rotate();
    }
    
    @Test
    @DisplayName("Player 2 - 세미콜론 키 (하드 드롭)")
    void testPlayer2_SemicolonKey_HardDrops() {
        KeyEvent semicolonEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                               System.currentTimeMillis(), 0, 
                                               KeyEvent.VK_SEMICOLON, ';');
        
        keyListener.keyPressed(semicolonEvent);
        
        verify(mockGC2, times(1)).hardDrop();
        verify(mockGC1, never()).hardDrop();
    }
    
    @Test
    @DisplayName("일시정지 상태 - 게임 키 입력 무시")
    void testPausedState_IgnoresGameKeys() {
        when(mockFrameBoard.isPaused()).thenReturn(true);
        
        KeyEvent aEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_A, 'a');
        
        keyListener.keyPressed(aEvent);
        
        verify(mockGC1, never()).moveLeft();
        verify(mockGC2, never()).moveLeft();
    }
    
    @Test
    @DisplayName("게임오버 상태 - 게임 키 입력 무시")
    void testGameOverState_IgnoresGameKeys() {
        when(mockFrameBoard.isGameOver()).thenReturn(true);
        
        KeyEvent dEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_D, 'd');
        
        keyListener.keyPressed(dEvent);
        
        verify(mockGC1, never()).moveRight();
        verify(mockGC2, never()).moveRight();
    }
    
    @Test
    @DisplayName("일시정지 상태에서도 ESC 키는 동작")
    void testPausedState_EscapeKeyStillWorks() {
        when(mockFrameBoard.isPaused()).thenReturn(true);
        
        KeyEvent escEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                         System.currentTimeMillis(), 0, 
                                         KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
        
        keyListener.keyPressed(escEvent);
        
        verify(mockFrameBoard, times(1)).togglePause();
    }
    
    @Test
    @DisplayName("게임오버 상태에서도 ESC 키는 동작")
    void testGameOverState_EscapeKeyStillWorks() {
        when(mockFrameBoard.isGameOver()).thenReturn(true);
        
        KeyEvent escEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                         System.currentTimeMillis(), 0, 
                                         KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
        
        keyListener.keyPressed(escEvent);
        
        verify(mockFrameBoard, times(1)).togglePause();
    }
    
    @Test
    @DisplayName("알 수 없는 키 - 아무 동작 안함")
    void testUnknownKey_NoAction() {
        KeyEvent unknownEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                            System.currentTimeMillis(), 0, 
                                            KeyEvent.VK_Z, 'z');
        
        keyListener.keyPressed(unknownEvent);
        
        verify(mockGC1, never()).moveLeft();
        verify(mockGC1, never()).moveRight();
        verify(mockGC1, never()).moveDown();
        verify(mockGC1, never()).rotate();
        verify(mockGC1, never()).hardDrop();
        
        verify(mockGC2, never()).moveLeft();
        verify(mockGC2, never()).moveRight();
        verify(mockGC2, never()).moveDown();
        verify(mockGC2, never()).rotate();
        verify(mockGC2, never()).hardDrop();
    }
    
    @Test
    @DisplayName("Player1과 Player2의 키 조합 - 동시 입력")
    void testBothPlayers_SimultaneousInput() {
        // Player 1의 A키와 Player 2의 J키를 동시에 누른다고 가정
        // (실제로는 순차적이지만 테스트상 두 번 호출)
        KeyEvent aEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_A, 'a');
        KeyEvent jEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_J, 'j');
        
        keyListener.keyPressed(aEvent);
        keyListener.keyPressed(jEvent);
        
        verify(mockGC1, times(1)).moveLeft();
        verify(mockGC2, times(1)).moveLeft();
    }
    
    @Test
    @DisplayName("연속된 키 입력 테스트")
    void testConsecutiveKeyPresses() {
        KeyEvent wEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_W, 'w');
        KeyEvent sEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_S, 's');
        KeyEvent fEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_F, 'f');
        
        keyListener.keyPressed(wEvent);  // 회전
        keyListener.keyPressed(sEvent);  // 아래로
        keyListener.keyPressed(fEvent);  // 하드드롭
        
        verify(mockGC1, times(1)).rotate();
        verify(mockGC1, times(1)).moveDown();
        verify(mockGC1, times(1)).hardDrop();
    }
    
    @Test
    @DisplayName("생성자 테스트 - null FrameBoard")
    void testConstructor_WithNullFrameBoard() {
        // null을 허용하는지 확인 (NullPointerException이 발생할 수 있음)
        assertDoesNotThrow(() -> {
            new VersusKeyListener(null);
        });
    }
    
    @Test
    @DisplayName("FrameBoard 상태 확인 호출 테스트")
    void testFrameBoard_StateChecks() {
        KeyEvent aEvent = new KeyEvent(mockComponent, KeyEvent.KEY_PRESSED, 
                                       System.currentTimeMillis(), 0, 
                                       KeyEvent.VK_A, 'a');
        
        keyListener.keyPressed(aEvent);
        
        verify(mockFrameBoard, atLeastOnce()).isPaused();
        verify(mockFrameBoard, atLeastOnce()).isGameOver();
        verify(mockFrameBoard, atLeastOnce()).getGameController1();
    }
}
