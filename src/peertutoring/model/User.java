package peertutoring.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public abstract class User {

    private final String userId;      
    private final String name;
    private final String email;
    private final String major;
    private final String passwordHash;
    private boolean active;
    protected User(String userId, String name, String email, String major, String passwordHash, boolean active) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.major = major;
        this.passwordHash = passwordHash;
        this.active = active;
    }

    public boolean login(String password) {
        return passwordHash.equals(hash(password));
    }

    public void deactivate() { active = false; }
    public void reactivate() { active = true; }

    public abstract String getRole();

 //hashing
    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMajor() { return major; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isActive() { return active; }

    @Override
    public String toString() {
        return name;
    }
}
