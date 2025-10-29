package settings;

import com.google.gson.Gson;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScoreModel {
    private static HighScoreModel instance; // 싱글톤 인스턴스
    private List<ScoreEntry> normalScores;  // 일반 모드 점수
    private List<ScoreEntry> itemScores;    // 아이템 모드 점수
    private static final int MAX_SCORES = 10;
    
    // 내부 클래스: 점수 항목
    public static class ScoreEntry implements Comparable<ScoreEntry> {
        private String name;
        private int score;
        private String difficulty;  // "easy", "normal", "hard"
        
        public ScoreEntry(String name, int score, String difficulty) {
            this.name = name;
            this.score = score;
            this.difficulty = difficulty;
        }
        
        public String getName() {
            return name;
        }
        
        public int getScore() {
            return score;
        }
        
        public String getDifficulty() {
            return difficulty;
        }
        
        @Override
        public int compareTo(ScoreEntry other) {
            // 내림차순 정렬 (높은 점수가 먼저)
            return Integer.compare(other.score, this.score);
        }
    }
    
    // JSON 구조와 매핑
    private static class HighScoreData {
        List<ScoreEntry> normalScores;
        List<ScoreEntry> itemScores;
    }
    
    // 설정 파일의 절대 경로를 반환하는 헬퍼 메서드
    private static java.nio.file.Path getHighScoreFilePath() {
        String currentDir = System.getProperty("user.dir");
        File appDir = new File(currentDir);
        if (appDir.getName().equals("app")) {
            return java.nio.file.Paths.get(currentDir, "src/main/java/settings/data/HighScore.json");
        } else {
            return java.nio.file.Paths.get(currentDir, "app/src/main/java/settings/data/HighScore.json");
        }
    }
    
    // Default 파일의 절대 경로를 반환하는 헬퍼 메서드
    private static java.nio.file.Path getDefaultFilePath() {
        String currentDir = System.getProperty("user.dir");
        File appDir = new File(currentDir);
        if (appDir.getName().equals("app")) {
            return java.nio.file.Paths.get(currentDir, "src/main/java/settings/data/HighScoreDefault.json");
        } else {
            return java.nio.file.Paths.get(currentDir, "app/src/main/java/settings/data/HighScoreDefault.json");
        }
    }
    
    // 싱글톤 인스턴스 반환
    public static HighScoreModel getInstance() {
        if (instance == null) {
            instance = new HighScoreModel();
        }
        return instance;
    }
    
    // private 생성자 (싱글톤 패턴)
    private HighScoreModel() {
        loadScores();
    }
    
    // JSON 파일에서 점수 불러오기
    private void loadScores() {
        try {
            java.nio.file.Path path = getHighScoreFilePath();
            String json = java.nio.file.Files.readString(path);
            Gson gson = new Gson();
            HighScoreData data = gson.fromJson(json, HighScoreData.class);
            
            if (data != null) {
                this.normalScores = data.normalScores != null ? data.normalScores : new ArrayList<>();
                this.itemScores = data.itemScores != null ? data.itemScores : new ArrayList<>();
            } else {
                this.normalScores = new ArrayList<>();
                this.itemScores = new ArrayList<>();
            }
        } catch (Exception e) {
            this.normalScores = new ArrayList<>();
            this.itemScores = new ArrayList<>();
        }
    }
    
    // JSON 파일에 점수 저장
    private void saveScores() {
        try {
            java.nio.file.Path path = getHighScoreFilePath();
            HighScoreData data = new HighScoreData();
            data.normalScores = this.normalScores;
            data.itemScores = this.itemScores;
            
            Gson gson = new Gson();
            String json = gson.toJson(data);
            java.nio.file.Files.writeString(path, json);
        } catch (Exception e) {
            System.err.println("점수 저장 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 새 점수 추가 (Top 10에 들어가는지 확인)
    public boolean addScore(String name, int score, String difficulty, boolean isItemMode) {
        // 점수가 0이면 추가하지 않음
        if (score <= 0) {
            return false;
        }
        
        ScoreEntry newEntry = new ScoreEntry(name, score, difficulty);
        List<ScoreEntry> targetList = isItemMode ? itemScores : normalScores;
        targetList.add(newEntry);
        
        // 정렬 (내림차순)
        Collections.sort(targetList);
        
        // Top 10만 유지
        if (targetList.size() > MAX_SCORES) {
            if (isItemMode) {
                itemScores = new ArrayList<>(targetList.subList(0, MAX_SCORES));
            } else {
                normalScores = new ArrayList<>(targetList.subList(0, MAX_SCORES));
            }
        }
        
        // 저장
        saveScores();
        
        return true;
    }
    
    // Top 10 점수 목록 반환
    public List<ScoreEntry> getTopScores(boolean isItemMode) {
        return new ArrayList<>(isItemMode ? itemScores : normalScores);
    }
    
    // 최고 점수 반환 (1등)
    public int getHighScore(boolean isItemMode) {
        List<ScoreEntry> targetList = isItemMode ? itemScores : normalScores;
        if (targetList.isEmpty()) {
            return 0;
        }
        return targetList.get(0).getScore();
    }
    
    // 특정 순위의 점수 반환
    public ScoreEntry getScoreByRank(int rank, boolean isItemMode) {
        List<ScoreEntry> targetList = isItemMode ? itemScores : normalScores;
        if (rank < 1 || rank > targetList.size()) {
            return null;
        }
        return targetList.get(rank - 1);
    }
    
    // Top 10에 들어갈 수 있는지 확인
    public boolean isTopScore(int score, boolean isItemMode) {
        if (score <= 0) {
            return false;
        }
        List<ScoreEntry> targetList = isItemMode ? itemScores : normalScores;
        if (targetList.size() < MAX_SCORES) {
            return true;
        }
        return score > targetList.get(MAX_SCORES - 1).getScore();
    }
    
    // 점수 초기화 - Default 파일을 복사
    public void resetScores() {
        try {
            // Default 파일의 내용을 읽어서 HighScore.json에 복사
            java.nio.file.Path defaultPath = getDefaultFilePath();
            java.nio.file.Path highScorePath = getHighScoreFilePath();
            
            String defaultContent = java.nio.file.Files.readString(defaultPath);
            java.nio.file.Files.writeString(highScorePath, defaultContent);
            
            // 메모리의 점수 리스트도 초기화
            normalScores = new ArrayList<>();
            itemScores = new ArrayList<>();
            
            System.out.println("점수 초기화 완료: Default 파일로부터 복사됨");
        } catch (Exception e) {
            System.err.println("점수 초기화 실패: " + e.getMessage());
            e.printStackTrace();
            
            // 파일 복사 실패 시 직접 초기화
            normalScores = new ArrayList<>();
            itemScores = new ArrayList<>();
            saveScores();
        }
    }
}
