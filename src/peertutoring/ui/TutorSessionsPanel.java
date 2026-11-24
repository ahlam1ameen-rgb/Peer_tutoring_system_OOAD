package peertutoring.ui;

import peertutoring.controller.ValidationException;
import peertutoring.model.Session;
import peertutoring.model.SessionStatus;
import peertutoring.model.Tutor;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.List;

public class TutorSessionsPanel extends JPanel {

    private final AppContext app;
    private final Tutor tutor;
    private final StatusBar status;
    private final SessionTableModel model;
    private final JTable table;
    private final JLabel pendingLabel = Theme.label(" ", Theme.BOLD, new java.awt.Color(0x8A, 0x62, 0x00));
    private final JButton acceptButton = Theme.button("Accept", Theme.ButtonKind.PRIMARY);
    private final JButton rejectButton = Theme.button("Reject", Theme.ButtonKind.DANGER);
    private final JButton completeButton = Theme.button("Mark Completed", Theme.ButtonKind.SECONDARY);
    private final JButton cancelButton = Theme.button("Cancel Session", Theme.ButtonKind.DANGER);

    public TutorSessionsPanel(AppContext app, Tutor tutor, StatusBar status) {
        super(new BorderLayout(0, 12));
        this.app = app;
        this.tutor = tutor;
        this.status = status;
        setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        top.add(Theme.label("Booking requests and sessions", Theme.BOLD, java.awt.Color.BLACK));
        top.add(pendingLabel);
        add(top, BorderLayout.NORTH);

        model = new SessionTableModel(app.store, false);
        table = new JTable(model);
        SessionTableModel.style(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        add(new JScrollPane(table), BorderLayout.CENTER);


        acceptButton.addActionListener(e -> perform("accept"));
        rejectButton.addActionListener(e -> perform("reject"));
        completeButton.addActionListener(e -> perform("complete"));
        cancelButton.addActionListener(e -> perform("cancel"));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.add(Theme.label("Select a row, then:", Theme.SMALL, Theme.TEXT_GREY));
        buttons.add(acceptButton);
        buttons.add(rejectButton);
        buttons.add(completeButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        List<Session> sessions = app.booking.getSessionsOf(tutor);
        model.setSessions(sessions);
        int pending = 0;
        for (Session s : sessions) {
            if (s.getStatus() == SessionStatus.PENDING) {
                pending++;
            }
        }
        pendingLabel.setText(pending == 0 ? "" : "  " + pending + " pending request" + (pending > 1 ? "s" : ""));
        updateButtons();
    }

    private Session selected() {
        return model.getSessionAt(table.getSelectedRow());
    }


    private void updateButtons() {
        Session s = selected();
        SessionStatus st = s == null ? null : s.getStatus();
        acceptButton.setEnabled(st == SessionStatus.PENDING);
        rejectButton.setEnabled(st == SessionStatus.PENDING);
        completeButton.setEnabled(st == SessionStatus.ACCEPTED);
        cancelButton.setEnabled(st == SessionStatus.ACCEPTED);
    }


    private void perform(String action) {
        Session s = selected();
        if (s == null) {
            return;
        }
        try {
            if (action.equals("accept")) {
                app.booking.acceptSession(tutor, s);
                status.success("Request accepted.");
            } else if (action.equals("reject")) {
                app.booking.rejectSession(tutor, s);
                status.success("Request rejected. The time slot is free again.");
            } else if (action.equals("complete")) {
                app.booking.completeSession(tutor, s);
                status.success("Session marked as completed. The student can now rate it.");
            } else if (action.equals("cancel")) {
                int answer = JOptionPane.showConfirmDialog(this, "Do you really want to cancel this session?",
                        "Cancel session", JOptionPane.YES_NO_OPTION);
                if (answer != JOptionPane.YES_OPTION) {
                    return;
                }
                app.booking.cancelSession(tutor, s);
                status.success("Session cancelled. The time slot is free again.");
            }
        } catch (ValidationException ex) {
            status.error(ex.getMessage());
        } catch (IOException ex) {
            status.error("Could not save: " + ex.getMessage());
        }
        refresh();
    }
}