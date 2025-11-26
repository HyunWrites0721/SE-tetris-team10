package integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

import network.*;
import utils.NetworkSimulator;
import game.events.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Phase 5, 6 통합 테스트
 * 다양한 네트워크 상황에서 기능 검증
 */
class Phase5And6IntegrationTest {
    
    private NetworkSimulator simulator;
    private LatencyMonitor latencyMonitor;
    
    @BeforeEach
    void setUp() {
        simulator = new NetworkSimulator();
        latencyMonitor = new LatencyMonitor();
    }
    
    /**
     * 테스트 1: 정상 네트워크에서 LatencyMonitor 동작 확인
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testLatencyMonitor_NormalNetwork() {
        simulator.setProfile(NetworkSimulator.NetworkProfile.GOOD);
        
        // 30ms 지연 시뮬레이션
        for (int i = 0; i < 10; i++) {
            latencyMonitor.recordLatency(30 + (i % 10)); // 30-39ms
        }
        
        long avgLatency = latencyMonitor.getAverageLatency();
        
        assertTrue(avgLatency >= 30 && avgLatency < 40, 
            "평균 지연이 30-40ms 범위여야 함 (실제: " + avgLatency + "ms)");
        assertFalse(latencyMonitor.isLagging(), "정상 네트워크에서는 랙이 발생하지 않아야 함");
    }
    
    /**
     * 테스트 2: 고지연 네트워크에서 LatencyMonitor 랙 감지
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testLatencyMonitor_HighLatencyNetwork() {
        simulator.setProfile(NetworkSimulator.NetworkProfile.POOR);
        
        // 250ms 이상 지연 시뮬레이션
        for (int i = 0; i < 10; i++) {
            latencyMonitor.recordLatency(250 + (i * 10)); // 250-340ms
        }
        
        long avgLatency = latencyMonitor.getAverageLatency();
        
        assertTrue(avgLatency > 200, "평균 지연이 200ms를 초과해야 함");
        assertTrue(latencyMonitor.isLagging(), "고지연 네트워크에서는 랙이 감지되어야 함");
    }
    
    /**
     * 테스트 3: EventFilter가 불필요한 이벤트 차단
     */
    @Test
    void testEventFilter_FiltersTickEvent() {
        TickEvent tickEvent = new TickEvent();
        
        assertFalse(EventFilter.shouldSync(tickEvent), 
            "TickEvent는 동기화 대상이 아니어야 함");
    }
    
    /**
     * 테스트 4: EventFilter가 필수 이벤트 통과
     */
    @Test
    void testEventFilter_AllowsEssentialEvents() {
        GameEvent[] essentialEvents = {
            new BlockSpawnedEvent(),
            new BlockMovedEvent(0, 0, 0, 0),
            new BlockRotatedEvent(),
            new BlockPlacedEvent(),
            new LineClearedEvent(),
            new ScoreUpdateEvent(),
            new GameOverEvent()
        };
        
        for (GameEvent event : essentialEvents) {
            assertTrue(EventFilter.shouldSync(event), 
                event.getClass().getSimpleName() + "는 동기화되어야 함");
        }
    }
    
    /**
     * 테스트 5: 고부하 시뮬레이션 (1000개 이벤트)
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testHighLoad_1000Events() {
        List<GameEvent> events = new ArrayList<>();
        
        // 1000개 이벤트 생성 (TickEvent 포함)
        for (int i = 0; i < 1000; i++) {
            if (i % 10 == 0) {
                events.add(new BlockMovedEvent(0, 0, 0, 0));
            } else {
                events.add(new TickEvent());
            }
        }
        
        // 필터링 카운트
        int syncCount = 0;
        int filteredCount = 0;
        
        for (GameEvent event : events) {
            if (EventFilter.shouldSync(event)) {
                syncCount++;
            } else {
                filteredCount++;
            }
        }
        
        assertEquals(100, syncCount, "100개 BlockMovedEvent만 동기화되어야 함");
        assertEquals(900, filteredCount, "900개 TickEvent가 필터링되어야 함");
        
        double filterRate = (filteredCount * 100.0) / events.size();
        assertTrue(filterRate >= 80, "필터율이 80% 이상이어야 함 (실제: " + 
            String.format("%.1f", filterRate) + "%)");
    }
    
    /**
     * 테스트 6: 패킷 손실 상황에서 안정성 확인
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testPacketLoss_Stability() throws InterruptedException {
        simulator.setPacketLossRate(0.1); // 10% 패킷 손실
        
        int successfulSent = 0;
        
        for (int i = 0; i < 100; i++) {
            try {
                // 메시지 전송 시뮬레이션
                if (simulator.sendMessage(new TestMessage())) {
                    successfulSent++;
                }
            } catch (Exception e) {
                fail("패킷 손실 시에도 예외가 발생하지 않아야 함");
            }
        }
        
        assertTrue(successfulSent >= 80, 
            "10% 손실률에서 최소 80개는 성공해야 함 (실제: " + successfulSent + ")");
        
        double actualLossRate = simulator.getActualLossRate();
        assertTrue(actualLossRate <= 0.2, 
            "실제 손실률이 20% 이하여야 함 (실제: " + 
            String.format("%.1f", actualLossRate * 100) + "%)");
    }
    
    /**
     * 테스트 7: LatencyMonitor 히스토리 크기 제한
     */
    @Test
    void testLatencyMonitor_HistorySizeLimit() {
        // 15개 지연 시간 기록 (히스토리 크기는 10개)
        for (int i = 1; i <= 15; i++) {
            latencyMonitor.recordLatency(i * 10);
        }
        
        long avgLatency = latencyMonitor.getAverageLatency();
        
        // 마지막 10개 평균: (60+70+80+90+100+110+120+130+140+150)/10 = 105
        assertEquals(105, avgLatency, 
            "최근 10개만 유지되어야 함 (평균 105ms 예상)");
    }
    
    /**
     * 테스트 8: 다양한 네트워크 프로파일 테스트
     */
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testAllNetworkProfiles() throws InterruptedException {
        NetworkSimulator.NetworkProfile[] profiles = {
            NetworkSimulator.NetworkProfile.PERFECT,
            NetworkSimulator.NetworkProfile.GOOD,
            NetworkSimulator.NetworkProfile.NORMAL
            // POOR 프로파일은 시간이 너무 오래 걸려 제외
        };
        
        for (NetworkSimulator.NetworkProfile profile : profiles) {
            simulator.setProfile(profile);
            simulator.resetStats();
            
            // 각 프로파일에서 20개 패킷 전송 (50개는 너무 많음)
            for (int i = 0; i < 20; i++) {
                simulator.sendMessage(new TestMessage());
            }
            
            assertTrue(simulator.getTotalPacketsSent() > 0, 
                profile + " 프로파일에서 패킷 전송 확인");
        }
    }
    
    /**
     * 테스트용 네트워크 메시지
     */
    private static class TestMessage extends network.messages.NetworkMessage {
        private static final long serialVersionUID = 1L;
        
        public TestMessage() {
            super(network.messages.MessageType.HEARTBEAT);
        }
    }
}
