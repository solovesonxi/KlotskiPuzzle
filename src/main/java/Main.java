import java.util.Locale;
import view.MainFrame;

import javax.swing.*;
import util.AppResources;

// 游戏的主类，负责启动游戏
public class Main {
    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.equals("en")) {
                AppResources.setLocale(Locale.ENGLISH);
            }
        }
        
        SwingUtilities.invokeLater(() -> new MainFrame(1532, 864).setVisible(true));
    }
}
