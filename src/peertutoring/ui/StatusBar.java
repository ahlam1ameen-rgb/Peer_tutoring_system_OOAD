package peertutoring.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import java.awt.Color;

public class StatusBar extends JLabel {

    public StatusBar() {
        super(" ");
        setOpaque(true);
        setBackground(Color.WHITE);
        setFont(Theme.SMALL);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
    }

    public void success(String message) {
        setForeground(Theme.GREEN);
        setText("✓ " + message);
    }

    public void error(String message) {
        setForeground(Theme.RED);
        setText("⚠ " + message);
    }

    public void info(String message) {
        setForeground(Theme.TEXT_GREY);
        setText(message);
    }
}
