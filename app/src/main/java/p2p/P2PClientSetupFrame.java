package p2p;

import javax.swing.*;
import start.StartFrame;
import network.*;
import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;

/**
 * P2P 클라이언트 설정 화면
 * 서버 IP와 포트를 입력하고 연결하는 화면
 */
public class P2PClientSetupFrame extends JFrame {
    private double screenRatio;
    private NetworkManager networkManager;
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    private static final String PREF_KEY_LAST_IP = "lastServerIP";
    private Preferences prefs;
    
    public P2PClientSetupFrame() {
        screenRatio = StartFrame.screenRatio;
        prefs = Preferences.userNodeForPackage(P2PClientSetupFrame.class);
        
        // 창 설정
        setTitle("서버에 연결");
        setSize((int)(500 * screenRatio), (int)(450 * screenRatio));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // 모든 창이 닫히면 프로세스 종료
        util.WindowManager.addAutoExitListener(this);
        
        // 제목
        JLabel titleLabel = new JLabel("서버에 연결", SwingConstants.CENTER);
        titleLabel.setFont(settings.FontManager.getKoreanFont(Font.BOLD, (int)(28 * screenRatio)));
        titleLabel.setBorder(BorderFactory.createEmptyBorder((int)(20 * screenRatio), 0, (int)(20 * screenRatio), 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // 중앙 패널
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder((int)(20 * screenRatio), (int)(40 * screenRatio), (int)(20 * screenRatio), (int)(40 * screenRatio)));
        
        // IP 주소 입력
        JLabel ipLabel = new JLabel("서버 IP 주소:", SwingConstants.LEFT);
        ipLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14 * screenRatio)));
        ipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(ipLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(5 * screenRatio))));
        
        ipField = new JTextField(getLastServerIP());
        ipField.setFont(new Font("Monospaced", Font.PLAIN, (int)(14 * screenRatio)));
        ipField.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)(40 * screenRatio)));
        ipField.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(ipField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(20 * screenRatio))));
        
        // 포트 번호 입력
        JLabel portLabel = new JLabel("포트 번호:", SwingConstants.LEFT);
        portLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14 * screenRatio)));
        portLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(portLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(5 * screenRatio))));
        
        portField = new JTextField(String.valueOf(NetworkConfig.DEFAULT_PORT));
        portField.setFont(new Font("Monospaced", Font.PLAIN, (int)(14 * screenRatio)));
        portField.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)(40 * screenRatio)));
        portField.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(portField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(20 * screenRatio))));
        
        // 최근 접속 IP 표시
        String lastIP = getLastServerIP();
        if (!lastIP.isEmpty() && !lastIP.equals("127.0.0.1")) {
            JLabel lastIPLabel = new JLabel("최근 접속: " + lastIP);
            lastIPLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(11 * screenRatio)));
            lastIPLabel.setForeground(Color.GRAY);
            lastIPLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerPanel.add(lastIPLabel);
            centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(15 * screenRatio))));
        }
        
        // 상태 표시
        statusLabel = new JLabel("서버 정보를 입력하고 '연결' 버튼을 누르세요.", SwingConstants.CENTER);
        statusLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(12 * screenRatio)));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(statusLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, (int)(10 * screenRatio))));
        
        // 안내 문구
        JLabel infoLabel = new JLabel("<html><center>서버 호스트로부터 전달받은<br>IP 주소와 포트를 입력하세요.</center></html>");
        infoLabel.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(11 * screenRatio)));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(infoLabel);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // 하단 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, (int)(10 * screenRatio), (int)(10 * screenRatio)));
        
        connectButton = new JButton("연결");
        connectButton.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14 * screenRatio)));
        connectButton.setPreferredSize(new Dimension((int)(120 * screenRatio), (int)(40 * screenRatio)));
        connectButton.addActionListener(e -> connectToServer());
        buttonPanel.add(connectButton);
        
        cancelButton = new JButton("취소");
        cancelButton.setFont(settings.FontManager.getKoreanFont(Font.PLAIN, (int)(14 * screenRatio)));
        cancelButton.setPreferredSize(new Dimension((int)(120 * screenRatio), (int)(40 * screenRatio)));
        cancelButton.addActionListener(e -> {
            new P2PMenuFrame();
            dispose();
        });
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Enter 키로 연결
        ipField.addActionListener(e -> portField.requestFocus());
        portField.addActionListener(e -> connectToServer());
        
        setVisible(true);
        ipField.requestFocus();
    }
    
    /**
     * 서버에 연결
     */
    private void connectToServer() {
        String ip = ipField.getText().trim();
        String portStr = portField.getText().trim();
        
        // 입력 검증
        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "IP 주소를 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            ipField.requestFocus();
            return;
        }
        
        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "올바른 포트 번호를 입력하세요. (1-65535)", "입력 오류", JOptionPane.WARNING_MESSAGE);
            portField.requestFocus();
            return;
        }
        
        // 버튼 비활성화
        connectButton.setEnabled(false);
        ipField.setEnabled(false);
        portField.setEnabled(false);
        statusLabel.setText("서버에 연결 중...");
        statusLabel.setForeground(Color.BLUE);
        
        // 백그라운드 스레드에서 연결
        new Thread(() -> {
            try {
                networkManager = new NetworkManager();
                networkManager.connectAsClient(ip, port);
                
                // 최근 IP 저장
                saveLastServerIP(ip);
                
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("연결 성공!");
                    statusLabel.setForeground(new Color(0, 128, 0));
                    
                    // 연결 완료 다이얼로그
                    new P2PConnectionDialog(this, networkManager, false);
                });
                
            } catch (ConnectionException e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("연결 실패: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                    connectButton.setEnabled(true);
                    ipField.setEnabled(true);
                    portField.setEnabled(true);
                    
                    JOptionPane.showMessageDialog(
                        this,
                        "서버에 연결할 수 없습니다.\n" + e.getMessage(),
                        "연결 오류",
                        JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        }).start();
    }
    
    /**
     * 최근 접속 IP 가져오기
     */
    private String getLastServerIP() {
        return prefs.get(PREF_KEY_LAST_IP, "127.0.0.1");
    }
    
    /**
     * 최근 접속 IP 저장
     */
    private void saveLastServerIP(String ip) {
        prefs.put(PREF_KEY_LAST_IP, ip);
    }
}
