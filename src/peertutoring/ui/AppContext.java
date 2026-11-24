package peertutoring.ui;

import peertutoring.controller.AdminController;
import peertutoring.controller.AuthController;
import peertutoring.controller.AvailabilityController;
import peertutoring.controller.BookingController;
import peertutoring.controller.CourseController;
import peertutoring.controller.FeedbackController;
import peertutoring.data.DataStore;

// Holds the data store and the controllers 
// so every screen can reach them without creating new copies.
public class AppContext {

    public final DataStore store;
    public final AuthController auth;
    public final BookingController booking;
    public final FeedbackController feedback;
    public final AvailabilityController availability;
    public final CourseController courses;
    public final AdminController admin;

    public AppContext(DataStore store) {
        this.store = store;
        this.auth = new AuthController(store);
        this.booking = new BookingController(store);
        this.feedback = new FeedbackController(store);
        this.availability = new AvailabilityController(store);
        this.courses = new CourseController(store);
        this.admin = new AdminController(store);
    }
}
