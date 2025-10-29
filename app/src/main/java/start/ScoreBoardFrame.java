package start;

import javax.swing.*;
import settings.HighScoreModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ScoreBoardFrame extends JFrame {
    private double screenRatio = StartFrame.screenRatio;
    private HighScoreModel highScoreModel;
    private JTabbedPane tabbedPane;  // 탭 패널을 필드로 선언

    public ScoreBoardFrame() {
        setTitle(StartFrame.titleName);
        setSize((int)(600*screenRatio), (int)(600*screenRatio));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        highScoreModel = HighScoreModel.getInstance();

        JLabel titleLabel = new JLabel("HIGH SCORE", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int)(36*screenRatio)));
        titleLabel.setBorder(BorderFactory.createEmptyBorder((int)(20*screenRatio), 0, (int)(20*screenRatio), 0));
        add(titleLabel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int)(16*screenRatio)));
        tabbedPane.setFocusable(false);  // 탭 패널 자체의 포커스 비활성화
        
        JPanel normalPanel = createScorePanel(false);
        tabbedPane.addTab("일반 모드", normalPanel);
        
        JPanel itemPanel = createScorePanel(true);
        tabbedPane.addTab("아이템 모드", itemPanel);
        
        add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder((int)(20*screenRatio), 0, (int)(20*screenRatio), 0));
        
        JButton backButton = new JButton("돌아가기 (ENTER)");
        backButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int)(20*screenRatio)));
        backButton.setPreferredSize(new Dimension((int)(200*screenRatio), (int)(50*screenRatio)));
        backButton.setFocusable(false);  // 버튼 포커스 비활성화
        
        backButton.addActionListener(e -> {
            new StartFrame();
            dispose();
        });
        
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 키 리스너 추가 (방향키로 탭 전환, 엔터로 메인 화면 복귀)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                
                if (keyCode == KeyEvent.VK_LEFT) {
                    // 왼쪽 방향키: 일반 모드 탭으로 이동
                    tabbedPane.setSelectedIndex(0);
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    // 오른쪽 방향키: 아이템 모드 탭으로 이동
                    tabbedPane.setSelectedIndex(1);
                } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ESCAPE) {
                    // 엔터 또는 ESC: 메인 화면으로 돌아가기
                    new StartFrame();
                    dispose();
                }
            }
        });

        setFocusable(true);
        requestFocusInWindow();  // 프레임에 포커스 설정
        setVisible(true);
    }

    private JPanel createScorePanel(boolean isItemMode) {
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        scorePanel.setBorder(BorderFactory.createEmptyBorder((int)(20*screenRatio), (int)(50*screenRatio), (int)(20*screenRatio), (int)(50*screenRatio)));

        List<HighScoreModel.ScoreEntry> topScores = highScoreModel.getTopScores(isItemMode);
        
        if (topScores.isEmpty()) {
            JLabel noScoreLabel = new JLabel("No scores yet!", SwingConstants.CENTER);
            noScoreLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, (int)(24*screenRatio)));
            noScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            scorePanel.add(noScoreLabel);
        } else {
            for (int i = 0; i < topScores.size(); i++) {
                HighScoreModel.ScoreEntry entry = topScores.get(i);
                
                JPanel entryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, (int)(10*screenRatio), 0));
                entryPanel.setMaximumSize(new Dimension((int)(500*screenRatio), (int)(40*screenRatio)));
                
                JLabel rankLabel = new JLabel(String.format("%2d.", i + 1));
                rankLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int)(18*screenRatio)));
                rankLabel.setPreferredSize(new Dimension((int)(40*screenRatio), (int)(30*screenRatio)));
                
                JLabel nameLabel = new JLabel(entry.getName());
                nameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, (int)(18*screenRatio)));
                nameLabel.setPreferredSize(new Dimension((int)(100*screenRatio), (int)(30*screenRatio)));
                
                JLabel scoreLabel = new JLabel(String.format("%,d", entry.getScore()));
                scoreLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int)(18*screenRatio)));
                scoreLabel.setPreferredSize(new Dimension((int)(120*screenRatio), (int)(30*screenRatio)));
                scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                
                JLabel difficultyLabel = new JLabel("[" + entry.getDifficulty().toUpperCase() + "]");
                difficultyLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int)(16*screenRatio)));
                difficultyLabel.setPreferredSize(new Dimension((int)(80*screenRatio), (int)(30*screenRatio)));
                
                switch (entry.getDifficulty().toLowerCase()) {
                    case "easy":
                        difficultyLabel.setForeground(new Color(34, 139, 34));
                        break;
                    case "hard":
                        difficultyLabel.setForeground(new Color(220, 20, 60));
                        break;
                    default:
                        difficultyLabel.setForeground(new Color(70, 130, 180));
                        break;
                }
                
                entryPanel.add(rankLabel);
                entryPanel.add(nameLabel);
                entryPanel.add(scoreLabel);
                entryPanel.add(difficultyLabel);
                
                scorePanel.add(entryPanel);
                scorePanel.add(Box.createVerticalStrut((int)(5*screenRatio)));
            }
        }

        JScrollPane scrollPane = new JScrollPane(scorePanel);
        scrollPane.setBorder(null);
        
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);
        
        return wrapperPanel;
    }
}
