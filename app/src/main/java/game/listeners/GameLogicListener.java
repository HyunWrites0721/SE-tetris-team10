package game.listeners;

import game.events.*;

/**
 * 게임 로직 처리를 담당하는 이벤트 리스너
 * 게임 상태 변화에 따른 추가 로직 처리
 */
public class GameLogicListener implements EventListener<GameEvent> {
    
    @Override
    public void onEvent(GameEvent event) {
        switch (event.getEventType()) {
            case "LINE_CLEARED":
                handleLineClearedLogic((LineClearedEvent) event);
                break;
            case "LEVEL_UP":
                handleLevelUpLogic((LevelUpEvent) event);
                break;
            case "ITEM_ACTIVATED":
                handleItemActivatedLogic((ItemActivatedEvent) event);
                break;
            case "GAME_OVER":
                handleGameOverLogic((GameOverEvent) event);
                break;
        }
    }
    
    private void handleLineClearedLogic(LineClearedEvent event) {
        int linesCleared = event.getClearedLines().length;
        
        // 라인 클리어에 따른 게임 속도 조정 로직
        if (linesCleared >= 4) { // 테트리스
            System.out.println("Logic: Tetris achieved! Special bonus applied.");
            // 특별 보너스 로직
        } else if (linesCleared >= 2) { // 더블/트리플
            System.out.println("Logic: Multi-line clear! Speed adjustment applied.");
            // 속도 조정 로직
        }
        
        // 연속 라인 클리어 콤보 시스템 (추후 구현 가능)
        System.out.println("Logic: Processing " + linesCleared + " line clear(s) with score " + event.getScore());
    }
    
    private void handleLevelUpLogic(LevelUpEvent event) {
        int newLevel = event.getNewLevel();
        
        // 레벨에 따른 게임 속도 및 난이도 조정
        System.out.println("Logic: Level " + newLevel + " reached. Adjusting game difficulty.");
        
        // 특별 레벨에서의 이벤트 처리
        if (newLevel % 5 == 0) { // 5레벨마다
            System.out.println("Logic: Milestone level reached! Special effects activated.");
        }
    }
    
    private void handleItemActivatedLogic(ItemActivatedEvent event) {
        String itemType = event.getItemType();
        
        // 아이템별 특별 로직 처리
        switch (itemType) {
            case "CLEAR_LINE":
                System.out.println("Logic: Line clear item activated. Processing line removal.");
                break;
            case "SLOW_DOWN":
                System.out.println("Logic: Slow down item activated. Reducing game speed temporarily.");
                break;
            case "DOUBLE_SCORE":
                System.out.println("Logic: Double score item activated. Next score will be doubled.");
                break;
            default:
                System.out.println("Logic: Unknown item activated: " + itemType);
        }
    }
    
    private void handleGameOverLogic(GameOverEvent event) {
        int finalScore = event.getFinalScore();
        int playerId = event.getPlayerId();
        
        System.out.println("Logic: Game over for player " + playerId + " with final score: " + finalScore);
        
        // 게임 오버 시 통계 처리
        // 최고점수 기록, 플레이 시간 저장 등
        
        // 자동 저장 로직
        System.out.println("Logic: Saving game statistics and high scores.");
    }
}