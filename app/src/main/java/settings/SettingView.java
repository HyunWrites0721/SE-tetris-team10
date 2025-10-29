package settings;
import javax.swing.*;

import start.StartFrame;

import java.awt.*;
import java.awt.event.*;

public class SettingView extends JPanel{
    
    private double screenRatio = StartFrame.screenRatio;
    private final String menuName;
    JRadioButton Button1, Button2, Button3;
    JButton checkButton;
    JButton cancelButton;
    JPanel panel;
    Color panelColor;
    private int selectedRadioIndex = 0; // 현재 선택된 라디오 버튼 인덱스
    private JRadioButton[] radioButtons; // 라디오 버튼 배열
    private boolean focusOnButtons = false; // 확인/취소 버튼에 포커스 여부 

    public SettingView(String menuName, SettingModel model){
        this.menuName = menuName;  
        setLayout(new GridBagLayout());
        
        // 외부 여백 설정 (왼쪽, 오른쪽에 마진 추가)
        setBorder(BorderFactory.createEmptyBorder(
            (int)(20*screenRatio),  // top
            (int)(30*screenRatio),  // left
            (int)(20*screenRatio),  // bottom
            (int)(30*screenRatio)   // right
        ));
        
        // GridBagLayout으로 변경하여 위치 고정
        panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension((int)(400*screenRatio), (int)(320*screenRatio)));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        
        panelColor = new Color(245, 245, 245);
        panel.setBackground(panelColor);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets((int)(10*screenRatio), (int)(10*screenRatio), (int)(10*screenRatio), (int)(10*screenRatio));
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Button1 = new JRadioButton();
        Button2 = new JRadioButton();
        Button3 = new JRadioButton();
        
        // 라디오 버튼은 focusable false로 설정 (KeyListener가 작동하도록)
        // 마우스 클릭은 ActionListener로 처리
        Button1.setFocusable(false);
        Button2.setFocusable(false);
        Button3.setFocusable(false);
        
        // 라디오 버튼 배경을 불투명하게 설정해야 테두리가 보임
        Button1.setOpaque(true);
        Button2.setOpaque(true);
        Button3.setOpaque(true);

        ButtonGroup buttonGroup = new ButtonGroup();

        // 라벨 생성
        JLabel label = new JLabel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(label, gbc);

        switch (menuName) {
            case "설정값 초기화":
                label.setText("설정을 초기화 하겠습니까?");
                checkButton = new JButton("예");
                checkButton.setFocusable(false);
                cancelButton = new JButton("취소");
                cancelButton.setFocusable(false);
                
                // 버튼들을 아래쪽에 배치
                gbc.gridy = 2;
                gbc.gridwidth = 1;
                gbc.gridx = 0;
                panel.add(checkButton, gbc);
                gbc.gridx = 1;
                panel.add(cancelButton, gbc);
                
                radioButtons = new JRadioButton[0];
                break;
            case "스코어보드 초기화":
                label.setText("스코어보드를 초기화 하겠습니까?");
                checkButton = new JButton("예");
                checkButton.setFocusable(false);
                cancelButton = new JButton("취소");
                cancelButton.setFocusable(false);
                
                // 버튼들을 아래쪽에 배치
                gbc.gridy = 2;
                gbc.gridwidth = 1;
                gbc.gridx = 0;
                panel.add(checkButton, gbc);
                gbc.gridx = 1;
                panel.add(cancelButton, gbc);
                
                radioButtons = new JRadioButton[0];
                break;
            case "화면 크기 설정":
                label.setText("화면 크기:");
                Button1.setText("작은 화면");
                Button2.setText("보통 화면");
                Button3.setText("큰 화면");
                
                // 체크박스들을 윗줄에 배치
                gbc.gridy = 1;
                gbc.gridwidth = 1;
                gbc.gridx = 0;
                panel.add(Button1, gbc);
                gbc.gridx = 1;
                panel.add(Button2, gbc);
                gbc.gridx = 2;
                panel.add(Button3, gbc);
                
                buttonGroup.add(Button1);
                buttonGroup.add(Button2);
                buttonGroup.add(Button3);
                
                // 현재 저장된 설정값에 따라 라디오 버튼 미리 선택
                String currentScreenSize = model.getScreenSize();
                if ("small".equals(currentScreenSize)) {
                    Button1.setSelected(true);
                    selectedRadioIndex = 0;
                } else if ("medium".equals(currentScreenSize)) {
                    Button2.setSelected(true);
                    selectedRadioIndex = 1;
                } else if ("large".equals(currentScreenSize)) {
                    Button3.setSelected(true);
                    selectedRadioIndex = 2;
                } else {
                    Button2.setSelected(true); // 기본값
                    selectedRadioIndex = 1;
                }
                
                checkButton = new JButton("확인");
                checkButton.setFocusable(false);
                cancelButton = new JButton("취소");
                cancelButton.setFocusable(false);
                
                // 버튼들을 아래쪽에 배치
                gbc.gridy = 2;
                gbc.gridx = 0;
                gbc.gridwidth = 1;
                panel.add(checkButton, gbc);
                gbc.gridx = 1;
                panel.add(cancelButton, gbc);
                
                radioButtons = new JRadioButton[]{Button1, Button2, Button3};
                break;
            case "색맹 모드":
                label.setText("색맹 모드: ");
                Button1.setText("끔");
                Button2.setText("켬");
                
                // 체크박스들을 윗줄에 배치
                gbc.gridy = 1;
                gbc.gridwidth = 1;
                gbc.gridx = 0;
                panel.add(Button1, gbc);
                gbc.gridx = 1;
                panel.add(Button2, gbc);
                
                buttonGroup.add(Button1);
                buttonGroup.add(Button2);
                
                // 현재 저장된 설정값에 따라 라디오 버튼 미리 선택
                boolean currentColorBlindMode = model.isColorBlindMode();
                if (currentColorBlindMode) {
                    Button2.setSelected(true);
                    selectedRadioIndex = 1;
                } else {
                    Button1.setSelected(true);
                    selectedRadioIndex = 0;
                }
                
                checkButton = new JButton("확인");
                checkButton.setFocusable(false);
                cancelButton = new JButton("취소");
                cancelButton.setFocusable(false);
                
                // 버튼들을 아래쪽에 배치
                gbc.gridy = 2;
                gbc.gridx = 0;
                panel.add(checkButton, gbc);
                gbc.gridx = 1;
                panel.add(cancelButton, gbc);
                
                radioButtons = new JRadioButton[]{Button1, Button2};
                break;
            case "조작키 설정":
                label.setText("조작키: ");
                Button1.setText("방향키");
                Button2.setText("WASD");
                
                // 체크박스들을 윗줄에 배치
                gbc.gridy = 1;
                gbc.gridwidth = 1;
                gbc.gridx = 0;
                panel.add(Button1, gbc);
                gbc.gridx = 1;
                panel.add(Button2, gbc);
                
                buttonGroup.add(Button1);
                buttonGroup.add(Button2);
                
                // 현재 저장된 설정값에 따라 라디오 버튼 미리 선택
                String currentControlType = model.getControlType();
                if ("arrow".equals(currentControlType)) {
                    Button1.setSelected(true);
                    selectedRadioIndex = 0;
                } else if ("wasd".equals(currentControlType)) {
                    Button2.setSelected(true);
                    selectedRadioIndex = 1;
                } else {
                    Button1.setSelected(true); // 기본값
                    selectedRadioIndex = 0;
                }
                
                checkButton = new JButton("확인");
                checkButton.setFocusable(false);
                cancelButton = new JButton("취소");
                cancelButton.setFocusable(false);
                
                // 버튼들을 아래쪽에 배치
                gbc.gridy = 2;
                gbc.gridx = 0;
                panel.add(checkButton, gbc);
                gbc.gridx = 1;
                panel.add(cancelButton, gbc);
                
                radioButtons = new JRadioButton[]{Button1, Button2};
                break;
            case "난이도 설정":
                label.setText("난이도: ");
                Button1.setText("쉬움");
                Button2.setText("보통");
                Button3.setText("어려움");
                
                // 체크박스들을 윗줄에 배치
                gbc.gridy = 1;
                gbc.gridwidth = 1;
                gbc.gridx = 0;
                panel.add(Button1, gbc);
                gbc.gridx = 1;
                panel.add(Button2, gbc);
                gbc.gridx = 2;
                panel.add(Button3, gbc);
                
                buttonGroup.add(Button1);
                buttonGroup.add(Button2);
                buttonGroup.add(Button3);
                
                // 현재 저장된 설정값에 따라 라디오 버튼 미리 선택
                String currentDifficulty = model.getDifficulty();
                if ("easy".equals(currentDifficulty)) {
                    Button1.setSelected(true);
                    selectedRadioIndex = 0;
                } else if ("normal".equals(currentDifficulty)) {
                    Button2.setSelected(true);
                    selectedRadioIndex = 1;
                } else if ("hard".equals(currentDifficulty)) {
                    Button3.setSelected(true);
                    selectedRadioIndex = 2;
                } else {
                    Button2.setSelected(true); // 기본값
                    selectedRadioIndex = 1;
                }
                
                checkButton = new JButton("확인");
                checkButton.setFocusable(false);
                cancelButton = new JButton("취소");
                cancelButton.setFocusable(false);
                
                // 버튼들을 아래쪽에 배치
                gbc.gridy = 2;
                gbc.gridx = 0;
                panel.add(checkButton, gbc);
                gbc.gridx = 1;
                panel.add(cancelButton, gbc);
                
                radioButtons = new JRadioButton[]{Button1, Button2, Button3};
                break;
            default:
                label.setText("Invalid Setting");
                radioButtons = new JRadioButton[0];
                break;
        }
        add(panel);
        
        // 라디오 버튼 클릭 시 확인/취소 버튼으로 포커스 이동
        for (JRadioButton radioButton : radioButtons) {
            if (radioButton != null) {
                radioButton.addActionListener(e -> {
                    // 라디오 버튼 선택 후 확인/취소 버튼으로 포커스 이동
                    SwingUtilities.invokeLater(() -> {
                        // 선택된 버튼의 인덱스 업데이트
                        for (int i = 0; i < radioButtons.length; i++) {
                            if (radioButtons[i].isSelected()) {
                                selectedRadioIndex = i;
                                break;
                            }
                        }
                        // 모든 라디오 버튼 하이라이트 및 테두리 제거
                        for (JRadioButton rb : radioButtons) {
                            rb.setBackground(panelColor);
                            rb.setBorder(UIManager.getBorder("RadioButton.border"));
                        }
                        // 확인/취소 버튼으로 포커스 전환 (배경색 없이 테두리만)
                        focusOnButtons = true;
                        checkButton.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                        requestFocusInWindow();
                    });
                });
            }
        }
        
        // KeyListener 추가 - 라디오 버튼과 확인/취소 버튼 간 포커스 이동
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                
                if (!focusOnButtons && radioButtons.length > 0) {
                    // 라디오 버튼 간 좌우 이동
                    if (key == KeyEvent.VK_LEFT) {
                        selectedRadioIndex = (selectedRadioIndex - 1 + radioButtons.length) % radioButtons.length;
                        updateRadioButtonFocus();
                    } else if (key == KeyEvent.VK_RIGHT) {
                        selectedRadioIndex = (selectedRadioIndex + 1) % radioButtons.length;
                        updateRadioButtonFocus();
                    } else if (key == KeyEvent.VK_ENTER) {
                        // 라디오 버튼 선택
                        radioButtons[selectedRadioIndex].setSelected(true);
                        // 모든 라디오 버튼 하이라이트 및 테두리 제거
                        for (JRadioButton rb : radioButtons) {
                            rb.setBackground(panelColor);
                            rb.setBorder(UIManager.getBorder("RadioButton.border"));
                        }
                        // 확인/취소 버튼으로 포커스 이동 (배경색 없이 테두리만)
                        focusOnButtons = true;
                        checkButton.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                    }
                } else if (focusOnButtons) {
                    // 확인/취소 버튼 간 좌우 이동
                    if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
                        if (checkButton.getBorder() != null && checkButton.getBorder() instanceof javax.swing.border.LineBorder) {
                            // 확인 → 취소로 전환 (배경색 없이 테두리만)
                            checkButton.setBorder(UIManager.getBorder("Button.border"));
                            cancelButton.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                        } else {
                            // 취소 → 확인으로 전환 (배경색 없이 테두리만)
                            cancelButton.setBorder(UIManager.getBorder("Button.border"));
                            checkButton.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                        }
                    } else if (key == KeyEvent.VK_ENTER) {
                        // 현재 하이라이트된 버튼 클릭
                        if (checkButton.getBorder() != null && checkButton.getBorder() instanceof javax.swing.border.LineBorder) {
                            checkButton.doClick();
                        } else if (cancelButton.getBorder() != null && cancelButton.getBorder() instanceof javax.swing.border.LineBorder) {
                            cancelButton.doClick();
                        }
                    }
                }
            }
        });
        
        // 초기 라디오 버튼 하이라이트 및 포커스 설정
        if (radioButtons.length > 0) {
            selectedRadioIndex = 0;
            updateRadioButtonFocus();
        }
        
        // 패널이 표시될 때 자동으로 포커스 요청
        SwingUtilities.invokeLater(() -> {
            requestFocusInWindow();
            setFocusable(true);
        });
    }
    
    // 라디오 버튼 포커스 업데이트
    private void updateRadioButtonFocus() {
        for (int i = 0; i < radioButtons.length; i++) {
            if (i == selectedRadioIndex) {
                // 선택된 라디오 버튼: 회색 배경 + 빨간색 테두리
                radioButtons[i].setBackground(Color.LIGHT_GRAY);
                radioButtons[i].setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            } else {
                // 선택되지 않은 라디오 버튼: 기본 배경 + 기본 테두리
                radioButtons[i].setBackground(panelColor);
                radioButtons[i].setBorder(UIManager.getBorder("RadioButton.border"));
            }
        }
        // 패널이 포커스를 유지하도록
        requestFocusInWindow();
    }
    
    // 포커스를 라디오 버튼으로 리셋 (취소 버튼용)
    public void resetFocus() {
        focusOnButtons = false;
        selectedRadioIndex = 0;
        checkButton.setBorder(UIManager.getBorder("Button.border"));
        if (cancelButton != null) {
            cancelButton.setBorder(UIManager.getBorder("Button.border"));
        }
        if (radioButtons.length > 0) {
            updateRadioButtonFocus();
        }
        requestFocusInWindow();
    }

    public String getSettingName() {
        return menuName;
    }
}