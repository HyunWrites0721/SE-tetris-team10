package p2p;

import javax.swing.*;
import java.awt.*;
import game.GameView;
import game.core.GameController;
import game.events.*;
import network.NetworkManager;
import network.DisconnectionHandler;
import network.messages.GameControlMessage;
import network.messages.GameControlMessage.ControlType;
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
    
    private final int FRAME_WIDTH = (int)(1400 * safeScreenRatio());  // 대전 모드와 동일
    private final int FRAME_HEIGHT = (int)(700 * safeScreenRatio());  // 대전 모드와 동일
    
    // 네트워크
    private final NetworkManager networkManager;
    private final int myPlayerId;
    private EventSynchronizer eventSynchronizer;
    // 등록한 네트워크 리스너 참조 (정리용)
    private NetworkManager.GameControlListener gameControlListener;
    
    // 게임 상태
    private final VersusMode mode;
    private final int difficulty;
    
    // 시간제한 모드 관련
    private javax.swing.Timer gameTimer;
    private int remainingSeconds = 180;
    private JLabel timerLabel;
    private volatile boolean isGameOver = false;
    
    // 내 게임
    private GameView myGameView;
    private GameController myGameController;
    private JLabel myScoreLabel;
    
    // 상대방 게임 (GameView + 입력 비활성화된 GameController)
    private GameView remoteGameView;
    private GameController remoteGameController;
    private JLabel remoteScoreLabel;
    private RemoteGamePanel remoteGamePanel;
    
    // 네트워크 상태 표시
    private JLabel networkStatusLabel;
    
    private int myScore = 0;
    private int remoteScore = 0;
    // START_GAME 메시지 전송 플래그
    private boolean startGameMessageSent = false;
    // 게임 정상 종료 플래그 (승패가 결정되어 정상 종료된 경우)
    private volatile boolean gameEndedNormally = false;
    
    public P2PVersusFrameBoard(NetworkManager networkManager, VersusMode mode, int difficulty) {
        this.networkManager = networkManager;
        this.mode = mode;
        this.difficulty = difficulty;
        this.myPlayerId = (networkManager.getRole() == NetworkRole.SERVER) ? 1 : 2;
        
        // 연결 끊김 핸들러 등록
        networkManager.setDisconnectionHandler(new DisconnectionHandler(() -> {
            handleOpponentDisconnected();
        }));
        
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
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 강제 종료 방지
        
        // 윈도우 닫기 이벤트 처리
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                P2PVersusFrameBoard.this.handleWindowClosing();
            }
        });
        
        setResizable(false);
        setLayout(new BorderLayout());
        
        // Register game control listener FIRST to catch early START_GAME messages
        registerGameControlListener();
        
        setupUI();
        setupNetworkSync();

        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);

        // 게임 시작은 START_GAME 메시지 수신 또는 서버의 직접 요청으로만 시작합니다.
    }

    private volatile boolean started = false;

    /**
     * 외부에서 게임 시작을 요청할 때 호출 (서버가 직접 시작할 때 사용)
     */
    public synchronized void requestStart() {
        if (started) return;
        started = true;
        startGame();
    }
    
    private void setupUI() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        
        boolean itemMode = (mode == VersusMode.ITEM);
        
        // 내 게임 생성 (HighScore 숨김, AttackPreview 표시)
        myGameView = new GameView(itemMode, false, true);
        myGameController = new GameController(myGameView, itemMode, difficulty);
        try {
            System.out.println("[DEBUG P2PVersusFrameBoard] myGameController instance=" + System.identityHashCode(myGameController)
                + ", myEventBus=" + System.identityHashCode(myGameController.getEventBus()));
        } catch (Throwable __) {
            // ignore
        }
        JPanel myPanel = createMyPanel();
        
        // 상대방 게임 생성 (HighScore 숨김, AttackPreview 표시)
        remoteGameView = new GameView(itemMode, false, true);
        remoteGameController = new GameController(remoteGameView, itemMode, difficulty);
        try {
            System.out.println("[DEBUG P2PVersusFrameBoard] remoteGameController instance=" + System.identityHashCode(remoteGameController)
                + ", remoteEventBus=" + System.identityHashCode(remoteGameController.getEventBus()));
        } catch (Throwable __) {
            // ignore
        }
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
        
        // 시간제한 모드일 때 타이머 UI 추가
        JPanel centerContainer = new JPanel(new BorderLayout());
        
        if (mode == VersusMode.TIME_LIMIT) {
            JPanel timerContainer = new JPanel(new BorderLayout());
            timerContainer.setOpaque(false);
            
            JPanel timerPanel = new JPanel();
            timerPanel.setBackground(new Color(200, 50, 50));
            timerPanel.setPreferredSize(new Dimension((int)(150 * safeScreenRatio()), (int)(40 * safeScreenRatio())));
            timerPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            
            timerLabel = new JLabel("3:00", SwingConstants.CENTER);
            timerLabel.setFont(settings.FontManager.getKoreanFont(Font.BOLD, (int)(20 * safeScreenRatio())));
            timerLabel.setForeground(Color.WHITE);
            
            timerPanel.add(timerLabel);
            
            JPanel topPadding = new JPanel(new FlowLayout(FlowLayout.CENTER));
            topPadding.setOpaque(false);
            topPadding.add(timerPanel);
            
            timerContainer.add(topPadding, BorderLayout.NORTH);
            centerContainer.add(timerContainer, BorderLayout.NORTH);
        }
        
        centerContainer.add(mainPanel, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);
        
        // 하단에 네트워크 상태 표시
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.setBackground(Color.BLACK);
        networkStatusLabel = new JLabel("⚫ 연결 확인 중...");
        networkStatusLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(12 * safeScreenRatio())));
        networkStatusLabel.setForeground(Color.GRAY);
        statusPanel.add(networkStatusLabel);
        add(statusPanel, BorderLayout.SOUTH);
        
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
        
        // 내가 공격을 적용했을 때 remoteGameController의 큐 초기화
        myGameController.getEventBus().subscribe(game.events.AttackAppliedEvent.class, e -> {
            System.out.println("[P2P] 💥 내가 AttackAppliedEvent 발행: lines=" + e.getAttackLines());
            SwingUtilities.invokeLater(() -> {
                try {
                    // 내가 공격을 적용했으므로 remoteGameController의 큐에서 해당 공격 제거
                    remoteGameController.clearAttackQueue();
                    remoteGameView.repaint();
                } catch (Exception ex) {
                    System.err.println("[P2P] remoteGameController.clearAttackQueue 예외: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        }, 0);
        
        // 라인 클리어 (공격 발생) - remoteGameController의 AttackPreviewPanel 업데이트
        myGameController.getEventBus().subscribe(LineClearedEvent.class, e -> {
            int lines = e.getClearedLines().length;
            final int attackLines = (lines >= 2) ? lines : 0; // 2줄 이상이면 같은 수만큼 공격
            if (attackLines > 0) {
                System.out.println("[P2P] 💥 내가 공격 발생: " + attackLines + "줄 → remoteGameController.queueAttackLines() 호출");
                SwingUtilities.invokeLater(() -> {
                    try {
                        // remoteGameController의 큐에 추가하여 AttackPreviewPanel 업데이트
                        remoteGameController.queueAttackLines(attackLines, e.getLastBlockPattern(), e.getLastBlockX());
                        remoteGameView.repaint();
                    } catch (Exception ex) {
                        System.err.println("[P2P] remoteGameController.queueAttackLines 예외: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
            }
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
    
    /**
     * Register the game control listener to handle START_GAME messages.
     * This must be called EARLY before UI setup to catch messages that arrive quickly.
     */
    private void registerGameControlListener() {
        // 게임 제어 메시지(START_GAME) 수신 처리: START_GAME을 받으면 게임 시작
        gameControlListener = message -> {
            if (message.getControlType() == network.messages.GameControlMessage.ControlType.START_GAME) {
                System.out.println("[P2PVersusFrameBoard] START_GAME 수신, 게임 시작 요청");
                requestStart();
            }
        };
        networkManager.addGameControlListener(gameControlListener);
        System.out.println("[P2PVersusFrameBoard] Game control listener registered");
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

        // Debug: print listener counts for verification
        try {
            System.out.println("[DEBUG] Local EventBus listener counts:");
            System.out.println("  BlockSpawnedEvent: " + myGameController.getEventBus().getListenerCount(game.events.BlockSpawnedEvent.class));
            System.out.println("  BlockMovedEvent: " + myGameController.getEventBus().getListenerCount(game.events.BlockMovedEvent.class));
            System.out.println("  BlockRotatedEvent: " + myGameController.getEventBus().getListenerCount(game.events.BlockRotatedEvent.class));
            System.out.println("  BlockPlacedEvent: " + myGameController.getEventBus().getListenerCount(game.events.BlockPlacedEvent.class));
            System.out.println("  LineClearedEvent: " + myGameController.getEventBus().getListenerCount(game.events.LineClearedEvent.class));
            System.out.println("  ScoreUpdateEvent: " + myGameController.getEventBus().getListenerCount(game.events.ScoreUpdateEvent.class));
        } catch (Throwable t) {
            System.err.println("[DEBUG] Failed to print listener counts: " + t.getMessage());
        }
        
        // 네트워크 메시지 수신
        networkManager.addMessageListener(eventSynchronizer);
        
        // 원격 이벤트 처리
        setupRemoteEventHandlers(remoteEventBus);
        
        // 네트워크 상태 모니터링
        startNetworkStatusMonitoring();
        
        System.out.println("✅ P2P 네트워크 동기화 설정 완료");
    }

    @Override
    public void dispose() {
        System.out.println("🔄 P2PVersusFrameBoard dispose() 호출됨");
        cleanupResources();
        super.dispose();
    }
    
    /**
     * 윈도우 닫기 이벤트 처리
     */
    private void handleWindowClosing() {
        System.out.println("⚠️  사용자가 창을 닫으려고 함");
        
        int option = JOptionPane.showConfirmDialog(
            this,
            "게임을 종료하시겠습니까?",
            "게임 종료",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            System.out.println("✅ 사용자가 게임 종료 확인");
            cleanupResources();
            dispose();
            new p2p.P2PMenuFrame();
        } else {
            System.out.println("❌ 사용자가 게임 종료 취소");
        }
    }
    
    /**
     * 모든 리소스 정리
     */
    private void cleanupResources() {
        System.out.println("🧹 리소스 정리 시작...");
        
        // 1. 게임 컨트롤러 중지
        try {
            if (myGameController != null) {
                System.out.println("  - 내 게임 컨트롤러 중지");
                myGameController.stop();
            }
        } catch (Exception e) {
            System.err.println("  ✗ 내 게임 컨트롤러 중지 실패: " + e.getMessage());
        }
        
        try {
            if (remoteGameController != null) {
                System.out.println("  - 상대방 게임 컨트롤러 중지");
                remoteGameController.stop();
            }
        } catch (Exception e) {
            System.err.println("  ✗ 상대방 게임 컨트롤러 중지 실패: " + e.getMessage());
        }
        
        // 2. 네트워크 리스너 정리
        try {
            if (networkManager != null) {
                System.out.println("  - 네트워크 리스너 제거");
                if (eventSynchronizer != null) {
                    networkManager.removeMessageListener(eventSynchronizer);
                }
                if (gameControlListener != null) {
                    networkManager.removeGameControlListener(gameControlListener);
                }
            }
        } catch (Exception e) {
            System.err.println("  ✗ 네트워크 리스너 제거 실패: " + e.getMessage());
        }
        
        // 3. 네트워크 연결 종료
        try {
            if (networkManager != null) {
                System.out.println("  - 네트워크 연결 종료");
                networkManager.disconnect();
            }
        } catch (Exception e) {
            System.err.println("  ✗ 네트워크 연결 종료 실패: " + e.getMessage());
        }
        
        System.out.println("✅ 리소스 정리 완료");
    }
    
    private void setupRemoteEventHandlers(EventBus remoteEventBus) {
        // 블록 생성
        remoteEventBus.subscribe(BlockSpawnedEvent.class, e -> {
            System.out.println("[P2P] 🎯 BlockSpawnedEvent 받음: " + e.getBlockClassName());
            try {
                Class<?> blockClass = Class.forName(e.getBlockClassName());
                blocks.Block block = (blocks.Block) blockClass.getDeclaredConstructor().newInstance();
                // 새로 생성된 블록 인스턴스의 shape가 초기화되지 않았을 수 있으므로 setShape() 호출
                try {
                    if (block.getShape() == null) {
                        block.setShape();
                        System.out.println("[P2P] block.setShape() 호출로 shape 초기화됨: " + block.getClass().getSimpleName());
                    }
                } catch (Throwable t) {
                    System.err.println("[P2P] 블록 shape 초기화 실패: " + t.getMessage());
                }
                
                System.out.println("[P2P]   블록 생성됨: " + block.getClass().getSimpleName());
                System.out.println("[P2P]   색상: " + block.getColor());
                System.out.println("[P2P]   Shape: " + (block.getShape() != null ? block.getShape().length + "x" + block.getShape()[0].length : "null"));
                
                block.bind(remoteGameView);
                block.setPosition(e.getX(), e.getY());

                System.out.println("[P2P]   위치 설정: (" + block.getX() + ", " + block.getY() + ")");
                System.out.println("[P2P]   remoteGamePanel.spawnBlock() 호출 (EDT 안전 처리)...");

                // UI 업데이트는 EDT에서 실행
                SwingUtilities.invokeLater(() -> {
                    if (block.getShape() == null) {
                        System.err.println("[P2P] ❌ 블록의 shape가 null이라 spawn을 건너뜁니다: " + block.getClass().getName());
                        return;
                    }
                    try {
                        remoteGamePanel.spawnBlock(block);
                    } catch (Exception ex) {
                        System.err.println("[P2P] spawnBlock 예외: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
                
                // Next block 처리: 있으면 remote view에 표시
                String nextClassName = e.getNextBlockClassName();
                if (nextClassName != null && !nextClassName.isEmpty()) {
                    try {
                        Class<?> nextClass = Class.forName(nextClassName);
                        blocks.Block nb = (blocks.Block) nextClass.getDeclaredConstructor().newInstance();
                        try {
                            if (nb.getShape() == null) nb.setShape();
                        } catch (Throwable tt) {
                            System.err.println("[P2P] next block setShape 실패: " + tt.getMessage());
                        }
                        nb.bind(remoteGameView);
                        // EDT에서 실제로 NEXT 패널에 반영
                        SwingUtilities.invokeLater(() -> {
                            try {
                                remoteGameView.setNextBlock(nb);
                            } catch (Exception ex) {
                                System.err.println("[P2P] setNextBlock 예외: " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        });
                        System.out.println("[P2P] ✅ NextBlock 설정 완료: " + nb.getClass().getSimpleName());
                    } catch (Exception nex) {
                        System.err.println("[P2P] Next 블록 생성 실패: " + nex.getMessage());
                        nex.printStackTrace();
                    }
                }

                System.out.println("[P2P] ✅ BlockSpawnedEvent 처리 완료");
            } catch (Exception ex) {
                System.err.println("[P2P] ❌ 블록 생성 실패: " + ex.getMessage());
                ex.printStackTrace();
            }
        }, 0);
        
        // 블록 이동
        remoteEventBus.subscribe(BlockMovedEvent.class, e -> {
            System.out.println("[P2P] 📍 BlockMovedEvent: (" + e.getX() + ", " + e.getY() + ")");
            SwingUtilities.invokeLater(() -> {
                try {
                    remoteGamePanel.moveBlock(e.getX(), e.getY());
                } catch (Exception ex) {
                    System.err.println("[P2P] moveBlock 예외: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        }, 0);

        // 라인 클리어
        remoteEventBus.subscribe(LineClearedEvent.class, e -> {
            System.out.println("[P2P] 🧹 LineClearedEvent: " + java.util.Arrays.toString(e.getClearedLines()));
            SwingUtilities.invokeLater(() -> {
                try {
                    remoteGamePanel.clearLines(e.getClearedLines());
                } catch (Exception ex) {
                    System.err.println("[P2P] clearLines 예외: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        }, 0);
        
        // 블록 회전
        remoteEventBus.subscribe(BlockRotatedEvent.class, e -> {
            System.out.println("[P2P] 🔄 BlockRotatedEvent");
            SwingUtilities.invokeLater(() -> {
                try {
                    remoteGamePanel.rotateBlock();
                } catch (Exception ex) {
                    System.err.println("[P2P] rotateBlock 예외: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        }, 0);
        
        // 블록 고정
        remoteEventBus.subscribe(BlockPlacedEvent.class, e -> {
            System.out.println("[P2P] 🔻 BlockPlacedEvent");
            SwingUtilities.invokeLater(() -> {
                try {
                    remoteGamePanel.placeBlock();
                } catch (Exception ex) {
                    System.err.println("[P2P] placeBlock 예외: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        }, 0);
        
        // 점수 업데이트
        remoteEventBus.subscribe(ScoreUpdateEvent.class, e -> {
            remoteScore = e.getNewScore();
            SwingUtilities.invokeLater(() -> {
                // 상단 점수 레이블 업데이트
                remoteScoreLabel.setText("Score: " + remoteScore);
                
                // 상단 점수를 그대로 하단 scoreboard에 복사
                if (remoteGameView != null) {
                    remoteGameView.setScore(remoteScore);
                    System.out.println("[P2P] 상대방 점수 업데이트: " + remoteScore);
                }
            });
        }, 0);
        
        // 게임 오버
        remoteEventBus.subscribe(GameOverEvent.class, e -> {
            handleGameOver(false, e.getFinalScore());
        }, 0);

        // 아이템 활성화 수신: 원격 보드에 효과 적용
        remoteEventBus.subscribe(ItemActivatedEvent.class, e -> {
            System.out.println("[P2P] 🧩 ItemActivatedEvent: " + e.getItemType());
            SwingUtilities.invokeLater(() -> {
                try {
                    remoteGamePanel.applyItemEffect(e.getItemType());
                } catch (Exception ex) {
                    System.err.println("[P2P] applyItemEffect 예외: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        }, 0);

        // 상대방이 공격을 실제로 적용했을 때 내 화면의 상대방 패널에 시각적으로 표시
        remoteEventBus.subscribe(game.events.AttackAppliedEvent.class, e -> {
            System.out.println("[P2P] 🛡️ AttackAppliedEvent 수신: lines=" + e.getAttackLines());
            SwingUtilities.invokeLater(() -> {
                try {
                    // 이미 LineClearedEvent에서 remoteGameController.queueAttackLines()로 큐에 추가했으므로
                    // 여기서는 큐에 있는 공격을 적용만 함
                    remoteGameController.applyQueuedAttacks();
                    // RemoteGamePanel에 업데이트된 보드 상태 동기화
                    remoteGamePanel.syncFromController(remoteGameController);
                    remoteGameView.repaint();
                } catch (Exception ex) {
                    System.err.println("[P2P] AttackAppliedEvent 처리 예외: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        }, 0);

        // 공격 수신: 원격 플레이어의 공격은 내 로컬 보드에 큐에 추가 (대전 모드와 동일)
        remoteEventBus.subscribe(game.events.AttackEvent.class, e -> {
            int[][] pattern = e.getBlockPattern();
            game.util.GameLogger.debug("P2PVersusFrameBoard AttackEvent 수신: lines=" + e.getAttackLines() + 
                " pattern=" + (pattern!=null?(pattern.length+"x"+(pattern.length>0?pattern[0].length:0)) : "null"));
            if (pattern != null && pattern.length > 0 && pattern[0].length > 0) {
                StringBuilder sb = new StringBuilder("  받은 pattern[0]=");
                for (int j = 0; j < Math.min(pattern[0].length, 10); j++) {
                    sb.append(pattern[0][j]);
                }
                game.util.GameLogger.debug(sb.toString());
            }
            
            System.out.println("[P2P] ⚔️ AttackEvent 수신: lines=" + e.getAttackLines() + " from=" + e.getPlayerId()
                + " pattern=" + (e.getBlockPattern()!=null?(e.getBlockPattern().length+"x"+(e.getBlockPattern().length>0?e.getBlockPattern()[0].length:0)) : "<none>"));
            SwingUtilities.invokeLater(() -> {
                try {
                    System.out.println("[DEBUG P2PVersusFrameBoard] invoking queueAttackLines: lines=" + e.getAttackLines()
                        + ", controllerId=" + System.identityHashCode(myGameController)
                        + ", thread=" + Thread.currentThread().getName());
                    // 원격의 공격은 내 로컬 컨트롤러의 큐에 추가 (블럭 착지 시 적용됨)
                    myGameController.queueAttackLines(e.getAttackLines(), e.getBlockPattern(), e.getBlockX());
                    // 즉시 뷰 갱신을 보장하기 위해 myGameView를 리페인트
                    try {
                        myGameView.repaint();
                    } catch (Throwable __) {
                        // ignore
                    }
                } catch (Exception ex) {
                    System.err.println("[P2P] queueAttackLines 예외: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        }, 0);
    }
    
    private void startGame() {
        System.out.println("🎮 P2P 게임 시작: Player " + myPlayerId);
        // 서버라면 START_GAME 메시지를 클라이언트에 전송하여 함께 시작을 알립니다.
        if (myPlayerId == 1 && !startGameMessageSent) {
            try {
                GameControlMessage startMsg = new GameControlMessage(ControlType.START_GAME, mode, Integer.valueOf(myPlayerId), null);
                boolean ok = networkManager.sendMessage(startMsg);
                System.out.println("[P2PVersusFrameBoard] START_GAME 전송 시도: success=" + ok);
                if (ok) startGameMessageSent = true;
            } catch (Throwable t) {
                System.err.println("[P2PVersusFrameBoard] START_GAME 전송 중 오류: " + t.getMessage());
            }
        }

        myGameController.start();
        
        // 원격 게임 컨트롤러는 시작하지 않음 - 네트워크 이벤트로만 화면 업데이트
        // remoteGameController.start()를 호출하면 독립적으로 블럭이 생성되어 동기화 문제 발생
        
        // 시간제한 모드일 때 타이머 시작 (서버와 클라이언트 모두)
        if (mode == VersusMode.TIME_LIMIT) {
            startTimer();
        }
    }
    
    /**
     * 타이머 시작 (시간제한 모드)
     */
    private void startTimer() {
        remainingSeconds = 180;  // 3분
        
        gameTimer = new javax.swing.Timer(1000, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!isGameOver) {
                    remainingSeconds--;
                    updateTimerDisplay();
                    
                    if (remainingSeconds <= 0) {
                        handleTimeUp();
                    }
                }
            }
        });
        gameTimer.start();
    }
    
    /**
     * 타이머 표시 업데이트
     */
    private void updateTimerDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String timeText = String.format("%d:%02d", minutes, seconds);
        
        if (timerLabel != null) {
            SwingUtilities.invokeLater(() -> {
                timerLabel.setText(timeText);
                
                // 10초 이하일 때 깜빡임
                if (remainingSeconds <= 10) {
                    timerLabel.setForeground(remainingSeconds % 2 == 0 ? Color.WHITE : Color.YELLOW);
                }
            });
        }
    }
    
    /**
     * 시간 종료 처리
     */
    private void handleTimeUp() {
        if (isGameOver) return;
        
        isGameOver = true;
        if (gameTimer != null) {
            gameTimer.stop();
        }
        
        // 게임 중지
        try {
            if (myGameController != null) {
                myGameController.stop();
            }
            if (remoteGameController != null) {
                remoteGameController.stop();
            }
        } catch (Exception e) {
            System.err.println("게임 중지 실패: " + e.getMessage());
        }
        
        // 점수로 승자 결정
        SwingUtilities.invokeLater(() -> {
            String message;
            if (myScore > remoteScore) {
                message = "승리!\n내 점수: " + myScore + "\n상대방 점수: " + remoteScore;
            } else if (remoteScore > myScore) {
                message = "패배!\n내 점수: " + myScore + "\n상대방 점수: " + remoteScore;
            } else {
                message = "무승부!\n내 점수: " + myScore + "\n상대방 점수: " + remoteScore;
            }
            
            JOptionPane.showMessageDialog(this, message, "시간 종료", JOptionPane.INFORMATION_MESSAGE);
            cleanupResources();
            dispose();
            new p2p.P2PMenuFrame();
        });
    }
    
    private void handleGameOver(boolean isLocal, int finalScore) {
        if (isGameOver) return;  // 이미 게임 오버 처리됨
        
        isGameOver = true;
        
        String player = isLocal ? "나" : "상대방";
        System.out.println(player + " 게임 오버! 최종 점수: " + finalScore);
        
        // 게임이 정상 종료되었음을 표시 (승패가 결정됨)
        gameEndedNormally = true;
        
        // 타이머 정지
        if (gameTimer != null) {
            gameTimer.stop();
        }
        
        // 게임 종료 시 양쪽 게임 모두 중지
        try {
            if (myGameController != null) {
                myGameController.stop();
            }
            if (remoteGameController != null) {
                remoteGameController.stop();
            }
        } catch (Exception e) {
            System.err.println("게임 중지 실패: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> {
            String message;
            if (isLocal) {
                message = "패배!\n내 점수: " + myScore + "\n상대방 점수: " + remoteScore;
            } else {
                message = "승리!\n내 점수: " + myScore + "\n상대방 점수: " + remoteScore;
            }
            
            JOptionPane.showMessageDialog(this, message, "게임 종료", JOptionPane.INFORMATION_MESSAGE);
            cleanupResources();
            dispose();
            new p2p.P2PMenuFrame();
        });
    }
    
    /**
     * 네트워크 상태 모니터링 시작
     * 1초마다 ConnectionMonitor의 상태를 확인하여 UI 업데이트
     */
    private void startNetworkStatusMonitoring() {
        Timer statusTimer = new Timer(1000, e -> {
            if (networkManager == null || networkManager.getConnectionMonitor() == null) {
                return;
            }
            
            network.ConnectionMonitor monitor = networkManager.getConnectionMonitor();
            network.LatencyMonitor latencyMonitor = monitor.getLatencyMonitor();
            network.ConnectionState state = monitor.getCurrentState();
            
            long avgLatency = latencyMonitor.getAverageLatency();
            String statusText;
            Color statusColor;
            
            switch (state) {
                case CONNECTED:
                    if (avgLatency > 0) {
                        statusText = "🟢 연결됨 (지연: " + avgLatency + "ms)";
                        statusColor = new Color(0, 200, 0);
                    } else {
                        statusText = "🟢 연결됨";
                        statusColor = new Color(0, 200, 0);
                    }
                    break;
                    
                case LAGGING:
                    statusText = "🟡 랙 걸림 (지연: " + avgLatency + "ms)";
                    statusColor = Color.ORANGE;
                    break;
                    
                case TIMEOUT:
                case DISCONNECTED:
                    statusText = "🔴 연결 끊김";
                    statusColor = Color.RED;
                    
                    // 연결 끊김 감지 시 자동으로 승리 처리
                    SwingUtilities.invokeLater(() -> {
                        handleOpponentDisconnected();
                    });
                    ((Timer)e.getSource()).stop();
                    break;
                    
                default:
                    statusText = "⚫ 연결 확인 중...";
                    statusColor = Color.GRAY;
                    break;
            }
            
            SwingUtilities.invokeLater(() -> {
                if (networkStatusLabel != null) {
                    networkStatusLabel.setText(statusText);
                    networkStatusLabel.setForeground(statusColor);
                }
            });
        });
        
        statusTimer.start();
        System.out.println("✅ 네트워크 상태 모니터링 시작");
    }
    
    /**
     * 상대방 연결 끊김 처리
     */
    private void handleOpponentDisconnected() {
        System.out.println("⚠️ 상대방 연결 끊김 감지");
        
        // 게임이 정상 종료된 경우 (승패가 이미 결정됨) - 아무것도 하지 않음
        if (gameEndedNormally) {
            System.out.println("ℹ️ 게임이 이미 정상 종료되어 연결 끊김 알림을 표시하지 않습니다.");
            return;
        }
        
        // 게임 중지
        if (myGameController != null) {
            myGameController.stop();
        }
        if (remoteGameController != null) {
            remoteGameController.stop();
        }
        
        // 승리 처리 (게임 중 상대방이 연결 끊김)
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                this,
                "상대방과의 연결이 끊어졌습니다.\n당신의 승리입니다!",
                "승리",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // 메뉴로 돌아가기
            cleanupResources();
            dispose();
            new p2p.P2PMenuFrame();
        });
    }
}
