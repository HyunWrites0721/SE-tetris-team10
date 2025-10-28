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
                if (frameBoard.isPaused) {
                    break;
                }
                else {
                    frameBoard.isPaused = !frameBoard.isPaused;
                    frameBoard.paused();
                }
                
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
                if (blockText != null) {
                    blockText.Rotate90();
                    // 회전한 블록을 즉시 View에 반영
                    gameBoard.setFallingBlock(blockText.getCurrentBlock());
                    gameBoard.repaint();
                }
                break;
                
            case KeyEvent.VK_SPACE:
                if (blockText != null) {
                    int dropDistance = blockText.HardDrop();
                    int speedMultiplier = blockText.getCurrentSpeedLevel() + 1; // 속도 레벨 배율 (1~6배)
                    int lineClearScore = blockText.getLastLineClearScore();  // 마지막 라인 클리어 점수 가져오기
                    // 하드드롭 점수에만 배율 적용, 라인클리어 점수는 이미 계산되어 더해진 상태
                    frameBoard.increaseScore(dropDistance * 2 * speedMultiplier + lineClearScore);
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
                        // 소프트 드롭: 기본 1점 * (속도레벨 + 1)
                        int speedMultiplier = blockText.getCurrentSpeedLevel() + 1;
                        frameBoard.increaseScore(1 * speedMultiplier);
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
