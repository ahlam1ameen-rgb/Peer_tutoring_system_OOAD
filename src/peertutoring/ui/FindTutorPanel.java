package peertutoring.ui;

import peertutoring.controller.ValidationException;
import peertutoring.model.Course;
import peertutoring.model.Feedback;
import peertutoring.model.Student;
import peertutoring.model.TimeSlot;
import peertutoring.model.Tutor;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.List;

public class FindTutorPanel extends JPanel {

    private final AppContext app;
    private final Student student;
    private final StatusBar status;

    private final JTextField searchField = new JTextField(24);
    private final DefaultListModel<Tutor> resultModel = new DefaultListModel<>();
    private final JList<Tutor> resultList = new JList<>(resultModel);
    private final JLabel resultCount = Theme.label(" ", Theme.SMALL, Theme.TEXT_GREY);
    private final JPanel profilePanel = new JPanel(new BorderLayout());

    private TimeSlot selectedSlot;

    public FindTutorPanel(AppContext app, Student student, StatusBar status) {
        super(new BorderLayout(0, 12));
        this.app = app;
        this.student = student;
        this.status = status;
        setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JButton searchButton = Theme.button("Search", Theme.ButtonKind.PRIMARY);
        searchButton.addActionListener(e -> search());
        searchField.addActionListener(e -> search());
        searchField.setPreferredSize(new Dimension(380, 34));
        searchField.setToolTipText("Tutor name, course code, or course name");
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.add(Theme.label("Search:", Theme.BOLD, Theme.TEXT_GREY));
        searchBar.add(searchField);
        searchBar.add(searchButton);
        searchBar.add(Box.createHorizontalStrut(12));
        searchBar.add(resultCount);
        add(searchBar, BorderLayout.NORTH);

        resultList.setCellRenderer(new TutorCardRenderer());
        resultList.setFixedCellHeight(66);
        resultList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showProfile(resultList.getSelectedValue());
            }
        });
        JScrollPane listScroll = new JScrollPane(resultList);
        listScroll.setPreferredSize(new Dimension(330, 0));
        listScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

     
        profilePanel.setBackground(Color.WHITE);
        profilePanel.setBorder(Theme.cardBorder());

        JPanel center = new JPanel(new BorderLayout(14, 0));
        center.add(listScroll, BorderLayout.WEST);
        center.add(profilePanel, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        search(); 
    }

    private void search() {
        List<Tutor> tutors = app.booking.searchTutors(searchField.getText());
        resultModel.clear();
        for (Tutor t : tutors) {
            resultModel.addElement(t);
        }
        if (tutors.isEmpty()) {
            resultCount.setText("No tutors found");
            showProfile(null);
        } else {
            resultCount.setText(tutors.size() + (tutors.size() == 1 ? " tutor found" : " tutors found"));
            resultList.setSelectedIndex(0);
        }
    }

    public void refresh() {
        showProfile(resultList.getSelectedValue());
    }

    // Builds the profile view of a tutor 
    private void showProfile(Tutor tutor) {
        profilePanel.removeAll();
        selectedSlot = null;
        if (tutor == null) {
            profilePanel.add(Theme.label("Select a tutor to see their profile.", Theme.NORMAL, Theme.TEXT_GREY),
                    BorderLayout.NORTH);
            profilePanel.revalidate();
            profilePanel.repaint();
            return;
        }

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        info.add(Theme.label(tutor.getName(), new Font("SansSerif", Font.BOLD, 18), Color.BLACK));
        info.add(Theme.label(tutor.getMajor(), Theme.NORMAL, Theme.TEXT_GREY));
        info.add(Box.createVerticalStrut(4));
        info.add(Theme.label(Theme.stars(tutor.getAverageRating(), tutor.getRatingCount()),
                new Font("SansSerif", Font.PLAIN, 15), Theme.STAR));
        info.add(Box.createVerticalStrut(10));

        info.add(Theme.label("Courses", Theme.BOLD, Theme.TEXT_GREY));
        StringBuilder courses = new StringBuilder("<html>");
        for (String code : tutor.getCourseCodes()) {
            Course c = app.store.findCourse(code);
            courses.append("• ").append(c == null ? code : c.toString()).append("<br>");
        }
        info.add(Theme.label(courses + "</html>", Theme.NORMAL, Color.BLACK));
        info.add(Box.createVerticalStrut(6));

        // the two most recent comments
        int shown = 0;
        for (Feedback f : app.store.findFeedbackFor(tutor)) {
            if (!f.getComment().isEmpty() && shown < 2) {
                info.add(Theme.label("<html><i>“" + f.getComment() + "”</i></html>",
                        Theme.NORMAL, new Color(0x44, 0x44, 0x44)));
                shown++;
            }
        }
        info.add(Box.createVerticalStrut(12));

        // free time slots as toggle buttons
        info.add(Theme.label("Available time slots", Theme.BOLD, Theme.TEXT_GREY));
        info.add(Box.createVerticalStrut(6));
        List<TimeSlot> free = app.booking.getAvailableSlots(tutor);
        JPanel slotGrid = new JPanel(new GridLayout(0, 2, 8, 8));
        slotGrid.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (TimeSlot slot : free) {
            JToggleButton b = new JToggleButton(slot.toString());
            styleSlotButton(b, false);
            b.addItemListener(e -> {
                styleSlotButton(b, b.isSelected());
                if (b.isSelected()) {
                    selectedSlot = slot;
                }
            });
            group.add(b);
            slotGrid.add(b);
        }
        if (free.isEmpty()) {
            slotGrid.add(Theme.label("No free time slots at the moment.", Theme.NORMAL, Theme.TEXT_GREY));
        }
        slotGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        slotGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, slotGrid.getPreferredSize().height));
        info.add(slotGrid);
        info.add(Box.createVerticalGlue());

        for (Component c : info.getComponents()) {
            ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        JComboBox<Course> courseBox = new JComboBox<>();
        for (String code : tutor.getCourseCodes()) {
            Course c = app.store.findCourse(code);
            if (c != null) {
                courseBox.addItem(c);
            }
        }
        JButton book = Theme.button("Book Session", Theme.ButtonKind.PRIMARY);
        book.setEnabled(!free.isEmpty());
        book.addActionListener(e -> book(tutor, (Course) courseBox.getSelectedItem()));
        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setOpaque(false);
        JPanel coursePick = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        coursePick.setOpaque(false);
        coursePick.add(Theme.label("Course:", Theme.BOLD, Theme.TEXT_GREY));
        coursePick.add(courseBox);
        bottom.add(coursePick, BorderLayout.WEST);
        bottom.add(book, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(info);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        profilePanel.add(scroll, BorderLayout.CENTER);
        profilePanel.add(bottom, BorderLayout.SOUTH);
        profilePanel.revalidate();
        profilePanel.repaint();
    }

    private void book(Tutor tutor, Course course) {
        try {
            app.booking.bookSession(student, tutor, course == null ? null : course.getCourseCode(), selectedSlot);
            status.success("Request sent to " + tutor.getName() + ". You can follow it in My Sessions.");
            showProfile(tutor);
        } catch (ValidationException ex) {
            status.error(ex.getMessage());
            showProfile(tutor);
        } catch (IOException ex) {
            status.error("Could not save: " + ex.getMessage());
        }
    }

    private void styleSlotButton(JToggleButton b, boolean selected) {
        b.setFont(Theme.SMALL);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(selected ? Theme.GREEN : Color.WHITE);
        b.setForeground(selected ? Color.WHITE : Color.BLACK);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(selected ? Theme.GREEN : new Color(0xBB, 0xBB, 0xBB)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    }

    // Draws each tutor in the result list as a small card. 
    private static class TutorCardRenderer extends JPanel implements ListCellRenderer<Tutor> {
        private final JLabel name = Theme.label("", Theme.BOLD, Color.BLACK);
        private final JLabel courses = Theme.label("", Theme.SMALL, Theme.TEXT_GREY);
        private final JLabel rating = Theme.label("", Theme.SMALL, Theme.STAR);

        TutorCardRenderer() {
            super(new BorderLayout());
            JPanel left = new JPanel(new GridLayout(2, 1));
            left.setOpaque(false);
            left.add(name);
            left.add(courses);
            add(left, BorderLayout.CENTER);
            add(rating, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Tutor> list, Tutor tutor, int index,
                                                      boolean selected, boolean focus) {
            name.setText(tutor.getName());
            courses.setText(String.join(" · ", tutor.getCourseCodes()));
            rating.setText(Theme.stars(tutor.getAverageRating(), tutor.getRatingCount()));
            setBackground(selected ? Theme.GREEN_LIGHT : Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, selected ? 4 : 0, 1, 0,
                            selected ? Theme.GREEN : new Color(0xEE, 0xEE, 0xEE)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            return this;
        }
    }
}
