package peertutoring.model;

public class Student extends User {

    public Student(String userId, String name, String email, String major, String passwordHash, boolean active) {
        super(userId, name, email, major, passwordHash, active);
    }

    @Override
    public String getRole() {
        return "STUDENT";
    }
}
