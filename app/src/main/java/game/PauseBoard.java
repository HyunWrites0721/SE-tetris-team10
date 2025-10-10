package game;

import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PauseBoard extends JPanel{

    public PauseBoard() {
        setOpaque(true);
        setBackground(new Color(0,0,0,255));
        setLayout(new GridLayout());

        JLabel pauseLabel = new JLabel("PAUSED");
        pauseLabel.setFont(new Font("Arial", Font.BOLD, 24));
        pauseLabel.setForeground(Color.WHITE);
        pauseLabel.setHorizontalAlignment(JLabel.CENTER);
        pauseLabel.setVerticalAlignment(JLabel.CENTER);
        add(pauseLabel);


    }
    
}