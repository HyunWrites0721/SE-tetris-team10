package settings;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class SettingControllerTest {

	private Path settingsDir;
	private Path settingSavePath;
	private Path defaultSettingPath;
	private Path highScorePath;
	private Path highScoreDefaultPath;
	private String originalSettingSave;
	private String originalHighScore;

	@BeforeEach
	public void setUp() throws Exception {
		// ConfigManager가 사용하는 실제 경로 사용
		settingsDir = Paths.get(ConfigManager.getConfigDir());
		settingSavePath = Paths.get(ConfigManager.getSettingsPath());
		defaultSettingPath = Paths.get(ConfigManager.getDefaultSettingsPath());
		highScorePath = Paths.get(ConfigManager.getHighScorePath());
		highScoreDefaultPath = Paths.get(ConfigManager.getDefaultHighScorePath());

		if (!Files.exists(settingsDir)) {
			Files.createDirectories(settingsDir);
		}

		if (Files.exists(settingSavePath)) {
			originalSettingSave = Files.readString(settingSavePath);
		}
		if (Files.exists(highScorePath)) {
			originalHighScore = Files.readString(highScorePath);
		}
	}

	@AfterEach
	public void tearDown() throws Exception {
		if (originalSettingSave != null && Files.exists(settingSavePath)) {
			Files.writeString(settingSavePath, originalSettingSave);
		}
		if (originalHighScore != null && Files.exists(highScorePath)) {
			Files.writeString(highScorePath, originalHighScore);
		}
	}

	private static JsonObject readJson(Path p) throws Exception {
		String s = Files.readString(p);
		return new Gson().fromJson(s, JsonObject.class);
	}

	@Test
	void cancelCallback_isInvoked() {
		SettingModel model = new SettingModel();
		SettingView view = new SettingView("색맹 모드", model);
	SettingController controller = new SettingController(model, view);
	assertNotNull(controller);
	assertNotNull(controller);
		AtomicBoolean cancelled = new AtomicBoolean(false);
		controller.setOnCancelCallback(() -> cancelled.set(true));

		assertNotNull(view.cancelButton, "취소 버튼이 초기화되어야 합니다");
		view.cancelButton.doClick();

		assertTrue(cancelled.get(), "취소 콜백이 호출되어야 합니다");
	}

	@Test
	void resetSettings_writesDefaultJson() throws Exception {
		// 현재 SettingSave.json을 의도적으로 변경
		String altered = "{\"colorBlindMode\":true,\"controlType\":\"wasd\",\"screenSize\":\"large\",\"difficulty\":\"hard\"}";
		Files.writeString(settingSavePath, altered);

		SettingModel model = new SettingModel();
		SettingView view = new SettingView("설정값 초기화", model);
	SettingController controller = new SettingController(model, view);
	assertNotNull(controller);

		view.checkButton.doClick();

		JsonObject after = readJson(settingSavePath);
		JsonObject expected = readJson(defaultSettingPath);
		assertEquals(expected, after, "설정 초기화 후 SettingSave.json은 DefaultSetting.json과 동일해야 합니다");
	}

	@Test
	void colorBlindMode_saveOnOff() throws Exception {
		// 기본값으로 초기화하고 시작 (꺼짐)
		Files.writeString(settingSavePath, Files.readString(defaultSettingPath));

		SettingModel model = new SettingModel();
		SettingView view = new SettingView("색맹 모드", model);
	SettingController controller = new SettingController(model, view);
	assertNotNull(controller);

		// 켬 선택 후 확인
		view.Button2.setSelected(true);
		view.checkButton.doClick();
		assertTrue(model.isColorBlindMode(), "색맹 모드가 켜져야 합니다");

		JsonObject j = readJson(settingSavePath);
		assertTrue(j.get("colorBlindMode").getAsBoolean(), "파일에도 colorBlindMode=true가 저장되어야 합니다");

		// 끔 선택 후 확인
		view.Button1.setSelected(true);
		view.checkButton.doClick();
		assertFalse(model.isColorBlindMode(), "색맹 모드가 꺼져야 합니다");

		j = readJson(settingSavePath);
		assertFalse(j.get("colorBlindMode").getAsBoolean(), "파일에도 colorBlindMode=false가 저장되어야 합니다");
	}

	@Test
	void screenSize_saveLarge() throws Exception {
		Files.writeString(settingSavePath, Files.readString(defaultSettingPath));

		SettingModel model = new SettingModel();
		SettingView view = new SettingView("화면 크기 설정", model);
	SettingController controller = new SettingController(model, view);
	assertNotNull(controller);

		// large 선택 후 확인
		view.Button3.setSelected(true);
		view.checkButton.doClick();
		assertEquals("large", model.getScreenSize(), "화면 크기는 large로 설정되어야 합니다");

		JsonObject j = readJson(settingSavePath);
		assertEquals("large", j.get("screenSize").getAsString(), "파일에도 screenSize=large가 저장되어야 합니다");
	}

	@Test
	void controlType_saveWasd() throws Exception {
		Files.writeString(settingSavePath, Files.readString(defaultSettingPath));

		SettingModel model = new SettingModel();
		SettingView view = new SettingView("조작키 설정", model);
	SettingController controller = new SettingController(model, view);
	assertNotNull(controller);

		// WASD 선택 후 확인
		view.Button2.setSelected(true);
		view.checkButton.doClick();
		assertEquals("wasd", model.getControlType(), "조작키는 wasd로 설정되어야 합니다");

		JsonObject j = readJson(settingSavePath);
		assertEquals("wasd", j.get("controlType").getAsString(), "파일에도 controlType=wasd가 저장되어야 합니다");
	}

	@Test
	void difficulty_saveHard() throws Exception {
		Files.writeString(settingSavePath, Files.readString(defaultSettingPath));

		SettingModel model = new SettingModel();
		SettingView view = new SettingView("난이도 설정", model);
	SettingController controller = new SettingController(model, view);
	assertNotNull(controller);

		// hard 선택 후 확인
		view.Button3.setSelected(true);
		view.checkButton.doClick();
		assertEquals("hard", model.getDifficulty(), "난이도는 hard로 설정되어야 합니다");

		JsonObject j = readJson(settingSavePath);
		assertEquals("hard", j.get("difficulty").getAsString(), "파일에도 difficulty=hard가 저장되어야 합니다");
	}

	@Test
	void scoreboardReset_clearsScoresAndFile() throws Exception {
		// 현재 HighScore.json이 비어있지 않음을 보장 (점수 추가)
		HighScoreModel.getInstance().addScore("AAA", 999, "Medium", false);
		String before = Files.readString(highScorePath);
		assertTrue(before.contains("normalScores") || before.contains("itemScores"), "HighScore.json이 존재해야 합니다");

	SettingModel model = new SettingModel();
	SettingView view = new SettingView("스코어보드 초기화", model);
	SettingController controller = new SettingController(model, view);
	assertNotNull(controller);

		view.checkButton.doClick();

		// 파일은 기본값과 동일해야 함
		JsonObject after = readJson(highScorePath);
		JsonObject expected = readJson(highScoreDefaultPath);
		assertEquals(expected, after, "스코어보드 초기화 후 HighScore.json은 기본값과 같아야 합니다");

		// 메모리 상 리스트도 비어 있어야 함
		assertTrue(HighScoreModel.getInstance().getTopScores(false).isEmpty(), "초기화 후 Normal Mode TopScores는 비어 있어야 합니다");
		assertTrue(HighScoreModel.getInstance().getTopScores(true).isEmpty(), "초기화 후 Item Mode TopScores는 비어 있어야 합니다");
	}
}
