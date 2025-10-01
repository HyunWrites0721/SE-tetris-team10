package blocks;


public class SBlock extends Block {
	
	public SBlock() {
		setColor(4);
	}
	
	public void setShape() {
		shape = new int[][] {
			{0,1,1},
			{1,1,0},
			{0,0,0}
		};
	}
}
