package game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PauseBoard extends JPanel implements KeyListener {

    private FrameBoard frameBoard;
    private JLabel pauseLabel;
    private JPanel buttonPanel;
    private javax.swing.JButton resumeButton;
    private javax.swing.JButton restartButton;
    private javax.swing.JButton mainButton;
    private javax.swing.JButton quitButton;
    
    private int selectedButtonIndex = 0; // 현재 선택된 버튼 인덱스
    private javax.swing.JButton[] buttons; // 버튼 배열
    
    public void convertScale(double scale) {
        // Update top panel margin
        ((JPanel)getComponent(0)).setBorder(javax.swing.BorderFactory.createEmptyBorder(
            (int)(40 * scale), 0, 0, 0));
            
        // Update pause label font
        pauseLabel.setFont(new Font("Arial", Font.BOLD, (int)(32 * scale)));
        
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
        
        for (javax.swing.JButton button : new javax.swing.JButton[]{resumeButton, restartButton, mainButton, quitButton}) {
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

    public PauseBoard(FrameBoard frameBoard) {
        this.frameBoard = frameBoard;
        
        // 키 입력을 받을 수 있도록 포커스 설정
        setFocusable(true);
        setFocusTraversalKeysEnabled(false); // 탭키 등이 포커스를 뺏지 않도록 설정
        addKeyListener(this);
        
        setOpaque(true);
        setLayout(new BorderLayout());

        // Top margin panel for PAUSED label
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        // Scale top margin
        topPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder((int)(40 * start.StartFrame.screenRatio), 0, 0, 0));

        pauseLabel = new JLabel("PAUSED");
        pauseLabel.setFont(new Font("Arial", Font.BOLD, (int)(32 * start.StartFrame.screenRatio)));
        pauseLabel.setForeground(Color.black);
        pauseLabel.setHorizontalAlignment(JLabel.CENTER);
        pauseLabel.setVerticalAlignment(JLabel.TOP);
        topPanel.add(pauseLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        buttonPanel = new JPanel();
        Color buttonColor = new Color(245, 245, 245);

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        buttonPanel.setOpaque(false);
        // Scale button panel margins
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(
            (int)(5 * start.StartFrame.screenRatio), 
            (int)(40 * start.StartFrame.screenRatio), 
            (int)(10 * start.StartFrame.screenRatio), 
            (int)(40 * start.StartFrame.screenRatio)));

        resumeButton = new javax.swing.JButton("resume");
        // Scale button dimensions
        int buttonWidth = (int)(200 * start.StartFrame.screenRatio);
        int buttonHeight = (int)(60 * start.StartFrame.screenRatio);
        resumeButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        resumeButton.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
        resumeButton.setFont(new Font("Arial", Font.BOLD, (int)(12 * start.StartFrame.screenRatio))); 
        resumeButton.setBackground(buttonColor); 
        resumeButton.setForeground(Color.BLACK); 
        resumeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resumeButton.setFocusPainted(false); 
        resumeButton.setOpaque(true);
        resumeButton.addActionListener(e -> {
            // PauseBoard만 숨기고 게임 화면으로 복귀
            PauseBoard.this.setVisible(false);
            setOpaque(false);
            // 프레임보드에 토글 위임 (현재 일시정지 상태에서 호출되므로 재개됨)
            frameBoard.paused();

        });

        restartButton = new javax.swing.JButton("restart");
        // Scale button dimensions
        restartButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        restartButton.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
        restartButton.setFont(new Font("Arial", Font.BOLD, (int)(12 * start.StartFrame.screenRatio)));
        restartButton.setBackground(buttonColor);
        restartButton.setForeground(Color.BLACK);
        restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        restartButton.setFocusPainted(false);
        restartButton.setOpaque(true);

        restartButton.addActionListener(e -> {
            if (frameBoard != null) {
                this.setVisible(false);
                frameBoard.gameInit();
            }
        });


        mainButton = new javax.swing.JButton("Main Menu");
        // Scale button dimensions
        mainButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        mainButton.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
        mainButton.setFont(new Font("Arial", Font.BOLD, (int)(12 * start.StartFrame.screenRatio)));
        mainButton.setBackground(buttonColor); 
        mainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainButton.setForeground(Color.BLACK); 
        mainButton.setFocusPainted(false); 
        mainButton.setOpaque(true);

                mainButton.addActionListener(e -> {
            if (frameBoard != null) {
                frameBoard.dispose();  // 현재 게임 창 닫기
                new start.StartFrame();  // 새로운 시작 화면 생성
            }
        });

        quitButton = new javax.swing.JButton("Quit");
        // Scale button dimensions
        quitButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        quitButton.setMaximumSize(new Dimension(buttonWidth, buttonHeight));
        quitButton.setFont(new Font("Arial", Font.BOLD, (int)(12 * start.StartFrame.screenRatio)));
        quitButton.setBackground(buttonColor); 
        quitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
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
        
        // 버튼 배열 초기화
        buttons = new javax.swing.JButton[]{resumeButton, restartButton, mainButton, quitButton};
        
        // 초기 버튼 선택 상태 설정
        updateButtonSelection();
        
        add(buttonPanel, BorderLayout.CENTER);
    }
    
}