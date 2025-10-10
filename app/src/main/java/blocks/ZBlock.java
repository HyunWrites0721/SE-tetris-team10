package blocks;


public class ZBlock extends Block {
	
	public ZBlock() {
		setColor(setBlindColor_1(),6);
	}
	
	public void setShape() {
		shape = new int[][] {
			{1,1,0},
			{0,1,1},
			{0,0,0}
		};
	}
}
