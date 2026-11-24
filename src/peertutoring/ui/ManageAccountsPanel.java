package peertutoring.ui;

import peertutoring.controller.ValidationException;
import peertutoring.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//admin Manage accounts screen 
 
public class ManageAccountsPanel extends JPanel {

    private final AppContext app;
    private final StatusBar status;
    private final AccountTableModel model = new AccountTableModel();
    private final JTable table = new JTable(model);
    private final JButton deactivateButton = Theme.button("Deactivate", Theme.ButtonKind.DANGER);
    private final JButton reactivateButton = Theme.button("Reactivate", Theme.ButtonKind.SECONDARY);

    public ManageAccountsPanel(AppContext app, StatusBar status) {
        super(new BorderLayout(0, 12));
        this.app = app;
        this.status = status;
        setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(Theme.GREEN_LIGHT);
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(Theme.BOLD);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, f, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                setFont(c == 3 ? Theme.BOLD : Theme.NORMAL);
                if (c == 3) {
                    setForeground("ACTIVE".equals(v) ? Theme.GREEN : Theme.RED);
                } else {
                    setForeground(Color.BLACK);
                }
                return this;
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        add(new JScrollPane(table), BorderLayout.CENTER);

        deactivateButton.addActionListener(e -> deactivateSelected());
        reactivateButton.addActionListener(e -> reactivateSelected());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.add(Theme.label("Select an account, then:", Theme.SMALL, Theme.TEXT_GREY));
        buttons.add(reactivateButton);
        buttons.add(deactivateButton);
        add(buttons, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        int row = table.getSelectedRow();
        model.setUsers(app.admin.getAccounts());
        if (row >= 0 && row < model.getRowCount()) {
            table.setRowSelectionInterval(row, row);
        }
        updateButtons();
    }

    private void updateButtons() {
        User u = model.getUserAt(table.getSelectedRow());
        deactivateButton.setEnabled(u != null && u.isActive());
        reactivateButton.setEnabled(u != null && !u.isActive());
    }

    private void deactivateSelected() {
        User user = model.getUserAt(table.getSelectedRow());
        int answer = JOptionPane.showConfirmDialog(this,
                "Deactivate " + user.getName() + "?\nThey will not be able to log in, and their upcoming sessions will be cancelled.",
                "Deactivate account", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int cancelled = app.admin.deactivateUser(user);
            status.success(user.getName() + " deactivated. " + cancelled
                    + (cancelled == 1 ? " upcoming session was" : " upcoming sessions were") + " cancelled.");
        } catch (ValidationException ex) {
            status.error(ex.getMessage());
        } catch (IOException ex) {
            status.error("Could not save: " + ex.getMessage());
        }
        refresh();
    }

    private void reactivateSelected() {
        User user = model.getUserAt(table.getSelectedRow());
        try {
            app.admin.reactivateUser(user);
            status.success(user.getName() + " reactivated and can log in again.");
        } catch (ValidationException ex) {
            status.error(ex.getMessage());
        } catch (IOException ex) {
            status.error("Could not save: " + ex.getMessage());
        }
        refresh();
    }

    private static class AccountTableModel extends AbstractTableModel {
        private List<User> users = new ArrayList<>();

        void setUsers(List<User> users) {
            this.users = users;
            fireTableDataChanged();
        }

        User getUserAt(int row) {
            return row >= 0 && row < users.size() ? users.get(row) : null;
        }

        @Override public int getRowCount() { return users.size(); }
        @Override public int getColumnCount() { return 4; }

        @Override
        public String getColumnName(int column) {
            return new String[] {"University ID", "Name", "Role", "Status"}[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            User u = users.get(row);
            switch (column) {
                case 0: return u.getUserId();
                case 1: return u.getName();
                case 2: return u.getRole().equals("TUTOR") ? "Tutor" : "Student";
                default: return u.isActive() ? "ACTIVE" : "DEACTIVATED";
            }
        }
    }
}
