package network;

import network.messages.NetworkMessage;
import network.messages.MessageType;

/**
 * 연결 상태 모니터링 스레드
 * Heartbeat를 주기적으로 전송하고 응답을 확인하여 연결 상태를 관리합니다.
 */
public class ConnectionMonitor extends Thread {
    private final MessageSender sender;
    private final LatencyMonitor latencyMonitor;
    private volatile boolean running = true;
    private volatile long lastHeartbeatReceived;
    private volatile long lastHeartbeatSent;  // RTT 측정용
    private volatile ConnectionState currentState;
    private ConnectionStateListener stateListener;
    
    public ConnectionMonitor(MessageSender sender) {
        this.sender = sender;
        this.latencyMonitor = new LatencyMonitor();
        this.lastHeartbeatReceived = System.currentTimeMillis();
        this.lastHeartbeatSent = System.currentTimeMillis();
        this.currentState = ConnectionState.CONNECTED;
        setDaemon(true);
        setName("ConnectionMonitor-Thread");
    }
    
    /**
     * 상태 변경 리스너 설정
     */
    public void setStateListener(ConnectionStateListener listener) {
        this.stateListener = listener;
    }
    
    /**
     * Heartbeat 수신 시 호출
     */
    public void onHeartbeatReceived() {
        long receivedTime = System.currentTimeMillis();
        lastHeartbeatReceived = receivedTime;
        
        // RTT(Round Trip Time) 계산
        long rtt = receivedTime - lastHeartbeatSent;
        latencyMonitor.recordLatency(rtt);
        
        // 랙 상태 확인
        if (latencyMonitor.isLagging()) {
            updateState(ConnectionState.LAGGING);
        } else {
            updateState(ConnectionState.CONNECTED);
        }
    }
    
    @Override
    public void run() {
        System.out.println("ConnectionMonitor 시작");
        
        while (running) {
            try {
                // Heartbeat 전송
                sendHeartbeat();
                
                // 연결 상태 확인
                checkConnectionState();
                
                // 다음 체크까지 대기
                Thread.sleep(NetworkConfig.HEARTBEAT_INTERVAL);
                
            } catch (InterruptedException e) {
                System.out.println("ConnectionMonitor 중단됨");
                break;
            } catch (Exception e) {
                System.err.println("ConnectionMonitor 오류: " + e.getMessage());
            }
        }
        
        System.out.println("ConnectionMonitor 종료");
    }
    
    /**
     * Heartbeat 메시지 전송
     */
    private void sendHeartbeat() {
        lastHeartbeatSent = System.currentTimeMillis();
        HeartbeatMessage heartbeat = new HeartbeatMessage();
        sender.sendMessage(heartbeat);
    }
    
    /**
     * 연결 상태 확인
     */
    private void checkConnectionState() {
        long timeSinceLastHeartbeat = System.currentTimeMillis() - lastHeartbeatReceived;
        
        if (timeSinceLastHeartbeat > NetworkConfig.HEARTBEAT_TIMEOUT) {
            // 타임아웃
            updateState(ConnectionState.TIMEOUT);
        } else if (timeSinceLastHeartbeat > NetworkConfig.LAG_THRESHOLD) {
            // 지연 중
            updateState(ConnectionState.LAGGING);
        } else {
            // 정상
            updateState(ConnectionState.CONNECTED);
        }
    }
    
    /**
     * 상태 업데이트
     */
    private void updateState(ConnectionState newState) {
        if (currentState != newState) {
            ConnectionState oldState = currentState;
            currentState = newState;
            
            System.out.println("연결 상태 변경: " + oldState + " → " + newState);
            
            // 리스너에게 알림
            if (stateListener != null) {
                stateListener.onStateChanged(newState);
            }
        }
    }
    
    /**
     * 모니터링 중지
     */
    public void shutdown() {
        running = false;
        interrupt();
    }
    
    /**
     * 현재 연결 상태 반환
     */
    public ConnectionState getCurrentState() {
        return currentState;
    }
    
    /**
     * LatencyMonitor 반환
     */
    public LatencyMonitor getLatencyMonitor() {
        return latencyMonitor;
    }
    
    /**
     * 연결 상태 리스너 인터페이스
     */
    public interface ConnectionStateListener {
        void onStateChanged(ConnectionState newState);
    }
    
    /**
     * Heartbeat 메시지 (내부 클래스)
     */
    private static class HeartbeatMessage extends NetworkMessage {
        private static final long serialVersionUID = 1L;
        
        public HeartbeatMessage() {
            super(MessageType.HEARTBEAT);
        }
    }
}
