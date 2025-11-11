package game.events;

import java.nio.ByteBuffer;

/**
 * 아이템이 활성화되었을 때 발생하는 이벤트
 */
public class ItemActivatedEvent extends GameEvent {
    private String itemType;
    private int playerId;
    
    public ItemActivatedEvent(String itemType, int playerId) {
        super("ITEM_ACTIVATED");
        this.itemType = itemType;
        this.playerId = playerId;
    }
    
    // 기본 생성자 (역직렬화용)
    public ItemActivatedEvent() {
        super("ITEM_ACTIVATED");
    }
    
    // Getters
    public String getItemType() { return itemType; }
    public int getPlayerId() { return playerId; }
    
    @Override
    public byte[] serialize() {
        byte[] itemBytes = itemType.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(16 + itemBytes.length);
        buffer.putLong(getTimestamp());
        buffer.putInt(playerId);
        buffer.putInt(itemBytes.length);
        buffer.put(itemBytes);
        return buffer.array();
    }
    
    @Override
    public void deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.getLong(); // timestamp skip
        this.playerId = buffer.getInt();
        int itemLength = buffer.getInt();
        byte[] itemBytes = new byte[itemLength];
        buffer.get(itemBytes);
        this.itemType = new String(itemBytes);
    }
    
    @Override
    public String toString() {
        return "ItemActivatedEvent{" +
                "itemType='" + itemType + '\'' +
                ", playerId=" + playerId +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}