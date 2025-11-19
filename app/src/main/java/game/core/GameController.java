package game.core;

import blocks.Block;
import game.GameView;
import game.events.EventBus;
import game.events.TickEvent;
import game.events.EventListener;
import game.loop.GameLoop;
import game.loop.LocalGameLoop;

/**
 * 게임의 메인 컨트롤러
 * GameModel과 GameTimer의 역할을 통합하여 게임 전체를 조율
 * 
 * 책임:
 * - GameEngine (순수 로직) 관리
 * - GameState (불변 상태) 관리
 * - LocalGameLoop (타이머) 관리
 * - 점수 및 레벨 관리
 * - 이벤트 처리
 */
public class GameController {
    
    // 핵심 컴포넌트
    private final GameEngine engine;
    private GameState currentState;
    private final GameLoop gameLoop;
    private final EventBus eventBus;
    private final GameView view;
    private final BlockSpawner blockSpawner;  // 블록 생성 관리
    private final AnimationManager animationManager;  // 애니메이션 관리
    private final ItemBlockHandler itemBlockHandler;  // 아이템 블록 처리
    
    // 게임 상태
    private int score = 0;
    private boolean isPaused = false;
    private boolean isRunning = false;
    private boolean isGameOver = false;
    
    // 설정
    private final boolean itemMode;
    private final int difficulty;
    
    // 난이도별 점수 가중치
    private static final double[] DIFFICULTY_MULTIPLIERS = {1.0, 1.1, 0.9};
    
    /**
     * GameController 생성자
     * 
     * @param view 게임 뷰 (렌더링 담당)
     * @param itemMode 아이템 모드 활성화 여부
     * @param difficulty 난이도 (0: normal, 1: hard, 2: easy)
     */
    public GameController(GameView view, boolean itemMode, int difficulty) {
        this.view = view;
        this.itemMode = itemMode;
        this.difficulty = difficulty;
        
        // 이벤트 시스템 초기화
        this.eventBus = new EventBus();
        
        // GameEngine 생성
        this.engine = new GameEngine(difficulty, eventBus);
        
        // LocalGameLoop 생성 (타이머 역할)
        this.gameLoop = new LocalGameLoop(eventBus, difficulty);
        
        // 초기 상태 생성
        this.currentState = createInitialState();
        
        // BlockSpawner 생성
        this.blockSpawner = new BlockSpawner(itemMode, view);
        
        // AnimationManager 생성
        this.animationManager = new AnimationManager(view);
        
        // ItemBlockHandler 생성
        this.itemBlockHandler = new ItemBlockHandler(view, animationManager);
        
        // 이벤트 리스너 등록
        setupEventListeners();
        
        // 초기 렌더링
        view.render(currentState);
    }
    
    /**
     * 초기 게임 상태 생성
     */
    private GameState createInitialState() {
        int ROWS = 23;
        int COLS = 12;
        int[][] emptyBoard = new int[ROWS][COLS];
        int[][] emptyColorBoard = new int[ROWS][COLS];
        
        // 테두리 초기화 (벽)
        for (int i = 0; i < ROWS; i++) {
            emptyBoard[i][0] = 1;  // 왼쪽 벽
            emptyBoard[i][COLS - 1] = 1;  // 오른쪽 벽
        }
        for (int j = 0; j < COLS; j++) {
            emptyBoard[0][j] = 1;  // 위쪽 벽
            emptyBoard[1][j] = 1;  // 위쪽 벽 2줄
            emptyBoard[ROWS - 1][j] = 1;  // 바닥
            emptyBoard[ROWS - 2][j] = 1;  // 바닥 2줄
        }
        
        return new GameState.Builder(emptyBoard, emptyColorBoard, null, null, itemMode)
            .score(0)
            .totalLinesCleared(0)
            .currentLevel(1)
            .lineClearCount(0)
            .itemGenerateCount(0)
            .blocksSpawned(0)
            .lastLineClearScore(0)
            .build();
    }
    
    /**
     * 이벤트 리스너 설정
     */
    private void setupEventListeners() {
        // TickEvent 리스너 등록 - 게임 루프의 심장
        eventBus.subscribe(TickEvent.class, new EventListener<TickEvent>() {
            @Override
            public void onEvent(TickEvent event) {
                handleTick(event);
            }
        }, 0); // 최고 우선순위
    }
    
    /**
     * 게임 틱 처리 (타이머가 호출)
     * GameTimer.handleGameTick()의 역할을 대체
     */
    private void handleTick(TickEvent event) {
        // 일시정지 중이면 무시
        if (isPaused || !isRunning) {
            return;
        }
        
        // 애니메이션 중이면 무시 (AnimationManager에서 체크)
        if (animationManager.isAnimating()) {
            return;
        }
        
        // 게임 로직 실행
        processGameTick(event);
    }
    
    /**
     * 실제 게임 로직 처리
     * GameTimer.processGameLogic()의 역할을 대체
     */
    private void processGameTick(TickEvent event) {
        Block currentBlock = currentState.getCurrentBlock();
        
        // 현재 블록이 없으면 새 블록 생성
        if (currentBlock == null) {
            spawnNewBlock();
            return;
        }
        
        int[][] board = currentState.getBoardArray();
        
        // 블록이 아래로 이동 가능한지 확인
        if (currentBlock.canMoveDown(board)) {
            // 블록 이동
            currentBlock.moveDown(board);
            
            // 자동 낙하 점수 추가
            int autoDropScore = engine.calculateAutoDropScore(event.getSpeedLevel());
            score += autoDropScore;
            
            // 뷰 업데이트
            view.setScore(score);
            view.setFallingBlock(currentBlock);
            view.render(currentState);
        } else {
            // 블록을 고정할 수 없으면 착지 처리
            handleBlockLanding();
        }
    }
    
    /**
     * 블록 착지 처리
     */
    private void handleBlockLanding() {
        Block currentBlock = currentState.getCurrentBlock();
        if (currentBlock == null) return;
        
        int[][] board = currentState.getBoardArray();
        int[][] colorBoard = currentState.getColorBoard();
        
        // 블록을 보드에 고정
        int specialType = engine.placeBlock(currentBlock, board, colorBoard);
        
        // 특수 블록 처리 (ItemBlockHandler에 위임)
        if (specialType != 0) {
            // AllClear(2), BoxClear(3), OneLineClear(4) 처리
            GameState tempState = new GameState.Builder(
                board,
                colorBoard,
                null,
                currentState.getNextBlock(),
                currentState.isItemMode()
            )
                .score(score)
                .totalLinesCleared(currentState.getTotalLinesCleared())
                .currentLevel(currentState.getCurrentLevel())
                .lineClearCount(currentState.getLineClearCount())
                .itemGenerateCount(currentState.getItemGenerateCount())
                .blocksSpawned(currentState.getBlocksSpawned())
                .lastLineClearScore(currentState.getLastLineClearScore())
                .build();
            
            itemBlockHandler.handleSpecialBlock(specialType, tempState, (newState) -> {
                // 특수 블록 처리 완료 후
                currentState = newState;
                score = newState.getScore();
                
                // 게임 오버 체크
                if (engine.checkGameOver(newState.getBoardArray())) {
                    handleGameOver();
                    return;
                }
                
                // 새 블록 생성
                spawnNewBlock();
            });
            return;  // 애니메이션 진행 중, 콜백에서 처리
        }
        
        // 일반 블록: 라인 클리어
        int linesCleared = engine.performLineClear(board, colorBoard);
        
        if (linesCleared > 0) {
            // 점수 계산
            int lineClearScore = engine.calculateLineClearScore(linesCleared, currentState.getCurrentLevel());
            score += lineClearScore;
            
            // 상태 업데이트
            int totalLines = currentState.getTotalLinesCleared() + linesCleared;
            int newLevel = engine.calculateLevel(totalLines);
            
            currentState = new GameState.Builder(
                board,
                colorBoard,
                null,  // 새 블록 생성 전이므로 null
                currentState.getNextBlock(),
                currentState.isItemMode()
            )
                .score(score)
                .totalLinesCleared(totalLines)
                .currentLevel(newLevel)
                .lineClearCount(currentState.getLineClearCount() + linesCleared)
                .itemGenerateCount(currentState.getItemGenerateCount())
                .blocksSpawned(currentState.getBlocksSpawned())
                .build();
            
            // BlockSpawner에 라인 클리어 알림
            blockSpawner.addLineClearCount(linesCleared);
        }
        
        // 게임 오버 체크
        if (engine.checkGameOver(board)) {
            handleGameOver();
            return;
        }
        
        // 새 블록 생성
        spawnNewBlock();
    }
    
    /**
     * 새 블록 생성
     */
    private void spawnNewBlock() {
        BlockSpawner.SpawnResult result = blockSpawner.spawnNewBlock(currentState);
        currentState = result.newState;
        
        // 속도 업데이트
        updateSpeed(result.speedLevel);
        
        // 뷰 업데이트
        view.setFallingBlock(currentState.getCurrentBlock());
        view.render(currentState);
    }
    
    /**
     * 게임 오버 처리
     */
    private void handleGameOver() {
        isGameOver = true;
        stop();
        System.out.println("Game Over! Final Score: " + score);
        // TODO: 게임 오버 이벤트 발생
    }
    
    /**
     * 자동 낙하 점수 계산
     */
    private int calculateAutoDropScore(int speedLevel) {
        return engine.calculateAutoDropScore(speedLevel);
    }
    
    // ==================== 게임 제어 메소드 ====================
    
    /**
     * 게임 시작
     */
    public void start() {
        if (isRunning || gameLoop.isRunning()) {
            System.out.println("GameController already running - ignored start()");
            return;
        }
        
        System.out.println("GameController started");
        isRunning = true;
        isPaused = false;
        gameLoop.start();
    }
    
    /**
     * 게임 정지
     */
    public void stop() {
        System.out.println("GameController stopped");
        isRunning = false;
        isPaused = false;
        gameLoop.stop();
    }
    
    /**
     * 게임 일시정지
     */
    public void pause() {
        isPaused = true;
        gameLoop.pause();
    }
    
    /**
     * 게임 재개
     */
    public void resume() {
        isPaused = false;
        gameLoop.resume();
    }
    
    /**
     * 실행 중 확인
     */
    public boolean isRunning() {
        return isRunning && gameLoop.isRunning();
    }
    
    /**
     * 일시정지 확인
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    // ==================== 블록 조작 메소드 ====================
    
    /**
     * 블록을 왼쪽으로 이동
     */
    public void moveLeft() {
        if (isPaused || !isRunning) return;
        
        // TODO: GameEngine.moveLeft() 사용
        currentState = engine.moveLeft(currentState);
        view.render(currentState);
    }
    
    /**
     * 블록을 오른쪽으로 이동
     */
    public void moveRight() {
        if (isPaused || !isRunning) return;
        
        // TODO: GameEngine.moveRight() 사용
        currentState = engine.moveRight(currentState);
        view.render(currentState);
    }
    
    /**
     * 블록을 아래로 이동
     */
    public void moveDown() {
        if (isPaused || !isRunning) return;
        
        // TODO: GameEngine.moveDown() 사용
        currentState = engine.moveDown(currentState);
        view.render(currentState);
    }
    
    /**
     * 블록 회전
     */
    public void rotate() {
        if (isPaused || !isRunning) return;
        
        // TODO: GameEngine.rotate() 사용
        currentState = engine.rotate(currentState);
        view.render(currentState);
    }
    
    /**
     * 하드 드롭 (즉시 바닥까지)
     */
    public int hardDrop() {
        if (isPaused || !isRunning) return 0;
        
        // TODO: GameEngine.hardDrop() 사용하여 거리 계산
        // 현재는 임시로 0 반환
        return 0;
    }
    
    // ==================== 속도 및 레벨 관리 ====================
    
    /**
     * 게임 속도 업데이트
     */
    public void updateSpeed(int speedLevel) {
        if (gameLoop instanceof LocalGameLoop) {
            LocalGameLoop localLoop = (LocalGameLoop) gameLoop;
            localLoop.updateSpeedLevel(speedLevel);
        }
    }
    
    // ==================== 상태 접근 메소드 ====================
    
    /**
     * 현재 게임 상태 반환
     */
    public GameState getCurrentState() {
        return currentState;
    }
    
    /**
     * 현재 블록 반환
     */
    public Block getCurrentBlock() {
        return currentState.getCurrentBlock();
    }
    
    /**
     * 게임 보드 배열 반환
     */
    public int[][] getBoard() {
        return currentState.getBoardArray();
    }
    
    /**
     * 현재 점수 반환
     */
    public int getScore() {
        return score;
    }
    
    /**
     * 점수 추가
     */
    public void addScore(int points) {
        score += points;
        view.setScore(score);
    }
    
    /**
     * EventBus 반환
     */
    public EventBus getEventBus() {
        return eventBus;
    }
    
    /**
     * GameEngine 반환
     */
    public GameEngine getEngine() {
        return engine;
    }
}
