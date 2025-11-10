package game.player;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;

import game.core.GameState;
import game.player.engine.GameEngine;

/**
 * Player 객체 스텁: 한 플레이어의 상태/입력/뷰/엔진을 캡슐화
 * 이 클래스는 점진적 마이그레이션을 위해 먼저 스텁으로 추가한 뒤 기능을 확장하세요.
 */
public class Player {
    private final PlayerId id;
    private final GameEngine engine;
    private volatile GameState state;
    private final InputController input;
    private final PlayerView view;

    // 입력 명령 큐 (EDT에서 enqueue, tick에서 처리)
    private final Queue<GameCommand> commandQueue = new ConcurrentLinkedQueue<>();

    private volatile boolean running = false;

    public Player(PlayerId id, GameEngine engine, GameState initialState, InputController input, PlayerView view) {
        this.id = id;
        this.engine = engine;
        this.state = initialState;
        this.input = input;
        this.view = view;
        // 뷰와 입력은 생성 후 start()에서 바인딩/초기화 권장
    }

    public PlayerId getId() { return id; }

    /** start에서 실제 바인딩을 수행한다 */
    public void start() {
        if (running) return;
        running = true;
        if (input != null) input.bind(this);
        if (view != null) view.setPlayer(this);
        // 초기 렌더
        requestRender();
    }

    public void pause() { running = false; }
    public void resume() { if (!running) { running = true; } }

    public void stop() {
        running = false;
        if (input != null) input.unbind();
    }

    /** 외부에서 명령을 즉시 큐에 넣음 (주로 EDT에서 호출) */
    public void enqueueCommand(GameCommand cmd) {
        if (cmd == null) return;
        commandQueue.add(cmd);
    }

    /** applyCommand는 즉시 명령을 적용하고 렌더를 트리거한다 */
    public void applyCommand(GameCommand cmd) {
        // 기본: 즉시 엔진에 위임
        synchronized(this) {
            this.state = engine.command(state, cmd);
        }
        requestRender();
    }

    /** tick: 명령 큐를 비우고 엔진 step을 호출 */
    public GameState tick(long dt) {
        if (!running) return state;

        // 먼저 큐에 쌓인 명령을 적용
        GameCommand cmd;
        while ((cmd = commandQueue.poll()) != null) {
            synchronized(this) {
                state = engine.command(state, cmd);
            }
        }

        // 시간 경과에 따른 상태 업데이트
        synchronized(this) {
            state = engine.step(state, dt);
        }

        requestRender();
        return state;
    }

    public GameState getState() { return state; }
    public void setState(GameState s) { this.state = s; requestRender(); }

    private void requestRender() {
        if (view == null) return;
        final GameState snapshot = state; // capture
        // 뷰의 requestRender는 스레드 안전해야 하지만, 안전을 위해 EDT로 위임
        SwingUtilities.invokeLater(() -> view.requestRender(snapshot));
    }

    // Debug
    public String dumpState() { return state == null ? "null" : state.toString(); }
}
