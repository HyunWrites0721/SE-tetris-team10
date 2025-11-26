package network;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import utils.NetworkSimulator;
import network.messages.NetworkMessage;
import network.messages.MessageType;
import static org.junit.jupiter.api.Assertions.*;

/**
 * NetworkSimulator 테스트
 */
class NetworkSimulatorTest {
    
    private NetworkSimulator simulator;
    
    @BeforeEach
    void setUp() {
        simulator = new NetworkSimulator();
    }
    
    @Test
    void testPerfectNetwork() throws InterruptedException {
        simulator.setProfile(NetworkSimulator.NetworkProfile.PERFECT);
        
        // 100개 패킷 전송
        int successCount = 0;
        for (int i = 0; i < 100; i++) {
            if (simulator.sendMessage(new TestMessage())) {
                successCount++;
            }
        }
        
        assertEquals(100, successCount, "완벽한 네트워크에서는 모든 패킷이 전송되어야 함");
        assertEquals(0, simulator.getTotalPacketsLost(), "패킷 손실 없어야 함");
    }
    
    @Test
    void testLatencySimulation() throws InterruptedException {
        long latency = 100; // 100ms
        simulator.setLatency(latency);
        
        long startTime = System.currentTimeMillis();
        simulator.sendMessage(new TestMessage());
        long endTime = System.currentTimeMillis();
        
        long actualLatency = endTime - startTime;
        assertTrue(actualLatency >= latency, "실제 지연이 설정한 지연보다 커야 함");
        assertTrue(actualLatency < latency + 50, "실제 지연이 설정한 지연에 근접해야 함");
    }
    
    @Test
    void testPacketLoss() throws InterruptedException {
        simulator.setPacketLossRate(0.5); // 50% 손실
        
        int successCount = 0;
        int totalPackets = 1000;
        
        for (int i = 0; i < totalPackets; i++) {
            if (simulator.sendMessage(new TestMessage())) {
                successCount++;
            }
        }
        
        // 50% ± 10% 범위 (통계적 오차 고려)
        assertTrue(successCount >= totalPackets * 0.4, "최소 40% 성공해야 함");
        assertTrue(successCount <= totalPackets * 0.6, "최대 60% 성공해야 함");
        
        double actualLossRate = simulator.getActualLossRate();
        assertTrue(actualLossRate >= 0.4 && actualLossRate <= 0.6, 
            "실제 손실률이 40-60% 범위여야 함");
    }
    
    @Test
    void testJitter() throws InterruptedException {
        simulator.setLatency(100);
        simulator.setJitter(50); // ±50ms
        
        long minMeasured = Long.MAX_VALUE;
        long maxMeasured = 0;
        
        for (int i = 0; i < 10; i++) {
            long startTime = System.currentTimeMillis();
            simulator.sendMessage(new TestMessage());
            long endTime = System.currentTimeMillis();
            
            long latency = endTime - startTime;
            minMeasured = Math.min(minMeasured, latency);
            maxMeasured = Math.max(maxMeasured, latency);
        }
        
        // 지터로 인해 지연 시간 변동 있어야 함
        assertTrue(maxMeasured - minMeasured > 0, "지터로 인한 변동이 있어야 함");
    }
    
    @Test
    void testGoodNetworkProfile() throws InterruptedException {
        simulator.setProfile(NetworkSimulator.NetworkProfile.GOOD);
        
        int successCount = 0;
        for (int i = 0; i < 100; i++) {
            if (simulator.sendMessage(new TestMessage())) {
                successCount++;
            }
        }
        
        assertTrue(successCount >= 95, "좋은 네트워크에서는 대부분 패킷이 전송되어야 함");
    }
    
    @Test
    void testPoorNetworkProfile() throws InterruptedException {
        simulator.setProfile(NetworkSimulator.NetworkProfile.POOR);
        
        // 나쁜 네트워크는 높은 지연과 손실률을 가져야 함
        // (실제 측정을 위해서는 패킷을 전송해야 함)
        for (int i = 0; i < 100; i++) {
            simulator.sendMessage(new TestMessage());
        }
        
        assertTrue(simulator.getAverageLatency() > 150, "나쁜 네트워크는 높은 지연을 가져야 함");
        assertTrue(simulator.getActualLossRate() > 0, "나쁜 네트워크는 패킷 손실이 있어야 함");
    }
    
    @Test
    void testStatsReset() throws InterruptedException {
        simulator.sendMessage(new TestMessage());
        simulator.sendMessage(new TestMessage());
        
        assertTrue(simulator.getTotalPacketsSent() > 0, "전송 전 패킷 수 > 0");
        
        simulator.resetStats();
        
        assertEquals(0, simulator.getTotalPacketsSent(), "리셋 후 패킷 수 = 0");
        assertEquals(0, simulator.getTotalPacketsLost(), "리셋 후 손실 패킷 = 0");
    }
    
    @Test
    void testPrintStats() throws InterruptedException {
        simulator.setLatency(50);
        simulator.setPacketLossRate(0.1);
        
        for (int i = 0; i < 10; i++) {
            simulator.sendMessage(new TestMessage());
        }
        
        // 예외 없이 실행되는지 확인
        assertDoesNotThrow(() -> simulator.printStats());
    }
    
    /**
     * 테스트용 메시지 클래스
     */
    private static class TestMessage extends NetworkMessage {
        private static final long serialVersionUID = 1L;
        
        public TestMessage() {
            super(MessageType.HEARTBEAT);
        }
    }
}
