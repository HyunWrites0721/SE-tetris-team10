package versus;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * 대전 모드용 일시정지 화면
 */
public class VersusPauseBoard extends JPanel implements KeyListener {
    private VersusFrameBoard frameBoard;
    private JLabel pauseLabel;
    private JButton resumeButton;
    private JButton mainButton;
    private JButton quitButton;
    private JButton[] buttons;
    private int selectedButtonIndex = 0;
    private double screenRatio;
    
    public VersusPauseBoard(VersusFrameBoard frameBoard) {
        this.frameBoard = frameBoard;
        this.screenRatio = safeScreenRatio();
        
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 200)); // 반투명 검은색
        
        // 상단 패널
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder((int)(40 * screenRatio), 0, 0, 0));
        
        // PAUSED 라벨
        pauseLabel = new JLabel("일시정지", SwingConstants.CENTER);
        pauseLabel.setFont(settings.FontManager.getKoreanFont(Font.BOLD, (int)(32 * screenRatio)));
        pauseLabel.setForeground(Color.YELLOW);
        pauseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        topPanel.add(pauseLabel);
        add(topPanel, BorderLayout.NORTH);
        
        // 중앙 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(
            (int)(50 * screenRatio), 
            (int)(40 * screenRatio), 
            (int)(50 * screenRatio), 
            (int)(40 * screenRatio)
        ));
        
        // 버튼 생성
        resumeButton = createButton("계속하기");
        mainButton = createButton("메인 메뉴");
        quitButton = createButton("게임 종료");
        
        buttons = new JButton[]{resumeButton, mainButton, quitButton};
        
        // 버튼 액션
        resumeButton.addActionListener(e -> {
            frameBoard.togglePause();
        });
        
        mainButton.addActionListener(e -> {
            new start.StartFrame();
            frameBoard.dispose();
        });
        
        quitButton.addActionListener(e -> {
            System.exit(0);
        });
        
        // 버튼 패널에 추가
        for (JButton button : buttons) {
            buttonPanel.add(button);
            buttonPanel.add(Box.createRigidArea(new Dimension(0, (int)(10 * screenRatio))));
        }
        
        add(buttonPanel, BorderLayout.CENTER);
        
        // 초기 버튼 선택
        updateButtonSelection();
        
        // 키 리스너 등록
        addKeyListener(this);
        setFocusable(true);
    }
    
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(settings.FontManager.getKoreanFont(Font.BOLD, (int)(14 * screenRatio)));
        button.setPreferredSize(new Dimension((int)(200 * screenRatio), (int)(50 * screenRatio)));
        button.setMaximumSize(new Dimension((int)(200 * screenRatio), (int)(50 * screenRatio)));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusable(false);
        
        // 버튼 색상 설정 (검은 배경 + 흰색 텍스트)
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        
        return button;
    }
    
    private void updateButtonSelection() {
        for (int i = 0; i < buttons.length; i++) {
            if (i == selectedButtonIndex) {
                // 선택된 버튼: 빨간 테두리 + 검은 배경 + 흰색 텍스트
                buttons[i].setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                buttons[i].setBackground(Color.BLACK);
                buttons[i].setForeground(Color.WHITE);
            } else {
                // 선택되지 않은 버튼: 기본 테두리 + 검은 배경 + 흰색 텍스트
                buttons[i].setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                buttons[i].setBackground(Color.BLACK);
                buttons[i].setForeground(Color.WHITE);
            }
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                frameBoard.togglePause();
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
            case KeyEvent.VK_I:
                selectedButtonIndex = (selectedButtonIndex - 1 + buttons.length) % buttons.length;
                updateButtonSelection();
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
            case KeyEvent.VK_K:
                selectedButtonIndex = (selectedButtonIndex + 1) % buttons.length;
                updateButtonSelection();
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                buttons[selectedButtonIndex].doClick();
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    private static double safeScreenRatio() {
        double r = start.StartFrame.screenRatio;
        if (Double.isNaN(r) || Double.isInfinite(r) || r <= 0.0) return 1.2;
        return r;
    }
}
