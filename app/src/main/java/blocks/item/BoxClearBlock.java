package blocks.item;

import blocks.Block;

public class BoxClearBlock extends Block {

    public BoxClearBlock() {
        setColor(setBlindColor_1(),7);
    }

    @Override
    public void setShape() {
        shape = new int[][] {
            {0,0,0},
            {0,3,0},
            {0,0,0}

        };
    }
    
}
