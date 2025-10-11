package game;
import javax.swing.*;


public class FrameBoard extends JFrame {

    // 프레임 크기 설정
    private  int FRAME_WIDTH = 900;
    private  int FRAME_HEIGHT = 1200;


    // pause 상태 변수
    public boolean isPaused = false;
    public boolean isGameOver = false;

    // 게임 보드와 일시정지 보드, 블록 텍스트
    private final GameView gameBoard;
    private final PauseBoard pauseBoard;
    private final GameModel gameModel;
    private final GameOverBoard gameOverBoard;
    private GameTimer gameTimer;

    public GameView getGameBoard() {
        return gameBoard;
    }
    public GameModel getGameModel() {
        return gameModel;
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

    // 타이머 시작: 1초마다 블록 낙하 및 화면 갱신
    gameTimer = new GameTimer(gameBoard, gameModel, this);
    // gameTimer.start();
    

    pauseBoard = new PauseBoard(this);
    pauseBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
    pauseBoard.setVisible(false);
    layeredPane.add(pauseBoard, JLayeredPane.PALETTE_LAYER);

    gameOverBoard = new GameOverBoard(this);
    gameOverBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
    gameOverBoard.setVisible(isGameOver);
    layeredPane.add(gameOverBoard, JLayeredPane.MODAL_LAYER);

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
        // if (isPaused) {
        //     if (gameTimer != null) gameTimer.stop();
        // } else {
        //     if (gameTimer != null) gameTimer.start();
        // }
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
        isPaused = false;
        // if (gameTimer != null) gameTimer.start();
    }
}