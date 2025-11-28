package game.model;

/**
 * Simple DTO describing a queued garbage (attack) entry for preview.
 */
public class AttackPreviewItem {
    public final int lines;
    public final int[][] pattern;
    public final int blockX;

    public AttackPreviewItem(int lines, int[][] pattern, int blockX) {
        this.lines = lines;
        this.pattern = deepCopy(pattern);
        this.blockX = blockX;
    }

    private int[][] deepCopy(int[][] src) {
        if (src == null) return null;
        int[][] out = new int[src.length][];
        for (int i = 0; i < src.length; i++) out[i] = src[i].clone();
        return out;
    }
}
