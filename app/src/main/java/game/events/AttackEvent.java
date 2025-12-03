package game.events;

/**
 * 네트워크로부터 수신된 공격 정보를 이벤트로 변환하여 UI/컨트롤러가 처리하도록 함
 */
public class AttackEvent extends GameEvent {
    private int attackLines;
    private int playerId;
    private int[][] blockPattern;
    private int blockX;

    public AttackEvent(int attackLines, int playerId) {
        super("ATTACK");
        this.attackLines = attackLines;
        this.playerId = playerId;
    }

    public AttackEvent(int attackLines, int playerId, int[][] blockPattern, int blockX) {
        super("ATTACK");
        this.attackLines = attackLines;
        this.playerId = playerId;
        this.blockPattern = blockPattern == null ? null : deepCopy(blockPattern);
        this.blockX = blockX;
    }

    // 기본 생성자 (역직렬화용)
    public AttackEvent() {
        super("ATTACK");
    }

    public int getAttackLines() { return attackLines; }
    public int getPlayerId() { return playerId; }
    public int[][] getBlockPattern() { return blockPattern == null ? null : deepCopy(blockPattern); }
    public int getBlockX() { return blockX; }

    @Override
    public byte[] serialize() {
        int patternFlag = (blockPattern == null) ? 0 : 1;
        int patternH = 0, patternW = 0;
        if (patternFlag == 1) {
            patternH = blockPattern.length;
            patternW = blockPattern.length > 0 ? blockPattern[0].length : 0;
        }
        // timestamp(8) + playerId(4) + attackLines(4) + patternFlag(4)
        // if pattern: + patternH(4) + patternW(4) + blockX(4) + cells(patternH*patternW*4)
        int bufferSize = 8 + 4 + 4 + 4;
        if (patternFlag == 1) {
            bufferSize += 4 + 4 + 4 + (patternH * patternW * 4);
        }
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(bufferSize);
        buf.putLong(getTimestamp());
        buf.putInt(playerId);
        buf.putInt(attackLines);
        buf.putInt(patternFlag);
        if (patternFlag == 1) {
            buf.putInt(patternH);
            buf.putInt(patternW);
            buf.putInt(blockX);
            for (int r = 0; r < patternH; r++) for (int c = 0; c < patternW; c++) buf.putInt(blockPattern[r][c]);
        }
        return buf.array();
    }

    @Override
    public void deserialize(byte[] data) {
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.wrap(data);
        buf.getLong(); // timestamp
        this.playerId = buf.getInt();
        this.attackLines = buf.getInt();
        int patternFlag = buf.getInt();
        if (patternFlag == 1) {
            int patternH = buf.getInt();
            int patternW = buf.getInt();
            this.blockX = buf.getInt();
            this.blockPattern = new int[patternH][patternW];
            for (int r = 0; r < patternH; r++) for (int c = 0; c < patternW; c++) this.blockPattern[r][c] = buf.getInt();
        } else {
            this.blockPattern = null;
            this.blockX = 0;
        }
    }

    @Override
    public String toString() {
        return "AttackEvent{playerId=" + playerId + ", attackLines=" + attackLines + ", blockX=" + blockX + ", pattern=" + (blockPattern!=null? (blockPattern.length+"x"+(blockPattern.length>0?blockPattern[0].length:0)) : "<none>") + "}";
    }

    private int[][] deepCopy(int[][] src) {
        if (src == null) return null;
        int[][] out = new int[src.length][];
        for (int i = 0; i < src.length; i++) out[i] = src[i].clone();
        return out;
    }
}
