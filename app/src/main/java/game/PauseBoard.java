package game;

import java.awt.*;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PauseBoard extends JPanel{

    private FrameBoard frameBoard;

    public PauseBoard(FrameBoard frameBoard) {
        this.frameBoard = frameBoard;
        setOpaque(true);
        setLayout(new BorderLayout());

        // Top margin panel for PAUSED label
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(40, 0, 0, 0)); // top margin

        JLabel pauseLabel = new JLabel("PAUSED");
        pauseLabel.setFont(new Font("Arial", Font.BOLD, 32));
        pauseLabel.setForeground(Color.black);
        pauseLabel.setHorizontalAlignment(JLabel.CENTER);
        pauseLabel.setVerticalAlignment(JLabel.TOP);
        topPanel.add(pauseLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        Color buttonColor = new Color(245, 245, 245);

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 40, 10, 40)); // reduce button layout size

        javax.swing.JButton resumeButton = new javax.swing.JButton("resume");
        resumeButton.setPreferredSize(new Dimension(200,60));
        resumeButton.setMaximumSize(new Dimension(200, 60));
        resumeButton.setFont(new Font("Arial", Font.BOLD, 12)); 
        resumeButton.setBackground(buttonColor); 
        resumeButton.setForeground(Color.BLACK); 
        resumeButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
        resumeButton.setFocusPainted(false); 
        resumeButton.setOpaque(true);
        resumeButton.addActionListener(e -> {
            // PauseBoard만 숨기고 게임 화면으로 복귀
            PauseBoard.this.setVisible(false);
            setOpaque(false);
            frameBoard.isPaused = false;
            frameBoard.paused();

        });

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
                this.setVisible(false);
                frameBoard.isPaused = false;
                frameBoard.gameInit();
            }
        });


        javax.swing.JButton mainButton = new javax.swing.JButton("Main Menu");
        mainButton.setPreferredSize(new Dimension(200,60));
        mainButton.setMaximumSize(new Dimension(200, 60));
        mainButton.setFont(new Font("Arial", Font.BOLD, 12));
        mainButton.setBackground(buttonColor); 
        mainButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
        mainButton.setForeground(Color.BLACK); 
        mainButton.setFocusPainted(false); 
        mainButton.setOpaque(true);

                mainButton.addActionListener(e -> {
            // Main Menu 버튼 클릭 시 동작





        });

        javax.swing.JButton quitButton = new javax.swing.JButton("Quit");
        quitButton.setPreferredSize(new Dimension(200,60));
        quitButton.setMaximumSize(new Dimension(200, 60));
        quitButton.setFont(new Font("Arial", Font.BOLD, 12));
        quitButton.setBackground(buttonColor); 
        quitButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
        quitButton.setForeground(Color.BLACK); 
        quitButton.setFocusPainted(false); 
        quitButton.setOpaque(true);

        quitButton.addActionListener(e -> {
            // Quit 버튼 클릭 시 동작
            System.exit(0);
        });



        buttonPanel.add(resumeButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(mainButton);
        buttonPanel.add(quitButton);
        add(buttonPanel, BorderLayout.CENTER);
    }
    
}