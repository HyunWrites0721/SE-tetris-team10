package blocks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import blocks.item.AllClearBlock;
import blocks.item.BoxClearBlock;
import blocks.item.OneLineClearBlock;
import blocks.item.ScoreDoubleBlock;
import blocks.item.WeightBlock;

/**
 * 아이템 블록 관련 테스트 전용 클래스
 */
public class ItemBlockTest {

	private Path settingsDir;
	private Path settingsPath;
	private String originalContent;

	@BeforeEach
	public void setUp() throws Exception {
		// 설정 파일 경로 준비 (BlockTest와 동일 경로 사용)
		settingsDir = Paths.get("app/src/main/java/settings/data");
		settingsPath = settingsDir.resolve("SettingSave.json");

		if (!Files.exists(settingsDir)) {
			Files.createDirectories(settingsDir);
		}

		if (Files.exists(settingsPath)) {
			originalContent = Files.readString(settingsPath);
		}
	}

	@AfterEach
	public void tearDown() throws Exception {
		// 원본 설정 복원
		if (originalContent != null && Files.exists(settingsPath)) {
			Files.writeString(settingsPath, originalContent);
		}
	}

	// 공통 유틸: 2차원 배열에서 0이 아닌 셀 개수 세기
	private int countNonZero(int[][] a) {
		int cnt = 0;
		for (int[] row : a) {
			for (int v : row) {
				if (v != 0) cnt++;
			}
		}
		return cnt;
	}

	// 공통 유틸: 2차원 배열의 깊은 복사
	private int[][] deepCopy(int[][] src) {
		int[][] dst = new int[src.length][];
		for (int i = 0; i < src.length; i++) {
			dst[i] = java.util.Arrays.copyOf(src[i], src[i].length);
		}
		return dst;
	}

	// 공통 유틸: 2차원 배열의 0이 아닌 좌표를 행우선(row-major) 순서로 수집
	private java.util.List<int[]> nonZeroCoordsRowMajor(int[][] a) {
		java.util.List<int[]> coords = new java.util.ArrayList<>();
		for (int r = 0; r < a.length; r++) {
			for (int c = 0; c < a[r].length; c++) {
				if (a[r][c] != 0) coords.add(new int[]{r, c});
			}
		}
		return coords;
	}

	@Test
	public void testItemBlocksShapes() {
		// 아이템 블록들의 기본 모양 및 값 검증
		// 1) AllClear: 2x2, 값은 2
		AllClearBlock ac = new AllClearBlock();
		ac.setShape();
		int[][] acs = ac.getShape();
		assertEquals(2, acs.length, "AllClear는 2행이어야 합니다");
		assertEquals(2, acs[0].length, "AllClear는 2열이어야 합니다");
		for (int[] row : acs) {
			for (int v : row) {
				assertEquals(2, v, "AllClear 셀 값은 2여야 합니다");
			}
		}

		// 2) BoxClear: 중앙만 3, 나머지 0
		BoxClearBlock bc = new BoxClearBlock();
		bc.setShape();
		int[][] bcs = bc.getShape();
		int threeCount = 0;
		for (int[] row : bcs) {
			for (int v : row) {
				if (v == 3) threeCount++;
				else assertEquals(0, v, "BoxClear는 중앙 외에는 0이어야 합니다");
			}
		}
		assertEquals(1, threeCount, "BoxClear는 정확히 1개의 3 값을 가져야 합니다");

		// 3) WeightBlock: 0이 아닌 셀이 존재해야 함
		WeightBlock wb = new WeightBlock();
		wb.setShape();
		assertTrue(countNonZero(wb.getShape()) > 0, "WeightBlock은 0이 아닌 셀이 있어야 합니다");
	}

	@Test
	public void testOneLineAndScoreDoubleMarkExactlyOneCell() {
		// 기본 블록의 shape를 아이템으로 감쌀 때, 정확히 1개 셀이 4 또는 5로 표시되어야 함
		Block base = new LBlock();
		base.setShape();
		int[][] baseShape = base.getShape();
		int baseNonZero = countNonZero(baseShape);

		OneLineClearBlock ol = new OneLineClearBlock(deepCopy(baseShape));
		ol.setShape();
		int[][] ols = ol.getShape();
		int nonZeroOl = countNonZero(ols);
		int count4 = 0;
		for (int[] row : ols) {
			for (int v : row) {
				if (v == 4) count4++;
			}
		}
		assertEquals(1, count4, "OneLineClear는 정확히 1개의 4 표식을 가져야 합니다");
		assertEquals(baseNonZero, nonZeroOl, "아이템 래핑 후에도 전체 점유 칸 수는 동일해야 합니다");

		ScoreDoubleBlock sd = new ScoreDoubleBlock(deepCopy(baseShape));
		sd.setShape();
		int[][] sds = sd.getShape();
		int nonZeroSd = countNonZero(sds);
		int count5 = 0;
		for (int[] row : sds) {
			for (int v : row) {
				if (v == 5) count5++;
			}
		}
		assertEquals(1, count5, "ScoreDouble는 정확히 1개의 5 표식을 가져야 합니다");
		assertEquals(baseNonZero, nonZeroSd, "아이템 래핑 후에도 전체 점유 칸 수는 동일해야 합니다");
	}

	@Test
	public void testItemColorInheritanceForWrappedBlocks() throws Exception {
		// 아이템(OneLine/ScoreDouble)은 기본 블록의 색상을 그대로 상속하도록 setExactColor를 사용함
		String jsonNormal = "{\"colorBlindMode\":false,\"controlType\":\"arrow\",\"screenSize\":\"medium\",\"difficulty\":\"normal\"}";
		Files.writeString(settingsPath, jsonNormal);
		Block.reloadSettings();

		Block base = new TBlock();
		base.setShape();
		java.awt.Color baseColor = base.getColor();
		assertNotNull(baseColor, "기본 블록의 색상은 null이면 안됩니다");

		OneLineClearBlock ol = new OneLineClearBlock(deepCopy(base.getShape()));
		ol.setExactColor(baseColor);
		assertEquals(baseColor, ol.getColor(), "OneLine 아이템은 기본 블록 색상을 상속해야 합니다");

		ScoreDoubleBlock sd = new ScoreDoubleBlock(deepCopy(base.getShape()));
		sd.setExactColor(baseColor);
		assertEquals(baseColor, sd.getColor(), "ScoreDouble 아이템은 기본 블록 색상을 상속해야 합니다");
	}

	@Test
	public void testOneLineRandomCellUniformOverFour_AllBlocks() {
		// 7개의 모든 기본 블록(I,J,L,O,S,T,Z)에 대해 동일하게 검증
		Block[] bases = new Block[] { new IBlock(), new JBlock(), new LBlock(), new OBlock(), new SBlock(), new TBlock(), new ZBlock() };
		int trials = 4000; // 충분한 표본
		double lower = 0.20, upper = 0.30; // 25% ± 5%

		for (Block base : bases) {
			base.setShape();
			int[][] baseShape = base.getShape();
			java.util.List<int[]> cells = nonZeroCoordsRowMajor(baseShape);
			assertEquals(4, cells.size(), base.getClass().getSimpleName() + ": 기본 블록은 정확히 4칸을 점유해야 합니다");

			int[] counts = new int[cells.size()];
			for (int t = 0; t < trials; t++) {
				OneLineClearBlock item = new OneLineClearBlock(deepCopy(baseShape));
				item.setShape();
				int[][] shaped = item.getShape();
				boolean found = false;
				for (int i = 0; i < cells.size(); i++) {
					int r = cells.get(i)[0];
					int c = cells.get(i)[1];
					if (shaped[r][c] == 4) { counts[i]++; found = true; break; }
				}
				assertTrue(found, base.getClass().getSimpleName() + ": 점유 셀 중 정확히 하나가 4로 표시되어야 합니다");
			}

			for (int i = 0; i < cells.size(); i++) {
				double ratio = (double) counts[i] / trials;
				System.out.printf("OneLine %s idx %d ratio = %.4f (%d/%d)\n",
					base.getClass().getSimpleName(), i, ratio, counts[i], trials);
				assertTrue(ratio >= lower && ratio <= upper,
					String.format("OneLine(%s) 분포가 균일 범위를 벗어났습니다: idx %d, %.3f (허용 %.2f~%.2f)",
						base.getClass().getSimpleName(), i, ratio, lower, upper));
			}
		}
	}

	@Test
	public void testScoreDoubleRandomCellUniformOverFour_AllBlocks() {
		// 7개의 모든 기본 블록(I,J,L,O,S,T,Z)에 대해 동일하게 검증
		Block[] bases = new Block[] { new IBlock(), new JBlock(), new LBlock(), new OBlock(), new SBlock(), new TBlock(), new ZBlock() };
		int trials = 4000; // 충분한 표본
		double lower = 0.20, upper = 0.30; // 25% ± 5%

		for (Block base : bases) {
			base.setShape();
			int[][] baseShape = base.getShape();
			java.util.List<int[]> cells = nonZeroCoordsRowMajor(baseShape);
			assertEquals(4, cells.size(), base.getClass().getSimpleName() + ": 기본 블록은 정확히 4칸을 점유해야 합니다");

			int[] counts = new int[cells.size()];
			for (int t = 0; t < trials; t++) {
				ScoreDoubleBlock item = new ScoreDoubleBlock(deepCopy(baseShape));
				item.setShape();
				int[][] shaped = item.getShape();
				boolean found = false;
				for (int i = 0; i < cells.size(); i++) {
					int r = cells.get(i)[0];
					int c = cells.get(i)[1];
					if (shaped[r][c] == 5) { counts[i]++; found = true; break; }
				}
				assertTrue(found, base.getClass().getSimpleName() + ": 점유 셀 중 정확히 하나가 5로 표시되어야 합니다");
			}

			for (int i = 0; i < cells.size(); i++) {
				double ratio = (double) counts[i] / trials;
				System.out.printf("ScoreDouble %s idx %d ratio = %.4f (%d/%d)\n",
					base.getClass().getSimpleName(), i, ratio, counts[i], trials);
				assertTrue(ratio >= lower && ratio <= upper,
					String.format("ScoreDouble(%s) 분포가 균일 범위를 벗어났습니다: idx %d, %.3f (허용 %.2f~%.2f)",
						base.getClass().getSimpleName(), i, ratio, lower, upper));
			}
		}
	}

	@Test
	public void testItemSpawnDistributionUniform() {
		// 아이템 스폰 분포가 균일(각 20% 내외)하게 나오는지 검증
		// Math.random()을 사용하는 특성상 완전 고정은 불가하므로 충분한 표본과 느슨한 허용오차를 사용

		Block base = new OBlock();
		base.setShape();

		int trials = 5000; // 표본 수 (너무 작으면 분산이 커져 실패 가능)
		int[] counts = new int[5]; // 0:AllClear,1:BoxClear,2:OneLine,3:ScoreDouble,4:Weight

		for (int i = 0; i < trials; i++) {
			Block item = Block.spawnItem(base);
			assertNotNull(item.getShape(), "spawnItem은 shape을 설정해야 합니다");
			if (item instanceof AllClearBlock) counts[0]++;
			else if (item instanceof BoxClearBlock) counts[1]++;
			else if (item instanceof OneLineClearBlock) counts[2]++;
			else if (item instanceof ScoreDoubleBlock) counts[3]++;
			else if (item instanceof WeightBlock) counts[4]++;
			else {
				// 알 수 없는 타입이 나오면 실패
				assertTrue(false, "알 수 없는 아이템 타입이 스폰되었습니다: " + item.getClass().getName());
			}
		}

		// 기대 비율: 각 0.2 (20%). 허용오차 ±4% (0.16~0.24)
		double lower = 0.16;
		double upper = 0.24;
		String[] names = {"AllClear", "BoxClear", "OneLine", "ScoreDouble", "Weight"};
		for (int i = 0; i < 5; i++) {
			double ratio = (double) counts[i] / trials;
			System.out.printf("%s ratio = %.4f (%d/%d)\n", names[i], ratio, counts[i], trials);
			assertTrue(ratio >= lower && ratio <= upper,
				String.format("%s 분포가 균일 범위를 벗어났습니다: %.3f (허용 %.2f~%.2f)", names[i], ratio, lower, upper));
		}
	}
}
