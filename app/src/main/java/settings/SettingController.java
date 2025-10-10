package settings;
import java.awt.event.*;

public class SettingController implements ActionListener {
    private SettingModel model;
    private SettingView view;

    public SettingController(SettingModel model, SettingView view) {
        this.model = model;
        this.view = view;

        // 모든 버튼에 ActionListener 연결 (case별로 분기)
        if (view.checkButton != null) {
            view.checkButton.addActionListener(this);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        String menu = view.getSettingName();
        Object src = e.getSource();
        // 설정값 초기화
        if ("설정값 초기화".equals(menu) && src == view.checkButton) {
            model.resetSettings();
            // 필요시: UI 갱신, 알림 등 추가 가능
        }
        // 색맹 모드
        else if ("색맹 모드".equals(menu)) {
            if (src == view.Button1) {
                model.setColorBlindMode(false);
                model.SaveColorBlindMode();
            } else if (src == view.Button2) {
                model.setColorBlindMode(true);
                model.SaveColorBlindMode();
            }
        }
        // 화면 크기 설정
        else if ("화면 크기 설정".equals(menu)) {
            if (src == view.Button1) {
                model.setScreenSize("small");
                model.SaveScreenSize();
            } else if (src == view.Button2) {
                model.setScreenSize("medium");
                model.SaveScreenSize();
            } else if (src == view.Button3) {
                model.setScreenSize("large");
                model.SaveScreenSize();
            }
        }
        // 조작키 설정
        else if ("조작키 설정".equals(menu)) {
            if (src == view.Button1) {
                model.setControlType("arrow");
                model.SaveControlType();
            } else if (src == view.Button2) {
                model.setControlType("wasd");
                model.SaveControlType();
            }
        }
        // 스코어보드 초기화 등 추가 가능
    }


}
