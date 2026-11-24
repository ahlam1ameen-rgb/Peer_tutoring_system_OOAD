package peertutoring.controller;

import peertutoring.data.DataStore;
import peertutoring.model.Feedback;
import peertutoring.model.Session;
import peertutoring.model.Student;
import peertutoring.model.Tutor;

import java.io.IOException;
import java.time.LocalDate;

// Handles rating tutors 
public class FeedbackController {

    private final DataStore store;

    public FeedbackController(DataStore store) {
        this.store = store;
    }

    public boolean canRate(Student student, Session session) {
        return session.getStudentId().equals(student.getUserId()) && session.canBeRated();
    }

    public void submitFeedback(Student student, Session session, int rating, String comment)
            throws ValidationException, IOException {
        if (!canRate(student, session)) {
            throw new ValidationException("This session cannot be rated.");
        }
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Please choose a rating from 1 to 5 stars.");
        }
        Feedback feedback = new Feedback(rating, comment.trim(), LocalDate.now());
        session.setFeedback(feedback);
        Tutor tutor = store.findTutor(session.getTutorId());
        tutor.updateRating(rating);
        store.save();
    }
}
