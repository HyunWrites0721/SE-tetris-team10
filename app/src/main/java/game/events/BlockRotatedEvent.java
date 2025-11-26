package game.events;

import java.nio.ByteBuffer;

/**
 * 블록이 회전했을 때 발생하는 이벤트 (실시간 동기화용)
 */
public class BlockRotatedEvent extends GameEvent {
    private int x, y;
    private int blockType;
    private int rotation;  // 회전 후 상태
    
    public BlockRotatedEvent(int x, int y, int blockType, int rotation) {
        super("BLOCK_ROTATED");
        this.x = x;
        this.y = y;
        this.blockType = blockType;
        this.rotation = rotation;
    }
    
    // 기본 생성자 (역직렬화용)
    public BlockRotatedEvent() {
        super("BLOCK_ROTATED");
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
        return "BlockRotatedEvent{" +
                "x=" + x +
                ", y=" + y +
                ", blockType=" + blockType +
                ", rotation=" + rotation +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
