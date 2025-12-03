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
		// ConfigManager가 사용하는 실제 경로 사용
		settingsDir = Paths.get(ConfigManager.getConfigDir());
		settingSavePath = Paths.get(ConfigManager.getSettingsPath());
		defaultSettingPath = Paths.get(ConfigManager.getDefaultSettingsPath());

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

	// JSON의 특정 필드가 null일 때 기본값 사용 확인
	@Test
	void constructor_nullFields_usesDefaults() throws Exception {
		String json = "{\"colorBlindMode\":null,\"controlType\":null,\"screenSize\":null,\"difficulty\":null}";
		Files.writeString(settingSavePath, json);

		SettingModel model = new SettingModel();
		assertFalse(model.isColorBlindMode(), "null colorBlindMode는 false로 초기화");
		assertEquals("arrow", model.getControlType(), "null controlType은 arrow로 초기화");
		assertEquals("medium", model.getScreenSize(), "null screenSize는 medium으로 초기화");
		assertEquals("normal", model.getDifficulty(), "null difficulty는 normal로 초기화");
	}

	// JSON에 일부 필드가 누락된 경우
	@Test
	void constructor_missingFields_usesDefaults() throws Exception {
		String json = "{\"colorBlindMode\":true}";
		Files.writeString(settingSavePath, json);

		SettingModel model = new SettingModel();
		assertTrue(model.isColorBlindMode(), "colorBlindMode는 로드됨");
		assertEquals("arrow", model.getControlType(), "누락된 controlType은 기본값");
		assertEquals("medium", model.getScreenSize(), "누락된 screenSize는 기본값");
		assertEquals("normal", model.getDifficulty(), "누락된 difficulty는 기본값");
	}

	// 빈 JSON 파일
	@Test
	void constructor_emptyJson_usesDefaults() throws Exception {
		Files.writeString(settingSavePath, "{}");

		SettingModel model = new SettingModel();
		assertFalse(model.isColorBlindMode());
		assertEquals("arrow", model.getControlType());
		assertEquals("medium", model.getScreenSize());
		assertEquals("normal", model.getDifficulty());
	}

	// screenSize 모든 유효값 테스트
	@Test
	void saveScreenSize_allValidValues() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}");

		SettingModel model = new SettingModel();
		
		// small
		model.setScreenSize("small");
		model.SaveScreenSize();
		JsonObject j1 = readJson(settingSavePath);
		assertEquals("small", j1.get("screenSize").getAsString());
		
		// medium
		model.setScreenSize("medium");
		model.SaveScreenSize();
		JsonObject j2 = readJson(settingSavePath);
		assertEquals("medium", j2.get("screenSize").getAsString());
		
		// large
		model.setScreenSize("large");
		model.SaveScreenSize();
		JsonObject j3 = readJson(settingSavePath);
		assertEquals("large", j3.get("screenSize").getAsString());
	}

	// controlType 모든 유효값 테스트
	@Test
	void saveControlType_allValidValues() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}");

		SettingModel model = new SettingModel();
		
		// arrow
		model.setControlType("arrow");
		model.SaveControlType();
		JsonObject j1 = readJson(settingSavePath);
		assertEquals("arrow", j1.get("controlType").getAsString());
		
		// wasd
		model.setControlType("wasd");
		model.SaveControlType();
		JsonObject j2 = readJson(settingSavePath);
		assertEquals("wasd", j2.get("controlType").getAsString());
	}

	// difficulty 모든 유효값 테스트
	@Test
	void saveDifficulty_allValidValues() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}");

		SettingModel model = new SettingModel();
		
		// easy
		model.setDifficulty("easy");
		model.SaveDifficulty();
		JsonObject j1 = readJson(settingSavePath);
		assertEquals("easy", j1.get("difficulty").getAsString());
		
		// normal
		model.setDifficulty("normal");
		model.SaveDifficulty();
		JsonObject j2 = readJson(settingSavePath);
		assertEquals("normal", j2.get("difficulty").getAsString());
		
		// hard
		model.setDifficulty("hard");
		model.SaveDifficulty();
		JsonObject j3 = readJson(settingSavePath);
		assertEquals("hard", j3.get("difficulty").getAsString());
	}

	// setter/getter 일관성 테스트
	@Test
	void setterGetter_consistency() {
		SettingModel model = new SettingModel();
		
		// colorBlindMode
		model.setColorBlindMode(true);
		assertTrue(model.isColorBlindMode());
		model.setColorBlindMode(false);
		assertFalse(model.isColorBlindMode());
		
		// screenSize
		model.setScreenSize("small");
		assertEquals("small", model.getScreenSize());
		model.setScreenSize("large");
		assertEquals("large", model.getScreenSize());
		
		// controlType
		model.setControlType("wasd");
		assertEquals("wasd", model.getControlType());
		model.setControlType("arrow");
		assertEquals("arrow", model.getControlType());
		
		// difficulty
		model.setDifficulty("easy");
		assertEquals("easy", model.getDifficulty());
		model.setDifficulty("hard");
		assertEquals("hard", model.getDifficulty());
	}

	// Save 후 다시 로드했을 때 값 유지 확인
	@Test
	void save_thenLoad_valuesPersist() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}");

		SettingModel model1 = new SettingModel();
		model1.setColorBlindMode(true);
		model1.setScreenSize("large");
		model1.setControlType("wasd");
		model1.setDifficulty("hard");
		
		model1.SaveColorBlindMode();
		model1.SaveScreenSize();
		model1.SaveControlType();
		model1.SaveDifficulty();
		
		// 새로운 모델 생성하여 로드
		SettingModel model2 = new SettingModel();
		assertTrue(model2.isColorBlindMode(), "저장된 colorBlindMode 로드");
		assertEquals("large", model2.getScreenSize(), "저장된 screenSize 로드");
		assertEquals("wasd", model2.getControlType(), "저장된 controlType 로드");
		assertEquals("hard", model2.getDifficulty(), "저장된 difficulty 로드");
	}

	// resetSettings가 Default 파일과 동일한지 확인
	@Test
	void resetSettings_matchesDefault() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":true,\"controlType\":\"wasd\",\"screenSize\":\"large\",\"difficulty\":\"hard\"}");

		SettingModel model = new SettingModel();
		model.resetSettings();
		
		String settingsContent = Files.readString(settingSavePath);
		String defaultContent = Files.readString(defaultSettingPath);
		
		assertEquals(defaultContent, settingsContent, "resetSettings 후 파일 내용이 기본값과 동일해야 함");
	}

	// 유효하지 않은 값들에 대한 추가 테스트
	@Test
	void saveScreenSize_variousInvalidValues() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}");

		SettingModel model = new SettingModel();
		
		String[] invalidValues = {"", "tiny", "huge", "MEDIUM", "Small", "null"};
		for (String invalid : invalidValues) {
			model.setScreenSize(invalid);
			model.SaveScreenSize();
			JsonObject j = readJson(settingSavePath);
			assertEquals("medium", j.get("screenSize").getAsString(), 
				"유효하지 않은 값(" + invalid + ")은 무시되고 이전 값 유지");
		}
	}

	@Test
	void saveControlType_variousInvalidValues() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}");

		SettingModel model = new SettingModel();
		
		String[] invalidValues = {"", "mouse", "gamepad", "WASD", "Arrow", "null"};
		for (String invalid : invalidValues) {
			model.setControlType(invalid);
			model.SaveControlType();
			JsonObject j = readJson(settingSavePath);
			assertEquals("arrow", j.get("controlType").getAsString(), 
				"유효하지 않은 값(" + invalid + ")은 무시되고 이전 값 유지");
		}
	}

	@Test
	void saveDifficulty_variousInvalidValues() throws Exception {
		Files.writeString(settingSavePath, "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}");

		SettingModel model = new SettingModel();
		
		String[] invalidValues = {"", "medium", "insane", "EASY", "Normal", "null"};
		for (String invalid : invalidValues) {
			model.setDifficulty(invalid);
			model.SaveDifficulty();
			JsonObject j = readJson(settingSavePath);
			assertEquals("normal", j.get("difficulty").getAsString(), 
				"유효하지 않은 값(" + invalid + ")은 무시되고 이전 값 유지");
		}
	}
}
