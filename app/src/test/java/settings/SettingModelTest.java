package settings;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class SettingModelTest {

	private Path settingsDir;
	private Path settingSavePath;
	private Path defaultSettingPath;
	private String originalSettingSave;

	@BeforeEach
	public void setUp() throws Exception {
		// Gradle 테스트 실행 시 working dir이 app 모듈이므로, 모듈 상대 경로 사용
		settingsDir = Paths.get("src/main/java/settings/data");
		settingSavePath = settingsDir.resolve("SettingSave.json");
		defaultSettingPath = settingsDir.resolve("DefaultSetting.json");

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

	private static JsonObject readJson(Path p) throws Exception {
		return new Gson().fromJson(Files.readString(p), JsonObject.class);
	}

	// 생성자가 SettingSave.json의 모든 필드를 올바르게 로드하는지 확인
	@Test
	void constructor_loadsAllFields_fromJson() throws Exception {
		String json = "{\"colorBlindMode\":true,\"controlType\":\"wasd\",\"screenSize\":\"large\",\"difficulty\":\"hard\"}";
		Files.writeString(settingSavePath, json);

		SettingModel model = new SettingModel();
		assertTrue(model.isColorBlindMode(), "colorBlindMode=true 로드");
		assertEquals("wasd", model.getControlType(), "controlType=wasd 로드");
		assertEquals("large", model.getScreenSize(), "screenSize=large 로드");
		assertEquals("hard", model.getDifficulty(), "difficulty=hard 로드");
	}

	// colorBlindMode 저장이 파일에 반영되는지 확인 (true/false 왕복)
	@Test
	void saveColorBlindMode_persistsToFile() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}");

		SettingModel model = new SettingModel();
		model.setColorBlindMode(true);
		model.SaveColorBlindMode();

		JsonObject j1 = readJson(settingSavePath);
		assertTrue(j1.get("colorBlindMode").getAsBoolean(), "파일에 colorBlindMode=true 반영");

		model.setColorBlindMode(false);
		model.SaveColorBlindMode();
		JsonObject j2 = readJson(settingSavePath);
		assertFalse(j2.get("colorBlindMode").getAsBoolean(), "파일에 colorBlindMode=false 반영");
	}

	// screenSize 저장: 유효값(small/medium/large)은 저장되고, 잘못된 값은 무시되는지 확인
	@Test
	void saveScreenSize_validAccepted_invalidIgnored() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}");

		SettingModel model = new SettingModel();
		model.setScreenSize("large");
		model.SaveScreenSize();
		JsonObject j1 = readJson(settingSavePath);
		assertEquals("large", j1.get("screenSize").getAsString(), "유효값 large 저장");

		model.setScreenSize("invalid-size");
		model.SaveScreenSize();
		JsonObject j2 = readJson(settingSavePath);
		assertEquals("large", j2.get("screenSize").getAsString(), "잘못된 값은 무시되어 이전값 유지");
	}

	// controlType 저장: 유효값(arrow/wasd)은 저장, 잘못된 값은 무시
	@Test
	void saveControlType_validAccepted_invalidIgnored() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}");

		SettingModel model = new SettingModel();
		model.setControlType("wasd");
		model.SaveControlType();
		JsonObject j1 = readJson(settingSavePath);
		assertEquals("wasd", j1.get("controlType").getAsString(), "유효값 wasd 저장");

		model.setControlType("gamepad");
		model.SaveControlType();
		JsonObject j2 = readJson(settingSavePath);
		assertEquals("wasd", j2.get("controlType").getAsString(), "잘못된 값은 무시되어 이전값 유지");
	}

	// difficulty 저장: 유효값(easy/normal/hard)은 저장, 잘못된 값은 무시
	@Test
	void saveDifficulty_validAccepted_invalidIgnored() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}");

		SettingModel model = new SettingModel();
		model.setDifficulty("hard");
		model.SaveDifficulty();
		JsonObject j1 = readJson(settingSavePath);
		assertEquals("hard", j1.get("difficulty").getAsString(), "유효값 hard 저장");

		model.setDifficulty("insane");
		model.SaveDifficulty();
		JsonObject j2 = readJson(settingSavePath);
		assertEquals("hard", j2.get("difficulty").getAsString(), "잘못된 값은 무시되어 이전값 유지");
	}

	// resetSettings가 DefaultSetting.json 내용으로 SettingSave.json을 덮어쓰는지 확인
	@Test
	void resetSettings_overwritesWithDefault() throws Exception {
		// SettingSave.json을 임의 값으로 변경
		Files.writeString(settingSavePath, "{\"colorBlindMode\":true,\"controlType\":\"wasd\",\"screenSize\":\"large\",\"difficulty\":\"hard\"}");

		SettingModel model = new SettingModel();
		model.resetSettings();

		JsonObject after = readJson(settingSavePath);
		JsonObject expected = readJson(defaultSettingPath);
		assertEquals(expected, after, "resetSettings 이후 파일 내용은 DefaultSetting.json과 동일해야 함");
	}

	// 손상된 JSON일 때 생성자가 예외 없이 기본값을 사용하여 초기화하는지 확인
	@Test
	void constructor_onCorruptJson_usesDefaults() throws Exception {
		Files.writeString(settingSavePath, "{not-a-valid-json}");

		SettingModel model = new SettingModel();
		assertFalse(model.isColorBlindMode(), "기본값 colorBlindMode=false");
		assertEquals("arrow", model.getControlType(), "기본값 controlType=arrow");
		assertEquals("medium", model.getScreenSize(), "기본값 screenSize=medium");
		assertEquals("normal", model.getDifficulty(), "기본값 difficulty=normal");
	}
}
