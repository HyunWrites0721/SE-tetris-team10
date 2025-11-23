package game.events;

import java.nio.ByteBuffer;

/**
 * 블록이 이동했을 때 발생하는 이벤트 (실시간 동기화용)
 */
public class BlockMovedEvent extends GameEvent {
    private int x, y;
    private int blockType;
    private int rotation;  // 회전 상태
    
    public BlockMovedEvent(int x, int y, int blockType, int rotation) {
        super("BLOCK_MOVED");
        this.x = x;
        this.y = y;
        this.blockType = blockType;
        this.rotation = rotation;
    }
    
    // 기본 생성자 (역직렬화용)
    public BlockMovedEvent() {
        super("BLOCK_MOVED");
    }
    
    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getBlockType() { return blockType; }
    public int getRotation() { return rotation; }
    
    @Override
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(24);
        buffer.putLong(getTimestamp());
        buffer.putInt(x);
        buffer.putInt(y);
        buffer.putInt(blockType);
        buffer.putInt(rotation);
        return buffer.array();
    }
    
    @Override
    public void deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.getLong(); // timestamp skip
        this.x = buffer.getInt();
        this.y = buffer.getInt();
        this.blockType = buffer.getInt();
        this.rotation = buffer.getInt();
    }
    
    @Override
    public String toString() {
        return "BlockMovedEvent{" +
                "x=" + x +
                ", y=" + y +
                ", blockType=" + blockType +
                ", rotation=" + rotation +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
