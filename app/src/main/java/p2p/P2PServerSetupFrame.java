package p2p;

import javax.swing.*;
import start.StartFrame;
import network.*;
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;

/**
 * P2P 서버 설정 화면
 * 서버로 호스트하고 클라이언트 연결을 대기하는 화면
 */
public class P2PServerSetupFrame extends JFrame {
    private double screenRatio;
    private NetworkManager networkManager;
    private JLabel statusLabel;
    private JButton startButton;
    private JButton cancelButton;
    private int port;
    
    public P2PServerSetupFrame() {
        screenRatio = StartFrame.screenRatio;
        port = NetworkConfig.DEFAULT_PORT;
        
        // 창 설정
        setTitle("서버 호스트");
        setSize((int)(500 * screenRatio), (int)(450 * screenRatio));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // 창 닫기 이벤트 처리
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleCancel();
            }
        });
        
        // 제목
        JLabel titleLabel = new JLabel("서버 호스트", SwingConstants.CENTER);
        titleLabel.setFont(settings.FontManager.getKoreanFont(Font.BOLD, (int)(28 * screenRatio)));
        titleLabel.setBorder(BorderFactory.createEmptyBorder((int)(20 * screenRatio), 0, (int)(20 * screenRatio), 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // 중앙 패널
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder((int)(20 * screenRatio), (int)(40 * screenRatio), (int)(20 * screenRatio), (int)(40 * screenRatio)));
        
        // 로컬 IP 표시
        String localIP = getLocalIP();
        JLabel ipLabel = new JLabel("서버 IP 주소:", SwingConstants.LEFT);
        ipLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14 * screenRatio)));
        ipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(ipLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(5 * screenRatio))));
        
        JTextField ipField = new JTextField(localIP);
        ipField.setFont(new Font("Monospaced", Font.BOLD, (int)(16 * screenRatio)));
        ipField.setEditable(false);
        ipField.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)(40 * screenRatio)));
        ipField.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(ipField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(20 * screenRatio))));
        
        // 포트 번호 표시
        JLabel portLabel = new JLabel("포트 번호:", SwingConstants.LEFT);
        portLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14 * screenRatio)));
        portLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(portLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(5 * screenRatio))));
        
        JTextField portField = new JTextField(String.valueOf(port));
        portField.setFont(new Font("Monospaced", Font.BOLD, (int)(16 * screenRatio)));
        portField.setEditable(false);
        portField.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)(40 * screenRatio)));
        portField.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(portField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(20 * screenRatio))));
        
        // 상태 표시
        statusLabel = new JLabel("서버를 시작하려면 '시작' 버튼을 누르세요.", SwingConstants.CENTER);
        statusLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(12 * screenRatio)));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(statusLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(10 * screenRatio))));
        
        // 안내 문구
        JLabel infoLabel = new JLabel("<html><center>클라이언트가 위 IP와 포트로<br>접속할 수 있도록 알려주세요.</center></html>");
        infoLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(11 * screenRatio)));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(infoLabel);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // 하단 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, (int)(10 * screenRatio), (int)(10 * screenRatio)));
        
        startButton = new JButton("시작");
        startButton.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14 * screenRatio)));
        startButton.setPreferredSize(new Dimension((int)(120 * screenRatio), (int)(40 * screenRatio)));
        startButton.addActionListener(e -> startServer());
        buttonPanel.add(startButton);
        
        cancelButton = new JButton("취소");
        cancelButton.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14 * screenRatio)));
        cancelButton.setPreferredSize(new Dimension((int)(120 * screenRatio), (int)(40 * screenRatio)));
        cancelButton.addActionListener(e -> handleCancel());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    /**
     * 로컬 IP 주소 가져오기
     */
    private String getLocalIP() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
    
    /**
     * 서버 시작
     */
    private void startServer() {
        startButton.setEnabled(false);
        statusLabel.setText("서버 시작 중...");
        statusLabel.setForeground(Color.BLUE);
        
        // 백그라운드 스레드에서 서버 시작
        new Thread(() -> {
            try {
                networkManager = new NetworkManager();
                networkManager.startAsServer(port);
                
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("클라이언트 연결 대기 중...");
                    statusLabel.setForeground(new Color(0, 128, 0));
                });
                
                // 연결 대기 (별도 다이얼로그로 표시)
                SwingUtilities.invokeLater(() -> {
                    new P2PConnectionDialog(this, networkManager, true);
                });
                
            } catch (ConnectionException e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("서버 시작 실패: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                    startButton.setEnabled(true);
                    
                    JOptionPane.showMessageDialog(
                        this,
                        "서버를 시작할 수 없습니다.\n" + e.getMessage(),
                        "오류",
                        JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        }).start();
    }
    
    /**
     * 취소 처리
     */
    private void handleCancel() {
        if (networkManager != null) {
            networkManager.disconnect();
        }
        new P2PMenuFrame();
        dispose();
    }
}
