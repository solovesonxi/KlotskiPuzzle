package view.login;

import data.UserRepository;
import util.AppResources;
import view.GameTheme;
import view.MainFrame;

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
    private final JLabel usernameLabel;
    private final JLabel passwordLabel;
    private final JLabel titleLabel;
    private final JButton loginBtn;
    private final JButton registerBtn;
    private final JButton guestLoginBtn;
    private final JButton labBtn;
    private final JPanel loginCard;
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

        loginCard = new LoginCardPanel();
        loginCard.setLayout(new GridBagLayout());
        contentPanel.add(loginCard);

        username = new JTextField();
        password = new JPasswordField();
        GameTheme.styleTextField(username);
        GameTheme.styleTextField(password);
        usernameLabel = createFormLabel(text("login.username"));
        passwordLabel = createFormLabel(text("login.password"));
        titleLabel = new JLabel(text("login.title"), SwingConstants.CENTER);
        titleLabel.setForeground(GameTheme.TEXT);
        titleLabel.setFont(GameTheme.displayFont(44));

        loginBtn = createCardButton(text("login.sign.in"));
        registerBtn = createCardButton(text("login.register"));
        guestLoginBtn = createCardButton(text("login.guest"));
        labBtn = createCardButton(text("login.lab"));
        buildLoginCard();
        loginBtn.addActionListener(event -> handleLogin()); // 登录按钮事件
        registerBtn.addActionListener(event -> handleRegister()); // 注册按钮事件
        guestLoginBtn.addActionListener(event -> mainFrame.showControl(null)); // 游客登录事件
        labBtn.addActionListener(event -> mainFrame.showLab());
    }

    @Override
    public void doLayout() {
        super.doLayout();
        contentPanel.setBounds(0, 0, getWidth(), getHeight());
        int cardWidth = Math.min(520, Math.max(360, getWidth() - 80));
        int cardHeight = Math.min(460, Math.max(400, getHeight() - 120));
        loginCard.setBounds((getWidth() - cardWidth) / 2,
                (getHeight() - cardHeight) / 2 + 12, cardWidth, cardHeight);
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

    public void applyLanguage() {
        usernameLabel.setText(text("login.username"));
        passwordLabel.setText(text("login.password"));
        titleLabel.setText(text("login.title"));
        loginBtn.setText(text("login.sign.in"));
        registerBtn.setText(text("login.register"));
        guestLoginBtn.setText(text("login.guest"));
        labBtn.setText(text("login.lab"));
        loginCard.revalidate();
        loginCard.repaint();
    }

    private void buildLoginCard() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 12, 10, 12);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.insets = new Insets(14, 30, 28, 30);
        loginCard.add(titleLabel, constraints);

        constraints.gridwidth = 1;
        constraints.weightx = 0;
        constraints.gridy++;
        constraints.insets = new Insets(8, 36, 8, 8);
        loginCard.add(usernameLabel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.insets = new Insets(8, 8, 8, 36);
        username.setPreferredSize(new Dimension(230, 36));
        loginCard.add(username, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.weightx = 0;
        constraints.insets = new Insets(8, 36, 8, 8);
        loginCard.add(passwordLabel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.insets = new Insets(8, 8, 8, 36);
        password.setPreferredSize(new Dimension(230, 36));
        loginCard.add(password, constraints);

        JPanel accountActions = new JPanel(new GridLayout(1, 2, 12, 0));
        accountActions.setOpaque(false);
        accountActions.add(loginBtn);
        accountActions.add(registerBtn);
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.insets = new Insets(24, 36, 8, 36);
        loginCard.add(accountActions, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(8, 36, 8, 36);
        loginCard.add(guestLoginBtn, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(8, 36, 20, 36);
        loginCard.add(labBtn, constraints);
    }

    private JLabel createFormLabel(String labelText) {
        JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
        label.setForeground(GameTheme.TEXT_MUTED);
        label.setFont(GameTheme.bodyFont(16));
        return label;
    }

    private JButton createCardButton(String buttonText) {
        return GameTheme.createButton(buttonText);
    }

    private static final class LoginCardPanel extends JPanel {
        private LoginCardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(new Color(GameTheme.INK.getRed(), GameTheme.INK.getGreen(),
                    GameTheme.INK.getBlue(), 224));
            graphics2D.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 34, 34);
            graphics2D.setColor(GameTheme.GOLD_SOFT);
            graphics2D.setStroke(new BasicStroke(2f));
            graphics2D.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 34, 34);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }
    }
}
