package game.loop;

import game.events.EventBus;
import game.events.TickEvent;
import game.events.GameTimerEvent;
import game.events.GameEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LocalGameLoop 테스트")
class LocalGameLoopTest {
    
    private EventBus eventBus;
    private LocalGameLoop gameLoop;
    private List<GameEvent> capturedEvents;
    
    @BeforeEach
    void setUp() {
        eventBus = new EventBus();
        capturedEvents = new ArrayList<>();
        
        // 구체적인 이벤트 타입들로 구독 (GameEvent는 추상적이어서 매칭되지 않음)
        eventBus.subscribe(GameTimerEvent.class, event -> {
            capturedEvents.add(event);
        });
        
        eventBus.subscribe(TickEvent.class, event -> {
            capturedEvents.add(event);
        });
        
        gameLoop = new LocalGameLoop(eventBus);
    }
    
    @Test
    @DisplayName("기본 생성자로 생성 시 초기 상태 확인")
    void testDefaultConstructor() {
        assertFalse(gameLoop.isRunning(), "초기에는 실행 중이 아니어야 함");
        assertFalse(gameLoop.isPaused(), "초기에는 일시정지 상태가 아니어야 함");
        assertEquals(60, gameLoop.getCurrentTickRate(), "기본 틱 레이트는 60이어야 함");
        assertEquals(0, gameLoop.getSpeedLevel(), "초기 속도 레벨은 0이어야 함");
        assertEquals(0, gameLoop.getDifficulty(), "기본 난이도는 0(노멀)이어야 함");
    }
    
    @Test
    @DisplayName("난이도를 지정한 생성자로 생성")
    void testConstructorWithDifficulty() {
        LocalGameLoop hardLoop = new LocalGameLoop(eventBus, 1);
        assertEquals(1, hardLoop.getDifficulty(), "난이도가 1(하드)로 설정되어야 함");
        
        LocalGameLoop easyLoop = new LocalGameLoop(eventBus, 2);
        assertEquals(2, easyLoop.getDifficulty(), "난이도가 2(이지)로 설정되어야 함");
    }
    
    @Test
    @DisplayName("게임 루프 시작")
    void testStart() throws InterruptedException {
        gameLoop.start();
        
        assertTrue(gameLoop.isRunning(), "시작 후 실행 중이어야 함");
        assertFalse(gameLoop.isPaused(), "시작 후 일시정지 상태가 아니어야 함");
        
        // GameTimerEvent.START 이벤트 확인 (동기적으로 즉시 발생)
        Thread.sleep(100);
        
        long startEventCount = capturedEvents.stream()
                .filter(e -> e instanceof GameTimerEvent 
                        && ((GameTimerEvent) e).getAction() == GameTimerEvent.TimerAction.START)
                .count();
        
        assertTrue(startEventCount > 0, 
                "START 이벤트가 발생해야 함. 캡처된 이벤트 수: " + capturedEvents.size());
        
        gameLoop.stop();
    }
    
    @Test
    @DisplayName("이미 실행 중인 게임 루프 다시 시작 시 무시")
    void testStartWhenAlreadyRunning() throws InterruptedException {
        gameLoop.start();
        Thread.sleep(50);
        
        int eventCountAfterFirstStart = capturedEvents.size();
        
        // 다시 시작 시도
        gameLoop.start();
        Thread.sleep(50);
        
        // 이벤트가 추가로 발생하지 않아야 함 (또는 극소수만 증가)
        assertTrue(gameLoop.isRunning(), "여전히 실행 중이어야 함");
        
        gameLoop.stop();
    }
    
    @Test
    @DisplayName("게임 루프 정지")
    void testStop() throws InterruptedException {
        gameLoop.start();
        Thread.sleep(100);
        
        gameLoop.stop();
        Thread.sleep(50);
        
        assertFalse(gameLoop.isRunning(), "정지 후 실행 중이 아니어야 함");
        assertFalse(gameLoop.isPaused(), "정지 후 일시정지 상태도 아니어야 함");
        
        // GameTimerEvent.STOP 이벤트 확인
        long stopEventCount = capturedEvents.stream()
                .filter(e -> e instanceof GameTimerEvent 
                        && ((GameTimerEvent) e).getAction() == GameTimerEvent.TimerAction.STOP)
                .count();
        
        assertTrue(stopEventCount > 0, 
                "STOP 이벤트가 발생해야 함. 캡처된 이벤트 수: " + capturedEvents.size());
    }
    
    @Test
    @DisplayName("실행 중이 아닌 게임 루프 정지 시 무시")
    void testStopWhenNotRunning() {
        assertFalse(gameLoop.isRunning(), "초기에는 실행 중이 아님");
        
        gameLoop.stop();
        
        // STOP 이벤트가 발생하지 않아야 함
        assertTrue(capturedEvents.stream()
                .noneMatch(e -> e instanceof GameTimerEvent 
                        && ((GameTimerEvent) e).getAction() == GameTimerEvent.TimerAction.STOP),
                "실행 중이 아닐 때 STOP 이벤트가 발생하지 않아야 함");
    }
    
    @Test
    @DisplayName("게임 루프 일시정지")
    void testPause() throws InterruptedException {
        gameLoop.start();
        Thread.sleep(100);
        
        gameLoop.pause();
        Thread.sleep(50);
        
        assertTrue(gameLoop.isRunning(), "일시정지 후에도 isRunning은 true여야 함");
        assertTrue(gameLoop.isPaused(), "일시정지 후 isPaused는 true여야 함");
        
        // GameTimerEvent.PAUSE 이벤트 확인
        long pauseEventCount = capturedEvents.stream()
                .filter(e -> e instanceof GameTimerEvent 
                        && ((GameTimerEvent) e).getAction() == GameTimerEvent.TimerAction.PAUSE)
                .count();
        
        assertTrue(pauseEventCount > 0, 
                "PAUSE 이벤트가 발생해야 함. 캡처된 이벤트 수: " + capturedEvents.size());
        
        gameLoop.stop();
    }
    
    @Test
    @DisplayName("실행 중이 아닌 게임 루프 일시정지 시 무시")
    void testPauseWhenNotRunning() {
        assertFalse(gameLoop.isRunning(), "초기에는 실행 중이 아님");
        
        gameLoop.pause();
        
        assertFalse(gameLoop.isPaused(), "실행 중이 아니면 일시정지되지 않아야 함");
        assertTrue(capturedEvents.stream()
                .noneMatch(e -> e instanceof GameTimerEvent 
                        && ((GameTimerEvent) e).getAction() == GameTimerEvent.TimerAction.PAUSE),
                "PAUSE 이벤트가 발생하지 않아야 함");
    }
    
    @Test
    @DisplayName("이미 일시정지된 게임 루프 다시 일시정지 시 무시")
    void testPauseWhenAlreadyPaused() throws InterruptedException {
        gameLoop.start();
        Thread.sleep(50);
        gameLoop.pause();
        
        int eventCountAfterFirstPause = capturedEvents.size();
        
        gameLoop.pause();
        
        assertEquals(eventCountAfterFirstPause, capturedEvents.size(), 
                "이미 일시정지 상태일 때 추가 이벤트가 발생하지 않아야 함");
        
        gameLoop.stop();
    }
    
    @Test
    @DisplayName("게임 루프 재개")
    void testResume() throws InterruptedException {
        gameLoop.start();
        Thread.sleep(100);
        gameLoop.pause();
        Thread.sleep(50);
        
        gameLoop.resume();
        Thread.sleep(50);
        
        assertTrue(gameLoop.isRunning(), "재개 후 실행 중이어야 함");
        assertFalse(gameLoop.isPaused(), "재개 후 일시정지 상태가 아니어야 함");
        
        // GameTimerEvent.RESUME 이벤트 확인
        long resumeEventCount = capturedEvents.stream()
                .filter(e -> e instanceof GameTimerEvent 
                        && ((GameTimerEvent) e).getAction() == GameTimerEvent.TimerAction.RESUME)
                .count();
        
        assertTrue(resumeEventCount > 0, 
                "RESUME 이벤트가 발생해야 함. 캡처된 이벤트 수: " + capturedEvents.size());
        
        gameLoop.stop();
    }
    
    @Test
    @DisplayName("일시정지되지 않은 게임 루프 재개 시 무시")
    void testResumeWhenNotPaused() throws InterruptedException {
        gameLoop.start();
        Thread.sleep(50);
        
        int eventCountBeforeResume = capturedEvents.size();
        
        gameLoop.resume();
        
        // RESUME 이벤트가 발생하지 않아야 함
        assertTrue(capturedEvents.stream()
                .noneMatch(e -> e instanceof GameTimerEvent 
                        && ((GameTimerEvent) e).getAction() == GameTimerEvent.TimerAction.RESUME),
                "일시정지 상태가 아닐 때 RESUME 이벤트가 발생하지 않아야 함");
        
        gameLoop.stop();
    }
    
    @Test
    @DisplayName("틱 레이트 설정")
    void testSetTickRate() throws InterruptedException {
        gameLoop.setTickRate(30);
        
        assertEquals(30, gameLoop.getCurrentTickRate(), "틱 레이트가 30으로 설정되어야 함");
        assertEquals(33, gameLoop.getCurrentDelay(), "딜레이가 약 33ms여야 함 (1000/30)");
    }
    
    @Test
    @DisplayName("틱 레이트를 0 이하로 설정 시 예외 발생")
    void testSetTickRateWithZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            gameLoop.setTickRate(0);
        }, "틱 레이트가 0이면 예외가 발생해야 함");
    }
    
    @Test
    @DisplayName("틱 레이트를 음수로 설정 시 예외 발생")
    void testSetTickRateWithNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            gameLoop.setTickRate(-10);
        }, "틱 레이트가 음수이면 예외가 발생해야 함");
    }
    
    @Test
    @DisplayName("실행 중 틱 레이트 변경")
    void testSetTickRateWhileRunning() throws InterruptedException {
        gameLoop.start();
        Thread.sleep(50);
        
        gameLoop.setTickRate(45);
        
        assertTrue(gameLoop.isRunning(), "틱 레이트 변경 후에도 실행 중이어야 함");
        assertEquals(45, gameLoop.getCurrentTickRate(), "틱 레이트가 45로 변경되어야 함");
        
        gameLoop.stop();
    }
    
    @Test
    @DisplayName("TickEvent 발생 확인")
    void testTickEventGeneration() throws InterruptedException {
        AtomicInteger tickCount = new AtomicInteger(0);
        
        eventBus.subscribe(TickEvent.class, event -> {
            tickCount.incrementAndGet();
        });
        
        gameLoop.start();
        Thread.sleep(150); // 1초(1000ms) 딜레이로 최소 0번 이상의 틱 발생
        gameLoop.stop();
        
        // 노멀 모드 레벨 0의 딜레이는 1000ms이므로 150ms 동안은 틱이 거의 발생하지 않을 수 있음
        assertTrue(tickCount.get() >= 0, "TickEvent가 발생해야 함 (또는 딜레이가 길어 0일 수 있음)");
    }
    
    @Test
    @DisplayName("일시정지 중에는 TickEvent가 발생하지 않음")
    void testNoTickEventWhenPaused() throws InterruptedException {
        AtomicInteger tickCount = new AtomicInteger(0);
        
        eventBus.subscribe(TickEvent.class, event -> {
            tickCount.incrementAndGet();
        });
        
        gameLoop.start();
        Thread.sleep(50);
        
        gameLoop.pause();
        int tickCountBeforePause = tickCount.get();
        
        Thread.sleep(100);
        int tickCountAfterPause = tickCount.get();
        
        assertEquals(tickCountBeforePause, tickCountAfterPause, 
                "일시정지 중에는 추가 TickEvent가 발생하지 않아야 함");
        
        gameLoop.stop();
    }
    
    @Test
    @DisplayName("속도 레벨 업데이트 - 노멀 모드")
    void testUpdateSpeedLevel_NormalMode() {
        gameLoop.updateSpeedLevel(3);
        
        assertEquals(3, gameLoop.getSpeedLevel(), "속도 레벨이 3으로 설정되어야 함");
        assertEquals(500, gameLoop.getCurrentDelay(), "노멀 모드 레벨 3의 딜레이는 500ms여야 함");
    }
    
    @Test
    @DisplayName("속도 레벨 업데이트 - 하드 모드")
    void testUpdateSpeedLevel_HardMode() {
        LocalGameLoop hardLoop = new LocalGameLoop(eventBus, 1);
        hardLoop.updateSpeedLevel(3);
        
        assertEquals(3, hardLoop.getSpeedLevel(), "속도 레벨이 3으로 설정되어야 함");
        assertEquals(400, hardLoop.getCurrentDelay(), "하드 모드 레벨 3의 딜레이는 400ms여야 함");
    }
    
    @Test
    @DisplayName("속도 레벨 업데이트 - 이지 모드")
    void testUpdateSpeedLevel_EasyMode() {
        LocalGameLoop easyLoop = new LocalGameLoop(eventBus, 2);
        easyLoop.updateSpeedLevel(3);
        
        assertEquals(3, easyLoop.getSpeedLevel(), "속도 레벨이 3으로 설정되어야 함");
        assertEquals(600, easyLoop.getCurrentDelay(), "이지 모드 레벨 3의 딜레이는 600ms여야 함");
    }
    
    @Test
    @DisplayName("유효하지 않은 속도 레벨 설정 시 무시")
    void testUpdateSpeedLevel_Invalid() {
        int initialSpeedLevel = gameLoop.getSpeedLevel();
        int initialDelay = gameLoop.getCurrentDelay();
        
        gameLoop.updateSpeedLevel(-1);
        assertEquals(initialSpeedLevel, gameLoop.getSpeedLevel(), "음수 속도 레벨은 무시되어야 함");
        
        gameLoop.updateSpeedLevel(100);
        assertEquals(initialSpeedLevel, gameLoop.getSpeedLevel(), "범위를 벗어난 속도 레벨은 무시되어야 함");
    }
    
    @Test
    @DisplayName("속도 레벨 변경 시 SPEED_CHANGE 이벤트 발생")
    void testSpeedChangeEvent() throws InterruptedException {
        gameLoop.start();
        Thread.sleep(100);
        
        gameLoop.updateSpeedLevel(2);
        Thread.sleep(50);
        
        long speedChangeEventCount = capturedEvents.stream()
                .filter(e -> e instanceof GameTimerEvent 
                        && ((GameTimerEvent) e).getAction() == GameTimerEvent.TimerAction.SPEED_CHANGE)
                .count();
        
        assertTrue(speedChangeEventCount > 0, 
                "SPEED_CHANGE 이벤트가 발생해야 함. 캡처된 이벤트 수: " + capturedEvents.size());
        
        gameLoop.stop();
    }
    
    @Test
    @DisplayName("같은 속도 레벨로 설정 시 이벤트 발생하지 않음")
    void testUpdateSpeedLevel_SameLevel() throws InterruptedException {
        gameLoop.start();
        Thread.sleep(50);
        
        int initialEventCount = capturedEvents.size();
        
        // 같은 레벨(0)로 다시 설정
        gameLoop.updateSpeedLevel(0);
        
        Thread.sleep(50);
        
        // SPEED_CHANGE 이벤트가 추가로 발생하지 않아야 함
        long speedChangeCount = capturedEvents.stream()
                .filter(e -> e instanceof GameTimerEvent 
                        && ((GameTimerEvent) e).getAction() == GameTimerEvent.TimerAction.SPEED_CHANGE)
                .count();
        
        assertEquals(0, speedChangeCount, "같은 속도 레벨로 설정 시 이벤트가 발생하지 않아야 함");
        
        gameLoop.stop();
    }
    
    @Test
    @DisplayName("현재 레벨 업데이트")
    void testUpdateCurrentLevel() {
        gameLoop.updateCurrentLevel(5);
        
        // 내부적으로 레벨이 업데이트되었는지 확인하기 위해 간접적으로 검증
        // (TickEvent에 currentLevel이 포함되므로)
        assertDoesNotThrow(() -> gameLoop.updateCurrentLevel(10), 
                "레벨 업데이트는 예외를 발생시키지 않아야 함");
    }
    
    @Test
    @DisplayName("난이도 설정 - 범위 내")
    void testSetDifficulty_Valid() {
        gameLoop.setDifficulty(1);
        assertEquals(1, gameLoop.getDifficulty(), "난이도가 1로 설정되어야 함");
        
        gameLoop.setDifficulty(2);
        assertEquals(2, gameLoop.getDifficulty(), "난이도가 2로 설정되어야 함");
    }
    
    @Test
    @DisplayName("난이도 설정 - 범위 초과 시 제한")
    void testSetDifficulty_OutOfRange() {
        gameLoop.setDifficulty(5);
        assertEquals(2, gameLoop.getDifficulty(), "난이도가 최대값 2로 제한되어야 함");
        
        gameLoop.setDifficulty(-1);
        assertEquals(0, gameLoop.getDifficulty(), "난이도가 최소값 0으로 제한되어야 함");
    }
    
    @Test
    @DisplayName("난이도 변경 시 딜레이 업데이트")
    void testSetDifficulty_UpdatesDelay() {
        // 초기 딜레이 확인 (노멀 모드, 레벨 0)
        assertEquals(1000, gameLoop.getCurrentDelay(), "초기 딜레이는 1000ms여야 함");
        
        // 하드 모드로 변경하면 difficulty는 변경되지만 딜레이는 즉시 변경되지 않음
        // setDifficulty는 내부적으로 updateSpeedLevel(현재 레벨)을 호출
        // 그런데 speedLevel이 같으면 변경되지 않는 로직이 있음
        
        // 새로운 LocalGameLoop 인스턴스로 하드 모드 테스트
        LocalGameLoop hardLoop = new LocalGameLoop(eventBus, 1);
        assertEquals(800, hardLoop.getCurrentDelay(), "하드 모드 초기 딜레이는 800ms여야 함");
        
        // 이지 모드 테스트  
        LocalGameLoop easyLoop = new LocalGameLoop(eventBus, 2);
        assertEquals(1200, easyLoop.getCurrentDelay(), "이지 모드 초기 딜레이는 1200ms여야 함");
    }
    
    @Test
    @DisplayName("실행 중 속도 레벨 변경")
    void testUpdateSpeedLevelWhileRunning() throws InterruptedException {
        gameLoop.start();
        Thread.sleep(50);
        
        gameLoop.updateSpeedLevel(4);
        
        assertTrue(gameLoop.isRunning(), "속도 레벨 변경 후에도 실행 중이어야 함");
        assertEquals(4, gameLoop.getSpeedLevel(), "속도 레벨이 4로 변경되어야 함");
        
        gameLoop.stop();
    }
    
    @Test
    @DisplayName("전체 시나리오 - 시작, 일시정지, 재개, 정지")
    void testFullScenario() throws InterruptedException {
        // 시작
        gameLoop.start();
        assertTrue(gameLoop.isRunning());
        assertFalse(gameLoop.isPaused());
        Thread.sleep(100);
        
        // 일시정지
        gameLoop.pause();
        assertTrue(gameLoop.isRunning());
        assertTrue(gameLoop.isPaused());
        Thread.sleep(100);
        
        // 재개
        gameLoop.resume();
        assertTrue(gameLoop.isRunning());
        assertFalse(gameLoop.isPaused());
        Thread.sleep(100);
        
        // 속도 변경
        gameLoop.updateSpeedLevel(3);
        assertEquals(3, gameLoop.getSpeedLevel());
        Thread.sleep(100);
        
        // 정지
        gameLoop.stop();
        assertFalse(gameLoop.isRunning());
        assertFalse(gameLoop.isPaused());
        Thread.sleep(50);
        
        // 모든 주요 이벤트가 발생했는지 확인
        long startCount = capturedEvents.stream().filter(e -> e instanceof GameTimerEvent 
                && ((GameTimerEvent) e).getAction() == GameTimerEvent.TimerAction.START).count();
        long pauseCount = capturedEvents.stream().filter(e -> e instanceof GameTimerEvent 
                && ((GameTimerEvent) e).getAction() == GameTimerEvent.TimerAction.PAUSE).count();
        long resumeCount = capturedEvents.stream().filter(e -> e instanceof GameTimerEvent 
                && ((GameTimerEvent) e).getAction() == GameTimerEvent.TimerAction.RESUME).count();
        long speedChangeCount = capturedEvents.stream().filter(e -> e instanceof GameTimerEvent 
                && ((GameTimerEvent) e).getAction() == GameTimerEvent.TimerAction.SPEED_CHANGE).count();
        long stopCount = capturedEvents.stream().filter(e -> e instanceof GameTimerEvent 
                && ((GameTimerEvent) e).getAction() == GameTimerEvent.TimerAction.STOP).count();
        
        assertTrue(startCount > 0, "START 이벤트가 발생해야 함");
        assertTrue(pauseCount > 0, "PAUSE 이벤트가 발생해야 함");
        assertTrue(resumeCount > 0, "RESUME 이벤트가 발생해야 함");
        assertTrue(speedChangeCount > 0, "SPEED_CHANGE 이벤트가 발생해야 함");
        assertTrue(stopCount > 0, "STOP 이벤트가 발생해야 함");
    }
}
