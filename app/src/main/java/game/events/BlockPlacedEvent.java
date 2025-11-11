package game.events;

import java.nio.ByteBuffer;

/**
 * 블록이 보드에 배치되었을 때 발생하는 이벤트
 */
public class BlockPlacedEvent extends GameEvent {
    private int x, y;
    private int blockType;
    private int playerId;
    
    public BlockPlacedEvent(int x, int y, int blockType, int playerId) {
        super("BLOCK_PLACED");
        this.x = x;
        this.y = y;
        this.blockType = blockType;
        this.playerId = playerId;
    }
    
    // 기본 생성자 (역직렬화용)
    public BlockPlacedEvent() {
        super("BLOCK_PLACED");
    }
    
    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getBlockType() { return blockType; }
    public int getPlayerId() { return playerId; }
    
    @Override
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(24);
        buffer.putLong(getTimestamp());
        buffer.putInt(x);
        buffer.putInt(y);
        buffer.putInt(blockType);
        buffer.putInt(playerId);
        return buffer.array();
    }
    
    @Override
    public void deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.getLong(); // timestamp skip
        this.x = buffer.getInt();
        this.y = buffer.getInt();
        this.blockType = buffer.getInt();
        this.playerId = buffer.getInt();
    }
    
    @Override
    public String toString() {
        return "BlockPlacedEvent{" +
                "x=" + x +
                ", y=" + y +
                ", blockType=" + blockType +
                ", playerId=" + playerId +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}