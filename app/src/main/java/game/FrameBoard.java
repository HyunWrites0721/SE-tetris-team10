package game;
import javax.swing.*;


public class FrameBoard extends JFrame {

    // 프레임 크기 설정
    private  int FRAME_WIDTH = 900;
    private  int FRAME_HEIGHT = 1200;
    
    public void updateFrameSize(double scale) {
        FRAME_WIDTH = (int)(900 * scale);
        FRAME_HEIGHT = (int)(1200 * scale);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        
        // ScoreBoard 위치 업데이트
        if (scoreBoard != null) {
            scoreBoard.convertScale(scale);
            int cellSize = (int)(30 * scale);
            int boardWidth = 10 * cellSize;
            int x = (FRAME_WIDTH - boardWidth) / 3;
            
            scoreBoard.setBounds(x + boardWidth,  // Next 패널과 동일한 x 좌표
                               8 * cellSize,      // Next 패널 아래
                               6 * cellSize,      // Next 패널과 동일한 너비
                               4 * cellSize);     // 높이 4칸
        }
        
        setLocationRelativeTo(null); // 화면 중앙에 위치
    }


    // pause 상태 변수
    public boolean isPaused = false;
    public boolean isGameOver = false;

    // 게임 보드와 일시정지 보드, 블록 텍스트
    private final GameView gameBoard;
    private final PauseBoard pauseBoard;
    private final GameModel gameModel;
    private final GameOverBoard gameOverBoard;
    private final ScoreBoard scoreBoard;
    private GameTimer gameTimer;
    private int score = 0;  // 점수 변수 추가

    public void increaseScore(int points) {
        score += points;
        scoreBoard.setScore(score);
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

    JLayeredPane layeredPane = getLayeredPane();

    // 게임 보드와 일시정지 보드, 블록 텍스트를 레이어드 추가
    gameBoard = new GameView();
    gameBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
    layeredPane.add(gameBoard, JLayeredPane.DEFAULT_LAYER);

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
    // GameView의 좌표 계산 방식을 따라 ScoreBoard 위치 설정
    int boardWidth = 10 * 30;  // COLS * CELL_SIZE
    int x = (FRAME_WIDTH - boardWidth) / 3;
    // Next 패널과 동일한 x 위치, y는 Next 패널 바로 아래
    scoreBoard.setBounds(x + boardWidth,  // Next 패널과 동일한 x 좌표
                        8 * 30,  // Next 패널 아래 (NEXT_MARGIN * 2 + NEXT_ROWS = 2 + 4 + 2 = 8)
                        6 * 30,  // Next 패널과 동일한 너비 (NEXT_COLS * CELL_SIZE)
                        4 * 30); // 높이 4칸
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
         } else {
             if (gameTimer != null) gameTimer.start();
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
    }

    public void gameInit() {
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

         // 점수 초기화
        score = 0;
        scoreBoard.setScore(0);
        
        isPaused = false;
         if (gameTimer != null) gameTimer.start();
    }

}