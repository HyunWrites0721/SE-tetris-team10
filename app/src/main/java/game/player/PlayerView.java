package game.player;

import javax.swing.JComponent;

import game.core.GameState;

/**
 * 플레이어 단위의 뷰(렌더러) 인터페이스
 */
public interface PlayerView {
    /** 뷰에 플레이어를 연결한다. */
    void setPlayer(Player player);

    /** 스레드 안전하게 렌더 요청을 수행한다(내부에서 EDT로 위임해야 함). */
    void requestRender(GameState state);

    /** Swing 컴포넌트를 반환해 FrameBoard 등에 배치할 수 있게 한다. */
    JComponent getComponent();
}
