package peertutoring.model;

public class Admin extends User {

    public Admin(String userId, String name, String email, String passwordHash, boolean active) {
        super(userId, name, email, "-", passwordHash, active);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
