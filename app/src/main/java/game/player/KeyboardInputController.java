package game.player;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 키보드 입력을 GameCommand로 변환하여 Player에 전달하는 InputController 구현체
 */
public class KeyboardInputController implements InputController {
    private final Component target; // KeyListener를 등록할 컴포넌트
    private Player boundPlayer;
    private final KeyAdapter adapter;

    public KeyboardInputController(Component target) {
        this.target = target;
        this.adapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKey(e);
            }
        };
    }

    @Override
    public void bind(Player player) {
        this.boundPlayer = player;
        if (target != null) target.addKeyListener(adapter);
    }

    @Override
    public void unbind() {
        if (target != null) target.removeKeyListener(adapter);
        this.boundPlayer = null;
    }

    private void handleKey(KeyEvent e) {
        if (boundPlayer == null) return;

        GameCommand.Type t = null;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                t = GameCommand.Type.LEFT; break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                t = GameCommand.Type.RIGHT; break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                t = GameCommand.Type.ROTATE; break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                t = GameCommand.Type.SOFT_DROP; break;
            case KeyEvent.VK_SPACE:
                t = GameCommand.Type.HARD_DROP; break;
            default:
                break;
        }

        if (t != null) {
            boundPlayer.enqueueCommand(new GameCommand(t));
        }
    }
}
