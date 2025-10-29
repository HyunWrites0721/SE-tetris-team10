package game;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import settings.SettingModel;


public class GameKeyListener extends KeyAdapter  {

    private final FrameBoard frameBoard;
    private final GameView gameBoard;
    private final GameModel blockText;
    private final GameTimer gameTimer;
    private String controlType;

    public GameKeyListener(FrameBoard frameBoard, GameView gameBoard, GameModel gameModel, GameTimer gameTimer) {
        this.frameBoard = frameBoard;
        this.gameBoard = gameBoard;
        this.blockText = gameModel;
        this.gameTimer = gameTimer;
        loadControlType();
    }
    
    // 설정에서 controlType 불러오기
    private void loadControlType() {
        try {
            SettingModel settingModel = new SettingModel();
            this.controlType = settingModel.getControlType();
        } catch (Exception e) {
            this.controlType = "arrow"; // 기본값
        }
    }
    
    // controlType 재로드 (설정 변경 시 호출)
    public void reloadControlType() {
        loadControlType();
    }
    
    // 키가 올바른 컨트롤 키인지 확인
    private boolean isValidKey(int keyCode, String action) {
        if (controlType.equals("wasd")) {
            switch (action) {
                case "ROTATE":
                    return keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP;
                case "LEFT":
                    return keyCode == KeyEvent.VK_A;
                case "RIGHT":
                    return keyCode == KeyEvent.VK_D;
                case "DOWN":
                    return keyCode == KeyEvent.VK_S;
                default:
                    return false;
            }
        } else { // arrow (기본값)
            switch (action) {
                case "ROTATE":
                    return keyCode == KeyEvent.VK_UP;
                case "LEFT":
                    return keyCode == KeyEvent.VK_LEFT;
                case "RIGHT":
                    return keyCode == KeyEvent.VK_RIGHT;
                case "DOWN":
                    return keyCode == KeyEvent.VK_DOWN;
                default:
                    return false;
            }
        }
    }
    
    


    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                if (frameBoard.isPaused) { // 
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
            
            // 회전 (Arrow: UP, WASD: W)
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if (isValidKey(e.getKeyCode(), "ROTATE") && blockText != null) {
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
            
            // 왼쪽 이동 (Arrow: LEFT, WASD: A)
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                if (isValidKey(e.getKeyCode(), "LEFT") && !frameBoard.isPaused && blockText != null && blockText.getCurrentBlock() != null) {
                    blockText.getCurrentBlock().moveLeft(blockText.getBoard());
                    gameBoard.setFallingBlock(blockText.getCurrentBlock());
                    gameBoard.repaint();
                }
                break;

            // 오른쪽 이동 (Arrow: RIGHT, WASD: D)
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                if (isValidKey(e.getKeyCode(), "RIGHT") && !frameBoard.isPaused && blockText != null && blockText.getCurrentBlock() != null) {
                    blockText.getCurrentBlock().moveRight(blockText.getBoard());
                    gameBoard.setFallingBlock(blockText.getCurrentBlock());
                    gameBoard.repaint();
                }
                break;

            // 아래로 이동 (Arrow: DOWN, WASD: S)
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                if (isValidKey(e.getKeyCode(), "DOWN") && !frameBoard.isPaused && blockText != null && blockText.getCurrentBlock() != null) {
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
