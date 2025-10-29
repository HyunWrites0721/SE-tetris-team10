package start;

import javax.swing.*;

import settings.SettingFrame;
import game.GameStart;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class StartFrame extends JFrame {
    // JSON 매핑용 내부 클래스 (Gson 정상 동작)
    static class SettingSaveData {
        String screenSize;
        boolean colorBlindMode;
        String controlType;
    }
    
    public static final String titleName = "Tetris";

    public static double screenRatio; //화면크기
    
    private int selectedIndex = 0; // 현재 선택된 메뉴 인덱스
    private JButton[] menuButtons; // 메뉴 버튼 배열

    // 설정 파일의 절대 경로를 반환하는 헬퍼 메서드
    private static java.nio.file.Path getSettingFilePath(String filename) {
        // 현재 작업 디렉토리 확인
        String currentDir = System.getProperty("user.dir");
        // app 폴더가 현재 디렉토리인지 확인
        File appDir = new File(currentDir);
        if (appDir.getName().equals("app")) {
            // 이미 app 폴더 안에 있음
            return java.nio.file.Paths.get(currentDir, "src/main/java/settings/data", filename);
        } else {
            // 프로젝트 루트에 있음
            return java.nio.file.Paths.get(currentDir, "app/src/main/java/settings/data", filename);
        }
    }

    public StartFrame() {
        // 화면 비율 값 초기화
        screenRatio = 1.2; // 기본값
        try {
            java.nio.file.Path path = getSettingFilePath("SettingSave.json");
            String json = java.nio.file.Files.readString(path);
            com.google.gson.Gson gson = new com.google.gson.Gson();
            SettingSaveData data = gson.fromJson(json, SettingSaveData.class);
            String size;
            if (data != null && data.screenSize != null) {
                size = data.screenSize;
            } else {
                size = "medium";
                System.out.println("[DEBUG] screenSize fallback: data=" + data + ", data.screenSize=" + (data != null ? data.screenSize : "null"));
            }
            switch (size) {
                case "small": screenRatio = 0.8; break;
                case "large": screenRatio = 1.5; break;  // 2.0에서 1.5로 수정
                case "medium": default: screenRatio = 1.2; break;
            }
        } catch (Exception e) {
            screenRatio = 1.2;
            System.out.println("screenRatio 예외 발생: " + e.getMessage());
        }
        System.out.println("screenRatio: " + screenRatio); // 실제 값 확인

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
            btn.setFocusable(false); // 버튼이 포커스를 받지 않도록
            
            // 마우스 클릭 이벤트 처리
            final int index = i;
            btn.addActionListener(e -> handleMenuAction(index));
            
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
                    handleMenuAction(selectedIndex);
                } 

            }
        });

        //시각화
        setVisible(true);

        SwingUtilities.invokeLater(menuPanel::requestFocusInWindow);
    }
    
    // 메뉴 액션 처리 (키보드와 마우스 공통)
    private void handleMenuAction(int index) {
        // 마우스 클릭 시에도 selectedIndex 업데이트 및 하이라이트 갱신
        selectedIndex = index;
        updateMenuHighlight(menuButtons, selectedIndex);
        
        if (index == 0) {
            new GameStart(); //게임 시작
            dispose();
        } 
        else if (index == 1) { 
            // 설정 메뉴로 이동
            new SettingFrame(); //설정 창 띄우기
            dispose(); // 현재 창 닫기 (또는 setVisible(false))
        } 
        else if (index == 2) {
            //스코어보드
        } 
        else if (index == 3) {
            //게임 종료
            System.exit(0);
        }
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