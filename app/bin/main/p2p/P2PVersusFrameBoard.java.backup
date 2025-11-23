package p2p;

import javax.swing.*;
import java.awt.*;

import game.GameView;
import game.core.GameController;
import game.events.*;
import network.NetworkManager;
import network.NetworkRole;
import network.messages.GameEventMessage;
import network.messages.NetworkMessage;
import network.MessageReceiver;
import versus.VersusMode;

/**
 * P2P 네트워크 대전용 프레임
 * 내 게임 (조작 가능) + 상대방 게임 (네트워크 이벤트로만 표시)
 */
public class P2PVersusFrameBoard extends JFrame {
    
    // 화면 크기
    private static double safeScreenRatio() {
        double r = start.StartFrame.screenRatio;
        if (Double.isNaN(r) || Double.isInfinite(r) || r <= 0.0) return 1.2;
        return r;
    }
    
    private final int FRAME_WIDTH = (int)(1200 * safeScreenRatio());
    private final int FRAME_HEIGHT = (int)(600 * safeScreenRatio());
    
    // 네트워크
    private final NetworkManager networkManager;
    private final int myPlayerId;  // 1=서버, 2=클라이언트
    
    // 게임 상태
    private final VersusMode mode;
    private final int difficulty;
    
    // 내 게임 (로컬, 조작 가능)
    private GameView myGameView;
    private GameController myGameController;
    private JLabel myScoreLabel;
    
    // 상대방 게임 (원격, 단순 표시)
    private RemoteGameState remoteGameState;
    private RemoteGamePanel remoteGamePanel;
    private JLabel remoteScoreLabel;
    
    /**
     * P2PVersusFrameBoard 생성자
     * 
     * @param networkManager 네트워크 관리자
     * @param mode 게임 모드 (NORMAL, ITEM, TIME_LIMIT)
     * @param difficulty 난이도 (0=normal, 1=hard, 2=easy)
     */
    public P2PVersusFrameBoard(NetworkManager networkManager, VersusMode mode, int difficulty) {
        this.networkManager = networkManager;
        this.mode = mode;
        this.difficulty = difficulty;
        
        // 역할에 따라 Player ID 결정
        this.myPlayerId = (networkManager.getRole() == NetworkRole.SERVER) ? 1 : 2;
        
        String roleName = (myPlayerId == 1) ? "서버" : "클라이언트";
        setTitle("Tetris - P2P 대전 (" + roleName + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        
        // UI 구성
        setupUI();
        
        // 네트워크 동기화 설정
        setupNetworkSync();
        
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
        
        // 게임 시작
        startGame();
    }
    
    /**
     * UI 구성
     */
    private void setupUI() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));  // 좌우 2분할
        
        boolean itemMode = (mode == VersusMode.ITEM);
        
        // === 내 게임 패널 ===
        myGameView = new GameView(itemMode, false);
        myGameController = new GameController(myGameView, itemMode, difficulty);
        JPanel myPanel = createPlayerPanel("나 (" + (myPlayerId == 1 ? "서버" : "클라이언트") + ")", 
                                           myGameView, myGameController, true);
        
        // === 상대방 게임 패널 ===
        remoteGameView = new GameView(itemMode, false);
        remoteGameController = new GameController(remoteGameView, itemMode, difficulty);
        JPanel remotePanel = createPlayerPanel("상대방", remoteGameView, remoteGameController, false);
        
        // 레이아웃: 서버는 왼쪽, 클라이언트는 오른쪽
        if (myPlayerId == 1) {  // 서버
            mainPanel.add(myPanel);
            mainPanel.add(remotePanel);
        } else {  // 클라이언트
            mainPanel.add(remotePanel);
            mainPanel.add(myPanel);
        }
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 내 게임만 키 입력 등록
        P2PKeyListener keyListener = new P2PKeyListener(myGameController);
        addKeyListener(keyListener);
        setFocusable(true);
        requestFocusInWindow();
    }
    
    /**
     * 플레이어 패널 생성
     * 
     * @param title 패널 제목
     * @param gameView 게임 뷰
     * @param gameController 게임 컨트롤러
     * @param isLocal 로컬 플레이어 여부
     */
    private JPanel createPlayerPanel(String title, GameView gameView, 
                                    GameController gameController, boolean isLocal) {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 상단 정보 패널
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.DARK_GRAY);
        infoPanel.setPreferredSize(new Dimension(0, (int)(50 * safeScreenRatio())));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(settings.FontManager.getKoreanFont(Font.BOLD, (int)(16 * safeScreenRatio())));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        scoreLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14 * safeScreenRatio())));
        scoreLabel.setForeground(Color.WHITE);
        
        infoPanel.setLayout(new GridLayout(2, 1));
        infoPanel.add(titleLabel);
        infoPanel.add(scoreLabel);
        
        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(gameView, BorderLayout.CENTER);
        
        // 이벤트 구독
        final boolean local = isLocal;
        
        // 점수 업데이트
        gameController.getEventBus().subscribe(ScoreUpdateEvent.class, new EventListener<ScoreUpdateEvent>() {
            @Override
            public void onEvent(ScoreUpdateEvent event) {
                updateScore(local, event.getNewScore(), scoreLabel);
            }
        }, 0);
        
        // 블록 이동 시 화면 업데이트 (실시간 동기화)
        gameController.getEventBus().subscribe(game.events.BlockMovedEvent.class, new EventListener<game.events.BlockMovedEvent>() {
            @Override
            public void onEvent(game.events.BlockMovedEvent event) {
                SwingUtilities.invokeLater(() -> {
                    gameView.repaint();
                });
            }
        }, 0);
        
        // 블록 회전 시 화면 업데이트 (실시간 동기화)
        gameController.getEventBus().subscribe(game.events.BlockRotatedEvent.class, new EventListener<game.events.BlockRotatedEvent>() {
            @Override
            public void onEvent(game.events.BlockRotatedEvent event) {
                SwingUtilities.invokeLater(() -> {
                    gameView.repaint();
                });
            }
        }, 0);
        
        // 블록 배치 시 화면 업데이트
        gameController.getEventBus().subscribe(game.events.BlockPlacedEvent.class, new EventListener<game.events.BlockPlacedEvent>() {
            @Override
            public void onEvent(game.events.BlockPlacedEvent event) {
                SwingUtilities.invokeLater(() -> {
                    gameView.repaint();
                });
            }
        }, 0);
        
        // 게임 오버
        gameController.getEventBus().subscribe(GameOverEvent.class, new EventListener<GameOverEvent>() {
            @Override
            public void onEvent(GameOverEvent event) {
                handleGameOver(local, event.getFinalScore());
            }
        }, 0);
        
        // 참조 저장
        if (isLocal) {
            myScoreLabel = scoreLabel;
        } else {
            remoteScoreLabel = scoreLabel;
        }
        
        return panel;
    }
    
    /**
     * 네트워크 동기화 설정
     */
    private void setupNetworkSync() {
        // MessageSender 래퍼 생성 (NetworkManager의 sendMessage를 사용)
        network.MessageSender senderWrapper = new network.MessageSender(null) {
            @Override
            public boolean sendMessage(network.messages.NetworkMessage message) {
                return networkManager.sendMessage(message);
            }
        };
        
        eventSynchronizer = new EventSynchronizer(
            myGameController.getEventBus(),         // 내 이벤트
            remoteGameController.getEventBus(),     // 상대방 이벤트
            senderWrapper,
            myPlayerId
        );
        
        // NetworkManager에 리스너 등록
        networkManager.addMessageListener(eventSynchronizer);
        
        // 원격 게임 컨트롤러의 블록 이동 이벤트 리스너 추가
        setupRemoteBlockUpdateListeners();
        
        System.out.println("P2P 이벤트 동기화 설정 완료");
    }
    
    /**
     * 원격 게임 컨트롤러의 블록 위치 업데이트 리스너 설정
     */
    private void setupRemoteBlockUpdateListeners() {
        // BlockMovedEvent를 받으면 원격 게임 뷰를 즉시 다시 그림
        remoteGameController.getEventBus().subscribe(game.events.BlockMovedEvent.class, 
            new EventListener<game.events.BlockMovedEvent>() {
                @Override
                public void onEvent(game.events.BlockMovedEvent event) {
                    SwingUtilities.invokeLater(() -> {
                        remoteGameView.repaint();
                        System.out.println("원격 블록 이동: (" + event.getX() + ", " + event.getY() + ")");
                    });
                }
            }, 0);
        
        // BlockRotatedEvent를 받으면 원격 게임 뷰를 즉시 다시 그림
        remoteGameController.getEventBus().subscribe(game.events.BlockRotatedEvent.class,
            new EventListener<game.events.BlockRotatedEvent>() {
                @Override
                public void onEvent(game.events.BlockRotatedEvent event) {
                    SwingUtilities.invokeLater(() -> {
                        remoteGameView.repaint();
                        System.out.println("원격 블록 회전: (" + event.getX() + ", " + event.getY() + ")");
                    });
                }
            }, 0);
        
        // BlockPlacedEvent를 받으면 원격 게임 뷰를 즉시 다시 그림
        remoteGameController.getEventBus().subscribe(game.events.BlockPlacedEvent.class,
            new EventListener<game.events.BlockPlacedEvent>() {
                @Override
                public void onEvent(game.events.BlockPlacedEvent event) {
                    SwingUtilities.invokeLater(() -> {
                        remoteGameView.repaint();
                        System.out.println("원격 블록 배치: (" + event.getX() + ", " + event.getY() + ")");
                    });
                }
            }, 0);
    }
    
    /**
     * 게임 시작
     */
    private void startGame() {
        System.out.println("P2P 게임 시작: Player " + myPlayerId);
        
        // 내 게임만 시작 (키보드 입력 + 자동 낙하)
        myGameController.start();
        
        // 원격 게임은 시작하지 않음!
        // 네트워크 이벤트만으로 GameView 업데이트
        System.out.println("원격 게임은 네트워크 이벤트로만 업데이트됩니다.");
    }
    
    /**
     * 점수 업데이트
     */
    private void updateScore(boolean isLocal, int newScore, JLabel scoreLabel) {
        if (isLocal) {
            myScore = newScore;
        } else {
            remoteScore = newScore;
        }
        
        SwingUtilities.invokeLater(() -> {
            scoreLabel.setText("Score: " + newScore);
        });
    }
    
    /**
     * 게임 오버 처리
     */
    private void handleGameOver(boolean isLocal, int finalScore) {
        String player = isLocal ? "나" : "상대방";
        System.out.println(player + " 게임 오버! 최종 점수: " + finalScore);
        
        // 양쪽 다 게임 오버면 결과 표시
        SwingUtilities.invokeLater(() -> {
            String message;
            if (isLocal) {
                message = "패배!\n내 점수: " + myScore + "\n상대방 점수: " + remoteScore;
            } else {
                message = "승리!\n내 점수: " + myScore + "\n상대방 점수: " + remoteScore;
            }
            
            JOptionPane.showMessageDialog(this, message, "게임 종료", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // 메뉴로 돌아가기
            dispose();
            new p2p.P2PMenuFrame();
        });
    }
    
    /**
     * 일시정지/재개
     */
    public void togglePause() {
        if (isPaused) {
            myGameController.resume();
            isPaused = false;
        } else {
            myGameController.pause();
            isPaused = true;
        }
    }
    
    /**
     * 게임 컨트롤러 접근 (테스트용)
     */
    public GameController getMyGameController() {
        return myGameController;
    }
    
    public GameController getRemoteGameController() {
        return remoteGameController;
    }
    
    public EventSynchronizer getEventSynchronizer() {
        return eventSynchronizer;
    }
}
