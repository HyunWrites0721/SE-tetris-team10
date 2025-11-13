package game.core;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * GameState 객체를 변경하지 않고 JSON 직렬화/역직렬화를 제공하는 유틸리티 클래스입니다.
 * GameState 내부의 Block 참조는 직렬화 대상에서 제외되며, 역직렬화 시 null로 세팅됩니다.
 */
public final class GameStateSerializer {
    private static final Gson GSON = new GsonBuilder().create();

    private GameStateSerializer() { /* util */ }

    public static String toJson(GameState state) {
        SerializableState s = new SerializableState();
        s.boardArray = state.getBoardArray();
        s.colorBoard = state.getColorBoard();
        s.score = state.getScore();
        s.totalLinesCleared = state.getTotalLinesCleared();
        s.currentLevel = state.getCurrentLevel();
        s.lineClearCount = state.getLineClearCount();
        s.itemGenerateCount = state.getItemGenerateCount();
        s.blocksSpawned = state.getBlocksSpawned();
        s.itemMode = state.isItemMode();
        s.isAnimating = state.isAnimating();
        s.lastLineClearScore = state.getLastLineClearScore();
        s.lineClearAnimating = state.isLineClearAnimating();
        s.flashBlack = state.isFlashBlack();
        s.flashingRows = state.getFlashingRows();
        s.allClearAnimating = state.isAllClearAnimating();
        s.allClearFlashBlack = state.isAllClearFlashBlack();
        s.boxClearAnimating = state.isBoxClearAnimating();
        s.boxFlashBlack = state.isBoxFlashBlack();
        s.boxFlashCenters = state.getBoxFlashCenters();
        s.weightAnimating = state.isWeightAnimating();
        return GSON.toJson(s);
    }

    public static GameState fromJson(String json) {
        SerializableState s = GSON.fromJson(json, SerializableState.class);
        // Blocks are not serialized; pass null and rely on game logic to set them if needed
        GameState.Builder b = new GameState.Builder(s.boardArray, s.colorBoard, null, null, s.itemMode)
                .score(s.score)
                .totalLinesCleared(s.totalLinesCleared)
                .currentLevel(s.currentLevel)
                .lineClearCount(s.lineClearCount)
                .itemGenerateCount(s.itemGenerateCount)
                .blocksSpawned(s.blocksSpawned)
                .isAnimating(s.isAnimating)
                .lastLineClearScore(s.lastLineClearScore)
                .lineClearAnimating(s.lineClearAnimating)
                .flashBlack(s.flashBlack)
                .flashingRows(s.flashingRows)
                .allClearAnimating(s.allClearAnimating)
                .allClearFlashBlack(s.allClearFlashBlack)
                .boxClearAnimating(s.boxClearAnimating)
                .boxFlashBlack(s.boxFlashBlack)
                .boxFlashCenters(s.boxFlashCenters)
                .weightAnimating(s.weightAnimating);
        return b.build();
    }

    private static class SerializableState {
        int[][] boardArray;
        int[][] colorBoard;
        int score;
        int totalLinesCleared;
        int currentLevel;
        int lineClearCount;
        int itemGenerateCount;
        int blocksSpawned;
        boolean itemMode;
        boolean isAnimating;
        int lastLineClearScore;
        boolean lineClearAnimating;
        boolean flashBlack;
        List<Integer> flashingRows = new ArrayList<>();
        boolean allClearAnimating;
        boolean allClearFlashBlack;
        boolean boxClearAnimating;
        boolean boxFlashBlack;
        List<int[]> boxFlashCenters = new ArrayList<>();
        boolean weightAnimating;
    }
}
