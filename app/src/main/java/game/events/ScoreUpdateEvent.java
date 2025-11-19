package game.events;

import java.nio.ByteBuffer;

/**
 * 점수 업데이트 이벤트
 */
public class ScoreUpdateEvent extends GameEvent {
    private int newScore;
    
    public ScoreUpdateEvent(int newScore) {
        super("SCORE_UPDATE");
        this.newScore = newScore;
    }
    
    public int getNewScore() {
        return newScore;
    }
    
    @Override
    public byte[] serialize() {
        return ByteBuffer.allocate(4).putInt(newScore).array();
    }
    
    @Override
    public void deserialize(byte[] data) {
        this.newScore = ByteBuffer.wrap(data).getInt();
    }
}
