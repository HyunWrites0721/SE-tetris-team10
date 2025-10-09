package blocks;


public class LBlock extends Block{
	
	public LBlock() {
		setColor(setBlindColor_1(),2);
	}
	
	public void setShape() {
		shape = new int[][] {
			{0,1,0},
			{0,1,0},
			{0,1,1}
		};
	}
}
