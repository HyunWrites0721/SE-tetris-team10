package game.events;

import java.nio.ByteBuffer;

/**
 * 새 블록이 생성되었을 때 발생하는 이벤트
 * P2P 모드에서 양쪽이 같은 블록을 보도록 동기화
 */
public class BlockSpawnedEvent extends GameEvent {
    private String blockClassName;  // 블록 클래스 이름 (예: "blocks.IBlock")
    private int x, y;               // 초기 위치
    
    public BlockSpawnedEvent(String blockClassName, int x, int y) {
        super("BLOCK_SPAWNED");
        this.blockClassName = blockClassName;
        this.x = x;
        this.y = y;
    }
    
    // 기본 생성자 (역직렬화용)
    public BlockSpawnedEvent() {
        super("BLOCK_SPAWNED");
    }
    
    // Getters
    public String getBlockClassName() { return blockClassName; }
    public int getX() { return x; }
    public int getY() { return y; }
    
    @Override
    public byte[] serialize() {
        byte[] classNameBytes = blockClassName.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + 4 + 4 + classNameBytes.length);
        buffer.putLong(getTimestamp());
        buffer.putInt(x);
        buffer.putInt(y);
        buffer.putInt(classNameBytes.length);
        buffer.put(classNameBytes);
        return buffer.array();
    }
    
    @Override
    public void deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.getLong(); // timestamp skip
        this.x = buffer.getInt();
        this.y = buffer.getInt();
        int classNameLength = buffer.getInt();
        byte[] classNameBytes = new byte[classNameLength];
        buffer.get(classNameBytes);
        this.blockClassName = new String(classNameBytes);
    }
    
    @Override
    public String toString() {
        return "BlockSpawnedEvent{" +
                "blockClassName='" + blockClassName + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
