package network;

/**
 * 네트워크 연결 상태
 */
public enum ConnectionState {
    DISCONNECTED,    // 연결 안됨
    CONNECTING,      // 연결 중
    CONNECTED,       // 정상 연결
    LAGGING,         // 지연 중 (랙)
    TIMEOUT          // 타임아웃
}
