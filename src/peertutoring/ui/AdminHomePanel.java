package peertutoring.ui;

import peertutoring.model.Admin;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

// admin manage accounts screen 
 
public class AdminHomePanel extends JPanel {

    public AdminHomePanel(AppContext app, MainFrame frame, Admin admin) {
        super(new BorderLayout());
        StatusBar status = new StatusBar();
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.BOLD);
        tabs.addTab("  Manage Accounts  ", new ManageAccountsPanel(app, status));

        add(new HeaderPanel(admin, frame), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        status.info("Select an account to deactivate or reactivate it.");
    }
}
