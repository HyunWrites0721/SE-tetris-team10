package blocks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BlockTest {
    
    @Test
    public void testColorBlindModeSetting() throws Exception {
        // 테스트용 JSON 파일 생성 (colorBlindMode: false)
        String jsonFalse = "{\"colorBlindMode\": false}";
        Files.write(Paths.get("SettingGave.json"), jsonFalse.getBytes());
        
        // 일반 모드(false)에서 테스트
        IBlock blockNormal = new IBlock();
        Color normalColor = blockNormal.getColor();
        int normalIndex = blockNormal.setBlindColor_1();
        assertEquals(0, normalIndex, "Normal mode should return index 0");
        
        // 테스트용 JSON 파일 수정 (colorBlindMode: true)
        String jsonTrue = "{\"colorBlindMode\": true}";
        Files.write(Paths.get("SettingGave.json"), jsonTrue.getBytes());
        
        // 색맹 모드(true)에서 테스트
        IBlock blockColorBlind = new IBlock();
        Color colorBlindColor = blockColorBlind.getColor();
        int colorBlindIndex = blockColorBlind.setBlindColor_1();
        assertEquals(1, colorBlindIndex, "Color blind mode should return index 1");
        
        // 두 모드에서 반환된 색상이 다른지 확인
        assertNotEquals(normalColor, colorBlindColor, "Colors should be different between normal and colorblind mode");
        
        // 테스트 후 파일 삭제
        Files.deleteIfExists(Paths.get("SettingGave.json"));
    }
}
