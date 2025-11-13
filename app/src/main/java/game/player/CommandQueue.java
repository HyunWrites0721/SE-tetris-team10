package game.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 스레드 안전한 커맨드 큐. 입력은 EDT(또는 다른 스레드)에서 enqueue 되고,
 * 게임 틱에서 drainForTick()으로 배치 처리됩니다.
 */
public class CommandQueue {
    private final Queue<GameCommand> queue = new ConcurrentLinkedQueue<>();

    public void enqueue(GameCommand cmd) {
        if (cmd == null) return;
        queue.add(cmd);
    }

    /** 큐에 쌓인 모든 명령을 가져오고 큐는 비웁니다. */
    public List<GameCommand> drainForTick() {
        List<GameCommand> out = new ArrayList<>();
        GameCommand c;
        while ((c = queue.poll()) != null) {
            out.add(c);
        }
        return out;
    }

    public int size() { return queue.size(); }

    /** 직렬화된 JSON 문자열 배치(네트워크 전송) */
    public String serializeBatch() {
        List<GameCommand> list = drainForTick();
        if (list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (GameCommand g : list) {
            if (!first) sb.append(',');
            sb.append(CommandSerializer.toJson(g));
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }
}
