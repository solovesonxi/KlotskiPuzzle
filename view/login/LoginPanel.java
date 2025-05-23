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
    private final MainFrame mainFrame;
    private final JTextField username;
    private final JTextField password;
    public JPanel contentPanel;
    private HashMap<String, String> users;


    public LoginPanel(MainFrame mainFrame, int width, int height) {
        this.setLayout(null);
        this.setSize(width, height);
        this.mainFrame = mainFrame;
        loadUsers();

        contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setBounds(0, 0, width, height);
        contentPanel.setOpaque(false);
        ImageIcon gifIcon = new ImageIcon("resources/image/三国1080.gif"); // 使用 ImageIcon 加载GIF动画
        JLabel backgroundLabel = new JLabel(gifIcon);
        backgroundLabel.setBounds(0, 0, 1920, 1080);
        this.add(contentPanel);
        this.add(backgroundLabel);

        username = ViewUtil.createJTextField(contentPanel, new Point(width / 2 - 40, height / 2 - 100), 160, 30);
        password = ViewUtil.createJTextField(contentPanel, new Point(width / 2 - 40, height / 2 - 50), 160, 30);
        ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 110, height / 2 - 100), 70, 30, 16, "用户名:");
        ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 110, height / 2 - 50), 70, 30, 16, "密码:");
        ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 110, height / 2 - 300), 240, 120, 60, "华 容 道");

        // 登录注册按钮
        Color btnColor = new Color(139, 69, 19);
        Font btnFont = new Font("楷体", Font.PLAIN, 16);
        JButton loginBtn = ViewUtil.createStyledButton(contentPanel, "登录", new Point(width / 2 - 80, height / 2 + 30), 80, 30, btnColor, btnFont);
        JButton registerBtn = ViewUtil.createStyledButton(contentPanel, "注册", new Point(width / 2 + 20, height / 2 + 30), 80, 30, btnColor, btnFont);
        JButton guestLoginBtn = ViewUtil.createStyledButton(contentPanel, "游客登录", new Point(width / 2 - 80, height / 2 + 80), 180, 32, btnColor, btnFont);
        loginBtn.addActionListener(_ -> handleLogin());
        registerBtn.addActionListener(_ -> handleRegister());
        guestLoginBtn.addActionListener(_ -> mainFrame.showControl(null));
    }


    // 加载用户信息
    public void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader("resources/users.txt"))) {
            String line;
            users = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
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
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("保存用户信息时出错: " + e.getMessage());
        }
    }

    // 验证用户名和密码
    public boolean isValidCredentials(String user, String pass) {
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名或密码不能为空");
        } else if (Pattern.matches("^[a-zA-Z0-9一-龥]+$", user) && Pattern.matches("^[a-zA-Z0-9]+$", pass)) {
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "用户名只能包含数字、汉字和大小写字母\n密码只能包含数字和大小写字母");
        }
        return false;
    }


    private void handleLogin() {
        String user = username.getText();
        String pass = password.getText();
        if (isValidCredentials(user, pass)) {
            if (users.containsKey(user) && users.get(user).equals(pass)) {
                mainFrame.showControl(user);
            } else {
                JOptionPane.showMessageDialog(this, "用户名或密码错误");
            }
        }
    }

    private void handleRegister() {
        String user = username.getText();
        String pass = password.getText();
        if (isValidCredentials(user, pass)) {
            if (!users.containsKey(user)) {
                JOptionPane.showMessageDialog(this, "注册成功");
                users.put(user, pass);
                saveUsers();
                mainFrame.showControl(user);
            } else {
                JOptionPane.showMessageDialog(this, "该用户名已被注册");
            }
        }
    }


}