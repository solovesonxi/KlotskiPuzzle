package view.login;

import data.UserRepository;
import util.AppResources;
import view.MainFrame;
import view.ViewUtil;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import static util.Messages.text;

public class LoginPanel extends JPanel {
    private final MainFrame mainFrame; // 主窗口引用
    private final Image backgroundImage;
    private final JTextField username; // 用户名输入框
    private final JPasswordField password; // 密码输入框
    public JPanel contentPanel; // 内容面板
    private UserRepository users;

    public LoginPanel(MainFrame mainFrame, int width, int height) {
        this.setLayout(null); // 设置为绝对布局
        this.setSize(width, height);
        this.mainFrame = mainFrame; // 关联主窗口
        try {
            users = new UserRepository();
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this, text("login.data.init.error", exception.getMessage()));
        }

        contentPanel = new JPanel();
        contentPanel.setLayout(null); // 设置内容面板为绝对布局
        contentPanel.setBounds(0, 0, width, height);
        contentPanel.setOpaque(false); // 设置为透明
        backgroundImage = AppResources.icon("resources/original/image/login-background.gif").getImage();
        this.add(contentPanel); // 添加内容面板

        // 创建用户名和密码输入框
        username = ViewUtil.createJTextField(contentPanel, new Point(width / 2 - 40, height / 2 - 100), 160, 30);
        password = ViewUtil.createJPasswordField(contentPanel, new Point(width / 2 - 40, height / 2 - 50), 160, 30);
        ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 110, height / 2 - 100), 90, 30, 16, text("login.username")); // 用户名标签
        ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 110, height / 2 - 50), 90, 30, 16, text("login.password")); // 密码标签
        ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 180, height / 2 - 300), 380, 120, 48, text("login.title")); // 游戏标题

        // 登录注册按钮
        Color btnColor = new Color(139, 69, 19); // 按钮颜色
        Font btnFont = new Font("楷体", Font.PLAIN, 16); // 按钮字体
        JButton loginBtn = ViewUtil.createStyledButton(contentPanel, text("login.sign.in"), new Point(width / 2 - 100, height / 2 + 30), 100, 30, btnColor, btnFont);
        JButton registerBtn = ViewUtil.createStyledButton(contentPanel, text("login.register"), new Point(width / 2 + 20, height / 2 + 30), 100, 30, btnColor, btnFont);
        JButton guestLoginBtn = ViewUtil.createStyledButton(contentPanel, text("login.guest"), new Point(width / 2 - 100, height / 2 + 80), 220, 32, btnColor, btnFont);
        loginBtn.addActionListener(event -> handleLogin()); // 登录按钮事件
        registerBtn.addActionListener(event -> handleRegister()); // 注册按钮事件
        guestLoginBtn.addActionListener(event -> mainFrame.showControl(null)); // 游客登录事件
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }

    // 验证用户名和密码
    public boolean isValidCredentials(String user, String pass) {
        if (user.isEmpty() || pass.isEmpty()) { // 检查是否为空
            JOptionPane.showMessageDialog(this, text("login.credentials.required"));
        } else if (Pattern.matches("^[a-zA-Z0-9一-龥]+$", user) && Pattern.matches("^[a-zA-Z0-9]+$", pass)) {
            return true; // 验证通过
        } else {
            JOptionPane.showMessageDialog(this, text("login.credentials.syntax"));
        }
        return false;
    }

    // 处理登录请求
    private void handleLogin() {
        String user = username.getText();
        char[] pass = password.getPassword();
        try {
            if (isValidCredentials(user, new String(pass))) {
                if (users != null && users.verify(user, pass)) {
                mainFrame.showControl(user); // 显示控制面板
                } else {
                    JOptionPane.showMessageDialog(this, text("login.credentials.invalid"));
                }
            }
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this, text("login.read.error", exception.getMessage()));
        } finally {
            Arrays.fill(pass, '\0');
        }
    }

    // 处理注册请求
    private void handleRegister() {
        String user = username.getText();
        char[] pass = password.getPassword();
        try {
            if (isValidCredentials(user, new String(pass))) {
                if (users != null && users.register(user, pass)) {
                    JOptionPane.showMessageDialog(this, text("login.register.success"));
                    mainFrame.showControl(user);
                } else {
                    JOptionPane.showMessageDialog(this, text("login.register.exists"));
                }
            }
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this, text("login.write.error", exception.getMessage()));
        } finally {
            Arrays.fill(pass, '\0');
        }
    }
}
