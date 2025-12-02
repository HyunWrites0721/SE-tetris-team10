package versus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import start.StartFrame;

/**
 * 대전 모드 결과 화면
 * 승패 표시 및 재시작/메뉴 선택
 */
public class VersusResultBoard extends JPanel {
    private VersusFrameBoard frameBoard;
    private int winner;
    private int winnerScore;
    private int loserScore;
    private double screenRatio;
    
    private JLabel resultLabel;
    private JLabel scoreLabel;
    private JButton[] menuButtons;
    private int selectedIndex = 0;
    
    public VersusResultBoard(VersusFrameBoard frameBoard) {
        this.frameBoard = frameBoard;
        this.screenRatio = safeScreenRatio();
        
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 200));  // 반투명 검은색
        
        // 중앙 패널
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        // 승자 표시
        resultLabel = new JLabel("", SwingConstants.CENTER);
        resultLabel.setFont(settings.FontManager.getKoreanFont(Font.BOLD, (int)(48 * screenRatio)));
        resultLabel.setForeground(Color.YELLOW);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 점수 표시
        scoreLabel = new JLabel("", SwingConstants.CENTER);
        scoreLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(24 * screenRatio)));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(resultLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(20 * screenRatio))));
        centerPanel.add(scoreLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(50 * screenRatio))));
        
        // 메뉴 버튼들
        String[] menuNames = {"다시 하기", "대전 모드 메뉴", "메인 메뉴"};
        menuButtons = new JButton[menuNames.length];
        
        for (int i = 0; i < menuNames.length; i++) {
            JButton btn = new JButton(menuNames[i]);
            btn.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(16 * screenRatio)));
            btn.setMaximumSize(new Dimension((int)(200 * screenRatio), (int)(50 * screenRatio)));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFocusable(false);
            
            final int index = i;
            btn.addActionListener(e -> handleMenuAction(index));
            
            centerPanel.add(btn);
            centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(10 * screenRatio))));
            menuButtons[i] = btn;
        }
        
        centerPanel.add(Box.createVerticalGlue());
        add(centerPanel, BorderLayout.CENTER);
        
        // 키보드 이벤트
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W || key == KeyEvent.VK_I) {
                    selectedIndex = (selectedIndex - 1 + menuButtons.length) % menuButtons.length;
                    updateMenuHighlight();
                } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S || key == KeyEvent.VK_K) {
                    selectedIndex = (selectedIndex + 1) % menuButtons.length;
                    updateMenuHighlight();
                } else if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
                    handleMenuAction(selectedIndex);
                }
            }
        });
    }
    
    /**
     * 결과 표시
     */
    public void showResult(int winner, int winnerScore, int loserScore) {
        this.winner = winner;
        this.winnerScore = winnerScore;
        this.loserScore = loserScore;
        
        resultLabel.setText("Player " + winner + " 승리!");
        scoreLabel.setText("P" + winner + ": " + winnerScore + "점  vs  P" + (3-winner) + ": " + loserScore + "점");
        
        selectedIndex = 0;
        updateMenuHighlight();
    }
    
    /**
     * 무승부 표시 (시간제한 모드)
     */
    public void showDraw(int finalScore) {
        this.winner = 0;
        this.winnerScore = finalScore;
        this.loserScore = finalScore;
        
        resultLabel.setText("무승부!");
        scoreLabel.setText("P1: " + finalScore + "점  vs  P2: " + finalScore + "점");
        
        selectedIndex = 0;
        updateMenuHighlight();
    }
    
    /**
     * 메뉴 액션 처리
     */
    private void handleMenuAction(int index) {
        if (index == 0) {
            // 다시 하기 - 같은 모드로 재시작
            VersusMode currentMode = VersusMode.NORMAL;  // TODO: frameBoard에서 현재 모드 가져오기
            int currentDifficulty = 0;  // TODO: frameBoard에서 난이도 가져오기
            new VersusGameStart(currentMode);
            frameBoard.dispose();
        } else if (index == 1) {
            // 대전 모드 메뉴로
            new VersusMenuFrame();
            frameBoard.dispose();
        } else if (index == 2) {
            // 메인 메뉴로
            new StartFrame();
            frameBoard.dispose();
        }
    }
    
    /**
     * 메뉴 하이라이트 업데이트
     */
    private void updateMenuHighlight() {
        for (int i = 0; i < menuButtons.length; i++) {
            if (i == selectedIndex) {
                menuButtons[i].setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            } else {
                menuButtons[i].setBorder(UIManager.getBorder("Button.border"));
            }
        }
    }
    
    private static double safeScreenRatio() {
        double r = start.StartFrame.screenRatio;
        if (Double.isNaN(r) || Double.isInfinite(r) || r <= 0.0) return 1.2;
        return r;
    }
}
