package game.core;

import game.GameView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AnimationManager 테스트")
class AnimationManagerTest {
    
    private AnimationManager animationManager;
    private int[][] testBoard;
    private int[][] testColorBoard;
    private GameState testState;
    
    @BeforeEach
    void setUp() {
        animationManager = new AnimationManager(null);
        
        // 보드 초기화
        testBoard = new int[23][12];
        testColorBoard = new int[23][12];
        
        // 벽 설정
        for (int i = 0; i < 23; i++) {
            testBoard[i][0] = 1;
            testBoard[i][11] = 1;
        }
        for (int j = 0; j < 12; j++) {
            testBoard[0][j] = 1;
            testBoard[1][j] = 1;
            testBoard[22][j] = 1;
        }
        
        testState = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
    }
    
    @Test
    @DisplayName("AnimationManager 생성 테스트")
    void testAnimationManagerCreation() {
        AnimationManager manager = new AnimationManager(null);
        
        assertNotNull(manager);
        assertFalse(manager.isAnimating());
        assertFalse(manager.isLineClearAnimating());
        assertFalse(manager.isAllClearAnimating());
        assertFalse(manager.isBoxClearAnimating());
        assertFalse(manager.isWeightAnimating());
    }
    
    @Test
    @DisplayName("라인 클리어 애니메이션 시작 테스트")
    void testStartLineClearAnimation() {
        List<Integer> fullLines = Arrays.asList(10, 11, 12);
        boolean[] callbackInvoked = {false};
        
        animationManager.startLineClearAnimation(fullLines, () -> {
            callbackInvoked[0] = true;
        });
        
        assertTrue(animationManager.isAnimating());
        assertTrue(animationManager.isLineClearAnimating());
        assertTrue(animationManager.isFlashBlack());
    }
    
    @Test
    @DisplayName("라인 클리어 애니메이션 - 빈 라인 리스트")
    void testLineClearAnimation_EmptyList() {
        List<Integer> emptyLines = new ArrayList<>();
        boolean[] callbackInvoked = {false};
        
        animationManager.startLineClearAnimation(emptyLines, () -> {
            callbackInvoked[0] = true;
        });
        
        // 빈 리스트면 애니메이션이 시작되지 않고 즉시 콜백 호출
        assertTrue(callbackInvoked[0]);
        assertFalse(animationManager.isAnimating());
    }
    
    @Test
    @DisplayName("라인 클리어 애니메이션 - 행 깜빡임 확인")
    void testLineClearAnimation_RowFlashing() {
        List<Integer> fullLines = Arrays.asList(5, 10, 15);
        
        animationManager.startLineClearAnimation(fullLines, () -> {});
        
        // 지정된 행만 깜빡여야 함
        assertTrue(animationManager.isRowFlashing(5));
        assertTrue(animationManager.isRowFlashing(10));
        assertTrue(animationManager.isRowFlashing(15));
        assertFalse(animationManager.isRowFlashing(7));
        assertFalse(animationManager.isRowFlashing(20));
    }
    
    @Test
    @DisplayName("AllClear 애니메이션 시작 테스트")
    void testStartAllClearAnimation() {
        boolean[] callbackInvoked = {false};
        
        animationManager.startAllClearAnimation(() -> {
            callbackInvoked[0] = true;
        });
        
        assertTrue(animationManager.isAnimating());
        assertTrue(animationManager.isAllClearAnimating());
        assertTrue(animationManager.isAllClearFlashBlack());
    }
    
    @Test
    @DisplayName("BoxClear 애니메이션 시작 테스트")
    void testStartBoxClearAnimation() {
        List<int[]> centers = Arrays.asList(
            new int[]{10, 5},
            new int[]{15, 8}
        );
        boolean[] callbackInvoked = {false};
        
        animationManager.startBoxClearAnimation(centers, () -> {
            callbackInvoked[0] = true;
        });
        
        assertTrue(animationManager.isAnimating());
        assertTrue(animationManager.isBoxClearAnimating());
        assertTrue(animationManager.isBoxFlashBlack());
    }
    
    @Test
    @DisplayName("BoxClear 애니메이션 - 빈 중심점 리스트")
    void testBoxClearAnimation_EmptyList() {
        List<int[]> emptyCenters = new ArrayList<>();
        boolean[] callbackInvoked = {false};
        
        animationManager.startBoxClearAnimation(emptyCenters, () -> {
            callbackInvoked[0] = true;
        });
        
        // 빈 리스트면 애니메이션이 시작되지 않고 즉시 콜백 호출
        assertTrue(callbackInvoked[0]);
        assertFalse(animationManager.isAnimating());
    }
    
    @Test
    @DisplayName("BoxClear 애니메이션 - 셀 깜빡임 확인")
    void testBoxClearAnimation_CellFlashing() {
        List<int[]> centers = Arrays.asList(new int[]{10, 5});
        
        animationManager.startBoxClearAnimation(centers, () -> {});
        
        // 중심점 기준 5x5 영역 (±2)
        assertTrue(animationManager.isCellInBoxFlash(10, 5));  // 중심
        assertTrue(animationManager.isCellInBoxFlash(8, 3));   // 좌상단
        assertTrue(animationManager.isCellInBoxFlash(12, 7));  // 우하단
        assertFalse(animationManager.isCellInBoxFlash(5, 5));  // 범위 밖
        assertFalse(animationManager.isCellInBoxFlash(15, 5)); // 범위 밖
    }
    
    @Test
    @DisplayName("WeightBlock 애니메이션 시작 테스트")
    void testStartWeightAnimation() {
        boolean[] frameCallbackInvoked = {false};
        
        animationManager.startWeightAnimation(() -> {
            frameCallbackInvoked[0] = true;
        }, () -> {});
        
        assertTrue(animationManager.isAnimating());
        assertTrue(animationManager.isWeightAnimating());
    }
    
    @Test
    @DisplayName("WeightBlock 애니메이션 정지 테스트")
    void testStopWeightAnimation() {
        animationManager.startWeightAnimation(() -> {}, () -> {});
        
        assertTrue(animationManager.isWeightAnimating());
        
        animationManager.stopWeightAnimation();
        
        assertFalse(animationManager.isWeightAnimating());
    }
    
    @Test
    @DisplayName("모든 애니메이션 정지 테스트")
    void testStopAllAnimations() {
        // 여러 애니메이션 시작
        animationManager.startLineClearAnimation(Arrays.asList(10), () -> {});
        animationManager.startAllClearAnimation(() -> {});
        animationManager.startWeightAnimation(() -> {}, () -> {});
        
        assertTrue(animationManager.isAnimating());
        
        // 모든 애니메이션 정지
        animationManager.stopAllAnimations();
        
        assertFalse(animationManager.isAnimating());
        assertFalse(animationManager.isLineClearAnimating());
        assertFalse(animationManager.isAllClearAnimating());
        assertFalse(animationManager.isBoxClearAnimating());
        assertFalse(animationManager.isWeightAnimating());
    }
    
    @Test
    @DisplayName("애니메이션 상태 적용 테스트")
    void testApplyAnimationState() {
        // 라인 클리어 애니메이션 시작
        List<Integer> fullLines = Arrays.asList(5, 10);
        animationManager.startLineClearAnimation(fullLines, () -> {});
        
        // 애니메이션 상태를 GameState에 반영
        GameState stateWithAnimation = animationManager.applyAnimationState(testState);
        
        assertNotNull(stateWithAnimation);
        assertTrue(stateWithAnimation.isLineClearAnimating());
        assertTrue(stateWithAnimation.isFlashBlack());
        assertEquals(2, stateWithAnimation.getFlashingRows().size());
    }
    
    @Test
    @DisplayName("애니메이션 상태 적용 - AllClear")
    void testApplyAnimationState_AllClear() {
        animationManager.startAllClearAnimation(() -> {});
        
        GameState stateWithAnimation = animationManager.applyAnimationState(testState);
        
        assertTrue(stateWithAnimation.isAllClearAnimating());
        assertTrue(stateWithAnimation.isAllClearFlashBlack());
    }
    
    @Test
    @DisplayName("애니메이션 상태 적용 - BoxClear")
    void testApplyAnimationState_BoxClear() {
        List<int[]> centers = Arrays.asList(new int[]{10, 5});
        animationManager.startBoxClearAnimation(centers, () -> {});
        
        GameState stateWithAnimation = animationManager.applyAnimationState(testState);
        
        assertTrue(stateWithAnimation.isBoxClearAnimating());
        assertTrue(stateWithAnimation.isBoxFlashBlack());
        assertEquals(1, stateWithAnimation.getBoxFlashCenters().size());
    }
    
    @Test
    @DisplayName("애니메이션 상태 적용 - WeightBlock")
    void testApplyAnimationState_WeightBlock() {
        animationManager.startWeightAnimation(() -> {}, () -> {});
        
        GameState stateWithAnimation = animationManager.applyAnimationState(testState);
        
        assertTrue(stateWithAnimation.isWeightAnimating());
    }
    
    @Test
    @DisplayName("동시 다중 애니메이션 테스트")
    void testMultipleAnimationsSimultaneous() {
        animationManager.startLineClearAnimation(Arrays.asList(10), () -> {});
        animationManager.startAllClearAnimation(() -> {});
        
        // 두 애니메이션 모두 활성화되어야 함
        assertTrue(animationManager.isLineClearAnimating());
        assertTrue(animationManager.isAllClearAnimating());
        assertTrue(animationManager.isAnimating());
    }
    
    @Test
    @DisplayName("애니메이션 중복 시작 방지 테스트")
    void testPreventDuplicateAnimation() {
        boolean[] firstCallbackInvoked = {false};
        boolean[] secondCallbackInvoked = {false};
        
        animationManager.startLineClearAnimation(Arrays.asList(10), () -> {
            firstCallbackInvoked[0] = true;
        });
        
        // 이미 진행 중이면 두 번째는 즉시 콜백 호출
        animationManager.startLineClearAnimation(Arrays.asList(11), () -> {
            secondCallbackInvoked[0] = true;
        });
        
        assertTrue(secondCallbackInvoked[0]);
    }
    
    @Test
    @DisplayName("BoxClear - 벽 영역 제외 테스트")
    void testBoxClear_ExcludeWalls() {
        List<int[]> centers = Arrays.asList(new int[]{3, 1});
        
        animationManager.startBoxClearAnimation(centers, () -> {});
        
        // 벽 영역은 깜빡이지 않아야 함
        assertFalse(animationManager.isCellInBoxFlash(3, 0));  // 왼쪽 벽
        assertFalse(animationManager.isCellInBoxFlash(3, 11)); // 오른쪽 벽
        assertFalse(animationManager.isCellInBoxFlash(0, 1));  // 위쪽 벽
        assertFalse(animationManager.isCellInBoxFlash(22, 1)); // 바닥
    }
    
    @Test
    @DisplayName("애니메이션 없는 상태에서 applyAnimationState 테스트")
    void testApplyAnimationState_NoAnimation() {
        // 애니메이션이 없는 상태
        GameState stateWithAnimation = animationManager.applyAnimationState(testState);
        
        assertNotNull(stateWithAnimation);
        assertFalse(stateWithAnimation.isLineClearAnimating());
        assertFalse(stateWithAnimation.isAllClearAnimating());
        assertFalse(stateWithAnimation.isBoxClearAnimating());
        assertFalse(stateWithAnimation.isWeightAnimating());
    }
    
    @Test
    @DisplayName("깜빡임 상태 초기화 테스트")
    void testFlashStateInitialization() {
        AnimationManager manager = new AnimationManager(null);
        
        assertFalse(manager.isFlashBlack());
        assertFalse(manager.isAllClearFlashBlack());
        assertFalse(manager.isBoxFlashBlack());
    }
    
    @Test
    @DisplayName("행 깜빡임 - 애니메이션 없는 경우")
    void testRowFlashing_NoAnimation() {
        assertFalse(animationManager.isRowFlashing(10));
    }
    
    @Test
    @DisplayName("셀 깜빡임 - 애니메이션 없는 경우")
    void testCellFlashing_NoAnimation() {
        assertFalse(animationManager.isCellInBoxFlash(10, 5));
    }
}
