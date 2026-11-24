package peertutoring.ui;

import peertutoring.data.DataStore;
import peertutoring.model.Course;
import peertutoring.model.Session;
import peertutoring.model.SessionStatus;
import peertutoring.model.TimeSlot;
import peertutoring.model.User;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

public class SessionTableModel extends AbstractTableModel {

    private final DataStore store;
    private final boolean forStudent;
    private List<Session> sessions = new ArrayList<>();

    public SessionTableModel(DataStore store, boolean forStudent) {
        this.store = store;
        this.forStudent = forStudent;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
        fireTableDataChanged();
    }

    public Session getSessionAt(int row) {
        return row >= 0 && row < sessions.size() ? sessions.get(row) : null;
    }

    @Override
    public int getRowCount() { return sessions.size(); }

    @Override
    public int getColumnCount() { return 5; }

    @Override
    public String getColumnName(int column) {
        String[] names = {"Course", forStudent ? "Tutor" : "Student", "Date & time", "Status", "Rating"};
        return names[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        Session s = sessions.get(row);
        switch (column) {
            case 0:
                Course course = store.findCourse(s.getCourseCode());
                return course == null ? s.getCourseCode() : course.getCourseName();
            case 1:
                User other = store.findUser(forStudent ? s.getTutorId() : s.getStudentId());
                return other == null ? "?" : other.getName();
            case 2:
                TimeSlot slot = store.findSlot(s.getSlotId());
                return slot == null ? "" : slot.toString();
            case 3:
                return s.getStatus();
            default:
                if (s.hasFeedback()) {
                    int r = s.getFeedback().getRating();
                    return "★".repeat(r) + "☆".repeat(5 - r);
                }
                return s.getStatus() == SessionStatus.COMPLETED ? "not rated yet" : "";
        }
    }

    public static void style(JTable table) {
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xEE, 0xEE, 0xEE));
        table.setSelectionBackground(Theme.GREEN_LIGHT);
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(Theme.BOLD);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, f, r, c);
                l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                return l;
            }
        });
        int[] widths = {210, 150, 260, 110, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                setForeground(String.valueOf(v).startsWith("★") ? Theme.STAR : Theme.TEXT_GREY);
                return this;
            }
        });
    }

    static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            label.setFont(Theme.BOLD);
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            SessionStatus status = (SessionStatus) value;
            switch (status) {
                case PENDING:   label.setForeground(new Color(0x8A, 0x62, 0x00)); break;
                case ACCEPTED:  label.setForeground(new Color(0x0B, 0x53, 0x94)); break;
                case COMPLETED: label.setForeground(Theme.GREEN); break;
                default:        label.setForeground(Theme.RED);
            }
            return label;
        }
    }
}
