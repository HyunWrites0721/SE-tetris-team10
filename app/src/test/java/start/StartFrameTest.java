package start;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import javax.swing.*;
import java.awt.Component;
import java.awt.Container;

public class StartFrameTest {

	@Test
	void testStartFrameConstructor() throws Exception {
		final StartFrame[] frameHolder = new StartFrame[1];
		SwingUtilities.invokeAndWait(() -> {
			frameHolder[0] = new StartFrame();
			frameHolder[0].setVisible(true);
		});
		StartFrame frame = frameHolder[0];
		assertEquals(StartFrame.titleName, frame.getTitle(), "타이틀이 올바르게 설정되어야 함");
		frame.dispose();
	}

	// 재귀적으로 컴포넌트에서 JButton 찾기
	private boolean containsButton(Component comp) {
		if (comp instanceof JButton) return true;
		if (comp instanceof Container) {
			for (Component child : ((Container)comp).getComponents()) {
				if (containsButton(child)) return true;
			}
		}
		return false;
	}

	@Test
	void testMenuButtonsCreated() throws Exception {
		final StartFrame[] frameHolder = new StartFrame[1];
		SwingUtilities.invokeAndWait(() -> {
			frameHolder[0] = new StartFrame();
			frameHolder[0].setVisible(true);
		});
		StartFrame frame = frameHolder[0];
		assertTrue(containsButton(frame.getContentPane()), "메뉴 버튼이 생성되어야 함");
		frame.dispose();
	}

	// 재귀적으로 JLabel 찾기
	private JLabel findLabel(Component comp, String text) {
		if (comp instanceof JLabel && ((JLabel)comp).getText().equals(text)) return (JLabel)comp;
		if (comp instanceof Container) {
			for (Component child : ((Container)comp).getComponents()) {
				JLabel found = findLabel(child, text);
				if (found != null) return found;
			}
		}
		return null;
	}

	@Test
	void testTitleLabelText() throws Exception {
		final StartFrame[] frameHolder = new StartFrame[1];
		SwingUtilities.invokeAndWait(() -> {
			frameHolder[0] = new StartFrame();
			frameHolder[0].setVisible(true);
		});
		StartFrame frame = frameHolder[0];
		JLabel titleLabel = findLabel(frame.getContentPane(), "테트리스");
		assertNotNull(titleLabel, "타이틀 라벨 텍스트가 올바르게 설정되어야 함");
		frame.dispose();
	}

	// 재귀적으로 JButton 텍스트 찾기
	private boolean hasMenuButtonText(Component comp) {
		if (comp instanceof JButton) {
			String text = ((JButton)comp).getText();
			return text.equals("게임 시작") || text.equals("설정") || text.equals("스코어보드") || text.equals("게임 종료");
		}
		if (comp instanceof Container) {
			for (Component child : ((Container)comp).getComponents()) {
				if (hasMenuButtonText(child)) return true;
			}
		}
		return false;
	}

	@Test
	void testMenuButtonText() throws Exception {
		final StartFrame[] frameHolder = new StartFrame[1];
		SwingUtilities.invokeAndWait(() -> {
			frameHolder[0] = new StartFrame();
			frameHolder[0].setVisible(true);
		});
		StartFrame frame = frameHolder[0];
		assertTrue(hasMenuButtonText(frame.getContentPane()), "메뉴 버튼 텍스트가 올바르게 설정되어야 함");
		frame.dispose();
	}
}
