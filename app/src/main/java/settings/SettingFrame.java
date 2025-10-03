package settings;
import javax.swing.*;

import start.StartFrame;

import java.awt.*;
import java.awt.event.*;

public class SettingFrame extends JFrame {

    private double screenRatio = StartFrame.screenRatio;
    private int selectedIndex = 0;
    private JButton[] menuButtons;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    
    private String[] menuNames = {
        "색맹 모드", "화면 크기 설정", "조작키 설정", 
        "스코어보드 초기화", "설정값 초기화", "나가기"
    };

    public SettingFrame() {
        setTitle(StartFrame.titleName);
        setSize((int)(600*screenRatio), (int)(600*screenRatio));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 상단 제목
        JLabel titleLabel = new JLabel("게임 설정", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int)(36*screenRatio)));
        add(titleLabel, BorderLayout.NORTH);

        // 우측 메뉴 패널
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setPreferredSize(new Dimension((int)(200*screenRatio), 0));

        menuButtons = new JButton[menuNames.length];
        for (int i = 0; i < menuNames.length; i++) {
            JButton btn = new JButton(menuNames[i]);
            btn.setMaximumSize(new Dimension((int)(200*screenRatio), (int)(50*screenRatio)));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuPanel.add(btn);
            menuPanel.add(Box.createRigidArea(new Dimension(0, (int)(20*screenRatio))));
            menuButtons[i] = btn;
        }
        add(menuPanel, BorderLayout.EAST);

        // 초기 하이라이트
        StartFrame.updateMenuHighlight(menuButtons, selectedIndex);

        // 카드 레이아웃 패널 생성
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        add(cardPanel, BorderLayout.CENTER);

        // 방향키 이벤트 + 엔터키 처리
        menuPanel.setFocusable(true);
        menuPanel.requestFocusInWindow();
        menuPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_UP) {
                    selectedIndex = (selectedIndex -1 + menuButtons.length) % menuButtons.length;
                    StartFrame.updateMenuHighlight(menuButtons, selectedIndex);
                } else if (key == KeyEvent.VK_DOWN) {
                    selectedIndex = (selectedIndex +1) % menuButtons.length;
                    StartFrame.updateMenuHighlight(menuButtons, selectedIndex);
                } else if (key == KeyEvent.VK_ENTER) {
                    if (selectedIndex == menuButtons.length -1) { // 나가기
                        new StartFrame();
                        dispose();
                    } else {
                        String menuName = menuNames[selectedIndex];
                        SettingView view = SettingMain.launchSetting(menuName);

                        // 카드 중복 추가 방지
                        boolean exists = false;
                        for (Component comp : cardPanel.getComponents()) {
                            if (comp.getName() != null && comp.getName().equals(menuName)) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            view.setName(menuName);
                            cardPanel.add(view, menuName);
                            cardPanel.revalidate();
                            cardPanel.repaint();
                        }
                        cardLayout.show(cardPanel, menuName);
                    }
                }
            }
        });

        setVisible(true);
        SwingUtilities.invokeLater(menuPanel::requestFocusInWindow);
    }
}