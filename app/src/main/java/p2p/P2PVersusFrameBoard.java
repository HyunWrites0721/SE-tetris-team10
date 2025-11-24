package p2p;

import javax.swing.*;
import java.awt.*;
import game.GameView;
import game.core.GameController;
import game.events.*;
import network.NetworkManager;
import network.NetworkRole;
import versus.VersusMode;

/**
 * P2P 네트워크 대전용 프레임 (RemoteGamePanel 사용 버전)
 */
public class P2PVersusFrameBoard extends JFrame {
    
    private static double safeScreenRatio() {
        double r = start.StartFrame.screenRatio;
        if (Double.isNaN(r) || Double.isInfinite(r) || r <= 0.0) return 1.2;
        return r;
    }
    
    private final int FRAME_WIDTH = (int)(1200 * safeScreenRatio());
    private final int FRAME_HEIGHT = (int)(600 * safeScreenRatio());
    
    // 네트워크
    private final NetworkManager networkManager;
    private final int myPlayerId;
    private EventSynchronizer eventSynchronizer;
    
    // 게임 상태
    private final VersusMode mode;
    private final int difficulty;
    
    // 내 게임
    private GameView myGameView;
    private GameController myGameController;
    private JLabel myScoreLabel;
    
    // 상대방 게임 (GameView + 입력 비활성화된 GameController)
    private GameView remoteGameView;
    private GameController remoteGameController;
    private JLabel remoteScoreLabel;
    private RemoteGamePanel remoteGamePanel;
    
    private int myScore = 0;
    private int remoteScore = 0;
    
    public P2PVersusFrameBoard(NetworkManager networkManager, VersusMode mode, int difficulty) {
        this.networkManager = networkManager;
        this.mode = mode;
        this.difficulty = difficulty;
        this.myPlayerId = (networkManager.getRole() == NetworkRole.SERVER) ? 1 : 2;
        
        // 로그 파일로 출력
        try {
            String logFile = "p2p_debug_player" + myPlayerId + ".log";
            java.io.PrintStream out = new java.io.PrintStream(
                new java.io.FileOutputStream(logFile, true), true, "UTF-8");
            System.setOut(out);
            System.setErr(out);
            System.out.println("\n\n========== NEW GAME SESSION ==========");
            System.out.println("Player ID: " + myPlayerId);
            System.out.println("Time: " + new java.util.Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String roleName = (myPlayerId == 1) ? "서버" : "클라이언트";
        setTitle("Tetris - P2P 대전 (" + roleName + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        
        setupUI();
        setupNetworkSync();
        
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
        
        startGame();
    }
    
    private void setupUI() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        
        boolean itemMode = (mode == VersusMode.ITEM);
        
        // 내 게임 생성
        myGameView = new GameView(itemMode, false);
        myGameController = new GameController(myGameView, itemMode, difficulty);
        JPanel myPanel = createMyPanel();
        
        // 상대방 게임 생성
        remoteGameView = new GameView(itemMode, false);
        remoteGameController = new GameController(remoteGameView, itemMode, difficulty);
        remoteGamePanel = new RemoteGamePanel();
        remoteGamePanel.setRemoteComponents(remoteGameView, remoteGameController);
        JPanel remotePanel = createRemotePanel();
        
        // 레이아웃
        if (myPlayerId == 1) {
            mainPanel.add(myPanel);
            mainPanel.add(remotePanel);
        } else {
            mainPanel.add(remotePanel);
            mainPanel.add(myPanel);
        }
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 키 입력
        P2PKeyListener keyListener = new P2PKeyListener(myGameController);
        addKeyListener(keyListener);
        setFocusable(true);
        requestFocusInWindow();
    }
    
    private JPanel createMyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(Color.DARK_GRAY);
        infoPanel.setPreferredSize(new Dimension(0, (int)(50 * safeScreenRatio())));
        
        JLabel title = new JLabel("나 (" + (myPlayerId == 1 ? "서버" : "클라이언트") + ")", SwingConstants.CENTER);
        title.setFont(settings.FontManager.getKoreanFont(Font.BOLD, (int)(16 * safeScreenRatio())));
        title.setForeground(Color.WHITE);
        
        myScoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        myScoreLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14 * safeScreenRatio())));
        myScoreLabel.setForeground(Color.WHITE);
        
        infoPanel.add(title);
        infoPanel.add(myScoreLabel);
        
        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(myGameView, BorderLayout.CENTER);
        
        // 점수 업데이트
        myGameController.getEventBus().subscribe(ScoreUpdateEvent.class, e -> {
            myScore = e.getNewScore();
            SwingUtilities.invokeLater(() -> myScoreLabel.setText("Score: " + myScore));
        }, 0);
        
        // 게임 오버
        myGameController.getEventBus().subscribe(GameOverEvent.class, e -> {
            handleGameOver(true, e.getFinalScore());
        }, 0);
        
        return panel;
    }
    
    private JPanel createRemotePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(Color.DARK_GRAY);
        infoPanel.setPreferredSize(new Dimension(0, (int)(50 * safeScreenRatio())));
        
        JLabel title = new JLabel("상대방", SwingConstants.CENTER);
        title.setFont(settings.FontManager.getKoreanFont(Font.BOLD, (int)(16 * safeScreenRatio())));
        title.setForeground(Color.WHITE);
        
        remoteScoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        remoteScoreLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14 * safeScreenRatio())));
        remoteScoreLabel.setForeground(Color.WHITE);
        
        infoPanel.add(title);
        infoPanel.add(remoteScoreLabel);
        
        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(remoteGameView, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupNetworkSync() {
        // EventBus 생성 (원격용)
        EventBus remoteEventBus = new EventBus();
        
        // MessageSender 래퍼
        network.MessageSender senderWrapper = new network.MessageSender(null) {
            @Override
            public boolean sendMessage(network.messages.NetworkMessage message) {
                return networkManager.sendMessage(message);
            }
        };
        
        eventSynchronizer = new EventSynchronizer(
            myGameController.getEventBus(),
            remoteEventBus,
            senderWrapper,
            myPlayerId
        );
        
        // 네트워크 메시지 수신
        networkManager.addMessageListener(eventSynchronizer);
        
        // 원격 이벤트 처리
        setupRemoteEventHandlers(remoteEventBus);
        
        System.out.println("✅ P2P 네트워크 동기화 설정 완료");
    }
    
    private void setupRemoteEventHandlers(EventBus remoteEventBus) {
        // 블록 생성
        remoteEventBus.subscribe(BlockSpawnedEvent.class, e -> {
            System.out.println("[P2P] 🎯 BlockSpawnedEvent 받음: " + e.getBlockClassName());
            try {
                Class<?> blockClass = Class.forName(e.getBlockClassName());
                blocks.Block block = (blocks.Block) blockClass.getDeclaredConstructor().newInstance();
                
                System.out.println("[P2P]   블록 생성됨: " + block.getClass().getSimpleName());
                System.out.println("[P2P]   색상: " + block.getColor());
                System.out.println("[P2P]   Shape: " + (block.getShape() != null ? block.getShape().length + "x" + block.getShape()[0].length : "null"));
                
                block.bind(remoteGameView);
                block.setPosition(e.getX(), e.getY());
                
                System.out.println("[P2P]   위치 설정: (" + block.getX() + ", " + block.getY() + ")");
                System.out.println("[P2P]   remoteGamePanel.spawnBlock() 호출...");
                
                remoteGamePanel.spawnBlock(block);
                
                System.out.println("[P2P] ✅ BlockSpawnedEvent 처리 완료");
            } catch (Exception ex) {
                System.err.println("[P2P] ❌ 블록 생성 실패: " + ex.getMessage());
                ex.printStackTrace();
            }
        }, 0);
        
        // 블록 이동
        remoteEventBus.subscribe(BlockMovedEvent.class, e -> {
            System.out.println("[P2P] 📍 BlockMovedEvent: (" + e.getX() + ", " + e.getY() + ")");
            remoteGamePanel.moveBlock(e.getX(), e.getY());
        }, 0);
        
        // 블록 회전
        remoteEventBus.subscribe(BlockRotatedEvent.class, e -> {
            System.out.println("[P2P] 🔄 BlockRotatedEvent");
            remoteGamePanel.rotateBlock();
        }, 0);
        
        // 블록 고정
        remoteEventBus.subscribe(BlockPlacedEvent.class, e -> {
            System.out.println("[P2P] 🔻 BlockPlacedEvent");
            remoteGamePanel.placeBlock();
        }, 0);
        
        // 점수 업데이트
        remoteEventBus.subscribe(ScoreUpdateEvent.class, e -> {
            remoteScore = e.getNewScore();
            SwingUtilities.invokeLater(() -> remoteScoreLabel.setText("Score: " + remoteScore));
        }, 0);
        
        // 게임 오버
        remoteEventBus.subscribe(GameOverEvent.class, e -> {
            handleGameOver(false, e.getFinalScore());
        }, 0);
    }
    
    private void startGame() {
        System.out.println("🎮 P2P 게임 시작: Player " + myPlayerId);
        myGameController.start();
    }
    
    private void handleGameOver(boolean isLocal, int finalScore) {
        String player = isLocal ? "나" : "상대방";
        System.out.println(player + " 게임 오버! 최종 점수: " + finalScore);
        
        SwingUtilities.invokeLater(() -> {
            String message;
            if (isLocal) {
                message = "패배!\n내 점수: " + myScore + "\n상대방 점수: " + remoteScore;
            } else {
                message = "승리!\n내 점수: " + myScore + "\n상대방 점수: " + remoteScore;
            }
            
            JOptionPane.showMessageDialog(this, message, "게임 종료", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new p2p.P2PMenuFrame();
        });
    }
}
