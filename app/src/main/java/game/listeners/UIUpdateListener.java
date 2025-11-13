package game.listeners;

import javax.swing.SwingUtilities;
import game.GameView;
import game.events.*;

/**
 * UI 업데이트를 담당하는 이벤트 리스너
 * 모든 UI 업데이트는 EDT(Event Dispatch Thread)에서 실행됩니다
 */
public class UIUpdateListener implements EventListener<GameEvent> {
    private GameView gameView;
    
    public UIUpdateListener(GameView gameView) {
        this.gameView = gameView;
    }
    
    @Override
    public void onEvent(GameEvent event) {
        if (gameView == null) return;
        
        // UI 업데이트는 EDT에서 실행
        SwingUtilities.invokeLater(() -> {
            try {
                switch (event.getEventType()) {
                    case "BLOCK_PLACED":
                        handleBlockPlacedEvent((BlockPlacedEvent) event);
                        break;
                    case "LINE_CLEARED":
                        handleLineClearedEvent((LineClearedEvent) event);
                        break;
                    case "LEVEL_UP":
                        handleLevelUpEvent((LevelUpEvent) event);
                        break;
                    case "ITEM_ACTIVATED":
                        handleItemActivatedEvent((ItemActivatedEvent) event);
                        break;
                    case "GAME_OVER":
                        handleGameOverEvent((GameOverEvent) event);
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error updating UI for event " + event.getEventType() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void handleBlockPlacedEvent(BlockPlacedEvent event) {
        gameView.repaintBlock();
        System.out.println("UI: Block placed at (" + event.getX() + ", " + event.getY() + ")");
    }
    
    private void handleLineClearedEvent(LineClearedEvent event) {
        // 라인 클리어 애니메이션 실행
        gameView.repaintBlock();
        System.out.println("UI: Line cleared animation for " + event.getClearedLines().length + " lines");
    }
    
    private void handleLevelUpEvent(LevelUpEvent event) {
        // 레벨업 효과 표시
        gameView.repaintBlock();
        System.out.println("UI: Level up effect for level " + event.getNewLevel());
    }
    
    private void handleItemActivatedEvent(ItemActivatedEvent event) {
        // 아이템 활성화 효과 표시
        gameView.repaintBlock();
        System.out.println("UI: Item effect for " + event.getItemType());
    }
    
    private void handleGameOverEvent(GameOverEvent event) {
        // 게임 오버 화면 표시
        gameView.repaintBlock();
        System.out.println("UI: Game over screen with score " + event.getFinalScore());
    }
}