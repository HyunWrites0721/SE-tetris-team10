package game;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;

import settings.HighScoreModel;
import game.events.*;
import game.listeners.*;

// Note: Do not depend on StartFrame initialization. Use safeScreenRatio() to fallback when needed.


public class FrameBoard extends JFrame {

    // 프레임 크기 설정 (모든 창의 기본 크기를 600x600으로 통일하고 StartFrame의 screenRatio를 곱함)
    private  int FRAME_WIDTH = (int)(600 * safeScreenRatio());
    private  int FRAME_HEIGHT = (int)(600 * safeScreenRatio());

    // StartFrame을 통하지 않고 실행될 때 screenRatio가 0.0일 수 있으므로 기본값(1.2) 보정
    private static double safeScreenRatio() {
        double r = start.StartFrame.screenRatio;
        if (Double.isNaN(r) || Double.isInfinite(r) || r <= 0.0) return 1.2;
        return r;
    }
    
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

        // ScoreBoard UI 컴포넌트 제거됨 - GameView에서 점수 표시

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
    public boolean itemMode = false;     // 아이템 모드 플래그 추가

    // 게임 보드와 일시정지 보드, 블록 텍스트
    private final GameView gameBoard;
    private final PauseBoard pauseBoard;
    private final GameModel gameModel;
    private final GameOverBoard gameOverBoard;
    // ScoreBoard UI 컴포넌트 제거 - HighScore 로직만 유지
    private final HighScoreModel highScoreModel;
    private GameTimer gameTimer;
    private int score = 0;  // 점수 변수 추가
    
    // 이벤트 시스템
    private EventBus eventBus;
    private ScoreBoard scoreBoard; // 이벤트 리스너용 ScoreBoard

    public void increaseScore(int points) {
        score += points;
        gameBoard.setScore(score);  // GameView에 점수 업데이트
        
        // 현재 점수가 최고 점수를 넘으면 실시간으로 하이스코어 갱신
        int currentHighScore = highScoreModel.getHighScore(itemMode);
        if (score > currentHighScore) {
            // 임시로 현재 점수를 하이스코어로 표시 (실제 저장은 게임 종료 시)
            gameBoard.setHighScore(score);
        } else {
            // 현재 점수가 최고 점수를 넘지 않으면 기존 최고 점수 표시
            gameBoard.setHighScore(currentHighScore);
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
    
    /**
     * 이벤트 리스너들을 등록합니다
     */
    private void registerEventListeners() {
        // UI 업데이트 리스너 (높은 우선순위 - 즉시 시각적 피드백)
        eventBus.subscribe(GameEvent.class, new UIUpdateListener(gameBoard), 1);
        
        // 점수판 업데이트 리스너 (중간 우선순위)
        eventBus.subscribe(GameEvent.class, new ScoreBoardListener(scoreBoard), 10);
        
        // 게임 로직 리스너 (중간 우선순위)
        eventBus.subscribe(GameEvent.class, new GameLogicListener(), 20);
        
        // 네트워크 전송 리스너 (낮은 우선순위 - 성능 영향 최소화)
        eventBus.subscribe(GameEvent.class, new NetworkListener(false, "Player1"), 100);
        
        // 특정 이벤트별 추가 처리
        eventBus.subscribe(LineClearedEvent.class, this::handleLineClearedForFrameBoard, 5);
        eventBus.subscribe(GameOverEvent.class, this::handleGameOverForFrameBoard, 5);
        eventBus.subscribe(LevelUpEvent.class, this::handleLevelUpForFrameBoard, 5);
        
        System.out.println("EventBus: All event listeners registered successfully");
    }
    
    /**
     * FrameBoard용 라인 클리어 이벤트 처리
     */
    private void handleLineClearedForFrameBoard(LineClearedEvent event) {
        // 점수 업데이트
        increaseScore(event.getScore());
        
        System.out.println("FrameBoard: Line cleared - Score: " + event.getScore() + 
                         ", Lines: " + event.getClearedLines().length);
    }
    
    /**
     * FrameBoard용 게임 오버 이벤트 처리
     */
    private void handleGameOverForFrameBoard(GameOverEvent event) {
        this.isGameOver = true;
        
        // 점수 저장
        if (!scoreSaved) {
            int finalScore = event.getFinalScore();
            // HighScoreModel의 addScore 메서드 사용
            boolean isTopScore = highScoreModel.addScore("Player", finalScore, "normal", itemMode);
            if (isTopScore) {
                System.out.println("FrameBoard: New high score achieved!");
            }
            scoreSaved = true;
            System.out.println("FrameBoard: Game over - Final score saved: " + finalScore);
        }
        
        // 타이머 정지
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }
    
    /**
     * FrameBoard용 레벨업 이벤트 처리
     */
    private void handleLevelUpForFrameBoard(LevelUpEvent event) {
        // 게임 속도 조정이나 다른 레벨 관련 로직
        System.out.println("FrameBoard: Level up to " + event.getNewLevel());
    }


    public FrameBoard(boolean itemMode) {
    System.out.println("[DEBUG] FrameBoard: constructor enter");
    this.itemMode = itemMode; // 아이템 모드 설정
    System.out.println("[DEBUG] Item Mode: " + (itemMode ? "ON" : "OFF"));
    
    setTitle("Tetris");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);
    
    // High Score 모델 초기화 (싱글톤)
    highScoreModel = HighScoreModel.getInstance();
    
    // 게임 시작 시 블록 생성 설정 로드
    blocks.Block.reloadSettings();
    
    // EventBus 초기화 (싱글플레이어는 동기, 멀티플레이어는 비동기)
    this.eventBus = new EventBus(false); // 기본은 동기 모드
    this.scoreBoard = new ScoreBoard(); // 이벤트 리스너용 ScoreBoard

    JLayeredPane layeredPane = getLayeredPane();

    // 게임 보드와 일시정지 보드, 블록 텍스트를 레이어드 추가
    gameBoard = new GameView(itemMode);  // itemMode 전달
    gameBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
    layeredPane.add(gameBoard, JLayeredPane.DEFAULT_LAYER);

    // StartFrame의 screenRatio에 맞게 GameView의 스케일 적용 (초기 프레임 크기 반영)
    try {
        gameBoard.convertScale(safeScreenRatio());
    } catch (Exception ignored) {}

    // BlockText를 GameBoard 위에 오버레이 (크기 항상 일치 보장)
    gameModel = new GameModel(gameBoard, itemMode);
    // gameModel.setBounds(gameBoard.getBounds());
    // layeredPane.add(gameModel, JLayeredPane.MODAL_LAYER);

    // GameView가 보드와 쌓인 블록을 그릴 수 있도록 모델 바인딩
    gameBoard.setGameModel(gameModel);
    gameBoard.setFallingBlock(gameModel.getCurrentBlock());
    
    // EventBus를 GameModel에 설정
    gameModel.setEventBus(eventBus);
    gameModel.setPlayerId(1); // 기본 플레이어 ID
    
    // 이벤트 리스너들 등록
    registerEventListeners();
    
    // 초기 점수와 최고 점수 설정
    gameBoard.setScore(0);
    gameBoard.setHighScore(highScoreModel.getHighScore(itemMode));

    // 타이머 생성: 1초마다 블록 낙하 및 화면 갱신
    gameTimer = new GameTimer(gameBoard, gameModel, this);
    //
    
    pauseBoard = new PauseBoard(this);
    pauseBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
    pauseBoard.setVisible(false);
    layeredPane.add(pauseBoard, JLayeredPane.PALETTE_LAYER);

    gameOverBoard = new GameOverBoard(this);
    gameOverBoard.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
    gameOverBoard.setVisible(isGameOver);
    layeredPane.add(gameOverBoard, JLayeredPane.MODAL_LAYER);

    // ScoreBoard UI 컴포넌트 제거 - HighScore 로직은 내부적으로만 유지
    // GameView에서 점수 표시를 처리함


    setSize(FRAME_WIDTH, FRAME_HEIGHT);
    setLocationRelativeTo(null);
    setVisible(true);
    System.out.println("[DEBUG] FrameBoard: setVisible(true) called");

    // 키 리스너 추가
    addKeyListener(new GameKeyListener(this, gameBoard, gameModel, gameTimer));
    setFocusable(true);
    requestFocusInWindow();
    }

    // ESC로 일시정지/재개 토글: 오버레이 표시 + 타이머 정지/재시작
    public void paused() {
        // isPaused 상태에 따라 동작 (외부에서 이미 토글됨)
        pauseBoard.setVisible(isPaused);
        pauseBoard.setOpaque(isPaused);
         if (isPaused) {
             if (gameTimer != null) gameTimer.stop();
             // 일시정지 화면이 표시될 때 점수 및 정보 업데이트
             pauseBoard.updateInfo();
             // 일시정지 화면이 표시될 때 포커스 설정
             pauseBoard.setFocusable(true);
             pauseBoard.requestFocusInWindow();
         } else {
             // 타이머 재시작
             if (gameTimer != null) {
                 gameTimer.start();
             }
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
        // 이미 게임오버 상태가 아닐 때만 최종 점수 출력
        if (!isGameOver) {
            System.out.println("Final Score: " + score);  // 최종 점수 출력
            isGameOver = true;  // 게임오버 상태로 설정
        }
        // GameOverBoard 정보 업데이트
        gameOverBoard.updateInfo();
        gameOverBoard.setVisible(true);
        gameOverBoard.setOpaque(true);
        if (gameTimer != null) {
            gameTimer.stop();
        }
        
        // 점수가 이미 저장되었으면 다시 저장하지 않음 (중복 방지)
        if (!scoreSaved && score > 0) {
            // 난이도 문자열 변환 (0: normal, 1: hard, 2: easy)
            String difficulty = "normal";
            if (gameTimer != null) {
                switch (gameTimer.difficulty) {
                    case 0: difficulty = "normal"; break;
                    case 1: difficulty = "hard"; break;
                    case 2: difficulty = "easy"; break;
                    default: difficulty = "normal"; break;
                }
            }
            
            // 사용자 이름 입력받기
            String playerName = promptPlayerName();
            
            highScoreModel.addScore(playerName, score, difficulty, itemMode);
            scoreSaved = true;  // 저장 완료 플래그 설정
            System.out.println("점수 저장 완료: " + playerName + " - " + score + " (Mode: " + (itemMode ? "Item" : "Normal") + ", Difficulty: " + difficulty + ")");
            
            // HighScore는 내부적으로만 업데이트 (UI는 제거됨)
        }
        
        // 게임오버 화면이 표시될 때 포커스 설정
        gameOverBoard.setFocusable(true);
        gameOverBoard.requestFocusInWindow();
    }
    
    // 플레이어 이름 입력받기
    private String promptPlayerName() {
        // 한글 지원 폰트 설정
        javax.swing.UIManager.put("OptionPane.messageFont", settings.FontManager.getKoreanFont(Font.PLAIN, 14));
        javax.swing.UIManager.put("OptionPane.buttonFont", settings.FontManager.getKoreanFont(Font.PLAIN, 12));
        
        String name = JOptionPane.showInputDialog(
            this,
            "게임 종료! 이름을 입력하세요:",
            "Player Name",
            JOptionPane.PLAIN_MESSAGE
        );
        
        // 이름이 입력되지 않았거나 빈 문자열이면 기본값 사용
        if (name == null || name.trim().isEmpty()) {
            return "Player";
        }
        
        // 이름이 너무 길면 자르기 (최대 10자)
        if (name.length() > 10) {
            name = name.substring(0, 10);
        }
        
        return name.trim();
    }

    public void gameInit() {
        // 게임 재시작 시 블록 생성 설정 리로드
        blocks.Block.reloadSettings();
        
        // 기존 타이머를 완전히 정지하고 제거
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;  // 타이머 객체를 null로 설정
        }
        
        // 보드/색상 초기화
        gameModel.boardInit();
        // 블록 상태 초기화 및 뷰 동기화
        gameModel.resetBlocks();
        gameBoard.setFallingBlock(gameModel.getCurrentBlock());
        // 오버레이/상태 초기화
        isGameOver = false;
        if (gameOverBoard != null) gameOverBoard.setVisible(false);
        if (pauseBoard != null) pauseBoard.setVisible(false);

         // 점수 초기화 (최고 점수는 유지)
        score = 0;
        scoreSaved = false;  // 점수 저장 플래그 리셋
        gameBoard.setScore(0);  // 현재 점수 0으로 초기화
        gameBoard.setHighScore(highScoreModel.getHighScore(itemMode));  // 최고 점수 다시 설정

        // 아이템 관련 상태 초기화
        gameModel.itemGenerateCount = 0;
        gameModel.lineClearCount = 0;

        // 일시정지 상태 해제
        isPaused = false;

        // 새로운 타이머 생성 및 시작
        gameTimer = new GameTimer(gameBoard, gameModel, this);
        gameTimer.start();
    }

}