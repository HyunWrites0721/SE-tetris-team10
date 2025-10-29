package blocks.item;

import blocks.Block;

public class OneLineClearBlock extends Block {

    public OneLineClearBlock(int[][] baseShape) {
        // Initialize the inherited 'shape' field with a deep copy of the base block's shape
        this.shape = copy2D(baseShape);
    }

    // Ensure exactly one non-zero cell is marked with 4 to visualize the item
    @Override
    public void setShape() {
        if (shape == null || shape.length == 0) return;

        // Collect all non-zero cell positions
        java.util.List<int[]> cells = new java.util.ArrayList<>();
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    cells.add(new int[]{r, c});
                }
            }
        }
        if (cells.isEmpty()) return;

        int idx = (int) (Math.random() * cells.size());
        int[] pos = cells.get(idx);
        shape[pos[0]][pos[1]] = 4; // Mark selected cell as item
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
