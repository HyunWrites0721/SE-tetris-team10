package game.panels;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import game.model.AttackPreviewItem;

/**
 * 공격줄 미리보기 패널
 * 상대방의 공격줄이 들어오기 전에 어떤 패턴으로 들어올지 보여줌
 */
public class AttackPreviewPanel extends JPanel {
    
    private int cellSize = 30;
    private int fontSize = 16;
    private List<AttackPreviewItem> attackQueue;
    private static final int PREVIEW_ROWS = 10;  // 축소된 10줄
    private static final int PREVIEW_COLS = 10;  // 10열
    
    public AttackPreviewPanel() {
        setOpaque(true);
        setBackground(new Color(240, 240, 240));
        attackQueue = java.util.Collections.emptyList();
    }
    
    /**
     * 공격 큐 업데이트
     */
    public void updateQueue(List<AttackPreviewItem> items) {
        this.attackQueue = items != null ? items : java.util.Collections.emptyList();
        repaint();
    }
    
    /**
     * 셀 크기 설정
     */
    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
        repaint();
    }
    
    /**
     * 폰트 크기 설정
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 제목 그리기
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "ATTACK";
        int titleWidth = fm.stringWidth(title);
        int titleX = (getWidth() - titleWidth) / 2;
        g2d.drawString(title, titleX, fm.getHeight());
        
        // 공격 큐가 비어있으면 "No Attack" 표시
        if (attackQueue.isEmpty()) {
            g2d.setFont(new Font("맑은 고딕", Font.PLAIN, fontSize - 2));
            String noAttack = "No Attack";
            int noAttackWidth = g2d.getFontMetrics().stringWidth(noAttack);
            int noAttackX = (getWidth() - noAttackWidth) / 2;
            int noAttackY = getHeight() / 2;
            g2d.setColor(Color.GRAY);
            g2d.drawString(noAttack, noAttackX, noAttackY);
            return;
        }
        
        // 공격 큐 정보 그리기
        int totalLines = 0;
        for (AttackPreviewItem item : attackQueue) {
            totalLines += item.lines;
        }
        
        g2d.setFont(new Font("맑은 고딕", Font.PLAIN, 9));
        String queueInfo = "Queue: " + totalLines + " lines";
        int queueInfoWidth = g2d.getFontMetrics().stringWidth(queueInfo);
        int queueInfoX = (getWidth() - queueInfoWidth) / 2;
        g2d.setColor(Color.RED);
        int queueInfoY = fm.getHeight() + g2d.getFontMetrics().getHeight();
        g2d.drawString(queueInfo, queueInfoX, queueInfoY);
        
        // 미리보기 그리드 그리기 (10x20 테트리스 보드를 축소)
        int startY = queueInfoY + 5;
        int availableHeight = getHeight() - startY - 5;
        int availableWidth = getWidth() - 10;
        
        // 셀 크기를 사용 가능한 공간에 맞게 계산
        int previewCellSize = Math.min(availableWidth / PREVIEW_COLS, availableHeight / PREVIEW_ROWS);
        previewCellSize = Math.max(previewCellSize, 3);  // 최소 3픽셀
        
        int gridWidth = PREVIEW_COLS * previewCellSize;
        int gridHeight = PREVIEW_ROWS * previewCellSize;
        int gridX = (getWidth() - gridWidth) / 2;
        
        // 전체 보드 틀 그리기 (빈 셀)
        for (int row = 0; row < PREVIEW_ROWS; row++) {
            for (int col = 0; col < PREVIEW_COLS; col++) {
                int x = gridX + col * previewCellSize;
                int y = startY + row * previewCellSize;
                
                // 빈 셀 (밝은 회색)
                g2d.setColor(new Color(220, 220, 220));
                g2d.fillRect(x, y, previewCellSize, previewCellSize);
                
                // 테두리
                g2d.setColor(new Color(200, 200, 200));
                g2d.drawRect(x, y, previewCellSize, previewCellSize);
            }
        }
        
        // 공격 줄을 아래부터 채워나감 (가장 나중에 온 공격이 맨 아래)
        int currentRow = PREVIEW_ROWS - 1;  // 맨 아래부터 시작
        
        // 큐를 역순으로 순회 (마지막 공격부터 그림)
        for (int i = attackQueue.size() - 1; i >= 0; i--) {
            if (currentRow < 0) break;  // 화면 밖으로 나감
            AttackPreviewItem item = attackQueue.get(i);
            
            int[][] pattern = item.pattern;
            int patternHeight = pattern != null ? pattern.length : 0;
            int patternWidth = pattern != null && pattern.length > 0 ? pattern[0].length : 0;
            
            for (int line = 0; line < item.lines && currentRow >= 0; line++, currentRow--) {
                // 각 줄을 회색 블록으로 채움
                for (int col = 0; col < PREVIEW_COLS; col++) {
                    int x = gridX + col * previewCellSize;
                    int y = startY + currentRow * previewCellSize;
                    
                    // 공격 줄 (진한 회색)
                    g2d.setColor(new Color(150, 150, 150));
                    g2d.fillRect(x, y, previewCellSize, previewCellSize);
                    
                    // 테두리
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, y, previewCellSize, previewCellSize);
                }
                
                // 패턴이 있으면 구멍 표시 (흰색)
                if (pattern != null && patternHeight > 0) {
                    int patternRow = line % patternHeight;
                    
                    // blockX 위치를 고려하여 구멍 위치 계산 (보드는 1부터 시작하므로 -1)
                    int holeStartCol = Math.max(0, item.blockX - 1);
                    
                    for (int j = 0; j < patternWidth; j++) {
                        int displayCol = holeStartCol + j;
                        if (displayCol >= PREVIEW_COLS) break;  // 범위 벗어남
                        
                        if (patternRow < pattern.length && j < pattern[patternRow].length) {
                            if (pattern[patternRow][j] == 1) {
                                // 구멍 (흰색)
                                int x = gridX + displayCol * previewCellSize;
                                int y = startY + currentRow * previewCellSize;
                                g2d.setColor(Color.WHITE);
                                g2d.fillRect(x + 1, y + 1, previewCellSize - 2, previewCellSize - 2);
                            }
                        }
                    }
                }
            }
        }
        
        // 외곽 테두리 (테트리스 보드처럼)
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(gridX - 1, startY - 1, gridWidth + 2, gridHeight + 2);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PREVIEW_COLS * cellSize / 2, PREVIEW_ROWS * cellSize / 2 + fontSize * 3);
    }
}
