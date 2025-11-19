package versus;

/**
 * 대전 모드 공격 시스템
 * - 상대방에게 줄 추가 (최대 10줄)
 * - 줄 삭제 시 상대방에게 공격
 */
public class VersusAttackManager {
    private static final int MAX_PENDING_LINES = 10;  // 최대 대기 줄 수
    
    private int pendingLines = 0;  // 상대방으로부터 받은 대기 중인 공격 줄
    
    /**
     * 공격 받기 (상대방이 줄을 삭제했을 때)
     * @param lines 추가할 줄 수
     * @return 실제로 추가된 줄 수
     */
    public int receiveAttack(int lines) {
        int actualLines = lines;
        
        // 이미 10줄이 차있으면 무시
        if (pendingLines >= MAX_PENDING_LINES) {
            return 0;
        }
        
        // 10줄을 넘으면 잘라냄
        if (pendingLines + lines > MAX_PENDING_LINES) {
            actualLines = MAX_PENDING_LINES - pendingLines;
        }
        
        pendingLines += actualLines;
        return actualLines;
    }
    
    /**
     * 공격 줄 적용 (블록이 바닥에 닿았을 때)
     * @return 추가할 줄 수
     */
    public int applyPendingLines() {
        int lines = pendingLines;
        pendingLines = 0;
        return lines;
    }
    
    /**
     * 대기 중인 공격 줄 수
     */
    public int getPendingLines() {
        return pendingLines;
    }
    
    /**
     * 줄 삭제로 인한 공격 계산
     * @param linesCleared 삭제한 줄 수
     * @return 상대방에게 보낼 공격 줄 수 (2줄 이상일 때만 공격 가능)
     */
    public static int calculateAttackLines(int linesCleared) {
        // 2줄 이상 삭제해야 공격 가능
        if (linesCleared < 2) {
            return 0;
        }
        // 삭제한 줄 수와 동일하게 공격
        return linesCleared;
    }
}
