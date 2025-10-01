import javax.swing.*;
import java.util.Scanner;
public class App {

    public static void main(String[] args) {
        // SwingUtilities.invokeLater(new Runnable() {
        //     public void run() {
        //         StartFrame startFrame = new StartFrame();
        //         startFrame.setVisible(true);
        //     }
        // });

        FrameBoard frame = new FrameBoard();

        frame.setVisible(true);

        Scanner sc = new Scanner(System.in);
        
        while(true) {

            System.out.print("Enter row and column to set block text (e.g., '5 3'): ");
            int row = sc.nextInt();
            int col = sc.nextInt();
            frame.setBlockText(row, col);  // 입력받은 위치에 "" 표시
            

            for (int i = 0; i < 20; i++) {

                frame.oneLineClear(i);

            }
        }

    }
    
}
