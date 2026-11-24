package peertutoring.model;

import java.time.LocalDate;

public class Feedback {

    private final int rating;
    private final String comment;
    private final LocalDate date;

    public Feedback(int rating, String comment, LocalDate date) {
        this.rating = rating;
        this.comment = comment;
        this.date = date;
    }

    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDate getDate() { return date; }
}
