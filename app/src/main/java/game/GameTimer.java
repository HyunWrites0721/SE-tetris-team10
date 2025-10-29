package game;

import javax.swing.Timer;

import java.awt.event.*;
import blocks.item.WeightBlock;

public class GameTimer {
    protected static final int Init_DELAY = 1000;  // 1s (=1000ms)
    protected Timer timer;
    private GameView gameBoard;
    private GameModel blockText;
    private FrameBoard frameBoard;
    private boolean isRunning = false;

    public GameTimer(GameView gameBoard, GameModel blockText, FrameBoard frameBoard){
        this.gameBoard = gameBoard;
        this.blockText = blockText;
        this.frameBoard = frameBoard;
        timer = new Timer(Init_DELAY,new ActionListener() {   // 타이머 이벤트가 1초마다 발생함을 정의
            @Override
            public void actionPerformed(ActionEvent e){
                // 타이머가 정지되었거나 일시정지 중이거나, WeightBlock/라인클리어 애니메이션 중이면 아무 것도 하지 않음
                if (!isRunning 
                    || (frameBoard != null && frameBoard.isPaused) 
                    || (blockText != null && (blockText.isWeightAnimating() 
                        || blockText.isLineClearAnimating()
                        || blockText.isAllClearAnimating()
                        || blockText.isBoxClearAnimating()))) {
                    return;
                }
                if (blockText.getCurrentBlock() != null){  // 현재 블록이 있는지 확인
                    if(blockText.getCurrentBlock().canMoveDown(blockText.getBoard())) {  // 아래로 이동 할 수 있는지 검사. 현재 게임판을 검사함으로 써 블록과 board의 상태를 검사.
                        blockText.getCurrentBlock().moveDown(blockText.getBoard()); //떨어지기
                        gameBoard.setFallingBlock(blockText.getCurrentBlock());
                    } else {
                        // WeightBlock이면 특수 낙하 처리 (바닥까지 뚫고 내려가며 지움, 바닥에서 소멸)
                        boolean wasWeight = (blockText.getCurrentBlock() instanceof WeightBlock);
                        if (wasWeight) {
                            blockText.applyWeightEffectAndDespawn();
                        } else {
                            blockText.placePiece(); // 일반 블록 쌓기 (내부에서 아이템/라인클리어 처리)
                        }
                        //게임오버인지 확인
                        if(blockText.isGameOver()){
                            if (frameBoard != null) {
                                frameBoard.gameOver();
                            }
                            //게임오버 로직 구현 필요
                            return;
                        }
                        // WeightBlock은 내부에서 이미 스폰했으므로 여기서 스폰하지 않음
                        if (!wasWeight) {
                            blockText.spawnNewBlock();
                        }
                        gameBoard.setFallingBlock(blockText.getCurrentBlock());
                    }
                    gameBoard.repaint();
                }
            }
        });
    }
    public void start(){
        isRunning = true;
        timer.start();
    }
    public void stop(){
        isRunning = false;
        timer.stop();
    }
}