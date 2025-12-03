package settings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Font;

/**
 * FontManager 클래스 테스트
 */
class FontManagerTest {

    @Test
    void testGetKoreanFont_ValidParameters() {
        Font font = FontManager.getKoreanFont(Font.PLAIN, 12);
        
        assertNotNull(font);
        assertEquals(12, font.getSize());
        assertEquals(Font.PLAIN, font.getStyle());
    }

    @Test
    void testGetKoreanFont_BoldStyle() {
        Font font = FontManager.getKoreanFont(Font.BOLD, 16);
        
        assertEquals(Font.BOLD, font.getStyle());
        assertEquals(16, font.getSize());
    }

    @Test
    void testGetKoreanFont_ItalicStyle() {
        Font font = FontManager.getKoreanFont(Font.ITALIC, 14);
        
        assertEquals(Font.ITALIC, font.getStyle());
        assertEquals(14, font.getSize());
    }

    @Test
    void testGetKoreanFont_BoldItalicStyle() {
        Font font = FontManager.getKoreanFont(Font.BOLD | Font.ITALIC, 18);
        
        assertEquals(Font.BOLD | Font.ITALIC, font.getStyle());
        assertEquals(18, font.getSize());
    }

    @Test
    void testGetKoreanFont_DifferentSizes() {
        Font font10 = FontManager.getKoreanFont(Font.PLAIN, 10);
        Font font20 = FontManager.getKoreanFont(Font.PLAIN, 20);
        Font font30 = FontManager.getKoreanFont(Font.PLAIN, 30);
        
        assertEquals(10, font10.getSize());
        assertEquals(20, font20.getSize());
        assertEquals(30, font30.getSize());
    }

    @Test
    void testGetKoreanFont_ZeroSize() {
        Font font = FontManager.getKoreanFont(Font.PLAIN, 0);
        
        assertNotNull(font);
        assertEquals(0, font.getSize());
    }

    @Test
    void testGetKoreanFont_NegativeSize() {
        Font font = FontManager.getKoreanFont(Font.PLAIN, -5);
        
        assertNotNull(font);
        // 음수 크기도 Font 객체는 생성됨
    }

    @Test
    void testGetKoreanFont_LargeSize() {
        Font font = FontManager.getKoreanFont(Font.PLAIN, 100);
        
        assertEquals(100, font.getSize());
    }

    @Test
    void testGetDefaultKoreanFont() {
        Font defaultFont = FontManager.getDefaultKoreanFont();
        
        assertNotNull(defaultFont);
        assertEquals(Font.PLAIN, defaultFont.getStyle());
        assertEquals(12, defaultFont.getSize());
    }

    @Test
    void testGetDefaultKoreanFont_IsConsistent() {
        Font font1 = FontManager.getDefaultKoreanFont();
        Font font2 = FontManager.getDefaultKoreanFont();
        
        // 같은 설정의 폰트여야 함
        assertEquals(font1.getStyle(), font2.getStyle());
        assertEquals(font1.getSize(), font2.getSize());
        assertEquals(font1.getName(), font2.getName());
    }

    @Test
    void testFontName_IsKoreanOrFallback() {
        Font font = FontManager.getKoreanFont(Font.PLAIN, 12);
        String fontName = font.getFamily();
        
        // 맑은 고딕 또는 fallback 폰트여야 함
        assertTrue(
            fontName.equals("맑은 고딕") || 
            fontName.equals(Font.SANS_SERIF) ||
            fontName.contains("Sans"),
            "폰트는 맑은 고딕이거나 기본 Sans-Serif여야 함"
        );
    }

    @Test
    void testMultipleFonts_AreDifferentInstances() {
        Font font1 = FontManager.getKoreanFont(Font.PLAIN, 12);
        Font font2 = FontManager.getKoreanFont(Font.PLAIN, 12);
        
        // 같은 설정이지만 다른 인스턴스
        assertNotSame(font1, font2);
        assertEquals(font1, font2);
    }

    @Test
    void testFont_IsNotNull() {
        assertNotNull(FontManager.getKoreanFont(Font.PLAIN, 12));
        assertNotNull(FontManager.getKoreanFont(Font.BOLD, 14));
        assertNotNull(FontManager.getKoreanFont(Font.ITALIC, 16));
        assertNotNull(FontManager.getDefaultKoreanFont());
    }

    @Test
    void testFontStyles_AllValid() {
        int[] styles = {Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD | Font.ITALIC};
        
        for (int style : styles) {
            Font font = FontManager.getKoreanFont(style, 12);
            assertNotNull(font);
            assertEquals(style, font.getStyle());
        }
    }

    @Test
    void testFontSizeRange() {
        int[] sizes = {8, 10, 12, 14, 16, 18, 20, 24, 28, 32, 48, 72};
        
        for (int size : sizes) {
            Font font = FontManager.getKoreanFont(Font.PLAIN, size);
            assertNotNull(font);
            assertEquals(size, font.getSize());
        }
    }

    @Test
    void testGetKoreanFont_ThreadSafe() throws InterruptedException {
        Thread[] threads = new Thread[10];
        Font[] fonts = new Font[10];
        
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                fonts[index] = FontManager.getKoreanFont(Font.PLAIN, 12 + index);
            });
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 모든 폰트가 생성되었는지 확인
        for (Font font : fonts) {
            assertNotNull(font);
        }
    }

    @Test
    void testFontEquality() {
        Font font1 = FontManager.getKoreanFont(Font.BOLD, 16);
        Font font2 = FontManager.getKoreanFont(Font.BOLD, 16);
        
        assertEquals(font1, font2, "같은 설정의 폰트는 동등해야 함");
    }

    @Test
    void testFontInequality() {
        Font font1 = FontManager.getKoreanFont(Font.PLAIN, 12);
        Font font2 = FontManager.getKoreanFont(Font.BOLD, 12);
        Font font3 = FontManager.getKoreanFont(Font.PLAIN, 14);
        
        assertNotEquals(font1, font2, "스타일이 다르면 다른 폰트");
        assertNotEquals(font1, font3, "크기가 다르면 다른 폰트");
    }

    @Test
    void testFontCanBeUsedInComponent() {
        Font font = FontManager.getKoreanFont(Font.PLAIN, 12);
        
        // Font 객체가 정상적으로 생성되어 컴포넌트에 사용 가능한지 확인
        assertNotNull(font.getFamily());
        assertNotNull(font.getName());
        assertTrue(font.getSize() > 0 || font.getSize() == 0);
    }
}
