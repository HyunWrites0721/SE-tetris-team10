package game.player;

import java.util.Objects;

/**
 * 플레이어에 전달되는 명령 객체의 간단한 표현
 */
public final class GameCommand {
    public enum Type { LEFT, RIGHT, ROTATE, SOFT_DROP, HARD_DROP, PAUSE, RESET }

    private final Type type;
    private final long timestamp;

    public GameCommand(Type type) {
        this(type, System.currentTimeMillis());
    }

    public GameCommand(Type type, long timestamp) {
        this.type = Objects.requireNonNull(type);
        this.timestamp = timestamp;
    }

    public Type getType() { return type; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "GameCommand{" + "type=" + type + ", ts=" + timestamp + '}';
    }
}
