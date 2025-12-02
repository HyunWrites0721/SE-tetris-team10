package p2p;

import network.NetworkManager;
import network.messages.GameControlMessage;
import network.messages.GameControlMessage.ControlType;
import versus.VersusMode;

import javax.swing.*;
import java.awt.*;

/**
 * P2P 대전 대기실 (최종 개선 버전)
 * 
 * 새로운 프로토콜:
 * 1. 연결 즉시 양쪽 모두 대기실 자동 진입
 * 2. 서버: MODE_SELECT 전송 (초기 모드)
 * 3. 클라이언트: VERSION_CHECK 응답
 * 4. 클라이언트: READY/READY_CANCEL 토글 가능
 * 5. 서버: READY 수신 후 3초 대기 후 START_REQUEST 활성화
 * 6. 양쪽: 게임 시작
 * 
 * - 서버는 언제든지 모드 변경 가능 (MODE_CHANGED 전송)
 * - 클라이언트는 MODE_CHANGED 수신 시 자동 준비 해제
 * - 클라이언트는 언제든지 준비/취소 토글 가능
 */
public class P2PWaitingRoom extends JFrame implements NetworkManager.GameControlListener {
    private static final String GAME_VERSION = "1.0.0";
    
    private final NetworkManager networkManager;
    private final boolean isServer;
    private final int myPlayerId;

    // UI Components
    private JComboBox<VersusMode> modeSelector;
    private JButton changeModeButton;
    private JButton startGameButton;
    private JButton readyButton;
    private JLabel statusLabel;
    private JLabel modeDisplayLabel;

    // State
    private VersusMode currentMode = VersusMode.NORMAL;
    private boolean isReady = false;
    private boolean clientVersionChecked = false;
    private boolean clientReady = false;
    
    // Timers
    private Timer startDelayTimer = null;
    private int remainingSeconds = 0;

    public P2PWaitingRoom(NetworkManager networkManager, boolean isServer) {
        this.networkManager = networkManager;
        this.isServer = isServer;
        this.myPlayerId = isServer ? 1 : 2;

        setTitle("P2P 대기실");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        setupUI();

        // 네트워크 제어 메시지 리스너 등록
        networkManager.addGameControlListener(this);

        // 서버는 연결 즉시 초기 모드 전송
        if (isServer) {
            SwingUtilities.invokeLater(() -> {
                sendModeSelect(currentMode);
            });
        }

        setVisible(true);
    }

    @Override
    public void dispose() {
        try {
            // 타이머 정리
            if (startDelayTimer != null) {
                startDelayTimer.stop();
                startDelayTimer = null;
            }
            
            if (networkManager != null) {
                networkManager.removeGameControlListener(this);
            }
        } catch (Exception e) {
            System.err.println("Error removing GameControlListener in P2PWaitingRoom: " + e.getMessage());
        }
        super.dispose();
    }

    private void setupUI() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 타이틀
        JLabel title = new JLabel(isServer ? "서버 대기실" : "클라이언트 대기실", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Dialog", Font.BOLD, 20));
        center.add(title);
        center.add(Box.createRigidArea(new Dimension(0, 20)));

        if (isServer) {
            // === 서버 UI ===
            
            // 모드 선택기
            JLabel modeLabel = new JLabel("게임 모드:", SwingConstants.CENTER);
            modeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(modeLabel);
            center.add(Box.createRigidArea(new Dimension(0, 5)));
            
            modeSelector = new JComboBox<>(VersusMode.values());
            modeSelector.setSelectedItem(VersusMode.NORMAL);
            modeSelector.setMaximumSize(new Dimension(300, 30));
            modeSelector.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(modeSelector);
            center.add(Box.createRigidArea(new Dimension(0, 10)));

            // 모드 변경 버튼
            changeModeButton = new JButton("모드 전송 및 대기");
            changeModeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            changeModeButton.addActionListener(e -> onChangeModeClick());
            center.add(changeModeButton);
            center.add(Box.createRigidArea(new Dimension(0, 15)));

            // 상태 표시
            statusLabel = new JLabel("클라이언트 준비 상태: 대기 중", SwingConstants.CENTER);
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            statusLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
            center.add(statusLabel);
            center.add(Box.createRigidArea(new Dimension(0, 10)));

            // 게임 시작 버튼
            startGameButton = new JButton("게임 시작");
            startGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            startGameButton.setEnabled(false);
            startGameButton.addActionListener(e -> onStartGameClick());
            center.add(startGameButton);

        } else {
            // === 클라이언트 UI ===
            
            // 현재 모드 표시
            JLabel modeLabel = new JLabel("현재 모드:", SwingConstants.CENTER);
            modeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(modeLabel);
            center.add(Box.createRigidArea(new Dimension(0, 5)));
            
            modeDisplayLabel = new JLabel("NORMAL", SwingConstants.CENTER);
            modeDisplayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            modeDisplayLabel.setFont(new Font("Dialog", Font.BOLD, 16));
            modeDisplayLabel.setForeground(new Color(0, 100, 200));
            center.add(modeDisplayLabel);
            center.add(Box.createRigidArea(new Dimension(0, 15)));

            // 상태 표시
            statusLabel = new JLabel("모드 전송됨. 클라이언트 READY 대기 중...", SwingConstants.CENTER);
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            statusLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
            center.add(statusLabel);
            center.add(Box.createRigidArea(new Dimension(0, 15)));

            // 준비 버튼
            readyButton = new JButton("준비 완료");
            readyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            readyButton.setEnabled(false);  // 모드 수신 후 활성화
            readyButton.addActionListener(e -> onReadyClick());
            center.add(readyButton);
        }

        add(center, BorderLayout.CENTER);

        // 하단 버튼
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton cancel = new JButton("취소");
        cancel.addActionListener(e -> onCancel());
        bottom.add(cancel);
        add(bottom, BorderLayout.SOUTH);
    }

    // ==================== 서버 액션 ====================
    
    /**
     * 서버: 모드 변경 버튼 클릭
     */
    private void onChangeModeClick() {
        VersusMode newMode = (VersusMode) modeSelector.getSelectedItem();
        
        if (newMode == currentMode) {
            // 같은 모드면 단순히 MODE_SELECT 재전송
            sendModeSelect(newMode);
            statusLabel.setText("모드 재전송: " + newMode);
        } else {
            // 모드가 변경됨 → MODE_CHANGED 전송
            currentMode = newMode;
            sendModeChanged(newMode);
            
            // 클라이언트 준비 상태 초기화
            clientReady = false;
            clientVersionChecked = false;
            startGameButton.setEnabled(false);
            
            statusLabel.setText("모드 변경됨: " + newMode + " (클라이언트 준비 대기)");
        }
        
        changeModeButton.setEnabled(false);  // 중복 클릭 방지
        Timer timer = new Timer(1000, e -> changeModeButton.setEnabled(true));
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * 서버: 게임 시작 버튼 클릭
     */
    private void onStartGameClick() {
        if (!clientVersionChecked || !clientReady) {
            JOptionPane.showMessageDialog(this, 
                "클라이언트가 아직 준비되지 않았습니다.", 
                "게임 시작 불가", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        statusLabel.setText("START_REQUEST 전송 중...");
        
        // START_REQUEST 전송
        GameControlMessage msg = new GameControlMessage(
            ControlType.START_REQUEST, 
            currentMode, 
            myPlayerId, 
            null
        );
        
        boolean ok = networkManager.sendMessage(msg);
        if (!ok) {
            JOptionPane.showMessageDialog(this, 
                "START_REQUEST 전송 실패", 
                "오류", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 서버도 즉시 게임 시작
        startP2PGame(currentMode);
    }
    
    // ==================== 클라이언트 액션 ====================
    
    /**
     * 클라이언트: 준비/취소 버튼 클릭 (토글)
     */
    private void onReadyClick() {
        if (isReady) {
            // 준비 취소
            isReady = false;
            readyButton.setText("준비 완료");
            statusLabel.setText("준비 취소됨");
            
            // READY_CANCEL 전송
            GameControlMessage msg = new GameControlMessage(
                ControlType.READY_CANCEL, 
                null, 
                myPlayerId, 
                null
            );
            
            boolean ok = networkManager.sendMessage(msg);
            if (!ok) {
                JOptionPane.showMessageDialog(this, 
                    "준비 취소 메시지 전송 실패", 
                    "오류", 
                    JOptionPane.ERROR_MESSAGE);
                // 실패 시 상태 복원
                isReady = true;
                readyButton.setText("준비 취소");
                statusLabel.setText("준비 완료! 서버의 시작 대기 중...");
            }
        } else {
            // 준비 완료
            isReady = true;
            readyButton.setText("준비 취소");
            statusLabel.setText("준비 완료! 서버의 시작 대기 중...");
            
            // READY 전송
            GameControlMessage msg = new GameControlMessage(
                ControlType.READY, 
                null, 
                myPlayerId, 
                null
            );
            
            boolean ok = networkManager.sendMessage(msg);
            if (!ok) {
                JOptionPane.showMessageDialog(this, 
                    "READY 전송 실패", 
                    "오류", 
                    JOptionPane.ERROR_MESSAGE);
                // 실패 시 상태 복원
                isReady = false;
                readyButton.setText("준비 완료");
                statusLabel.setText("모드 수신: " + currentMode);
            }
        }
    }
    
    // ==================== 공통 액션 ====================
    
    private void onCancel() {
        networkManager.removeGameControlListener(this);
        networkManager.disconnect();
        dispose();
        new P2PMenuFrame();
    }

    // ==================== 네트워크 메시지 핸들러 ====================
    
    @Override
    public void onControlMessage(GameControlMessage message) {
        ControlType type = message.getControlType();
        System.out.println("[P2PWaitingRoom] 수신: " + type + " (isServer=" + isServer + ")");

        SwingUtilities.invokeLater(() -> {
            switch (type) {
                case MODE_SELECT:
                    handleModeSelect(message);
                    break;
                    
                case MODE_CHANGED:
                    handleModeChanged(message);
                    break;
                    
                case VERSION_CHECK:
                    handleVersionCheck(message);
                    break;
                    
                case READY:
                    handleReady(message);
                    break;
                    
                case READY_CANCEL:
                    handleReadyCancel(message);
                    break;
                    
                case START_REQUEST:
                    handleStartRequest(message);
                    break;
                    
                default:
                    System.out.println("[P2PWaitingRoom] Unhandled control type: " + type);
            }
        });
    }
    
    /**
     * MODE_SELECT 수신 처리 (클라이언트만)
     */
    private void handleModeSelect(GameControlMessage message) {
        if (isServer) return;
        
        VersusMode mode = message.getMode();
        if (mode == null) mode = VersusMode.NORMAL;
        
        currentMode = mode;
        modeDisplayLabel.setText(mode.toString());
        statusLabel.setText("모드 수신: " + mode);
        
        // 버전 확인 응답 전송
        sendVersionCheck();
        
        // 준비 버튼 활성화
        readyButton.setEnabled(true);
    }
    
    /**
     * MODE_CHANGED 수신 처리 (클라이언트만)
     */
    private void handleModeChanged(GameControlMessage message) {
        if (isServer) return;
        
        VersusMode mode = message.getMode();
        if (mode == null) mode = VersusMode.NORMAL;
        
        currentMode = mode;
        modeDisplayLabel.setText(mode.toString());
        
        // 준비 상태 해제
        if (isReady) {
            isReady = false;
            readyButton.setText("준비 완료");
            readyButton.setEnabled(true);
            statusLabel.setText("⚠️ 모드 변경됨: " + mode + " (다시 준비 필요)");
        } else {
            statusLabel.setText("모드 변경됨: " + mode);
        }
        
        // 버전 체크 재전송
        sendVersionCheck();
    }
    
    /**
     * VERSION_CHECK 수신 처리 (서버만)
     */
    private void handleVersionCheck(GameControlMessage message) {
        if (!isServer) return;
        
        String clientVersion = message.getInfo();
        System.out.println("[P2PWaitingRoom] 클라이언트 버전: " + clientVersion);
        
        clientVersionChecked = true;
        updateServerStatus();
    }
    
    /**
     * READY 수신 처리 (서버만)
     */
    private void handleReady(GameControlMessage message) {
        if (!isServer) return;
        
        clientReady = true;
        
        // 3초 대기 타이머 시작
        startReadyTimer();
        updateServerStatus();
    }
    
    /**
     * READY_CANCEL 수신 처리 (서버만)
     */
    private void handleReadyCancel(GameControlMessage message) {
        if (!isServer) return;
        
        clientReady = false;
        
        // 타이머 취소
        if (startDelayTimer != null) {
            startDelayTimer.stop();
            startDelayTimer = null;
        }
        
        // 게임 시작 버튼 비활성화
        startGameButton.setEnabled(false);
        
        updateServerStatus();
    }
    
    /**
     * 서버: 준비 완료 3초 대기 타이머 시작
     */
    private void startReadyTimer() {
        // 기존 타이머가 있으면 중지
        if (startDelayTimer != null) {
            startDelayTimer.stop();
        }
        
        remainingSeconds = 3;
        statusLabel.setText("클라이언트 준비 완료! (" + remainingSeconds + "초 후 시작 가능)");
        
        startDelayTimer = new Timer(1000, e -> {
            remainingSeconds--;
            
            if (remainingSeconds > 0) {
                statusLabel.setText("클라이언트 준비 완료! (" + remainingSeconds + "초 후 시작 가능)");
            } else {
                // 3초 경과 - 게임 시작 버튼 활성화
                startDelayTimer.stop();
                startDelayTimer = null;
                startGameButton.setEnabled(true);
                statusLabel.setText("클라이언트 상태: ✅ 준비 완료! (게임 시작 가능)");
            }
        });
        startDelayTimer.start();
    }
    
    /**
     * START_REQUEST 수신 처리 (클라이언트만)
     */
    private void handleStartRequest(GameControlMessage message) {
        if (isServer) return;
        
        VersusMode mode = message.getMode();
        if (mode == null) mode = currentMode;
        
        statusLabel.setText("게임 시작 요청 수신!");
        
        // 클라이언트도 게임 시작
        startP2PGame(mode);
    }
    
    // ==================== 메시지 전송 헬퍼 ====================
    
    /**
     * 서버: MODE_SELECT 전송
     */
    private void sendModeSelect(VersusMode mode) {
        GameControlMessage msg = new GameControlMessage(
            ControlType.MODE_SELECT, 
            mode, 
            myPlayerId, 
            null
        );
        
        boolean ok = networkManager.sendMessage(msg);
        if (ok) {
            System.out.println("[서버] MODE_SELECT 전송: " + mode);
            statusLabel.setText("모드 전송됨: " + mode + " (클라이언트 대기 중)");
        } else {
            System.err.println("[서버] MODE_SELECT 전송 실패");
        }
    }
    
    /**
     * 서버: MODE_CHANGED 전송
     */
    private void sendModeChanged(VersusMode mode) {
        GameControlMessage msg = new GameControlMessage(
            ControlType.MODE_CHANGED, 
            mode, 
            myPlayerId, 
            null
        );
        
        boolean ok = networkManager.sendMessage(msg);
        if (ok) {
            System.out.println("[서버] MODE_CHANGED 전송: " + mode);
        } else {
            System.err.println("[서버] MODE_CHANGED 전송 실패");
        }
    }
    
    /**
     * 클라이언트: VERSION_CHECK 전송
     */
    private void sendVersionCheck() {
        GameControlMessage msg = new GameControlMessage(
            ControlType.VERSION_CHECK, 
            null, 
            myPlayerId, 
            GAME_VERSION
        );
        
        boolean ok = networkManager.sendMessage(msg);
        if (ok) {
            System.out.println("[클라이언트] VERSION_CHECK 전송: " + GAME_VERSION);
        } else {
            System.err.println("[클라이언트] VERSION_CHECK 전송 실패");
        }
    }
    
    // ==================== 상태 업데이트 ====================
    
    /**
     * 서버: 상태 업데이트 및 게임 시작 버튼 활성화 체크
     */
    private void updateServerStatus() {
        String status = "클라이언트 상태: ";
        
        if (!clientVersionChecked) {
            status += "버전 확인 대기";
            startGameButton.setEnabled(false);
        } else if (!clientReady) {
            status += "버전 확인 완료, 준비 대기";
            startGameButton.setEnabled(false);
        } else {
            // clientReady = true
            // 타이머가 실행 중이면 버튼 비활성화, 타이머가 없으면 활성화
            if (startDelayTimer != null && startDelayTimer.isRunning()) {
                status += "✅ 준비 완료! (대기 중...)";
                startGameButton.setEnabled(false);
            } else {
                status += "✅ 준비 완료!";
                startGameButton.setEnabled(true);
            }
        }
        
        // 타이머 실행 중이 아닐 때만 상태 업데이트
        if (startDelayTimer == null || !startDelayTimer.isRunning()) {
            statusLabel.setText(status);
        }
    }
    
    // ==================== 게임 시작 ====================
    
    private void startP2PGame(VersusMode mode) {
        System.out.println("[P2PWaitingRoom] 게임 시작: mode=" + mode + ", isServer=" + isServer);
        
        // 대기실 종료 후 P2PVersusFrameBoard로 이동
        networkManager.removeGameControlListener(this);
        dispose();
        
        P2PVersusFrameBoard board = new P2PVersusFrameBoard(networkManager, mode, 0);
        
        // 서버/클라이언트 모두 requestStart 호출
        board.requestStart();
    }
}

