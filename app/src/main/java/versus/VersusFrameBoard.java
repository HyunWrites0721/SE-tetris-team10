package versus;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import game.GameView;
import game.core.GameController;
import game.events.*;

/**
 * 2인 대전용 프레임
 * 화면을 좌우로 나누어 2개의 게임을 동시에 진행
 */
public class VersusFrameBoard extends JFrame {
    
    // 화면 크기 (가로 2배)
    private static double safeScreenRatio() {
        double r = start.StartFrame.screenRatio;
        if (Double.isNaN(r) || Double.isInfinite(r) || r <= 0.0) return 1.2;
        return r;
    }
    
    private int FRAME_WIDTH = (int)(1200 * safeScreenRatio());  // 가로 2배
    private int FRAME_HEIGHT = (int)(600 * safeScreenRatio());
    
    // 게임 상태
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private VersusMode mode;
    private int difficulty;
    
    // Player 1 (왼쪽) - WASD + F
    private GameView gameBoard1;
    private GameController gameController1;
    private JPanel scorePanel1;
    private JLabel scoreLabel1;
    private int score1 = 0;
    private VersusAttackManager attackManager1;  // Player 1의 공격 관리자
    
    // Player 2 (오른쪽) - UHJK + L
    private GameView gameBoard2;
    private GameController gameController2;
    private JPanel scorePanel2;
    private JLabel scoreLabel2;
    private int score2 = 0;
    private VersusAttackManager attackManager2;  // Player 2의 공격 관리자
    
    // 공통
    private VersusPauseBoard pauseBoard;
    private VersusResultBoard resultBoard;
    
    public VersusFrameBoard(VersusMode mode, int difficulty) {
        this.mode = mode;
        this.difficulty = difficulty;
        
        // 공격 관리자 초기화
        attackManager1 = new VersusAttackManager();
        attackManager2 = new VersusAttackManager();
        
        setTitle("Tetris - 대전 모드");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        
        // 메인 컨테이너
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));  // 좌우 2분할
        
        // === Player 1 (왼쪽) ===
        JPanel player1Panel = createPlayerPanel(1);
        mainPanel.add(player1Panel);
        
        // === Player 2 (오른쪽) ===
        JPanel player2Panel = createPlayerPanel(2);
        mainPanel.add(player2Panel);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Layered Pane for overlays
        JLayeredPane layeredPane = getLayeredPane();
        
        // Pause Board
        pauseBoard = new VersusPauseBoard(this);
        pauseBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        pauseBoard.setVisible(false);
        layeredPane.add(pauseBoard, JLayeredPane.PALETTE_LAYER);
        
        // Result Board (게임 종료 시 승패 표시)
        resultBoard = new VersusResultBoard(this);
        resultBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        resultBoard.setVisible(false);
        layeredPane.add(resultBoard, JLayeredPane.MODAL_LAYER);
        
        // 키 리스너 추가
        addKeyListener(new VersusKeyListener(this));
        setFocusable(true);
        requestFocusInWindow();
        
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
        
        // 게임 시작
        startGame();
    }
    
    /**
     * 플레이어 패널 생성
     */
    private JPanel createPlayerPanel(int playerNum) {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 점수 패널
        JPanel scorePanel = new JPanel();
        scorePanel.setBackground(Color.DARK_GRAY);
        scorePanel.setPreferredSize(new Dimension(0, (int)(50 * safeScreenRatio())));
        
        JLabel playerLabel = new JLabel("Player " + playerNum, SwingConstants.CENTER);
        playerLabel.setFont(settings.FontManager.getKoreanFont(Font.BOLD, (int)(16 * safeScreenRatio())));
        playerLabel.setForeground(Color.WHITE);
        
        JLabel scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        scoreLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14 * safeScreenRatio())));
        scoreLabel.setForeground(Color.WHITE);
        
        scorePanel.setLayout(new GridLayout(2, 1));
        scorePanel.add(playerLabel);
        scorePanel.add(scoreLabel);
        
        panel.add(scorePanel, BorderLayout.NORTH);
        
        // 게임 보드 (HighScore 패널 숨김)
        boolean itemMode = (mode == VersusMode.ITEM);
        GameView gameBoard = new GameView(itemMode, false);  // false = HighScore 패널 숨김
        
        // GameController 생성
        GameController gameController = new GameController(gameBoard, itemMode, difficulty);
        
        // 이벤트 리스너 등록
        final int player = playerNum;
        gameController.getEventBus().subscribe(GameOverEvent.class, new EventListener<GameOverEvent>() {
            @Override
            public void onEvent(GameOverEvent event) {
                handleGameOver(player, event.getFinalScore());
            }
        }, 0);
        
        // 점수 업데이트 이벤트 구독
        gameController.getEventBus().subscribe(game.events.ScoreUpdateEvent.class, new EventListener<game.events.ScoreUpdateEvent>() {
            @Override
            public void onEvent(game.events.ScoreUpdateEvent event) {
                updateScore(player, event.getNewScore());
            }
        }, 0);
        
        // 줄 삭제 이벤트 구독 (공격 시스템)
        gameController.getEventBus().subscribe(LineClearedEvent.class, new EventListener<LineClearedEvent>() {
            @Override
            public void onEvent(LineClearedEvent event) {
                handleLineCleared(player, event.getClearedLines().length);
            }
        }, 0);
        
        panel.add(gameBoard, BorderLayout.CENTER);
        
        // 플레이어별 참조 저장
        if (playerNum == 1) {
            gameBoard1 = gameBoard;
            gameController1 = gameController;
            scorePanel1 = scorePanel;
            scoreLabel1 = scoreLabel;
        } else {
            gameBoard2 = gameBoard;
            gameController2 = gameController;
            scorePanel2 = scorePanel;
            scoreLabel2 = scoreLabel;
        }
        
        return panel;
    }
    
    /**
     * 게임 시작
     */
    private void startGame() {
        gameController1.start();
        gameController2.start();
    }
    
    /**
     * 점수 업데이트
     */
    public void updateScore(int player, int score) {
        if (player == 1) {
            score1 = score;
            scoreLabel1.setText("Score: " + score1);
        } else {
            score2 = score;
            scoreLabel2.setText("Score: " + score2);
        }
    }
    
    /**
     * 줄 삭제 처리 (공격 시스템)
     */
    private void handleLineCleared(int player, int linesCleared) {
        // 공격 줄 수 계산
        int attackLines = VersusAttackManager.calculateAttackLines(linesCleared);
        
        if (attackLines > 0) {
            // 상대방에게 공격
            if (player == 1) {
                int received = attackManager2.receiveAttack(attackLines);
                System.out.println("Player 1 attacks Player 2 with " + received + " lines");
                // 즉시 상대방 보드에 공격 줄 추가 (Player 1의 블록 패턴 사용)
                if (received > 0) {
                    Object[] blockInfo = gameController1.getLastBlockInfo();
                    int[][] pattern = (int[][]) blockInfo[0];
                    int blockX = (int) blockInfo[1];
                    gameController2.addAttackLines(received, pattern, blockX);
                }
            } else {
                int received = attackManager1.receiveAttack(attackLines);
                System.out.println("Player 2 attacks Player 1 with " + received + " lines");
                // 즉시 상대방 보드에 공격 줄 추가 (Player 2의 블록 패턴 사용)
                if (received > 0) {
                    Object[] blockInfo = gameController2.getLastBlockInfo();
                    int[][] pattern = (int[][]) blockInfo[0];
                    int blockX = (int) blockInfo[1];
                    gameController1.addAttackLines(received, pattern, blockX);
                }
            }
        }
    }
    
    /**
     * 게임 오버 처리
     */
    private void handleGameOver(int player, int finalScore) {
        if (isGameOver) return;  // 이미 게임오버 처리됨
        
        isGameOver = true;
        
        // 두 게임 모두 정지
        gameController1.stop();
        gameController2.stop();
        
        // 승자 결정
        int winner = (player == 1) ? 2 : 1;  // 먼저 게임오버 된 사람이 패배
        int winnerScore = (winner == 1) ? score1 : score2;
        int loserScore = (winner == 1) ? score2 : score1;
        
        // 결과 화면 표시
        resultBoard.showResult(winner, winnerScore, loserScore);
        resultBoard.setVisible(true);
        resultBoard.setOpaque(true);
        resultBoard.setFocusable(true);
        resultBoard.requestFocusInWindow();
    }
    
    /**
     * 일시정지 토글
     */
    public void togglePause() {
        isPaused = !isPaused;
        
        if (isPaused) {
            gameController1.pause();
            gameController2.pause();
            pauseBoard.setVisible(true);
            pauseBoard.setOpaque(true);
            pauseBoard.setFocusable(true);
            pauseBoard.requestFocusInWindow();
        } else {
            gameController1.resume();
            gameController2.resume();
            pauseBoard.setVisible(false);
            this.requestFocusInWindow();
        }
    }
    
    /**
     * Getter methods
     */
    public GameController getGameController1() {
        return gameController1;
    }
    
    public GameController getGameController2() {
        return gameController2;
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public boolean isGameOver() {
        return isGameOver;
    }
}
