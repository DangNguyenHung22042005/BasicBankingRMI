package Banker.Client;

import Banker.Server.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutionException;

public class BankClientGUI extends JFrame implements ClientCallback {
    private Registry registry;
    private AccountList accountListRMI;
    private NotificationService notificationServiceRMI;
    private Account currentAccountRMI;
    private ClientCallback clientStub;

    private JTextArea txtLog;
    private JTextField txtServerIP;
    private JTextField txtAccountLogin;
    private JButton btnLogin;
    private JButton btnCheckBalance;
    private JButton btnDeposit;
    private JButton btnWithdraw;
    private JButton btnTransfer;
    private JButton btnLogout;
    private JPanel panelActions;

    public BankClientGUI() {
        super("Banking Client");
        setupGUI();

        txtServerIP.setText("localhost");
        log("Sẵn sàng. Vui lòng nhập IP Server và Số tài khoản.");

        try {
            clientStub = (ClientCallback) UnicastRemoteObject.exportObject(this, 0);
            log("Client Callback đã sẵn sàng.");
        } catch (Exception e) {
            log("Lỗi tạo RMI Callback: " + e.getMessage());
        }

        addListeners();
    }

    @Override
    public void receiveNotification(String message) {
        log("CALLBACK: " + message);
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(BankClientGUI.this, message, "Thông báo từ Server", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(message + "\n");
        });
    }

    private void setActionsEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            btnCheckBalance.setEnabled(enabled);
            btnDeposit.setEnabled(enabled);
            btnWithdraw.setEnabled(enabled);
            btnTransfer.setEnabled(enabled);
            btnLogout.setEnabled(enabled);
        });
    }

    private void setupGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("TitlePane.background", new Color(0, 102, 204));
            UIManager.put("TitlePane.foreground", Color.WHITE);
            UIManager.put("TitlePane.buttonHoverBackground", new Color(0, 120, 240));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setTitle("HQ BANK - Ngân Hàng Số Tương Lai 2025");
        setSize(1000, 680);
        setMinimumSize(new Dimension(800, 550));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel background = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                GradientPaint gp = new GradientPaint(0, 0, new Color(10, 25, 70), getWidth(), getHeight(), new Color(0, 150, 100));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 30, 30);
                g2d.dispose();
            }
        };
        background.setLayout(new BorderLayout());
        background.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(background);

        Font fontTitle = new Font("Segoe UI", Font.BOLD, 38);
        Font fontLabel = new Font("Segoe UI", Font.BOLD, 15);
        Font fontField = new Font("Segoe UI", Font.PLAIN, 16);
        Font fontButton = new Font("Segoe UI", Font.BOLD, 17);
        Font fontLog = new Font("JetBrains Mono", Font.PLAIN, 14);
        Color primary = new Color(0, 150, 255);
        Color success = new Color(0, 200, 100);
        Color danger = new Color(255, 60, 80);
        Color textLight = new Color(240, 248, 255);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JLabel lblLogo = new JLabel("HQ");
        lblLogo.setFont(fontTitle);
        lblLogo.setForeground(Color.WHITE);
        header.add(lblLogo, BorderLayout.WEST);

        JPanel loginBox = new JPanel(new GridBagLayout());
        loginBox.setOpaque(false);
        loginBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2, true),
                BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));
        loginBox.setBackground(new Color(255, 255, 255, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblIP = new JLabel("Server IP:");
        lblIP.setFont(fontLabel);
        lblIP.setForeground(textLight);
        gbc.gridx = 0; gbc.gridy = 0;
        loginBox.add(lblIP, gbc);

        txtServerIP = createRoundedTextField("localhost");
        txtServerIP.setFont(fontField);
        gbc.gridx = 1; gbc.gridwidth = 2;
        loginBox.add(txtServerIP, gbc);
        gbc.gridwidth = 1;

        JLabel lblAcc = new JLabel("Số TK:");
        lblAcc.setFont(fontLabel);
        lblAcc.setForeground(textLight);
        gbc.gridx = 3; gbc.gridy = 0;
        loginBox.add(lblAcc, gbc);

        txtAccountLogin = createRoundedTextField("");
        txtAccountLogin.setFont(fontField);
        gbc.gridx = 4;
        loginBox.add(txtAccountLogin, gbc);

        btnLogin = createModernButton("ĐĂNG NHẬP", success, 180, 50);
        btnLogin.setFont(fontButton);
        gbc.gridx = 5; gbc.insets = new Insets(8, 20, 8, 0);
        loginBox.add(btnLogin, gbc);

        header.add(loginBox, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(fontLog);
        txtLog.setBackground(new Color(20, 30, 50));
        txtLog.setForeground(new Color(0, 255, 150));
        txtLog.setLineWrap(true);
        txtLog.setWrapStyleWord(true);

        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 200, 255), 2),
                " NHẬT KÝ GIAO DỊCH",
                0, 0, new Font("Segoe UI", Font.BOLD, 16), new Color(0, 200, 255)
        ));
        scrollLog.getViewport().setOpaque(false);
        add(scrollLog, BorderLayout.CENTER);

        // === ACTION PANEL - PHẢI ===
        panelActions = new JPanel();
        panelActions.setLayout(new GridBagLayout());
        panelActions.setOpaque(false);
        panelActions.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(15, 0, 15, 0);
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.anchor = GridBagConstraints.WEST;

        String[] btnTexts = {
                "Hiển thị số dư tài khoản",
                "Nạp tiền",
                "Rút tiền",
                "Chuyển khoản",
                "Đăng xuất"
        };

        Color[] btnColors = {primary, success, new Color(255, 165, 0), new Color(138, 43, 226), danger};
        JButton[] buttons = new JButton[5];

        for (int i = 0; i < btnTexts.length; i++) {
            buttons[i] = createModernButton(btnTexts[i], btnColors[i], 320, 62);
            buttons[i].setFont(fontButton);
            gbc2.gridy = i;
            panelActions.add(buttons[i], gbc2);
        }

        btnCheckBalance = buttons[0];
        btnDeposit = buttons[1];
        btnWithdraw = buttons[2];
        btnTransfer = buttons[3];
        btnLogout = buttons[4];

        panelActions.setVisible(false);
        add(panelActions, BorderLayout.EAST);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel lblFooter = new JLabel("© 2025 HQ Bank - RMI Java", SwingConstants.CENTER);
        lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblFooter.setForeground(new Color(200, 230, 255));
        footer.add(lblFooter, BorderLayout.CENTER);

        JLabel lblStatus = new JLabel("Sẵn sàng kết nối...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(150, 255, 150));
        footer.add(lblStatus, BorderLayout.WEST);

        add(footer, BorderLayout.SOUTH);
    }

    private JTextField createRoundedTextField(String text) {
        JTextField field = new JTextField(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setBackground(new Color(255, 255, 255, 180));
        field.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        field.setPreferredSize(new Dimension(160, 45));
        return field;
    }

    private JButton createModernButton(String text, Color color, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(color.brighter());
                } else {
                    g2.setColor(color);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, (getHeight() + fm.getAscent()) / 2 - 2);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(w, h));
        return btn;
    }

    private void addListeners() {
        btnLogin.addActionListener(e -> login());
        btnCheckBalance.addActionListener(e -> checkBalance());
        btnDeposit.addActionListener(e -> deposit());
        btnWithdraw.addActionListener(e -> withdraw());
        btnTransfer.addActionListener(e -> transfer());
        btnLogout.addActionListener(e -> logout());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (currentAccountRMI != null) {
                    JOptionPane.showMessageDialog(BankClientGUI.this,
                            "Bạn phải nhấn \"Đăng xuất\" trước khi thoát!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        if (clientStub != null) {
                            UnicastRemoteObject.unexportObject(BankClientGUI.this, true);
                        }
                    } catch (Exception ex) {}
                    System.exit(0);
                }
            }
        });
    }

    private void login() {
        String accNumStr = txtAccountLogin.getText().trim();
        String serverIP = txtServerIP.getText().trim();

        if (accNumStr.isEmpty() || serverIP.isEmpty()) {
            log("Lỗi: Vui lòng nhập Server IP và Số tài khoản.");
            return;
        }

        btnLogin.setEnabled(false);
        txtServerIP.setEditable(false);
        setActionsEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private int accNum;

            @Override
            protected Boolean doInBackground() throws Exception {
                accNum = Integer.parseInt(accNumStr);

                try {
                    registry = LocateRegistry.getRegistry(serverIP, 2204);
                    accountListRMI = (AccountList) registry.lookup("accounts");
                    notificationServiceRMI = (NotificationService) registry.lookup("notification");
                } catch (Exception e) {
                    throw new RuntimeException("Lỗi RMI: Không thể kết nối tới server " + serverIP + ". Kiểm tra IP và Tường lửa.", e);
                }

                boolean found = false;
                for (int a : accountListRMI.getAccounts()) {
                    if (a == accNum) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new RuntimeException("Số tài khoản " + accNum + " không tồn tại.");
                }

                currentAccountRMI = (Account) registry.lookup("act" + accNum);
                currentAccountRMI.login();
                notificationServiceRMI.registerCallback(accNum, clientStub);
                return true;
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        log("Kết nối RMI thành công đến " + serverIP);
                        log("Đăng nhập thành công tài khoản: " + accNum);
                        log("Tài khoản đã được KHÓA.");
                        log("Đã đăng ký nhận thông báo cho TK " + accNum);

                        panelActions.setVisible(true);
                        btnLogin.setText("Đã đăng nhập");
                        txtAccountLogin.setEditable(false);
                        setActionsEnabled(true);
                    }
                } catch (ExecutionException e) {
                    String errorMessage;
                    if (e.getCause() instanceof LoginException) {
                        errorMessage = "Lỗi đăng nhập: " + e.getCause().getMessage();
                    } else {
                        errorMessage = "Lỗi: " + e.getCause().getMessage();
                    }
                    log(errorMessage);
                    JOptionPane.showMessageDialog(BankClientGUI.this, errorMessage, "Lỗi", JOptionPane.ERROR_MESSAGE);

                    currentAccountRMI = null;
                    registry = null;
                    btnLogin.setEnabled(true);
                    txtServerIP.setEditable(true);
                } catch (Exception e) {
                    log("Lỗi đăng nhập: " + e.getMessage());
                    currentAccountRMI = null;
                    registry = null;
                    btnLogin.setEnabled(true);
                    txtServerIP.setEditable(true);
                }
            }
        };
        worker.execute();
    }

    private void logout() {
        log("Đang đăng xuất...");
        setActionsEnabled(false);
        btnLogin.setEnabled(false);

        SwingWorker<Void, Void> logoutWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (currentAccountRMI != null) {
                    currentAccountRMI.logout();
                    notificationServiceRMI.unregisterCallback(currentAccountRMI.getNumber(), clientStub);
                }
                return null;
            }

            @Override
            protected void done() {
                log("Đã đăng xuất. Tài khoản đã được mở khóa.");

                currentAccountRMI = null;
                registry = null;
                panelActions.setVisible(false);

                txtAccountLogin.setEditable(true);
                txtAccountLogin.setText("");

                txtServerIP.setEditable(true);

                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
            }
        };
        logoutWorker.execute();
    }

    private void checkBalance() {
        setActionsEnabled(false);
        log("Đang lấy thông tin số dư tài khoản...");
        SwingWorker<Double, Void> worker = new SwingWorker<>() {
            @Override
            protected Double doInBackground() throws Exception {
                return currentAccountRMI.getBalance();
            }
            @Override
            protected void done() {
                try {
                    double balance = get();
                    log("SỐ DƯ: " + balance);
                } catch (Exception e) {
                    log("Lỗi lấy thông tin số dư: " + e.getCause().getMessage());
                }
                setActionsEnabled(true);
            }
        };
        worker.execute();
    }

    private void deposit() {
        String amountStr = JOptionPane.showInputDialog(this, "Nhập số tiền cần nạp:");
        if (amountStr == null) return;
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException nfe) {
            log("Lỗi: Số tiền không hợp lệ.");
            return;
        }
        setActionsEnabled(false);
        log("Đang nạp tiền " + amount + "...");
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return currentAccountRMI.deposit(amount);
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        log("Nạp tiền thành công.");
                    } else {
                        log("Nạp tiền thất bại.");
                    }
                } catch (Exception e) {
                    log("Lỗi RMI khi nạp tiền: " + e.getCause().getMessage());
                }
                setActionsEnabled(true);
            }
        };
        worker.execute();
    }

    private void withdraw() {
        String amountStr = JOptionPane.showInputDialog(this, "Nhập số tiền cần rút:");
        if (amountStr == null) return;
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException nfe) {
            log("Lỗi: Số tiền không hợp lệ.");
            return;
        }
        setActionsEnabled(false);
        log("Đang rút tiền " + amount + "...");
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return currentAccountRMI.withdraw(amount);
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        log("Rút tiền thành công.");
                    } else {
                        log("Rút tiền thất bại (Không đủ số dư?).");
                    }
                } catch (Exception e) {
                    log("Lỗi RMI khi rút tiền: " + e.getCause().getMessage());
                }
                setActionsEnabled(true);
            }
        };
        worker.execute();
    }

    private void transfer() {
        JTextField txtDestAccount = new JTextField(10);
        JTextField txtAmount = new JTextField(10);
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Số tài khoản nhận:"));
        panel.add(txtDestAccount);
        panel.add(new JLabel("Số tiền:"));
        panel.add(txtAmount);
        int result = JOptionPane.showConfirmDialog(this, panel, "Chuyển khoản",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            int destAcc;
            double amount;
            try {
                destAcc = Integer.parseInt(txtDestAccount.getText());
                amount = Double.parseDouble(txtAmount.getText());
            } catch (NumberFormatException nfe) {
                log("Lỗi: Số tài khoản hoặc số tiền không hợp lệ.");
                return;
            }
            setActionsEnabled(false);
            log("Đang chuyển " + amount + " đến TK " + destAcc + "...");
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return currentAccountRMI.transfer(destAcc, amount);
                }
                @Override
                protected void done() {
                    try {
                        if (get()) {
                            log("Chuyển khoản thành công.");
                        } else {
                            log("Chuyển khoản thất bại (TK đích, số dư?).");
                        }
                    } catch (Exception e) {
                        log("Lỗi RMI khi chuyển khoản: " + e.getCause().getMessage());
                    }
                    setActionsEnabled(true);
                }
            };
            worker.execute();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BankClientGUI().setVisible(true);
        });
    }
}