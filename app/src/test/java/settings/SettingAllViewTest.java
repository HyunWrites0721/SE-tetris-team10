package settings;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.swing.JRadioButton;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * SettingView/SettingMain 전반 동작을 폭넓게 확인하는 테스트 모음
 */
public class SettingAllViewTest {

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

	@AfterEach
	public void tearDown() throws Exception {
		if (originalSettingSave != null && Files.exists(settingSavePath)) {
			Files.writeString(settingSavePath, originalSettingSave);
		}
	}

	// 색맹 모드 뷰가 라디오 버튼 2개(끔/켬)를 표시하고, 파일 설정값에 맞춰 초기 선택 상태가 반영되는지 확인
	@Test
	void view_colorBlind_initialSelection_fromFile() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":true,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}");

		SettingModel model = new SettingModel();
		SettingView view = new SettingView("색맹 모드", model);

		assertNotNull(view.Button1);
		assertNotNull(view.Button2);
		assertEquals("끔", view.Button1.getText());
		assertEquals("켬", view.Button2.getText());

		// 파일에서 colorBlindMode=true로 저장했으므로 '켬'이 선택되어야 함
		assertTrue(view.Button2.isSelected());
		assertFalse(view.Button1.isSelected());

		// 확인/취소 버튼도 있어야 함
		assertNotNull(view.checkButton);
		assertNotNull(view.cancelButton);
	}

	// 화면 크기 뷰가 3개의 옵션을 표시하고, 파일에 저장된 screenSize가 초기 선택에 반영되는지 확인
	@Test
	void view_screenSize_initialSelection_fromFile() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"large\",\"difficulty\":\"normal\"}");

		SettingModel model = new SettingModel();
		SettingView view = new SettingView("화면 크기 설정", model);

		assertNotNull(view.Button1);
		assertNotNull(view.Button2);
		assertNotNull(view.Button3);
		assertEquals("작은 화면", view.Button1.getText());
		assertEquals("보통 화면", view.Button2.getText());
		assertEquals("큰 화면", view.Button3.getText());

		// 파일에서 screenSize=large이므로 세 번째 버튼이 선택되어야 함
		assertTrue(view.Button3.isSelected());
		assertFalse(view.Button1.isSelected());
		assertFalse(view.Button2.isSelected());

		assertNotNull(view.checkButton);
		assertNotNull(view.cancelButton);
	}

	// 조작키 뷰가 2개의 옵션(방향키/WASD)을 표시하고, 파일 설정값이 초기 선택에 반영되는지 확인
	@Test
	void view_controlType_initialSelection_fromFile() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":false,\"controlType\":\"wasd\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}");

		SettingModel model = new SettingModel();
		SettingView view = new SettingView("조작키 설정", model);

		assertNotNull(view.Button1);
		assertNotNull(view.Button2);
		assertEquals("방향키", view.Button1.getText());
		assertEquals("WASD", view.Button2.getText());

		// 파일에서 controlType=wasd이므로 WASD 버튼이 선택되어야 함
		assertTrue(view.Button2.isSelected());
		assertFalse(view.Button1.isSelected());

		assertNotNull(view.checkButton);
		assertNotNull(view.cancelButton);
	}

	// 난이도 뷰가 3개의 옵션(쉬움/보통/어려움)을 표시하고, 파일 설정값이 초기 선택에 반영되는지 확인
	@Test
	void view_difficulty_initialSelection_fromFile() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"hard\"}");

		SettingModel model = new SettingModel();
		SettingView view = new SettingView("난이도 설정", model);

		assertNotNull(view.Button1);
		assertNotNull(view.Button2);
		assertNotNull(view.Button3);
		assertEquals("쉬움", view.Button1.getText());
		assertEquals("보통", view.Button2.getText());
		assertEquals("어려움", view.Button3.getText());

		// 파일에서 difficulty=hard이므로 세 번째 버튼이 선택되어야 함
		assertTrue(view.Button3.isSelected());
		assertFalse(view.Button1.isSelected());
		assertFalse(view.Button2.isSelected());

		assertNotNull(view.checkButton);
		assertNotNull(view.cancelButton);
	}

	// 스코어보드 초기화 뷰는 라디오 버튼 없이 확인/취소 버튼만 표시되는지 확인
	@Test
	void view_scoreboardReset_hasOnlyButtons() {
		SettingModel model = new SettingModel();
		SettingView view = new SettingView("스코어보드 초기화", model);

		assertNotNull(view.checkButton);
		assertNotNull(view.cancelButton);
		assertEquals("예", view.checkButton.getText());
		assertEquals("취소", view.cancelButton.getText());

		// 패널 내부에 라디오 버튼이 없는지 확인
		long radioCount = Arrays.stream(view.panel.getComponents())
				.filter(c -> c instanceof JRadioButton)
				.count();
		assertEquals(0, radioCount);
	}

	// 설정값 초기화 뷰는 라디오 버튼 없이 확인/취소 버튼만 표시되는지 확인
	@Test
	void view_settingsReset_hasOnlyButtons() {
		SettingModel model = new SettingModel();
		SettingView view = new SettingView("설정값 초기화", model);

		assertNotNull(view.checkButton);
		assertNotNull(view.cancelButton);
		assertEquals("예", view.checkButton.getText());
		assertEquals("취소", view.cancelButton.getText());

		long radioCount = Arrays.stream(view.panel.getComponents())
				.filter(c -> c instanceof JRadioButton)
				.count();
		assertEquals(0, radioCount);
	}

	// SettingMain.launchSetting 이 모든 세부 메뉴에 대해 view/controller를 정상적으로 생성하는지 확인
	@Test
	void launchSetting_returnsPair_forAllMenus() {
		String[] menus = {
				"색맹 모드", "화면 크기 설정", "조작키 설정", "난이도 설정",
				"스코어보드 초기화", "설정값 초기화"
		};

		for (String menu : menus) {
			SettingMain.SettingViewControllerPair pair = SettingMain.launchSetting(menu);
			assertNotNull(pair);
			assertNotNull(pair.view);
			assertNotNull(pair.controller);
			assertEquals(menu, pair.view.getSettingName());
		}
	}
}
