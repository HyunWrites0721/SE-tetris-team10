package game;

public class GameStart {

    public GameStart(){
        FrameBoard frame = new FrameBoard();
        frame.setVisible(true);

        // 프레임이 소유한 타이머만 시작 (중복 생성/시작 방지)
        frame.getGameTimer().start();
        // 게임 오버 체크는 타이머 내부 또는 별도 이벤트에서 처리 필요
    }

    // EDT에서 호출하기 위한 정적 헬퍼 메서드
    public static void start() {
        new GameStart();
    }

}