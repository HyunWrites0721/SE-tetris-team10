package game.player.engine;

import game.core.GameState;
import game.player.GameCommand;

/**
 * 플레이어별 게임 로직 엔진 인터페이스 (간단한 버전)
 */
public interface GameEngine {
    /** 시간 경과에 따른 상태 업데이트 (dt: ms) */
    GameState step(GameState state, long dt);

    /** 명령(Command)에 따른 상태 변경 */
    GameState command(GameState state, GameCommand cmd);
}
