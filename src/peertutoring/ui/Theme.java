package peertutoring.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Enumeration;


public final class Theme {

    public static final Color GREEN = new Color(0x19, 0x6B, 0x24);
    public static final Color GREEN_LIGHT = new Color(0xD8, 0xEC, 0xDC);
    public static final Color RED = new Color(0xB3, 0x26, 0x1E);
    public static final Color BACKGROUND = new Color(0xF5, 0xF7, 0xF5);
    public static final Color BORDER = new Color(0xDC, 0xDC, 0xDC);
    public static final Color TEXT_GREY = new Color(0x66, 0x66, 0x66);
    public static final Color STAR = new Color(0xE0, 0xA1, 0x00);

    public static final Font NORMAL = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 20);
    public static final Font SMALL = new Font("SansSerif", Font.PLAIN, 12);

    public enum ButtonKind { PRIMARY, SECONDARY, DANGER }

    private Theme() { }

    public static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, new FontUIResource(NORMAL));
            }
        }
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("TabbedPane.selected", Color.WHITE);
    }

    public static JButton button(String text, ButtonKind kind) {
        Color bg;
        Color fg;
        Color line;
        switch (kind) {
            case PRIMARY:
                bg = GREEN; fg = Color.WHITE; line = GREEN;
                break;
            case DANGER:
                bg = Color.WHITE; fg = RED; line = RED;
                break;
            default:
                bg = Color.WHITE; fg = GREEN; line = GREEN;
        }
        return new RoundButton(text, bg, fg, line);
    }

    public static String stars(double rating, int count) {
        if (count == 0) {
            return "No ratings yet";
        }
        int full = (int) Math.round(rating);
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= full ? '★' : '☆');
        }
        return sb + String.format("  %.1f", rating);
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14));
    }

    private static class RoundButton extends JButton {
        private final Color bg;
        private final Color line;

        RoundButton(String text, Color bg, Color fg, Color line) {
            super(text);
            this.bg = bg;
            this.line = line;
            setForeground(fg);
            setFont(BOLD);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = bg;
            if (!isEnabled()) {
                fill = new Color(0xE6, 0xE6, 0xE6);
            } else if (getModel().isPressed()) {
                fill = bg.darker();
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.setColor(isEnabled() ? line : new Color(0xC8, 0xC8, 0xC8));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, Math.max(d.height, 34));
        }
    }
}
