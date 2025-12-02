package network;

import network.messages.NetworkMessage;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 메시지 수신 스레드
 * 네트워크로부터 메시지를 받아서 리스너들에게 전달합니다.
 */
public class MessageReceiver extends Thread {
    private final ObjectInputStream in;
    private final List<MessageListener> listeners;
    private volatile boolean running = true;
    
    public MessageReceiver(ObjectInputStream in) {
        this.in = in;
        this.listeners = new ArrayList<>();
        setDaemon(true);
        setName("MessageReceiver-Thread");
    }
    
    /**
     * 메시지 리스너 등록
     */
    public void addMessageListener(MessageListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * 메시지 리스너 제거
     */
    public void removeMessageListener(MessageListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    @Override
    public void run() {
        System.out.println("MessageReceiver 시작");
        
        while (running) {
            try {
                // 메시지 수신 (블로킹)
                NetworkMessage message = (NetworkMessage) in.readObject();
                
                System.out.println("메시지 수신: " + message + 
                                 " (지연: " + message.getElapsedTime() + "ms)");
                
                // 모든 리스너에게 메시지 전달
                notifyListeners(message);
                
            } catch (Exception e) {
                // running이 false면 정상 종료 (오류 출력 안 함)
                if (running) {
                    System.err.println("메시지 수신 실패: " + e.getMessage());
                    // 연결 끊김 알림
                    notifyConnectionLost();
                } else {
                    System.out.println("MessageReceiver 정상 종료");
                }
                break;
            }
        }
        
        System.out.println("MessageReceiver 종료");
    }
    
    /**
     * 리스너들에게 메시지 전달
     */
    private void notifyListeners(NetworkMessage message) {
        List<MessageListener> listenersCopy;
        synchronized (listeners) {
            listenersCopy = new ArrayList<>(listeners);
        }
        
        for (MessageListener listener : listenersCopy) {
            try {
                listener.onMessageReceived(message);
            } catch (Exception e) {
                System.err.println("리스너 처리 중 오류: " + e.getMessage());
            }
        }
    }
    
    /**
     * 연결 끊김 알림
     */
    private void notifyConnectionLost() {
        List<MessageListener> listenersCopy;
        synchronized (listeners) {
            listenersCopy = new ArrayList<>(listeners);
        }
        
        for (MessageListener listener : listenersCopy) {
            try {
                listener.onConnectionLost();
            } catch (Exception e) {
                System.err.println("연결 끊김 알림 중 오류: " + e.getMessage());
            }
        }
    }
    
    /**
     * 수신 스레드 중지
     */
    public void shutdown() {
        running = false;
        interrupt();
    }
    
    /**
     * 메시지 리스너 인터페이스
     */
    public interface MessageListener {
        /**
         * 메시지 수신 시 호출
         */
        void onMessageReceived(NetworkMessage message);
        
        /**
         * 연결 끊김 시 호출
         */
        void onConnectionLost();
    }
}
