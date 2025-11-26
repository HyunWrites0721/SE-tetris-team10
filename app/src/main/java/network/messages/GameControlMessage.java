package network.messages;

import versus.VersusMode;

/**
 * 게임 제어용 네트워크 메시지
 * 서버와 클라이언트 간의 게임 시작/정지/준비/모드선택 등을 전달합니다.
 */
public class GameControlMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;

    public enum ControlType {
        START_GAME,
        PAUSE_GAME,
        RESUME_GAME,
        END_GAME,
        READY,
        MODE_SELECT
    }

    private final ControlType controlType;
    // MODE_SELECT 시 사용되는 게임 모드 (nullable)
    private final VersusMode mode;
    // READY 또는 제어를 요청한 플레이어 ID (nullable)
    private final Integer playerId;
    // 추가 정보(디버깅/메시지 설명 등, nullable)
    private final String info;

    public GameControlMessage(ControlType controlType) {
        this(controlType, null, null, null);
    }

    public GameControlMessage(ControlType controlType, VersusMode mode) {
        this(controlType, mode, null, null);
    }

    public GameControlMessage(ControlType controlType, int playerId) {
        this(controlType, null, Integer.valueOf(playerId), null);
    }

    public GameControlMessage(ControlType controlType, VersusMode mode, Integer playerId, String info) {
        super(MessageType.GAME_CONTROL);
        this.controlType = controlType;
        this.mode = mode;
        this.playerId = playerId;
        this.info = info;
    }

    public ControlType getControlType() {
        return controlType;
    }

    public VersusMode getMode() {
        return mode;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return "GameControlMessage{" +
                "type=" + controlType +
                ", mode=" + mode +
                ", playerId=" + playerId +
                ", info='" + info + '\'' +
                ", id=" + getMessageId() +
                '}';
    }
}
