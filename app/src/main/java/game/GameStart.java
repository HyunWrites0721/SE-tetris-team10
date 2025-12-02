package game;

import settings.SettingModel;

public class GameStart {

    public GameStart(boolean itemMode){
        // 설정 파일에서 난이도 읽기
        SettingModel settingModel = new SettingModel();
        String difficultyStr = settingModel.getDifficulty();
        
        // 문자열을 숫자로 변환
        int difficulty = 1; // 기본값: normal
        switch (difficultyStr.toLowerCase()) {
            case "easy":
                difficulty = 2;
                break;
            case "normal":
                difficulty = 0;
                break;
            case "hard":
                difficulty = 1;
                break;
            default:
                difficulty = 0; // 기본값 normal
                break;
        }
        
        System.out.println("[GameStart] 설정 파일에서 읽은 난이도: " + difficultyStr + " (" + difficulty + ")");
        
        FrameBoard frame = new FrameBoard(itemMode, difficulty);
        frame.setVisible(true);

        // GameController를 통해 게임 시작
        frame.getGameController().start();
    }

    // EDT에서 호출하기 위한 정적 헬퍼 메서드 (기본값: 아이템 모드 OFF)
    public static void start() {
        new GameStart(false);
    }

}