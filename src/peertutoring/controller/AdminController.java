package peertutoring.controller;

import peertutoring.data.DataStore;
import peertutoring.model.Admin;
import peertutoring.model.Session;
import peertutoring.model.TimeSlot;
import peertutoring.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// lets the admin manage User accounts

public class AdminController {

    private final DataStore store;

    public AdminController(DataStore store) {
        this.store = store;
    }


  public List<User> getAccounts() {
        List<User> result = new ArrayList<>();
        for (User u : store.getUsers()) {
            if (!(u instanceof Admin)) {
                result.add(u);
            }
        }

        for (int i = 0; i < result.size(); i++) {
            for (int j = 0; j < result.size() - 1 - i; j++) {
                User a = result.get(j);
                User b = result.get(j + 1);
                int byRole = a.getRole().compareTo(b.getRole());
                if (byRole > 0 || (byRole == 0 && a.getName().compareTo(b.getName()) > 0)) {
                    result.set(j, b);
                    result.set(j + 1, a);
                }
            }
        }
        return result;
    }

    public int deactivateUser(User user) throws ValidationException, IOException {
        if (user instanceof Admin) {
            throw new ValidationException("Admin accounts cannot be deactivated here.");
        }
        if (!user.isActive()) {
            throw new ValidationException(user.getName() + " is already deactivated.");
        }
        user.deactivate();
        List<Session> upcoming = store.findUpcomingSessionsOf(user);
        for (Session session : upcoming) {
            session.cancel();
            TimeSlot slot = store.findSlot(session.getSlotId());
            if (slot != null) {
                slot.release();
            }
        }
        store.save();
        return upcoming.size();
    }

    public void reactivateUser(User user) throws ValidationException, IOException {
        if (user.isActive()) {
            throw new ValidationException(user.getName() + " is already active.");
        }
        user.reactivate();
        store.save();
    }
}
