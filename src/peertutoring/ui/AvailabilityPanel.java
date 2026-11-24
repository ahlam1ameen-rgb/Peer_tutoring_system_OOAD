package peertutoring.ui;

import peertutoring.controller.ValidationException;
import peertutoring.model.TimeSlot;
import peertutoring.model.Tutor;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//The time slot part of My Courses & Availability screen

public class AvailabilityPanel extends JPanel {

    private final AppContext app;
    private final Tutor tutor;
    private final StatusBar status;
    private final JTextField dateField = new JTextField(LocalDate.now().plusDays(1).toString(), 10);
    private final JTextField startField = new JTextField("10:00", 6);
    private final JTextField endField = new JTextField("11:00", 6);
    private final SlotTableModel model = new SlotTableModel();
    private final JTable table = new JTable(model);
    private final JButton removeButton = Theme.button("Remove Slot", Theme.ButtonKind.DANGER);

    public AvailabilityPanel(AppContext app, Tutor tutor, StatusBar status) {
        super(new BorderLayout(0, 12));
        this.app = app;
        this.tutor = tutor;
        this.status = status;
        setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        //form to add a slot
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        form.setBackground(Color.WHITE);
        form.setBorder(Theme.cardBorder());
        form.add(field("Date (yyyy-MM-dd)", dateField));
        form.add(field("Start (HH:mm)", startField));
        form.add(field("End (HH:mm)", endField));
        JButton addButton = Theme.button("Add Slot", Theme.ButtonKind.PRIMARY);
        addButton.addActionListener(e -> addSlot());
        form.add(addButton);
        form.add(Theme.label("<html>Slots cannot overlap.<br>Reserved slots cannot be removed.</html>",
                Theme.SMALL, Theme.TEXT_GREY));
        add(form, BorderLayout.NORTH);

        //table of slots
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
                return this;
            }
        });
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, f, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                setFont(Theme.BOLD);
                setForeground("FREE".equals(v) ? Theme.GREEN : new Color(0x8A, 0x62, 0x00));
                return this;
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        add(new JScrollPane(table), BorderLayout.CENTER);

        removeButton.addActionListener(e -> removeSelected());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.add(Theme.label("Select a free slot, then:", Theme.SMALL, Theme.TEXT_GREY));
        buttons.add(removeButton);
        add(buttons, BorderLayout.SOUTH);

        refresh();
    }

    private JPanel field(String label, JTextField field) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        p.add(Theme.label(label, Theme.BOLD, Theme.TEXT_GREY));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 32));
        p.add(field);
        return p;
    }

    public void refresh() {
        model.setSlots(app.availability.getUpcomingSlots(tutor));
        updateButtons();
    }

    private void updateButtons() {
        TimeSlot s = model.getSlotAt(table.getSelectedRow());
        removeButton.setEnabled(s != null && !s.isReserved());
    }

    private void addSlot() {
        try {
            TimeSlot slot = app.availability.addSlot(tutor, dateField.getText(), startField.getText(), endField.getText());
            status.success("Time slot added: " + slot);
        } catch (ValidationException ex) {
            status.error(ex.getMessage());
        } catch (IOException ex) {
            status.error("Could not save: " + ex.getMessage());
        }
        refresh();
    }

    private void removeSelected() {
        TimeSlot slot = model.getSlotAt(table.getSelectedRow());
        try {
            app.availability.removeSlot(tutor, slot);
            status.success("Time slot removed.");
        } catch (ValidationException ex) {
            status.error(ex.getMessage());
        } catch (IOException ex) {
            status.error("Could not save: " + ex.getMessage());
        }
        refresh();
    }


    private static class SlotTableModel extends AbstractTableModel {
        private List<TimeSlot> slots = new ArrayList<>();

        void setSlots(List<TimeSlot> slots) {
            this.slots = slots;
            fireTableDataChanged();
        }

        TimeSlot getSlotAt(int row) {
            return row >= 0 && row < slots.size() ? slots.get(row) : null;
        }

        @Override public int getRowCount() { return slots.size(); }
        @Override public int getColumnCount() { return 3; }

        @Override
        public String getColumnName(int column) {
            return new String[] {"Date", "Time", "Status"}[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            TimeSlot s = slots.get(row);
            switch (column) {
                case 0: return s.toString().substring(0, s.toString().indexOf(','));
                case 1: return s.getStartTime() + " - " + s.getEndTime();
                default: return s.isReserved() ? "RESERVED" : "FREE";
            }
        }
    }
}
