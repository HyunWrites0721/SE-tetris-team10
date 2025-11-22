package network;

import network.messages.NetworkMessage;
import network.messages.MessageType;

/**
 * 네트워크 총괄 관리자
 * ConnectionManager, MessageSender, MessageReceiver, ConnectionMonitor를 통합 관리합니다.
 */
public class NetworkManager {
    private ConnectionManager connectionManager;
    private MessageSender messageSender;
    private MessageReceiver messageReceiver;
    private ConnectionMonitor connectionMonitor;
    
    private NetworkRole role;
    private ConnectionState state;
    
    public NetworkManager() {
        this.connectionManager = new ConnectionManager();
        this.state = ConnectionState.DISCONNECTED;
    }
    
    /**
     * 서버로 시작
     */
    public void startAsServer(int port) throws ConnectionException {
        role = NetworkRole.SERVER;
        state = ConnectionState.CONNECTING;
        
        System.out.println("서버 시작 중... 포트: " + port);
        connectionManager.startServer(port);
        
        initializeThreads();
        state = ConnectionState.CONNECTED;
        
        System.out.println("서버 준비 완료");
    }
    
    /**
     * 클라이언트로 연결
     */
    public void connectAsClient(String host, int port) throws ConnectionException {
        role = NetworkRole.CLIENT;
        state = ConnectionState.CONNECTING;
        
        System.out.println("서버에 연결 중... " + host + ":" + port);
        connectionManager.connectToServer(host, port);
        
        initializeThreads();
        state = ConnectionState.CONNECTED;
        
        System.out.println("서버 연결 완료");
    }
    
    /**
     * 송수신 스레드 및 모니터 초기화
     */
    private void initializeThreads() {
        // MessageSender 시작
        messageSender = new MessageSender(connectionManager.getOutputStream());
        messageSender.start();
        
        // MessageReceiver 시작
        messageReceiver = new MessageReceiver(connectionManager.getInputStream());
        messageReceiver.addMessageListener(new InternalMessageListener());
        messageReceiver.start();
        
        // ConnectionMonitor 시작
        connectionMonitor = new ConnectionMonitor(messageSender);
        connectionMonitor.setStateListener(newState -> {
            state = newState;
            System.out.println("네트워크 상태: " + newState);
            
            // 타임아웃 시 연결 종료
            if (newState == ConnectionState.TIMEOUT) {
                System.err.println("연결 타임아웃! 연결을 종료합니다.");
                disconnect();
            }
        });
        connectionMonitor.start();
    }
    
    /**
     * 메시지 전송
     */
    public boolean sendMessage(NetworkMessage message) {
        if (messageSender == null) {
            System.err.println("MessageSender가 초기화되지 않았습니다.");
            return false;
        }
        return messageSender.sendMessage(message);
    }
    
    /**
     * 메시지 리스너 등록
     */
    public void addMessageListener(MessageReceiver.MessageListener listener) {
        if (messageReceiver != null) {
            messageReceiver.addMessageListener(listener);
        }
    }
    
    /**
     * 메시지 리스너 제거
     */
    public void removeMessageListener(MessageReceiver.MessageListener listener) {
        if (messageReceiver != null) {
            messageReceiver.removeMessageListener(listener);
        }
    }
    
    /**
     * 연결 종료
     */
    public void disconnect() {
        System.out.println("네트워크 연결 종료 중...");
        
        state = ConnectionState.DISCONNECTED;
        
        // 모든 스레드 종료
        if (connectionMonitor != null) {
            connectionMonitor.shutdown();
        }
        if (messageSender != null) {
            messageSender.shutdown();
        }
        if (messageReceiver != null) {
            messageReceiver.shutdown();
        }
        
        // 연결 종료
        connectionManager.disconnect();
        
        System.out.println("네트워크 연결 종료 완료");
    }
    
    // ===== Getter 메서드 =====
    
    public NetworkRole getRole() {
        return role;
    }
    
    public ConnectionState getState() {
        return state;
    }
    
    public String getLocalAddress() {
        return connectionManager.getLocalAddress();
    }
    
    public int getLocalPort() {
        return connectionManager.getLocalPort();
    }
    
    public String getRemoteAddress() {
        return connectionManager.getRemoteAddress();
    }
    
    public int getRemotePort() {
        return connectionManager.getRemotePort();
    }
    
    public boolean isConnected() {
        return connectionManager.isConnected();
    }
    
    /**
     * 내부 메시지 리스너 (Heartbeat 처리)
     */
    private class InternalMessageListener implements MessageReceiver.MessageListener {
        @Override
        public void onMessageReceived(NetworkMessage message) {
            // Heartbeat 메시지 처리
            if (message.getType() == MessageType.HEARTBEAT) {
                connectionMonitor.onHeartbeatReceived();
            }
        }
        
        @Override
        public void onConnectionLost() {
            System.err.println("연결이 끊어졌습니다!");
            state = ConnectionState.DISCONNECTED;
            disconnect();
        }
    }
}
