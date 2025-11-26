package network;

import network.messages.NetworkMessage;
import network.messages.MessageType;
import network.messages.GameControlMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 네트워크 총괄 관리자
 * ConnectionManager, MessageSender, MessageReceiver, ConnectionMonitor를 통합 관리합니다.
 */
public class NetworkManager {
    private ConnectionManager connectionManager;
    private MessageSender messageSender;
    private MessageReceiver messageReceiver;
    private ConnectionMonitor connectionMonitor;
    private DisconnectionHandler disconnectionHandler;
    
    private NetworkRole role;
    private ConnectionState state;
    // GameControlMessage 리스너 목록
    private final List<GameControlListener> gameControlListeners = new ArrayList<>();
    
    public NetworkManager() {
        this.connectionManager = new ConnectionManager();
        this.state = ConnectionState.DISCONNECTED;
    }
    
    /**
     * DisconnectionHandler 설정
     * @param handler 연결 끊김 시 실행할 핸들러
     */
    public void setDisconnectionHandler(DisconnectionHandler handler) {
        this.disconnectionHandler = handler;
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
            
            // 타임아웃 시 연결 종료 및 핸들러 호출
            if (newState == ConnectionState.TIMEOUT) {
                System.err.println("연결 타임아웃! 연결을 종료합니다.");
                disconnect();
                
                // DisconnectionHandler 호출 (설정된 경우)
                if (disconnectionHandler != null && !disconnectionHandler.isHandled()) {
                    disconnectionHandler.handleTimeout(null);
                }
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
    
    public ConnectionMonitor getConnectionMonitor() {
        return connectionMonitor;
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
                return;
            }

            // 게임 제어 메시지 처리 (MODE_SELECT, READY, START_GAME 등)
            if (message.getType() == MessageType.GAME_CONTROL) {
                try {
                    GameControlMessage ctrl = (GameControlMessage) message;
                    notifyGameControlListeners(ctrl);
                } catch (Exception e) {
                    System.err.println("GameControlMessage 처리 실패: " + e.getMessage());
                }

                return;
            }
        }
        
        @Override
        public void onConnectionLost() {
            System.err.println("연결이 끊어졌습니다!");
            state = ConnectionState.DISCONNECTED;
            disconnect();
            
            // DisconnectionHandler 호출 (설정된 경우)
            if (disconnectionHandler != null && !disconnectionHandler.isHandled()) {
                disconnectionHandler.handleDisconnection("상대방과의 연결이 끊어졌습니다.", null);
            }
        }
    }

    /**
     * GameControlMessage 리스너 등록
     */
    public void addGameControlListener(GameControlListener listener) {
        synchronized (gameControlListeners) {
            gameControlListeners.add(listener);
        }
    }

    /**
     * GameControlMessage 리스너 제거
     */
    public void removeGameControlListener(GameControlListener listener) {
        synchronized (gameControlListeners) {
            gameControlListeners.remove(listener);
        }
    }

    /**
     * 등록된 리스너들에게 GameControlMessage 전달
     */
    private void notifyGameControlListeners(GameControlMessage msg) {
        List<GameControlListener> copy;
        synchronized (gameControlListeners) {
            copy = new ArrayList<>(gameControlListeners);
        }

        for (GameControlListener l : copy) {
            try {
                l.onControlMessage(msg);
            } catch (Exception e) {
                System.err.println("GameControlListener 처리 중 오류: " + e.getMessage());
            }
        }
    }

    /**
     * GameControlMessage 수신 리스너 인터페이스
     */
    public interface GameControlListener {
        void onControlMessage(GameControlMessage message);
    }
}
