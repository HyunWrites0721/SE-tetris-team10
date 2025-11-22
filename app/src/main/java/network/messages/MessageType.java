package network.messages;

/**
 * 네트워크 메시지 타입 정의
 */
public enum MessageType {
    GAME_EVENT,      // 게임 이벤트 (블록 이동, 줄 삭제 등)
    CONNECTION,      // 연결 관련
    GAME_CONTROL,    // 게임 시작/종료
    HEARTBEAT,       // 연결 유지 확인
    ATTACK           // 공격 줄
}
