package utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import network.messages.NetworkMessage;
import network.messages.MessageType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NetworkSimulator 유닛 테스트
 */
class NetworkSimulatorTest {
    
    // 테스트용 NetworkMessage 구현체
    private static class TestMessage extends NetworkMessage {
        public TestMessage() {
            super(MessageType.HEARTBEAT);
        }
    }
    
    private NetworkSimulator simulator;
    
    @BeforeEach
    void setUp() {
        simulator = new NetworkSimulator();
    }
    
    @Test
    void testDefaultConstructor_PerfectNetwork() {
        // Given: 기본 생성자로 생성
        NetworkSimulator defaultSim = new NetworkSimulator();
        
        // Then: 완벽한 네트워크 설정
        assertEquals(0, defaultSim.getBaseLatency());
        assertEquals(0, defaultSim.getJitter());
        assertEquals(0.0, defaultSim.getPacketLossRate());
    }
    
    @Test
    void testParameterizedConstructor() {
        // Given & When: 파라미터로 생성
        NetworkSimulator sim = new NetworkSimulator(100, 20, 0.05);
        
        // Then: 설정된 값 확인
        assertEquals(100, sim.getBaseLatency());
        assertEquals(20, sim.getJitter());
        assertEquals(0.05, sim.getPacketLossRate(), 0.001);
    }
    
    @Test
    void testSetLatency_ValidValue() {
        // Given: 시뮬레이터 생성
        
        // When: 지연 설정
        simulator.setLatency(150);
        
        // Then: 지연 값 확인
        assertEquals(150, simulator.getBaseLatency());
    }
    
    @Test
    void testSetLatency_NegativeValue_ShouldClampToZero() {
        // Given: 시뮬레이터 생성
        
        // When: 음수 지연 설정
        simulator.setLatency(-50);
        
        // Then: 0으로 클램프됨
        assertEquals(0, simulator.getBaseLatency());
    }
    
    @Test
    void testSetJitter_ValidValue() {
        // Given: 시뮬레이터 생성
        
        // When: 지터 설정
        simulator.setJitter(30);
        
        // Then: 지터 값 확인
        assertEquals(30, simulator.getJitter());
    }
    
    @Test
    void testSetJitter_NegativeValue_ShouldClampToZero() {
        // Given: 시뮬레이터 생성
        
        // When: 음수 지터 설정
        simulator.setJitter(-20);
        
        // Then: 0으로 클램프됨
        assertEquals(0, simulator.getJitter());
    }
    
    @Test
    void testSetPacketLossRate_ValidValue() {
        // Given: 시뮬레이터 생성
        
        // When: 패킷 손실률 설정
        simulator.setPacketLossRate(0.1);
        
        // Then: 손실률 확인
        assertEquals(0.1, simulator.getPacketLossRate(), 0.001);
    }
    
    @Test
    void testSetPacketLossRate_GreaterThanOne_ShouldClampToOne() {
        // Given: 시뮬레이터 생성
        
        // When: 1보다 큰 손실률 설정
        simulator.setPacketLossRate(1.5);
        
        // Then: 1.0으로 클램프됨
        assertEquals(1.0, simulator.getPacketLossRate(), 0.001);
    }
    
    @Test
    void testSetPacketLossRate_NegativeValue_ShouldClampToZero() {
        // Given: 시뮬레이터 생성
        
        // When: 음수 손실률 설정
        simulator.setPacketLossRate(-0.5);
        
        // Then: 0.0으로 클램프됨
        assertEquals(0.0, simulator.getPacketLossRate(), 0.001);
    }
    
    @Test
    void testSetBandwidthLimit_ValidValue() {
        // Given: 시뮬레이터 생성
        
        // When: 대역폭 제한 설정
        simulator.setBandwidthLimit(100 * 1024); // 100 KB/s
        
        // Then: 설정 확인 (getter 없으므로 예외 없이 실행되는지 확인)
        assertDoesNotThrow(() -> simulator.setBandwidthLimit(100 * 1024));
    }
    
    @Test
    void testSetBandwidthLimit_Zero_UnlimitedBandwidth() {
        // Given: 시뮬레이터 생성
        
        // When: 무제한 대역폭 설정
        assertDoesNotThrow(() -> simulator.setBandwidthLimit(0));
        
        // Then: 예외 없이 실행됨
    }
    
    @Test
    void testSendMessage_PerfectNetwork_AlwaysSucceeds() throws InterruptedException {
        // Given: 완벽한 네트워크 (기본 설정)
        NetworkMessage message = new TestMessage();
        
        // When: 메시지 전송
        boolean result = simulator.sendMessage(message);
        
        // Then: 항상 성공
        assertTrue(result);
        assertEquals(1, simulator.getTotalPacketsSent());
        assertEquals(0, simulator.getTotalPacketsLost());
    }
    
    @Test
    void testSendMessage_WithPacketLoss_SometimesFails() throws InterruptedException {
        // Given: 100% 패킷 손실률
        simulator.setPacketLossRate(1.0);
        NetworkMessage message = new TestMessage();
        
        // When: 메시지 전송
        boolean result = simulator.sendMessage(message);
        
        // Then: 항상 실패
        assertFalse(result);
        assertEquals(1, simulator.getTotalPacketsSent());
        assertEquals(1, simulator.getTotalPacketsLost());
    }
    
    @Test
    void testSendMessage_MultipleMessages() throws InterruptedException {
        // Given: 시뮬레이터 생성
        NetworkMessage message = new TestMessage();
        
        // When: 여러 메시지 전송
        for (int i = 0; i < 10; i++) {
            simulator.sendMessage(message);
        }
        
        // Then: 전송 횟수 확인
        assertEquals(10, simulator.getTotalPacketsSent());
    }
    
    @Test
    void testGetActualLossRate_NoPackets() {
        // Given: 메시지를 보내지 않음
        
        // When: 실제 손실률 조회
        double actualLossRate = simulator.getActualLossRate();
        
        // Then: 0 반환
        assertEquals(0.0, actualLossRate, 0.001);
    }
    
    @Test
    void testGetActualLossRate_WithPackets() throws InterruptedException {
        // Given: 50% 패킷 손실률 설정
        simulator.setPacketLossRate(0.5);
        NetworkMessage message = new TestMessage();
        
        // When: 많은 메시지 전송 (통계적으로 50%에 근접)
        for (int i = 0; i < 100; i++) {
            simulator.sendMessage(message);
        }
        
        // Then: 실제 손실률이 0보다 큼
        assertTrue(simulator.getActualLossRate() > 0);
    }
    
    @Test
    void testGetAverageLatency_NoMessages() {
        // Given: 메시지를 보내지 않음
        
        // When: 평균 지연 조회
        long avgLatency = simulator.getAverageLatency();
        
        // Then: 0 반환
        assertEquals(0, avgLatency);
    }
    
    @Test
    void testSetProfile_Perfect() {
        // Given: 시뮬레이터 생성
        
        // When: PERFECT 프로파일 설정
        simulator.setProfile(NetworkSimulator.NetworkProfile.PERFECT);
        
        // Then: 완벽한 네트워크 설정
        assertEquals(0, simulator.getBaseLatency());
        assertEquals(0, simulator.getJitter());
        assertEquals(0.0, simulator.getPacketLossRate());
    }
    
    @Test
    void testSetProfile_Good() {
        // Given: 시뮬레이터 생성
        
        // When: GOOD 프로파일 설정
        simulator.setProfile(NetworkSimulator.NetworkProfile.GOOD);
        
        // Then: 좋은 네트워크 설정
        assertEquals(30, simulator.getBaseLatency());
        assertEquals(5, simulator.getJitter());
        assertEquals(0.001, simulator.getPacketLossRate(), 0.0001);
    }
    
    @Test
    void testSetProfile_Normal() {
        // Given: 시뮬레이터 생성
        
        // When: NORMAL 프로파일 설정
        simulator.setProfile(NetworkSimulator.NetworkProfile.NORMAL);
        
        // Then: 일반 네트워크 설정
        assertEquals(80, simulator.getBaseLatency());
        assertEquals(15, simulator.getJitter());
        assertEquals(0.01, simulator.getPacketLossRate(), 0.001);
    }
    
    @Test
    void testSetProfile_Poor() {
        // Given: 시뮬레이터 생성
        
        // When: POOR 프로파일 설정
        simulator.setProfile(NetworkSimulator.NetworkProfile.POOR);
        
        // Then: 나쁜 네트워크 설정
        assertEquals(200, simulator.getBaseLatency());
        assertEquals(50, simulator.getJitter());
        assertEquals(0.05, simulator.getPacketLossRate(), 0.001);
    }
    
    @Test
    void testSetProfile_Terrible() {
        // Given: 시뮬레이터 생성
        
        // When: TERRIBLE 프로파일 설정
        simulator.setProfile(NetworkSimulator.NetworkProfile.TERRIBLE);
        
        // Then: 매우 나쁜 네트워크 설정
        assertEquals(500, simulator.getBaseLatency());
        assertEquals(100, simulator.getJitter());
        assertEquals(0.15, simulator.getPacketLossRate(), 0.001);
    }
    
    @Test
    void testResetStats() throws InterruptedException {
        // Given: 메시지 전송 후 통계 생성
        NetworkMessage message = new TestMessage();
        simulator.sendMessage(message);
        simulator.sendMessage(message);
        
        // When: 통계 리셋
        simulator.resetStats();
        
        // Then: 모든 통계가 초기화됨
        assertEquals(0, simulator.getTotalPacketsSent());
        assertEquals(0, simulator.getTotalPacketsLost());
        assertEquals(0.0, simulator.getActualLossRate());
        assertEquals(0, simulator.getAverageLatency());
    }
    
    @Test
    void testPrintStats_NoException() throws InterruptedException {
        // Given: 메시지 전송
        NetworkMessage message = new TestMessage();
        simulator.sendMessage(message);
        
        // When & Then: 통계 출력 (예외 없이 실행)
        assertDoesNotThrow(() -> simulator.printStats());
    }
    
    @Test
    void testPrintStats_EmptyStats() {
        // Given: 메시지를 보내지 않음
        
        // When & Then: 통계 출력 (예외 없이 실행)
        assertDoesNotThrow(() -> simulator.printStats());
    }
    
    @Test
    void testNetworkProfileEnum_AllValues() {
        // Given & When: 모든 프로파일 열거
        NetworkSimulator.NetworkProfile[] profiles = NetworkSimulator.NetworkProfile.values();
        
        // Then: 5개의 프로파일 존재
        assertEquals(5, profiles.length);
        
        // 각 프로파일 이름 확인
        assertEquals("PERFECT", NetworkSimulator.NetworkProfile.PERFECT.name());
        assertEquals("GOOD", NetworkSimulator.NetworkProfile.GOOD.name());
        assertEquals("NORMAL", NetworkSimulator.NetworkProfile.NORMAL.name());
        assertEquals("POOR", NetworkSimulator.NetworkProfile.POOR.name());
        assertEquals("TERRIBLE", NetworkSimulator.NetworkProfile.TERRIBLE.name());
    }
    
    @Test
    void testNetworkProfileEnum_ValueOf() {
        // Given & When: 문자열로 프로파일 조회
        NetworkSimulator.NetworkProfile profile = NetworkSimulator.NetworkProfile.valueOf("NORMAL");
        
        // Then: 올바른 프로파일 반환
        assertEquals(NetworkSimulator.NetworkProfile.NORMAL, profile);
    }
    
    @Test
    void testConcurrentSendMessages() throws InterruptedException {
        // Given: 시뮬레이터 생성
        NetworkMessage message = new TestMessage();
        
        // When: 동시에 여러 메시지 전송 (간단한 동시성 테스트)
        Thread thread1 = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    simulator.sendMessage(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    simulator.sendMessage(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        
        // Then: 총 20개 메시지 전송
        assertEquals(20, simulator.getTotalPacketsSent());
    }
}
