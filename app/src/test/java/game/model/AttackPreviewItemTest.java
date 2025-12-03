package game.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AttackPreviewItem 테스트")
class AttackPreviewItemTest {
    
    @Test
    @DisplayName("기본 생성자로 생성")
    void testConstructor() {
        int[][] pattern = {
            {1, 0, 1},
            {0, 1, 0}
        };
        
        AttackPreviewItem item = new AttackPreviewItem(3, pattern, 5);
        
        assertEquals(3, item.lines, "lines 값이 3이어야 함");
        assertEquals(5, item.blockX, "blockX 값이 5여야 함");
        assertNotNull(item.pattern, "pattern이 null이 아니어야 함");
    }
    
    @Test
    @DisplayName("패턴 딥 카피 확인 - 원본 배열 변경이 복사본에 영향 없음")
    void testPatternDeepCopy() {
        int[][] originalPattern = {
            {1, 0, 1},
            {0, 1, 0}
        };
        
        AttackPreviewItem item = new AttackPreviewItem(2, originalPattern, 3);
        
        // 원본 배열 변경
        originalPattern[0][0] = 9;
        originalPattern[1][1] = 8;
        
        // item의 pattern은 변경되지 않아야 함
        assertEquals(1, item.pattern[0][0], "딥 카피로 인해 원본 변경이 영향 없어야 함");
        assertEquals(1, item.pattern[1][1], "딥 카피로 인해 원본 변경이 영향 없어야 함");
    }
    
    @Test
    @DisplayName("패턴 딥 카피 확인 - item의 패턴 변경이 외부에 영향 없음")
    void testPatternDeepCopyReverse() {
        int[][] originalPattern = {
            {1, 0, 1},
            {0, 1, 0}
        };
        
        AttackPreviewItem item = new AttackPreviewItem(2, originalPattern, 3);
        
        // item의 pattern 변경
        item.pattern[0][0] = 7;
        item.pattern[1][1] = 6;
        
        // 원본 배열은 변경되지 않아야 함
        assertEquals(1, originalPattern[0][0], "딥 카피로 인해 item 변경이 원본에 영향 없어야 함");
        assertEquals(1, originalPattern[1][1], "딥 카피로 인해 item 변경이 원본에 영향 없어야 함");
    }
    
    @Test
    @DisplayName("null 패턴으로 생성")
    void testConstructorWithNullPattern() {
        AttackPreviewItem item = new AttackPreviewItem(5, null, 2);
        
        assertEquals(5, item.lines, "lines 값이 5여야 함");
        assertEquals(2, item.blockX, "blockX 값이 2여야 함");
        assertNull(item.pattern, "null 패턴은 null로 유지되어야 함");
    }
    
    @Test
    @DisplayName("빈 패턴으로 생성")
    void testConstructorWithEmptyPattern() {
        int[][] emptyPattern = new int[0][];
        
        AttackPreviewItem item = new AttackPreviewItem(0, emptyPattern, 0);
        
        assertEquals(0, item.lines, "lines 값이 0이어야 함");
        assertEquals(0, item.blockX, "blockX 값이 0이어야 함");
        assertNotNull(item.pattern, "빈 패턴도 null이 아니어야 함");
        assertEquals(0, item.pattern.length, "빈 패턴의 길이는 0이어야 함");
    }
    
    @Test
    @DisplayName("단일 행 패턴으로 생성")
    void testConstructorWithSingleRowPattern() {
        int[][] singleRowPattern = {
            {1, 1, 1, 1}
        };
        
        AttackPreviewItem item = new AttackPreviewItem(1, singleRowPattern, 0);
        
        assertEquals(1, item.lines, "lines 값이 1이어야 함");
        assertEquals(1, item.pattern.length, "패턴의 행 수가 1이어야 함");
        assertEquals(4, item.pattern[0].length, "패턴의 열 수가 4여야 함");
        assertArrayEquals(new int[]{1, 1, 1, 1}, item.pattern[0], "패턴 내용이 일치해야 함");
    }
    
    @Test
    @DisplayName("큰 패턴으로 생성")
    void testConstructorWithLargePattern() {
        int[][] largePattern = new int[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                largePattern[i][j] = (i + j) % 2;
            }
        }
        
        AttackPreviewItem item = new AttackPreviewItem(10, largePattern, 5);
        
        assertEquals(10, item.lines, "lines 값이 10이어야 함");
        assertEquals(10, item.pattern.length, "패턴의 행 수가 10이어야 함");
        assertEquals(10, item.pattern[0].length, "패턴의 열 수가 10이어야 함");
        
        // 패턴 내용 확인
        for (int i = 0; i < 10; i++) {
            assertArrayEquals(largePattern[i], item.pattern[i], 
                    "행 " + i + "의 내용이 일치해야 함");
        }
    }
    
    @Test
    @DisplayName("음수 lines 값으로 생성")
    void testConstructorWithNegativeLines() {
        int[][] pattern = {{1, 0}};
        
        AttackPreviewItem item = new AttackPreviewItem(-1, pattern, 0);
        
        assertEquals(-1, item.lines, "음수 lines 값도 허용되어야 함");
    }
    
    @Test
    @DisplayName("음수 blockX 값으로 생성")
    void testConstructorWithNegativeBlockX() {
        int[][] pattern = {{1, 0}};
        
        AttackPreviewItem item = new AttackPreviewItem(1, pattern, -5);
        
        assertEquals(-5, item.blockX, "음수 blockX 값도 허용되어야 함");
    }
    
    @Test
    @DisplayName("불규칙한 패턴 배열로 생성 (jagged array)")
    void testConstructorWithJaggedArray() {
        int[][] jaggedPattern = {
            {1, 0, 1},
            {0, 1},
            {1, 1, 1, 1}
        };
        
        AttackPreviewItem item = new AttackPreviewItem(3, jaggedPattern, 2);
        
        assertEquals(3, item.lines, "lines 값이 3이어야 함");
        assertEquals(3, item.pattern.length, "패턴의 행 수가 3이어야 함");
        assertEquals(3, item.pattern[0].length, "첫 번째 행의 길이가 3이어야 함");
        assertEquals(2, item.pattern[1].length, "두 번째 행의 길이가 2여야 함");
        assertEquals(4, item.pattern[2].length, "세 번째 행의 길이가 4여야 함");
    }
    
    @Test
    @DisplayName("0 값으로 모든 필드 생성")
    void testConstructorWithAllZeros() {
        int[][] pattern = {{0, 0, 0}};
        
        AttackPreviewItem item = new AttackPreviewItem(0, pattern, 0);
        
        assertEquals(0, item.lines, "lines 값이 0이어야 함");
        assertEquals(0, item.blockX, "blockX 값이 0이어야 함");
        assertArrayEquals(new int[]{0, 0, 0}, item.pattern[0], "패턴이 모두 0이어야 함");
    }
    
    @Test
    @DisplayName("매우 큰 blockX 값으로 생성")
    void testConstructorWithLargeBlockX() {
        int[][] pattern = {{1}};
        
        AttackPreviewItem item = new AttackPreviewItem(1, pattern, Integer.MAX_VALUE);
        
        assertEquals(Integer.MAX_VALUE, item.blockX, "매우 큰 blockX 값도 허용되어야 함");
    }
    
    @Test
    @DisplayName("매우 큰 lines 값으로 생성")
    void testConstructorWithLargeLines() {
        int[][] pattern = {{1}};
        
        AttackPreviewItem item = new AttackPreviewItem(Integer.MAX_VALUE, pattern, 0);
        
        assertEquals(Integer.MAX_VALUE, item.lines, "매우 큰 lines 값도 허용되어야 함");
    }
    
    @Test
    @DisplayName("패턴의 독립성 확인 - 여러 item 생성")
    void testMultipleItemsIndependence() {
        int[][] pattern1 = {{1, 0}};
        int[][] pattern2 = {{0, 1}};
        
        AttackPreviewItem item1 = new AttackPreviewItem(1, pattern1, 0);
        AttackPreviewItem item2 = new AttackPreviewItem(2, pattern2, 1);
        
        // item1 패턴 변경
        item1.pattern[0][0] = 9;
        
        // item2는 영향받지 않아야 함
        assertEquals(0, item2.pattern[0][0], "item2의 패턴은 독립적이어야 함");
        
        // 원본 배열도 영향받지 않아야 함
        assertEquals(1, pattern1[0][0], "원본 배열은 독립적이어야 함");
    }
    
    @Test
    @DisplayName("final 필드 확인 - 재할당 불가")
    void testFinalFields() {
        int[][] pattern = {{1, 0}};
        AttackPreviewItem item = new AttackPreviewItem(3, pattern, 5);
        
        // final 필드이므로 재할당 불가 (컴파일 타임 체크)
        // item.lines = 10; // 컴파일 에러
        // item.blockX = 20; // 컴파일 에러
        // item.pattern = null; // 컴파일 에러
        
        // 하지만 배열의 내용은 변경 가능
        item.pattern[0][0] = 7;
        assertEquals(7, item.pattern[0][0], "배열 내용은 변경 가능해야 함");
    }
}
