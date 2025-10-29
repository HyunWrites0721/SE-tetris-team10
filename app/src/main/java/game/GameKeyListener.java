package game;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


public class GameKeyListener extends KeyAdapter  {

    private final FrameBoard frameBoard;
    private final GameView gameBoard;
    private final GameModel blockText;
    private final GameTimer gameTimer;

    public GameKeyListener(FrameBoard frameBoard, GameView gameBoard, GameModel gameModel, GameTimer gameTimer) {
        this.frameBoard = frameBoard;
        this.gameBoard = gameBoard;
        this.blockText = gameModel;
        this.gameTimer = gameTimer;
    }
    
    


    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                // 일시정지/재개는 FrameBoard가 내부에서 토글하도록 위임
                frameBoard.paused();
                break;
                
            case KeyEvent.VK_1:
                gameBoard.scale = 0.5;
                gameBoard.convertScale(gameBoard.scale);

                break;
                
            case KeyEvent.VK_2:
                gameBoard.scale = 1.0;
                gameBoard.convertScale(gameBoard.scale);
                break;
                
            case KeyEvent.VK_3:
                gameBoard.scale = 1.5;
                gameBoard.convertScale(gameBoard.scale);
                break;
            
            case KeyEvent.VK_UP:
                if (!frameBoard.isPaused && blockText != null) {
                    blockText.Rotate90();
                    // 회전한 블록을 즉시 View에 반영
                    gameBoard.setFallingBlock(blockText.getCurrentBlock());
                    gameBoard.repaint();
                }
                break;
                
            case KeyEvent.VK_SPACE:
                if (!frameBoard.isPaused && blockText != null) {
                    blockText.HardDrop();
                    // 하드드롭 후 스폰된 블록을 즉시 반영
                    gameBoard.setFallingBlock(blockText.getCurrentBlock());
                    gameBoard.repaint();
                }
                break;
            
            case KeyEvent.VK_LEFT:
                if (!frameBoard.isPaused && blockText != null && blockText.getCurrentBlock() != null) {
                    blockText.getCurrentBlock().moveLeft(blockText.getBoard());
                    gameBoard.setFallingBlock(blockText.getCurrentBlock());
                    gameBoard.repaint();
                }
                break;

            case KeyEvent.VK_RIGHT:
                if (!frameBoard.isPaused && blockText != null && blockText.getCurrentBlock() != null) {
                    blockText.getCurrentBlock().moveRight(blockText.getBoard());
                    gameBoard.setFallingBlock(blockText.getCurrentBlock());
                    gameBoard.repaint();
                }
                break;

            case KeyEvent.VK_DOWN:
                if (!frameBoard.isPaused && blockText != null && blockText.getCurrentBlock() != null) {
                    if (blockText.getCurrentBlock().isMoveDown(blockText.getBoard())) {
                        // 성공적으로 아래로 이동했을 때만 점수 증가
                        frameBoard.increaseScore(1);
                        gameBoard.setFallingBlock(blockText.getCurrentBlock());
                        gameBoard.repaint();
                    }
                }
                break;
                
            default:
                // 다른 키는 처리하지 않음
                break;
        }
    }

}
