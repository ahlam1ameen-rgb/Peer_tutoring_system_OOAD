package peertutoring.ui;

import peertutoring.model.Course;
import peertutoring.model.Session;
import peertutoring.model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;


public class RatingDialog extends JDialog {

    private final JButton[] stars = new JButton[5];
    private final JTextArea commentArea = new JTextArea(4, 30);
    private final JLabel errorLabel = Theme.label(" ", Theme.SMALL, Theme.RED);
    private int chosen = 0; 
    private int result = 0;

    public RatingDialog(Component parent, AppContext app, Session session) {
        super((Window) SwingUtilities.getWindowAncestor(parent), "Rate your session", ModalityType.APPLICATION_MODAL);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        User tutor = app.store.findUser(session.getTutorId());
        Course course = app.store.findCourse(session.getCourseCode());
        root.add(Theme.label("Rate your session", Theme.TITLE, Color.BLACK));
        root.add(Theme.label((course == null ? "" : course.getCourseName()) + " with " + tutor.getName(),
                Theme.NORMAL, Theme.TEXT_GREY));
        root.add(Box.createVerticalStrut(14));
        root.add(Theme.label("Rating", Theme.BOLD, Theme.TEXT_GREY));

        JPanel starRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        starRow.setOpaque(false);
        for (int i = 0; i < 5; i++) {
            final int value = i + 1;
            JButton star = new JButton("★");
            star.setFont(new Font("SansSerif", Font.PLAIN, 34));
            star.setContentAreaFilled(false);
            star.setBorderPainted(false);
            star.setFocusPainted(false);
            star.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            star.addActionListener(e -> setStars(value));
            stars[i] = star;
            starRow.add(star);
        }
        setStars(0);
        root.add(starRow);
        root.add(Box.createVerticalStrut(8));

        root.add(Theme.label("Feedback (optional)", Theme.BOLD, Theme.TEXT_GREY));
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane commentScroll = new JScrollPane(commentArea);
        root.add(commentScroll);
        root.add(Box.createVerticalStrut(6));
        root.add(errorLabel);

        JButton cancel = Theme.button("Cancel", Theme.ButtonKind.SECONDARY);
        JButton submit = Theme.button("Submit", Theme.ButtonKind.PRIMARY);
        cancel.addActionListener(e -> dispose());
        submit.addActionListener(e -> submit());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(submit);
        root.add(Box.createVerticalStrut(8));
        root.add(buttons);

        for (Component c : root.getComponents()) {
            ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        setContentPane(new JPanel(new BorderLayout()));
        getContentPane().add(root);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    void setStars(int n) {
        chosen = n;
        for (int i = 0; i < 5; i++) {
            stars[i].setForeground(i < n ? Theme.STAR : new Color(0xCC, 0xCC, 0xCC));
        }
    }

    private void submit() {
        if (chosen == 0) {
            errorLabel.setText("⚠ Please choose a rating from 1 to 5 stars.");
            return;
        }
        result = chosen;
        dispose();
    }

    public int getRating() { return result; }
    public String getComment() { return commentArea.getText(); }
}
