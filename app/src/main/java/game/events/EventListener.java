package game.events;

/**
 * 이벤트 리스너를 위한 함수형 인터페이스
 */
@FunctionalInterface
public interface EventListener<T extends GameEvent> {
    /**
     * 이벤트가 발생했을 때 호출되는 메서드
     * @param event 발생한 이벤트
     */
    void onEvent(T event);
}