package game;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.Border;

public class GameOverBoard extends JPanel implements KeyListener {

    private final FrameBoard frameBoard;
    private JLabel gameOverLabel;
    private JPanel buttonPanel;
    private javax.swing.JButton restartButton;
    private javax.swing.JButton mainButton;
    private javax.swing.JButton quitButton;
    
    private int selectedButtonIndex = 0; // 현재 선택된 버튼 인덱스
    private javax.swing.JButton[] buttons; // 버튼 배열

    public void convertScale(double scale) {
        // Update game over label font
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, (int)(32 * scale)));

        // Update button panel margins
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(
            (int)(5 * scale),
            (int)(40 * scale),
            (int)(10 * scale),
            (int)(40 * scale)));

        // Update button dimensions and fonts
        int buttonWidth = (int)(200 * scale);
        int buttonHeight = (int)(60 * scale);
        int fontSize = (int)(12 * scale);

        // Update all buttons
        for (javax.swing.JButton button : new javax.swing.JButton[]{restartButton, mainButton, quitButton}) {
            button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
            button.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
            button.setFont(new Font("Arial", Font.BOLD, fontSize));
        }

        revalidate();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // 키 입력 이벤트 처리
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                selectedButtonIndex = (selectedButtonIndex - 1 + buttons.length) % buttons.length;
                updateButtonSelection();
                break;
            case KeyEvent.VK_DOWN:
                selectedButtonIndex = (selectedButtonIndex + 1) % buttons.length;
                updateButtonSelection();
                break;
            case KeyEvent.VK_ENTER:
                buttons[selectedButtonIndex].doClick();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // 키 해제 이벤트 처리
    }

    private void updateButtonSelection() {
        Color buttonColor = new Color(245, 245, 245);
        Color selectedColor = new Color(200, 200, 200);
        
        for (int i = 0; i < buttons.length; i++) {
            javax.swing.JButton button = buttons[i];
            if (i == selectedButtonIndex) {
                button.setBackground(selectedColor);
                button.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            } else {
                button.setBackground(buttonColor);
                button.setBorder(BorderFactory.createEmptyBorder());
            }
        }
    }

    public GameOverBoard(FrameBoard frameBoard) {
        this.frameBoard = frameBoard;
        
        // 키 입력을 받을 수 있도록 포커스 설정
        setFocusable(true);
        setFocusTraversalKeysEnabled(false); // 탭키 등이 포커스를 뺏지 않도록 설정
        addKeyListener(this);

        setOpaque(true);
        setBackground(new Color(0,0,0,255));
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(40, 0, 0, 0)); // top margin

        gameOverLabel = new JLabel("GAME OVER");
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 32));
        gameOverLabel.setForeground(Color.WHITE);
        gameOverLabel.setHorizontalAlignment(JLabel.CENTER);
        gameOverLabel.setVerticalAlignment(JLabel.TOP);
        topPanel.add(gameOverLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        buttonPanel = new JPanel();
        Color buttonColor = new Color(245, 245, 245);

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(50, 40, 10, 40)); // 상단 여백 증가

        restartButton = new javax.swing.JButton("restart");
        restartButton.setPreferredSize(new Dimension(200, 60));
        restartButton.setMaximumSize(new Dimension(200, 60));
        restartButton.setFont(new Font("Arial", Font.BOLD, 12));
        restartButton.setBackground(buttonColor);
        restartButton.setForeground(Color.BLACK);
        restartButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
        restartButton.setFocusPainted(false);
        restartButton.setBorderPainted(false); // 테두리 제거
        restartButton.setContentAreaFilled(true); // 배경색 채우기 활성화
        restartButton.setOpaque(true);

        restartButton.addActionListener(e -> {
            if (frameBoard != null) {
                frameBoard.gameInit();
                this.setVisible(false);
            }
        });

        mainButton = new javax.swing.JButton("Main Menu");
        mainButton.setPreferredSize(new Dimension(200, 60));
        mainButton.setMaximumSize(new Dimension(200, 60));
        mainButton.setFont(new Font("Arial", Font.BOLD, 12));
        mainButton.setBackground(buttonColor);
        mainButton.setForeground(Color.BLACK);
        mainButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
        mainButton.setFocusPainted(false);
        mainButton.setBorderPainted(false); // 테두리 제거
        mainButton.setContentAreaFilled(true); // 배경색 채우기 활성화
        mainButton.setOpaque(true);

        mainButton.addActionListener(e -> {
            // Main Menu 버튼 클릭 시 동작
            if (frameBoard != null) {
                frameBoard.dispose();  // 현재 게임 창 닫기
                new start.StartFrame();  // 새로운 시작 화면 생성
            }
        });

        quitButton = new javax.swing.JButton("Quit");
        quitButton.setPreferredSize(new Dimension(200, 60));
        quitButton.setMaximumSize(new Dimension(200, 60));
        quitButton.setFont(new Font("Arial", Font.BOLD, 12));
        quitButton.setBackground(buttonColor);
        quitButton.setForeground(Color.BLACK);
        quitButton.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬
        quitButton.setFocusPainted(false);
        quitButton.setBorderPainted(false); // 테두리 제거
        quitButton.setContentAreaFilled(true); // 배경색 채우기 활성화
        quitButton.setOpaque(true);

        quitButton.addActionListener(e -> {
            // Quit 버튼 클릭 시 동작
            System.exit(0);
        });

        // Add buttons with vertical spacing
        buttonPanel.add(Box.createVerticalStrut(20)); // Top spacing
        buttonPanel.add(restartButton);
        buttonPanel.add(Box.createVerticalStrut(10)); // Spacing between buttons
        buttonPanel.add(mainButton);
        buttonPanel.add(Box.createVerticalStrut(10)); // Spacing between buttons
        buttonPanel.add(quitButton);
        buttonPanel.add(Box.createVerticalStrut(20)); // Bottom spacing

        // 버튼 배열 초기화
        buttons = new JButton[]{restartButton, mainButton, quitButton};
        
        // 초기 버튼 선택 상태 설정
        updateButtonSelection();

        // 버튼 패널을 바로 추가 (y축은 자연스럽게 배치)
        add(buttonPanel, BorderLayout.CENTER);
    }

}