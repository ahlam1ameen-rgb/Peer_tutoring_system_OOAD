package peertutoring.controller;

import peertutoring.data.DataStore;
import peertutoring.model.Course;
import peertutoring.model.Session;
import peertutoring.model.SessionStatus;
import peertutoring.model.Student;
import peertutoring.model.TimeSlot;
import peertutoring.model.Tutor;
import peertutoring.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//Coordinates searching booking and managing sessions 
public class BookingController {

    private final DataStore store;

    public BookingController(DataStore store) {
        this.store = store;
    }

    public List<Tutor> searchTutors(String text) {
        String query = text.trim().toLowerCase();
        String queryNoSpaces = query.replace(" ", "");
        List<Tutor> result = new ArrayList<>();
        for (Tutor tutor : store.getTutors()) {
            if (!tutor.isActive()) {
                continue;
            }
            if (query.isEmpty() || tutor.getName().toLowerCase().contains(query)) {
                result.add(tutor);
                continue;
            }
            for (String code : tutor.getCourseCodes()) {
                Course course = store.findCourse(code);
                boolean codeMatches = code.toLowerCase().contains(queryNoSpaces);
                boolean nameMatches = course != null && course.getCourseName().toLowerCase().contains(query);
                if (codeMatches || nameMatches) {
                    result.add(tutor);
                    break;
                }
            }
        }
        sortByRating(result); 
        return result;
    }

    public List<TimeSlot> getAvailableSlots(Tutor tutor) {
        List<TimeSlot> result = new ArrayList<>();
        for (TimeSlot slot : tutor.getTimeSlots()) {
            if (slot.isFree()) {
                result.add(slot);
            }
        }
        sortSlotsByTime(result);
        return result;
    }

    public Session bookSession(Student student, Tutor tutor, String courseCode, TimeSlot slot)
            throws ValidationException, IOException {
        if (slot == null) {
            throw new ValidationException("Please choose a time slot.");
        }
        if (courseCode == null || !tutor.teaches(courseCode)) {
            throw new ValidationException("Please choose a course this tutor offers.");
        }
        if (!tutor.isAvailable(slot)) {
            throw new ValidationException("This time slot is no longer available.");
        }
        Session session = new Session(store.nextSessionId(), student.getUserId(), tutor.getUserId(),
                courseCode, slot.getSlotId(), SessionStatus.PENDING);
        slot.reserve();
        store.addSession(session);
        store.save();
        return session;
    }

    public void acceptSession(Tutor tutor, Session session) throws ValidationException, IOException {
        checkOwner(tutor, session);
        try {
            session.accept();
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
        store.save();
    }

    public void rejectSession(Tutor tutor, Session session) throws ValidationException, IOException {
        checkOwner(tutor, session);
        try {
            session.reject();
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
        releaseSlot(session);
        store.save();
    }

    public void cancelSession(User user, Session session) throws ValidationException, IOException {
        checkOwner(user, session);
        if (user instanceof Tutor && session.getStatus() == SessionStatus.PENDING) {
            throw new ValidationException("Use Reject for pending requests.");
        }
        try {
            session.cancel();
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
        releaseSlot(session);
        store.save();
    }

    public void completeSession(Tutor tutor, Session session) throws ValidationException, IOException {
        checkOwner(tutor, session);
        TimeSlot slot = store.findSlot(session.getSlotId());
        if (slot != null && !slot.hasEnded()) {
            throw new ValidationException("A session can only be completed after it has taken place.");
        }
        try {
            session.complete();
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
        store.save();
    }

    public List<Session> getSessionsOf(User user) {
        List<Session> list = store.findSessionsOf(user);

        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size() - 1 - i; j++) {
                if (slotStart(list.get(j)).isBefore(slotStart(list.get(j + 1)))) {
                    Session tmp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, tmp);
                }
            }
        }
        return list;
    }

    private void checkOwner(User user, Session session) throws ValidationException {
        boolean owner = session.getStudentId().equals(user.getUserId())
                || session.getTutorId().equals(user.getUserId());
        if (!owner) {
            throw new ValidationException("You can only change your own sessions.");
        }
    }

    private void releaseSlot(Session session) {
        TimeSlot slot = store.findSlot(session.getSlotId());
        if (slot != null) {
            slot.release();
        }
    }

    private void sortByRating(List<Tutor> tutors) {
        for (int i = 0; i < tutors.size(); i++) {
            for (int j = 0; j < tutors.size() - 1 - i; j++) {
                if (tutors.get(j).getAverageRating() < tutors.get(j + 1).getAverageRating()) {
                    Tutor tmp = tutors.get(j);
                    tutors.set(j, tutors.get(j + 1));
                    tutors.set(j + 1, tmp);
                }
            }
        }
    }

    static void sortSlotsByTime(List<TimeSlot> slots) {
        for (int i = 0; i < slots.size(); i++) {
            for (int j = 0; j < slots.size() - 1 - i; j++) {
                if (slots.get(j).getStart().isAfter(slots.get(j + 1).getStart())) {
                    TimeSlot tmp = slots.get(j);
                    slots.set(j, slots.get(j + 1));
                    slots.set(j + 1, tmp);
                }
            }
        }
    }

    private java.time.LocalDateTime slotStart(Session s) {
        TimeSlot slot = store.findSlot(s.getSlotId());
        return slot == null ? java.time.LocalDateTime.MIN : slot.getStart();
    }
}
