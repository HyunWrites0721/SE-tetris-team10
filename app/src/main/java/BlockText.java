import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BlockText extends JPanel {

    private final GameBoard gameBoard;
    private final List<JLabel> textLabels;
    private final int ROWS = 20;
    private final int COLS = 10;

    public BlockText(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        this.textLabels = new ArrayList<>();
        
        setLayout(null); // 절대 위치 설정을 위해
        setOpaque(false); // 투명 배경
        
        createTextLabels();
    }
    
    private void createTextLabels() {
        // 기존 라벨들 제거
        removeAll();
        textLabels.clear();
        
        // 20x10 격자에 맞춰 라벨 생성
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                JLabel label = new JLabel("@");
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setVerticalAlignment(SwingConstants.CENTER);
                label.setForeground(Color.BLACK);
                
                textLabels.add(label);
                add(label);
            }
        }
        
        updateLabelPositions();
    }
    
    public void updateLabelPositions() {
        if (gameBoard == null || textLabels.isEmpty()) return;
        
        // GameBoard의 현재 설정값들 가져오기
        int cellSize = gameBoard.CELL_SIZE;
        int margin = gameBoard.MARGIN;
        
        // GameBoard의 위치 계산 (GameBoard.paintComponent와 동일한 로직)
        int boardWidth = COLS * cellSize;
        int x = (gameBoard.getWidth() - boardWidth) / 3;
        int y = margin * cellSize;
        
        // 폰트 크기 계산 (셀 크기의 70%)
        int fontSize = (int)(cellSize * 0.7);
        Font font = new Font("Arial", Font.BOLD, fontSize);
        
        // 각 라벨의 위치와 크기 업데이트
        int labelIndex = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (labelIndex < textLabels.size()) {
                    JLabel label = textLabels.get(labelIndex);
                    label.setFont(font);
                    
                    // 라벨 위치와 크기 설정
                    int labelX = x + col * cellSize;
                    int labelY = y + row * cellSize;
                    label.setBounds(labelX, labelY, cellSize, cellSize);
                    
                    labelIndex++;
                }
            }
        }
        
        repaint();
    }
    
    // GameBoard 크기가 변경될 때 호출
    public void onGameBoardScaleChanged() {
        updateLabelPositions();
    }
}