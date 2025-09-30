package blocks;


public class IBlock extends Block {
	
	public IBlock() {
		setColor(0);
	}

	public void setShape() {
		shape = new int[][] {
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,1,1,1,1},
			{0,0,0,0,0},
			{0,0,0,0,0}
		};
	}
	
}
