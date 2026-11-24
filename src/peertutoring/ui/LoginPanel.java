package peertutoring.ui;

import peertutoring.controller.ValidationException;
import peertutoring.model.User;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.net.URL;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

 
public class LoginPanel extends JPanel {

    private final AppContext app;
    private final MainFrame frame;
    private final JTextField idField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel errorLabel = Theme.label(" ", Theme.SMALL, Theme.RED);

    public LoginPanel(AppContext app, MainFrame frame) {
        super(new BorderLayout());
        this.app = app;
        this.frame = frame;
        add(buildWelcomeSide(), BorderLayout.WEST);
        add(buildFormSide(), BorderLayout.CENTER);
    }

    private JPanel buildWelcomeSide() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.GREEN);
        side.setPreferredSize(new Dimension(430, 0));
        side.setBorder(BorderFactory.createEmptyBorder(80, 48, 40, 40));

        JPanel logo = buildLogo();

        JLabel title = Theme.label("<html>Peer Tutoring<br>System</html>",
                new Font("SansSerif", Font.BOLD, 28), Color.WHITE);
        JLabel text = Theme.label("<html>Find a tutor for your course, book a session, "
                + "and rate your experience.</html>", new Font("SansSerif", Font.PLAIN, 15), Color.WHITE);

        for (javax.swing.JComponent c : new javax.swing.JComponent[] {logo, title, text}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        side.add(logo);
        side.add(Box.createVerticalStrut(28));
        side.add(title);
        side.add(Box.createVerticalStrut(16));
        side.add(text);
        return side;
    }

    private JPanel buildLogo() {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 8, 14));
        URL url = LoginPanel.class.getResource("aau_logo.png");
        File file = new File("src/peertutoring/ui/aau_logo.png");
        ImageIcon icon = null;
        if (url != null) {
            icon = new ImageIcon(url);
        } else if (file.exists()) {
            icon = new ImageIcon(file.getPath());
        }
        JLabel image;
        if (icon != null) {
            Image scaled = icon.getImage().getScaledInstance(-1, 100, Image.SCALE_SMOOTH);
            image = new JLabel(new ImageIcon(scaled));
        } else {
            image = Theme.label("AAU", new Font("SansSerif", Font.BOLD, 26), Theme.GREEN);
        }
        card.add(image);
        Dimension size = card.getPreferredSize();
        card.setMaximumSize(size);
        return card;
    }

    private JPanel buildFormSide() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        JButton loginButton = Theme.button("Log in", Theme.ButtonKind.PRIMARY);
        loginButton.addActionListener(e -> login());
        passwordField.addActionListener(e -> login()); 

        JLabel registerLink = Theme.label("<html>New user? <b><font color='#196B24'>Create an account</font></b></html>",
                Theme.NORMAL, Theme.TEXT_GREY);
        registerLink.setHorizontalAlignment(JLabel.CENTER);
        registerLink.setMaximumSize(new Dimension(320, 24));
        registerLink.setPreferredSize(new Dimension(320, 24));
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new RegisterDialog(frame, app).setVisible(true);
            }
        });

        for (JTextField field : new JTextField[] {idField, passwordField}) {
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xBB, 0xBB, 0xBB), 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        }

        Component[] items = {
            Theme.label("Log in", new Font("SansSerif", Font.BOLD, 26), Color.BLACK),
            Theme.label("Use your university ID and password", Theme.NORMAL, Theme.TEXT_GREY),
            Box.createVerticalStrut(20),
            Theme.label("University ID", Theme.BOLD, Theme.TEXT_GREY), idField,
            Box.createVerticalStrut(12),
            Theme.label("Password", Theme.BOLD, Theme.TEXT_GREY), passwordField,
            Box.createVerticalStrut(8), errorLabel,
            Box.createVerticalStrut(10), loginButton,
            Box.createVerticalStrut(16), registerLink,
        };
        for (Component c : items) {
            ((javax.swing.JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
            if (c instanceof JTextField || c instanceof JButton) {
                c.setMaximumSize(new Dimension(320, 40));
                c.setPreferredSize(new Dimension(320, 40));
            }
            form.add(c);
        }

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.add(form);
        return wrapper;
    }

    private void login() {
        try {
            User user = app.auth.login(idField.getText(), new String(passwordField.getPassword()));
            frame.showHomeFor(user);
        } catch (ValidationException ex) {
            errorLabel.setText("⚠ " + ex.getMessage());
            passwordField.setText("");
        }
    }
}
