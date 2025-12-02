package p2p;

import javax.swing.*;
import start.StartFrame;
import network.NetworkConfig;
import java.awt.*;
import java.awt.event.*;

/**
 * P2P 메뉴 화면
 * 서버로 호스트할지, 클라이언트로 참가할지 선택하는 화면
 */
public class P2PMenuFrame extends JFrame {
    private double screenRatio;
    private int selectedIndex = 0;
    private JButton[] menuButtons;
    
    public P2PMenuFrame() {
        // 화면 비율 가져오기
        screenRatio = StartFrame.screenRatio;
        
        // 창 설정
        setTitle("P2P 대전 모드");
        setSize((int)(500 * screenRatio), (int)(400 * screenRatio));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // 모든 창이 닫히면 프로세스 종료
        util.WindowManager.addAutoExitListener(this);
        
        // 제목 라벨
        JLabel titleLabel = new JLabel("P2P 대전 모드", SwingConstants.CENTER);
        titleLabel.setFont(settings.FontManager.getKoreanFont(Font.BOLD, (int)(28 * screenRatio)));
        titleLabel.setBorder(BorderFactory.createEmptyBorder((int)(20 * screenRatio), 0, (int)(20 * screenRatio), 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // 중앙 패널 (버튼들)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalGlue());
        
        // 설명 라벨
        JLabel descLabel = new JLabel("<html><center>P2P 네트워크로 1:1 대전을 시작합니다.<br>서버로 호스트하거나 클라이언트로 참가하세요.</center></html>");
        descLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(12 * screenRatio)));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, (int)(30 * screenRatio), 0));
        centerPanel.add(descLabel);
        
        // 메뉴 버튼들
        String[] menuNames = {"서버로 호스트", "클라이언트로 참가", "뒤로 가기"};
        menuButtons = new JButton[menuNames.length];
        
        for (int i = 0; i < menuNames.length; i++) {
            JButton btn = new JButton(menuNames[i]);
            btn.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(16 * screenRatio)));
            btn.setMaximumSize(new Dimension((int)(250 * screenRatio), (int)(50 * screenRatio)));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFocusable(false);
            
            final int index = i;
            btn.addActionListener(e -> handleMenuAction(index));
            
            centerPanel.add(btn);
            centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(15 * screenRatio))));
            menuButtons[i] = btn;
        }
        
        centerPanel.add(Box.createVerticalGlue());
        add(centerPanel, BorderLayout.CENTER);
        
        // 초기 하이라이트
        updateMenuHighlight();
        
        // 키보드 이벤트
        centerPanel.setFocusable(true);
        centerPanel.requestFocusInWindow();
        centerPanel.addKeyListener(new KeyAdapter() {
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
                }
            }
        });
        
        setVisible(true);
        SwingUtilities.invokeLater(centerPanel::requestFocusInWindow);
    }
    
    private void handleMenuAction(int index) {
        selectedIndex = index;
        updateMenuHighlight();
        
        if (index == 0) {
            // 서버로 호스트
            new P2PServerSetupFrame();
            dispose();
        } else if (index == 1) {
            // 클라이언트로 참가
            new P2PClientSetupFrame();
            dispose();
        } else if (index == 2) {
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
