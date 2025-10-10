package game;
import javax.swing.*;


public class FrameBoard extends JFrame {

    // 프레임 크기 설정
    private  int FRAME_WIDTH = 900;
    private  int FRAME_HEIGHT = 1200;


    // pause 상태 변수
    private boolean isPaused = false;
    private boolean isGameOver = false;

    // 게임 보드와 일시정지 보드, 블록 텍스트
    private final GameView gameBoard;
    private final PauseBoard pauseBoard;
    private final GameModel blockText;
    private final GameOverBoard gameOverBoard;

    public GameView getGameBoard() {
        return gameBoard;
    }
    public GameModel getBlockText() {
        return blockText;
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
    blockText = new GameModel(gameBoard);
    blockText.setBounds(gameBoard.getBounds());
    layeredPane.add(blockText, JLayeredPane.MODAL_LAYER);

    pauseBoard = new PauseBoard();
    pauseBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
    pauseBoard.setVisible(isPaused);
    layeredPane.add(pauseBoard, JLayeredPane.PALETTE_LAYER);
    
    gameOverBoard = new GameOverBoard();
    gameOverBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
    gameOverBoard.setVisible(isGameOver);
    layeredPane.add(gameOverBoard, JLayeredPane.MODAL_LAYER);

    setSize(FRAME_WIDTH, FRAME_HEIGHT);
    setLocationRelativeTo(null);
    setVisible(true);

    // 키 리스너 추가
    addKeyListener(new GameKeyListener(this, gameBoard));
    setFocusable(true);
    requestFocusInWindow();
    }

    // pause 상태 변환 함수 및 pause 보드 표시
    public void togglePause() {
        isPaused = !isPaused;
        pauseBoard.setVisible(isPaused);
    }
    

    public void setBlockText(int row, int col) {
        blockText.setBlockText(row, col);
    }

    public void oneLineClear(int row) {
        blockText.oneLineClear(row);
    }
    
    public void toggleGameOver() {
        if (blockText.isGameOver()) {
            isGameOver = true;
            gameOverBoard.setVisible(isGameOver);
        }
    }

    public void gameInit() {
        isGameOver = false;
        gameOverBoard.setVisible(isGameOver);
        blockText.boardInit();
    }
}