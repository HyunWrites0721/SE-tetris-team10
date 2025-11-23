package p2p;

import javax.swing.*;
import java.awt.*;

/**
 * P2P 원격 게임 화면 패널
 * RemoteGameState를 직접 렌더링
 */
public class RemoteGamePanel extends JPanel {
    private static final int CELL_SIZE = 30;
    private static final int ROWS = 20;
    private static final int COLS = 10;
    
    private RemoteGameState gameState;
    private int currentBlockX = -1;
    private int currentBlockY = -1;
    
    // 블록 색상 (0=빈칸, 1-7=블록 타입별 색상)
    private static final Color[] BLOCK_COLORS = {
        Color.BLACK,        // 0: 빈 칸
        Color.CYAN,         // 1: I
        Color.BLUE,         // 2: J
        Color.ORANGE,       // 3: L
        Color.YELLOW,       // 4: O
        Color.GREEN,        // 5: S
        Color.MAGENTA,      // 6: T
        Color.RED           // 7: Z
    };
    
    public RemoteGamePanel(RemoteGameState gameState) {
        this.gameState = gameState;
        setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        setBackground(Color.BLACK);
    }
    
    public void updateCurrentBlock(int x, int y) {
        this.currentBlockX = x;
        this.currentBlockY = y;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // 보드 그리기 (고정된 블록들)
        int[][] board = gameState.getBoard();
        for (int y = 0; y < ROWS && y < board.length; y++) {
            for (int x = 0; x < COLS && x < board[y].length; x++) {
                int cellValue = board[y][x];
                
                if (cellValue > 0 && cellValue < BLOCK_COLORS.length) {
                    g2d.setColor(BLOCK_COLORS[cellValue]);
                    g2d.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    
                    // 테두리
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
        
        // 현재 블록 그리기 (간단히 한 칸으로 표시)
        if (currentBlockX >= 0 && currentBlockY >= 0) {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(currentBlockX * CELL_SIZE, currentBlockY * CELL_SIZE, 
                        CELL_SIZE, CELL_SIZE);
            g2d.setColor(Color.GRAY);
            g2d.drawRect(currentBlockX * CELL_SIZE, currentBlockY * CELL_SIZE, 
                        CELL_SIZE, CELL_SIZE);
        }
        
        // 그리드 라인
        g2d.setColor(new Color(50, 50, 50));
        for (int y = 0; y <= ROWS; y++) {
            g2d.drawLine(0, y * CELL_SIZE, COLS * CELL_SIZE, y * CELL_SIZE);
        }
        for (int x = 0; x <= COLS; x++) {
            g2d.drawLine(x * CELL_SIZE, 0, x * CELL_SIZE, ROWS * CELL_SIZE);
        }
    }
}
