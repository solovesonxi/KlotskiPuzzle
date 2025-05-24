package view.login;

import view.MainFrame;
import view.ViewUtil;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LoginPanel extends JPanel {
    private final MainFrame mainFrame; // 主窗口引用
    private final JTextField username; // 用户名输入框
    private final JTextField password; // 密码输入框
    public JPanel contentPanel; // 内容面板
    private HashMap<String, String> users; // 用户信息存储

    public LoginPanel(MainFrame mainFrame, int width, int height) {
        this.setLayout(null); // 设置为绝对布局
        this.setSize(width, height);
        this.mainFrame = mainFrame; // 关联主窗口
        loadUsers(); // 加载用户信息

        contentPanel = new JPanel();
        contentPanel.setLayout(null); // 设置内容面板为绝对布局
        contentPanel.setBounds(0, 0, width, height);
        contentPanel.setOpaque(false); // 设置为透明
        ImageIcon gifIcon = new ImageIcon("resources/image/三国1080.gif"); // 使用 ImageIcon 加载GIF动画
        JLabel backgroundLabel = new JLabel(gifIcon); // 背景标签
        backgroundLabel.setBounds(0, 0, 1920, 1080);
        this.add(contentPanel); // 添加内容面板
        this.add(backgroundLabel); // 添加背景标签

        // 创建用户名和密码输入框
        username = ViewUtil.createJTextField(contentPanel, new Point(width / 2 - 40, height / 2 - 100), 160, 30);
        password = ViewUtil.createJTextField(contentPanel, new Point(width / 2 - 40, height / 2 - 50), 160, 30);
        ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 110, height / 2 - 100), 70, 30, 16, "用户名:"); // 用户名标签
        ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 110, height / 2 - 50), 70, 30, 16, "密码:"); // 密码标签
        ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 110, height / 2 - 300), 240, 120, 60, "华 容 道"); // 游戏标题

        // 登录注册按钮
        Color btnColor = new Color(139, 69, 19); // 按钮颜色
        Font btnFont = new Font("楷体", Font.PLAIN, 16); // 按钮字体
        JButton loginBtn = ViewUtil.createStyledButton(contentPanel, "登录", new Point(width / 2 - 80, height / 2 + 30), 80, 30, btnColor, btnFont);
        JButton registerBtn = ViewUtil.createStyledButton(contentPanel, "注册", new Point(width / 2 + 20, height / 2 + 30), 80, 30, btnColor, btnFont);
        JButton guestLoginBtn = ViewUtil.createStyledButton(contentPanel, "游客登录", new Point(width / 2 - 80, height / 2 + 80), 180, 32, btnColor, btnFont);
        loginBtn.addActionListener(_ -> handleLogin()); // 登录按钮事件
        registerBtn.addActionListener(_ -> handleRegister()); // 注册按钮事件
        guestLoginBtn.addActionListener(_ -> mainFrame.showControl(null)); // 游客登录事件
    }

    // 加载用户信息
    public void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader("resources/users.txt"))) {
            String line;
            users = new HashMap<>(); // 初始化用户Map
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":"); // 按照冒号分割
                String username = parts[0].trim();
                String password = parts[1].trim();
                users.put(username, password); // 将用户名和密码存入 HashMap
            }
        } catch (IOException e) {
            System.err.println("加载用户信息时出错: " + e.getMessage());
        }
    }

    // 保存用户信息
    public void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("resources/users.txt"))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue()); // 写入用户名和密码
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("保存用户信息时出错: " + e.getMessage());
        }
    }

    // 验证用户名和密码
    public boolean isValidCredentials(String user, String pass) {
        if (user.isEmpty() || pass.isEmpty()) { // 检查是否为空
            JOptionPane.showMessageDialog(this, "用户名或密码不能为空");
        } else if (Pattern.matches("^[a-zA-Z0-9一-龥]+$", user) && Pattern.matches("^[a-zA-Z0-9]+$", pass)) {
            return true; // 验证通过
        } else {
            JOptionPane.showMessageDialog(this, "用户名只能包含数字、汉字和大小写字母\n密码只能包含数字和大小写字母");
        }
        return false;
    }

    // 处理登录请求
    private void handleLogin() {
        String user = username.getText();
        String pass = password.getText();
        if (isValidCredentials(user, pass)) {
            if (users.containsKey(user) && users.get(user).equals(pass)) { // 验证用户存在及密码正确性
                mainFrame.showControl(user); // 显示控制面板
            } else {
                JOptionPane.showMessageDialog(this, "用户名或密码错误");
            }
        }
    }

    // 处理注册请求
    private void handleRegister() {
        String user = username.getText();
        String pass = password.getText();
        if (isValidCredentials(user, pass)) {
            if (!users.containsKey(user)) { // 检查用户名是否已被注册
                JOptionPane.showMessageDialog(this, "注册成功");
                users.put(user, pass); // 将新用户信息存入Map
                saveUsers(); // 保存用户信息
                mainFrame.showControl(user); // 显示控制面板
            } else {
                JOptionPane.showMessageDialog(this, "该用户名已被注册");
            }
        }
    }
}
