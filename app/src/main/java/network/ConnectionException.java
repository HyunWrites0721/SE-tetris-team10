package network;

/**
 * 네트워크 연결 관련 예외
 */
public class ConnectionException extends Exception {
    
    public ConnectionException(String message) {
        super(message);
    }
    
    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
