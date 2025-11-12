package game.loop;

import game.events.EventBus;
import game.events.TickEvent;
import game.events.GameTimerEvent;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 싱글플레이어용 Fixed Timestep 게임 루프 구현
 * Swing Timer를 래핑하여 이벤트 기반으로 게임 루프를 관리
 */
public class LocalGameLoop implements GameLoop {
    
    private static final int DEFAULT_TICK_RATE = 60; // 기본 60 FPS
    
    private final EventBus eventBus;
    private Timer timer;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private int tickRate = DEFAULT_TICK_RATE;
    private long lastTickTime = 0;
    private int currentLevel = 1;
    private int speedLevel = 0;
    
    // 속도 레벨별 딜레이 배열 (난이도별)
    private static final int[][] SPEED_DELAYS = {
        {1000, 800, 650, 500, 350, 200, 100},     // 노멀 모드
        {800, 640, 520, 400, 280, 160, 80},       // 하드 모드
        {1200, 960, 780, 600, 420, 250, 120}      // 이지 모드
    };
    
    private int difficulty = 0; // 기본 노멀 모드
    
    public LocalGameLoop(EventBus eventBus) {
        this.eventBus = eventBus;
        initializeTimer();
    }
    
    public LocalGameLoop(EventBus eventBus, int difficulty) {
        this.eventBus = eventBus;
        this.difficulty = Math.max(0, Math.min(2, difficulty)); // 0-2 범위로 제한
        initializeTimer();
    }
    
    private void initializeTimer() {
        int initialDelay = SPEED_DELAYS[difficulty][speedLevel];
        timer = new Timer(initialDelay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isRunning || isPaused) {
                    return;
                }
                
                long currentTime = System.currentTimeMillis();
                long deltaTime = currentTime - lastTickTime;
                lastTickTime = currentTime;
                
                // 틱 이벤트 발생
                TickEvent tickEvent = new TickEvent(currentLevel, speedLevel, deltaTime);
                eventBus.publish(tickEvent);
            }
        });
    }
    
    @Override
    public void start() {
        if (isRunning) {
            System.out.println("LocalGameLoop already running - ignored start()");
            return;
        }
        
        isRunning = true;
        isPaused = false;
        lastTickTime = System.currentTimeMillis();
        timer.start();
        
        // 시작 이벤트 발생
        eventBus.publish(new GameTimerEvent(GameTimerEvent.TimerAction.START));
        System.out.println("LocalGameLoop started with delay: " + timer.getDelay() + "ms");
    }
    
    @Override
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        isPaused = false;
        if (timer.isRunning()) {
            timer.stop();
        }
        
        // 정지 이벤트 발생
        eventBus.publish(new GameTimerEvent(GameTimerEvent.TimerAction.STOP));
        System.out.println("LocalGameLoop stopped");
    }
    
    @Override
    public void pause() {
        if (!isRunning || isPaused) {
            return;
        }
        
        isPaused = true;
        
        // 일시정지 이벤트 발생
        eventBus.publish(new GameTimerEvent(GameTimerEvent.TimerAction.PAUSE));
        System.out.println("LocalGameLoop paused");
    }
    
    @Override
    public void resume() {
        if (!isRunning || !isPaused) {
            return;
        }
        
        isPaused = false;
        lastTickTime = System.currentTimeMillis(); // 시간 재설정
        
        // 재개 이벤트 발생
        eventBus.publish(new GameTimerEvent(GameTimerEvent.TimerAction.RESUME));
        System.out.println("LocalGameLoop resumed");
    }
    
    @Override
    public void setTickRate(int ticksPerSecond) {
        if (ticksPerSecond <= 0) {
            throw new IllegalArgumentException("Tick rate must be positive");
        }
        
        this.tickRate = ticksPerSecond;
        int newDelay = 1000 / ticksPerSecond;
        
        boolean wasRunning = timer.isRunning();
        if (wasRunning) {
            timer.stop();
        }
        
        timer.setDelay(newDelay);
        
        if (wasRunning && isRunning && !isPaused) {
            timer.start();
        }
        
        System.out.println("LocalGameLoop tick rate changed: " + ticksPerSecond + " TPS (delay: " + newDelay + "ms)");
    }
    
    /**
     * 게임 속도 레벨 업데이트 (테트리스 게임용)
     * @param speedLevel 새로운 속도 레벨
     */
    public void updateSpeedLevel(int speedLevel) {
        if (speedLevel < 0 || speedLevel >= SPEED_DELAYS[difficulty].length) {
            return;
        }
        
        if (this.speedLevel != speedLevel) {
            this.speedLevel = speedLevel;
            int newDelay = SPEED_DELAYS[difficulty][speedLevel];
            
            boolean wasRunning = timer.isRunning();
            if (wasRunning) {
                timer.stop();
            }
            
            timer.setDelay(newDelay);
            
            if (wasRunning && isRunning && !isPaused) {
                timer.start();
            }
            
            // 속도 변경 이벤트 발생
            eventBus.publish(new GameTimerEvent(GameTimerEvent.TimerAction.SPEED_CHANGE, newDelay, speedLevel));
            System.out.println("Speed level updated: " + speedLevel + " (delay: " + newDelay + "ms)");
        }
    }
    
    /**
     * 현재 레벨 업데이트
     * @param currentLevel 새로운 레벨
     */
    public void updateCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }
    
    @Override
    public boolean isRunning() {
        return isRunning && timer.isRunning();
    }
    
    @Override
    public boolean isPaused() {
        return isPaused;
    }
    
    @Override
    public int getCurrentTickRate() {
        return tickRate;
    }
    
    /**
     * 현재 속도 레벨 반환
     */
    public int getSpeedLevel() {
        return speedLevel;
    }
    
    /**
     * 현재 딜레이 반환
     */
    public int getCurrentDelay() {
        return timer.getDelay();
    }
    
    /**
     * 난이도 설정
     */
    public void setDifficulty(int difficulty) {
        this.difficulty = Math.max(0, Math.min(2, difficulty));
        // 현재 속도 레벨로 딜레이 업데이트
        updateSpeedLevel(this.speedLevel);
    }
    
    /**
     * 현재 난이도 반환
     */
    public int getDifficulty() {
        return difficulty;
    }
}