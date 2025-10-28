package MainGame;
// import java.awt.*;
import javax.swing.JFrame;
public class BaseGame {
    public static void main(String[] args){
        JFrame window = new JFrame("Dust & Magic");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GamePanle gamePanle = new GamePanle();
        gamePanle.setPreferredSize(new java.awt.Dimension(750, 580));

        window.setContentPane(gamePanle);
        window.pack(); // ปรับขนาด JFrame ให้พอดีกับ GamePanle
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanle.startGameThread();
    }
}
