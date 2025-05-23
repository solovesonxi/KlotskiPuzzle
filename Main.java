import view.MainFrame;
import view.login.LoginPanel;

import javax.swing.*;

// 游戏的主类，负责启动游戏
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame(1532, 864).setVisible(true);
        });
    }
}
