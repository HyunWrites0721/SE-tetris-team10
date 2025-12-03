package versus;

import javax.swing.*;
import start.StartFrame;
import java.awt.*;
import java.awt.event.*;

/**
 * 대전 모드 선택 화면
 * - 일반 모드
 * - 아이템 모드
 * - 시간 제한 모드
 */
public class VersusMenuFrame extends JFrame {
    private int selectedIndex = 0;
    private JButton[] menuButtons;
    private double screenRatio;

    public VersusMenuFrame() {
        // 화면 비율 가져오기
        screenRatio = StartFrame.screenRatio;

        setTitle("대전 모드 선택");
        setSize((int)(600*screenRatio), (int)(600*screenRatio));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // JLayeredPane을 사용하여 배경과 전경 분리
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension((int)(600*screenRatio), (int)(600*screenRatio)));
        setContentPane(layeredPane);

        // 배경 레이어: 애니메이션 패널
        start.BackgroundAnimationPanel animationPanel = new start.BackgroundAnimationPanel(
            (int)(600 * screenRatio), 
            (int)(600 * screenRatio)
        );
        animationPanel.setBounds(0, 0, (int)(600*screenRatio), (int)(600*screenRatio));
        layeredPane.add(animationPanel, JLayeredPane.DEFAULT_LAYER);

        // 전경 레이어: 메인 컨텐츠
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBounds(0, 0, (int)(600*screenRatio), (int)(600*screenRatio));
        layeredPane.add(mainPanel, JLayeredPane.PALETTE_LAYER);

        // 제목
        JLabel titleLabel = new JLabel("대전 모드", SwingConstants.CENTER);
        titleLabel.setFont(settings.FontManager.getKoreanFont(Font.BOLD, (int)(36*screenRatio)));
        titleLabel.setForeground(Color.WHITE); // 흰색 텍스트
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 메뉴 패널
        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.add(Box.createVerticalStrut((int)(50*screenRatio)));

        String[] menuNames = {"일반 모드", "아이템 모드", "시간 제한 모드", "뒤로 가기"};
        menuButtons = new JButton[menuNames.length];

        for (int i = 0; i < menuNames.length; i++) {
            JButton btn = new JButton(menuNames[i]);
            btn.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14*screenRatio)));
            btn.setMaximumSize(new Dimension((int)(200*screenRatio), (int)(50*screenRatio)));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFocusable(false);

            // 버튼 색상 설정 (우주 배경색과 동일)
            Color bgColor = new Color(10, 15, 35); // 우주 배경색
            btn.setBackground(bgColor);
            btn.setForeground(Color.WHITE);
            btn.setOpaque(true);
            btn.setBorderPainted(true);
            btn.setContentAreaFilled(true);
            btn.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // 기본 UI로 강제 설정

            final int index = i;
            btn.addActionListener(e -> handleMenuAction(index));

            menuPanel.add(btn);
            menuPanel.add(Box.createRigidArea(new Dimension((int)(10*screenRatio), (int)(50*screenRatio))));
            menuButtons[i] = btn;
        }

        menuPanel.add(Box.createVerticalStrut((int)(50*screenRatio)));
        mainPanel.add(menuPanel, BorderLayout.CENTER);

        // 초기 하이라이트
        updateMenuHighlight();

        // 키보드 이벤트
        menuPanel.setFocusable(true);
        menuPanel.requestFocusInWindow();
        menuPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_UP) {
                    selectedIndex = (selectedIndex - 1 + menuButtons.length) % menuButtons.length;
                    updateMenuHighlight();
                } else if (key == KeyEvent.VK_DOWN) {
                    selectedIndex = (selectedIndex + 1) % menuButtons.length;
                    updateMenuHighlight();
                } else if (key == KeyEvent.VK_ENTER) {
                    handleMenuAction(selectedIndex);
                } else if (key == KeyEvent.VK_ESCAPE) {
                    // ESC로 뒤로 가기
                    new StartFrame();
                    dispose();
                }
            }
        });

        setVisible(true);
        SwingUtilities.invokeLater(menuPanel::requestFocusInWindow);
    }

    private void handleMenuAction(int index) {
        selectedIndex = index;
        updateMenuHighlight();

        if (index == 0) {
            // 일반 대전 모드
            new VersusGameStart(VersusMode.NORMAL);
            dispose();
        } else if (index == 1) {
            // 아이템 대전 모드
            new VersusGameStart(VersusMode.ITEM);
            dispose();
        } else if (index == 2) {
            // 시간 제한 대전 모드
            new VersusGameStart(VersusMode.TIME_LIMIT);
            dispose();
        } else if (index == 3) {
            // 뒤로 가기
            new StartFrame();
            dispose();
        }
    }

    private void updateMenuHighlight() {
        for (int i = 0; i < menuButtons.length; i++) {
            if (i == selectedIndex) {
                menuButtons[i].setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            } else {
                menuButtons[i].setBorder(UIManager.getBorder("Button.border"));
            }
        }
    }
}
