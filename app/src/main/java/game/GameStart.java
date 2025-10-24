package game;

public class GameStart {

    public GameStart(){
        FrameBoard frame = new FrameBoard();
        frame.setVisible(true);

        // 게임 타이머 생성 및 시작
        GameTimer timer = new GameTimer(frame.getGameBoard(), frame.getGameModel(), frame);
        timer.start();
        // 게임 오버 체크는 타이머 내부 또는 별도 이벤트에서 처리 필요
    }
    
    
}