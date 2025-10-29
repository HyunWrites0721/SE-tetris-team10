package start;

import javax.swing.*;
import settings.HighScoreModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ScoreBoardFrame extends JFrame {
    private double screenRatio = StartFrame.screenRatio;
    private HighScoreModel highScoreModel;

    public ScoreBoardFrame() {
        setTitle(StartFrame.titleName);
        setSize((int)(600*screenRatio), (int)(600*screenRatio));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // High Score 모델 초기화 (싱글톤)
        highScoreModel = HighScoreModel.getInstance();

        // 상단 제목
        JLabel titleLabel = new JLabel("HIGH SCORE", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int)(36*screenRatio)));
        titleLabel.setBorder(BorderFactory.createEmptyBorder((int)(20*screenRatio), 0, (int)(20*screenRatio), 0));
        add(titleLabel, BorderLayout.NORTH);

        // 중앙 점수 표시 패널
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        scorePanel.setBorder(BorderFactory.createEmptyBorder((int)(20*screenRatio), (int)(50*screenRatio), (int)(20*screenRatio), (int)(50*screenRatio)));

        // Top 10 점수 표시
        List<HighScoreModel.ScoreEntry> topScores = highScoreModel.getTopScores();
        
        if (topScores.isEmpty()) {
            JLabel noScoreLabel = new JLabel("No scores yet!", SwingConstants.CENTER);
            noScoreLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, (int)(24*screenRatio)));
            noScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            scorePanel.add(noScoreLabel);
        } else {
            for (int i = 0; i < topScores.size(); i++) {
                HighScoreModel.ScoreEntry entry = topScores.get(i);
                
                JPanel entryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, (int)(20*screenRatio), 0));
                entryPanel.setMaximumSize(new Dimension((int)(500*screenRatio), (int)(40*screenRatio)));
                
                // 순위
                JLabel rankLabel = new JLabel(String.format("%2d.", i + 1));
                rankLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int)(20*screenRatio)));
                rankLabel.setPreferredSize(new Dimension((int)(50*screenRatio), (int)(30*screenRatio)));
                
                // 이름
                JLabel nameLabel = new JLabel(entry.getName());
                nameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, (int)(20*screenRatio)));
                nameLabel.setPreferredSize(new Dimension((int)(150*screenRatio), (int)(30*screenRatio)));
                
                // 점수
                JLabel scoreLabel = new JLabel(String.format("%,d", entry.getScore()));
                scoreLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int)(20*screenRatio)));
                scoreLabel.setPreferredSize(new Dimension((int)(150*screenRatio), (int)(30*screenRatio)));
                scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                
                entryPanel.add(rankLabel);
                entryPanel.add(nameLabel);
                entryPanel.add(scoreLabel);
                
                scorePanel.add(entryPanel);
                scorePanel.add(Box.createVerticalStrut((int)(5*screenRatio)));
            }
        }

        JScrollPane scrollPane = new JScrollPane(scorePanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // 하단 버튼
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder((int)(20*screenRatio), 0, (int)(20*screenRatio), 0));
        
        JButton backButton = new JButton("돌아가기");
        backButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int)(20*screenRatio)));
        backButton.setPreferredSize(new Dimension((int)(150*screenRatio), (int)(50*screenRatio)));
        backButton.setFocusable(true);  // 포커스 가능하도록 변경
        
        // 마우스 클릭
        backButton.addActionListener(e -> {
            new StartFrame();
            dispose();
        });
        
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 키보드 ESC 또는 ENTER로 돌아가기
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    new StartFrame();
                    dispose();
                }
            }
        });

        setFocusable(true);
        backButton.requestFocusInWindow();  // 버튼에 포커스 설정
        setVisible(true);
    }
}
