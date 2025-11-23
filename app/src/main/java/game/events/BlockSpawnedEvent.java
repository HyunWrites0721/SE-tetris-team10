package game.events;

import java.nio.ByteBuffer;

/**
 * 새 블록이 생성되었을 때 발생하는 이벤트
 * P2P 모드에서 양쪽이 같은 블록을 보도록 동기화
 */
public class BlockSpawnedEvent extends GameEvent {
    private int blockType;  // 블록 타입 (0-6: I,J,L,O,S,T,Z)
    private int x, y;       // 초기 위치
    private int rotation;   // 초기 회전 상태
    
    public BlockSpawnedEvent(int blockType, int x, int y, int rotation) {
        super("BLOCK_SPAWNED");
        this.blockType = blockType;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }
    
    // 기본 생성자 (역직렬화용)
    public BlockSpawnedEvent() {
        super("BLOCK_SPAWNED");
    }
    
    // Getters
    public int getBlockType() { return blockType; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getRotation() { return rotation; }
    
    @Override
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(24);
        buffer.putLong(getTimestamp());
        buffer.putInt(blockType);
        buffer.putInt(x);
        buffer.putInt(y);
        buffer.putInt(rotation);
        return buffer.array();
    }
    
    @Override
    public void deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.getLong(); // timestamp skip
        this.blockType = buffer.getInt();
        this.x = buffer.getInt();
        this.y = buffer.getInt();
        this.rotation = buffer.getInt();
    }
    
    @Override
    public String toString() {
        return "BlockSpawnedEvent{" +
                "blockType=" + blockType +
                ", x=" + x +
                ", y=" + y +
                ", rotation=" + rotation +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
