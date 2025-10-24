package game;

import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BoxLayout;

public class GameOverBoard extends JPanel{

    private final FrameBoard frameBoard;

    public GameOverBoard(FrameBoard frameBoard) {
        this.frameBoard = frameBoard;
  

        setOpaque(true);
        setBackground(new Color(0,0,0,255));
        setLayout(new GridLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(40, 0, 0, 0)); // top margin

        JLabel gameOverLabel = new JLabel("GAME OVER");
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 32));
        gameOverLabel.setForeground(Color.WHITE);
        gameOverLabel.setHorizontalAlignment(JLabel.CENTER);
        gameOverLabel.setVerticalAlignment(JLabel.TOP);
        topPanel.add(gameOverLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        Color buttonColor = new Color(245, 245, 245);

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 40, 10, 40)); // reduce button layout size

        javax.swing.JButton restartButton = new javax.swing.JButton("restart");
        restartButton.setPreferredSize(new Dimension(200, 60));
        restartButton.setMaximumSize(new Dimension(200, 60));
        restartButton.setFont(new Font("Arial", Font.BOLD, 12));
        restartButton.setBackground(buttonColor);
        restartButton.setForeground(Color.BLACK);
        restartButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
        restartButton.setFocusPainted(false);
        restartButton.setOpaque(true);

        restartButton.addActionListener(e -> {
            if (frameBoard != null) {
                frameBoard.gameInit();
                this.setVisible(false);
            }
        });

        javax.swing.JButton mainButton = new javax.swing.JButton("Main Menu");
        mainButton.setPreferredSize(new Dimension(200, 60));
        mainButton.setMaximumSize(new Dimension(200, 60));
        mainButton.setFont(new Font("Arial", Font.BOLD, 12));
        mainButton.setBackground(buttonColor);
        mainButton.setForeground(Color.BLACK);
        mainButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
        mainButton.setFocusPainted(false);
        mainButton.setOpaque(true);

        mainButton.addActionListener(e -> {
            // Main Menu 버튼 클릭 시 동작
        });

        javax.swing.JButton quitButton = new javax.swing.JButton("Quit");
        quitButton.setPreferredSize(new Dimension(200, 60));
        quitButton.setMaximumSize(new Dimension(200, 60));
        quitButton.setFont(new Font("Arial", Font.BOLD, 12));
        quitButton.setBackground(buttonColor);
        quitButton.setForeground(Color.BLACK);
        quitButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
        quitButton.setFocusPainted(false);
        quitButton.setOpaque(true);

        quitButton.addActionListener(e -> {
            // Quit 버튼 클릭 시 동작
            System.exit(0);
        });

        buttonPanel.add(restartButton);
        buttonPanel.add(mainButton);
        buttonPanel.add(quitButton);
        add(buttonPanel, BorderLayout.CENTER);
    }

}

