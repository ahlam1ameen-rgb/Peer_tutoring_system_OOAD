package peertutoring.data;

import peertutoring.model.Admin;
import peertutoring.model.Course;
import peertutoring.model.Feedback;
import peertutoring.model.Session;
import peertutoring.model.SessionStatus;
import peertutoring.model.Student;
import peertutoring.model.TimeSlot;
import peertutoring.model.Tutor;
import peertutoring.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DataStore {

    private static final String SEP = "|";
    private static final String SEP_REGEX = "\\|";

    private final Path folder;
    private final List<Course> courses = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private final List<TimeSlot> slots = new ArrayList<>();
    private final List<Session> sessions = new ArrayList<>();

    public DataStore(Path folder) {
        this.folder = folder;
    }


   // the admin account 
    public static final String ADMIN_ID = "900000001";
    public static final String ADMIN_PASSWORD = "admin123";
    
    //loading
    public void load() throws IOException {
        if (!Files.exists(folder.resolve("users.txt"))) {
            Files.createDirectories(folder);
            users.add(new Admin(ADMIN_ID, "Admin", "admin@aau.ac.ae", User.hash(ADMIN_PASSWORD), true));
            save();
            return;
        }
        courses.clear();
        users.clear();
        slots.clear();
        sessions.clear();

        for (String[] f : readLines("courses.txt")) {
            courses.add(new Course(f[0], f[1]));
        }
        for (String[] f : readLines("users.txt")) {
            users.add(readUser(f));
        }
        for (String[] f : readLines("slots.txt")) {
            TimeSlot slot = new TimeSlot(f[0], f[1], LocalDate.parse(f[2]),
                    LocalTime.parse(f[3]), LocalTime.parse(f[4]), Boolean.parseBoolean(f[5]));
            slots.add(slot);
            Tutor tutor = findTutor(slot.getTutorId());
            if (tutor != null) {
                tutor.addTimeSlot(slot);
            }
        }
        for (String[] f : readLines("sessions.txt")) {
            sessions.add(new Session(f[0], f[1], f[2], f[3], f[4],
                    SessionStatus.valueOf(f[5])));
        }
        for (String[] f : readLines("feedback.txt")) {
            Session session = findSession(f[0]);
            if (session != null) {
                session.setFeedback(new Feedback(Integer.parseInt(f[1]), f[2], LocalDate.parse(f[3])));
            }
        }
    }

    private User readUser(String[] f) {
        String role = f[0];
        boolean active = Boolean.parseBoolean(f[6]);
        if (role.equals("ADMIN")) {
            return new Admin(f[1], f[2], f[3], f[5], active);
        }
        if (role.equals("STUDENT")) {
            return new Student(f[1], f[2], f[3], f[4], f[5], active);
        }
        Tutor tutor = new Tutor(f[1], f[2], f[3], f[4], f[5], active,
                Double.parseDouble(f[8]), Integer.parseInt(f[9]));
        for (String code : f[7].split(";")) {
            if (!code.isBlank()) {
                tutor.addCourse(code);
            }
        }
        return tutor;
    }

    private List<String[]> readLines(String fileName) throws IOException {
        List<String[]> result = new ArrayList<>();
        Path file = folder.resolve(fileName);
        if (!Files.exists(file)) {
            return result;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (!line.isBlank()) {

                result.add(line.split(SEP_REGEX, -1));
            }
        }
        return result;
    }

    //saving

    public void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (Course c : courses) {
            lines.add(join(c.getCourseCode(), c.getCourseName()));
        }
        write("courses.txt", lines);

        lines = new ArrayList<>();
        for (User u : users) {
            String active = String.valueOf(u.isActive());
            if (u instanceof Tutor) {
                Tutor t = (Tutor) u;
                lines.add(join("TUTOR", t.getUserId(), t.getName(), t.getEmail(), t.getMajor(),
                        t.getPasswordHash(), active, String.join(";", t.getCourseCodes()),
                        String.valueOf(t.getAverageRating()), String.valueOf(t.getRatingCount())));
            } else {
                lines.add(join(u.getRole(), u.getUserId(), u.getName(), u.getEmail(), u.getMajor(),
                        u.getPasswordHash(), active));
            }
        }
        write("users.txt", lines);

        lines = new ArrayList<>();
        for (TimeSlot s : slots) {
            lines.add(join(s.getSlotId(), s.getTutorId(), s.getDate().toString(),
                    s.getStartTime().toString(), s.getEndTime().toString(), String.valueOf(s.isReserved())));
        }
        write("slots.txt", lines);

        lines = new ArrayList<>();
        List<String> feedbackLines = new ArrayList<>();
        for (Session s : sessions) {
            lines.add(join(s.getSessionId(), s.getStudentId(), s.getTutorId(), s.getCourseCode(),
                    s.getSlotId(), s.getStatus().name()));
            if (s.hasFeedback()) {
                Feedback f = s.getFeedback();
                feedbackLines.add(join(s.getSessionId(), String.valueOf(f.getRating()),
                        f.getComment(), f.getDate().toString()));
            }
        }
        write("sessions.txt", lines);
        write("feedback.txt", feedbackLines);
    }

    private void write(String fileName, List<String> lines) throws IOException {
        Files.write(folder.resolve(fileName), lines, StandardCharsets.UTF_8);
    }

    private static String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(SEP);
            }
            sb.append(fields[i].replace("|", "/").replace("\n", " ").replace("\r", " "));
        }
        return sb.toString();
    }

    //queries

    public List<Course> getCourses() { return courses; }
    public List<User> getUsers() { return users; }
    public List<Session> getSessions() { return sessions; }

    public void addCourse(Course course) { courses.add(course); }
    public void addUser(User user) { users.add(user); }
    public void addSession(Session session) { sessions.add(session); }

    public void addSlot(TimeSlot slot) {
        slots.add(slot);
        Tutor tutor = findTutor(slot.getTutorId());
        if (tutor != null) {
            tutor.addTimeSlot(slot);
        }
    }

    public void removeSlot(TimeSlot slot) {
        slots.remove(slot);
        Tutor tutor = findTutor(slot.getTutorId());
        if (tutor != null) {
            tutor.removeTimeSlot(slot);
        }
    }

    public User findUser(String userId) {
        for (User u : users) {
            if (u.getUserId().equals(userId)) {
                return u;
            }
        }
        return null;
    }

    public Tutor findTutor(String userId) {
        User u = findUser(userId);
        return (u instanceof Tutor) ? (Tutor) u : null;
    }

    public List<Tutor> getTutors() {
        List<Tutor> tutors = new ArrayList<>();
        for (User u : users) {
            if (u instanceof Tutor) {
                tutors.add((Tutor) u);
            }
        }
        return tutors;
    }

    public Course findCourse(String code) {
        for (Course c : courses) {
            if (c.getCourseCode().equals(code)) {
                return c;
            }
        }
        return null;
    }

    public TimeSlot findSlot(String slotId) {
        for (TimeSlot s : slots) {
            if (s.getSlotId().equals(slotId)) {
                return s;
            }
        }
        return null;
    }

    public Session findSession(String sessionId) {
        for (Session s : sessions) {
            if (s.getSessionId().equals(sessionId)) {
                return s;
            }
        }
        return null;
    }

    public List<Session> findSessionsOf(User user) {
        List<Session> result = new ArrayList<>();
        for (Session s : sessions) {
            if (s.getStudentId().equals(user.getUserId()) || s.getTutorId().equals(user.getUserId())) {
                result.add(s);
            }
        }
        return result;
    }

    public List<Session> findUpcomingSessionsOf(User user) {
        List<Session> result = new ArrayList<>();
        for (Session s : findSessionsOf(user)) {
            if (s.getStatus() == SessionStatus.PENDING || s.getStatus() == SessionStatus.ACCEPTED) {
                result.add(s);
            }
        }
        return result;
    }

    public List<Feedback> findFeedbackFor(Tutor tutor) {
        List<Feedback> result = new ArrayList<>();
        for (Session s : sessions) {
            if (s.getTutorId().equals(tutor.getUserId()) && s.hasFeedback()) {
                result.add(0, s.getFeedback());
            }
        }
        return result;
    }

    public String nextSlotId() {
        int max = 0;
        for (TimeSlot s : slots) {
            max = Math.max(max, Integer.parseInt(s.getSlotId().substring(2)));
        }
        return "SL" + (max + 1);
    }

    public String nextSessionId() {
        int max = 0;
        for (Session s : sessions) {
            max = Math.max(max, Integer.parseInt(s.getSessionId().substring(2)));
        }
        return "SE" + (max + 1);
    }
}
