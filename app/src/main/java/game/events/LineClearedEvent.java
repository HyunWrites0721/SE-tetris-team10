package game.events;

import java.nio.ByteBuffer;

/**
 * 라인이 클리어되었을 때 발생하는 이벤트
 */
public class LineClearedEvent extends GameEvent {
    private int[] clearedLines;
    private int score;
    private int playerId;
    // optional last block pattern and its X position (used for attack hole reproduction)
    private int[][] lastBlockPattern;
    private int lastBlockX;
    
    public LineClearedEvent(int[] clearedLines, int score, int playerId) {
        this(clearedLines, score, playerId, null, 0);
    }

    public LineClearedEvent(int[] clearedLines, int score, int playerId, int[][] lastBlockPattern, int lastBlockX) {
        super("LINE_CLEARED");
        this.clearedLines = clearedLines == null ? new int[0] : clearedLines.clone(); // defensive copy
        this.score = score;
        this.playerId = playerId;
        this.lastBlockPattern = lastBlockPattern == null ? null : deepCopy(lastBlockPattern);
        this.lastBlockX = lastBlockX;
    }

    private int[][] deepCopy(int[][] src) {
        if (src == null) return null;
        int[][] out = new int[src.length][];
        for (int i = 0; i < src.length; i++) out[i] = src[i].clone();
        return out;
    }
    
    // 기본 생성자 (역직렬화용)
    public LineClearedEvent() {
        super("LINE_CLEARED");
    }
    
    // Getters
    public int[] getClearedLines() { return clearedLines.clone(); }
    public int getScore() { return score; }
    public int getPlayerId() { return playerId; }
    public int[][] getLastBlockPattern() { return lastBlockPattern == null ? null : deepCopy(lastBlockPattern); }
    public int getLastBlockX() { return lastBlockX; }
    
    @Override
    public byte[] serialize() {
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
             java.io.DataOutputStream dos = new java.io.DataOutputStream(baos)) {

            // timestamp
            dos.writeLong(getTimestamp());
            // score, playerId
            dos.writeInt(score);
            dos.writeInt(playerId);

            // cleared lines
            int lineCount = clearedLines == null ? 0 : clearedLines.length;
            dos.writeInt(lineCount);
            if (lineCount > 0) {
                for (int line : clearedLines) dos.writeInt(line);
            }

            // pattern flag and optional pattern data
            int patternFlag = (lastBlockPattern == null) ? 0 : 1;
            dos.writeInt(patternFlag);
            if (patternFlag == 1) {
                int patternH = lastBlockPattern.length;
                int patternW = 0;
                for (int r = 0; r < patternH; r++) if (lastBlockPattern[r] != null) patternW = Math.max(patternW, lastBlockPattern[r].length);
                // ensure we serialize in rectangular form: pad missing cells with 0
                dos.writeInt(patternH);
                dos.writeInt(patternW);
                dos.writeInt(lastBlockX);
                for (int r = 0; r < patternH; r++) {
                    int[] row = lastBlockPattern[r];
                    for (int c = 0; c < patternW; c++) {
                        int val = (row != null && c < row.length) ? row[c] : 0;
                        dos.writeInt(val);
                    }
                }
            }

            dos.flush();
            return baos.toByteArray();
        } catch (java.io.IOException ioe) {
            // should not happen with ByteArrayOutputStream
            System.err.println("LineClearedEvent.serialize I/O error: " + ioe.getMessage());
            return new byte[0];
        }
    }
    
    @Override
    public void deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.getLong(); // timestamp skip
        this.score = buffer.getInt();
        this.playerId = buffer.getInt();
        int lineCount = buffer.getInt();
        this.clearedLines = new int[lineCount];
        for (int i = 0; i < lineCount; i++) this.clearedLines[i] = buffer.getInt();
        int patternFlag = buffer.getInt();
        if (patternFlag == 1) {
            int patternH = buffer.getInt();
            int patternW = buffer.getInt();
            this.lastBlockX = buffer.getInt();
            this.lastBlockPattern = new int[patternH][patternW];
            for (int r = 0; r < patternH; r++) for (int c = 0; c < patternW; c++) this.lastBlockPattern[r][c] = buffer.getInt();
        } else {
            this.lastBlockPattern = null;
            this.lastBlockX = 0;
        }
    }
    
    @Override
    public String toString() {
        return "LineClearedEvent{" +
                "clearedLines=" + java.util.Arrays.toString(clearedLines) +
                ", score=" + score +
                ", playerId=" + playerId +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}