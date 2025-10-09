package game;

import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GameOverBoard extends JPanel{

    public GameOverBoard() {
        setOpaque(true);
        setBackground(new Color(0,0,0,255));
        setLayout(new GridLayout());

        JLabel gameOverLabel = new JLabel("GAME OVER");
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gameOverLabel.setForeground(Color.WHITE);
        gameOverLabel.setHorizontalAlignment(JLabel.CENTER);
        gameOverLabel.setVerticalAlignment(JLabel.CENTER);
        add(gameOverLabel);


    }
    
}