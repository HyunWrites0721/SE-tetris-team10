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
    private JPanel menuPanel; // menuPanel을 필드로 변경
    
    private String[] menuNames = {
        "색맹 모드", "화면 크기 설정", "조작키 설정", "난이도 설정",
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

        // 우측 메뉴 패널 (GridBagLayout으로 중앙 정렬)
        menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setPreferredSize(new Dimension((int)(200*screenRatio), 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 10, 0); // 버튼 간 간격
        gbc.anchor = GridBagConstraints.CENTER;

        menuButtons = new JButton[menuNames.length];
        JPanel buttonBox = new JPanel();
        buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.Y_AXIS));
        buttonBox.setMaximumSize(new Dimension((int)(200*screenRatio), (int)((menuNames.length * 60)*screenRatio)));
        Dimension btnSize = new Dimension((int)(200*screenRatio), (int)(50*screenRatio));
        for (int i = 0; i < menuNames.length; i++) {
            JButton btn = new JButton(menuNames[i]);
            btn.setMinimumSize(btnSize);
            btn.setPreferredSize(btnSize);
            btn.setMaximumSize(btnSize);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFocusable(false); // 버튼이 포커스를 받지 않도록
            
            // 마우스 클릭 이벤트 처리
            final int index = i;
            btn.addActionListener(e -> handleMenuAction(index));
            
            buttonBox.add(btn);
            if (i < menuNames.length - 1)
                buttonBox.add(Box.createVerticalStrut((int)(20*screenRatio)));
            menuButtons[i] = btn;
        }
        menuPanel.add(buttonBox, gbc);
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
                    handleMenuAction(selectedIndex);
                }
            }
        });

        setVisible(true);
        SwingUtilities.invokeLater(menuPanel::requestFocusInWindow);
    }
    
    // 메뉴 액션 처리 (키보드와 마우스 공통)
    private void handleMenuAction(int index) {
        // 마우스 클릭 시에도 selectedIndex 업데이트 및 하이라이트 갱신
        selectedIndex = index;
        StartFrame.updateMenuHighlight(menuButtons, selectedIndex);
        
        if (index == menuButtons.length - 1) { // 나가기
            new StartFrame();
            dispose();
        } else {
            String menuName = menuNames[index];
            SettingMain.SettingViewControllerPair pair = SettingMain.launchSetting(menuName);
            SettingView view = pair.view;
            SettingController controller = pair.controller;

            // 기존 뷰가 있다면 제거하고 새로운 뷰를 추가 (재진입 문제 해결)
            Component[] components = cardPanel.getComponents();
            for (Component comp : components) {
                if (comp.getName() != null && comp.getName().equals(menuName)) {
                    cardPanel.remove(comp);
                    break;
                }
            }
            
            // 새로운 뷰를 항상 추가
            view.setName(menuName);
            cardPanel.add(view, menuName);
            cardPanel.revalidate();
            cardPanel.repaint();
            
            // 콜백 설정은 매번 실행 (여러 번 진입 가능하도록)
            view.resetFocus(); // 뷰를 표시하기 전에 포커스 상태를 초기화합니다.
            
            // 취소 버튼 콜백 설정 - 우측 메뉴로 포커스 복귀
            controller.setOnCancelCallback(() -> {
                cardLayout.show(cardPanel, "empty");
                SwingUtilities.invokeLater(() -> {
                    menuPanel.requestFocusInWindow();
                });
            });
            
            // 확인 버튼 콜백 설정 - 우측 메뉴로 포커스 복귀
            controller.setOnConfirmCallback(() -> {
                cardLayout.show(cardPanel, "empty");
                SwingUtilities.invokeLater(() -> {
                    menuPanel.requestFocusInWindow();
                });
            });
            
            cardLayout.show(cardPanel, menuName);
            
            // 세부 메뉴에 포커스 전달
            SwingUtilities.invokeLater(() -> {
                view.requestFocusInWindow();
                view.setFocusable(true);
            });
        }
    }
}