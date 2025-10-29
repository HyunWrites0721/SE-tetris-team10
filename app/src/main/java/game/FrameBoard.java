package game;
import javax.swing.*;
import settings.HighScoreModel;
import start.StartFrame;


public class FrameBoard extends JFrame {

    // 프레임 크기 설정 (모든 창의 기본 크기를 600x600으로 통일하고 StartFrame의 screenRatio를 곱함)
    private  int FRAME_WIDTH = (int)(600 * StartFrame.screenRatio);
    private  int FRAME_HEIGHT = (int)(600 * StartFrame.screenRatio);
    
    public void updateFrameSize(double scale) {
        // 기본 크기 600x600을 기준으로 하되, 내부 요소들의 실제 크기를 고려
        int minWidth = (int)(600 * scale);
        int minHeight = (int)(600 * scale);
        
        // GameView의 실제 필요 크기 계산
        if (gameBoard != null) {
            int cellSize = gameBoard.CELL_SIZE;
            int totalCols = gameBoard.COLS + gameBoard.NEXT_COLS + 4; // +4는 여백용
            int totalRows = gameBoard.ROWS + 2; // +2는 여백용
            
            minWidth = Math.min((int)(600 * scale), cellSize * totalCols);
            minHeight = Math.min((int)(600 * scale), cellSize * totalRows);
        }
        
        FRAME_WIDTH = minWidth;
        FRAME_HEIGHT = minHeight;
        setSize(FRAME_WIDTH, FRAME_HEIGHT);

        // ScoreBoard 위치 업데이트
        if (scoreBoard != null && gameBoard != null) {
            scoreBoard.convertScale(scale);
            int cellSize = gameBoard.CELL_SIZE;
            int boardWidth = gameBoard.COLS * cellSize;
            int nextSize = gameBoard.NEXT_COLS * cellSize;
            
            // 게임 보드의 우측 영역 시작점
            int rightPanelX = (FRAME_WIDTH - boardWidth - nextSize) / 2 + boardWidth;
            
            // Next 보드와 Score 보드의 크기를 screenRatio에 맞춰 조정
            int sidePanelWidth = nextSize;
            int nextHeight = nextSize;
            int scoreHeight = (int)(nextSize * 0.8);
            
            // Score 보드의 위치 계산
            int nextPanelBottomY = (FRAME_HEIGHT - nextHeight) / 3;  // Next 패널을 위쪽으로
            int scoreY = nextPanelBottomY + nextHeight + cellSize;   // Next 패널 아래에 약간의 간격을 두고 배치
            
            scoreBoard.setBounds(rightPanelX,        // x 좌표
                               scoreY,               // y 좌표
                               sidePanelWidth,       // 너비
                               scoreHeight);         // 높이
        }

        // Update PauseBoard scaling
        if (pauseBoard != null) {
            pauseBoard.convertScale(scale);
            pauseBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        }

        // Update GameOverBoard scaling
        if (gameOverBoard != null) {
            gameOverBoard.convertScale(scale);
            gameOverBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        }

        setLocationRelativeTo(null); // 화면 중앙에 위치
    }


    // pause 상태 변수
    public boolean isPaused = false;
    public boolean isGameOver = false;
    private boolean scoreSaved = false;  // 점수 저장 여부 플래그 추가

    // 게임 보드와 일시정지 보드, 블록 텍스트
    private final GameView gameBoard;
    private final PauseBoard pauseBoard;
    private final GameModel gameModel;
    private final GameOverBoard gameOverBoard;
    private final ScoreBoard scoreBoard;
    private final HighScoreModel highScoreModel;
    private GameTimer gameTimer;
    private int score = 0;  // 점수 변수 추가

    public void increaseScore(int points) {
        score += points;
        scoreBoard.setScore(score);
        
        // 현재 점수가 최고 점수를 넘으면 실시간 업데이트
        if (score > highScoreModel.getHighScore()) {
            scoreBoard.setHighScore(score);
        }
    }

    public GameView getGameBoard() {
        return gameBoard;
    }
    public GameModel getGameModel() {
        return gameModel;
    }

    public GameTimer getGameTimer() {
        return gameTimer;
    }


    public FrameBoard() {
    setTitle("Tetris");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);
    
    // High Score 모델 초기화 (싱글톤)
    highScoreModel = HighScoreModel.getInstance();
    
    // 게임 시작 시 블록 생성 설정 로드
    blocks.Block.reloadSettings();

    JLayeredPane layeredPane = getLayeredPane();

    // 게임 보드와 일시정지 보드, 블록 텍스트를 레이어드 추가
    gameBoard = new GameView();
    gameBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
    layeredPane.add(gameBoard, JLayeredPane.DEFAULT_LAYER);

    // StartFrame의 screenRatio에 맞게 GameView의 스케일 적용 (초기 프레임 크기 반영)
    try {
        gameBoard.convertScale(StartFrame.screenRatio);
    } catch (Exception ignored) {}

    // BlockText를 GameBoard 위에 오버레이 (크기 항상 일치 보장)
    gameModel = new GameModel(gameBoard);
    gameModel.setBounds(gameBoard.getBounds());
    layeredPane.add(gameModel, JLayeredPane.MODAL_LAYER);

    // GameView가 보드와 쌓인 블록을 그릴 수 있도록 모델 바인딩
    gameBoard.setGameModel(gameModel);
    gameBoard.setFallingBlock(gameModel.getCurrentBlock());

    // 타이머 생성: 1초마다 블록 낙하 및 화면 갱신
    gameTimer = new GameTimer(gameBoard, gameModel, this);
    
    pauseBoard = new PauseBoard(this);
    pauseBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
    pauseBoard.setVisible(false);
    layeredPane.add(pauseBoard, JLayeredPane.PALETTE_LAYER);

    gameOverBoard = new GameOverBoard(this);
    gameOverBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
    gameOverBoard.setVisible(isGameOver);
    layeredPane.add(gameOverBoard, JLayeredPane.MODAL_LAYER);

    scoreBoard = new ScoreBoard();
    // 초기 최고 점수 설정
    scoreBoard.setHighScore(highScoreModel.getHighScore());
    // GameView의 좌표 계산 방식을 따라 ScoreBoard 위치 설정 (StartFrame.screenRatio 반영)
    int cellSizeInit = (int)(30 * StartFrame.screenRatio);
    int boardWidth = 10 * cellSizeInit;  // COLS * CELL_SIZE
    int x = (FRAME_WIDTH - boardWidth) / 3;
    // Next 패널과 동일한 x 위치, y는 Next 패널 바로 아래
    scoreBoard.setBounds(x + boardWidth,  // Next 패널과 동일한 x 좌표
                        8 * cellSizeInit,  // Next 패널 아래 (NEXT_MARGIN * 2 + NEXT_ROWS = 2 + 4 + 2 = 8)
                        6 * cellSizeInit,  // Next 패널과 동일한 너비 (NEXT_COLS * CELL_SIZE)
                        6 * cellSizeInit); // 높이를 6칸으로 증가 (HIGH SCORE + SCORE)
    layeredPane.add(scoreBoard, JLayeredPane.PALETTE_LAYER);


    setSize(FRAME_WIDTH, FRAME_HEIGHT);
    setLocationRelativeTo(null);
    setVisible(true);

    // 키 리스너 추가
    addKeyListener(new GameKeyListener(this, gameBoard, gameModel, gameTimer));
    setFocusable(true);
    requestFocusInWindow();
    }

    // ESC로 일시정지/재개 토글: 오버레이 표시 + 타이머 정지/재시작
    public void paused() {
        pauseBoard.setVisible(isPaused);
        pauseBoard.setOpaque(isPaused);
         if (isPaused) {
             if (gameTimer != null) gameTimer.stop();
             // 일시정지 화면이 표시될 때 포커스 설정
             pauseBoard.setFocusable(true);
             pauseBoard.requestFocusInWindow();
         } else {
             if (gameTimer != null) gameTimer.start();
             // 게임 재개 시 프레임으로 포커스 반환
             this.requestFocusInWindow();
         }
    }
    

    public void setBlockText(int row, int col) {
        gameModel.setBlockText(row, col);
    }

    public void oneLineClear(int row) {
        gameModel.lineClear();
    }
    
    public void gameOver() {
        gameOverBoard.setVisible(true);
        gameOverBoard.setOpaque(true);
        gameTimer.stop();
        
        // 점수가 이미 저장되었으면 다시 저장하지 않음 (중복 방지)
        if (!scoreSaved && score > 0) {
            highScoreModel.addScore("Player", score);
            scoreSaved = true;  // 저장 완료 플래그 설정
            System.out.println("점수 저장 완료: " + score);
            
            // 저장 후 최고 점수가 갱신되었으면 ScoreBoard 업데이트
            scoreBoard.setHighScore(highScoreModel.getHighScore());
        }
        
        // 게임오버 화면이 표시될 때 포커스 설정
        gameOverBoard.setFocusable(true);
        gameOverBoard.requestFocusInWindow();
    }

    public void gameInit() {
        // 게임 재시작 시 블록 생성 설정 리로드
        blocks.Block.reloadSettings();
        
        // 보드/색상 초기화
        gameModel.boardInit();
        // 블록 상태 초기화 및 뷰 동기화
        gameModel.resetBlocks();
        gameBoard.setFallingBlock(gameModel.getCurrentBlock());
        // 오버레이/상태 초기화
        isGameOver = false;
        if (gameOverBoard != null) gameOverBoard.setVisible(false);
        if (pauseBoard != null) pauseBoard.setVisible(false);
        // 일시정지 해제 및 타이머 재시작

         // 점수 초기화 (최고 점수는 유지)
        score = 0;
        scoreSaved = false;  // 점수 저장 플래그 리셋
        scoreBoard.setScore(0);
        scoreBoard.setHighScore(highScoreModel.getHighScore());
        
        gameModel.itemGenerateCount = 0;
        gameModel.lineClearCount = 0;

        isPaused = false;
         if (gameTimer != null) gameTimer.start();
    }

}