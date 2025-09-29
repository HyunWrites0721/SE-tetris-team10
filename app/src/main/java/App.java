import javax.swing.*;

public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                StartFrame startFrame = new StartFrame();
                startFrame.setVisible(true);
            }
        });
        
    }
}
