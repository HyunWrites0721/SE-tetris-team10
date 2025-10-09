package settings;

import com.google.gson.Gson;

public class SettingModel {
    private boolean colorBlindMode;
    private String controlType = "arrow";
    private String screenSize = "medium";

    public SettingModel() {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("app/src/main/java/settings/SettingSave.json");
            String json = java.nio.file.Files.readString(path);
            Gson gson = new Gson();
            SettingSaveData data = gson.fromJson(json, SettingSaveData.class);
            if (data.colorBlindMode != null) {
                this.colorBlindMode = data.colorBlindMode;
            } else {
                this.colorBlindMode = false;
            }
            if (data.controlType != null) {
                this.controlType = data.controlType;
            }
            if (data.screenSize != null) {
                this.screenSize = data.screenSize;
            }
        } catch (Exception e) {
            this.colorBlindMode = false;
            this.controlType = "arrow";
            this.screenSize = "medium";
        }
    }

    // 내부 클래스: JSON 구조와 매핑
    private static class SettingSaveData {
        Boolean colorBlindMode;
        String controlType;
        String screenSize;
    }

    // 색맹 모드
    // getter, setter
    public void setColorBlindMode(boolean mode) { this.colorBlindMode = mode; }
    public boolean isColorBlindMode() { return colorBlindMode; }

    // 컨트롤 타입
    // getter, setter
    public void setControlType(String type) { this.controlType = type; }
    public String getControlType() { return controlType; }

    // 화면 크기
    // getter, setter
    public void setScreenSize(String size) { this.screenSize = size; }
    public String getScreenSize() { return screenSize; }
}
