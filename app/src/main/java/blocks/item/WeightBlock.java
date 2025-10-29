package blocks.item;
import blocks.Block;

public class WeightBlock extends Block {

    public WeightBlock() {
        setColor(setBlindColor_1(),8);
    }

    @Override
    public void setShape() {
        shape = new int[][] {
            {0,0,0,0},
            {0,0,0,0},
            {0,6,6,0},
            {6,6,6,6}
        };
    }

}
