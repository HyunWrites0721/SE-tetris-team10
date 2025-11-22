package network;

import network.messages.NetworkMessage;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 메시지 송신 스레드
 * 큐에 쌓인 메시지를 순차적으로 전송합니다.
 */
public class MessageSender extends Thread {
    private final ObjectOutputStream out;
    private final BlockingQueue<NetworkMessage> messageQueue;
    private volatile boolean running = true;
    
    public MessageSender(ObjectOutputStream out) {
        this.out = out;
        this.messageQueue = new LinkedBlockingQueue<>(NetworkConfig.MESSAGE_QUEUE_SIZE);
        setDaemon(true);  // 메인 스레드 종료 시 자동 종료
        setName("MessageSender-Thread");
    }
    
    /**
     * 메시지를 전송 큐에 추가
     * @param message 전송할 메시지
     * @return 큐에 추가 성공 여부
     */
    public boolean sendMessage(NetworkMessage message) {
        if (!running) {
            return false;
        }
        
        try {
            return messageQueue.offer(message);  // 큐가 가득 차면 false 반환
        } catch (Exception e) {
            System.err.println("메시지 큐 추가 실패: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public void run() {
        System.out.println("MessageSender 시작");
        
        while (running) {
            try {
                // 큐에서 메시지 가져오기 (메시지가 없으면 대기)
                NetworkMessage message = messageQueue.take();
                
                // 메시지 전송
                out.writeObject(message);
                out.flush();
                out.reset();  // 객체 캐시 초기화 (메모리 누수 방지)
                
                System.out.println("메시지 전송: " + message);
                
            } catch (InterruptedException e) {
                // 스레드 중단 신호
                System.out.println("MessageSender 중단됨");
                break;
            } catch (Exception e) {
                System.err.println("메시지 전송 실패: " + e.getMessage());
                // 연결 끊김으로 간주하고 종료
                break;
            }
        }
        
        System.out.println("MessageSender 종료");
    }
    
    /**
     * 송신 스레드 중지
     */
    public void shutdown() {
        running = false;
        interrupt();  // 대기 중인 스레드 깨우기
    }
    
    /**
     * 큐에 남은 메시지 개수
     */
    public int getQueueSize() {
        return messageQueue.size();
    }
    
    /**
     * 큐가 가득 찼는지 확인
     */
    public boolean isQueueFull() {
        return messageQueue.remainingCapacity() == 0;
    }
}
