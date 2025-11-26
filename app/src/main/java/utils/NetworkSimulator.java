package utils;

import network.messages.NetworkMessage;
import java.util.Random;

/**
 * 네트워크 시뮬레이터
 * 테스트 환경에서 다양한 네트워크 상황을 시뮬레이션합니다.
 * 
 * 기능:
 * - 지연 시뮬레이션 (10ms ~ 500ms)
 * - 패킷 손실 시뮬레이션 (0% ~ 20%)
 * - 대역폭 제한 시뮬레이션
 * - 지터(jitter) 시뮬레이션
 */
public class NetworkSimulator {
    
    private long baseLatency = 0;           // 기본 지연 시간 (ms)
    private long jitter = 0;                // 지터 (ms)
    private double packetLossRate = 0.0;    // 패킷 손실률 (0.0 ~ 1.0)
    private long bandwidthLimit = 0;        // 대역폭 제한 (bytes/sec, 0 = 무제한)
    
    private final Random random = new Random();
    private long totalPacketsSent = 0;
    private long totalPacketsLost = 0;
    private long totalBytesTransferred = 0;
    
    // 통계
    private long minLatency = Long.MAX_VALUE;
    private long maxLatency = 0;
    private long totalLatency = 0;
    private long latencyCount = 0;
    
    /**
     * 기본 생성자 (완벽한 네트워크)
     */
    public NetworkSimulator() {
        this(0, 0, 0.0);
    }
    
    /**
     * 네트워크 시뮬레이터 생성자
     * 
     * @param baseLatency 기본 지연 시간 (ms)
     * @param jitter 지터 (ms)
     * @param packetLossRate 패킷 손실률 (0.0 ~ 1.0)
     */
    public NetworkSimulator(long baseLatency, long jitter, double packetLossRate) {
        this.baseLatency = baseLatency;
        this.jitter = jitter;
        this.packetLossRate = Math.max(0.0, Math.min(1.0, packetLossRate));
    }
    
    /**
     * 네트워크 지연 설정
     * 
     * @param latency 지연 시간 (ms)
     */
    public void setLatency(long latency) {
        this.baseLatency = Math.max(0, latency);
        System.out.println("🌐 [NetworkSimulator] 지연 설정: " + baseLatency + "ms");
    }
    
    /**
     * 지터 설정
     * 
     * @param jitter 지터 (ms)
     */
    public void setJitter(long jitter) {
        this.jitter = Math.max(0, jitter);
        System.out.println("🌐 [NetworkSimulator] 지터 설정: " + this.jitter + "ms");
    }
    
    /**
     * 패킷 손실률 설정
     * 
     * @param rate 손실률 (0.0 ~ 1.0)
     */
    public void setPacketLossRate(double rate) {
        this.packetLossRate = Math.max(0.0, Math.min(1.0, rate));
        System.out.println("🌐 [NetworkSimulator] 패킷 손실률 설정: " + 
            String.format("%.1f", packetLossRate * 100) + "%");
    }
    
    /**
     * 대역폭 제한 설정
     * 
     * @param bytesPerSecond 초당 바이트 수 (0 = 무제한)
     */
    public void setBandwidthLimit(long bytesPerSecond) {
        this.bandwidthLimit = Math.max(0, bytesPerSecond);
        System.out.println("🌐 [NetworkSimulator] 대역폭 제한: " + 
            (bandwidthLimit == 0 ? "무제한" : bandwidthLimit + " bytes/sec"));
    }
    
    /**
     * 메시지 전송 시뮬레이션
     * 
     * @param message 전송할 메시지
     * @return 전송 성공 여부 (패킷 손실 시 false)
     * @throws InterruptedException 지연 중 인터럽트 발생 시
     */
    public boolean sendMessage(NetworkMessage message) throws InterruptedException {
        totalPacketsSent++;
        
        // 1. 패킷 손실 시뮬레이션
        if (shouldDropPacket()) {
            totalPacketsLost++;
            System.out.println("📉 [NetworkSimulator] 패킷 손실 (손실률: " + 
                String.format("%.1f", packetLossRate * 100) + "%)");
            return false;
        }
        
        // 2. 지연 시뮬레이션
        long actualLatency = calculateActualLatency();
        if (actualLatency > 0) {
            Thread.sleep(actualLatency);
        }
        
        // 통계 업데이트
        updateLatencyStats(actualLatency);
        
        // 3. 대역폭 제한 시뮬레이션
        if (bandwidthLimit > 0) {
            simulateBandwidthLimit(message);
        }
        
        return true;
    }
    
    /**
     * 패킷 손실 여부 결정
     */
    private boolean shouldDropPacket() {
        return random.nextDouble() < packetLossRate;
    }
    
    /**
     * 실제 지연 시간 계산 (기본 지연 + 지터)
     */
    private long calculateActualLatency() {
        long jitterValue = jitter > 0 ? random.nextInt((int) jitter * 2 + 1) - (int) jitter : 0;
        return Math.max(0, baseLatency + jitterValue);
    }
    
    /**
     * 지연 통계 업데이트
     */
    private void updateLatencyStats(long latency) {
        minLatency = Math.min(minLatency, latency);
        maxLatency = Math.max(maxLatency, latency);
        totalLatency += latency;
        latencyCount++;
    }
    
    /**
     * 대역폭 제한 시뮬레이션
     */
    private void simulateBandwidthLimit(NetworkMessage message) throws InterruptedException {
        // 메시지 크기 추정 (실제로는 직렬화 크기를 사용해야 함)
        long messageSize = estimateMessageSize(message);
        totalBytesTransferred += messageSize;
        
        // 전송 시간 계산
        long transmitTime = (messageSize * 1000) / bandwidthLimit;
        if (transmitTime > 0) {
            Thread.sleep(transmitTime);
        }
    }
    
    /**
     * 메시지 크기 추정
     */
    private long estimateMessageSize(NetworkMessage message) {
        // 간단한 추정: 메시지 타입에 따라 다른 크기
        // 실제로는 직렬화된 크기를 사용해야 함
        return 1024; // 1KB로 가정
    }
    
    /**
     * 네트워크 프로파일 설정 (사전 정의된 시나리오)
     */
    public void setProfile(NetworkProfile profile) {
        switch (profile) {
            case PERFECT:
                setLatency(0);
                setJitter(0);
                setPacketLossRate(0.0);
                setBandwidthLimit(0);
                break;
                
            case GOOD:
                setLatency(30);
                setJitter(5);
                setPacketLossRate(0.001); // 0.1%
                setBandwidthLimit(0);
                break;
                
            case NORMAL:
                setLatency(80);
                setJitter(15);
                setPacketLossRate(0.01); // 1%
                setBandwidthLimit(0);
                break;
                
            case POOR:
                setLatency(200);
                setJitter(50);
                setPacketLossRate(0.05); // 5%
                setBandwidthLimit(100 * 1024); // 100 KB/s
                break;
                
            case TERRIBLE:
                setLatency(500);
                setJitter(100);
                setPacketLossRate(0.15); // 15%
                setBandwidthLimit(50 * 1024); // 50 KB/s
                break;
        }
        
        System.out.println("🌐 [NetworkSimulator] 프로파일 설정: " + profile);
    }
    
    /**
     * 통계 출력
     */
    public void printStats() {
        System.out.println("\n📊 [NetworkSimulator] 통계:");
        System.out.println("  총 전송 시도: " + totalPacketsSent + " 패킷");
        System.out.println("  손실: " + totalPacketsLost + " 패킷 (" + 
            String.format("%.2f", totalPacketsSent > 0 ? (totalPacketsLost * 100.0 / totalPacketsSent) : 0) + "%)");
        System.out.println("  전송 성공: " + (totalPacketsSent - totalPacketsLost) + " 패킷");
        
        if (latencyCount > 0) {
            System.out.println("  지연 시간:");
            System.out.println("    - 최소: " + minLatency + "ms");
            System.out.println("    - 최대: " + maxLatency + "ms");
            System.out.println("    - 평균: " + (totalLatency / latencyCount) + "ms");
        }
        
        if (bandwidthLimit > 0) {
            System.out.println("  전송량: " + totalBytesTransferred + " bytes");
        }
    }
    
    /**
     * 통계 리셋
     */
    public void resetStats() {
        totalPacketsSent = 0;
        totalPacketsLost = 0;
        totalBytesTransferred = 0;
        minLatency = Long.MAX_VALUE;
        maxLatency = 0;
        totalLatency = 0;
        latencyCount = 0;
        
        System.out.println("📊 [NetworkSimulator] 통계 리셋");
    }
    
    // Getters
    public long getBaseLatency() { return baseLatency; }
    public long getJitter() { return jitter; }
    public double getPacketLossRate() { return packetLossRate; }
    public long getTotalPacketsSent() { return totalPacketsSent; }
    public long getTotalPacketsLost() { return totalPacketsLost; }
    public double getActualLossRate() { 
        return totalPacketsSent > 0 ? (totalPacketsLost * 1.0 / totalPacketsSent) : 0;
    }
    public long getAverageLatency() {
        return latencyCount > 0 ? (totalLatency / latencyCount) : 0;
    }
    
    /**
     * 네트워크 프로파일 열거형
     */
    public enum NetworkProfile {
        PERFECT,    // 완벽한 네트워크 (0ms, 0% 손실)
        GOOD,       // 좋은 네트워크 (30ms, 0.1% 손실)
        NORMAL,     // 일반 네트워크 (80ms, 1% 손실)
        POOR,       // 나쁜 네트워크 (200ms, 5% 손실)
        TERRIBLE    // 매우 나쁜 네트워크 (500ms, 15% 손실)
    }
}
