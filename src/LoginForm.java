import javax.swing.*;
import java.awt.*;

/** Form 1: Login. */
public class LoginForm extends JFrame {
    private final JTextField tfUser = new JTextField(18);
    private final JPasswordField pfPass = new JPasswordField(18);

    public LoginForm() {
        super("Student Management System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(UI.header("Student Management System"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 6, 8, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Username:"), g);
        g.gridx = 1; form.add(tfUser, g);
        g.gridx = 0; g.gridy = 1; form.add(new JLabel("Password:"), g);
        g.gridx = 1; form.add(pfPass, g);

        JLabel hint = new JLabel("Default login:  admin / admin123");
        hint.setForeground(Color.GRAY);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2; g.anchor = GridBagConstraints.CENTER;
        form.add(hint, g);
        add(form, BorderLayout.CENTER);

        JButton btnLogin = UI.button("Login", UI.SUCCESS);
        JButton btnClear = UI.button("Clear", UI.WARNING);
        JButton btnExit = UI.button("Exit", UI.DANGER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        buttons.add(btnLogin);
        buttons.add(btnClear);
        buttons.add(btnExit);
        add(buttons, BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> doLogin());
        btnClear.addActionListener(e -> {
            tfUser.setText("");
            pfPass.setText("");
            tfUser.requestFocus();
        });
        btnExit.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Exit the application?", "Exit",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) System.exit(0);
        });
        getRootPane().setDefaultButton(btnLogin); // ENTER key = Login

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        String username = tfUser.getText().trim();
        String password = new String(pfPass.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            UI.error(this, "Please enter both username and password.");
            return;
        }
        User user = DataStore.get().authenticate(username, password);
        if (user == null) {
            UI.error(this, "Invalid username or password.");
            pfPass.setText("");
            pfPass.requestFocus();
            return;
        }
        dispose();
        new Dashboard(user).setVisible(true);
    }
}
