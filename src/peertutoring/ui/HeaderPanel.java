package peertutoring.ui;

import peertutoring.model.Tutor;
import peertutoring.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

public class HeaderPanel extends JPanel {

    private final JLabel userLabel = new JLabel();
    private final User user;

    public HeaderPanel(User user, MainFrame frame) {
        super(new BorderLayout());
        this.user = user;
        setBackground(Theme.GREEN);
        setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 22));

        JLabel title = Theme.label("  AAU  Peer Tutoring", new Font("SansSerif", Font.BOLD, 19), Color.WHITE);
        add(title, BorderLayout.WEST);

        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(Theme.NORMAL);
        refresh();

        JButton logout = Theme.button("Log out", Theme.ButtonKind.SECONDARY);
        logout.addActionListener(e -> frame.showLogin());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);
        right.add(userLabel);
        right.add(logout);
        add(right, BorderLayout.EAST);
    }

    // Updates the user text
    public void refresh() {
        String role = user.getRole().charAt(0) + user.getRole().substring(1).toLowerCase(); // "Tutor"
        String text = user.getName() + " (" + role + ")";
        if (user instanceof Tutor && ((Tutor) user).getRatingCount() > 0) {
            text += String.format("   ★ %.1f", ((Tutor) user).getAverageRating());
        }
        userLabel.setText(text);
    }
}
