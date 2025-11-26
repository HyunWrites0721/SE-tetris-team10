package network;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 네트워크 지연 시간(latency)을 측정하고 관리하는 클래스
 * RTT(Round Trip Time)를 기록하여 평균 지연 시간을 계산하고
 * 랙(lag) 상태를 판단합니다.
 */
public class LatencyMonitor {
    
    private final Queue<Long> latencyHistory = new LinkedList<>();
    private static final int HISTORY_SIZE = 10;  // 최근 10개 기록 유지
    
    /**
     * 지연 시간 기록
     * @param latency RTT(밀리초)
     */
    public synchronized void recordLatency(long latency) {
        latencyHistory.offer(latency);
        
        // 최대 HISTORY_SIZE개만 유지
        if (latencyHistory.size() > HISTORY_SIZE) {
            latencyHistory.poll();
        }
        
        System.out.println("📊 네트워크 지연: " + latency + "ms (평균: " + getAverageLatency() + "ms)");
    }
    
    /**
     * 평균 지연 시간 계산
     * @return 평균 RTT(밀리초)
     */
    public synchronized long getAverageLatency() {
        if (latencyHistory.isEmpty()) {
            return 0;
        }
        
        return (long) latencyHistory.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0);
    }
    
    /**
     * 랙(lag) 상태 판단
     * @return 평균 지연이 LAG_THRESHOLD를 초과하면 true
     */
    public boolean isLagging() {
        long avgLatency = getAverageLatency();
        boolean lagging = avgLatency > NetworkConfig.LAG_THRESHOLD;
        
        if (lagging) {
            System.out.println("⚠️ 랙 감지: 평균 지연 " + avgLatency + "ms (기준: " + NetworkConfig.LAG_THRESHOLD + "ms)");
        }
        
        return lagging;
    }
    
    /**
     * 가장 최근 지연 시간 반환
     * @return 마지막 RTT(밀리초)
     */
    public synchronized long getLastLatency() {
        if (latencyHistory.isEmpty()) {
            return 0;
        }
        return ((LinkedList<Long>) latencyHistory).getLast();
    }
    
    /**
     * 최소 지연 시간 반환
     * @return 최소 RTT(밀리초)
     */
    public synchronized long getMinLatency() {
        if (latencyHistory.isEmpty()) {
            return 0;
        }
        return latencyHistory.stream()
            .mapToLong(Long::longValue)
            .min()
            .orElse(0);
    }
    
    /**
     * 최대 지연 시간 반환
     * @return 최대 RTT(밀리초)
     */
    public synchronized long getMaxLatency() {
        if (latencyHistory.isEmpty()) {
            return 0;
        }
        return latencyHistory.stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0);
    }
    
    /**
     * 통계 정보 출력
     */
    public synchronized void printStats() {
        if (latencyHistory.isEmpty()) {
            System.out.println("📊 네트워크 통계: 데이터 없음");
            return;
        }
        
        System.out.println("📊 네트워크 통계:");
        System.out.println("  - 평균: " + getAverageLatency() + "ms");
        System.out.println("  - 최소: " + getMinLatency() + "ms");
        System.out.println("  - 최대: " + getMaxLatency() + "ms");
        System.out.println("  - 최근: " + getLastLatency() + "ms");
        System.out.println("  - 샘플: " + latencyHistory.size() + "개");
        System.out.println("  - 랙 상태: " + (isLagging() ? "⚠️ 랙 걸림" : "✓ 정상"));
    }
    
    /**
     * 기록 초기화
     */
    public synchronized void reset() {
        latencyHistory.clear();
        System.out.println("📊 LatencyMonitor 초기화됨");
    }
}
