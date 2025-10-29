package game;

import javax.swing.Timer;

import java.awt.event.*;
import blocks.item.WeightBlock;

public class GameTimer {
    protected static final int Init_DELAY = 1000;  // 1s (=1000ms)
    // 속도 레벨별 딜레이 배열 (0~5레벨, 6단계)
    private static final int[][] SPEED_DELAYS = {{1000, 800, 650, 500, 350, 200, 100},     // 노멀 모드 speed delays
                                                {800, 640, 520, 400, 280, 160, 80},        // 하드 모드 speed delays
                                                {1200, 960, 780, 600, 420, 250, 120}};     // 이지 모드 speed delays
    protected Timer timer;
    private GameView gameBoard;
    private GameModel blockText;
    private FrameBoard frameBoard;
    private boolean isRunning = false;
    private int currentSpeedLevel = 0;  // 현재 속도 레벨
    public int difficulty = 0; // 0: 노멀, 1: 하드, 2: 이지

    public GameTimer(GameView gameBoard, GameModel blockText, FrameBoard frameBoard){
        System.out.println("NEW GameTimer created!");  // 새 타이머 생성 로그
        this.gameBoard = gameBoard;
        this.blockText = blockText;
        this.frameBoard = frameBoard;
        
        // GameModel에 타이머 참조 설정
        blockText.setGameTimer(this);
        
        // 초기 딜레이를 difficulty에 맞는 첫 번째 속도로 설정
        int initialDelay = SPEED_DELAYS[difficulty][0];
        timer = new Timer(initialDelay, new ActionListener() {   // 타이머 이벤트가 difficulty에 맞는 속도로 발생
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
                        // 자동 낙하 점수에 속도 배율 적용
                        int speedMultiplier = blockText.getCurrentSpeedLevel() + 1;
                        frameBoard.increaseScore(1 * speedMultiplier); // 자동으로 떨어질 때마다 점수 * 속도 배율
                        gameBoard.setFallingBlock(blockText.getCurrentBlock());
                    } else {
                        // WeightBlock이면 특수 낙하 처리 (바닥까지 뚫고 내려가며 지움, 바닥에서 소멸)
                        boolean wasWeight = (blockText.getCurrentBlock() instanceof WeightBlock);
                        if (wasWeight) {
                            blockText.applyWeightEffectAndDespawn();
                        } else {
                            int lineClearScore = blockText.placePiece(); // 일반 블록 쌓기 (내부에서 아이템/라인클리어 처리) 및 라인 클리어 점수 받기
                            frameBoard.increaseScore(lineClearScore); // 라인 클리어 점수 추가
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
        // 이미 실행 중이면 시작하지 않음 (중복 방지)
        if (isRunning || timer.isRunning()) {
            System.out.println("Timer already running - ignored start()");
            return;
        }
        System.out.println("Timer started");
        isRunning = true;
        timer.start();
    }
    
    public void stop(){
        System.out.println("Timer stopped");
        isRunning = false;
        if (timer.isRunning()) {
            timer.stop();
        }
    }
    
    // 타이머 실행 상태 확인 메서드
    public boolean isRunning() {
        return isRunning && timer.isRunning();
    }
    
    // 속도 업데이트 메서드
    public void updateSpeed(int speedLevel) {
        if (speedLevel != currentSpeedLevel && speedLevel >= 0 && speedLevel < SPEED_DELAYS[difficulty].length) {
            currentSpeedLevel = speedLevel;
            int newDelay = SPEED_DELAYS[difficulty][speedLevel];
            
            System.out.println("Speed increased! Level: " + speedLevel + ", Delay: " + newDelay + "ms");
            
            // 타이머가 실행 중이면 새로운 속도로 재시작
            if (isRunning) {
                timer.stop();
                timer.setDelay(newDelay);
                timer.start();
            } else {
                // 실행 중이 아니면 딜레이만 변경
                timer.setDelay(newDelay);
            }
        }
    }
}