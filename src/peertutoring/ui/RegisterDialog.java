package peertutoring.ui;

import peertutoring.controller.ValidationException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegisterDialog extends JDialog {

    private final AppContext app;
    private final JTextField nameField = new JTextField();
    private final JTextField idField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField majorField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JRadioButton studentRadio = new JRadioButton("Student", true);
    private final JRadioButton tutorRadio = new JRadioButton("Tutor");
    private final JTextField courseCodeField = new JTextField(7);
    private final JTextField courseNameField = new JTextField(16);
    private final List<String[]> courses = new ArrayList<>(); 
    private final DefaultListModel<String> courseListModel = new DefaultListModel<>();
    private final JList<String> courseList = new JList<>(courseListModel);
    private final JPanel coursesPanel = new JPanel(new BorderLayout(0, 6));
    private final JLabel errorLabel = Theme.label(" ", Theme.SMALL, Theme.RED);

    public RegisterDialog(JFrame owner, AppContext app) {
        super(owner, "Create an account", true);
        this.app = app;

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setOpaque(false);
        top.add(Theme.label("Create an account", Theme.TITLE, Color.BLACK));
        top.add(Theme.label("All fields are required", Theme.NORMAL, Theme.TEXT_GREY));
        root.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 4));
        form.setOpaque(false);
        ButtonGroup roles = new ButtonGroup();
        roles.add(studentRadio);
        roles.add(tutorRadio);
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rolePanel.setOpaque(false);
        studentRadio.setOpaque(false);
        tutorRadio.setOpaque(false);
        rolePanel.add(studentRadio);
        rolePanel.add(tutorRadio);

        addField(form, "Full name", nameField, "University ID", idField);
        addField(form, "Email", emailField, "Major", majorField);
        addField(form, "Password (6+ characters)", passwordField, "Role", rolePanel);

        // Course fields only for tutors
        coursesPanel.setBackground(Color.WHITE);
        coursesPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Courses I can tutor (at least one)"),
                BorderFactory.createEmptyBorder(2, 6, 4, 6)));
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        addRow.setOpaque(false);
        addRow.add(Theme.label("Code", Theme.SMALL, Theme.TEXT_GREY));
        addRow.add(courseCodeField);
        addRow.add(Theme.label("Name", Theme.SMALL, Theme.TEXT_GREY));
        addRow.add(courseNameField);
        JButton addCourse = Theme.button("Add", Theme.ButtonKind.SECONDARY);
        JButton removeCourse = Theme.button("Remove", Theme.ButtonKind.DANGER);
        addCourse.addActionListener(e -> addCourse());
        courseNameField.addActionListener(e -> addCourse());
        removeCourse.addActionListener(e -> removeCourse());
        addRow.add(addCourse);
        addRow.add(removeCourse);
        courseList.setVisibleRowCount(3);
        JScrollPane listScroll = new JScrollPane(courseList);
        listScroll.setPreferredSize(new Dimension(0, 64));
        coursesPanel.add(addRow, BorderLayout.NORTH);
        coursesPanel.add(listScroll, BorderLayout.CENTER);
        coursesPanel.add(Theme.label("Example: CSC201 - Object-Oriented Programming", Theme.SMALL, Theme.TEXT_GREY),
                BorderLayout.SOUTH);
        coursesPanel.setVisible(false);
        studentRadio.addActionListener(e -> showCourses(false));
        tutorRadio.addActionListener(e -> showCourses(true));

        JPanel middle = new JPanel(new BorderLayout(0, 10));
        middle.setOpaque(false);
        middle.add(form, BorderLayout.NORTH);
        middle.add(coursesPanel, BorderLayout.CENTER);
        root.add(middle, BorderLayout.CENTER);

        JButton cancel = Theme.button("Cancel", Theme.ButtonKind.SECONDARY);
        JButton create = Theme.button("Create Account", Theme.ButtonKind.PRIMARY);
        cancel.addActionListener(e -> dispose());
        create.addActionListener(e -> register());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(create);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(errorLabel, BorderLayout.WEST);
        bottom.add(buttons, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setSize(620, getHeight());
        setLocationRelativeTo(owner);
    }

    private void addField(JPanel form, String label1, JComponent field1, String label2, JComponent field2) {
        form.add(Theme.label(label1, Theme.BOLD, Theme.TEXT_GREY));
        form.add(Theme.label(label2, Theme.BOLD, Theme.TEXT_GREY));
        form.add(field1);
        form.add(field2);
    }

    private void showCourses(boolean visible) {
        coursesPanel.setVisible(visible);
        pack();
        setSize(620, getHeight());
    }

    private void addCourse() {
        String code = courseCodeField.getText().replace(" ", "").toUpperCase();
        String name = courseNameField.getText().trim();
        if (code.isEmpty() || name.isEmpty()) {
            errorLabel.setText("⚠ Type the course code and the course name.");
            return;
        }
        for (String[] c : courses) {
            if (c[0].equals(code)) {
                errorLabel.setText("⚠ " + code + " is already in the list.");
                return;
            }
        }
        courses.add(new String[] {code, name});
        courseListModel.addElement(code + " - " + name);
        courseCodeField.setText("");
        courseNameField.setText("");
        errorLabel.setText(" ");
    }

    private void removeCourse() {
        int i = courseList.getSelectedIndex();
        if (i >= 0) {
            courses.remove(i);
            courseListModel.remove(i);
        }
    }

    private void register() {
        String role = tutorRadio.isSelected() ? "TUTOR" : "STUDENT";
        try {
            app.auth.register(idField.getText(), nameField.getText(), emailField.getText(),
                    majorField.getText(), new String(passwordField.getPassword()), role, courses);
            JOptionPane.showMessageDialog(this, "Account created, you can now log in.",
                    "Account created", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (ValidationException ex) {
            errorLabel.setText("⚠ " + ex.getMessage());
        } catch (IOException ex) {
            errorLabel.setText("⚠ Could not save the account: " + ex.getMessage());
        }
    }
}
