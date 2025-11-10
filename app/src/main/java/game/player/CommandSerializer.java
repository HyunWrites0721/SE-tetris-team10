package game.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 단순한 GameCommand 직렬화/역직렬화 유틸리티
 * 포맷은 JSON이며 버전 필드를 포함할 수 있음
 */
public final class CommandSerializer {
    private static final Gson GSON = new GsonBuilder().create();

    private CommandSerializer() {}

    public static String toJson(GameCommand cmd) {
        return GSON.toJson(new Envelope(1, cmd));
    }

    public static GameCommand fromJson(String json) {
        Envelope e = GSON.fromJson(json, Envelope.class);
        return e == null ? null : e.command;
    }

    private static class Envelope {
        int version;
        GameCommand command;

        Envelope() {}
        Envelope(int v, GameCommand c) { this.version = v; this.command = c; }
    }
}
