package peertutoring.ui;

import peertutoring.controller.ValidationException;
import peertutoring.model.Course;
import peertutoring.model.Tutor;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;

// my courses and availability tutor screen.

public class CoursesAvailabilityPanel extends JPanel {

    private final AppContext app;
    private final Tutor tutor;
    private final StatusBar status;
    private final JTextField codeField = new JTextField(8);
    private final JTextField nameField = new JTextField(20);
    private final DefaultListModel<Course> courseModel = new DefaultListModel<>();
    private final JList<Course> courseList = new JList<>(courseModel);
    private final JButton removeButton = Theme.button("Remove Course", Theme.ButtonKind.DANGER);
    private final AvailabilityPanel slots;

    public CoursesAvailabilityPanel(AppContext app, Tutor tutor, StatusBar status) {
        super(new BorderLayout());
        this.app = app;
        this.tutor = tutor;
        this.status = status;
        this.slots = new AvailabilityPanel(app, tutor, status);

        // My courses card
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(Theme.cardBorder());

        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        addRow.setOpaque(false);
        addRow.add(Theme.label("My courses", Theme.BOLD, Color.BLACK));
        addRow.add(Theme.label("   Code:", Theme.SMALL, Theme.TEXT_GREY));
        codeField.setPreferredSize(new Dimension(90, 32));
        addRow.add(codeField);
        addRow.add(Theme.label("Name:", Theme.SMALL, Theme.TEXT_GREY));
        nameField.setPreferredSize(new Dimension(230, 32));
        addRow.add(nameField);
        JButton addButton = Theme.button("Add Course", Theme.ButtonKind.PRIMARY);
        addButton.addActionListener(e -> addCourse());
        nameField.addActionListener(e -> addCourse());
        addRow.add(addButton);
        card.add(addRow, BorderLayout.NORTH);

        courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseList.setVisibleRowCount(3);
        courseList.setFont(Theme.NORMAL);
        courseList.setSelectionBackground(Theme.GREEN_LIGHT);
        courseList.setSelectionForeground(Color.BLACK);
        courseList.addListSelectionListener(e -> removeButton.setEnabled(courseList.getSelectedValue() != null));
        JScrollPane listScroll = new JScrollPane(courseList);
        listScroll.setPreferredSize(new Dimension(0, 74));
        card.add(listScroll, BorderLayout.CENTER);

        removeButton.addActionListener(e -> removeCourse());
        JPanel removeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        removeRow.setOpaque(false);
        removeRow.add(Theme.label("A course with upcoming sessions cannot be removed.", Theme.SMALL, Theme.TEXT_GREY));
        removeRow.add(removeButton);
        card.add(removeRow, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(14, 20, 0, 20));
        top.add(card, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(slots, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        courseModel.clear();
        for (Course c : app.courses.getCoursesOf(tutor)) {
            courseModel.addElement(c);
        }
        removeButton.setEnabled(false);
        slots.refresh();
    }

    private void addCourse() {
        try {
            Course course = app.courses.addCourse(tutor, codeField.getText(), nameField.getText());
            status.success("Course added: " + course);
            codeField.setText("");
            nameField.setText("");
        } catch (ValidationException ex) {
            status.error(ex.getMessage());
        } catch (IOException ex) {
            status.error("Could not save: " + ex.getMessage());
        }
        refresh();
    }

    private void removeCourse() {
        Course course = courseList.getSelectedValue();
        if (course == null) {
            return;
        }
        try {
            app.courses.removeCourse(tutor, course);
            status.success("Course removed: " + course);
        } catch (ValidationException ex) {
            status.error(ex.getMessage());
        } catch (IOException ex) {
            status.error("Could not save: " + ex.getMessage());
        }
        refresh();
    }
}
