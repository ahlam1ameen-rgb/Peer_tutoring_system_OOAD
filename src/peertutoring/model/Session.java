package peertutoring.model;

public class Session {

    private final String sessionId;
    private final String studentId;
    private final String tutorId;
    private final String courseCode;
    private final String slotId;
    private SessionStatus status;
    private Feedback feedback; 

    public Session(String sessionId, String studentId, String tutorId, String courseCode,
                   String slotId, SessionStatus status) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.tutorId = tutorId;
        this.courseCode = courseCode;
        this.slotId = slotId;
        this.status = status;
    }

    public void accept() {
        requireStatus(SessionStatus.PENDING, "Only pending requests can be accepted.");
        status = SessionStatus.ACCEPTED;
    }

    public void reject() {
        requireStatus(SessionStatus.PENDING, "Only pending requests can be rejected.");
        status = SessionStatus.REJECTED;
    }

    public void cancel() {
        if (status != SessionStatus.PENDING && status != SessionStatus.ACCEPTED) {
            throw new IllegalStateException("Only pending or accepted sessions can be cancelled.");
        }
        status = SessionStatus.CANCELLED;
    }

    public void complete() {
        requireStatus(SessionStatus.ACCEPTED, "Only accepted sessions can be marked completed.");
        status = SessionStatus.COMPLETED;
    }

    public boolean canBeRated() {
        return status == SessionStatus.COMPLETED && !hasFeedback();
    }

    public boolean hasFeedback() {
        return feedback != null;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    private void requireStatus(SessionStatus expected, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
    }

    public String getSessionId() { return sessionId; }
    public String getStudentId() { return studentId; }
    public String getTutorId() { return tutorId; }
    public String getCourseCode() { return courseCode; }
    public String getSlotId() { return slotId; }
    public SessionStatus getStatus() { return status; }
    public Feedback getFeedback() { return feedback; }
}