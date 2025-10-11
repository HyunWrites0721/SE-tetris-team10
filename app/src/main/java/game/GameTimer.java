package game;

import javax.swing.Timer;

import java.awt.event.*;

public class GameTimer {
    protected static final int Init_DELAY = 1000;  // 1s (=1000ms)
    protected Timer timer;
    private GameView gameBoard;
    private GameModel blockText;
    private FrameBoard frameBoard;

    public GameTimer(GameView gameBoard, GameModel blockText, FrameBoard frameBoard){
        this.gameBoard = gameBoard;
        this.blockText = blockText;
        this.frameBoard = frameBoard;
        timer = new Timer(Init_DELAY,new ActionListener() {   // 타이머 이벤트가 1초마다 발생함을 정의
            @Override
            public void actionPerformed(ActionEvent e){
                // 일시정지 중이면 아무 것도 하지 않음
                if (frameBoard != null && frameBoard.isPaused) {
                    return;
                }
                if (blockText.getCurrentBlock() != null){  // 현재 블록이 있는지 확인
                    if(blockText.getCurrentBlock().canMoveDown(blockText.getBoard())) {  // 아래로 이동 할 수 있는지 검사. 현재 게임판을 검사함으로 써 블록과 board의 상태를 검사.
                        blockText.getCurrentBlock().moveDown(blockText.getBoard()); //떨어지기
                        gameBoard.setFallingBlock(blockText.getCurrentBlock());
                    } else {
                        blockText.placePiece(); //쌓이기
                        blockText.lineClear(); // 라인 클리어가 있었다면 GameModel에서 리페인트 요청함
                        //게임오버인지 확인
                        if(blockText.isGameOver()){
                            frameBoard.gameOver();
                            //게임오버 로직 구현 필요
                            return;
                        }
                        blockText.spawnNewBlock();
                        gameBoard.setFallingBlock(blockText.getCurrentBlock());
                    }
                    gameBoard.repaint();
                }
            }
        });
    }
    public void start(){
        timer.start();
    }
    public void stop(){
        timer.stop();
    }
}