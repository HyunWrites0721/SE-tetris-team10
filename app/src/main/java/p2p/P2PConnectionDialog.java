package p2p;

import javax.swing.*;
import network.NetworkManager;
import network.ConnectionState;
import start.StartFrame;
import java.awt.*;

/**
 * P2P 연결 진행 상태 다이얼로그
 * 연결 완료 후 게임 시작 대기 화면
 */
public class P2PConnectionDialog extends JDialog {
    private double screenRatio;
    private NetworkManager networkManager;
    private boolean isServer;
    private JLabel statusLabel;
    private JButton startButton;
    private JButton cancelButton;
    private Timer statusCheckTimer;
    
    public P2PConnectionDialog(JFrame parent, NetworkManager networkManager, boolean isServer) {
        super(parent, "연결 완료", true);
        
        this.screenRatio = StartFrame.screenRatio;
        this.networkManager = networkManager;
        this.isServer = isServer;
        
        // 다이얼로그 설정
        setSize((int)(400 * screenRatio), (int)(250 * screenRatio));
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // 중앙 패널
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder((int)(20 * screenRatio), (int)(20 * screenRatio), (int)(20 * screenRatio), (int)(20 * screenRatio)));
        
        // 연결 성공 아이콘
        JLabel iconLabel = new JLabel("✓", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Dialog", Font.BOLD, (int)(48 * screenRatio)));
        iconLabel.setForeground(new Color(0, 128, 0));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(iconLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(15 * screenRatio))));
        
        // 연결 정보
        String roleText = isServer ? "서버" : "클라이언트";
        JLabel roleLabel = new JLabel(roleText + " 연결 완료", SwingConstants.CENTER);
        roleLabel.setFont(settings.FontManager.getKoreanFont(Font.BOLD, (int)(18 * screenRatio)));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(roleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(10 * screenRatio))));
        
        // 상대방 정보
        String remoteInfo = networkManager.getRemoteAddress() + ":" + networkManager.getRemotePort();
        JLabel infoLabel = new JLabel("상대방: " + remoteInfo, SwingConstants.CENTER);
        infoLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(12 * screenRatio)));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(infoLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(15 * screenRatio))));
        
        // 상태 표시
        statusLabel = new JLabel("연결 상태: 정상", SwingConstants.CENTER);
        statusLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(11 * screenRatio)));
        statusLabel.setForeground(new Color(0, 128, 0));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(statusLabel);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // 하단 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, (int)(10 * screenRatio), (int)(10 * screenRatio)));
        
        startButton = new JButton("게임 시작");
        startButton.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14 * screenRatio)));
        startButton.setPreferredSize(new Dimension((int)(120 * screenRatio), (int)(40 * screenRatio)));
        startButton.addActionListener(e -> startGame());
        buttonPanel.add(startButton);
        
        cancelButton = new JButton("취소");
        cancelButton.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14 * screenRatio)));
        cancelButton.setPreferredSize(new Dimension((int)(120 * screenRatio), (int)(40 * screenRatio)));
        cancelButton.addActionListener(e -> cancelConnection());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 연결 상태 모니터링 시작
        startStatusMonitoring();
        
        setVisible(true);
    }
    
    /**
     * 연결 상태 모니터링 시작
     */
    private void startStatusMonitoring() {
        statusCheckTimer = new Timer(500, e -> {
            ConnectionState state = networkManager.getState();
            
            switch (state) {
                case CONNECTED:
                    statusLabel.setText("연결 상태: 정상");
                    statusLabel.setForeground(new Color(0, 128, 0));
                    startButton.setEnabled(true);
                    break;
                    
                case LAGGING:
                    statusLabel.setText("연결 상태: 지연 중 (렉)");
                    statusLabel.setForeground(Color.ORANGE);
                    startButton.setEnabled(true);
                    break;
                    
                case TIMEOUT:
                case DISCONNECTED:
                    statusLabel.setText("연결 상태: 끊김");
                    statusLabel.setForeground(Color.RED);
                    startButton.setEnabled(false);
                    
                    // 연결 끊김 알림
                    statusCheckTimer.stop();
                    JOptionPane.showMessageDialog(
                        this,
                        "연결이 끊어졌습니다.",
                        "연결 끊김",
                        JOptionPane.ERROR_MESSAGE
                    );
                    cancelConnection();
                    break;
                    
                default:
                    break;
            }
        });
        statusCheckTimer.start();
    }
    
    /**
     * 게임 시작
     */
    private void startGame() {
        if (statusCheckTimer != null) {
            statusCheckTimer.stop();
        }
        
        // P2P 대전 게임 시작
        dispose();
        if (getOwner() != null) {
            getOwner().dispose();
        }
        
        // P2PVersusFrameBoard 생성 (NORMAL 모드, 난이도 0)
        SwingUtilities.invokeLater(() -> {
            new P2PVersusFrameBoard(networkManager, versus.VersusMode.NORMAL, 0);
        });
    }
    
    /**
     * 연결 취소
     */
    private void cancelConnection() {
        if (statusCheckTimer != null) {
            statusCheckTimer.stop();
        }
        
        networkManager.disconnect();
        
        dispose();
        if (getOwner() != null) {
            getOwner().dispose();
        }
        new P2PMenuFrame();
    }
}
