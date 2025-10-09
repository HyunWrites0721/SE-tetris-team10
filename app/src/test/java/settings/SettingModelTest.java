package settings;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SettingModelTest {

	@Test
	void testColorBlindModeLoadFromJson() {
		// SettingSave.json의 colorBlindMode 값이 false일 때
		SettingModel model = new SettingModel();
		assertFalse(model.isColorBlindMode(), "colorBlindMode 값이 false로 로드되어야 합니다.");
	}

	@Test
	void testSetColorBlindMode() {
		SettingModel model = new SettingModel();
		model.setColorBlindMode(true);
		assertTrue(model.isColorBlindMode(), "setColorBlindMode(true) 이후 true가 되어야 합니다.");
		model.setColorBlindMode(false);
		assertFalse(model.isColorBlindMode(), "setColorBlindMode(false) 이후 false가 되어야 합니다.");
	}
}
