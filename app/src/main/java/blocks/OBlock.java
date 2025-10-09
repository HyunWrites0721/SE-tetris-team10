package blocks;


public class OBlock extends Block {
	
	public OBlock() {
		setColor(setBlindColor_1(),3);
	}
	
	public void setShape() {
		shape = new int[][] {
			{1,1},
			{1,1}
		};
	}
}
