package peertutoring.controller;

import peertutoring.data.DataStore;
import peertutoring.model.Student;
import peertutoring.model.Tutor;
import peertutoring.model.User;

import java.io.IOException;
import java.util.List;

//Handles login and creating a new account
public class AuthController {

    private final DataStore store;

    public AuthController(DataStore store) {
        this.store = store;
    }

    public User login(String userId, String password) throws ValidationException {
        User user = store.findUser(userId.trim());
        if (user == null || !user.login(password)) {
            throw new ValidationException("Incorrect ID or password. Please try again.");
        }
        if (!user.isActive()) {
            throw new ValidationException("This account is deactivated. Please contact the admin.");
        }
        return user;
    }

    public User register(String userId, String name, String email, String major, String password,
                         String role, List<String[]> courses) throws ValidationException, IOException {
        userId = userId.trim();
        name = name.trim();
        email = email.trim();
        major = major.trim();

        if (userId.isEmpty() || name.isEmpty() || email.isEmpty() || major.isEmpty() || password.isEmpty()) {
            throw new ValidationException("All fields are required.");
        }
        if (!userId.matches("\\d{4,12}")) {
            throw new ValidationException("The university ID must contain only digits (4 to 12).");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new ValidationException("Please enter a valid email address.");
        }
        if (password.length() < 6) {
            throw new ValidationException("The password must be at least 6 characters long.");
        }
        if (containsSeparator(name, email, major)) {
            throw new ValidationException("The character | is not allowed.");
        }
        if (store.findUser(userId) != null) {
            throw new ValidationException("This ID already has an account.");
        }

        User user;
        String hash = User.hash(password);
        if (role.equals("TUTOR")) {
            if (courses == null || courses.isEmpty()) {
                throw new ValidationException("A tutor must add at least one course.");
            }
            Tutor tutor = new Tutor(userId, name, email, major, hash, true, 0, 0);
            CourseController courseHelper = new CourseController(store);
            for (String[] c : courses) {
                tutor.addCourse(courseHelper.findOrCreate(c[0], c[1]).getCourseCode());
            }
            user = tutor;
        } else {
            user = new Student(userId, name, email, major, hash, true);
        }
        store.addUser(user);
        store.save();
        return user;
    }

    private boolean containsSeparator(String... values) {
        for (String v : values) {
            if (v.contains("|")) {
                return true;
            }
        }
        return false;
    }
}
