package blocks.item;
import blocks.Block;

public class ScoreDoubleBlock extends Block {

    public ScoreDoubleBlock(int[][] baseShape) {
        // Copy the base block's shape into the inherited Block.shape field
        this.shape = copy2D(baseShape);
    }

    // Mark exactly one occupied cell as 5 to indicate score doubling
    @Override
    public void setShape() {
        if (shape == null || shape.length == 0) return;

        java.util.List<int[]> cells = new java.util.ArrayList<>();
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    cells.add(new int[]{r, c});
                }
            }
        }
        if (cells.isEmpty()) return;

        int idx = (int)(Math.random() * cells.size());
        int[] pos = cells.get(idx);
        shape[pos[0]][pos[1]] = 5;
    }

    private static int[][] copy2D(int[][] src) {
        if (src == null) return null;
        int[][] dst = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i] != null ? java.util.Arrays.copyOf(src[i], src[i].length) : null;
        }
        return dst;
    }
    
}
