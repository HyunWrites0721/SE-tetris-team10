package game.events;

import java.nio.ByteBuffer;

/**
 * 라인이 클리어되었을 때 발생하는 이벤트
 */
public class LineClearedEvent extends GameEvent {
    private int[] clearedLines;
    private int score;
    private int playerId;
    
    public LineClearedEvent(int[] clearedLines, int score, int playerId) {
        super("LINE_CLEARED");
        this.clearedLines = clearedLines.clone(); // 방어적 복사
        this.score = score;
        this.playerId = playerId;
    }
    
    // 기본 생성자 (역직렬화용)
    public LineClearedEvent() {
        super("LINE_CLEARED");
    }
    
    // Getters
    public int[] getClearedLines() { return clearedLines.clone(); }
    public int getScore() { return score; }
    public int getPlayerId() { return playerId; }
    
    @Override
    public byte[] serialize() {
        int lineCount = clearedLines.length;
        ByteBuffer buffer = ByteBuffer.allocate(20 + (lineCount * 4));
        buffer.putLong(getTimestamp());
        buffer.putInt(score);
        buffer.putInt(playerId);
        buffer.putInt(lineCount);
        for (int line : clearedLines) {
            buffer.putInt(line);
        }
        return buffer.array();
    }
    
    @Override
    public void deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.getLong(); // timestamp skip
        this.score = buffer.getInt();
        this.playerId = buffer.getInt();
        int lineCount = buffer.getInt();
        this.clearedLines = new int[lineCount];
        for (int i = 0; i < lineCount; i++) {
            this.clearedLines[i] = buffer.getInt();
        }
    }
    
    @Override
    public String toString() {
        return "LineClearedEvent{" +
                "clearedLines=" + java.util.Arrays.toString(clearedLines) +
                ", score=" + score +
                ", playerId=" + playerId +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}