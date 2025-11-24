package game.events;

import java.util.*;
import java.util.concurrent.*;

/**
 * 이벤트 발행/구독을 관리하는 EventBus 시스템
 * 우선순위 기반 리스너 실행, 에러 처리, 동기/비동기 모드 지원
 */
public class EventBus {
    private final Map<Class<? extends GameEvent>, List<ListenerWrapper<?>>> listeners;
    private final ExecutorService asyncExecutor;
    private boolean isAsync;
    
    public EventBus() {
        this(false); // 기본은 동기 처리
    }
    
    public EventBus(boolean isAsync) {
        this.listeners = new ConcurrentHashMap<>();
        this.isAsync = isAsync;
        this.asyncExecutor = isAsync ? 
            Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "EventBus-Worker");
                t.setDaemon(true);
                return t;
            }) : null;
    }
    
    /**
     * 이벤트 리스너를 등록합니다 (기본 우선순위)
     */
    public <T extends GameEvent> void subscribe(Class<T> eventType, EventListener<T> listener) {
        subscribe(eventType, listener, Integer.MAX_VALUE); // 기본 우선순위
    }
    
    /**
     * 우선순위를 지정하여 이벤트 리스너를 등록합니다
     * @param eventType 이벤트 타입
     * @param listener 리스너
     * @param priority 우선순위 (낮을수록 먼저 실행)
     */
    public <T extends GameEvent> void subscribe(Class<T> eventType, EventListener<T> listener, int priority) {
        List<ListenerWrapper<?>> eventListeners = listeners.computeIfAbsent(
            eventType, k -> new CopyOnWriteArrayList<>()
        );
        
        eventListeners.add(new ListenerWrapper<>(listener, priority));
        
        // 우선순위 순으로 정렬
        eventListeners.sort(Comparator.comparingInt(ListenerWrapper::getPriority));
    }
    
    /**
     * 이벤트 리스너를 제거합니다
     */
    public <T extends GameEvent> boolean unsubscribe(Class<T> eventType, EventListener<T> listener) {
        List<ListenerWrapper<?>> eventListeners = listeners.get(eventType);
        if (eventListeners == null) return false;
        
        return eventListeners.removeIf(wrapper -> wrapper.getListener().equals(listener));
    }
    
    /**
     * 이벤트를 발행합니다
     */
    public <T extends GameEvent> void publish(T event) {
        if (event == null) return;
        
        List<ListenerWrapper<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners == null || eventListeners.isEmpty()) return;

        try {
            System.out.println("[DEBUG EventBus] publish: " + event.getClass().getSimpleName() + ", listeners=" + eventListeners.size() + ", isAsync=" + isAsync);
        } catch (Throwable t) {
            // ignore logging failure
        }
        
        if (isAsync) {
            asyncExecutor.submit(() -> notifyListeners(event, eventListeners));
        } else {
            notifyListeners(event, eventListeners);
        }
    }
    
    /**
     * 리스너들에게 이벤트를 알립니다
     */
    @SuppressWarnings("unchecked")
    private <T extends GameEvent> void notifyListeners(T event, List<ListenerWrapper<?>> eventListeners) {
        for (ListenerWrapper<?> wrapper : eventListeners) {
            try {
                ((EventListener<T>) wrapper.getListener()).onEvent(event);
            } catch (Exception e) {
                System.err.println("Error in event listener for " + event.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 등록된 리스너 수를 반환합니다
     */
    public int getListenerCount(Class<? extends GameEvent> eventType) {
        List<ListenerWrapper<?>> eventListeners = listeners.get(eventType);
        return eventListeners != null ? eventListeners.size() : 0;
    }
    
    /**
     * EventBus를 종료합니다
     */
    public void shutdown() {
        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        listeners.clear();
    }
    
    /**
     * 내부 래퍼 클래스 - 리스너와 우선순위를 함께 저장
     */
    private static class ListenerWrapper<T extends GameEvent> {
        private final EventListener<T> listener;
        private final int priority;
        
        public ListenerWrapper(EventListener<T> listener, int priority) {
            this.listener = listener;
            this.priority = priority;
        }
        
        public EventListener<T> getListener() { return listener; }
        public int getPriority() { return priority; }
    }
}