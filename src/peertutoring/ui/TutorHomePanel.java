package peertutoring.ui;

import peertutoring.model.Tutor;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

public class TutorHomePanel extends JPanel {

    private final JTabbedPane tabs = new JTabbedPane();

    public TutorHomePanel(AppContext app, MainFrame frame, Tutor tutor) {
        super(new BorderLayout());
        StatusBar status = new StatusBar();
        HeaderPanel header = new HeaderPanel(tutor, frame);
        TutorSessionsPanel sessions = new TutorSessionsPanel(app, tutor, status);
        CoursesAvailabilityPanel availability = new CoursesAvailabilityPanel(app, tutor, status);

        tabs.setFont(Theme.BOLD);
        tabs.addTab("  My Sessions  ", sessions);
        tabs.addTab("  My Courses & Availability  ", availability);
        tabs.addChangeListener(e -> {
            sessions.refresh();
            availability.refresh();
            header.refresh();
        });

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        status.info("Select a request or session to manage it.");
    }
}