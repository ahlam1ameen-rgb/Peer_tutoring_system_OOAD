package peertutoring.controller;

import peertutoring.data.DataStore;
import peertutoring.model.TimeSlot;
import peertutoring.model.Tutor;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

// lets tutors manage availability

public class AvailabilityController {

    private final DataStore store;

    public AvailabilityController(DataStore store) {
        this.store = store;
    }

    public List<TimeSlot> getUpcomingSlots(Tutor tutor) {
        List<TimeSlot> result = new ArrayList<>();
        for (TimeSlot slot : tutor.getTimeSlots()) {
            if (!slot.getDate().isBefore(LocalDate.now())) {
                result.add(slot);
            }
        }
        BookingController.sortSlotsByTime(result);
        return result;
    }


    public TimeSlot addSlot(Tutor tutor, String dateText, String startText, String endText)
            throws ValidationException, IOException {
        LocalDate date;
        LocalTime start;
        LocalTime end;
        try {
            date = LocalDate.parse(dateText.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException("Please write the date as yyyy-MM-dd, for example 2026-11-22.");
        }
        try {
            start = LocalTime.parse(startText.trim());
            end = LocalTime.parse(endText.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException("Please write the times as HH:mm, for example 10:00.");
        }
        if (!end.isAfter(start)) {
            throw new ValidationException("The end time must be after the start time.");
        }
        if (!LocalDateTime.of(date, start).isAfter(LocalDateTime.now())) {
            throw new ValidationException("The time slot must be in the future.");
        }
        TimeSlot slot = new TimeSlot(store.nextSlotId(), tutor.getUserId(), date, start, end, false);
        for (TimeSlot existing : tutor.getTimeSlots()) {
            if (existing.overlaps(slot)) {
                throw new ValidationException("This time overlaps another slot (" + existing + ").");
            }
        }
        store.addSlot(slot);
        store.save();
        return slot;
    }

    public void removeSlot(Tutor tutor, TimeSlot slot) throws ValidationException, IOException {
        if (!slot.getTutorId().equals(tutor.getUserId())) {
            throw new ValidationException("You can only remove your own time slots.");
        }
        if (slot.isReserved()) {
            throw new ValidationException("This slot has a booking and cannot be removed.");
        }
        store.removeSlot(slot);
        store.save();
    }
}
