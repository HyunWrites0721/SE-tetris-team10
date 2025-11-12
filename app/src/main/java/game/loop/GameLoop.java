package game.loop;

/**
 * 게임 루프 추상화 인터페이스
 * 다양한 게임 모드(싱글플레이어, P2P, 리플레이)에서 공통으로 사용할 수 있는 게임 루프 정의
 */
public interface GameLoop {
    
    /**
     * 게임 루프 시작
     */
    void start();
    
    /**
     * 게임 루프 정지
     */
    void stop();
    
    /**
     * 게임 루프 일시정지
     */
    void pause();
    
    /**
     * 게임 루프 재개
     */
    void resume();
    
    /**
     * 틱 레이트 설정 (초당 틱 수)
     * @param ticksPerSecond 초당 틱 수
     */
    void setTickRate(int ticksPerSecond);
    
    /**
     * 현재 실행 상태 확인
     * @return 실행 중이면 true, 아니면 false
     */
    boolean isRunning();
    
    /**
     * 현재 일시정지 상태 확인
     * @return 일시정지 중이면 true, 아니면 false
     */
    boolean isPaused();
    
    /**
     * 현재 틱 레이트 반환
     * @return 현재 설정된 틱 레이트
     */
    int getCurrentTickRate();
}