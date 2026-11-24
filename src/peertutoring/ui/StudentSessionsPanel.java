package peertutoring.ui;

import peertutoring.controller.ValidationException;
import peertutoring.model.Session;
import peertutoring.model.SessionStatus;
import peertutoring.model.Student;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;

public class StudentSessionsPanel extends JPanel {

    private final AppContext app;
    private final Student student;
    private final StatusBar status;
    private final SessionTableModel model;
    private final JTable table;
    private final JButton cancelButton = Theme.button("Cancel Session", Theme.ButtonKind.DANGER);
    private final JButton rateButton = Theme.button("Rate Tutor", Theme.ButtonKind.PRIMARY);

    public StudentSessionsPanel(AppContext app, Student student, StatusBar status) {
        super(new BorderLayout(0, 12));
        this.app = app;
        this.student = student;
        this.status = status;
        setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        model = new SessionTableModel(app.store, true);
        table = new JTable(model);
        SessionTableModel.style(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        add(new JScrollPane(table), BorderLayout.CENTER);

        cancelButton.addActionListener(e -> cancelSelected());
        rateButton.addActionListener(e -> rateSelected());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.add(Theme.label("Select a session, then:", Theme.SMALL, Theme.TEXT_GREY));
        buttons.add(cancelButton);
        buttons.add(rateButton);
        add(buttons, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        model.setSessions(app.booking.getSessionsOf(student));
        updateButtons();
    }

    private Session selected() {
        return model.getSessionAt(table.getSelectedRow());
    }

    private void updateButtons() {
        Session s = selected();
        cancelButton.setEnabled(s != null
                && (s.getStatus() == SessionStatus.PENDING || s.getStatus() == SessionStatus.ACCEPTED));
        rateButton.setEnabled(s != null && app.feedback.canRate(student, s));
    }

    private void cancelSelected() {
        Session s = selected();
        int answer = JOptionPane.showConfirmDialog(this, "Do you really want to cancel this session?",
                "Cancel session", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            app.booking.cancelSession(student, s);
            status.success("Session cancelled. The time slot is free again.");
        } catch (ValidationException ex) {
            status.error(ex.getMessage());
        } catch (IOException ex) {
            status.error("Could not save: " + ex.getMessage());
        }
        refresh();
    }

    private void rateSelected() {
        Session s = selected();
        RatingDialog dialog = new RatingDialog(this, app, s);
        dialog.setVisible(true);
        if (dialog.getRating() == 0) {
            return; 
        }
        try {
            app.feedback.submitFeedback(student, s, dialog.getRating(), dialog.getComment());
            status.success("Thank you for your feedback!");
        } catch (ValidationException ex) {
            status.error(ex.getMessage());
        } catch (IOException ex) {
            status.error("Could not save: " + ex.getMessage());
        }
        refresh();
    }
}
