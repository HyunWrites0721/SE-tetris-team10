package game.listeners;

import game.ScoreBoard;
import game.events.*;

/**
 * 점수판 업데이트를 담당하는 이벤트 리스너
 */
public class ScoreBoardListener implements EventListener<GameEvent> {
    private ScoreBoard scoreBoard;
    
    public ScoreBoardListener(ScoreBoard scoreBoard) {
        this.scoreBoard = scoreBoard;
    }
    
    @Override
    public void onEvent(GameEvent event) {
        if (scoreBoard == null) return;
        
        switch (event.getEventType()) {
            case "LINE_CLEARED":
                handleLineClearedEvent((LineClearedEvent) event);
                break;
            case "LEVEL_UP":
                handleLevelUpEvent((LevelUpEvent) event);
                break;
            case "GAME_OVER":
                handleGameOverEvent((GameOverEvent) event);
                break;
            case "BLOCK_PLACED":
                handleBlockPlacedEvent((BlockPlacedEvent) event);
                break;
        }
    }
    
    private void handleLineClearedEvent(LineClearedEvent event) {
        try {
            // ScoreBoard는 현재 단순하므로 최고점수만 업데이트
            if (event.getScore() > scoreBoard.getHighScore()) {
                scoreBoard.setHighScore(event.getScore());
            }
            
            System.out.println("Score updated: +" + event.getScore() + 
                             ", Lines: +" + event.getClearedLines().length);
        } catch (Exception e) {
            System.err.println("Error updating score: " + e.getMessage());
        }
    }
    
    private void handleLevelUpEvent(LevelUpEvent event) {
        try {
            System.out.println("Level up! New level: " + event.getNewLevel());
        } catch (Exception e) {
            System.err.println("Error updating level: " + e.getMessage());
        }
    }
    
    private void handleGameOverEvent(GameOverEvent event) {
        try {
            // 게임 종료 시 최고점수 업데이트
            if (event.getFinalScore() > scoreBoard.getHighScore()) {
                scoreBoard.setHighScore(event.getFinalScore());
            }
            
            System.out.println("Game Over! Final score: " + event.getFinalScore());
        } catch (Exception e) {
            System.err.println("Error handling game over: " + e.getMessage());
        }
    }
    
    private void handleBlockPlacedEvent(BlockPlacedEvent event) {
        // 블록 배치 시 추가 점수나 UI 업데이트가 필요하면 여기서 처리
        System.out.println("Block placed at (" + event.getX() + ", " + event.getY() + ")");
    }
}