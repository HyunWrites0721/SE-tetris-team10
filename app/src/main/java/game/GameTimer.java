package game;

import javax.swing.Timer;
import java.awt.event.*;

public class GameTimer {
    protected static final int Init_DELAY = 1000;  // 1s (=1000ms)
    protected Timer timer;
    private GameBoard gameBoard;
    
    public GameTimer(GameBoard gameBoard){
        this.gameBoard = gameBoard;
        timer = new Timer(Init_DELAY,new ActionListener() {   // 타이머 이벤트가 1초마다 발생함을 정의
            @Override
            public void actionPerformed(ActionEvent e){
                if (gameBoard.getCurrentBlock() != null){  // 현재 블록이 있는지 확인
                    if(gameBoard.getCurrentBlock().canMoveDown(gameBoard.getBoard()))   // 아래로 이동 할 수 있는지 검사. 현재 게임판을 검사함으로 써 블록과 board의 상태를 검사.
                        gameBoard.getCurrentBlock().moveDown(gameBoard.getBoard());
                    else {
                        gameBoard.placePiece();
                        gameBoard.spawnNewBlock();
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
