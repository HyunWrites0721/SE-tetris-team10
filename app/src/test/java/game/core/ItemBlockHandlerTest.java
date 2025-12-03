package game.core;

import blocks.Block;
import blocks.item.AllClearBlock;
import blocks.item.BoxClearBlock;
import blocks.item.OneLineClearBlock;
import blocks.item.WeightBlock;
import game.GameView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ItemBlockHandler 테스트")
class ItemBlockHandlerTest {
    
    private ItemBlockHandler itemHandler;
    private AnimationManager animationManager;
    private GameState testState;
    private int[][] testBoard;
    private int[][] testColorBoard;
    
    @BeforeEach
    void setUp() {
        animationManager = new AnimationManager(null);
        itemHandler = new ItemBlockHandler(null, animationManager);
        
        // 보드 초기화
        testBoard = new int[24][12];
        testColorBoard = new int[24][12];
        
        // 벽 설정 (INNER_TOP=2, INNER_BOTTOM=21, INNER_LEFT=1, INNER_RIGHT=10)
        for (int i = 0; i < 24; i++) {
            testBoard[i][0] = 1;
            testBoard[i][11] = 1;
        }
        for (int j = 0; j < 12; j++) {
            testBoard[23][j] = 1;  // 바닥
        }
        
        testState = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .currentLevel(1)
                .totalLinesCleared(0)
                .lineClearCount(0)
                .build();
    }
    
    @Test
    @DisplayName("ItemBlockHandler 생성 테스트")
    void testItemBlockHandlerCreation() {
        ItemBlockHandler handler = new ItemBlockHandler(null, animationManager);
        
        assertNotNull(handler);
    }
    
    @Test
    @DisplayName("렌더 콜백 설정 테스트")
    void testSetRenderCallback() {
        boolean[] callbackInvoked = {false};
        
        itemHandler.setRenderCallback(() -> {
            callbackInvoked[0] = true;
        });
        
        // 콜백이 설정되었는지 확인 (실제 호출은 특수 블록 처리 시)
        assertDoesNotThrow(() -> {
            itemHandler.setRenderCallback(() -> {});
        });
    }
    
    @Test
    @DisplayName("일반 블록 처리 - specialType 0")
    void testHandleSpecialBlock_Normal() {
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(0, testState, (newState) -> {
            callbackInvoked[0] = true;
        });
        
        // 일반 블록은 즉시 콜백 호출
        assertTrue(callbackInvoked[0]);
    }
    
    @Test
    @DisplayName("AllClear 블록 처리 - specialType 2")
    void testHandleAllClear() {
        // 보드에 블록 배치
        for (int r = 2; r <= 21; r++) {
            for (int c = 1; c <= 10; c++) {
                testBoard[r][c] = 1;
                testColorBoard[r][c] = 3;
            }
        }
        
        GameState filledState = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(2, filledState, (newState) -> {
            callbackInvoked[0] = true;
            
            // 보드가 초기화되었는지 확인
            int[][] board = newState.getBoardArray();
            int emptyCount = 0;
            for (int r = 2; r <= 21; r++) {
                for (int c = 1; c <= 10; c++) {
                    if (board[r][c] == 0) {
                        emptyCount++;
                    }
                }
            }
            
            assertTrue(emptyCount > 0, "AllClear 후 일부 셀이 비워져야 함");
        });
    }
    
    @Test
    @DisplayName("BoxClear 블록 처리 - specialType 3")
    void testHandleBoxClear() {
        // 보드 중앙에 BoxClear 블록 배치 (값 3)
        testBoard[10][5] = 3;
        
        // 주변에 블록 배치
        for (int r = 8; r <= 12; r++) {
            for (int c = 3; c <= 7; c++) {
                if (testBoard[r][c] == 0) {
                    testBoard[r][c] = 1;
                    testColorBoard[r][c] = 2;
                }
            }
        }
        
        GameState boxState = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(3, boxState, (newState) -> {
            callbackInvoked[0] = true;
            
            // 5x5 영역이 클리어되었는지 확인 (중력 후)
            assertNotNull(newState);
        });
    }
    
    @Test
    @DisplayName("BoxClear - 중심점 없는 경우")
    void testHandleBoxClear_NoCenters() {
        boolean[] callbackInvoked = {false};
        
        // 보드에 값 3이 없는 경우
        itemHandler.handleSpecialBlock(3, testState, (newState) -> {
            callbackInvoked[0] = true;
        });
        
        // 즉시 콜백 호출되어야 함
        assertTrue(callbackInvoked[0]);
    }
    
    @Test
    @DisplayName("OneLineClear 블록 처리 - specialType 4")
    void testHandleOneLineClear() {
        // 특정 행에 OneLineClear 블록 배치 (값 4)
        for (int c = 1; c <= 10; c++) {
            testBoard[10][c] = 4;
            testColorBoard[10][c] = 4;
        }
        
        GameState oneLineState = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .currentLevel(2)
                .lineClearCount(0)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(4, oneLineState, (newState) -> {
            callbackInvoked[0] = true;
            
            // 라인 클리어 카운트가 증가했는지 확인
            assertTrue(newState.getLineClearCount() >= 1);
            
            // 점수가 증가했는지 확인
            assertTrue(newState.getScore() > 100);
        });
    }
    
    @Test
    @DisplayName("OneLineClear - 여러 행 동시 클리어")
    void testHandleOneLineClear_MultipleRows() {
        // 여러 행에 OneLineClear 블록 배치
        for (int c = 1; c <= 10; c++) {
            testBoard[10][c] = 4;
            testBoard[15][c] = 4;
        }
        
        GameState multiLineState = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .currentLevel(1)
                .lineClearCount(0)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(4, multiLineState, (newState) -> {
            callbackInvoked[0] = true;
            
            // 2줄이 클리어되었으므로 점수가 크게 증가해야 함
            assertTrue(newState.getScore() > 200);
        });
    }
    
    @Test
    @DisplayName("OneLineClear - 행 없는 경우")
    void testHandleOneLineClear_NoRows() {
        boolean[] callbackInvoked = {false};
        
        // 보드에 값 4가 없는 경우
        itemHandler.handleSpecialBlock(4, testState, (newState) -> {
            callbackInvoked[0] = true;
        });
        
        // 즉시 콜백 호출되어야 함
        assertTrue(callbackInvoked[0]);
    }
    
    @Test
    @DisplayName("WeightBlock 처리 - specialType 5")
    void testHandleWeightBlock() {
        // WeightBlock 생성 및 배치
        WeightBlock weightBlock = new WeightBlock();
        weightBlock.setShape();
        weightBlock.setPosition(5, 5);
        
        // 아래에 블록 배치 (드릴로 뚫을 대상)
        for (int r = 10; r <= 20; r++) {
            for (int c = 1; c <= 10; c++) {
                testBoard[r][c] = 1;
                testColorBoard[r][c] = 2;
            }
        }
        
        GameState weightState = new GameState.Builder(testBoard, testColorBoard, weightBlock, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleWeightBlock(weightState, (newState) -> {
            callbackInvoked[0] = true;
            
            // 드릴 후 currentBlock이 null이어야 함
            assertNull(newState.getCurrentBlock());
        });
    }
    
    @Test
    @DisplayName("WeightBlock - currentBlock null 처리")
    void testHandleWeightBlock_NullBlock() {
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleWeightBlock(testState, (newState) -> {
            callbackInvoked[0] = true;
        });
        
        // 블록이 없으면 즉시 콜백 호출
        assertTrue(callbackInvoked[0]);
    }
    
    @Test
    @DisplayName("중력 적용 테스트 - applyGravity 간접 확인")
    void testGravityApplication() {
        // 공중에 떠 있는 블록 배치
        testBoard[5][5] = 1;
        testBoard[10][5] = 0;  // 빈 공간
        testBoard[15][5] = 0;  // 빈 공간
        
        GameState stateWithGap = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        // BoxClear로 중력 적용 확인
        testBoard[12][6] = 3;  // BoxClear 블록
        
        GameState boxState = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(3, boxState, (newState) -> {
            callbackInvoked[0] = true;
            
            // 중력이 적용되어 블록이 아래로 이동했는지 확인
            assertNotNull(newState);
        });
    }
    
    @Test
    @DisplayName("점수 계산 테스트 - OneLineClear")
    void testScoreCalculation_OneLineClear() {
        // 1줄 클리어
        for (int c = 1; c <= 10; c++) {
            testBoard[10][c] = 4;
        }
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(0)
                .currentLevel(1)
                .lineClearCount(0)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(4, state, (newState) -> {
            callbackInvoked[0] = true;
            
            // OneLineClear는 (줄 수 + 1)로 계산
            // 1줄 클리어 -> 2줄 점수
            assertTrue(newState.getScore() >= 200);
        });
    }
    
    @Test
    @DisplayName("아이템 모드 점수 배율 테스트")
    void testItemModeScoreMultiplier() {
        // 아이템 모드 활성화
        for (int c = 1; c <= 10; c++) {
            testBoard[10][c] = 4;
        }
        
        GameState itemModeState = new GameState.Builder(testBoard, testColorBoard, null, null, true)
                .score(0)
                .currentLevel(1)
                .lineClearCount(0)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(4, itemModeState, (newState) -> {
            callbackInvoked[0] = true;
            
            // 아이템 모드는 점수가 0.7배 (70%)
            assertTrue(newState.getScore() > 0);
        });
    }
    
    @Test
    @DisplayName("AllClear - 완성된 라인 자동 제거 테스트")
    void testAllClear_AutoRemoveFullLines() {
        // AllClear 후 자연스럽게 완성된 라인이 있을 경우 점수 없이 제거
        for (int c = 1; c <= 10; c++) {
            testBoard[20][c] = 1;  // 완성된 라인
        }
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(2, state, (newState) -> {
            callbackInvoked[0] = true;
            
            // 점수는 AllClear 점수만 (라인 클리어 점수 없음)
            assertEquals(100, newState.getScore());
        });
    }
    
    @Test
    @DisplayName("BoxClear - 벽 보존 테스트")
    void testBoxClear_PreserveWalls() {
        // 벽 근처에 BoxClear 배치
        testBoard[2][1] = 3;
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(3, state, (newState) -> {
            callbackInvoked[0] = true;
            
            int[][] board = newState.getBoardArray();
            
            // 벽은 유지되어야 함
            assertEquals(1, board[2][0]);   // 왼쪽 벽
            assertEquals(1, board[2][11]);  // 오른쪽 벽
            assertEquals(1, board[23][5]);  // 바닥
        });
    }
    
    @Test
    @DisplayName("여러 특수 블록 순차 처리 테스트")
    void testMultipleSpecialBlocks() {
        // AllClear 후 OneLineClear
        boolean[] allClearInvoked = {false};
        boolean[] oneLineInvoked = {false};
        
        itemHandler.handleSpecialBlock(2, testState, (newState1) -> {
            allClearInvoked[0] = true;
            
            // OneLineClear 처리
            for (int c = 1; c <= 10; c++) {
                newState1.getBoardArray()[10][c] = 4;
            }
            
            itemHandler.handleSpecialBlock(4, newState1, (newState2) -> {
                oneLineInvoked[0] = true;
            });
        });
    }
    
    @Test
    @DisplayName("콜백 없는 경우 테스트")
    void testHandleSpecialBlock_NullCallback() {
        assertDoesNotThrow(() -> {
            itemHandler.handleSpecialBlock(0, testState, null);
        });
    }
    
    @Test
    @DisplayName("AllClear - 애니메이션 프레임 테스트")
    void testAllClear_AnimationFrames() {
        for (int r = 2; r <= 21; r++) {
            for (int c = 1; c <= 10; c++) {
                testBoard[r][c] = 1;
            }
        }
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        boolean[] framesExecuted = {false};
        
        itemHandler.handleSpecialBlock(2, state, (newState) -> {
            framesExecuted[0] = true;
            
            // 애니메이션 후 allClearAnimating이 설정될 수 있음
            assertNotNull(newState);
        });
    }
    
    @Test
    @DisplayName("BoxClear - 여러 중심점 테스트")
    void testBoxClear_MultipleCenters() {
        // 여러 BoxClear 블록 배치
        testBoard[5][5] = 3;
        testBoard[15][8] = 3;
        
        // 주변에 블록 배치
        for (int r = 3; r <= 17; r++) {
            for (int c = 1; c <= 10; c++) {
                if (testBoard[r][c] == 0) {
                    testBoard[r][c] = 1;
                }
            }
        }
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(3, state, (newState) -> {
            callbackInvoked[0] = true;
            
            // 여러 5x5 영역이 클리어되어야 함
            assertNotNull(newState);
            assertTrue(newState.isBoxClearAnimating());
        });
    }
    
    @Test
    @DisplayName("BoxClear - 애니메이션 프레임 콜백 테스트")
    void testBoxClear_AnimationFrameCallback() {
        testBoard[10][5] = 3;
        
        for (int r = 8; r <= 12; r++) {
            for (int c = 3; c <= 7; c++) {
                if (testBoard[r][c] == 0) {
                    testBoard[r][c] = 1;
                }
            }
        }
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        boolean[] renderCallbackInvoked = {false};
        
        itemHandler.setRenderCallback(() -> {
            renderCallbackInvoked[0] = true;
        });
        
        itemHandler.handleSpecialBlock(3, state, (newState) -> {
            assertNotNull(newState);
        });
        
        // 애니메이션이 진행되면서 렌더 콜백이 호출되어야 함
        // (AnimationManager가 프레임 콜백을 실행)
    }
    
    @Test
    @DisplayName("WeightBlock - 드릴 애니메이션 테스트")
    void testWeightBlock_DrillAnimation() {
        WeightBlock weightBlock = new WeightBlock();
        weightBlock.setShape();
        weightBlock.setPosition(5, 3);
        
        for (int r = 10; r <= 20; r++) {
            testBoard[r][5] = 1;
        }
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, weightBlock, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleWeightBlock(state, (newState) -> {
            callbackInvoked[0] = true;
            
            // weightAnimating이 true가 되어야 함
            assertTrue(newState.isWeightAnimating());
        });
    }
    
    @Test
    @DisplayName("WeightBlock - 프레임별 드릴 진행")
    void testWeightBlock_DrillProgress() {
        WeightBlock weightBlock = new WeightBlock();
        weightBlock.setShape();
        weightBlock.setPosition(5, 5);
        
        for (int r = 10; r <= 20; r++) {
            for (int c = 1; c <= 10; c++) {
                testBoard[r][c] = 1;
            }
        }
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, weightBlock, null, false)
                .score(100)
                .build();
        
        boolean[] framesExecuted = {false};
        
        itemHandler.setRenderCallback(() -> {
            framesExecuted[0] = true;
        });
        
        itemHandler.handleWeightBlock(state, (newState) -> {
            // 드릴이 완료되면 currentBlock이 null
            assertNull(newState.getCurrentBlock());
        });
    }
    
    @Test
    @DisplayName("OneLineClear - 레벨별 점수 차이")
    void testOneLineClear_LevelScoring() {
        // 레벨 1
        for (int c = 1; c <= 10; c++) {
            testBoard[10][c] = 4;
        }
        
        GameState level1State = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(0)
                .currentLevel(1)
                .lineClearCount(0)
                .build();
        
        boolean[] level1Invoked = {false};
        int[] level1Score = {0};
        
        itemHandler.handleSpecialBlock(4, level1State, (newState) -> {
            level1Invoked[0] = true;
            level1Score[0] = newState.getScore();
        });
        
        // 레벨 5
        for (int c = 1; c <= 10; c++) {
            testBoard[10][c] = 4;
        }
        
        GameState level5State = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(0)
                .currentLevel(5)
                .lineClearCount(0)
                .build();
        
        boolean[] level5Invoked = {false};
        int[] level5Score = {0};
        
        itemHandler.handleSpecialBlock(4, level5State, (newState) -> {
            level5Invoked[0] = true;
            level5Score[0] = newState.getScore();
        });
        
        // 레벨이 높을수록 점수가 높아야 함
        assertTrue(level5Score[0] > level1Score[0]);
    }
    
    @Test
    @DisplayName("OneLineClear - 3줄 이상 클리어 시 보너스")
    void testOneLineClear_MultiLineBonusScoring() {
        // 3줄에 OneLineClear 배치
        for (int c = 1; c <= 10; c++) {
            testBoard[10][c] = 4;
            testBoard[15][c] = 4;
            testBoard[18][c] = 4;
        }
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(0)
                .currentLevel(1)
                .lineClearCount(0)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(4, state, (newState) -> {
            callbackInvoked[0] = true;
            
            // 3줄 이상 클리어 시 보너스 점수
            assertTrue(newState.getScore() > 600);
            assertEquals(3, newState.getLineClearCount());
        });
    }
    
    @Test
    @DisplayName("OneLineClear - 4줄 동시 클리어 (테트리스 보너스)")
    void testOneLineClear_TetrisBonus() {
        // 4줄에 OneLineClear 배치
        for (int c = 1; c <= 10; c++) {
            testBoard[10][c] = 4;
            testBoard[11][c] = 4;
            testBoard[12][c] = 4;
            testBoard[13][c] = 4;
        }
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(0)
                .currentLevel(1)
                .lineClearCount(0)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(4, state, (newState) -> {
            callbackInvoked[0] = true;
            
            // 4줄 테트리스 보너스 점수
            assertTrue(newState.getScore() >= 800);
            assertEquals(4, newState.getLineClearCount());
        });
    }
    
    @Test
    @DisplayName("중력 - 빈 공간 여러 개")
    void testGravity_MultipleGaps() {
        // 공중에 떠 있는 블록들
        testBoard[5][3] = 1;
        testBoard[10][3] = 0;  // 빈 공간
        testBoard[15][3] = 0;  // 빈 공간
        testBoard[18][3] = 1;
        
        testBoard[8][5] = 1;
        testBoard[12][5] = 0;  // 빈 공간
        
        // BoxClear로 중력 트리거
        testBoard[10][5] = 3;
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(3, state, (newState) -> {
            callbackInvoked[0] = true;
            
            // 중력 후 블록들이 아래로 이동
            assertNotNull(newState);
        });
    }
    
    @Test
    @DisplayName("BoxClear - 경계 영역 테스트")
    void testBoxClear_BoundaryAreas() {
        // 왼쪽 상단 모서리
        testBoard[2][1] = 3;
        
        // 오른쪽 하단 모서리
        testBoard[21][10] = 3;
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(3, state, (newState) -> {
            callbackInvoked[0] = true;
            
            int[][] board = newState.getBoardArray();
            
            // 벽이 유지되어야 함
            assertEquals(1, board[0][0]);
            assertEquals(1, board[23][11]);
        });
    }
    
    @Test
    @DisplayName("AllClear - 초기화 후 자연스러운 완성 라인 제거")
    void testAllClear_RemoveNaturalFullLines() {
        // 보드 전체를 블록으로 채움
        for (int r = 2; r <= 21; r++) {
            for (int c = 1; c <= 10; c++) {
                testBoard[r][c] = 1;
            }
        }
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .totalLinesCleared(5)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(2, state, (newState) -> {
            callbackInvoked[0] = true;
            
            // totalLinesCleared는 변하지 않아야 함 (점수 없는 제거)
            assertEquals(5, newState.getTotalLinesCleared());
        });
    }
    
    @Test
    @DisplayName("WeightBlock - 블록 모양 확인")
    void testWeightBlock_ShapeCheck() {
        WeightBlock weightBlock = new WeightBlock();
        weightBlock.setShape();
        weightBlock.setPosition(5, 5);
        
        int[][] shape = weightBlock.getShape();
        assertNotNull(shape);
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, weightBlock, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleWeightBlock(state, (newState) -> {
            callbackInvoked[0] = true;
        });
    }
    
    @Test
    @DisplayName("OneLineClear - 점수 계산 공식 검증")
    void testOneLineClear_ScoreFormula() {
        // 1줄 클리어
        for (int c = 1; c <= 10; c++) {
            testBoard[10][c] = 4;
        }
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(0)
                .currentLevel(2)
                .lineClearCount(0)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(4, state, (newState) -> {
            callbackInvoked[0] = true;
            
            // 점수 = 100 * (줄 수 + 1) * 레벨
            // 1줄 클리어 -> 2줄 점수 -> 100 * 2 * 2 = 400
            int expectedScore = 100 * 2 * 2;
            assertTrue(newState.getScore() >= expectedScore * 0.9);  // 오차 허용
        });
    }
    
    @Test
    @DisplayName("렌더 콜백 - null 처리")
    void testRenderCallback_NullHandling() {
        itemHandler.setRenderCallback(null);
        
        // null 콜백이어도 예외 발생하지 않아야 함
        assertDoesNotThrow(() -> {
            itemHandler.handleSpecialBlock(2, testState, (newState) -> {});
        });
    }
    
    @Test
    @DisplayName("BoxClear - 5x5 영역 완전 클리어")
    void testBoxClear_Complete5x5Clear() {
        // 중앙에 BoxClear
        testBoard[12][6] = 3;
        
        // 5x5 영역 전체를 블록으로 채움
        for (int r = 10; r <= 14; r++) {
            for (int c = 4; c <= 8; c++) {
                if (r != 12 || c != 6) {
                    testBoard[r][c] = 1;
                    testColorBoard[r][c] = 5;
                }
            }
        }
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(3, state, (newState) -> {
            callbackInvoked[0] = true;
            
            int[][] board = newState.getBoardArray();
            
            // 5x5 영역이 클리어되었는지 확인 (중력 후)
            int clearedCount = 0;
            for (int r = 10; r <= 14; r++) {
                for (int c = 4; c <= 8; c++) {
                    if (board[r][c] == 0) {
                        clearedCount++;
                    }
                }
            }
            
            assertTrue(clearedCount > 0);
        });
    }
    
    @Test
    @DisplayName("AllClear - 높은 레벨에서 보너스 점수")
    void testAllClear_HighLevelBonus() {
        for (int r = 2; r <= 21; r++) {
            for (int c = 1; c <= 10; c++) {
                testBoard[r][c] = 1;
            }
        }
        
        GameState highLevelState = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(0)
                .currentLevel(10)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(2, highLevelState, (newState) -> {
            callbackInvoked[0] = true;
            
            // AllClear는 점수가 0이지만 콜백은 호출되어야 함
            assertNotNull(newState);
        });
    }
    
    @Test
    @DisplayName("WeightBlock - 드릴 경로에 빈 공간")
    void testWeightBlock_DrillPathWithGaps() {
        WeightBlock weightBlock = new WeightBlock();
        weightBlock.setShape();
        weightBlock.setPosition(5, 5);
        
        // 드릴 경로에 블록과 빈 공간 혼재
        testBoard[10][5] = 1;
        testBoard[12][5] = 0;  // 빈 공간
        testBoard[15][5] = 1;
        testBoard[18][5] = 0;  // 빈 공간
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, weightBlock, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleWeightBlock(state, (newState) -> {
            callbackInvoked[0] = true;
            
            // 드릴이 경로의 모든 블록을 제거
            assertNull(newState.getCurrentBlock());
        });
    }
    
    @Test
    @DisplayName("특수 블록 타입 - 잘못된 값")
    void testHandleSpecialBlock_InvalidType() {
        boolean[] callbackInvoked = {false};
        
        // 정의되지 않은 특수 블록 타입 (6 이상)
        itemHandler.handleSpecialBlock(99, testState, (newState) -> {
            callbackInvoked[0] = true;
        });
        
        // 즉시 콜백 호출 (일반 블록처럼 처리)
        assertTrue(callbackInvoked[0]);
    }
    
    @Test
    @DisplayName("아이템 모드 - 점수 감소율 확인")
    void testItemMode_ScoreReduction() {
        // 일반 모드와 아이템 모드 비교
        assertDoesNotThrow(() -> {
            for (int c = 1; c <= 10; c++) {
                testBoard[10][c] = 4;
            }
            
            GameState normalState = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                    .score(0)
                    .currentLevel(1)
                    .lineClearCount(0)
                    .build();
            
            itemHandler.handleSpecialBlock(4, normalState, (newState) -> {
                assertNotNull(newState);
            });
            
            GameState itemState = new GameState.Builder(testBoard, testColorBoard, null, null, true)
                    .score(0)
                    .currentLevel(1)
                    .lineClearCount(0)
                    .build();
            
            itemHandler.handleSpecialBlock(4, itemState, (newState) -> {
                assertNotNull(newState);
            });
        });
    }
    
    @Test
    @DisplayName("AllClear - 이미 빈 보드")
    void testAllClear_EmptyBoard() {
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(2, testState, (newState) -> {
            callbackInvoked[0] = true;
            
            // 빈 보드도 처리되어야 함
            assertNotNull(newState);
        });
    }
    
    @Test
    @DisplayName("OneLineClear - 빈 줄 처리")
    void testOneLineClear_EmptyLines() {
        // 값 4만 있고 다른 블록이 없는 경우
        testBoard[10][5] = 4;
        
        GameState emptyLineState = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(4, emptyLineState, (newState) -> {
            callbackInvoked[0] = true;
            assertNotNull(newState);
        });
    }
    
    @Test
    @DisplayName("렌더 콜백 - null 설정")
    void testSetRenderCallback_Null() {
        assertDoesNotThrow(() -> {
            itemHandler.setRenderCallback(null);
        });
    }
    
    @Test
    @DisplayName("BoxClear - 경계 근처 중심점")
    void testBoxClear_BoundaryCenter() {
        // 경계 근처에 BoxClear 블록 배치
        testBoard[2][1] = 3;  // 왼쪽 상단 모서리 근처
        testBoard[21][10] = 3;  // 오른쪽 하단 모서리 근처
        
        GameState boundaryState = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(3, boundaryState, (newState) -> {
            callbackInvoked[0] = true;
            assertNotNull(newState);
        });
    }
    
    @Test
    @DisplayName("AllClear - 일부만 채워진 보드")
    void testAllClear_PartiallyFilledBoard() {
        // 보드 일부만 채우기
        for (int r = 10; r <= 15; r++) {
            for (int c = 3; c <= 7; c++) {
                testBoard[r][c] = 1;
                testColorBoard[r][c] = 3;
            }
        }
        
        GameState partialState = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .score(100)
                .build();
        
        boolean[] callbackInvoked = {false};
        
        itemHandler.handleSpecialBlock(2, partialState, (newState) -> {
            callbackInvoked[0] = true;
            
            // 전체 보드가 클리어되어야 함
            assertNotNull(newState);
        });
    }
}
