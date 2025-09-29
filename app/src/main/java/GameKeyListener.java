import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


public class GameKeyListener extends KeyAdapter{

    private FrameBoard frameBoard;
    private GameBoard gameBoard;


    public GameKeyListener(FrameBoard frameBoard, GameBoard gameBoard) {
        this.frameBoard = frameBoard;
        this.gameBoard = gameBoard;
    }
    
    


    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                frameBoard.togglePause();
                break;
                
            case KeyEvent.VK_1:
                gameBoard.scale = 0.5;
                gameBoard.convertScale(gameBoard.scale);
                frameBoard.updateBlockTextPositions();
                break;
                
            case KeyEvent.VK_2:
                gameBoard.scale = 1.0;
                gameBoard.convertScale(gameBoard.scale);
                frameBoard.updateBlockTextPositions();
                break;
                
            case KeyEvent.VK_3:
                gameBoard.scale = 1.5;
                gameBoard.convertScale(gameBoard.scale);
                frameBoard.updateBlockTextPositions();
                break;
            default:
                // 다른 키는 처리하지 않음
                break;
        }
    }

}
