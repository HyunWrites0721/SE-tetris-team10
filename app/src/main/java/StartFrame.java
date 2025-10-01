import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StartFrame extends JFrame {
    public static final String titleName = "Tetris";
    public static double screenRatio = 1.6; //화면 크기 설정
    private int selectedIndex = 0; // 현재 선택된 메뉴 인덱스
    private JButton[] menuButtons; // 메뉴 버튼 배열

    public StartFrame() {
        //창
        setTitle(titleName); // 창 제목
        setSize((int)(600*screenRatio), (int)(600*screenRatio));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);


        // 전체 레이아웃: BorderLayout
        setLayout(new BorderLayout());


        // 1. 중앙 상단: 게임 제목
        JLabel titleLabel = new JLabel("테트리스", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int)(36*screenRatio)));
        add(titleLabel, BorderLayout.NORTH);


        // 2. 우측 메뉴 버튼들
        JPanel menuPanel = new JPanel(null);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.add(Box.createVerticalStrut((int)(50*screenRatio)));             //메뉴 위아래 여백
        menuPanel.setPreferredSize(new Dimension((int)(200*screenRatio), 0));// 메뉴 패널 가로 사이즈 200으로 맞춤, 세로 자동
        String[] menuNames = {"게임 시작", "설정", "스코어보드", "게임 종료"};
        menuButtons = new JButton[menuNames.length];
        for (int i = 0; i < menuNames.length; i++) {
            JButton btn = new JButton(menuNames[i]);
            btn.setMaximumSize(new Dimension((int)(200*screenRatio), (int)(50*screenRatio)));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuPanel.add(btn);
            menuPanel.add(Box.createRigidArea(new Dimension((int)(10*screenRatio), (int)(50*screenRatio))));
            menuButtons[i] = btn;
        }
        menuPanel.add(Box.createVerticalStrut((int)(50*screenRatio)));            //메뉴 위아래 여백
        add(menuPanel, BorderLayout.EAST);      //오른쪽에 메뉴 패널 얹기


        //초기 버튼 하이라이트
        updateMenuHighlight(menuButtons, selectedIndex);

        //방향키 이벤트 처리
        menuPanel.setFocusable(true);
        menuPanel.requestFocusInWindow();
        menuPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_UP) {
                    selectedIndex = (selectedIndex - 1 + menuButtons.length) % menuButtons.length;
                    updateMenuHighlight(menuButtons, selectedIndex);
                } else if (key == KeyEvent.VK_DOWN) {
                    selectedIndex = (selectedIndex + 1) % menuButtons.length;
                    updateMenuHighlight(menuButtons, selectedIndex);
                } else if (key == KeyEvent.VK_ENTER) {
                    if (selectedIndex == 0) {
                        //게임 시작
                    } 
                    else if (selectedIndex == 1) { 
                        // 설정 메뉴로 이동
                        new SettingFrame(); //설정 창 띄우기
                        dispose(); // 현재 창 닫기 (또는 setVisible(false))
                    } 
                    else if (selectedIndex == 2) {
                        //스코어보드
                    } 
                    else if (selectedIndex == 3) {
                        //게임 종료
                        System.exit(0);
                    }

                } 

            }
        });

        //시각화
        setVisible(true);

        SwingUtilities.invokeLater(menuPanel::requestFocusInWindow);
    }

    // 버튼 하이라이트 함수
    public static void updateMenuHighlight(JButton[] menuButtons, int selectedIndex) {
        for (int i = 0; i < menuButtons.length; i++) {
            if (i == selectedIndex) {
                menuButtons[i].setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            } else {
                menuButtons[i].setBorder(UIManager.getBorder("Button.border"));
            }
        }
    }


}