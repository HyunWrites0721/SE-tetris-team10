package blocks;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.Color;

/**
 * 블록의 색상 시스템(일반 모드 / 색맹 모드)을 테스트하는 클래스
 */
public class BlockColorTest {
    
    private Path settingsDir;
    private Path settingsPath;
    private String originalContent;
    
    @BeforeEach
    public void setUp() throws Exception {
        settingsPath = Paths.get(settings.ConfigManager.getSettingsPath());
        settingsDir = settingsPath.getParent();
        
        if (!Files.exists(settingsDir)) {
            Files.createDirectories(settingsDir);
        }
        
        if (Files.exists(settingsPath)) {
            originalContent = Files.readString(settingsPath);
        }
    }
    
    @org.junit.jupiter.api.AfterEach
    public void tearDown() throws Exception {
        if (originalContent != null && Files.exists(settingsPath)) {
            Files.writeString(settingsPath, originalContent);
        } else if (originalContent == null && Files.exists(settingsPath)) {
            Files.deleteIfExists(settingsPath);
        }
    }
    
    @Test
    public void testNormalMode_Colors() throws Exception {
        // 일반 모드 색상 테스트
        String jsonNormal = "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}";
        Files.writeString(settingsPath, jsonNormal);
        
        Block.reloadSettings();
        
        // 각 블록의 색상이 일반 모드 팔레트에서 나오는지 확인
        Block[] blocks = {
            new IBlock(), new JBlock(), new LBlock(), new OBlock(),
            new SBlock(), new TBlock(), new ZBlock()
        };
        
        for (Block block : blocks) {
            Color color = block.getColor();
            assertNotNull(color, block.getClass().getSimpleName() + "의 색상은 null이 아니어야 합니다");
            
            // 일반 모드 색상 팔레트 (Color.green, red, blue, orange, yellow, magenta, CYAN)
            assertTrue(
                color.equals(Color.green) || color.equals(Color.red) || 
                color.equals(Color.blue) || color.equals(Color.orange) || 
                color.equals(Color.yellow) || color.equals(Color.magenta) || 
                color.equals(Color.CYAN),
                block.getClass().getSimpleName() + "의 색상이 일반 팔레트에 속해야 합니다"
            );
        }
    }
    
    @Test
    public void testColorBlindMode_Colors() throws Exception {
        // 색맹 모드 색상 테스트
        String jsonColorBlind = "{\"colorBlindMode\":true,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}";
        Files.writeString(settingsPath, jsonColorBlind);
        
        Block.reloadSettings();
        
        Block[] blocks = {
            new IBlock(), new JBlock(), new LBlock(), new OBlock(),
            new SBlock(), new TBlock(), new ZBlock()
        };
        
        // 색맹 모드 팔레트 색상들
        Color[] colorBlindPalette = {
            new Color(0, 158, 115),   // bluish green
            new Color(213, 94, 0),    // vermilion
            new Color(0, 114, 178),   // blue
            new Color(230, 159, 0),   // orange
            new Color(240, 228, 66),  // yellow
            new Color(204, 121, 167), // magenta
            new Color(86, 180, 233)   // pink
        };
        
        for (Block block : blocks) {
            Color color = block.getColor();
            assertNotNull(color, block.getClass().getSimpleName() + "의 색상은 null이 아니어야 합니다");
            
            // 색맹 모드 팔레트에 속하는지 확인
            boolean inPalette = false;
            for (Color paletteColor : colorBlindPalette) {
                if (color.equals(paletteColor)) {
                    inPalette = true;
                    break;
                }
            }
            assertTrue(inPalette, 
                block.getClass().getSimpleName() + "의 색상이 색맹 모드 팔레트에 속해야 합니다");
        }
    }
    
    @Test
    public void testSetBlindColor_ReturnValues() throws Exception {
        // setBlindColor_1() 메서드가 올바른 값을 반환하는지 테스트
        
        // 일반 모드
        String jsonNormal = "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}";
        Files.writeString(settingsPath, jsonNormal);
        Block.reloadSettings();
        
        Block normalBlock = new IBlock();
        assertEquals(0, normalBlock.setBlindColor_1(), "일반 모드에서는 0을 반환해야 합니다");
        
        // 색맹 모드
        String jsonColorBlind = "{\"colorBlindMode\":true,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}";
        Files.writeString(settingsPath, jsonColorBlind);
        Block.reloadSettings();
        
        Block colorBlindBlock = new IBlock();
        assertEquals(1, colorBlindBlock.setBlindColor_1(), "색맹 모드에서는 1을 반환해야 합니다");
    }
    
    @Test
    public void testSetColor_ValidIndices() {
        // setColor 메서드가 유효한 인덱스로 작동하는지 테스트
        // 인덱스 0~6까지 설정 가능 (7개 블록 색상)
        for (int i = 0; i < 7; i++) {
            final int index = i;
            assertDoesNotThrow(() -> {
                Block testBlock = new TBlock();
                testBlock.setColor(0, index);
                assertNotNull(testBlock.getColor(), "인덱스 " + index + "로 설정한 색상은 null이 아니어야 합니다");
            }, "인덱스 " + i + "는 유효해야 합니다");
        }
    }
    
    @Test
    public void testSetExactColor() {
        // setExactColor 메서드 테스트 (아이템 블록용)
        Block block = new TBlock();
        Color customColor = new Color(123, 45, 67);
        
        block.setExactColor(customColor);
        
        assertEquals(customColor, block.getColor(), "setExactColor로 설정한 색상이 정확히 적용되어야 합니다");
    }
    
    @Test
    public void testGetColor() {
        // getColor 메서드가 null을 반환하지 않는지 테스트
        Block[] blocks = {
            new IBlock(), new JBlock(), new LBlock(), new OBlock(),
            new SBlock(), new TBlock(), new ZBlock()
        };
        
        for (Block block : blocks) {
            Color color = block.getColor();
            assertNotNull(color, block.getClass().getSimpleName() + "의 getColor()는 null을 반환하면 안됩니다");
        }
    }
    
    @Test
    public void testColorConsistency_AfterMultipleCalls() {
        // 같은 블록 타입은 생성 시마다 같은 색상을 가져야 함
        IBlock block1 = new IBlock();
        IBlock block2 = new IBlock();
        
        assertEquals(block1.getColor(), block2.getColor(), 
            "같은 타입의 블록은 같은 색상을 가져야 합니다");
    }
    
    @Test
    public void testColorBlindMode_DifferentFromNormalMode() throws Exception {
        // 색맹 모드와 일반 모드의 색상이 다른지 확인
        
        // 일반 모드
        String jsonNormal = "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}";
        Files.writeString(settingsPath, jsonNormal);
        Block.reloadSettings();
        
        IBlock normalBlock = new IBlock();
        Color normalColor = normalBlock.getColor();
        
        // 색맹 모드
        String jsonColorBlind = "{\"colorBlindMode\":true,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}";
        Files.writeString(settingsPath, jsonColorBlind);
        Block.reloadSettings();
        
        IBlock colorBlindBlock = new IBlock();
        Color colorBlindColor = colorBlindBlock.getColor();
        
        assertNotEquals(normalColor, colorBlindColor, 
            "IBlock의 일반 모드 색상과 색맹 모드 색상은 달라야 합니다");
    }
    
    @Test
    public void testItemBlocks_BlackWhiteColors() {
        // 아이템 블록들의 특수 색상 테스트
        blocks.item.AllClearBlock allClear = new blocks.item.AllClearBlock();
        blocks.item.BoxClearBlock boxClear = new blocks.item.BoxClearBlock();
        blocks.item.WeightBlock weight = new blocks.item.WeightBlock();
        
        // AllClear, BoxClear는 검정색(인덱스 7)
        Color blackColor = allClear.getColor();
        assertNotNull(blackColor, "AllClear의 색상은 null이 아니어야 합니다");
        
        Color boxColor = boxClear.getColor();
        assertNotNull(boxColor, "BoxClear의 색상은 null이 아니어야 합니다");
        
        // Weight는 흰색(인덱스 8)
        Color whiteColor = weight.getColor();
        assertNotNull(whiteColor, "WeightBlock의 색상은 null이 아니어야 합니다");
    }
    
    @Test
    public void testReloadSettings_UpdatesColorMode() throws Exception {
        // reloadSettings() 호출 시 색상 모드가 업데이트되는지 테스트
        
        // 초기: 일반 모드
        String jsonNormal = "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}";
        Files.writeString(settingsPath, jsonNormal);
        Block.reloadSettings();
        
        IBlock block1 = new IBlock();
        Color normalColor = block1.getColor();
        
        // 색맹 모드로 변경
        String jsonColorBlind = "{\"colorBlindMode\":true,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}";
        Files.writeString(settingsPath, jsonColorBlind);
        Block.reloadSettings();
        
        IBlock block2 = new IBlock();
        Color colorBlindColor = block2.getColor();
        
        assertNotEquals(normalColor, colorBlindColor, 
            "설정 리로드 후 색상이 변경되어야 합니다");
    }
}
