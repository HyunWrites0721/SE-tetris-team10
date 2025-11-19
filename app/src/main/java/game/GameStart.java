package game;

public class GameStart {

    public GameStart(boolean itemMode){
        FrameBoard frame = new FrameBoard(itemMode);
        frame.setVisible(true);

        // GameController를 통해 게임 시작
        frame.getGameController().start();
    }

    // EDT에서 호출하기 위한 정적 헬퍼 메서드 (기본값: 아이템 모드 OFF)
    public static void start() {
        new GameStart(false);
    }

}