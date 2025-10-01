import javax.swing.*;
import java.awt.*;

public class SettingView extends JPanel{
    
    private double screenRatio = StartFrame.screenRatio;
    private final String menuName;
    JRadioButton Button1, Button2, Button3;
    JButton checkButton;
    JPanel panel;

    public SettingView(String menuName){
        this.menuName = menuName;  
        setLayout(new GridBagLayout());
        
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, (int)(30*screenRatio), (int)(30*screenRatio)));
        panel.setPreferredSize(new Dimension((int)(320*screenRatio), (int)(240*screenRatio)));
        JLabel label = new JLabel();
        panel.add(label);
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

        Button1 = new JRadioButton();
        Button2 = new JRadioButton();
        Button3 = new JRadioButton();

        ButtonGroup buttonGroup = new ButtonGroup();

        switch (menuName) {
            case "설정값 초기화":
                label.setText("설정을 초기화 하겠습니까?");
                checkButton = new JButton("예");
                panel.add(checkButton);
                break;
            case "스코어보드 초기화":
                label.setText("스코어보드를 초기화 하겠습니까?");
                checkButton = new JButton("예");
                panel.add(checkButton);
                break;
            case "화면 크기 설정":
                label.setText("화면 크기:");
                Button1.setText("작은 화면");
                Button2.setText("보통 화면");
                Button3.setText("큰 화면");
                panel.add(Button1);
                panel.add(Button2);
                panel.add(Button3);
                buttonGroup.add(Button1);
                buttonGroup.add(Button2);
                buttonGroup.add(Button3);
                checkButton = new JButton("확인");
                panel.add(checkButton);
                break;
            case "색맹 모드":
                label.setText("색맹 모드: ");
                Button1.setText("끔");
                Button2.setText("켬");
                panel.add(Button1);
                panel.add(Button2);
                buttonGroup.add(Button1);
                buttonGroup.add(Button2);
                checkButton = new JButton("확인");
                panel.add(checkButton);
                break;
            case "조작키 설정":
                label.setText("조작키: ");
                Button1.setText("방향키");
                Button2.setText("WASD");
                panel.add(Button1);
                panel.add(Button2);
                buttonGroup.add(Button1);
                buttonGroup.add(Button2);
                checkButton = new JButton("확인");
                panel.add(checkButton);
                break;
            default:
                label.setText("Invalid Setting");
                break;
        }
        add(panel);
    }

    public String getSettingName() {
        return menuName;
    }
}