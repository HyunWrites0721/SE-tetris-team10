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
    
    /**
     * 무게추 블록은 회전 불가능
     */
    @Override
    public void getRotatedShape() {
        // 회전 하지 않음 (무게추는 항상 고정된 모양)
    }

}
