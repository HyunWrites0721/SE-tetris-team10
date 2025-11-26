package network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * LatencyMonitor 테스트
 */
class LatencyMonitorTest {
    
    private LatencyMonitor monitor;
    
    @BeforeEach
    void setUp() {
        monitor = new LatencyMonitor();
    }
    
    @Test
    void testRecordLatency() {
        monitor.recordLatency(50);
        assertEquals(50, monitor.getAverageLatency());
        assertEquals(50, monitor.getLastLatency());
    }
    
    @Test
    void testAverageLatency() {
        monitor.recordLatency(100);
        monitor.recordLatency(200);
        monitor.recordLatency(300);
        
        assertEquals(200, monitor.getAverageLatency());
    }
    
    @Test
    void testMinMaxLatency() {
        monitor.recordLatency(150);
        monitor.recordLatency(50);
        monitor.recordLatency(250);
        
        assertEquals(50, monitor.getMinLatency());
        assertEquals(250, monitor.getMaxLatency());
    }
    
    @Test
    void testIsLagging_Normal() {
        monitor.recordLatency(100);
        monitor.recordLatency(150);
        
        assertFalse(monitor.isLagging(), "평균 지연이 200ms 이하면 랙이 아님");
    }
    
    @Test
    void testIsLagging_High() {
        monitor.recordLatency(250);
        monitor.recordLatency(300);
        
        assertTrue(monitor.isLagging(), "평균 지연이 200ms 초과면 랙");
    }
    
    @Test
    void testHistorySize() {
        // 10개를 초과하면 오래된 것부터 제거
        for (int i = 1; i <= 15; i++) {
            monitor.recordLatency(i * 10);
        }
        
        // 마지막 10개만 유지되므로 평균은 (60+70+80+90+100+110+120+130+140+150)/10 = 105
        assertEquals(105, monitor.getAverageLatency());
    }
    
    @Test
    void testReset() {
        monitor.recordLatency(100);
        monitor.recordLatency(200);
        
        monitor.reset();
        
        assertEquals(0, monitor.getAverageLatency());
        assertEquals(0, monitor.getLastLatency());
    }
    
    @Test
    void testEmptyMonitor() {
        assertEquals(0, monitor.getAverageLatency());
        assertEquals(0, monitor.getMinLatency());
        assertEquals(0, monitor.getMaxLatency());
        assertEquals(0, monitor.getLastLatency());
        assertFalse(monitor.isLagging());
    }
    
    @Test
    void testPrintStats() {
        monitor.recordLatency(100);
        monitor.recordLatency(200);
        
        // 예외 없이 실행되는지 확인
        assertDoesNotThrow(() -> monitor.printStats());
    }
}
