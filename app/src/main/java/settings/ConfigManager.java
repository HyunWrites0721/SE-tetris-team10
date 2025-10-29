package settings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Manages configuration files, storing them in user's home directory
 * to ensure they persist even when running from packaged JAR/EXE.
 */
public class ConfigManager {
    private static final String APP_DIR_NAME = ".tetris";
    private static final String SETTINGS_FILE = "SettingSave.json";
    private static final String HIGHSCORE_FILE = "HighScore.json";
    private static final String DEFAULT_SETTINGS_FILE = "DefaultSetting.json";
    private static final String DEFAULT_HIGHSCORE_FILE = "HighScoreDefault.json";
    
    private static Path configDir;
    
    static {
        // Initialize config directory in user home
        String userHome = System.getProperty("user.home");
        configDir = Paths.get(userHome, APP_DIR_NAME);
        
        try {
            // Create directory if it doesn't exist
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            
            // Copy default files from resources if they don't exist
            initializeFile(SETTINGS_FILE);
            initializeFile(HIGHSCORE_FILE);
            initializeFile(DEFAULT_SETTINGS_FILE);
            initializeFile(DEFAULT_HIGHSCORE_FILE);
        } catch (IOException e) {
            System.err.println("Failed to initialize config directory: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize a config file by copying from resources if it doesn't exist
     */
    private static void initializeFile(String filename) throws IOException {
        Path targetFile = configDir.resolve(filename);
        
        // If file doesn't exist, try to copy from resources
        if (!Files.exists(targetFile)) {
            // Try to load from resources (works when running from JAR)
            InputStream resourceStream = ConfigManager.class.getResourceAsStream("/data/" + filename);
            
            if (resourceStream != null) {
                // Copy from resources
                Files.copy(resourceStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
                resourceStream.close();
            } else {
                // Try to load from development path
                Path devFile = Paths.get("app/src/main/java/settings/data", filename);
                if (Files.exists(devFile)) {
                    Files.copy(devFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    // Create empty file as last resort
                    Files.createFile(targetFile);
                }
            }
        }
    }
    
    /**
     * Get the path to the settings file
     */
    public static String getSettingsPath() {
        return configDir.resolve(SETTINGS_FILE).toString();
    }
    
    /**
     * Get the path to the high score file
     */
    public static String getHighScorePath() {
        return configDir.resolve(HIGHSCORE_FILE).toString();
    }
    
    /**
     * Get the path to the default settings file
     */
    public static String getDefaultSettingsPath() {
        return configDir.resolve(DEFAULT_SETTINGS_FILE).toString();
    }
    
    /**
     * Get the path to the default high score file
     */
    public static String getDefaultHighScorePath() {
        return configDir.resolve(DEFAULT_HIGHSCORE_FILE).toString();
    }
    
    /**
     * Get the config directory path
     */
    public static String getConfigDir() {
        return configDir.toString();
    }
}
