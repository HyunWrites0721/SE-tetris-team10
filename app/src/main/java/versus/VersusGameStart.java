package versus;

import settings.SettingModel;

/**
 * 대전 모드 게임 시작 클래스
 * GameStart와 유사하지만 2인용
 */
public class VersusGameStart {
    private VersusMode mode;
    private int difficulty;

    public VersusGameStart(VersusMode mode) {
        this.mode = mode;
        
        // 난이도 설정 가져오기
        SettingModel settingModel = new SettingModel();
        String difficultyStr = settingModel.getDifficulty();
        
        // 난이도 문자열을 int로 변환 (0: normal, 1: hard, 2: easy)
        int difficulty = 0;
        switch (difficultyStr) {
            case "normal": difficulty = 0; break;
            case "hard": difficulty = 1; break;
            case "easy": difficulty = 2; break;
            default: difficulty = 0; break;
        }
        
        // 대전 게임 보드 생성
        new VersusFrameBoard(mode, difficulty);
    }
}
