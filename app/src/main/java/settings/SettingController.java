package settings;
import java.awt.event.*;
import javax.swing.SwingUtilities;

public class SettingController implements ActionListener {
    private SettingModel model;
    private SettingView view;
    private Runnable onCancelCallback; // 취소 시 호출할 콜백
    private Runnable onConfirmCallback; // 확인 시 호출할 콜백

    public SettingController(SettingModel model, SettingView view) {
        this.model = model;
        this.view = view;

        // 모든 버튼에 ActionListener 연결 (case별로 분기)
        if (view.checkButton != null) {
            view.checkButton.addActionListener(this);
            // 확인 버튼 클릭 후 패널로 포커스 이동
            view.checkButton.addActionListener(e -> {
                if (onConfirmCallback != null) {
                    onConfirmCallback.run();
                }
            });
        }
        if (view.cancelButton != null) {
            view.cancelButton.addActionListener(this);
            // 취소 버튼 클릭 후 패널로 포커스 이동
            view.cancelButton.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> view.requestFocusInWindow());
            });
        }
        if (view.Button1 != null) {
            view.Button1.addActionListener(this);
        }
        if (view.Button2 != null) {
            view.Button2.addActionListener(this);
        }
        if (view.Button3 != null) {
            view.Button3.addActionListener(this);
        }
    }
    
    // 취소 콜백 설정
    public void setOnCancelCallback(Runnable callback) {
        this.onCancelCallback = callback;
    }
    
    // 확인 콜백 설정
    public void setOnConfirmCallback(Runnable callback) {
        this.onConfirmCallback = callback;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String menu = view.getSettingName();
        Object src = e.getSource();
        
        // 취소 버튼 처리
        if (src == view.cancelButton) {
            // 테두리 제거
            view.checkButton.setBorder(javax.swing.UIManager.getBorder("Button.border"));
            view.cancelButton.setBorder(javax.swing.UIManager.getBorder("Button.border"));
            
            view.resetFocus();
            if (onCancelCallback != null) {
                onCancelCallback.run();
            }
            return;
        }
        
        // 확인 버튼 처리 - 선택된 라디오 버튼 확인 후 저장
        if (src == view.checkButton) {
            // 테두리 제거
            view.checkButton.setBorder(javax.swing.UIManager.getBorder("Button.border"));
            if (view.cancelButton != null) {
                view.cancelButton.setBorder(javax.swing.UIManager.getBorder("Button.border"));
            }
            
            // 설정값 초기화
            if ("설정값 초기화".equals(menu)) {
                model.resetSettings();
            }
            // 스코어보드 초기화
            else if ("스코어보드 초기화".equals(menu)) {
                // 아직 미구현
            }
            // 색맹 모드 - 선택된 라디오 버튼 확인 후 저장
            else if ("색맹 모드".equals(menu)) {
                if (view.Button1 != null && view.Button1.isSelected()) {
                    model.setColorBlindMode(false);
                    model.SaveColorBlindMode();
                } else if (view.Button2 != null && view.Button2.isSelected()) {
                    model.setColorBlindMode(true);
                    model.SaveColorBlindMode();
                }
            }
            // 화면 크기 설정 - 선택된 라디오 버튼 확인 후 저장
            else if ("화면 크기 설정".equals(menu)) {
                if (view.Button1 != null && view.Button1.isSelected()) {
                    model.setScreenSize("small");
                    model.SaveScreenSize();
                } else if (view.Button2 != null && view.Button2.isSelected()) {
                    model.setScreenSize("medium");
                    model.SaveScreenSize();
                } else if (view.Button3 != null && view.Button3.isSelected()) {
                    model.setScreenSize("large");
                    model.SaveScreenSize();
                }
            }
            // 조작키 설정 - 선택된 라디오 버튼 확인 후 저장
            else if ("조작키 설정".equals(menu)) {
                if (view.Button1 != null && view.Button1.isSelected()) {
                    model.setControlType("arrow");
                    model.SaveControlType();
                } else if (view.Button2 != null && view.Button2.isSelected()) {
                    model.setControlType("wasd");
                    model.SaveControlType();
                }
            }
            // 난이도 설정 - 선택된 라디오 버튼 확인 후 저장
            else if ("난이도 설정".equals(menu)) {
                if (view.Button1 != null && view.Button1.isSelected()) {
                    model.setDifficulty("easy");
                    model.SaveDifficulty();
                } else if (view.Button2 != null && view.Button2.isSelected()) {
                    model.setDifficulty("normal");
                    model.SaveDifficulty();
                } else if (view.Button3 != null && view.Button3.isSelected()) {
                    model.setDifficulty("hard");
                    model.SaveDifficulty();
                }
            }
        }
    }


}
