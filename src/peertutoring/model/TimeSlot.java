package peertutoring.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeSlot {

    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("EEE d MMM yyyy");

    private final String slotId;
    private final String tutorId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private boolean reserved;

    public TimeSlot(String slotId, String tutorId, LocalDate date, LocalTime startTime,
                    LocalTime endTime, boolean reserved) {
        this.slotId = slotId;
        this.tutorId = tutorId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reserved = reserved;
    }

    public boolean overlaps(TimeSlot other) {
        return date.equals(other.date)
                && startTime.isBefore(other.endTime)
                && other.startTime.isBefore(endTime);
    }

    public boolean isFree() {
        return !reserved && getStart().isAfter(LocalDateTime.now());
    }

    public boolean hasEnded() {
        return LocalDateTime.of(date, endTime).isBefore(LocalDateTime.now());
    }

    public void reserve() { reserved = true; }
    public void release() { reserved = false; }

    public LocalDateTime getStart() { return LocalDateTime.of(date, startTime); }
    public String getSlotId() { return slotId; }
    public String getTutorId() { return tutorId; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public boolean isReserved() { return reserved; }

    @Override
    public String toString() {
        return date.format(DISPLAY_DATE) + ", " + startTime + " - " + endTime;
    }
}
