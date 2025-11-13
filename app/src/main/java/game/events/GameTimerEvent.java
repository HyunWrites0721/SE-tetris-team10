package game.events;

import java.nio.ByteBuffer;

/**
 * 게임 타이머 상태 변경 이벤트
 * 타이머 시작, 정지, 일시정지, 재개 시 발생
 */
public class GameTimerEvent extends GameEvent {
    
    public enum TimerAction {
        START,
        STOP,
        PAUSE,
        RESUME,
        SPEED_CHANGE
    }
    
    private final TimerAction action;
    private final int newDelay; // 속도 변경 시 새로운 딜레이
    private final int speedLevel; // 현재 속도 레벨
    
    public GameTimerEvent(TimerAction action) {
        this(action, -1, -1);
    }
    
    public GameTimerEvent(TimerAction action, int newDelay, int speedLevel) {
        super("TIMER");
        this.action = action;
        this.newDelay = newDelay;
        this.speedLevel = speedLevel;
    }
    
    public TimerAction getAction() {
        return action;
    }
    
    public int getNewDelay() {
        return newDelay;
    }
    
    public int getSpeedLevel() {
        return speedLevel;
    }
    
    @Override
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(20); // 8 + 4 + 4 + 4  20byte만 전송
        buffer.putLong(getTimestamp());
        buffer.putInt(action.ordinal());
        buffer.putInt(newDelay);
        buffer.putInt(speedLevel);
        return buffer.array();
    }
    
    @Override
    public void deserialize(byte[] data) {
        // 불변 객체이므로 역직렬화는 생성자에서 처리
        throw new UnsupportedOperationException("GameTimerEvent는 불변 객체입니다. 팩토리 메서드를 사용하세요.");
    }
    
    public static GameTimerEvent fromBytes(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.getLong(); // timestamp 건너뛰기
        TimerAction action = TimerAction.values()[buffer.getInt()];
        int newDelay = buffer.getInt();
        int speedLevel = buffer.getInt();
        
        return new GameTimerEvent(action, newDelay, speedLevel);
    }
    
    @Override
    public String toString() {
        return "GameTimerEvent{" +
                "action=" + action +
                ", newDelay=" + newDelay +
                ", speedLevel=" + speedLevel +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}