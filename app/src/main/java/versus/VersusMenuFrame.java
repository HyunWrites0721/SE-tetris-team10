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
        setLayout(new BorderLayout());

        // 제목
        JLabel titleLabel = new JLabel("대전 모드", SwingConstants.CENTER);
        titleLabel.setFont(settings.FontManager.getKoreanFont(Font.BOLD, (int)(36*screenRatio)));
        add(titleLabel, BorderLayout.NORTH);

        // 메뉴 패널
        JPanel menuPanel = new JPanel();
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

            final int index = i;
            btn.addActionListener(e -> handleMenuAction(index));

            menuPanel.add(btn);
            menuPanel.add(Box.createRigidArea(new Dimension((int)(10*screenRatio), (int)(50*screenRatio))));
            menuButtons[i] = btn;
        }

        menuPanel.add(Box.createVerticalStrut((int)(50*screenRatio)));
        add(menuPanel, BorderLayout.CENTER);

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
