package network.messages;

/**
 * 공격(attack) 정보를 담아 전송하는 메시지
 */
public class AttackMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;

    private final int attackLines;
    private final int playerId;
    // optional hole pattern and its x offset (may be null)
    private final int[][] blockPattern;
    private final int blockX;

    public AttackMessage(int attackLines, int playerId) {
        this(attackLines, playerId, null, 0);
    }

    public AttackMessage(int attackLines, int playerId, int[][] blockPattern, int blockX) {
        super(MessageType.ATTACK);
        this.attackLines = attackLines;
        this.playerId = playerId;
        this.blockPattern = blockPattern == null ? null : deepCopy(blockPattern);
        this.blockX = blockX;
    }

    private int[][] deepCopy(int[][] src) {
        if (src == null) return null;
        int[][] out = new int[src.length][];
        for (int i = 0; i < src.length; i++) out[i] = src[i].clone();
        return out;
    }

    public int getAttackLines() { return attackLines; }
    public int getPlayerId() { return playerId; }
    public int[][] getBlockPattern() { return blockPattern; }
    public int getBlockX() { return blockX; }

    @Override
    public String toString() {
        return "AttackMessage{" +
                "attackLines=" + attackLines +
                ", playerId=" + playerId +
                ", blockX=" + blockX +
                ", pattern=" + (blockPattern != null ? (blockPattern.length + "x" + (blockPattern.length>0?blockPattern[0].length:0)) : "<none>") +
                ", id=" + getMessageId() +
                '}';
    }
}
