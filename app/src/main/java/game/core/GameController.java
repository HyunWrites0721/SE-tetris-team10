package game.core;

import java.util.ArrayList;
import java.util.List;
import blocks.Block;
import game.GameView;
import game.events.EventBus;
import game.events.TickEvent;
import game.events.GameOverEvent;
import game.events.LineClearedEvent;
import game.events.ScoreUpdateEvent;
import game.events.BlockMovedEvent;
import game.events.BlockRotatedEvent;
import game.events.EventListener;
import game.loop.GameLoop;
import game.loop.LocalGameLoop;
import settings.HighScoreModel;

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
    private int[][] lastBlockPattern = null;  // 마지막 블록의 패턴 (대전 모드 공격용)
    private int lastBlockX = -1;  // 마지막 블록의 X 위치 (대전 모드 공격용)
    
    // 설정
    private final boolean itemMode;
    private final int difficulty;
    
    // 난이도별 점수 가중치
    private static final double[] DIFFICULTY_MULTIPLIERS = {1.0, 1.1, 0.9};
    
    /**
     * 마지막 블록 패턴 정보 반환 (대전 모드 공격용)
     * @return [0]: 블록 패턴 배열, [1][0]: 블록 X 위치
     */
    public Object[] getLastBlockInfo() {
        return new Object[] { lastBlockPattern, lastBlockX };
    }
    
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
            emptyBoard[ROWS - 1][j] = 1;  // 바닥만 1줄
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
            addScore(autoDropScore);  // ✅ addScore() 사용하여 HighScore도 체크
            
            // 뷰 업데이트
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
        
        // 블록 패턴과 위치를 저장 (대전 모드 공격용)
        int[][] shape = currentBlock.getShape();
        lastBlockPattern = new int[shape.length][];
        for (int i = 0; i < shape.length; i++) {
            lastBlockPattern[i] = shape[i].clone();
        }
        lastBlockX = currentBlock.getX();
        
        // 블록을 보드에 고정
        int specialType = engine.placeBlock(currentBlock, board, colorBoard);
        
        System.out.println("Block placed at x=" + lastBlockX + ", y=" + currentBlock.getY() + ", specialType=" + specialType);
        
        // 블록이 고정된 상태를 임시로 업데이트 (currentBlock을 null로)
        GameState placedState = new GameState.Builder(
            board,
            colorBoard,
            null,  // 블록 고정 후에는 currentBlock이 없음
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
        
        // 고정된 블록을 화면에 표시
        System.out.println("Rendering placed state...");
        currentState = placedState;  // ✅ currentState 업데이트!
        view.render(placedState);
        System.out.println("Placed state rendered");
        
        // 특수 블록 처리 (ItemBlockHandler에 위임)
        if (specialType != 0) {
            // AllClear(2), BoxClear(3), OneLineClear(4) 처리
            itemBlockHandler.handleSpecialBlock(specialType, placedState, (newState) -> {
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
        
        // 일반 블록: 라인 클리어 전에 삭제할 줄 찾기
        List<Integer> fullLines = findFullLines(board);
        
        if (fullLines.size() > 0) {
            // 애니메이션 시작
            animationManager.startLineClearAnimation(fullLines, () -> {
                // 애니메이션 완료 후 실제 라인 클리어 수행
                int[][] clearedBoard = currentState.getBoardArray();
                int[][] clearedColorBoard = currentState.getColorBoard();
                int linesCleared = engine.performLineClear(clearedBoard, clearedColorBoard);
                
                // 점수 계산
                int lineClearScore = engine.calculateLineClearScore(linesCleared, currentState.getCurrentLevel());
                int newScore = currentState.getScore() + lineClearScore;
                
                // 상태 업데이트
                int totalLines = currentState.getTotalLinesCleared() + linesCleared;
                int newLevel = engine.calculateLevel(totalLines);
                
                GameState newState = new GameState.Builder(
                    clearedBoard,
                    clearedColorBoard,
                    null,  // 새 블록 생성 전이므로 null
                    currentState.getNextBlock(),
                    currentState.isItemMode()
                )
                    .score(newScore)
                    .totalLinesCleared(totalLines)
                    .currentLevel(newLevel)
                    .lineClearCount(currentState.getLineClearCount() + linesCleared)
                    .itemGenerateCount(currentState.getItemGenerateCount())
                    .blocksSpawned(currentState.getBlocksSpawned())
                    .build();
                
                currentState = newState;
                score = newScore;
                
                // BlockSpawner에 라인 클리어 알림
                blockSpawner.addLineClearCount(linesCleared);
                
                // LineClearedEvent 발행
                int[] rows = fullLines.stream().mapToInt(Integer::intValue).toArray();
                eventBus.publish(new LineClearedEvent(rows, linesCleared, newScore));
                
                // 게임 오버 체크
                if (engine.checkGameOver(clearedBoard)) {
                    handleGameOver();
                    return;
                }
                
                // 새 블록 생성
                spawnNewBlock();
            });
            return;  // 애니메이션 진행 중
        }
        
        // 라인 클리어가 없는 경우
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
        
        // GameOverEvent 발생하여 FrameBoard에 알림
        GameOverEvent event = new GameOverEvent(score, 0);
        eventBus.publish(event);
        
        System.out.println("Game Over! Final Score: " + score);
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
        
        // 첫 블록 생성 (게임 시작 시)
        spawnNewBlock();
        
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
     * 게임 리셋 (재시작)
     */
    public void reset() {
        System.out.println("GameController reset");
        
        // 게임 정지
        stop();
        
        // 상태 초기화
        currentState = createInitialState();
        score = 0;
        isGameOver = false;
        
        // BlockSpawner 리셋
        blockSpawner.reset();
        
        // 애니메이션 정지
        animationManager.stopAllAnimations();
        
        // 뷰 업데이트
        view.render(currentState);
        
        System.out.println("GameController reset complete");
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
        
        GameState prevState = currentState;
        currentState = engine.moveLeft(currentState);
        view.render(currentState);
        
        // 실제로 이동했으면 이벤트 발행
        if (prevState != currentState && currentState.getCurrentBlock() != null) {
            Block block = currentState.getCurrentBlock();
            eventBus.publish(new BlockMovedEvent(block.getX(), block.getY(), 0, 0));
        }
    }
    
    /**
     * 블록을 오른쪽으로 이동
     */
    public void moveRight() {
        if (isPaused || !isRunning) return;
        
        GameState prevState = currentState;
        currentState = engine.moveRight(currentState);
        view.render(currentState);
        
        // 실제로 이동했으면 이벤트 발행
        if (prevState != currentState && currentState.getCurrentBlock() != null) {
            Block block = currentState.getCurrentBlock();
            eventBus.publish(new BlockMovedEvent(block.getX(), block.getY(), 0, 0));
        }
    }
    
    /**
     * 블록을 아래로 이동 (소프트 드롭)
     */
    public void moveDown() {
        if (isPaused || !isRunning) return;
        
        GameState prevState = currentState;
        currentState = engine.moveDown(currentState);
        view.render(currentState);
        
        // 실제로 이동했으면 이벤트 발행
        if (prevState != currentState && currentState.getCurrentBlock() != null) {
            Block block = currentState.getCurrentBlock();
            eventBus.publish(new BlockMovedEvent(block.getX(), block.getY(), 0, 0));
        }
    }
    
    /**
     * 블록 회전
     */
    public void rotate() {
        if (isPaused || !isRunning) return;
        
        GameState prevState = currentState;
        currentState = engine.rotate(currentState);
        view.render(currentState);
        
        // 실제로 회전했으면 이벤트 발행
        if (prevState != currentState && currentState.getCurrentBlock() != null) {
            Block block = currentState.getCurrentBlock();
            eventBus.publish(new BlockRotatedEvent(block.getX(), block.getY(), 0, 0));
        }
    }
    
    /**
     * 하드 드롭 (즉시 바닥까지)
     * @return 드롭한 거리
     */
    public int hardDrop() {
        if (isPaused || !isRunning) return 0;
        
        Block currentBlock = currentState.getCurrentBlock();
        if (currentBlock == null) return 0;
        
        // 하드 드롭 거리 계산하고 실제로 이동
        int dropDistance = engine.calculateHardDropDistance(currentState);
        
        // 하드 드롭 점수 추가 및 착지 처리
        if (dropDistance > 0) {
            int hardDropScore = dropDistance * 2;  // 한 칸당 2점
            addScore(hardDropScore);  // ✅ addScore() 사용하여 HighScore도 체크
            
            // 블록 착지 처리 (이미 hardDrop으로 이동된 상태)
            handleBlockLanding();
        }
        
        return dropDistance;
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
     * 점수 추가 (HighScore도 함께 체크)
     */
    public void addScore(int points) {
        score += points;
        view.setScore(score);
        
        // HighScore 체크 및 업데이트
        HighScoreModel highScoreModel = HighScoreModel.getInstance();
        int savedHighScore = highScoreModel.getHighScore(itemMode);
        if (score > savedHighScore) {
            view.setHighScore(score);
        }
        
        // 점수 업데이트 이벤트 발행 (대전 모드 등에서 사용)
        eventBus.publish(new ScoreUpdateEvent(score));
    }
    
    /**
     * 가득 찬 줄 찾기
     */
    private List<Integer> findFullLines(int[][] board) {
        List<Integer> fullLines = new ArrayList<>();
        int INNER_TOP = 2;
        int INNER_BOTTOM = board.length - 2;
        int INNER_LEFT = 1;
        int INNER_RIGHT = board[0].length - 2;
        
        for (int row = INNER_BOTTOM; row >= INNER_TOP; row--) {
            boolean isFull = true;
            for (int col = INNER_LEFT; col <= INNER_RIGHT; col++) {
                if (board[row][col] == 0) {
                    isFull = false;
                    break;
                }
            }
            if (isFull) {
                fullLines.add(row);
            }
        }
        return fullLines;
    }
    
    /**
     * 공격 줄 추가 (대전 모드용)
     * 보드 아래쪽에 줄을 추가하고, 블록 패턴 모양대로 빈 칸을 만듦
     * @param lines 추가할 줄 수
     * @param blockPattern 블록의 패턴 (shape)
     * @param blockX 블록의 X 위치
     */
    public void addAttackLines(int lines, int[][] blockPattern, int blockX) {
        if (lines <= 0) return;
        
        int[][] board = currentState.getBoardArray();
        int[][] colorBoard = currentState.getColorBoard();
        int ROWS = board.length;
        int COLS = board[0].length;
        int INNER_LEFT = 1;
        int INNER_RIGHT = COLS - 2;
        int INNER_TOP = 2;
        int INNER_BOTTOM = ROWS - 2;
        
        // 기존 블록들을 위로 올림
        for (int i = INNER_TOP; i <= INNER_BOTTOM - lines; i++) {
            for (int j = INNER_LEFT; j <= INNER_RIGHT; j++) {
                board[i][j] = board[i + lines][j];
                colorBoard[i][j] = colorBoard[i + lines][j];
            }
        }
        
        // 아래쪽에 새 줄 추가 (블록 패턴 모양으로 빈 칸 생성)
        for (int i = INNER_BOTTOM - lines + 1; i <= INNER_BOTTOM; i++) {
            for (int j = INNER_LEFT; j <= INNER_RIGHT; j++) {
                // 기본적으로 모두 채움
                board[i][j] = 1;
                colorBoard[i][j] = 8;  // 회색 (공격 줄 색상)
            }
        }
        
        // 블록 패턴이 있으면 그 모양대로 구멍 뚫기
        if (blockPattern != null && blockPattern.length > 0) {
            int patternHeight = Math.min(blockPattern.length, lines);
            
            for (int i = 0; i < patternHeight; i++) {
                int boardRow = INNER_BOTTOM - i;  // 아래에서부터 채움
                
                for (int j = 0; j < blockPattern[i].length; j++) {
                    int boardCol = blockX + j;
                    
                    // 보드 범위 체크
                    if (boardCol >= INNER_LEFT && boardCol <= INNER_RIGHT && 
                        blockPattern[i][j] == 1) {
                        // 블록이 있던 자리를 빈 칸으로
                        board[boardRow][boardCol] = 0;
                        colorBoard[boardRow][boardCol] = 0;
                    }
                }
            }
        }
        
        // 상태 업데이트
        currentState = new GameState.Builder(
            board,
            colorBoard,
            currentState.getCurrentBlock(),
            currentState.getNextBlock(),
            currentState.isItemMode()
        )
            .score(currentState.getScore())
            .totalLinesCleared(currentState.getTotalLinesCleared())
            .currentLevel(currentState.getCurrentLevel())
            .lineClearCount(currentState.getLineClearCount())
            .itemGenerateCount(currentState.getItemGenerateCount())
            .blocksSpawned(currentState.getBlocksSpawned())
            .lastLineClearScore(currentState.getLastLineClearScore())
            .build();
        
        // 화면 업데이트
        view.render(currentState);
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
