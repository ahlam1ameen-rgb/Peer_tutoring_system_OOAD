package peertutoring.ui;

import peertutoring.model.Admin;
import peertutoring.model.Student;
import peertutoring.model.Tutor;
import peertutoring.model.User;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;

public class MainFrame extends JFrame {

    private final AppContext app;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);

    public MainFrame(AppContext app) {
        super("AAU Peer Tutoring");
        this.app = app;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 620));
        setContentPane(content);
        showLogin();
        setLocationRelativeTo(null);
    }

    public void showLogin() {
        content.removeAll();
        content.add(new LoginPanel(app, this), "login");
        cards.show(content, "login");
        content.revalidate();
        content.repaint();
    }

    public void showHomeFor(User user) {
        content.removeAll();
        if (user instanceof Student) {
            content.add(new StudentHomePanel(app, this, (Student) user), "home");
        } else if (user instanceof Admin) {
            content.add(new AdminHomePanel(app, this, (Admin) user), "home");
        } else {
            content.add(new TutorHomePanel(app, this, (Tutor) user), "home");
        }
        cards.show(content, "home");
        content.revalidate();
        content.repaint();
    }
}
