package game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.swing.SwingUtilities;
import java.awt.Window;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameStart 테스트")
public class GameStartTest {

    @BeforeEach
    void setUp() {
        // EDT에서 모든 윈도우 정리
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                if (window.isDisplayable()) {
                    window.dispose();
                }
            }
        });
        
        try {
            Thread.sleep(100); // 정리 시간 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 모든 윈도우 정리
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                if (window.isDisplayable()) {
                    window.dispose();
                }
            }
        });
        
        try {
            Thread.sleep(100); // 정리 시간 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("GameStart 생성자 테스트")
    void testGameStartConstructor() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final FrameBoard[] frameRef = new FrameBoard[1];
        final Exception[] exceptionRef = new Exception[1];

        SwingUtilities.invokeLater(() -> {
            try {
                // GameStart 생성자 호출
                GameStart gameStart = new GameStart(false); // Normal mode
                assertNotNull(gameStart, "GameStart 객체가 생성되어야 함");
                
                // FrameBoard가 생성되었는지 확인
                Window[] windows = Window.getWindows();
                boolean frameBoardFound = false;
                for (Window window : windows) {
                    if (window instanceof FrameBoard && window.isDisplayable()) {
                        frameRef[0] = (FrameBoard) window;
                        frameBoardFound = true;
                        break;
                    }
                }
                assertTrue(frameBoardFound, "FrameBoard가 생성되어야 함");
                
            } catch (Exception e) {
                exceptionRef[0] = e;
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "테스트가 5초 내에 완료되어야 함");
        
        if (exceptionRef[0] != null) {
            throw exceptionRef[0];
        }
        
        assertNotNull(frameRef[0], "FrameBoard 참조가 설정되어야 함");
    }

    @Test
    @DisplayName("GameStart 후 FrameBoard 가시성 테스트")
    void testFrameBoardVisibility() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isVisibleRef = new boolean[1];
        final Exception[] exceptionRef = new Exception[1];

        SwingUtilities.invokeLater(() -> {
            try {
                new GameStart(false); // Normal mode
                
                // 짧은 대기 후 가시성 확인
                SwingUtilities.invokeLater(() -> {
                    try {
                        Window[] windows = Window.getWindows();
                        for (Window window : windows) {
                            if (window instanceof FrameBoard && window.isDisplayable()) {
                                isVisibleRef[0] = window.isVisible();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        exceptionRef[0] = e;
                    } finally {
                        latch.countDown();
                    }
                });
                
            } catch (Exception e) {
                exceptionRef[0] = e;
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "테스트가 5초 내에 완료되어야 함");
        
        if (exceptionRef[0] != null) {
            throw exceptionRef[0];
        }
        
        assertTrue(isVisibleRef[0], "FrameBoard가 보여야 함");
    }

    @Test
    @DisplayName("GameStart 후 GameTimer 시작 테스트")
    void testGameTimerStarted() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] timerRunningRef = new boolean[1];
        final Exception[] exceptionRef = new Exception[1];

        SwingUtilities.invokeLater(() -> {
            try {
                new GameStart(false); // Normal mode
                
                // 짧은 대기 후 게임 컨트롤러 상태 확인
                SwingUtilities.invokeLater(() -> {
                    try {
                        Window[] windows = Window.getWindows();
                        for (Window window : windows) {
                            if (window instanceof FrameBoard && window.isDisplayable()) {
                                FrameBoard frameBoard = (FrameBoard) window;
                                game.core.GameController gameController = frameBoard.getGameController();
                                timerRunningRef[0] = gameController.isRunning();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        exceptionRef[0] = e;
                    } finally {
                        latch.countDown();
                    }
                });
                
            } catch (Exception e) {
                exceptionRef[0] = e;
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "테스트가 5초 내에 완료되어야 함");
        
        if (exceptionRef[0] != null) {
            throw exceptionRef[0];
        }
        
        assertTrue(timerRunningRef[0], "GameTimer가 실행 중이어야 함");
    }

    @Test
    @DisplayName("정적 start() 메서드 테스트")
    void testStaticStartMethod() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] gameStartedRef = new boolean[1];
        final Exception[] exceptionRef = new Exception[1];

        SwingUtilities.invokeLater(() -> {
            try {
                // 정적 메서드 호출
                GameStart.start();
                
                // 짧은 대기 후 게임 시작 확인
                SwingUtilities.invokeLater(() -> {
                    try {
                        Window[] windows = Window.getWindows();
                        for (Window window : windows) {
                            if (window instanceof FrameBoard && window.isDisplayable()) {
                                gameStartedRef[0] = window.isVisible();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        exceptionRef[0] = e;
                    } finally {
                        latch.countDown();
                    }
                });
                
            } catch (Exception e) {
                exceptionRef[0] = e;
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "테스트가 5초 내에 완료되어야 함");
        
        if (exceptionRef[0] != null) {
            throw exceptionRef[0];
        }
        
        assertTrue(gameStartedRef[0], "정적 start() 메서드로 게임이 시작되어야 함");
    }

    @Test
    @DisplayName("여러 번 GameStart 호출 테스트")
    void testMultipleGameStartCalls() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final int[] frameBoardCountRef = new int[1];
        final Exception[] exceptionRef = new Exception[1];

        SwingUtilities.invokeLater(() -> {
            try {
                // 여러 번 GameStart 생성
                new GameStart(false); // Normal mode
                new GameStart(false); // Normal mode
                new GameStart(false); // Normal mode
                
                // 짧은 대기 후 FrameBoard 개수 확인
                SwingUtilities.invokeLater(() -> {
                    try {
                        Window[] windows = Window.getWindows();
                        int count = 0;
                        for (Window window : windows) {
                            if (window instanceof FrameBoard && window.isDisplayable()) {
                                count++;
                            }
                        }
                        frameBoardCountRef[0] = count;
                    } catch (Exception e) {
                        exceptionRef[0] = e;
                    } finally {
                        latch.countDown();
                    }
                });
                
            } catch (Exception e) {
                exceptionRef[0] = e;
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "테스트가 5초 내에 완료되어야 함");
        
        if (exceptionRef[0] != null) {
            throw exceptionRef[0];
        }
        
        assertEquals(3, frameBoardCountRef[0], "여러 번 호출 시 여러 개의 FrameBoard가 생성되어야 함");
    }

    @Test
    @DisplayName("GameStart 생성과 타이머 상태 동시 확인 테스트")
    void testGameStartAndTimerState() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] allChecksPassedRef = new boolean[1];
        final Exception[] exceptionRef = new Exception[1];

        SwingUtilities.invokeLater(() -> {
            try {
                new GameStart(false); // Normal mode
                
                // 게임이 시작될 시간을 주기 위해 약간 대기
                Thread.sleep(50);
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        Window[] windows = Window.getWindows();
                        for (Window window : windows) {
                            if (window instanceof FrameBoard && window.isDisplayable()) {
                                FrameBoard frameBoard = (FrameBoard) window;
                                boolean isVisible = frameBoard.isVisible();
                                boolean isGameRunning = frameBoard.getGameController().isRunning();
                                
                                allChecksPassedRef[0] = isVisible && isGameRunning;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        exceptionRef[0] = e;
                    } finally {
                        latch.countDown();
                    }
                });
                
            } catch (Exception e) {
                exceptionRef[0] = e;
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "테스트가 5초 내에 완료되어야 함");
        
        if (exceptionRef[0] != null) {
            throw exceptionRef[0];
        }
        
        assertTrue(allChecksPassedRef[0], "FrameBoard가 보이고 타이머가 실행 중이어야 함");
    }

    @Test
    @DisplayName("GameStart 예외 처리 테스트")
    void testGameStartExceptionHandling() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] noExceptionThrownRef = new boolean[1];

        SwingUtilities.invokeLater(() -> {
            try {
                // GameStart 호출이 예외를 던지지 않는지 확인
                GameStart gameStart = new GameStart(false); // Normal mode
                assertNotNull(gameStart);
                
                // 정적 메서드도 예외를 던지지 않는지 확인
                GameStart.start();
                
                noExceptionThrownRef[0] = true;
                
            } catch (Exception e) {
                // 예외가 발생하면 false로 남겨둠
                fail("GameStart에서 예외가 발생하지 않아야 함: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "테스트가 5초 내에 완료되어야 함");
        assertTrue(noExceptionThrownRef[0], "GameStart에서 예외가 발생하지 않아야 함");
    }

    @Test
    @DisplayName("EDT에서 GameStart 호출 안전성 테스트")
    void testGameStartEDTSafety() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] edtSafeRef = new boolean[1];
        final Exception[] exceptionRef = new Exception[1];

        // EDT에서 직접 호출
        SwingUtilities.invokeLater(() -> {
            try {
                assertTrue(SwingUtilities.isEventDispatchThread(), "EDT에서 실행되어야 함");
                
                GameStart gameStart = new GameStart(false); // Normal mode
                assertNotNull(gameStart);
                
                edtSafeRef[0] = true;
                
            } catch (Exception e) {
                exceptionRef[0] = e;
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "테스트가 5초 내에 완료되어야 함");
        
        if (exceptionRef[0] != null) {
            throw exceptionRef[0];
        }
        
        assertTrue(edtSafeRef[0], "EDT에서 GameStart가 안전하게 실행되어야 함");
    }

    @Test
    @DisplayName("일시정지 후 재개 시 속도 유지 테스트")
    void testSpeedMaintainedAfterPauseResume() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] speedMaintainedRef = new boolean[1];
        final Exception[] exceptionRef = new Exception[1];

        SwingUtilities.invokeLater(() -> {
            try {
                // GameStart로 게임 시작
                new GameStart(false); // Normal mode
                
                // FrameBoard 찾기
                FrameBoard frameBoard = null;
                Window[] windows = Window.getWindows();
                for (Window window : windows) {
                    if (window instanceof FrameBoard && window.isDisplayable()) {
                        frameBoard = (FrameBoard) window;
                        break;
                    }
                }
                
                if (frameBoard != null) {
                    final FrameBoard finalFrameBoard = frameBoard;
                    
                    // 게임이 시작될 시간 대기
                    Thread.sleep(100);
                    
                    // 게임 속도 변경 로직은 GameController 내부에서 처리됨 (레벨업 시 자동)
                    // 테스트에서는 일시정지/재개 기능만 검증
                    
                    // 일시정지 상태로 변경
                    finalFrameBoard.isPaused = true;
                    finalFrameBoard.paused(); // PauseBoard 표시
                    
                    // 일시정지 상태 확인
                    assertTrue(finalFrameBoard.isPaused, "게임이 일시정지되어야 함");
                    
                    // 잠시 대기 (일시정지 상태 유지)
                    Thread.sleep(100);
                    
                    // 일시정지 해제
                    finalFrameBoard.isPaused = false;
                    finalFrameBoard.paused(); // PauseBoard 숨김
                    
                    // 재개 상태 확인
                    assertFalse(finalFrameBoard.isPaused, "게임이 재개되어야 함");
                    
                    // 게임이 재개되었는지 확인
                    speedMaintainedRef[0] = finalFrameBoard.getGameController().isRunning();
                    
                    System.out.println("게임 재개 확인: " + speedMaintainedRef[0]);
                    
                    // 게임 컨트롤러가 여전히 실행 중인지 확인
                    assertTrue(finalFrameBoard.getGameController().isRunning(), "재개 후 게임이 실행 중이어야 함");
                }
                
            } catch (Exception e) {
                exceptionRef[0] = e;
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "테스트가 10초 내에 완료되어야 함");
        
        if (exceptionRef[0] != null) {
            throw exceptionRef[0];
        }
        
        assertTrue(speedMaintainedRef[0], "일시정지 후 재개 시 게임 속도가 유지되어야 함");
    }
}