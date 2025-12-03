package start;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 메인 화면 배경에 떨어지는 블록 애니메이션을 표시하는 패널
 */
public class BackgroundAnimationPanel extends JPanel {
    
    private static final int CELL_SIZE = 30; // 블록 한 칸 크기 (더 작게)
    private List<FallingBlock> fallingBlocks;
    private Timer animationTimer;
    private Random random;
    private int panelWidth;
    private int panelHeight;
    private BufferedImage backgroundImage;
    
    // 테트리스 블록 모양 정의
    private static final int[][][] SHAPES = {
        // I 블록
        {{1, 1, 1, 1}},
        // O 블록
        {{1, 1}, {1, 1}},
        // T 블록
        {{0, 1, 0}, {1, 1, 1}},
        // L 블록
        {{1, 0}, {1, 0}, {1, 1}},
        // J 블록
        {{0, 1}, {0, 1}, {1, 1}},
        // S 블록
        {{0, 1, 1}, {1, 1, 0}},
        // Z 블록
        {{1, 1, 0}, {0, 1, 1}}
    };
    
    // 블록 색상
    private static final Color[] COLORS = {
        new Color(0, 240, 240),   // I - 시안
        new Color(240, 240, 0),   // O - 노랑
        new Color(160, 0, 240),   // T - 보라
        new Color(240, 160, 0),   // L - 주황
        new Color(0, 0, 240),     // J - 파랑
        new Color(0, 240, 0),     // S - 초록
        new Color(240, 0, 0)      // Z - 빨강
    };
    
    public BackgroundAnimationPanel(int width, int height) {
        this(width, height, true); // 기본적으로 애니메이션 활성화
    }
    
    public BackgroundAnimationPanel(int width, int height, boolean enableAnimation) {
        this.panelWidth = width;
        this.panelHeight = height;
        this.fallingBlocks = new ArrayList<>();
        this.random = new Random();
        
        setPreferredSize(new Dimension(width, height));
        setOpaque(true); // 배경 이미지를 그리기 위해 불투명으로 설정
        
        // 배경 이미지 로드 (우주 별 이미지)
        loadBackgroundImage();
        
        // 애니메이션이 활성화된 경우에만 블록 생성 및 타이머 시작
        if (enableAnimation) {
            // 초기 블록 생성 (전체 화면이므로 더 많이!)
            for (int i = 0; i < 25; i++) {
                addRandomBlock();
            }
            
            // 애니메이션 타이머 (60 FPS)
            animationTimer = new Timer(16, e -> {
                updateBlocks();
                repaint();
            });
            animationTimer.start();
        }
    }
    
    /**
     * 배경 이미지 로드
     */
    private void loadBackgroundImage() {
        try {
            // 우주 배경 이미지 로드 (별 이미지 타일링)
            backgroundImage = createSpaceBackground(panelWidth, panelHeight);
        } catch (Exception e) {
            System.err.println("배경 이미지 생성 실패: " + e.getMessage());
            backgroundImage = null;
        }
    }
    
    /**
     * 우주 배경 생성 (별이 빛나는 검은 배경)
     */
    private BufferedImage createSpaceBackground(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // 검은 배경
        g2d.setColor(new Color(10, 15, 35)); // 어두운 남색
        g2d.fillRect(0, 0, width, height);
        
        // 별 그리기
        Random rand = new Random(12345); // 고정된 시드로 별 위치 일정하게
        
        // 작은 별들 (흰색)
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 200; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            int size = 1 + rand.nextInt(2);
            g2d.fillOval(x, y, size, size);
        }
        
        // 반짝이는 별들 (노란색)
        for (int i = 0; i < 30; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            
            // 별 모양 그리기
            g2d.setColor(new Color(255, 255, 200, 255));
            drawStar(g2d, x, y, 3 + rand.nextInt(3));
        }
        
        g2d.dispose();
        return image;
    }
    
    /**
     * 별 모양 그리기
     */
    private void drawStar(Graphics2D g2d, int x, int y, int size) {
        // 4방향 빛 선 그리기
        g2d.drawLine(x - size, y, x + size, y); // 가로
        g2d.drawLine(x, y - size, x, y + size); // 세로
        
        // 중심점
        g2d.fillOval(x - 1, y - 1, 3, 3);
    }
    
    /**
     * 랜덤한 위치와 속도로 새로운 블록 추가
     */
    private void addRandomBlock() {
        int shapeIndex = random.nextInt(SHAPES.length);
        int[][] shape = SHAPES[shapeIndex];
        Color color = COLORS[shapeIndex];
        
        // 랜덤 X 위치 (화면 내부)
        int maxX = (panelWidth / CELL_SIZE) - shape[0].length;
        int x = random.nextInt(Math.max(1, maxX)) * CELL_SIZE;
        
        // 화면 위쪽에서 시작 (더 넓은 범위로 분산)
        int y = -shape.length * CELL_SIZE - random.nextInt(400);
        
        // 랜덤 속도 (0.5~4 픽셀/프레임 - 더 다양한 속도)
        double speed = 0.5 + random.nextDouble() * 3.5;
        
        // 랜덤 투명도 (60%~80% - 덜 투명하게)
        float alpha = 0.6f + random.nextFloat() * 0.2f;
        
        fallingBlocks.add(new FallingBlock(x, y, shape, color, speed, alpha));
    }
    
    /**
     * 블록 위치 업데이트
     */
    private void updateBlocks() {
        List<FallingBlock> blocksToRemove = new ArrayList<>();
        
        for (FallingBlock block : fallingBlocks) {
            block.y += block.speed;
            
            // 화면 아래로 나간 블록 제거
            if (block.y > panelHeight) {
                blocksToRemove.add(block);
            }
        }
        
        // 제거된 블록만큼 새로운 블록 추가
        fallingBlocks.removeAll(blocksToRemove);
        for (int i = 0; i < blocksToRemove.size(); i++) {
            addRandomBlock();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // 배경 이미지 그리기
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, null);
        }
        
        // 안티앨리어싱
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 모든 블록 그리기
        for (FallingBlock block : fallingBlocks) {
            block.draw(g2d);
        }
    }
    
    /**
     * 애니메이션 정지
     */
    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
    
    /**
     * 떨어지는 블록 클래스
     */
    private class FallingBlock {
        int x;
        double y;
        int[][] shape;
        Color color;
        double speed;
        float alpha;
        
        FallingBlock(int x, double y, int[][] shape, Color color, double speed, float alpha) {
            this.x = x;
            this.y = y;
            this.shape = shape;
            this.color = color;
            this.speed = speed;
            this.alpha = alpha;
        }
        
        void draw(Graphics2D g2d) {
            // 투명도 적용
            Composite oldComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            
            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[row].length; col++) {
                    if (shape[row][col] == 1) {
                        int drawX = x + col * CELL_SIZE;
                        int drawY = (int)y + row * CELL_SIZE;
                        
                        // 블록 채우기
                        g2d.setColor(color);
                        g2d.fillRect(drawX, drawY, CELL_SIZE, CELL_SIZE);
                        
                        // 테두리
                        g2d.setColor(color.darker());
                        g2d.drawRect(drawX, drawY, CELL_SIZE, CELL_SIZE);
                        
                        // 하이라이트 효과
                        g2d.setColor(new Color(255, 255, 255, 100));
                        g2d.fillRect(drawX + 2, drawY + 2, CELL_SIZE - 4, CELL_SIZE / 3);
                    }
                }
            }
            
            // 원래 투명도로 복원
            g2d.setComposite(oldComposite);
        }
    }
}
