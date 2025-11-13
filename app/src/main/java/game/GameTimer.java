package game;

import javax.swing.Timer;
import java.awt.event.*;
import blocks.item.WeightBlock;
import game.loop.GameLoop;
import game.loop.LocalGameLoop;
import game.events.EventBus;
import game.events.TickEvent;
import game.events.EventListener;

public class GameTimer {
    protected static final int Init_DELAY = 1000;  // 1s (=1000ms)
    // 속도 레벨별 딜레이 배열 (0~6레벨, 7단계)
    private static final int[][] SPEED_DELAYS = {{1000, 800, 650, 500, 350, 200, 100},     // 노멀 모드 speed delays
                                                {800, 640, 520, 400, 280, 160, 80},        // 하드 모드 speed delays
                                                {1200, 960, 780, 600, 420, 250, 120}};     // 이지 모드 speed delays
    // 난이도별 점수 가중치 배열 (0: normal=1.0, 1: hard=1.1, 2: easy=0.9)
    private static final double[] DIFFICULTY_MULTIPLIERS = {1.0, 1.1, 0.9};
    
    // 새로운 아키텍처 - GameLoop 사용
    private final GameLoop gameLoop;
    private final EventBus eventBus;
    
    // 기존 필드들 (호환성 유지)
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
        
        // SettingSave.json에서 난이도 설정 읽어오기
        loadDifficultyFromSettings();
        
        // 이벤트 시스템 초기화
        this.eventBus = new EventBus();
        this.gameLoop = new LocalGameLoop(eventBus, difficulty);
        
        // GameModel에 타이머 참조 설정
        blockText.setGameTimer(this);
        
        // 틱 이벤트 리스너 등록
        setupTickEventListener();
        
        // 기존 호환성을 위한 Swing Timer 설정 (아직 사용됨)
        setupLegacyTimer();
    }
    
    /**
     * 틱 이벤트 리스너 설정
     */
    private void setupTickEventListener() {
        eventBus.subscribe(TickEvent.class, new EventListener<TickEvent>() {
            @Override
            public void onEvent(TickEvent event) {
                handleGameTick(event);
            }
        }, 0); // 최고 우선순위로 처리
    }
    
    /**
     * 게임 틱 처리 로직 (이벤트 기반)
     */
    private void handleGameTick(TickEvent event) {
        // 타이머가 정지되었거나 일시정지 중이거나, WeightBlock/라인클리어 애니메이션 중이면 아무 것도 하지 않음
        if (!isRunning 
            || (frameBoard != null && frameBoard.isPaused) 
            || (blockText != null && (blockText.isWeightAnimating() 
                || blockText.isLineClearAnimating()
                || blockText.isAllClearAnimating()
                || blockText.isBoxClearAnimating()))) {
            return;
        }
        
        processGameLogic(event);
    }
    
    /**
     * 게임 로직 처리 (기존 코드에서 분리)
     */
    private void processGameLogic(TickEvent event) {
        if (blockText.getCurrentBlock() != null){  // 현재 블록이 있는지 확인
            if(blockText.getCurrentBlock().canMoveDown(blockText.getBoard())) {  // 아래로 이동 할 수 있는지 검사. 현재 게임판을 검사함으로 써 블록과 board의 상태를 검사.
                blockText.getCurrentBlock().moveDown(blockText.getBoard()); //떨어지기
                // 자동 낙하 점수에 속도 배율과 난이도 가중치 적용
                int speedMultiplier = blockText.getCurrentSpeedLevel() + 1;
                double difficultyMultiplier = (difficulty >= 0 && difficulty < DIFFICULTY_MULTIPLIERS.length) 
                    ? DIFFICULTY_MULTIPLIERS[difficulty] : DIFFICULTY_MULTIPLIERS[0];
                int autoDropScore = (int) Math.round(1 * speedMultiplier * difficultyMultiplier);
                frameBoard.increaseScore(autoDropScore); // 자동으로 떨어질 때마다 점수 * 속도 배율 * 난이도 가중치
                gameBoard.setFallingBlock(blockText.getCurrentBlock());
                blockText.syncToState(); // GameState 동기화 및 렌더링
            } else {
                handleBlockLanding();
            }
        }
    }
    
    /**
     * 블록 착지 처리
     */
    private void handleBlockLanding() {
        // WeightBlock이면 특수 낙하 처리 (바닥까지 뚫고 내려가며 지움, 바닥에서 소멸)
        boolean wasWeight = (blockText.getCurrentBlock() instanceof WeightBlock);  // 현재 떨어지고 있는 블록이 weigthblock 타입인지 확인
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
        blockText.syncToState(); // GameState 동기화 및 렌더링
    }
    
    /**
     * 기존 호환성을 위한 레거시 타이머 (추후 제거 예정)
     */
    private void setupLegacyTimer() {
        // 초기 딜레이를 difficulty에 맞는 첫 번째 속도로 설정
        int initialDelay = SPEED_DELAYS[difficulty][0];
        timer = new Timer(initialDelay, new ActionListener() {   // 타이머 이벤트가 difficulty에 맞는 속도로 발생
            @Override
            public void actionPerformed(ActionEvent e){
                // 이제 GameLoop에서 처리하므로 비어둠 (호환성 유지용)
            }
        });
    }
    public void start(){
        // 이미 실행 중이면 시작하지 않음 (중복 방지)
        if (isRunning || gameLoop.isRunning()) {
            System.out.println("Timer already running - ignored start()");
            return;
        }
        System.out.println("Timer started");
        isRunning = true;
        
        // 새로운 GameLoop 시작
        gameLoop.start();
        
        // 기존 타이머도 호환성을 위해 유지 (추후 제거 예정)
        timer.start();
    }
    
    public void stop(){
        System.out.println("Timer stopped");
        isRunning = false;
        
        // GameLoop 정지
        gameLoop.stop();
        
        // 기존 타이머 정지
        if (timer.isRunning()) {
            timer.stop();
        }
    }
    
    // 타이머 실행 상태 확인 메서드
    public boolean isRunning() {
        return isRunning && gameLoop.isRunning();
    }
    
    // 속도 업데이트 메서드
    public void updateSpeed(int speedLevel) {
        if (speedLevel != currentSpeedLevel && speedLevel >= 0 && speedLevel < SPEED_DELAYS[difficulty].length) {
            currentSpeedLevel = speedLevel;
            
            // GameLoop이 LocalGameLoop인 경우 속도 업데이트
            if (gameLoop instanceof LocalGameLoop) {
                LocalGameLoop localLoop = (LocalGameLoop) gameLoop;
                localLoop.updateSpeedLevel(speedLevel);
            }
            
            // 기존 타이머도 업데이트 (호환성)
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
    
    // SettingSave.json에서 난이도 설정을 읽어오는 메서드
    private void loadDifficultyFromSettings() {
        try {
            // 설정 파일 경로 가져오기 (ConfigManager 사용)
            String settingPath = settings.ConfigManager.getSettingsPath();
            
            // JSON 파일 읽기
            String json = java.nio.file.Files.readString(java.nio.file.Paths.get(settingPath));
            com.google.gson.Gson gson = new com.google.gson.Gson();
            
            // JSON을 Map으로 파싱
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> settings = gson.fromJson(json, java.util.Map.class);
            String difficultyStr = (String) settings.get("difficulty");
            
            // 난이도 문자열을 정수로 변환
            if (difficultyStr != null) {
                switch (difficultyStr.toLowerCase()) {
                    case "normal":
                        difficulty = 0;
                        break;
                    case "hard":
                        difficulty = 1;
                        break;
                    case "easy":
                        difficulty = 2;
                        break;
                    default:
                        difficulty = 0; // 기본값: 노멀
                        break;
                }
            } else {
                difficulty = 0; // 기본값: 노멀
            }
            
            System.out.println("Difficulty loaded from settings: " + difficultyStr + " (index: " + difficulty + ")");
            
        } catch (Exception e) {
            System.err.println("난이도 설정 로드 실패: " + e.getMessage());
            difficulty = 0; // 기본값: 노멀
        }
    }
}