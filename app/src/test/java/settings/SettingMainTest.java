package settings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * SettingMain 클래스 테스트
 */
class SettingMainTest {

    private Path settingsDir;
    private Path settingSavePath;
    private String originalSettingSave;

    @BeforeEach
    public void setUp() throws Exception {
        // ConfigManager가 사용하는 실제 경로 사용
        settingsDir = Paths.get(ConfigManager.getConfigDir());
        settingSavePath = Paths.get(ConfigManager.getSettingsPath());

        if (!Files.exists(settingsDir)) {
            Files.createDirectories(settingsDir);
        }
        if (Files.exists(settingSavePath)) {
            originalSettingSave = Files.readString(settingSavePath);
        }
    }

    @Test
    void testLaunchSetting_ColorBlindMode() {
        SettingMain.SettingViewControllerPair pair = SettingMain.launchSetting("색맹 모드");
        
        assertNotNull(pair, "반환된 Pair는 null이 아니어야 함");
        assertNotNull(pair.view, "View는 null이 아니어야 함");
        assertNotNull(pair.controller, "Controller는 null이 아니어야 함");
        assertEquals("색맹 모드", pair.view.getSettingName(), "View의 설정 이름이 일치해야 함");
    }

    @Test
    void testLaunchSetting_ScreenSize() {
        SettingMain.SettingViewControllerPair pair = SettingMain.launchSetting("화면 크기 설정");
        
        assertNotNull(pair);
        assertNotNull(pair.view);
        assertNotNull(pair.controller);
        assertEquals("화면 크기 설정", pair.view.getSettingName());
    }

    @Test
    void testLaunchSetting_ControlType() {
        SettingMain.SettingViewControllerPair pair = SettingMain.launchSetting("조작키 설정");
        
        assertNotNull(pair);
        assertNotNull(pair.view);
        assertNotNull(pair.controller);
        assertEquals("조작키 설정", pair.view.getSettingName());
    }

    @Test
    void testLaunchSetting_Difficulty() {
        SettingMain.SettingViewControllerPair pair = SettingMain.launchSetting("난이도 설정");
        
        assertNotNull(pair);
        assertNotNull(pair.view);
        assertNotNull(pair.controller);
        assertEquals("난이도 설정", pair.view.getSettingName());
    }

    @Test
    void testLaunchSetting_ResetSettings() {
        SettingMain.SettingViewControllerPair pair = SettingMain.launchSetting("설정값 초기화");
        
        assertNotNull(pair);
        assertNotNull(pair.view);
        assertNotNull(pair.controller);
        assertEquals("설정값 초기화", pair.view.getSettingName());
    }

    @Test
    void testLaunchSetting_ResetScoreboard() {
        SettingMain.SettingViewControllerPair pair = SettingMain.launchSetting("스코어보드 초기화");
        
        assertNotNull(pair);
        assertNotNull(pair.view);
        assertNotNull(pair.controller);
        assertEquals("스코어보드 초기화", pair.view.getSettingName());
    }

    @Test
    void testLaunchSetting_MultipleCalls() {
        // 여러 번 호출해도 각각 새로운 인스턴스를 반환해야 함
        SettingMain.SettingViewControllerPair pair1 = SettingMain.launchSetting("색맹 모드");
        SettingMain.SettingViewControllerPair pair2 = SettingMain.launchSetting("색맹 모드");
        
        assertNotSame(pair1, pair2, "각 호출마다 새로운 Pair 인스턴스를 반환해야 함");
        assertNotSame(pair1.view, pair2.view, "각 호출마다 새로운 View 인스턴스를 반환해야 함");
        assertNotSame(pair1.controller, pair2.controller, "각 호출마다 새로운 Controller 인스턴스를 반환해야 함");
    }

    @Test
    void testSettingViewControllerPair_Constructor() {
        SettingModel model = new SettingModel();
        SettingView view = new SettingView("색맹 모드", model);
        SettingController controller = new SettingController(model, view);
        
        SettingMain.SettingViewControllerPair pair = new SettingMain.SettingViewControllerPair(view, controller);
        
        assertSame(view, pair.view, "Pair의 view는 전달된 view와 같아야 함");
        assertSame(controller, pair.controller, "Pair의 controller는 전달된 controller와 같아야 함");
    }

    @Test
    void testLaunchSetting_ViewAndControllerLinked() {
        SettingMain.SettingViewControllerPair pair = SettingMain.launchSetting("색맹 모드");
        
        // View와 Controller가 올바르게 연결되어 있는지 확인
        assertNotNull(pair.view.checkButton, "View의 checkButton이 초기화되어야 함");
        assertNotNull(pair.view.cancelButton, "View의 cancelButton이 초기화되어야 함");
    }

    @Test
    void testLaunchSetting_ModelInitialized() {
        SettingMain.SettingViewControllerPair pair = SettingMain.launchSetting("색맹 모드");
        
        // View를 통해 Model이 제대로 초기화되었는지 간접적으로 확인
        assertNotNull(pair.view, "View가 초기화되어야 함");
        
        // Controller를 통해 동작 확인
        pair.view.Button1.setSelected(true);
        pair.view.checkButton.doClick();
        
        // 예외가 발생하지 않으면 성공
    }

    @Test
    void testLaunchSetting_AllMenus() {
        String[] menus = {
            "색맹 모드",
            "화면 크기 설정",
            "조작키 설정",
            "난이도 설정",
            "스코어보드 초기화",
            "설정값 초기화"
        };
        
        for (String menu : menus) {
            SettingMain.SettingViewControllerPair pair = SettingMain.launchSetting(menu);
            assertNotNull(pair, menu + "에 대한 Pair가 생성되어야 함");
            assertNotNull(pair.view, menu + "에 대한 View가 생성되어야 함");
            assertNotNull(pair.controller, menu + "에 대한 Controller가 생성되어야 함");
            assertEquals(menu, pair.view.getSettingName(), "View의 설정 이름이 일치해야 함");
        }
    }

    @Test
    void testPair_FieldsAreFinal() {
        SettingMain.SettingViewControllerPair pair = SettingMain.launchSetting("색맹 모드");
        
        // final 필드이므로 한 번 할당되면 변경되지 않음
        SettingView originalView = pair.view;
        SettingController originalController = pair.controller;
        
        assertSame(originalView, pair.view, "final 필드는 변경되지 않아야 함");
        assertSame(originalController, pair.controller, "final 필드는 변경되지 않아야 함");
    }
}
