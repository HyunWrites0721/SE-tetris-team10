package network;

/**
 * 네트워크 설정 상수
 */
public class NetworkConfig {
    // 기본 포트 번호
    public static final int DEFAULT_PORT = 12345;
    
    // 타임아웃 설정
    public static final int CONNECTION_TIMEOUT = 60000;  // 60초 (연결 대기 시간)
    public static final int READ_TIMEOUT = 5000;         // 5초 (데이터 읽기 타임 아웃)
    public static final int HEARTBEAT_INTERVAL = 1000;   // 1초 (연결 확인 주기)
    public static final int HEARTBEAT_TIMEOUT = 5000;    // 5초 (무응답 시 끊김 판정)
    
    // 지연 설정
    public static final int LAG_THRESHOLD = 200;         // 200ms (지연 판정 기준 == "렉걸림" 표시 기준)
    
    // 재연결 설정
    public static final int MAX_RECONNECT_ATTEMPTS = 3;   // 최대 재연결 시도 횟수
    public static final int RECONNECT_DELAY = 2000;      // 2초 (재연결 대기 시간)
    
    // 메시지 큐 크기
    public static final int MESSAGE_QUEUE_SIZE = 100;  // 최대 메시지 큐 크기
    
    private NetworkConfig() {
        // 인스턴스화 방지
    }
}
