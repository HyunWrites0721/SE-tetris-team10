package settings;

import java.awt.Font;

/**
 * 한글 폰트 관리 유틸리티 클래스
 * 게임 전체에서 한글이 깨지지 않도록 일관된 폰트를 제공
 */
public class FontManager {
    private static final String KOREAN_FONT_NAME = "맑은 고딕";
    private static final String FALLBACK_FONT_NAME = Font.SANS_SERIF;
    
    /**
     * 한글 지원 폰트 생성
     * @param style 폰트 스타일 (Font.PLAIN, Font.BOLD 등)
     * @param size 폰트 크기
     * @return 한글을 지원하는 Font 객체
     */
    public static Font getKoreanFont(int style, int size) {
        Font font = new Font(KOREAN_FONT_NAME, style, size);
        
        // 맑은 고딕이 시스템에 없으면 기본 산세리프 폰트 사용
        if (!font.getFamily().equals(KOREAN_FONT_NAME)) {
            font = new Font(FALLBACK_FONT_NAME, style, size);
        }
        
        return font;
    }
    
    /**
     * 기본 한글 폰트 (PLAIN, 12pt)
     */
    public static Font getDefaultKoreanFont() {
        return getKoreanFont(Font.PLAIN, 12);
    }
}
