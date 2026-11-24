package peertutoring.controller;

import peertutoring.data.DataStore;
import peertutoring.model.Course;
import peertutoring.model.Session;
import peertutoring.model.Tutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Lets tutors manage  Courses
public class CourseController {

    private final DataStore store;

    public CourseController(DataStore store) {
        this.store = store;
    }

    public List<Course> getCoursesOf(Tutor tutor) {
        List<Course> result = new ArrayList<>();
        for (String code : tutor.getCourseCodes()) {
            Course c = store.findCourse(code);
            result.add(c != null ? c : new Course(code, code));
        }
        return result;
    }

    public Course addCourse(Tutor tutor, String code, String name) throws ValidationException, IOException {
        Course course = findOrCreate(code, name);
        if (tutor.teaches(course.getCourseCode())) {
            throw new ValidationException("You already teach " + course.getCourseCode() + ".");
        }
        tutor.addCourse(course.getCourseCode());
        store.save();
        return course;
    }


    public void removeCourse(Tutor tutor, Course course) throws ValidationException, IOException {
        for (Session s : store.findUpcomingSessionsOf(tutor)) {
            if (s.getCourseCode().equals(course.getCourseCode())) {
                throw new ValidationException("You have upcoming sessions for this course, so it cannot be removed.");
            }
        }
        if (tutor.getCourseCodes().size() == 1) {
            throw new ValidationException("A tutor must teach at least one course.");
        }
        tutor.removeCourse(course.getCourseCode());
        store.save();
    }

    Course findOrCreate(String code, String name) throws ValidationException {
        code = code.replace(" ", "").toUpperCase();
        name = name.trim();
        if (code.isEmpty() || name.isEmpty()) {
            throw new ValidationException("Please type the course code and the course name.");
        }
        if (!code.matches("[A-Z]{2,5}\\d{3,4}")) {
            throw new ValidationException("The course code should look like CSC201.");
        }
        if (name.contains("|") || name.contains(";")) {
            throw new ValidationException("The characters | and ; are not allowed in the course name.");
        }
        Course existing = store.findCourse(code);
        if (existing != null) {
            return existing;
        }
        Course course = new Course(code, name);
        store.addCourse(course);
        return course;
    }
}
