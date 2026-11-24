package peertutoring.model;

import java.util.ArrayList;
import java.util.List;


public class Tutor extends User {

    private final List<String> courseCodes = new ArrayList<>(); 
    private final List<TimeSlot> timeSlots = new ArrayList<>();  
    private double averageRating;
    private int ratingCount;

    public Tutor(String userId, String name, String email, String major, String passwordHash, boolean active,
                 double averageRating, int ratingCount) {
        super(userId, name, email, major, passwordHash, active);
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
    }

    @Override
    public String getRole() {
        return "TUTOR";
    }

    public boolean teaches(String courseCode) {
        return courseCodes.contains(courseCode);
    }

    public void addCourse(String courseCode) {
        if (!courseCodes.contains(courseCode)) {
            courseCodes.add(courseCode);
        }
    }

    public void removeCourse(String courseCode) {
        courseCodes.remove(courseCode);
    }

    public boolean isAvailable(TimeSlot slot) {
        return timeSlots.contains(slot) && slot.isFree();
    }

    public void addTimeSlot(TimeSlot slot) {
        timeSlots.add(slot);
    }

    public void removeTimeSlot(TimeSlot slot) {
        timeSlots.remove(slot);
    }

    public void updateRating(int rating) {
        double total = averageRating * ratingCount + rating;
        ratingCount++;
        averageRating = total / ratingCount;
    }

    public List<String> getCourseCodes() { return courseCodes; }
    public List<TimeSlot> getTimeSlots() { return timeSlots; }
    public double getAverageRating() { return averageRating; }
    public int getRatingCount() { return ratingCount; }
}
