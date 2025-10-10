package blocks;


public class JBlock extends Block{
	
	public JBlock() {
		setColor(setBlindColor_1(),1);
	}
	
	public void setShape() {
		shape = new int[][] {
			{0,0,1},
			{0,0,1},
			{0,1,1}
		};
	}
}
