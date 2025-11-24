package p2p;

import network.NetworkManager;
import network.messages.GameControlMessage;
import network.messages.GameControlMessage.ControlType;
import versus.VersusMode;

import javax.swing.*;
import java.awt.*;

/**
 * P2P 대전 대기실
 * 서버는 모드 선택 후 MODE_SELECT 전송, 클라이언트는 READY 전송.
 * 서버가 READY 수신 시 START_GAME 전송하여 양쪽 게임을 시작합니다.
 */
public class P2PWaitingRoom extends JFrame implements NetworkManager.GameControlListener {
    private final NetworkManager networkManager;
    private final boolean isServer;

    private JComboBox<VersusMode> modeSelector;
    private JButton sendModeButton;
    private JButton readyButton;
    private JLabel statusLabel;

    private VersusMode selectedMode = VersusMode.NORMAL;
    private final int myPlayerId;

    // 준비 상태
    private boolean serverReady = false;
    private boolean clientReady = false;

    public P2PWaitingRoom(NetworkManager networkManager, boolean isServer) {
        this.networkManager = networkManager;
        this.isServer = isServer;
        this.myPlayerId = isServer ? 1 : 2;

        setTitle("P2P 대기실");
        setSize(420, 280);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        setupUI();

        // 네트워크 제어 메시지 리스너 등록
        networkManager.addGameControlListener(this);

        setVisible(true);
    }

    @Override
    public void dispose() {
        try {
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
        center.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel(isServer ? "서버 대기실" : "클라이언트 대기실", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Dialog", Font.BOLD, 18));
        center.add(title);
        center.add(Box.createRigidArea(new Dimension(0, 12)));

        if (isServer) {
            modeSelector = new JComboBox<>(VersusMode.values());
            modeSelector.setSelectedItem(VersusMode.NORMAL);
            modeSelector.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(modeSelector);
            center.add(Box.createRigidArea(new Dimension(0, 8)));

            sendModeButton = new JButton("모드 전송 및 대기");
            sendModeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            sendModeButton.addActionListener(e -> onSendMode());
            center.add(sendModeButton);
            center.add(Box.createRigidArea(new Dimension(0, 8)));

            statusLabel = new JLabel("클라이언트 준비 상태: 대기 중", SwingConstants.CENTER);
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(statusLabel);
        } else {
            JLabel waiting = new JLabel("서버 모드 선택 대기 중...", SwingConstants.CENTER);
            waiting.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(waiting);
            center.add(Box.createRigidArea(new Dimension(0, 8)));

            readyButton = new JButton("준비 완료(READY)");
            readyButton.setEnabled(false);
            readyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            readyButton.addActionListener(e -> onReady());
            center.add(readyButton);

            statusLabel = new JLabel("모드 수신 전", SwingConstants.CENTER);
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(statusLabel);
        }

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton cancel = new JButton("취소");
        cancel.addActionListener(e -> onCancel());
        bottom.add(cancel);
        add(bottom, BorderLayout.SOUTH);
    }

    private void onSendMode() {
        selectedMode = (VersusMode) modeSelector.getSelectedItem();
        statusLabel.setText("MODE_SELECT 전송: " + selectedMode);
        // 서버가 MODE_SELECT 전송 시 자신의 준비 상태를 true로 설정
        serverReady = true;

        GameControlMessage msg = new GameControlMessage(ControlType.MODE_SELECT, selectedMode, Integer.valueOf(myPlayerId), null);
        boolean ok = networkManager.sendMessage(msg);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "모드 전송 실패", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 서버는 MODE_SELECT 전송 후 클라이언트의 READY를 대기
        sendModeButton.setEnabled(false);
        statusLabel.setText("모드 전송됨. 클라이언트 READY 대기 중...");
    }

    private void onReady() {
        // 클라이언트가 READY 전송
        GameControlMessage msg = new GameControlMessage(ControlType.READY, null, Integer.valueOf(myPlayerId), null);
        boolean ok = networkManager.sendMessage(msg);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "준비 메시지 전송 실패", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        readyButton.setEnabled(false);
        statusLabel.setText("READY 전송됨. 서버 대기 중...");
    }

    private void onCancel() {
        networkManager.removeGameControlListener(this);
        networkManager.disconnect();
        dispose();
        new P2PMenuFrame();
    }

    /**
     * GameControlMessage 수신 처리
     */
    @Override
    public void onControlMessage(GameControlMessage message) {
        ControlType type = message.getControlType();
        System.out.println("[P2PWaitingRoom] 수신: " + type);

        SwingUtilities.invokeLater(() -> {
            switch (type) {
                case MODE_SELECT:
                    // 클라이언트는 모드 정보를 받고 Ready 활성화
                    if (!isServer) {
                        VersusMode mode = message.getMode();
                        statusLabel.setText("모드 수신: " + mode);
                        readyButton.setEnabled(true);
                    }
                    break;

                case READY:
                    // 서버는 클라이언트 Ready를 받으면 준비 상태를 기록하고 모두 준비 시 START_GAME 전송
                    if (isServer) {
                        statusLabel.setText("클라이언트가 READY 함. START_GAME 조건 확인 중...");
                        clientReady = true;

                        if (serverReady && clientReady) {
                            statusLabel.setText("모든 플레이어 준비됨. START_GAME 전송 중...");
                            GameControlMessage startMsg = new GameControlMessage(ControlType.START_GAME, selectedMode, Integer.valueOf(myPlayerId), null);
                            boolean ok = networkManager.sendMessage(startMsg);
                            if (!ok) {
                                JOptionPane.showMessageDialog(this, "START_GAME 전송 실패", "오류", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            // 서버도 게임 시작
                            startP2PGame(selectedMode);
                        }
                    }
                    break;

                case START_GAME:
                    // 클라이언트는 START_GAME을 받으면 게임 시작
                    if (!isServer) {
                        VersusMode mode = message.getMode();
                        startP2PGame(mode != null ? mode : VersusMode.NORMAL);
                    }
                    break;

                default:
                    System.out.println("Unhandled control type: " + type);
            }
        });
    }

    private void startP2PGame(VersusMode mode) {
        // 대기실 종료 후 P2PVersusFrameBoard로 이동
        networkManager.removeGameControlListener(this);
        dispose();
        P2PVersusFrameBoard board = new P2PVersusFrameBoard(networkManager, mode, 0);
        // 서버는 START_GAME 메시지 수신에 의존하지 않고 직접 게임 시작을 요청
        if (isServer) {
            board.requestStart();
        }
    }
}
