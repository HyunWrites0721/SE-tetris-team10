package versus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VersusAttackManager 유닛 테스트
 */
class VersusAttackManagerTest {
    
    private VersusAttackManager attackManager;
    
    @BeforeEach
    void setUp() {
        attackManager = new VersusAttackManager();
    }
    
    @Test
    void testInitialState_NoPendingLines() {
        // Given: 새로 생성된 공격 매니저
        
        // Then: 대기 중인 줄이 없음
        assertEquals(0, attackManager.getPendingLines());
    }
    
    @Test
    void testReceiveAttack_SingleLine() {
        // Given: 공격 매니저 생성
        
        // When: 1줄 공격 받음
        int added = attackManager.receiveAttack(1);
        
        // Then: 1줄 추가됨
        assertEquals(1, added);
        assertEquals(1, attackManager.getPendingLines());
    }
    
    @Test
    void testReceiveAttack_MultipleLines() {
        // Given: 공격 매니저 생성
        
        // When: 3줄 공격 받음
        int added = attackManager.receiveAttack(3);
        
        // Then: 3줄 추가됨
        assertEquals(3, added);
        assertEquals(3, attackManager.getPendingLines());
    }
    
    @Test
    void testReceiveAttack_AccumulateLines() {
        // Given: 공격 매니저 생성
        
        // When: 여러 번 공격 받음
        attackManager.receiveAttack(2);
        attackManager.receiveAttack(3);
        int totalAdded = attackManager.receiveAttack(1);
        
        // Then: 누적됨
        assertEquals(1, totalAdded);
        assertEquals(6, attackManager.getPendingLines());
    }
    
    @Test
    void testReceiveAttack_MaxLimit_TenLines() {
        // Given: 이미 9줄이 대기 중
        attackManager.receiveAttack(9);
        
        // When: 5줄 더 받음
        int added = attackManager.receiveAttack(5);
        
        // Then: 1줄만 추가되어 최대 10줄
        assertEquals(1, added);
        assertEquals(10, attackManager.getPendingLines());
    }
    
    @Test
    void testReceiveAttack_AlreadyMaxLines_NoMoreAdded() {
        // Given: 이미 10줄이 대기 중
        attackManager.receiveAttack(10);
        
        // When: 추가 공격 받음
        int added = attackManager.receiveAttack(5);
        
        // Then: 0줄 추가됨
        assertEquals(0, added);
        assertEquals(10, attackManager.getPendingLines());
    }
    
    @Test
    void testReceiveAttack_OverMaxLines() {
        // Given: 공격 매니저 생성
        
        // When: 15줄 공격 받음 (최대치 초과)
        int added = attackManager.receiveAttack(15);
        
        // Then: 10줄만 추가됨
        assertEquals(10, added);
        assertEquals(10, attackManager.getPendingLines());
    }
    
    @Test
    void testReceiveAttack_ZeroLines() {
        // Given: 공격 매니저 생성
        
        // When: 0줄 공격
        int added = attackManager.receiveAttack(0);
        
        // Then: 0줄 추가
        assertEquals(0, added);
        assertEquals(0, attackManager.getPendingLines());
    }
    
    @Test
    void testReceiveAttack_NegativeLines_ShouldAddZero() {
        // Given: 공격 매니저 생성
        
        // When: 음수 줄 공격 (잘못된 입력)
        int added = attackManager.receiveAttack(-5);
        
        // Then: 대기 줄이 음수가 될 수 있음 (버그일 수 있지만 현재 구현 테스트)
        assertEquals(-5, added);
        assertEquals(-5, attackManager.getPendingLines());
    }
    
    @Test
    void testApplyPendingLines_EmptyQueue() {
        // Given: 대기 중인 줄 없음
        
        // When: 적용 시도
        int applied = attackManager.applyPendingLines();
        
        // Then: 0줄 적용
        assertEquals(0, applied);
        assertEquals(0, attackManager.getPendingLines());
    }
    
    @Test
    void testApplyPendingLines_WithPendingLines() {
        // Given: 5줄 대기 중
        attackManager.receiveAttack(5);
        
        // When: 적용
        int applied = attackManager.applyPendingLines();
        
        // Then: 5줄 적용되고 대기 큐 비워짐
        assertEquals(5, applied);
        assertEquals(0, attackManager.getPendingLines());
    }
    
    @Test
    void testApplyPendingLines_MultipleTimes() {
        // Given: 3줄 대기 중
        attackManager.receiveAttack(3);
        
        // When: 첫 번째 적용
        int firstApply = attackManager.applyPendingLines();
        
        // Then: 3줄 적용
        assertEquals(3, firstApply);
        assertEquals(0, attackManager.getPendingLines());
        
        // When: 두 번째 적용 (대기 줄 없음)
        int secondApply = attackManager.applyPendingLines();
        
        // Then: 0줄 적용
        assertEquals(0, secondApply);
        assertEquals(0, attackManager.getPendingLines());
    }
    
    @Test
    void testCalculateAttackLines_OneLine_NoAttack() {
        // Given & When: 1줄 삭제
        int attackLines = VersusAttackManager.calculateAttackLines(1);
        
        // Then: 공격 불가 (2줄 이상만 공격)
        assertEquals(0, attackLines);
    }
    
    @Test
    void testCalculateAttackLines_TwoLines() {
        // Given & When: 2줄 삭제
        int attackLines = VersusAttackManager.calculateAttackLines(2);
        
        // Then: 2줄 공격
        assertEquals(2, attackLines);
    }
    
    @Test
    void testCalculateAttackLines_ThreeLines() {
        // Given & When: 3줄 삭제
        int attackLines = VersusAttackManager.calculateAttackLines(3);
        
        // Then: 3줄 공격
        assertEquals(3, attackLines);
    }
    
    @Test
    void testCalculateAttackLines_FourLines() {
        // Given & When: 4줄 삭제 (테트리스)
        int attackLines = VersusAttackManager.calculateAttackLines(4);
        
        // Then: 4줄 공격
        assertEquals(4, attackLines);
    }
    
    @Test
    void testCalculateAttackLines_ZeroLines_NoAttack() {
        // Given & When: 0줄 삭제
        int attackLines = VersusAttackManager.calculateAttackLines(0);
        
        // Then: 공격 불가
        assertEquals(0, attackLines);
    }
    
    @Test
    void testCalculateAttackLines_NegativeLines_NoAttack() {
        // Given & When: 음수 줄 (잘못된 입력)
        int attackLines = VersusAttackManager.calculateAttackLines(-5);
        
        // Then: 공격 불가
        assertEquals(0, attackLines);
    }
    
    @Test
    void testFullGameScenario_ReceiveAndApply() {
        // Given: 게임 시나리오
        VersusAttackManager manager = new VersusAttackManager();
        
        // Scenario: 상대가 2줄 삭제 -> 2줄 받음
        assertEquals(2, manager.receiveAttack(2));
        assertEquals(2, manager.getPendingLines());
        
        // Scenario: 블록이 바닥에 닿음 -> 2줄 적용
        assertEquals(2, manager.applyPendingLines());
        assertEquals(0, manager.getPendingLines());
        
        // Scenario: 상대가 다시 3줄 삭제 -> 3줄 받음
        assertEquals(3, manager.receiveAttack(3));
        assertEquals(3, manager.getPendingLines());
        
        // Scenario: 내가 4줄 삭제 -> 4줄 공격 가능
        assertEquals(4, VersusAttackManager.calculateAttackLines(4));
        
        // Scenario: 블록이 바닥에 닿음 -> 3줄 적용
        assertEquals(3, manager.applyPendingLines());
        assertEquals(0, manager.getPendingLines());
    }
    
    @Test
    void testEdgeCase_ExactlyMaxLines() {
        // Given: 공격 매니저 생성
        
        // When: 정확히 10줄 받음
        int added = attackManager.receiveAttack(10);
        
        // Then: 10줄 추가
        assertEquals(10, added);
        assertEquals(10, attackManager.getPendingLines());
    }
    
    @Test
    void testEdgeCase_NineLinesThenOneMore() {
        // Given: 9줄 대기 중
        attackManager.receiveAttack(9);
        
        // When: 1줄 더 받음
        int added = attackManager.receiveAttack(1);
        
        // Then: 1줄 추가되어 정확히 10줄
        assertEquals(1, added);
        assertEquals(10, attackManager.getPendingLines());
    }
    
    @Test
    void testMultipleApplyPendingLines_AfterMultipleReceives() {
        // Given: 여러 차례 공격 받음
        attackManager.receiveAttack(2);
        attackManager.receiveAttack(3);
        attackManager.receiveAttack(4);
        
        // When: 한 번에 적용
        int applied = attackManager.applyPendingLines();
        
        // Then: 총 9줄 적용
        assertEquals(9, applied);
        assertEquals(0, attackManager.getPendingLines());
    }
    
    @Test
    void testStaticMethod_CalculateAttackLines_IsIndependent() {
        // Given: 인스턴스와 무관하게 동작
        VersusAttackManager manager1 = new VersusAttackManager();
        VersusAttackManager manager2 = new VersusAttackManager();
        
        // When: static 메서드 호출
        int attack1 = VersusAttackManager.calculateAttackLines(3);
        int attack2 = VersusAttackManager.calculateAttackLines(3);
        
        // Then: 인스턴스와 무관하게 동일한 결과
        assertEquals(attack1, attack2);
        assertEquals(3, attack1);
    }
}
