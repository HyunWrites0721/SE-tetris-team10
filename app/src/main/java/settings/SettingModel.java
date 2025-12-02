package settings;

import com.google.gson.Gson;

public class SettingModel {
    private boolean colorBlindMode;
    private String controlType = "arrow";
    private String screenSize = "medium";
    private String difficulty = "normal";

    public SettingModel() {
        try {
            // ConfigManager를 사용하여 실제 설정 파일 경로 가져오기
            String settingsPath = ConfigManager.getSettingsPath();
            String json = java.nio.file.Files.readString(java.nio.file.Paths.get(settingsPath));
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
            
            // 난이도
            if (data.difficulty != null) {
                this.difficulty = data.difficulty;
            } else {
                this.difficulty = "normal";
            }

        } catch (Exception e) {
            this.colorBlindMode = false;
            this.controlType = "arrow";
            this.screenSize = "medium";
            this.difficulty = "normal";
        }
    }

    // 내부 클래스: JSON 구조와 매핑
    private static class SettingSaveData {
        Boolean colorBlindMode;
        String controlType;
        String screenSize;
        String difficulty;
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
            String settingsPath = ConfigManager.getSettingsPath();
            String json = java.nio.file.Files.readString(java.nio.file.Paths.get(settingsPath));
            Gson gson = new Gson();
            SettingSaveData data = gson.fromJson(json, SettingSaveData.class);
            // colorBlindMode 값을 저장 (boolean이므로 항상 유효함)
            data.colorBlindMode = this.colorBlindMode;
            String newJson = gson.toJson(data);
            java.nio.file.Files.writeString(java.nio.file.Paths.get(settingsPath), newJson);
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
            String settingsPath = ConfigManager.getSettingsPath();
            String json = java.nio.file.Files.readString(java.nio.file.Paths.get(settingsPath));
            Gson gson = new Gson();
            SettingSaveData data = gson.fromJson(json, SettingSaveData.class);
            // screenSize가 유효한 값인지 확인 (equals 사용)
            if (!this.screenSize.equals("small") && !this.screenSize.equals("medium") && !this.screenSize.equals("large")) {
                // 유효하지 않은 경우 아무것도 하지 않음
                return;
            }
            data.screenSize = this.screenSize;
            String newJson = gson.toJson(data);
            java.nio.file.Files.writeString(java.nio.file.Paths.get(settingsPath), newJson);
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
            String settingsPath = ConfigManager.getSettingsPath();
            String json = java.nio.file.Files.readString(java.nio.file.Paths.get(settingsPath));
            Gson gson = new Gson();
            SettingSaveData data = gson.fromJson(json, SettingSaveData.class);
            // controlType이 유효한 값인지 확인 (equals 사용)
            if (!this.controlType.equals("arrow") && !this.controlType.equals("wasd")) {
                // 유효하지 않은 경우 아무것도 하지 않음
                return;
            }
            data.controlType = this.controlType;
            String newJson = gson.toJson(data);
            java.nio.file.Files.writeString(java.nio.file.Paths.get(settingsPath), newJson);
        } catch (Exception e) {
            // 파일 접근/파싱 오류 시 무시
        }
    }

    // 난이도 설정
    // getter, setter, save
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getDifficulty() { return difficulty; }
    public void SaveDifficulty(){
        //
        // SettingSave.json 파일에 difficulty 값 실제로 저장함. 
        //
        try {
            String settingsPath = ConfigManager.getSettingsPath();
            String json = java.nio.file.Files.readString(java.nio.file.Paths.get(settingsPath));
            Gson gson = new Gson();
            SettingSaveData data = gson.fromJson(json, SettingSaveData.class);
            // difficulty가 유효한 값이면 파일에 덮어씀
            if (!this.difficulty.equals("easy") && !this.difficulty.equals("normal") && !this.difficulty.equals("hard")) {
                // 유효하지 않은 경우 아무것도 하지 않음
                return;
            }
            data.difficulty = this.difficulty;
            String newJson = gson.toJson(data);
            java.nio.file.Files.writeString(java.nio.file.Paths.get(settingsPath), newJson);
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
            String defaultPath = ConfigManager.getDefaultSettingsPath();
            String savePath = ConfigManager.getSettingsPath();
            String defaultJson = java.nio.file.Files.readString(java.nio.file.Paths.get(defaultPath));
            java.nio.file.Files.writeString(java.nio.file.Paths.get(savePath), defaultJson);
        } catch (Exception e) {
            // 파일 접근/파싱 오류 시 무시
        }
    }

}
