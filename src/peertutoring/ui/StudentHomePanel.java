package peertutoring.ui;

import peertutoring.model.Student;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

public class StudentHomePanel extends JPanel {

    private final JTabbedPane tabs = new JTabbedPane();

    public StudentHomePanel(AppContext app, MainFrame frame, Student student) {
        super(new BorderLayout());
        StatusBar status = new StatusBar();
        FindTutorPanel findTutor = new FindTutorPanel(app, student, status);
        StudentSessionsPanel sessions = new StudentSessionsPanel(app, student, status);

        tabs.setFont(Theme.BOLD);
        tabs.addTab("  Find a Tutor  ", findTutor);
        tabs.addTab("  My Sessions  ", sessions);

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == sessions) {
                sessions.refresh();
            } else {
                findTutor.refresh();
            }
        });
        add(new HeaderPanel(student, frame), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        status.info("Search for a tutor by tutor name, course code, or course name.");
    }
}