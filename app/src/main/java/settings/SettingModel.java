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
            
            //색맹 모드
            if (data.colorBlindMode != null) {
                this.colorBlindMode = data.colorBlindMode;
            } else {
                this.colorBlindMode = false;
            }

            //조작키
            if (data.controlType != null) {
                this.controlType = data.controlType;
            } else {
                this.controlType = "arrow";
            }   
            
            // 화면크기
            if (data.screenSize != null) {
                this.screenSize = data.screenSize;
            } else {
                this.screenSize = "medium";
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
    // setter, getter, Save
    public void setColorBlindMode(boolean mode) { this.colorBlindMode = mode; }
    public boolean isColorBlindMode() { return colorBlindMode; }
    public void SaveColorBlindMode(){
        //
        // SettingSave.json 파일에 colorBlindMode 값 실제로 저장함. 
        //
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("app/src/main/java/settings/data/SettingSave.json");
            String json = java.nio.file.Files.readString(path);
            Gson gson = new Gson();
            SettingSaveData data = gson.fromJson(json, SettingSaveData.class);
            // colorBlindMode가 null이 아니면 파일에 덮어씀
            if (this.colorBlindMode != false && this.colorBlindMode != true) {
                // null인 경우 아무것도 하지 않음
                return;
            }
            data.colorBlindMode = this.colorBlindMode;
            String newJson = gson.toJson(data);
            java.nio.file.Files.writeString(path, newJson);
        } catch (Exception e) {
            // 파일 접근/파싱 오류 시 무시
        }
    }

    // 화면 크기
    // getter, setter
    public void setScreenSize(String size) { this.screenSize = size; }
    public String getScreenSize() { return screenSize; }
    public void SaveScreenSize(){
        //
        // SettingSave.json 파일에 screenSize 값 실제로 저장함. 
        //
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("app/src/main/java/settings/data/SettingSave.json");
            String json = java.nio.file.Files.readString(path);
            Gson gson = new Gson();
            SettingSaveData data = gson.fromJson(json, SettingSaveData.class);
            // screenSize가 null이 아니면 파일에 덮어씀
            if (this.screenSize != "small" && this.screenSize != "medium" && this.screenSize != "large") {
                // null인 경우 아무것도 하지 않음
                return;
            }
            data.screenSize = this.screenSize;
            String newJson = gson.toJson(data);
            java.nio.file.Files.writeString(path, newJson);
        } catch (Exception e) {
            // 파일 접근/파싱 오류 시 무시
        }
    }

    // 조작키 설정
    // getter, setter
    public void setControlType(String type) { this.controlType = type; }
    public String getControlType() { return controlType; }
    public void SaveControlType(){
        //
        // SettingSave.json 파일에 controlType 값 실제로 저장함. 
        //
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("app/src/main/java/settings/data/SettingSave.json");
            String json = java.nio.file.Files.readString(path);
            Gson gson = new Gson();
            SettingSaveData data = gson.fromJson(json, SettingSaveData.class);
            // controlType이 null이 아니면 파일에 덮어씀
            if (this.controlType != "arrow" && this.controlType != "wasd") {
                // null인 경우 아무것도 하지 않음
                return;
            }
            data.controlType = this.controlType;
            String newJson = gson.toJson(data);
            java.nio.file.Files.writeString(path, newJson);
        } catch (Exception e) {
            // 파일 접근/파싱 오류 시 무시
        }
    }

    // 스코어보드 초기화
    // 아직 미구현
    //
    //
    //

    //설정 초기화 -> DefaultSetting.json 파일로 덮어씀
    public void resetSettings() {
        try {
            java.nio.file.Path defaultPath = java.nio.file.Paths.get("app/src/main/java/settings/data/DefaultSetting.json");
            java.nio.file.Path savePath = java.nio.file.Paths.get("app/src/main/java/settings/data/SettingSave.json");
            String defaultJson = java.nio.file.Files.readString(defaultPath);
            java.nio.file.Files.writeString(savePath, defaultJson);
        } catch (Exception e) {
            // 파일 접근/파싱 오류 시 무시
        }
    }

}
