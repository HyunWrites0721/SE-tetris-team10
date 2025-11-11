package game.events;

/**
 * EventBus 시스템 테스트용 간단한 예제
 */
public class EventSystemTest {
    
    public static void main(String[] args) {
        // EventBus 생성
        EventBus eventBus = new EventBus(false); // 동기 모드
        
        // 테스트용 이벤트 리스너 등록
        eventBus.subscribe(BlockPlacedEvent.class, event -> {
            System.out.println("✓ Block Placed: " + event);
        });
        
        eventBus.subscribe(LineClearedEvent.class, event -> {
            System.out.println("✓ Line Cleared: " + event);
        });
        
        eventBus.subscribe(LevelUpEvent.class, event -> {
            System.out.println("✓ Level Up: " + event);
        });
        
        eventBus.subscribe(GameOverEvent.class, event -> {
            System.out.println("✓ Game Over: " + event);
        });
        
        eventBus.subscribe(ItemActivatedEvent.class, event -> {
            System.out.println("✓ Item Activated: " + event);
        });
        
        // 우선순위 테스트용 리스너
        eventBus.subscribe(GameEvent.class, event -> {
            System.out.println("Priority 1: " + event.getEventType());
        }, 1);
        
        eventBus.subscribe(GameEvent.class, event -> {
            System.out.println("Priority 10: " + event.getEventType());
        }, 10);
        
        System.out.println("=== EventBus System Test ===\n");
        
        // 테스트 이벤트들 발행
        System.out.println("1. Testing BlockPlacedEvent:");
        eventBus.publish(new BlockPlacedEvent(5, 10, 1, 1));
        
        System.out.println("\n2. Testing LineClearedEvent:");
        eventBus.publish(new LineClearedEvent(new int[]{18, 19}, 300, 1));
        
        System.out.println("\n3. Testing LevelUpEvent:");
        eventBus.publish(new LevelUpEvent(2, 1));
        
        System.out.println("\n4. Testing ItemActivatedEvent:");
        eventBus.publish(new ItemActivatedEvent("CLEAR_LINE", 1));
        
        System.out.println("\n5. Testing GameOverEvent:");
        eventBus.publish(new GameOverEvent(12450, 1));
        
        // EventBus 종료
        eventBus.shutdown();
        
        System.out.println("\n=== Test Completed ===");
    }
}