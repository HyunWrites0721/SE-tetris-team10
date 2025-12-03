package game.panels;

import game.model.AttackPreviewItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AttackPreviewPanel 테스트")
class AttackPreviewPanelTest {
    
    private AttackPreviewPanel panel;
    
    @BeforeEach
    void setUp() {
        panel = new AttackPreviewPanel();
    }
    
    @Test
    @DisplayName("초기 상태 - 빈 공격 큐")
    void testInitialState() {
        assertNotNull(panel, "패널이 생성되어야 함");
        assertDoesNotThrow(() -> panel.repaint(), "초기 상태에서 repaint 가능해야 함");
    }
    
    @Test
    @DisplayName("공격 큐 업데이트 - 단일 아이템")
    void testUpdateQueueSingleItem() {
        int[][] pattern = {{1, 0, 1}};
        AttackPreviewItem item = new AttackPreviewItem(2, pattern, 3);
        List<AttackPreviewItem> queue = Collections.singletonList(item);
        
        assertDoesNotThrow(() -> panel.updateQueue(queue), "공격 큐 업데이트는 예외를 발생시키지 않아야 함");
    }
    
    @Test
    @DisplayName("공격 큐 업데이트 - 여러 아이템")
    void testUpdateQueueMultipleItems() {
        List<AttackPreviewItem> queue = new ArrayList<>();
        queue.add(new AttackPreviewItem(1, new int[][]{{1, 0}}, 1));
        queue.add(new AttackPreviewItem(2, new int[][]{{0, 1}}, 2));
        queue.add(new AttackPreviewItem(3, new int[][]{{1, 1}}, 3));
        
        assertDoesNotThrow(() -> panel.updateQueue(queue), "여러 아이템 업데이트는 예외를 발생시키지 않아야 함");
    }
    
    @Test
    @DisplayName("null 큐 업데이트")
    void testUpdateQueueNull() {
        assertDoesNotThrow(() -> panel.updateQueue(null), "null 큐 업데이트는 예외를 발생시키지 않아야 함");
    }
    
    @Test
    @DisplayName("빈 큐 업데이트")
    void testUpdateQueueEmpty() {
        List<AttackPreviewItem> emptyQueue = Collections.emptyList();
        assertDoesNotThrow(() -> panel.updateQueue(emptyQueue), "빈 큐 업데이트는 예외를 발생시키지 않아야 함");
    }
    
    @Test
    @DisplayName("큐를 null로 초기화 후 다시 업데이트")
    void testUpdateQueueAfterNull() {
        panel.updateQueue(null);
        
        List<AttackPreviewItem> queue = Collections.singletonList(
            new AttackPreviewItem(1, new int[][]{{1}}, 1)
        );
        assertDoesNotThrow(() -> panel.updateQueue(queue), "null 후 업데이트가 가능해야 함");
    }
    
    @Test
    @DisplayName("셀 크기 설정")
    void testSetCellSize() {
        assertDoesNotThrow(() -> panel.setCellSize(40), "셀 크기 설정은 예외를 발생시키지 않아야 함");
        assertDoesNotThrow(() -> panel.setCellSize(15), "작은 셀 크기도 설정 가능해야 함");
    }
    
    @Test
    @DisplayName("폰트 크기 설정")
    void testSetFontSize() {
        assertDoesNotThrow(() -> panel.setFontSize(20), "폰트 크기 설정은 예외를 발생시키지 않아야 함");
        assertDoesNotThrow(() -> panel.setFontSize(10), "작은 폰트 크기도 설정 가능해야 함");
    }
    
    @Test
    @DisplayName("큐 업데이트 후 크기 변경")
    void testUpdateQueueThenResize() {
        List<AttackPreviewItem> queue = Collections.singletonList(
            new AttackPreviewItem(5, new int[][]{{1, 0, 1}}, 2)
        );
        panel.updateQueue(queue);
        panel.setCellSize(25);
        
        assertDoesNotThrow(() -> panel.repaint(), "큐 업데이트 후 크기 변경이 가능해야 함");
    }
    
    @Test
    @DisplayName("큰 공격 큐 처리")
    void testLargeQueue() {
        List<AttackPreviewItem> largeQueue = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            largeQueue.add(new AttackPreviewItem(1, new int[][]{{1}}, i % 10));
        }
        
        assertDoesNotThrow(() -> panel.updateQueue(largeQueue), "큰 공격 큐도 처리 가능해야 함");
    }
    
    @Test
    @DisplayName("null 패턴 아이템 처리")
    void testNullPatternItem() {
        AttackPreviewItem item = new AttackPreviewItem(3, null, 5);
        List<AttackPreviewItem> queue = Collections.singletonList(item);
        
        assertDoesNotThrow(() -> panel.updateQueue(queue), "null 패턴 아이템도 처리 가능해야 함");
    }
}
