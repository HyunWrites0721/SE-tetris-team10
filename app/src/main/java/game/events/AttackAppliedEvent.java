package game.events;

/**
 * 로컬에서 공격 줄이 적용된 후, 그 결과를 네트워크로 전송하기 위한 이벤트
 */
public class AttackAppliedEvent extends GameEvent {
    private int attackLines;
    private int[][] blockPattern;
    private int blockX;

    public AttackAppliedEvent() { super("ATTACK_APPLIED"); }

    public AttackAppliedEvent(int attackLines, int[][] blockPattern, int blockX) {
        super("ATTACK_APPLIED");
        this.attackLines = attackLines;
        this.blockPattern = deepCopy(blockPattern);
        this.blockX = blockX;
    }

    public int getAttackLines() { return attackLines; }
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
        // Compute total size: timestamp(8) + attackLines(4) + patternFlag(4)
        // If pattern present: patternH(4) + patternW(4) + blockX(4) + pattern cells (patternH*patternW*4)
        int total = 8 + 4 + 4;
        if (patternFlag == 1) total += 4 + 4 + 4 + (patternH * patternW * 4);
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(total);
        buf.putLong(getTimestamp());
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

    private int[][] deepCopy(int[][] src) {
        if (src == null) return null;
        int[][] out = new int[src.length][];
        for (int i = 0; i < src.length; i++) out[i] = src[i].clone();
        return out;
    }
}
