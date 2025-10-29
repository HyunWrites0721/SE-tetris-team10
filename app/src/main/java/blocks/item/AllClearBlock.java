package blocks.item;

import blocks.Block;

public class AllClearBlock extends Block {

    public AllClearBlock() {
        setColor(setBlindColor_1(),7);
    }

    @Override
    public void setShape() {
        shape = new int[][] {
            {2, 2},
            {2, 2},

        };
    }
    
}
